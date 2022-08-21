package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.Tuple;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralBaseOperationComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @author didi
 * @date 2022-07-26 3:57 下午
 */
@org.springframework.stereotype.Component
@DefaultHandler
public class RestartComponentHandler extends BaseComponentHandler implements ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestartComponentHandler.class);

    @Autowired
    private TaskDomainService taskDomainService;


    @Override
    public void eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralBaseOperationComponent baseOperationComponent = (GeneralBaseOperationComponent) componentEvent.getSource();

            baseOperationComponent.setTemplateId(getTemplateId(baseOperationComponent.getComponentId()));
            String content = JSONObject.toJSON(baseOperationComponent).toString();
            Map<String, List<Tuple<String, Integer>>> groupToIpList = getGroup2HostMap(baseOperationComponent.getGroupConfigList());
            taskDomainService.createTask(content, componentEvent.getOperateType(),
                    componentEvent.getDescribe(), baseOperationComponent.getAssociationId(), groupToIpList);
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public void taskFinishProcess(String content) throws ComponentHandlerException {
        //暂时不用操作
    }

    @Override
    public Integer getOperationType() {
        return OperationEnum.RESTART.getType();
    }

    @Override
    public Result<Void> execute(Task task) {
        return taskDomainService.executeDeployTask(task);
    }
}

