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
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.PackageTypeEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.BaseComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.sun.xml.internal.ws.api.pipe.Tube;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SPLIT;

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
    public void eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralInstallComponent installComponent = (GeneralInstallComponent) componentEvent.getSource();
            String associationId = installComponent.getAssociationId();

            Package pk = packageDomainService.queryPackage(Package.builder().id(installComponent.getPackageId()).build()).
                    getData().get(0);
            if (pk.getType() == PackageTypeEnum.CONFIG_DEPENDENT.getType()) {
                installComponent.setDependConfigComponentId(installComponent.getDependComponentId());
            }
            installComponent.setTemplateId(scriptDomainService.getScriptById(pk.getScriptId()).getData().getTemplateId());
            String content = JSONObject.toJSON(installComponent).toString();

            Map<String, List<Tuple<String, Integer>>> groupToIpList = getGroup2HostMap(installComponent.getGroupConfigList());
            taskDomainService.createTask(content, componentEvent.getOperateType(),
                    componentEvent.getDescribe(), associationId, groupToIpList);
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }
    }

    @Override
    public void taskFinishProcess(int taskId, String content) throws ComponentHandlerException {
        try {
            GeneralInstallComponent installComponent = JSON.parseObject(content, GeneralInstallComponent.class);
            Component component = ConvertUtil.obj2Obj(installComponent, Component.class);
            componentDomainService.createComponent(component);
            LOGGER.info("component[{}] handler success", content);
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
