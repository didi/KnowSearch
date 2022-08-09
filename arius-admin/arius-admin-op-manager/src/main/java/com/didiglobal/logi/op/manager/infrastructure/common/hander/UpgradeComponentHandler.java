package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralUpgradeComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author didi
 * @date 2022-08-08 5:06 下午
 */
@org.springframework.stereotype.Component
@DefaultHandler
public class UpgradeComponentHandler extends BaseComponentHandler implements ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeComponentHandler.class);

    @Autowired
    private TaskDomainService taskDomainService;

    @Autowired
    private ComponentDomainService componentDomainService;


    @Override
    public void eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralUpgradeComponent upgradeComponent = (GeneralUpgradeComponent) componentEvent.getSource();

            upgradeComponent.setTemplateId(getTemplateIdByPackageId(upgradeComponent.getPackageId()));

            String content = JSONObject.toJSON(upgradeComponent).toString();
            taskDomainService.createTask(content, componentEvent.getOperateType(),
                    componentEvent.getDescribe(), upgradeComponent.getAssociationId(),
                    componentDomainService.getComponentConfig(upgradeComponent.getComponentId()).getData());
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public void taskFinishProcess(String content) throws ComponentHandlerException {
        try {
            GeneralUpgradeComponent upgradeComponent = JSON.parseObject(content, GeneralUpgradeComponent.class);
            Component component = new Component();
            component.setId(upgradeComponent.getComponentId());
            component.setPackageId(upgradeComponent.getPackageId());
            componentDomainService.updateComponent(component);
        } catch (Exception e) {
            LOGGER.error("component[{}] handler error.", content, e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public Integer getOperationType() throws ComponentHandlerException {
        return OperationEnum.UPGRADE.getType();
    }
}

