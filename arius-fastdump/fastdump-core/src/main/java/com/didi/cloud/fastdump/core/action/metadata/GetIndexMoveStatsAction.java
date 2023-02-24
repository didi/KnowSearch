package com.didi.cloud.fastdump.core.action.metadata;

import static com.didi.cloud.fastdump.common.utils.BaseHttpUtil.buildHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.bean.stats.FailedShardInfoStatus;
import com.didi.cloud.fastdump.common.bean.stats.IndexMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;
import com.didi.cloud.fastdump.common.component.SpringTool;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;
import com.didi.cloud.fastdump.common.event.es.IndexMoveTaskSuccEvent;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.common.utils.BaseHttpUtil;
import com.didi.cloud.fastdump.common.utils.ConvertUtil;
import com.didi.cloud.fastdump.common.utils.RetryUtil;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/8
 */
@Component
public class GetIndexMoveStatsAction implements Action<String, IndexMoveTaskStats> {
    protected static final Logger         LOGGER     = LoggerFactory.getLogger(GetIndexMoveStatsAction.class);
    @Value("${fastdump.httpTransport.port:8300}")
    private int                           httpPort;
    @Autowired
    private IndexMoveTaskMetadata         indexMoveTaskMetadata;

    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("GetIndexMoveStatsAction-FutureUtil", 5, 5, 1000);

    @Override
    public IndexMoveTaskStats doAction(String taskId) throws Exception {
        // 成功任务直接返回
        if (indexMoveTaskMetadata.isTaskSucc(taskId)) {
            return  ConvertUtil.obj2ObjByJSON(indexMoveTaskMetadata.getSuccTaskStats(taskId), IndexMoveTaskStats.class);
        }

        List<String> ipList = indexMoveTaskMetadata.getTaskIpList(taskId);
        if (CollectionUtils.isEmpty(ipList)) {
            throw new BaseException(String.format("index move task[%s] is not exist", taskId), ResultType.ILLEGAL_PARAMS);
        }

        Map<String/*ip*/, IndexNodeMoveTaskStats> ip2IndexNodeMoveTaskStatsMap = new ConcurrentHashMap<>();
        for (String ip : ipList) {
            FUTURE_UTIL.runnableTask(() -> {
                Result result;
                try {
                    result = RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                            "GetIndexMoveStatsAction",
                            RetryUtil.DEFAULT_TIME,
                            RetryUtil.DEFAULT_INTERVAL_MILLS,
                            () -> {
                                String resp = BaseHttpUtil.get(buildHttpUrl(new String[] { ip, taskId }), null, buildHeader());
                                return ConvertUtil.str2ObjByJson(resp, Result.class);
                            }
                    );
                } catch (Exception e) {
                    LOGGER.error("class=GetIndexMoveStatsAction||ip={}||method=doAction||errMsg={}", ip,
                        e.getMessage(), e);
                    return;
                }

                if (result.getData() != null) {
                    IndexNodeMoveTaskStats indexNodeMoveTaskStats = ConvertUtil.obj2ObjByJSON(
                            result.getData(),
                            IndexNodeMoveTaskStats.class);
                    if (null != indexNodeMoveTaskStats) {
                        ip2IndexNodeMoveTaskStatsMap.put(ip, indexNodeMoveTaskStats);
                    }
                }
            });
        }

        FUTURE_UTIL.waitExecute();

        IndexMoveTaskStats indexMoveTaskStats = mergeStats(ip2IndexNodeMoveTaskStatsMap);
        if (null != indexMoveTaskStats && TaskStatusEnum.SUCCESS.getValue().equals(indexMoveTaskStats.getStatus())) {
            SpringTool.publish(new IndexMoveTaskSuccEvent(this, indexMoveTaskStats));
        }
        return indexMoveTaskStats;
    }

    private String buildHttpUrl(String[] args) {
        String ip = args[0], taskId = args[1];
        return "http://" + ip + ":" + httpPort + "/index-node-move/" + taskId + "/stats";
    }

    private IndexMoveTaskStats mergeStats(Map<String/*ip*/, IndexNodeMoveTaskStats> ip2IndexNodeMoveTaskStatsMap) {
        if (MapUtils.isEmpty(ip2IndexNodeMoveTaskStatsMap)) {
            return null;
        }

        IndexMoveTaskStats indexMoveTaskStats = new IndexMoveTaskStats();


        IndexNodeMoveTaskStats indexNodeMoveTaskStats = new ArrayList<>(ip2IndexNodeMoveTaskStatsMap.values()).get(0);

        indexMoveTaskStats.setTaskId(indexNodeMoveTaskStats.getTaskId());
        indexMoveTaskStats.setSourceIndex(indexNodeMoveTaskStats.getSourceIndex());
        indexMoveTaskStats.setSourceCluster(indexNodeMoveTaskStats.getSourceCluster());

        indexMoveTaskStats.setTargetIndex(indexNodeMoveTaskStats.getTargetIndex());
        indexMoveTaskStats.setTargetCluster(indexNodeMoveTaskStats.getTargetCluster());

        int size = ip2IndexNodeMoveTaskStatsMap.keySet().size();
        indexMoveTaskStats.setReadFileRateLimit(indexNodeMoveTaskStats.getReadFileRateLimit().get() * size);

        List<Integer> statusCodeList = new ArrayList<>();
        List<FailedShardInfoStatus> failedShardDataPathList = new ArrayList<>();

        long totalDocumentNum = 0L;
        long succDocumentNum = 0L;
        long failedDocumentNum = 0L;

        long costTime = 0L;

        int shardNum = 0;
        int succShardNum = 0;

        long totalReadFileRateLimit = 0L;
        long totalKernelEstimationReadFileRateLimit = 0L;

        boolean interruptMark = false;

        for (Map.Entry<String, IndexNodeMoveTaskStats> e : ip2IndexNodeMoveTaskStatsMap.entrySet()) {
            IndexNodeMoveTaskStats nodeMoveTaskStats = e.getValue();
            String ip = e.getKey();

            // 统计状态
            statusCodeList.add(nodeMoveTaskStats.getStatusCode());

            // 迁移异常详情
            if (StringUtils.isNotBlank(nodeMoveTaskStats.getDetail())
                || CollectionUtils.isNotEmpty(nodeMoveTaskStats.getFailedShardDataPath())) {
                FailedShardInfoStatus failedShardInfoStatus = new FailedShardInfoStatus();
                failedShardInfoStatus.setIp(ip);
                failedShardInfoStatus.setFailedShardDataPaths(
                    nodeMoveTaskStats.getFailedShardDataPath().stream().distinct().collect(Collectors.toList()));

                failedShardInfoStatus.setDetail("\n" + nodeMoveTaskStats.getDetail());

                failedShardDataPathList.add(failedShardInfoStatus);
            }

            // 文档信息
            AtomicLong succDocumentNumAtomicLong = nodeMoveTaskStats.getSuccDocumentNum();
            succDocumentNum   += succDocumentNumAtomicLong != null ? succDocumentNumAtomicLong.get() : 0;

            AtomicLong failedDocumentNumAtomicLong = nodeMoveTaskStats.getFailedDocumentNum();
            failedDocumentNum += failedDocumentNumAtomicLong != null ? failedDocumentNumAtomicLong.get() : 0;

            totalDocumentNum  += nodeMoveTaskStats.getTotalDocumentNum();

            AtomicLong readFileRateLimit = nodeMoveTaskStats.getReadFileRateLimit();
            totalReadFileRateLimit += readFileRateLimit != null ? readFileRateLimit.get() : 0;

            Long kernelEstimationReadFileRateLimit = nodeMoveTaskStats.getKernelEstimationReadFileRateLimit();
            totalKernelEstimationReadFileRateLimit += null != kernelEstimationReadFileRateLimit ? kernelEstimationReadFileRateLimit : 0;

            // 整个任务耗时
            Long singleCostTime = nodeMoveTaskStats.getCostTime();
            costTime = singleCostTime < costTime ? costTime : singleCostTime;

            // shard 信息
            shardNum += nodeMoveTaskStats.getShardNum();
            succShardNum += nodeMoveTaskStats.getSuccShardNum();

            // 是否被api中断
            if (nodeMoveTaskStats.isInterruptMark()) {
                interruptMark = true;
            }
        }

        TaskStatusEnum finalTaskStatusEnum = computeFinalStatusByCode(statusCodeList);
        indexMoveTaskStats.setStatus(finalTaskStatusEnum.getValue());

        indexMoveTaskStats.setFailedShardInfoStatus(failedShardDataPathList);

        indexMoveTaskStats.setSuccDocumentNum(succDocumentNum);
        indexMoveTaskStats.setFailedDocumentNum(failedDocumentNum);
        indexMoveTaskStats.setTotalDocumentNum(totalDocumentNum);
        indexMoveTaskStats.setCostTime(costTime);

        indexMoveTaskStats.setReadFileRateLimit(totalReadFileRateLimit);
        indexMoveTaskStats.setKernelEstimationReadFileRateLimit(totalKernelEstimationReadFileRateLimit);

        indexMoveTaskStats.setInterruptMark(interruptMark);


        indexMoveTaskStats.setShardNum(shardNum);
        indexMoveTaskStats.setSuccShardNum(succShardNum);
        return indexMoveTaskStats;
    }

    private TaskStatusEnum computeFinalStatusByCode(List<Integer> statusCodeList) {
        int total      = statusCodeList.size();
        int successNum = 0;
        int waitNum    = 0;
        int failedNum  = 0;
        int runningNum = 0;
        int pauseNum   = 0;

        for (int code : statusCodeList) {
            if (TaskStatusEnum.SUCCESS.getCode().equals(code)) {
                successNum++;
            }
            if (TaskStatusEnum.WAIT.getCode().equals(code)) {
                waitNum++;
            }
            if (TaskStatusEnum.PAUSE.getCode().equals(code)) {
                pauseNum++;
            }
            if (TaskStatusEnum.RUNNING.getCode().equals(code)) {
                runningNum++;
            }
            if (TaskStatusEnum.FAILED.getCode().equals(code)) {
                failedNum++;
            }
        }

        if (runningNum > 0) {
            return TaskStatusEnum.RUNNING;
        }
        if (failedNum > 0) {
            return TaskStatusEnum.FAILED;
        }
        if (pauseNum == total) {
            return TaskStatusEnum.PAUSE;
        }
        if (pauseNum > 0 && (pauseNum + successNum) == total) {
            return TaskStatusEnum.PAUSE;
        }
        if (successNum == total) {
            return TaskStatusEnum.SUCCESS;
        }
        if (waitNum == total) {
            return TaskStatusEnum.WAIT;
        }
        return TaskStatusEnum.UNKNOWN;
    }
}
