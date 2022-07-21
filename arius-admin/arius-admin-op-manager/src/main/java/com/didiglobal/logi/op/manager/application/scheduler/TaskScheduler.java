package com.didiglobal.logi.op.manager.application.scheduler;

import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.ComponentHandlerFactory;
import com.didiglobal.logi.op.manager.infrastructure.deployment.DeploymentService;
import com.didiglobal.logi.op.manager.infrastructure.deployment.ZeusTaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author didi
 * @date 2022-07-15 2:23 下午
 */
@Component
public class TaskScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskScheduler.class);

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    TaskDomainService taskDomainService;

    @Autowired
    ComponentHandlerFactory componentHandlerFactory;

    //TODO 一个任务的最后一个节点失败，那这个节点就没法重试了以及忽略
    @Scheduled(initialDelay = 10000, fixedDelay = 300000)
    public void monitor() {
        try {
            List<Task> taskList = taskDomainService.getUnFinishTaskList().getData();

            taskList.forEach(task -> {
                try {
                    Set<Integer> executeIdSet = new HashSet<>();
                    task.getDetailList().forEach(taskDetail -> {
                        executeIdSet.add(taskDetail.getExecuteTaskId());
                    });

                    if (executeIdSet.size() > 0) {
                        int finalStatus = task.getStatus();
                        int isFinish = 0;
                        ZeusTaskStatus totalStatus = new ZeusTaskStatus();
                        for (Integer id : executeIdSet) {
                            //TODO要考虑定时任务和手动操作
                            ZeusTaskStatus zeusTaskStatus = deploymentService.deployStatus(id).getData();
                            for (Field declaredField : zeusTaskStatus.getClass().getDeclaredFields()) {
                                List<String> hostList = (List<String>) declaredField.get(zeusTaskStatus);
                                taskDomainService.updateTaskDetail(task.getId(), id, TaskStatusEnum.valueOf(declaredField.getName().toUpperCase()).getStatus(),
                                        hostList);
                            }

                            totalStatus.addZeusTaskStatus(zeusTaskStatus);
                        }

                        finalStatus = getFinalStatusAndUpdate(task, finalStatus, isFinish, totalStatus);

                        if (finalStatus == TaskStatusEnum.SUCCESS.getStatus()) {
                            componentHandlerFactory.getByType(task.getType()).taskFinishProcess(task.getContent());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("task[{}] monitor error", task.getId(), e);
                    throw new RuntimeException(e);
                }

            });

        } catch (Exception e) {
            LOGGER.error("task monitor error", e);
        }
    }

    private int getFinalStatusAndUpdate(Task task, int finalStatus, int isFinish, ZeusTaskStatus totalStatus) {
        //更新任务最终状态
        //TODO这里的状态有点混乱
        if ((null != totalStatus.getTimeout() || null != totalStatus.getFailed()) &&
                null != totalStatus.getWaiting()) {
            finalStatus = TaskStatusEnum.WAITING.getStatus();
        } else if (null != totalStatus.getTimeout() || null != totalStatus.getFailed()) {
            finalStatus = TaskStatusEnum.FAILED.getStatus();
            isFinish = 1;
        } else if (null == totalStatus.getWaiting() && null == totalStatus.getRunning()) {
            finalStatus = TaskStatusEnum.SUCCESS.getStatus();
            isFinish = 1;
        } else if (null != totalStatus.getWaiting() && null == totalStatus.getRunning()) {
            finalStatus = TaskStatusEnum.WAITING.getStatus();
        }

        if (task.getStatus() != finalStatus) {
            taskDomainService.updateTaskStatus(task.getId(), task.getStatus(), isFinish);
        }
        return finalStatus;
    }
}
