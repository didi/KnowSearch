package com.didiglobal.logi.op.manager.application.scheduler;

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

                int finalStatus = getFinalStatusAndUpdate(task, totalStatus);

                if (finalStatus == TaskStatusEnum.SUCCESS.getStatus()) {
                    componentHandlerFactory.getByType(task.getType()).taskFinishProcess(task.getId(), task.getContent());
                }
            }
        } catch (Exception e) {
            LOGGER.error("task[{}] monitor error", task.getId(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 对zeus的转态转化到主任务的状态
     * 只要有超时或者失败就认为是失败，有running就认为是运行中，有waiting就认为是暂停，除此之外都是success
     *
     * @param task        任务
     * @param totalStatus zeus转态集合
     * @return 返回最终状态
     */
    private int getFinalStatusAndUpdate(Task task, ZeusTaskStatus totalStatus) {
        int isFinish = 0;
        int finalStatus;
        if (null != totalStatus.getTimeout() || null != totalStatus.getFailed()) {
            finalStatus = TaskStatusEnum.FAILED.getStatus();
            isFinish = 1;
        } else if (null != totalStatus.getRunning()) {
            finalStatus = TaskStatusEnum.RUNNING.getStatus();
        } else if (null != totalStatus.getWaiting()) {
            finalStatus = TaskStatusEnum.PAUSE.getStatus();
            //这里对于kill以及cancel操作，后续就不会执行，那这里就会标记成已完成，然后后续不会定时去监控状态
            if (task.isFinalStatus()) {
                isFinish = 1;
            }
        } else {
            //这里如果是final status，那状态就跟任务状态一致
            if (task.isFinalStatus()) {
                finalStatus = task.getStatus();
            } else {
                finalStatus = TaskStatusEnum.SUCCESS.getStatus();
            }
            isFinish = 1;
        }

        /**
         * 暂停以及kill以及cancel都是由用户api触发去做变更的，所以这里的状态变更对这些状态不处理
         * (有个特例就是如果最后状态是success，那不管用户操作，直接更新，可以理解你的操作时候，任务已经完成，操作无效)
         */
        if (task.getStatus() != finalStatus && (finalStatus == TaskStatusEnum.SUCCESS.getStatus() ||
                !task.isUserActionStatus())) {
            taskDomainService.updateTaskStatusAndIsFinish(task.getId(), isFinish, finalStatus);
        } else if (task.getIsFinish() != isFinish) {
            taskDomainService.updateTaskStatusAndIsFinish(task.getId(), isFinish, task.getStatus());
        }
        return finalStatus;
    }
}
