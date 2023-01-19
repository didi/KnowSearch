package com.didichuxing.datachannel.arius.admin.task.ecmtask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.biz.task.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * 定时同步集群任务状态
 */
//@Task(name = "syncEcmTaskStatus", description = "定时同步集群任务状态", cron = "0 0/2 * * * ?", autoRegister = true)
public class SyncEcmTaskStatus implements Job {
    private static final ILog LOGGER = LogFactory.getLog(SyncEcmTaskStatus.class);

    @Autowired
    private EcmTaskManager      ecmTaskManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=SyncEcmTaskStatus||method=syncTaskStatus||msg=start");

        // 获取处于running状态的ecm任务列表
        List<EcmTask> ecmTasks = ecmTaskManager.listRunningEcmTask();
        if (AriusObjUtils.isEmptyList(ecmTasks)) {
            LOGGER.info("class=SyncEcmTaskStatus||method=syncTaskStatus||msg=worktask empty and finished");
            return TaskResult.buildSuccess();
        }

        // todo:对于集群新建，扩缩容这些可能修改物理集群读写地址的任务，可能会出现重试中阻塞的问题，从而影响到下一个任务的执行，考虑是否采用线程池
        for (EcmTask ecmTask : ecmTasks) {
            try {
                EcmTaskStatusEnum ecmTaskStatusEnum = ecmTaskManager.refreshEcmTask(ecmTask);
                LOGGER.info("class=SyncEcmTaskStatus||method=syncTaskStatus||taskId={}||resultStatus={}",
                    ecmTask.getId(), ecmTaskStatusEnum.getValue());
            } catch (Exception e) {
                LOGGER.error("class=SyncEcmTaskStatus||method=syncTaskStatus||errMsg={}||worktask={}", e.getMessage(),
                    ecmTask);
            }
        }
        LOGGER.info("class=SyncEcmTaskStatus||method=syncTaskStatus||msg=finish");

        return TaskResult.buildSuccess();
    }
}