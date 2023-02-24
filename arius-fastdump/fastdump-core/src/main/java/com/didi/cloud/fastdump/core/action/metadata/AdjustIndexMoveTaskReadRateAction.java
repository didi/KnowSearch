package com.didi.cloud.fastdump.core.action.metadata;

import static com.didi.cloud.fastdump.common.utils.BaseHttpUtil.buildHeader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.bean.readrate.ReadFileRateInfo;
import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.exception.FastDumpOperateException;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.common.utils.BaseHttpUtil;
import com.didi.cloud.fastdump.common.utils.CommonUtils;
import com.didi.cloud.fastdump.common.utils.ConvertUtil;
import com.didi.cloud.fastdump.common.utils.RetryUtil;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/22
 * 限流调整机制 , 内核不同节点上的限流值在总限流值中的百分比
 */
@Component
public class AdjustIndexMoveTaskReadRateAction implements Action<ReadFileRateInfo, Boolean> {

    protected static final Logger         LOGGER      = LoggerFactory
        .getLogger(AdjustIndexMoveTaskReadRateAction.class);
    @Value("${fastdump.httpTransport.port:8300}")
    private int                           httpPort;

    @Value("${min.limit.read.rate.percent:0.005}")
    private double                        minLimitReadRatePercent;

    @Autowired
    private IndexMoveTaskMetadata         indexMoveTaskMetadata;

    @Autowired
    private GetIndexMoveStatsAction       getIndexMoveStatsAction;

    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("AdjustIndexMoveTaskReadRateAction-FutureUtil",
        5, 5, 1000);

    @Override
    public Boolean doAction(ReadFileRateInfo readFileRateInfo) throws Exception {
        // 1、参数校验
        String taskId                  = readFileRateInfo.getTaskId();
        double adjustReadFileRateLimit = readFileRateInfo.getReadFileRateLimit();
        if (indexMoveTaskMetadata.isTaskSucc(taskId)) {
            throw new BaseException(String.format("index move task[%s] is succ", taskId), ResultType.ILLEGAL_PARAMS);
        }

        List<String> ipList = indexMoveTaskMetadata.getTaskIpList(taskId);
        if (CollectionUtils.isEmpty(ipList)) {
            throw new BaseException(String.format("index move task[%s] is not exist", taskId), ResultType.ILLEGAL_PARAMS);
        }

        Map<String, IndexNodeMoveTaskStats> ip2IndexNodeMoveTaskStatsMap = getIp2IndexNodeMoveTaskStatsMap(taskId, ipList);
        long kernelEstimationReadFileRateLimit = ip2IndexNodeMoveTaskStatsMap
                .values()
                .stream()
                .mapToLong(IndexNodeMoveTaskStats::getKernelEstimationReadFileRateLimit)
                .sum();
        if (adjustReadFileRateLimit > kernelEstimationReadFileRateLimit) {
            throw new BaseException(String.format("the index move task[%s] adjust readFileRateLimit is:%f > %d, please adjust < %d",
                    taskId,
                    adjustReadFileRateLimit,
                    kernelEstimationReadFileRateLimit,
                    kernelEstimationReadFileRateLimit),
                    ResultType.ILLEGAL_PARAMS);
        }

        double minReadFileRateLimit = CommonUtils.formatDouble(kernelEstimationReadFileRateLimit * minLimitReadRatePercent, 0);
        if (adjustReadFileRateLimit < minReadFileRateLimit) {
            throw new BaseException(String.format("the index move task[%s] adjust readFileRateLimit is:%f < %f, please adjust > %f",
                    taskId,
                    adjustReadFileRateLimit,
                    minReadFileRateLimit,
                    minReadFileRateLimit),
                    ResultType.ILLEGAL_PARAMS);
        }

        // 2、计算各个节点调整的限流值
        long totalCurrentReadFileRateLimit = ip2IndexNodeMoveTaskStatsMap
                .values()
                .stream()
                .map(r -> r.getReadFileRateLimit().get())
                .mapToLong(Long::longValue)
                .sum();

        Map<String, Double> ip2AdjustReadFileRateLimitMap = getIp2AdjustReadFileRateLimitMap(
                adjustReadFileRateLimit, 
                ipList, 
                ip2IndexNodeMoveTaskStatsMap, 
                totalCurrentReadFileRateLimit);

        List<Result> resultList = new CopyOnWriteArrayList<>();
        for (String ip : ipList) {
            FUTURE_UTIL.runnableTask(() -> {
                Result  result;
                try {
                    result = RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                            "AdjustIndexMoveTaskReadRateAction",
                            RetryUtil.DEFAULT_TIME,
                            RetryUtil.DEFAULT_INTERVAL_MILLS,
                            () -> {
                                ReadFileRateInfo nodeReadFileRateInfoInfo = new ReadFileRateInfo();
                                nodeReadFileRateInfoInfo.setTaskId(taskId);
                                nodeReadFileRateInfoInfo.setReadFileRateLimit(ip2AdjustReadFileRateLimitMap.get(ip));
                                String res = BaseHttpUtil.putForString(buildNodeAdjustReadRateHttpUrl(new String[] { ip }),
                                        JSON.toJSONString(nodeReadFileRateInfoInfo), buildHeader());
                                return ConvertUtil.str2ObjByJson(res, Result.class);
                            }
                    );
                } catch (Exception e) {
                    result = Result.buildFail(String.format("ip:%s,", ip) + "err:" + e.getMessage());
                }
                resultList.add(result);
            });

        }
        FUTURE_UTIL.waitExecute();

        StringBuilder err = new StringBuilder();
        for (Result result : resultList) {
            if (result.failed()) { err.append(result.getMessage()).append("\n");}
        }

        if (err.length() > 0) {
            throw new FastDumpOperateException(String.format("触发成功, 部分节点上的索引shard失败, detail:%s", err));
        }

        return true;
    }

    private static Map<String, Double> getIp2AdjustReadFileRateLimitMap(double adjustReadFileRateLimit, 
                                                                 List<String> ipList, 
                                                                 Map<String, IndexNodeMoveTaskStats> ip2IndexNodeMoveTaskStatsMap, 
                                                                 long totalCurrentReadFileRateLimit) {
        Map<String, Double> ip2AdjustReadFileRateLimitMap = new HashMap<>();
        for (int i = 0; i < ipList.size(); i++) {
            String ip = ipList.get(i);
            IndexNodeMoveTaskStats indexNodeMoveTaskStats = ip2IndexNodeMoveTaskStatsMap.get(ip);
            long   singleCurrentReadFileRateLimit = indexNodeMoveTaskStats.getReadFileRateLimit().get();
            double single = Long.valueOf(singleCurrentReadFileRateLimit).doubleValue();
            double total  = Long.valueOf(totalCurrentReadFileRateLimit).doubleValue();
            double percent = single / total;

            double singleAdjustReadFileRateLimit = CommonUtils.formatDouble(adjustReadFileRateLimit * percent, 0);
            
            ip2AdjustReadFileRateLimitMap.put(ip, singleAdjustReadFileRateLimit);
        }
        return ip2AdjustReadFileRateLimitMap;
    }

    private Map<String, IndexNodeMoveTaskStats> getIp2IndexNodeMoveTaskStatsMap(String taskId, List<String> ipList) {
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
                                String resp = BaseHttpUtil.get(buildIndexNodeStatesHttpUrl(new String[] { ip, taskId}), null, buildHeader());
                                return ConvertUtil.str2ObjByJson(resp, Result.class);
                            }
                    );
                } catch (Exception e) {
                    LOGGER.error("class=AdjustIndexMoveTaskReadRateAction||ip={}||method=getIp2ReadFileRateLimitMap||errMsg={}", ip,
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

        return ip2IndexNodeMoveTaskStatsMap;
    }

    protected String buildNodeAdjustReadRateHttpUrl(String[] args) {
        String ip = args[0];
        return "http://" + ip + ":" + httpPort + "/index-node-move/adjust-readRate";
    }

    private String buildIndexNodeStatesHttpUrl(String[] args) {
        String ip = args[0], taskId = args[1];
        return "http://" + ip + ":" + httpPort + "/index-node-move/" + taskId + "/stats";
    }
}
