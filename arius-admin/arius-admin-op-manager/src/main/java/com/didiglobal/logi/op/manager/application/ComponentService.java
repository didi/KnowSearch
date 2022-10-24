package com.didiglobal.logi.op.manager.application;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.*;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
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

    /**
     * 根据条件获取所有的组件list
     *
     * @param component 组件
     * @return Result<List < Component>
     */
    public Result<List<Component>> listComponent(Component component) {
        return componentDomainService.queryComponent(component);
    }

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
        LOGGER.info("start upgrade component[{}]", generalUpgradeComponent);
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
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return taskCheckRes;
        }
        Result<Integer> submitRes = componentDomainService.submitRollbackComponent(generalRollbackComponent);
        if (submitRes.failed()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return submitRes;
    }

    public Result<Integer> uninstallComponent(Integer componentId) {
        LOGGER.info("start uninstall component[{}]", componentId);
        GeneralUninstallComponent uninstallComponent = new GeneralUninstallComponent();
        uninstallComponent.setComponentId(componentId);
        List<ComponentGroupConfig> list = componentDomainService.getComponentConfig(componentId).getData();
        uninstallComponent.setGroupConfigList(ConvertUtil.list2List(list, GeneralGroupConfig.class));
        Result taskCheckRes = taskDomainService.hasRepeatTask(null, componentId);
        if (taskCheckRes.failed()) {
            return taskCheckRes;
        }
        return componentDomainService.submitUninstallComponent(uninstallComponent);
    }

    @Nullable
    private Result checkStatusAndType(GeneralRollbackComponent generalRollbackComponent) {
        Result<Task> taskRes = taskDomainService.getTaskById(generalRollbackComponent.getTaskId());
        if (taskRes.failed() || null == taskRes.getData()) {
            return Result.fail("task任务不存在");
        }

        if (generalRollbackComponent.getType() != OperationEnum.CONFIG_CHANGE.getType()
                && generalRollbackComponent.getType() != OperationEnum.UPGRADE.getType()) {
            return Result.fail("只有配置变更以及升级才能允许回滚");
        }

        if (taskRes.getData().getStatus() != TaskStatusEnum.FAILED.getStatus()) {
            return Result.fail("task任务状态不能为failed");
        }

        return Result.success();
    }

    public Result<Integer> executeFunctionComponent(GeneralExecuteComponentFunction executeComponentFunction) {
        LOGGER.info("start execute function component[{}]", executeComponentFunction);
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

    public Result<GeneralGroupConfig> getGeneralConfig(Integer componentId, String groupName) {
        if (null == componentId || null == groupName) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "组件id或者groupName不能为空");
        }

        Result componentRes = componentDomainService.getComponentById(componentId);
        if (componentRes.failed()) {
            return componentRes;
        }

        Result configRes = componentDomainService.getComponentConfigByGroupName(componentId, groupName);
        if (configRes.failed()) {
            return configRes;
        }

        GeneralGroupConfig generalGroupConfig = ConvertUtil.obj2Obj(configRes.getData(), GeneralGroupConfig.class);
        Component component = (Component) componentRes.getData();
        generalGroupConfig.setUsername(component.getUsername());
        generalGroupConfig.setPassword(component.getPassword());
        generalGroupConfig.setIsOpenTSL(component.getIsOpenTSL());
        return Result.success(generalGroupConfig);
    }

    public Result<Integer> reportHostStatus(Integer componentId, String groupName, String host, Integer status) {
        if (HostStatusEnum.UN_KNOW == HostStatusEnum.find(status)) {
            return Result.fail(ResultCode.COMPONENT_HOST_STATUS_ILLEGAL_ERROR);
        }
        return componentDomainService.reportComponentHostStatus(componentId, groupName, host, status);
    }

    public Result<Integer> offLineComponent(Integer componentId) {
        Result<Integer> res = componentDomainService.offLine(componentId);
        if (res.getData() != 0) {
            return Result.fail("组件未删除或者多删除");
        }
        return res;
    }
}
