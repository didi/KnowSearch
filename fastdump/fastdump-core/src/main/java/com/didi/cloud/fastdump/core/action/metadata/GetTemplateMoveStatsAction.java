package com.didi.cloud.fastdump.core.action.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.stats.IndexMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.component.SpringTool;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;
import com.didi.cloud.fastdump.common.event.es.TemplateMoveTaskSuccEvent;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.common.utils.ConvertUtil;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.TemplateMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/8
 */
@Component
public class GetTemplateMoveStatsAction implements Action<String, TemplateMoveTaskStats> {
    protected static final Logger         LOGGER     = LoggerFactory.getLogger(GetTemplateMoveStatsAction.class);
    @Autowired
    private TemplateMoveTaskMetadata      templateMoveTaskMetadata;

    @Autowired
    private GetIndexMoveStatsAction       getIndexMoveStatsAction;

    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("GetTemplateMoveStatsAction-FutureUtil", 5, 5, 1000);

    @Override
    public TemplateMoveTaskStats doAction(String taskId) throws Exception {
        if (templateMoveTaskMetadata.isTaskSucc(taskId)) {
            return  ConvertUtil.obj2ObjByJSON(templateMoveTaskMetadata.getSuccTaskStats(taskId), TemplateMoveTaskStats.class);
        }

        TemplateMoveTaskStats templateMoveTaskStats = templateMoveTaskMetadata.getMoveTaskStats(taskId);
        if (null == templateMoveTaskStats) {
            throw new BaseException(String.format("template move task stats[%s] is not exist", taskId), ResultType.FAIL);
        }

        List<String> submitIndexMoveTaskIds = templateMoveTaskStats.getSubmitIndexMoveTaskIds();

        if (CollectionUtils.isEmpty(submitIndexMoveTaskIds)) {
            throw new BaseException(String.format("submit index move task[%s] is not exist", taskId), ResultType.ILLEGAL_PARAMS);
        }
        List<IndexMoveTaskStats> indexMoveTaskStatsList = new CopyOnWriteArrayList<>();
        for (String submitIndexMoveTaskId : submitIndexMoveTaskIds) {
            FUTURE_UTIL.runnableTask(() -> {
                try {
                    indexMoveTaskStatsList.add(getIndexMoveStatsAction.doAction(submitIndexMoveTaskId));
                } catch (Exception e) {
                    LOGGER.error("class=GetTemplateMoveStatsAction||method=doAction||errMsg={}", e.getMessage(), e);
                }
            });
        }
        FUTURE_UTIL.waitExecute();

        mergeStats(templateMoveTaskStats, indexMoveTaskStatsList);

        if (TaskStatusEnum.SUCCESS.getValue().equals(templateMoveTaskStats.getStatus())) {
            SpringTool.publish(new TemplateMoveTaskSuccEvent(this, templateMoveTaskStats));
        }
        return templateMoveTaskStats;
    }

    private void mergeStats(TemplateMoveTaskStats templateMoveTaskStats,
                                             List<IndexMoveTaskStats> indexMoveTaskStatsList) {
        if (CollectionUtils.isEmpty(indexMoveTaskStatsList)) { return;}

        int  succIndexNum = 0;
        long costTime     = 0L;
        int shardNum      = 0;
        int succShardNum  = 0;
        long totalDocNum  = 0L;
        long succDocNum   = 0L;
        long readFileRateLimit = 0L;
        long kernelEstimationReadFileRateLimit = 0L;

        List<String> subIndexStatusList = new ArrayList<>();
        for (IndexMoveTaskStats indexMoveTaskStats : indexMoveTaskStatsList) {
            String status = indexMoveTaskStats.getStatus();
            subIndexStatusList.add(status);
            if (TaskStatusEnum.SUCCESS.getValue().equals(status)) {
                succIndexNum++;
            }
            shardNum     += indexMoveTaskStats.getShardNum();
            succShardNum += indexMoveTaskStats.getSuccShardNum();
            totalDocNum  += indexMoveTaskStats.getTotalDocumentNum();
            succDocNum   += indexMoveTaskStats.getSuccDocumentNum();
            costTime     += indexMoveTaskStats.getCostTime();
            readFileRateLimit += indexMoveTaskStats.getReadFileRateLimit();
            kernelEstimationReadFileRateLimit += indexMoveTaskStats.getKernelEstimationReadFileRateLimit();
        }

        templateMoveTaskStats.setGlobalReadFileRateLimit(readFileRateLimit);
        templateMoveTaskStats.setKernelEstimationReadFileRateLimit(kernelEstimationReadFileRateLimit);

        templateMoveTaskStats.setSuccIndexNum(succIndexNum);

        templateMoveTaskStats.setTotalShardNum(shardNum);
        templateMoveTaskStats.setSuccShardNum(succShardNum);

        templateMoveTaskStats.setTotalDocNum(totalDocNum);
        templateMoveTaskStats.setSuccDocNum(succDocNum);

        templateMoveTaskStats.setCostTime(costTime);

        TaskStatusEnum taskStatusEnum = computeFinalStatus(subIndexStatusList,
            templateMoveTaskStats.getTotalIndexNum());

        templateMoveTaskStats.setStatus(taskStatusEnum.getValue());
    }

    private TaskStatusEnum computeFinalStatus(List<String> subIndexStatusList, Integer totalIndexNum) {
        List<Integer> statusCodeList = new ArrayList<>();
        for (String statusValue : subIndexStatusList) {
            TaskStatusEnum taskStatusEnum = TaskStatusEnum.valueOfType(statusValue);
            statusCodeList.add(taskStatusEnum.getCode());
        }

        int successNum = 0;
        int failedNum = 0;
        int runningNum = 0;
        int pauseNum = 0;
        int waitNum    = 0;

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
        if (pauseNum > 0 && (pauseNum + successNum) == totalIndexNum) {
            return TaskStatusEnum.PAUSE;
        }

        if (pauseNum > 0) {
            return TaskStatusEnum.PAUSE;
        }
        if (successNum > 0 && successNum == totalIndexNum) {
            return TaskStatusEnum.SUCCESS;
        }
        if (waitNum == totalIndexNum) {
            return TaskStatusEnum.WAIT;
        }
        return TaskStatusEnum.UNKNOWN;
    }
}
