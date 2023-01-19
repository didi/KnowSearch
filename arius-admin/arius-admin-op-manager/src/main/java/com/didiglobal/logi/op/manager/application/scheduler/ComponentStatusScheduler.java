package com.didiglobal.logi.op.manager.application.scheduler;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.REX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.deployment.DeploymentService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author didi
 * @date 2022-08-11 11:57 上午
 */
@org.springframework.stereotype.Component
public class ComponentStatusScheduler {
    private static final ILog LOGGER = LogFactory.getLog(ComponentStatusScheduler.class);

    private final Cache<String, Map<String, String>> packageId2TemplateIdMapCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(10000).build();

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
            LOGGER.info("start monitor task");
            //list所有有效的组件，里面的host详情也需要有效的host详情
            List<Component> componentList = componentDomainService.listComponentWithAll().getData();
            Map<String, String> packageIdToTemplate = packageId2TemplateIdMapCache.get(key, this::packageIdToTemplateMap);

            componentList.parallelStream().forEach(component -> {
                try {
                    //更新组件状态
                    int status = component.convergeHostStatus();
                    if (status != component.getStatus()) {
                        componentDomainService.updateComponent(Component.builder().id(component.getId()).status(status).build());
                    }

                    //下发任务更新组件节点状态
                    Map<String, List<String>> groupToHostList = component.groupNameToHost();
                    String templateId = packageIdToTemplate.get(component.getPackageId().toString());
                    if (null == templateId) {
                        LOGGER.error("package[{}] can not find template", component.getPackageId());
                    } else {
                        //状态需要zeus回调请求，这里给zeus下发的并发是全并发，因为无需顺序执行
                        //这里任务下发，真正的回调是再zeus侧，通过调用admin的接口来实现是否存在进程
                        //这里也需要zeus去获取配置
                        //TODO 回调未执行
                        groupToHostList.forEach((key1, value) -> {
                            Result<Integer> res = deploymentService.execute(packageIdToTemplate.get(component.getPackageId().toString()),
                                    Strings.join(value.iterator(), REX), String.valueOf(OperationEnum.STATUS.getType()),
                                    0, component.getId().toString(), key1);
                            if (res.failed()) {
                                LOGGER.error("组件[{}]分组[{}]执行失败，", component.getId(), key1, res.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    LOGGER.error("monitor component[{}] error", component.getName(), e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("monitor component status error for ", e);
        }

    }

    /**
     * 安装包id到模板map的映射
     * @return key->安装包id，value->模板id
     */
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