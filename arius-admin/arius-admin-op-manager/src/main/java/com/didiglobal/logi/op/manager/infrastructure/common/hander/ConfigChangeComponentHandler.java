package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralConfigChangeComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author didi
 * @date 2022-07-25 5:55 下午
 */
@org.springframework.stereotype.Component
@DefaultHandler
public class ConfigChangeComponentHandler extends BaseComponentHandler implements ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigChangeComponentHandler.class);

    @Autowired
    private TaskDomainService taskDomainService;

    @Override
    public void eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralConfigChangeComponent changeComponent = (GeneralConfigChangeComponent) componentEvent.getSource();
            changeComponent.setTemplateId(getTemplateId(changeComponent.getComponentId()));
            String content = JSONObject.toJSON(changeComponent).toString();
            Map<String, List<String>> groupToIpList = new LinkedHashMap<>(16);
            changeComponent.getGroupConfigList().forEach(config ->
            {
                if (!StringUtils.isEmpty(config.getHosts())) {
                    groupToIpList.put(config.getGroupName(), Arrays.asList(config.getHosts().split(Constants.SPLIT)));
                }
            });
            taskDomainService.createTask(content, componentEvent.getOperateType(),
                    componentEvent.getDescribe(), changeComponent.getAssociationId(), groupToIpList);
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public void taskFinishProcess(String content) throws ComponentHandlerException {
        try {
            GeneralConfigChangeComponent scaleComponent = JSON.parseObject(content, GeneralConfigChangeComponent.class);
            Component component = ConvertUtil.obj2Obj(scaleComponent, Component.class);
            componentDomainService.changeComponentConfig(component);
        } catch (Exception e) {
            LOGGER.error("task finish process error.", e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public Integer getOperationType() throws ComponentHandlerException {
        return OperationEnum.CONFIG_CHANGE.getType();
    }
}
