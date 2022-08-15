package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.ProcessStatus;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralExecuteComponentFunction;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SPLIT;

/**
 * @author didi
 * @date 2022-08-15 10:17
 */
public class ExecuteComponentFunction extends BaseComponentHandler implements ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteComponentFunction.class);

    @Autowired
    private TaskDomainService taskDomainService;

    @Override
    public void eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralExecuteComponentFunction executeComponentFunction = (GeneralExecuteComponentFunction) componentEvent.getSource();
            String associationId = executeComponentFunction.getAssociationId();
            executeComponentFunction.setTemplateId(getTemplateId(executeComponentFunction.getComponentId()));
            String content = JSONObject.toJSON(executeComponentFunction).toString();

            Map<String, List<String>> groupToIpList = new LinkedHashMap<>(16);
            executeComponentFunction.getGroupConfigList().forEach(config ->
            {
                if (!StringUtils.isEmpty(config.getHosts())) {
                    groupToIpList.put(config.getGroupName(), Arrays.asList(config.getHosts().split(SPLIT)));
                }
            });
            taskDomainService.createTask(content, componentEvent.getOperateType(),
                    componentEvent.getDescribe(), associationId, groupToIpList);
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }
    }

    @Override
    public void taskFinishProcess(String content) throws ComponentHandlerException {

    }

    @Override
    public <T extends ProcessStatus> T getProcessStatus(Task task) throws ComponentHandlerException {
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

