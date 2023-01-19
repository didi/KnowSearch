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
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.Tuple;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralRollbackComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;

/**
 * @author didi
 * @date 2022-09-01 11:27
 */
@org.springframework.stereotype.Component
@DefaultHandler
public class RollbackComponentHandler extends BaseComponentHandler implements ComponentHandler {
    private static final ILog LOGGER = LogFactory.getLog(RollbackComponentHandler.class);

    @Autowired
    private TaskDomainService taskDomainService;

    @Override
    public Integer eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralRollbackComponent rollbackComponent = (GeneralRollbackComponent) componentEvent.getSource();
            Component component = componentDomainService.getComponentById(rollbackComponent.getComponentId()).getData();
            rollbackComponent.setTemplateId(getTemplateId(component));
            String content = JSONObject.toJSON(rollbackComponent).toString();
            String desc = component.getName() + componentEvent.getDescribe() +
                    String.format("[类型:%s]", OperationEnum.valueOfType(rollbackComponent.getType()).getDescribe());
            Map<String, List<Tuple<String, Integer>>> groupToIpList = getGroup2HostMap(rollbackComponent.getGroupConfigList());
            int taskId = taskDomainService.createTask(content, componentEvent.getOperateType(), desc, groupToIpList).getData();
            return taskId;
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public Result<Void> execute(Task task) {
        return taskDomainService.executeDeployTask(task);
    }

    @Override
    public void taskFinishProcess(int taskId, String content) throws ComponentHandlerException {
        LOGGER.info("任务{}执行成功", taskId);
    }

    @Override
    public Integer getOperationType() {
        return OperationEnum.ROLLBACK.getType();
    }
}
