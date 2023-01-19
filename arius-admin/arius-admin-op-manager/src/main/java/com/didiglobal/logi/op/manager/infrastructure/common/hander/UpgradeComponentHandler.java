package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralUpgradeComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;

/**
 * @author didi
 * @date 2022-08-08 5:06 下午
 */
@org.springframework.stereotype.Component
@DefaultHandler
public class UpgradeComponentHandler extends BaseComponentHandler implements ComponentHandler {

    private static final ILog LOGGER = LogFactory.getLog(UpgradeComponentHandler.class);

    @Autowired
    private TaskDomainService taskDomainService;

    @Autowired
    private ComponentDomainService componentDomainService;


    @Override
    public Integer eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralUpgradeComponent upgradeComponent = (GeneralUpgradeComponent) componentEvent.getSource();
            Component component = componentDomainService.getComponentById(upgradeComponent.getComponentId()).getData();
            upgradeComponent.setTemplateId(getTemplateIdByPackageId(upgradeComponent.getPackageId()));
            List<ComponentGroupConfig> list = componentDomainService.getComponentConfig(upgradeComponent.getComponentId()).getData();
            upgradeComponent.setGroupConfigList(ConvertUtil.list2List(list, GeneralGroupConfig.class));
            String content = JSONObject.toJSON(upgradeComponent).toString();
            int taskId = taskDomainService.createTask(content, componentEvent.getOperateType(),
                    component.getName() + componentEvent.getDescribe(),
                    getGroup2HostMap(ConvertUtil.list2List(list, GeneralGroupConfig.class))).getData();
            return taskId;
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public void taskFinishProcess(int taskId, String content) throws ComponentHandlerException {
        try {
            GeneralUpgradeComponent upgradeComponent = JSON.parseObject(content, GeneralUpgradeComponent.class);
            Component component = new Component();
            component.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            component.setId(upgradeComponent.getComponentId());
            component.setPackageId(upgradeComponent.getPackageId());
            componentDomainService.updateComponent(component);
        } catch (Exception e) {
            LOGGER.error("component[{}] handler error.", content, e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public Integer getOperationType() {
        return OperationEnum.UPGRADE.getType();
    }

    @Override
    public Result<Void> execute(Task task) {
        return taskDomainService.executeDeployTask(task);
    }
}

