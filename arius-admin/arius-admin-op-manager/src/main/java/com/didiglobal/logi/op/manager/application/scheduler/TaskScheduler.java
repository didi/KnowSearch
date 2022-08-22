package com.didiglobal.logi.op.manager.application.scheduler;

import com.didiglobal.logi.op.manager.application.TaskService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.ComponentHandlerFactory;
import com.didiglobal.logi.op.manager.infrastructure.deployment.DeploymentService;
import com.didiglobal.logi.op.manager.infrastructure.deployment.zeus.ZeusTaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
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

    @Autowired
    TaskScheduler taskScheduler;

    //TODO 一个任务的最后一个节点失败，那这个节点就没法重试了以及忽略, 等志勇他们解决
    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void monitor() {
        try {
            List<Task> taskList = taskDomainService.getUnFinishTaskList().getData();
            taskList.forEach(task -> {
                taskScheduler.handle(task);
            });
        } catch (Exception e) {
            LOGGER.error("task monitor error", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handle(Task task) {
        try {
            Set<Integer> executeIdSet = new HashSet<>();
            boolean isContainEmptyExecuteId = false;

            for (TaskDetail taskDetail : task.getDetailList()) {
                if (null != taskDetail.getExecuteTaskId()) {
                    executeIdSet.add(taskDetail.getExecuteTaskId());
                } else {
                    isContainEmptyExecuteId = true;
                }
            }

            if (executeIdSet.size() > 0) {
                ZeusTaskStatus totalStatus = new ZeusTaskStatus();
                for (Integer id : executeIdSet) {
                    //TODO 要考虑并发时定时任务和手动操作数据库产生的不一致情况
                    //TODO 这里会有重复更新的情况，如何性能最大化
                    ZeusTaskStatus zeusTaskStatus = deploymentService.deployStatus(id).getData();
                    for (Field declaredField : zeusTaskStatus.getClass().getDeclaredFields()) {
                        declaredField.setAccessible(true);
                        List<String> hostList = (List<String>) declaredField.get(zeusTaskStatus);
                        if (hostList != null) {
                            taskDomainService.updateTaskDetail(task.getId(), id, TaskStatusEnum.valueOf(declaredField.getName().toUpperCase()).getStatus(),
                                    hostList);
                        }

                    }

                    totalStatus.addZeusTaskStatus(zeusTaskStatus);
                }

                //这里是把未执行的当成待执行
                if (isContainEmptyExecuteId) {
                    totalStatus.addZeusTaskStatus(ZeusTaskStatus.builder().waiting(new ArrayList<>()).build());
                }

                int finalStatus = getFinalStatusAndUpdate(task, totalStatus, isContainEmptyExecuteId);

                if (finalStatus == TaskStatusEnum.SUCCESS.getStatus()) {
                    componentHandlerFactory.getByType(task.getType()).taskFinishProcess(task.getContent());
                }
            }
        } catch (Exception e) {
            LOGGER.error("task[{}] monitor error", task.getId(), e);
            throw new RuntimeException(e);
        }
    }

    private int getFinalStatusAndUpdate(Task task, ZeusTaskStatus totalStatus, boolean isContainEmptyExecuteId) {
        int isFinish = 0;
        int finalStatus;
        if (null != totalStatus.getTimeout() || null != totalStatus.getFailed()) {
            finalStatus = TaskStatusEnum.FAILED.getStatus();
            isFinish = 1;
        } else if (null != totalStatus.getRunning()) {
            finalStatus = TaskStatusEnum.RUNNING.getStatus();
        } else if (null != totalStatus.getWaiting()) {
            finalStatus = TaskStatusEnum.WAITING.getStatus();
        } else {
            finalStatus = TaskStatusEnum.SUCCESS.getStatus();
            isFinish = 1;
        }

        if (task.getStatus() != finalStatus) {
            taskDomainService.updateTaskStatusAndIsFinish(task.getId(), isFinish, finalStatus);
        }
        return finalStatus;
    }
}
