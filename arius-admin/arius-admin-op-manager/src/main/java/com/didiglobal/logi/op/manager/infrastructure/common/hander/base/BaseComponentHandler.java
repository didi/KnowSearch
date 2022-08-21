package com.didiglobal.logi.op.manager.infrastructure.common.hander.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.ProcessStatus;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.Tuple;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralBaseOperationComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.MAP_SIZE;
import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SPLIT;

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

    @Autowired
    protected TaskDomainService taskDomainService;

    protected String getTemplateId(int componentId) {
        Component component = componentDomainService.getComponentById(componentId).getData();
        Package pk = packageDomainService.queryPackage(Package.builder().id(component.getPackageId()).build()).
                getData().get(0);
        return scriptDomainService.getScriptById(pk.getScriptId()).getData().getTemplateId();
    }

    protected String getTemplateIdByPackageId(int packageId) {
        Package pk = packageDomainService.queryPackage(Package.builder().id(packageId).build()).
                getData().get(0);
        return scriptDomainService.getScriptById(pk.getScriptId()).getData().getTemplateId();
    }

    @Override
    public <T extends ProcessStatus> T getProcessStatus(Task task) throws ComponentHandlerException {
        return null;
    }

    @NotNull
    public Map<String, List<Tuple<String, Integer>>> getGroup2HostMap(List<GeneralGroupConfig> generalGroupConfigList) {
        Map<String, List<Tuple<String, Integer>>> groupToIpList = new LinkedHashMap<>(generalGroupConfigList.size());
        generalGroupConfigList.forEach(config ->
        {
            JSONObject processNumJson = JSON.parseObject(config.getProcessNumConfig());
            List<Tuple<String, Integer>> hostProcessNumTuple = new ArrayList<>();
            if (!StringUtils.isEmpty(config.getHosts())) {
                Arrays.asList(config.getHosts().split(SPLIT)).forEach(host -> {
                    hostProcessNumTuple.add(new Tuple<String, Integer>(host, (Integer) processNumJson.get(host)));
                });
                groupToIpList.put(config.getGroupName(), hostProcessNumTuple);
            }
        });
        return groupToIpList;
    }
}
