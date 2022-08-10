package com.didiglobal.logi.op.manager.infrastructure.common.hander.base;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralBaseOperationComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.MAP_SIZE;

/**
 * @author didi
 * @date 2022-07-25 5:59 下午
 */
public abstract class BaseComponentHandler implements ComponentHandler {

    @Autowired
    protected PackageDomainService packageDomainService;

    @Autowired
    protected ComponentDomainService componentDomainService;

    @Autowired
    protected ScriptDomainService scriptDomainService;

    protected String getTemplateId(int componentId) {
        Component component = componentDomainService.getComponentById(componentId).getData();
        Package pk = packageDomainService.queryPackage(Package.builder().id(component.getPackageId()).build()).
                getData().get(0);
        return scriptDomainService.getScriptById(pk.getScriptId()).getData().getTemplateId();
    }

    @NotNull
    protected Map<String, List<String>> getGroupToIpList(GeneralBaseOperationComponent baseOperationComponent) {
        Map<String, List<String>> groupToIpList = new LinkedHashMap<>(MAP_SIZE);
        baseOperationComponent.getGroupConfigList().forEach(config ->
        {
            if (!StringUtils.isEmpty(config.getHosts())) {
                groupToIpList.put(config.getGroupName(), Arrays.asList(config.getHosts().split(Constants.SPLIT)));
            }
        });
        return groupToIpList;
    }

    protected String getTemplateIdByPackageId(int packageId) {
        Package pk = packageDomainService.queryPackage(Package.builder().id(packageId).build()).
                getData().get(0);
        return scriptDomainService.getScriptById(pk.getScriptId()).getData().getTemplateId();
    }
}
