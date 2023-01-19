package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.ProcessStatus;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.Tuple;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralExecuteComponentFunction;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;

/**
 * @author didi
 * @date 2022-08-15 10:17
 */
public class ExecuteComponentFunctionHandler extends BaseComponentHandler implements ComponentHandler {

    private static final ILog LOGGER = LogFactory.getLog(ExecuteComponentFunctionHandler.class);

    @Autowired
    private TaskDomainService taskDomainService;

    @Override
    public Integer eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralExecuteComponentFunction executeComponentFunction = (GeneralExecuteComponentFunction) componentEvent.getSource();
            Component component = componentDomainService.getComponentById(executeComponentFunction.getComponentId()).getData();
            executeComponentFunction.setTemplateId(getTemplateId(component));
            String content = JSONObject.toJSON(executeComponentFunction).toString();
            Map<String, List<Tuple<String, Integer>>> groupToIpList = getGroup2HostMap(executeComponentFunction.getGroupConfigList());
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

    }

    @Override
    public ProcessStatus getProcessStatus(Task task) throws ComponentHandlerException {
        return super.getProcessStatus(task);
    }

    @Override
    public Integer getOperationType() {
        return OperationEnum.FUNCTION_EXECUTE.getType();
    }

    @Override
    public Result<Void> execute(Task taskId) {
        return taskDomainService.executeFunctionTask(taskId);
    }
}

