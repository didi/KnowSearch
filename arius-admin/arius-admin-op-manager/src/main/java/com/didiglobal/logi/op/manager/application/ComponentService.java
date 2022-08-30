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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
        Result checkRes = scaleComponent.checkParam();
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
        Result checkRes = configChangeComponent.checkParam();
        if (checkRes.failed()) {
            return checkRes;
        }

        Result taskCheckRes = taskDomainService.hasRepeatTask(null, configChangeComponent.getComponentId());
        if (taskCheckRes.failed()) {
            return taskCheckRes;
        }
        return componentDomainService.submitConfigChangeComponent(configChangeComponent);
    }

    public Result<Integer> restartComponent(GeneralBaseOperationComponent restartOperationComponent) {
        LOGGER.info("start restart component[{}]", restartOperationComponent);
        Result checkRes = restartOperationComponent.checkParam();
        if (checkRes.failed()) {
            return checkRes;
        }
        Result taskCheckRes = taskDomainService.hasRepeatTask(null, restartOperationComponent.getComponentId());
        if (taskCheckRes.failed()) {
            return taskCheckRes;
        }
        return componentDomainService.submitRestartComponent(restartOperationComponent);
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

    public Result<Integer> executeFunctionComponent(GeneralExecuteComponentFunction executeComponentFunction) {
        LOGGER.info("start execute function component[{}]",executeComponentFunction);
        Result checkRes = executeComponentFunction.checkParam();
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
