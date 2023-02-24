package com.didi.cloud.fastdump.core.action.movetask;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.exception.FastDumpOperateException;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.TemplateMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/8
 */
@Component
public class PauseTemplateMoveAction implements Action<String, Boolean> {
    private static final FutureUtil<Void> futureUtil = FutureUtil.init("PauseTemplateMoveAction-FutureUtil", 5, 5, 1000);

    @Autowired
    private TemplateMoveTaskMetadata      templateMoveTaskMetadata;

    @Autowired
    private PauseIndexMoveAction          pauseIndexMoveAction;

    @Override
    public Boolean doAction(String taskId) throws Exception {
        if (templateMoveTaskMetadata.isTaskSucc(taskId)) {
            throw new BaseException(String.format("template move task[%s] is succ", taskId), ResultType.ILLEGAL_PARAMS);
        }

        TemplateMoveTaskStats moveTaskStats = templateMoveTaskMetadata.getMoveTaskStats(taskId);
        if (null == moveTaskStats) {
            throw new BaseException(String.format("template task[%s] is not exist", taskId), ResultType.ILLEGAL_PARAMS);
        }
        // 终止剩余任务
        moveTaskStats.setInterruptMark(true);

        List<String> submitIndexMoveTaskIds = moveTaskStats.getSubmitIndexMoveTaskIds();

        AtomicBoolean putFlag = new AtomicBoolean(true);
        StringBuffer errSb = new StringBuffer();
        for (String indexTaskId : submitIndexMoveTaskIds) {
            futureUtil.runnableTask(() -> {
                try {
                    pauseIndexMoveAction.doAction(indexTaskId);
                } catch (Exception e) {
                    putFlag.set(false);
                    errSb.append(String.format("template taskId:%s,", taskId)).append(e.getMessage()).append("\n");
                }
            });

        }
        futureUtil.waitExecute();
        if (!putFlag.get()) {
            throw new FastDumpOperateException(errSb.toString());
        }
        return true;
    }
}
