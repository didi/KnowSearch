package com.didi.cloud.fastdump.core.action.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.readrate.ReadFileRateInfo;
import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.exception.FastDumpOperateException;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;
import com.didi.cloud.fastdump.core.service.metadata.TemplateMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/22
 */
@Component
public class AdjustTemplateMoveTaskReadRateAction implements Action<ReadFileRateInfo, Boolean> {
    protected static final Logger             LOGGER      = LoggerFactory
        .getLogger(AdjustTemplateMoveTaskReadRateAction.class);
    @Autowired
    private TemplateMoveTaskMetadata          templateMoveTaskMetadata;

    @Autowired
    private GetTemplateMoveStatsAction        getTemplateMoveStatsAction;

    @Autowired
    private IndexMoveTaskMetadata             indexMoveTaskMetadata;

    @Autowired
    private AdjustIndexMoveTaskReadRateAction adjustIndexMoveTaskReadRateAction;
    private static final FutureUtil<Void>     FUTURE_UTIL = FutureUtil
        .init("AdjustTemplateMoveTaskReadRateAction-FutureUtil", 5, 5, 1000);

    @Override
    public Boolean doAction(ReadFileRateInfo readFileRateInfo) throws Exception {
        String taskId = readFileRateInfo.getTaskId();
        if (templateMoveTaskMetadata.isTaskSucc(taskId)) {
            throw new BaseException(String.format("template move task[%s] is succ", taskId), ResultType.ILLEGAL_PARAMS);
        }

        TemplateMoveTaskStats moveTaskStats = templateMoveTaskMetadata.getMoveTaskStats(taskId);
        if (null == moveTaskStats) {
            throw new BaseException(String.format("template move task[%s] is null", taskId), ResultType.ILLEGAL_PARAMS);
        }

        List<String> submitIndexMoveTaskIds = moveTaskStats.getSubmitIndexMoveTaskIds();
        int    templateIndexSize = submitIndexMoveTaskIds.size();
        double templateReadFileRateLimit = readFileRateInfo.getReadFileRateLimit();

        double singleIndexGlobalReadFileRateLimit =  templateReadFileRateLimit / templateIndexSize;
        if (singleIndexGlobalReadFileRateLimit < 1000) {
            throw new BaseException(
                    String.format("sourceTemplate[%s] has %d indices, " +
                                    "singleIndexGlobalReadFileRateLimit is %f"
                                    + "please adjust templateReadFileRateLimit between %d ~ %d",
                            moveTaskStats.getSourceTemplate(), templateIndexSize, singleIndexGlobalReadFileRateLimit,
                            1000 * templateIndexSize, 500000 * templateIndexSize),
                    ResultType.ILLEGAL_PARAMS);
        }

        List<ReadFileRateInfo> indexReadFileRateInfos = new ArrayList<>();
        for (String submitIndexMoveTaskId : submitIndexMoveTaskIds) {
            ReadFileRateInfo indexReadFileRateInfo = new ReadFileRateInfo();
            indexReadFileRateInfo.setTaskId(submitIndexMoveTaskId);
            indexReadFileRateInfo.setReadFileRateLimit(singleIndexGlobalReadFileRateLimit);

            indexReadFileRateInfos.add(indexReadFileRateInfo);
        }

        StringBuffer  errSb      = new StringBuffer();
        AtomicBoolean adjustFlag = new AtomicBoolean(true);
        for (ReadFileRateInfo indexReadFileRateInfo : indexReadFileRateInfos) {
            FUTURE_UTIL.runnableTask(() -> {
                try {
                    adjustIndexMoveTaskReadRateAction.doAction(indexReadFileRateInfo);
                } catch (Exception e) {
                    adjustFlag.set(false);
                    errSb.append(e.getMessage()).append("\n");
                    LOGGER.error("class=AdjustTemplateMoveTaskReadRateAction||method=doAction||ReadFileRateInfo={}" +
                            "||msg=failed to adjust indexReadRate, detail:{}", indexReadFileRateInfo, e.getMessage(), e);
                }
            });
        }

        FUTURE_UTIL.waitExecute();

        if (!adjustFlag.get()) {
            throw new FastDumpOperateException(String.format("触发成功, 部分节点上的索引shard失败, detail:%s", errSb));
        }
        return true;
    }
}
