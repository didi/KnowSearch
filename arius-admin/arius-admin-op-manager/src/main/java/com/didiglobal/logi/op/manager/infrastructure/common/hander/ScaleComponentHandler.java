package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.Tuple;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author didi
 * @date 2022-07-20 4:01 下午
 */
@org.springframework.stereotype.Component
@DefaultHandler
public class ScaleComponentHandler extends BaseComponentHandler implements ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScaleComponentHandler.class);

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
                    component.getName() + componentEvent.getDescribe(), scaleComponent.getAssociationId(), groupToIpList).getData();
            return taskId;
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public void taskFinishProcess(int taskId, String content) throws ComponentHandlerException {
        try {
            HashMap<String, Set<String>> groupName2HostNormalStatusMap = getGroupName2HostNormalStatusMap(taskId);
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
