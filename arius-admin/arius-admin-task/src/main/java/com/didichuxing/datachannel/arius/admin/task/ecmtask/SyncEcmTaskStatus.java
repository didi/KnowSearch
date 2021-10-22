package com.didichuxing.datachannel.arius.admin.task.ecmtask;

import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.consensual.ConsensualConstant;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 定时同步集群任务状态
 */
@Task(name = "syncEcmTaskStatus", description = "定时同步集群任务状态", cron = "0 0/2 * * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class SyncEcmTaskStatus implements Job {
    private final static Logger LOGGER = LoggerFactory.getLogger( SyncEcmTaskStatus.class);

    @Autowired
    private EcmTaskManager ecmTaskManager;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=SyncEcmTaskStatus||method=syncTaskStatus||msg=start");

        List<EcmTask> ecmTasks = ecmTaskManager.listEcmTask();
        if (ValidateUtils.isEmptyList(ecmTasks)) {
            LOGGER.info("class=SyncEcmTaskStatus||method=syncTaskStatus||msg=worktask empty and finished");
            return "failed";
        }

        for (EcmTask ecmTask : ecmTasks) {
            try {
                if (ecmTask.getStatus().equals(EcmTaskStatusEnum.SUCCESS.getValue())
                        || ecmTask.getStatus().equals(EcmTaskStatusEnum.CANCEL.getValue())
                        || ecmTask.getStatus().equals(EcmTaskStatusEnum.WAITING.getValue())) {
                    // 任务已完成 | 已取消 | 等待中, 则忽略状态的刷新
                    continue;
                }

                Result<EcmTaskStatusEnum> statusEnumResult = ecmTaskManager.refreshEcmTask(ecmTask);

                LOGGER.debug("class=SyncEcmTaskStatus||method=syncTaskStatus||taskId={}||resultStatus={}",
                        ecmTask.getId(), ConvertUtil.obj2Json(statusEnumResult));
            } catch (Exception e) {
                LOGGER.error("class=SyncEcmTaskStatus||method=syncTaskStatus||errMsg={}||worktask={}", e.getMessage(), ecmTask);
            }
        }
        LOGGER.info("class=SyncEcmTaskStatus||method=syncTaskStatus||msg=finish");

        return "success";
    }
}