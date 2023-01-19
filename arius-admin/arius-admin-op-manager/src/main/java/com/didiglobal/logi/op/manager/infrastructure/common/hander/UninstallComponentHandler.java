package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.Tuple;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralUninstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;

/**
 * @author didi
 * @date 2022-10-24 17:05
 */
@org.springframework.stereotype.Component
@DefaultHandler
public class UninstallComponentHandler extends BaseComponentHandler implements ComponentHandler {

    private static final ILog LOGGER = LogFactory.getLog(UninstallComponentHandler.class);

    @Autowired
    private TaskDomainService taskDomainService;

    @Override
    public Integer eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralUninstallComponent uninstallComponent = (GeneralUninstallComponent) componentEvent.getSource();
            Component component = componentDomainService.getComponentById(uninstallComponent.getComponentId()).getData();
            uninstallComponent.setTemplateId(getTemplateId(component));
            String content = JSONObject.toJSON(uninstallComponent).toString();
            Map<String, List<Tuple<String, Integer>>> groupToIpList = getGroup2HostMap(uninstallComponent.getGroupConfigList());
            int taskId = taskDomainService.createTask(content, componentEvent.getOperateType(),
                    component.getName() + componentEvent.getDescribe(), groupToIpList).getData();
            return taskId;
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }
    }

    @Override
    public void taskFinishProcess(int taskId, String content) throws ComponentHandlerException {
        try {
            GeneralUninstallComponent uninstallComponent = JSON.parseObject(content, GeneralUninstallComponent.class);
            componentDomainService.offLine(uninstallComponent.getComponentId());
        } catch (Exception e) {
            LOGGER.error("task finish process error.", e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public Integer getOperationType() {
        return OperationEnum.UNINSTALL.getType();
    }

    @Override
    public Result<Void> execute(Task task) {
        return taskDomainService.executeDeployTask(task);
    }
}
