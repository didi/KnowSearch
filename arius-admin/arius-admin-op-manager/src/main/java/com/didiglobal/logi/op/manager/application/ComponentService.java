package com.didiglobal.logi.op.manager.application;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.*;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-12 2:31 下午
 */
@org.springframework.stereotype.Component
public class ComponentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentService.class);

    @Autowired
    private ComponentDomainService componentDomainService;

    @Autowired
    private TaskDomainService taskDomainService;

    //TODO 这几个操作母亲啊都缺乏校验
    public Result<Integer> installComponent(GeneralInstallComponent installComponent) {
        LOGGER.info("start install component[{}]", installComponent.getName());
        Result checkRes = installComponent.checkInstallParam();
        if (checkRes.failed()) {
            return checkRes;
        }
        Result taskCheckRes = taskDomainService.hasRepeatTask(installComponent.getName(), null);
        if (taskCheckRes.failed()) {
            return taskCheckRes;
        }
        return componentDomainService.submitInstallComponent(installComponent);
    }

    public Result<Integer> scaleComponent(GeneralScaleComponent scaleComponent) {
        LOGGER.info("start scale component[{}]", scaleComponent);
        Result checkRes = scaleComponent.checkScaleParam();
        if (checkRes.failed()) {
            return checkRes;
        }

        Result taskCheckRes = taskDomainService.hasRepeatTask(null, scaleComponent.getComponentId());
        if (taskCheckRes.failed()) {
            return taskCheckRes;
        }
        return componentDomainService.submitScaleComponent(scaleComponent);
    }

    public Result<Integer> configChangeComponent(GeneralConfigChangeComponent configChangeComponent) {
        LOGGER.info("start change component config[{}]", configChangeComponent);
        Result checkRes = configChangeComponent.checkConfigChangeParam();
        if (checkRes.failed()) {
            return checkRes;
        }

        Result taskCheckRes = taskDomainService.hasRepeatTask(null, configChangeComponent.getComponentId());
        if (taskCheckRes.failed()) {
            return taskCheckRes;
        }
        return componentDomainService.submitConfigChangeComponent(configChangeComponent);
    }

    public Result<Integer> restartComponent(GeneralRestartComponent restartComponent) {
        LOGGER.info("start restart component[{}]", restartComponent);
        Result checkRes = restartComponent.checkRestartParam();
        if (checkRes.failed()) {
            return checkRes;
        }
        Result taskCheckRes = taskDomainService.hasRepeatTask(null, restartComponent.getComponentId());
        if (taskCheckRes.failed()) {
            return taskCheckRes;
        }
        return componentDomainService.submitRestartComponent(restartComponent);
    }

    public Result<Integer> upgradeComponent(GeneralUpgradeComponent generalUpgradeComponent) {
        LOGGER.info("start upgrade component[{}]",generalUpgradeComponent);
        Result checkRes = generalUpgradeComponent.checkUpgradeParam();
        if (checkRes.failed()) {
            return checkRes;
        }
        Result taskCheckRes = taskDomainService.hasRepeatTask(null, generalUpgradeComponent.getComponentId());
        if (taskCheckRes.failed()) {
            return taskCheckRes;
        }
        return componentDomainService.submitUpgradeComponent(generalUpgradeComponent);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> rollbackComponent(GeneralRollbackComponent generalRollbackComponent) {
        LOGGER.info("start rollback component[{}]", generalRollbackComponent);
        Result checkRes = generalRollbackComponent.checkRollbackParam();
        if (checkRes.failed()) {
            return checkRes;
        }

        //只有failed状态的任务才允许回滚
        Result statusAndTypeRes = checkStatusAndType(generalRollbackComponent);
        if (statusAndTypeRes.failed()) {
            return statusAndTypeRes;
        }

        Result updateRes = taskDomainService.updateTaskStatus(generalRollbackComponent.getTaskId(), TaskStatusEnum.CANCELLED.getStatus());
        if (updateRes.failed()) {
            return updateRes;
        }
        Result taskCheckRes = taskDomainService.hasRepeatTask(null, generalRollbackComponent.getComponentId());
        if (taskCheckRes.failed()) {
            //TODO 测试
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return taskCheckRes;
        }
        Result<Integer> submitRes = componentDomainService.submitRollbackComponent(generalRollbackComponent);
        if (submitRes.failed()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return submitRes;
    }

    @Nullable
    private Result checkStatusAndType(GeneralRollbackComponent generalRollbackComponent) {
        Result<Task> taskRes = taskDomainService.getTaskById(generalRollbackComponent.getTaskId());
        if (taskRes.failed() || null == taskRes.getData()) {
            return Result.fail("task任务不存在");
        }

        if (taskRes.getData().getStatus() != TaskStatusEnum.FAILED.getStatus()) {
            return Result.fail("task任务状态不能为failed");
        }

        if (taskRes.getData().getType() == OperationEnum.ROLLBACK.getType()) {
            return Result.fail("原任务不能是回滚任务");
        }
        return Result.success();
    }

    public Result<Integer> executeFunctionComponent(GeneralExecuteComponentFunction executeComponentFunction) {
        LOGGER.info("start execute function component[{}]",executeComponentFunction);
        Result checkRes = executeComponentFunction.checkExecuteFunctionParam();
        if (checkRes.failed()) {
            return checkRes;
        }
        Result taskCheckRes = taskDomainService.hasRepeatTask(null, executeComponentFunction.getComponentId());
        if (taskCheckRes.failed()) {
            return taskCheckRes;
        }
        return componentDomainService.submitExecuteFunctionComponent(executeComponentFunction);
    }

    public Result<ComponentGroupConfig> getConfig(Integer componentId, String groupName) {
        if (null == componentId || null == groupName) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "组件id或者groupName不能为空");
        }
        return componentDomainService.getComponentConfigByGroupName(componentId, groupName);
    }

    public Result<Integer> reportHostStatus(Integer componentId, String groupName, String host, Integer status) {
        if (HostStatusEnum.UN_KNOW == HostStatusEnum.find(status)) {
            return Result.fail(ResultCode.COMPONENT_HOST_STATUS_ILLEGAL_ERROR);
        }
        return componentDomainService.reportComponentHostStatus(componentId, groupName, host, status);
    }
}
