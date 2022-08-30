package com.didiglobal.logi.op.manager.application.scheduler;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.deployment.DeploymentService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.REX;

/**
 * @author didi
 * @date 2022-08-11 11:57 上午
 */
@org.springframework.stereotype.Component
public class ComponentStatusScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentStatusScheduler.class);

    private final Cache<String, Map<String, String>> packageId2TemplateIdMapCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(10000).build();

    @Autowired
    ComponentDomainService componentDomainService;

    @Autowired
    PackageDomainService packageDomainService;

    @Autowired
    ScriptDomainService scriptDomainService;

    @Autowired
    DeploymentService deploymentService;

    public final String key = "packageId2TemplateIdMap";

    //TODO 这里是否需要考虑分布式处理组件的监控
    @Scheduled(initialDelay = 10000, fixedDelay = 300000)
    public void monitor() {
        try {
            //list所有有限的组件，里面的host详情也需要有效的host详情
            List<Component> componentList = componentDomainService.listComponentWithAll().getData();

            Map<String, String> packageIdToTemplate = packageId2TemplateIdMapCache.get(key, this::packageIdToTemplateMap);

            componentList.parallelStream().forEach(component -> {
                Map<String, List<String>> groupToHostList = component.groupNameToHost();
                String templateId = packageIdToTemplate.get(component.getPackageId().toString());
                if (null == templateId) {
                    LOGGER.error("package[{}] can not find template", component.getPackageId());
                } else {
                    //安装目录需要zeus回调请求
                    //这里任务下发，真正的回调是再zeus侧，通过调用admin的接口来实现是否存在进程
                    //这里也需要zeus去获取配置
                    //TODO 回调未执行
                    groupToHostList.entrySet().forEach(entry -> {
                        deploymentService.execute(packageIdToTemplate.get(component.getPackageId().toString()),
                                Strings.join(entry.getValue().iterator(), REX), String.valueOf(OperationEnum.STATUS.getType()),
                                component.getId().toString(), entry.getKey());
                    });
                }
            });
        } catch (Exception e) {
            LOGGER.error("monitor component status error for ", e);
        }

    }

    @NotNull
    private Map<String, String> packageIdToTemplateMap() {
        List<Package> packageList = packageDomainService.queryPackage(Package.builder().build()).getData();
        List<Script> scriptList = scriptDomainService.queryScript(Script.builder().build()).getData();
        Map<String, String> packageIdToTemplate = new HashMap<>(packageList.size());
        Map<String, String> scriptIdToTemplate = new HashMap<>(scriptList.size());
        scriptList.forEach(script -> {
            scriptIdToTemplate.put(script.getId().toString(), script.getTemplateId());
        });
        packageList.forEach(pk -> {
            packageIdToTemplate.put(pk.getId().toString(), scriptIdToTemplate.get(pk.getScriptId().toString()));
        });
        return packageIdToTemplate;
    }
}
