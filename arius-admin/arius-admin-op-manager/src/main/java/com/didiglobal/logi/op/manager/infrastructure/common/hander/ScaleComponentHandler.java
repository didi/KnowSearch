package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;

/**
 * @author didi
 * @date 2022-07-20 4:01 下午
 */
@org.springframework.stereotype.Component
@DefaultHandler
public class ScaleComponentHandler extends BaseComponentHandler implements ComponentHandler {

    private static final ILog LOGGER = LogFactory.getLog(ScaleComponentHandler.class);

    @Autowired
    private TaskDomainService taskDomainService;


    @Override
    public Integer eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralScaleComponent scaleComponent = (GeneralScaleComponent) componentEvent.getSource();
            Component component = componentDomainService.getComponentById(scaleComponent.getComponentId()).getData();
            scaleComponent.setTemplateId(getTemplateId(component));
            String content = JSONObject.toJSON(scaleComponent).toString();
            Map<String, List<Tuple<String, Integer>>> groupToIpList = getGroup2HostMap(scaleComponent.getGroupConfigList());
            int taskId = taskDomainService.createTask(content, scaleComponent.getType(),
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
            Map<String, Set<String>> groupName2HostNormalStatusMap = getGroupName2HostMapByStatus(taskId, status -> status == TaskStatusEnum.SUCCESS.getStatus());
            GeneralScaleComponent scaleComponent = JSON.parseObject(content, GeneralScaleComponent.class);
            Component component = ComponentAssembler.toScaleComponent(scaleComponent);
            componentDomainService.scaleComponent(component, groupName2HostNormalStatusMap, scaleComponent.getType());
            LOGGER.info("scale[{}] handler success", content);
        } catch (Exception e) {
            LOGGER.error("task finish process error.", e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public Integer getOperationType() {
        return OperationEnum.SCALE.getType();
    }

    @Override
    public Result<Void> execute(Task task) {
        return taskDomainService.executeDeployTask(task);
    }
}
