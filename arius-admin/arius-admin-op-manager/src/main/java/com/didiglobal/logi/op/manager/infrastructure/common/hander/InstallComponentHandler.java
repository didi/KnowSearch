package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.Tuple;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.PackageTypeEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author didi
 * @date 2022-07-16 2:15 下午
 */
@org.springframework.stereotype.Component
@DefaultHandler
public class InstallComponentHandler extends BaseComponentHandler implements ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstallComponentHandler.class);

    @Autowired
    private TaskDomainService taskDomainService;

    @Autowired
    private PackageDomainService packageDomainService;

    @Autowired
    private ScriptDomainService scriptDomainService;

    @Autowired
    private ComponentDomainService componentDomainService;

    @Override
    public Integer eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralInstallComponent installComponent = (GeneralInstallComponent) componentEvent.getSource();

            Package pk = packageDomainService.queryPackage(Package.builder().id(installComponent.getPackageId()).build()).
                    getData().get(0);
            if (pk.getType() == PackageTypeEnum.CONFIG_DEPENDENT.getType()) {
                installComponent.setDependConfigComponentId(installComponent.getDependComponentId());
            }
            installComponent.setTemplateId(scriptDomainService.getScriptById(pk.getScriptId()).getData().getTemplateId());
            String content = JSONObject.toJSON(installComponent).toString();

            Map<String, List<Tuple<String, Integer>>> groupToIpList = getGroup2HostMap(installComponent.getGroupConfigList());
            int taskId = taskDomainService.createTask(content, componentEvent.getOperateType(),
                    installComponent.getName() + componentEvent.getDescribe(), groupToIpList).getData();
            return taskId;
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }
    }

    @Override
    public void taskFinishProcess(int taskId, String content) throws ComponentHandlerException {
        try {
            Map<String, Set<String>> groupName2HostNotNormalStatusMap = getGroupName2HostMapByStatus(taskId, status -> status != TaskStatusEnum.SUCCESS.getStatus());
            GeneralInstallComponent installComponent = JSON.parseObject(content, GeneralInstallComponent.class);
            Component component = ConvertUtil.obj2Obj(installComponent, Component.class);
            if (getGroupName2HostMapByStatus(taskId, status -> status == TaskStatusEnum.SUCCESS.getStatus()).size() == 0) {
                LOGGER.warn("component[{}],节点操作都忽略了", component.getName());
            } else {
                componentDomainService.createComponent(component, groupName2HostNotNormalStatusMap);
            }
            LOGGER.info("component[{}] handler success", component.getName());
        } catch (Exception e) {
            LOGGER.error("component[{}] handler error.", content, e);
            throw new ComponentHandlerException(e);
        }
    }

    @Override
    public Integer getOperationType() {
        return OperationEnum.INSTALL.getType();
    }

    @Override
    public Result<Void> execute(Task task) {
        return taskDomainService.executeDeployTask(task);
    }


}
