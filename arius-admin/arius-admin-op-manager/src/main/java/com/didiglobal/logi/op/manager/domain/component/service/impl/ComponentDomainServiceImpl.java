package com.didiglobal.logi.op.manager.domain.component.service.impl;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentGroupConfigRepository;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentHostRepository;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentRepository;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.component.service.handler.ScaleHandler;
import com.didiglobal.logi.op.manager.domain.component.service.handler.ScaleHandlerFactory;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.*;
import com.didiglobal.logi.op.manager.infrastructure.common.event.DomainEvent;
import com.didiglobal.logi.op.manager.infrastructure.common.event.SpringEventPublisher;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SPLIT;

/**
 * @author didi
 * @date 2022-07-12 2:34 下午
 */
@Service
public class ComponentDomainServiceImpl implements ComponentDomainService {

    @Autowired
    private SpringEventPublisher publisher;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private ComponentHostRepository componentHostRepository;

    @Autowired
    private ComponentGroupConfigRepository componentGroupConfigRepository;

    @Autowired
    private ScaleHandlerFactory scaleHandlerFactory;

    @Override
    public Result<Integer> submitInstallComponent(GeneralInstallComponent installComponent) {
        List<Component> repeatNameList = componentRepository.listAllComponent().stream().filter(component ->
                component.getName().equals(installComponent.getName())
        ).collect(Collectors.toList());
        if (!repeatNameList.isEmpty()) {
            return Result.fail(ResultCode.COMPONENT_NAME_REPEAT_ERROR);
        }
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createInstallEvent(installComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitScaleComponent(GeneralScaleComponent scaleComponent) {
        Result checkRes = scaleHandlerFactory.getScaleHandler(scaleComponent.getType()).check(scaleComponent);
        if (checkRes.failed()) {
            return checkRes;
        }
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createScaleEvent(scaleComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitConfigChangeComponent(GeneralConfigChangeComponent changeComponent) {
        List<ComponentGroupConfig> currentConfigList = componentGroupConfigRepository.getConfigByComponentId(changeComponent.getComponentId());
        for (GeneralGroupConfig config : changeComponent.getGroupConfigList()) {
            boolean isExist = currentConfigList.stream().filter(currentConfig ->
                    config.getGroupName().equals(currentConfig.getGroupName())).findFirst().isPresent();
            if (!isExist) {
                return Result.fail("更改的配置分组名不存在");
            }
        }
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createConfigChangeEvent(changeComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitRestartComponent(GeneralRestartComponent restartComponent) {

        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createRestartEvent(restartComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitUpgradeComponent(GeneralUpgradeComponent upgradeComponent) {
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createUpdateEvent(upgradeComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitRollbackComponent(GeneralRollbackComponent rollbackComponent) {
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createRollbackEvent(rollbackComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitExecuteFunctionComponent(GeneralExecuteComponentFunction executeComponentFunction) {
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createExecuteFunctionEvent(executeComponentFunction));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<List<ComponentGroupConfig>> getComponentConfig(int componentId) {
        List<ComponentGroupConfig> configList = componentGroupConfigRepository.getConfigByComponentId(componentId);
        return Result.success(getLatestGroupConfig(configList));
    }

    @Override
    public Result<Integer> updateComponent(Component component) {
        return Result.success(componentRepository.updateComponent(component));
    }

    @Override
    public Result<List<Component>> listComponentWithAll() {
        List<Component> componentList = componentRepository.listAllComponent();
        List<ComponentHost> hosts = componentHostRepository.listComponentHost();
        List<ComponentGroupConfig> configs = componentGroupConfigRepository.listGroupConfig();
        Map<String, List<ComponentHost>> componentIdToHostMap = new HashMap<>(componentList.size());
        Map<String, List<ComponentGroupConfig>> componentIdToGroupConfigMap = new HashMap<>(componentList.size());

        //构建componentIdToHostMap
        hosts.forEach(componentHost -> {
            List<ComponentHost> hostList = componentIdToHostMap.get(componentHost.getComponentId().toString());
            if (null == hostList) {
                hostList = new ArrayList<>();
                componentIdToHostMap.put(componentHost.getComponentId().toString(), hostList);
            }
            hostList.add(componentHost);
        });

        //构建componentIdToGroupConfigMap
        configs.forEach(groupConfig -> {
            List<ComponentGroupConfig> groupConfigList = componentIdToGroupConfigMap.get(groupConfig.getComponentId().toString());
            if (null == groupConfigList) {
                groupConfigList = new ArrayList<>();
                componentIdToGroupConfigMap.put(groupConfig.getComponentId().toString(), groupConfigList);
            }
            groupConfigList.add(groupConfig);
        });
        //赋值汇总
        componentList.forEach(component -> {
            component.setHostList(componentIdToHostMap.get(component.getId().toString()));
            if (null != componentIdToGroupConfigMap.get(component.getId().toString())) {
                component.setGroupConfigList(getLatestGroupConfig(componentIdToGroupConfigMap.get(component.getId().toString())));
            }

        });

        return Result.success(componentList);
    }

    /**
     * 获取组件下，最新的分组配置信息
     *
     * @param configList 组件对应的所有分组配置
     * @return List<ComponentGroupConfig> 最新分组配置信息
     */
    @NotNull
    private List<ComponentGroupConfig> getLatestGroupConfig(List<ComponentGroupConfig> configList) {
        Map<Integer, List<ComponentGroupConfig>> map = new TreeMap<>((o1, o2) -> o2 - o1);
        configList.forEach(groupConfig -> {
            List<ComponentGroupConfig> list = map.get(Integer.parseInt(groupConfig.getVersion()));
            if (null == list) {
                list = new ArrayList<>();
                map.put(Integer.parseInt(groupConfig.getVersion()), list);
            }
            list.add(groupConfig);
        });
        return map.entrySet().stream().findFirst().get().getValue();
    }

    @Override
    public Result<Boolean> hasPackageDependComponent(int packageId) {
        if (componentRepository.getComponentByPackageId(packageId).size() > 0) {
            return Result.success(true);
        }
        return Result.success(false);
    }

    @Override
    public Result<ComponentGroupConfig> getComponentConfigByGroupName(int componentId, String groupName) {
        List<ComponentGroupConfig> configList = componentGroupConfigRepository.getConfigByComponentId(componentId);
        List<ComponentGroupConfig> filterConfigList = configList.stream().filter(groupConfig -> groupConfig.getGroupName().equals(groupName)).collect(Collectors.toList());
        return Result.buildSuccess(getLatestGroupConfig(filterConfigList).stream().findFirst().get());
    }

    @Override
    public Result<Integer> reportComponentHostStatus(int componentId, String groupName, String host, int status) {
        return Result.buildSuccess(componentHostRepository.updateComponentHostStatus(componentId, host, groupName, status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createComponent(Component component) {
        //创建并保存组件
        component.create();
        int componentId = componentRepository.saveComponent(component);

        //依赖组件更新
        if (null != component.getDependComponentId()) {
            Component dependentComponent = componentRepository.getComponentById(component.getDependComponentId());
            dependentComponent.updateContainIds(componentId);
            componentRepository.updateContainIds(dependentComponent.getId(), dependentComponent.getContainComponentIds());
        }

        for (ComponentGroupConfig groupConfig : component.getGroupConfigList()) {
            //不依赖配置的组件，更新自己的配置
            if (null == component.getDependConfigComponentId()) {
                groupConfig.create();
                groupConfig.setComponentId(componentId);
                //创建并保存配置
                componentGroupConfigRepository.saveGroupConfig(groupConfig);
            }
            for (String host : groupConfig.getHosts().split(SPLIT)) {
                //创建并保存组件host
                //TODO 考虑下能否批量提交
                ComponentHost componentHost = new ComponentHost();
                componentHost.setHost(host);
                componentHost.setProcessNum(JSON.parseObject(groupConfig.getProcessNumConfig()).getInteger(host));
                componentHost.setComponentId(componentId);
                componentHost.setGroupName(groupConfig.getGroupName());
                componentHost.create();
                componentHostRepository.saveComponentHost(componentHost);
            }
        }

        //更改配置
        if (null != component.getDependConfigComponentId()) {
            changeComponentConfig(component.newDeployComponent());
        }
        return Result.success();
    }

    @Override
    public Result<Component> getComponentById(Integer id) {
        return Result.buildSuccess(componentRepository.getComponentById(id));

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> scaleComponent(Component component, Map<String, Set<String>> groupName2HostNormalStatusMap, int type) {
        //TODO 需要考虑部分成功以及部分失败场景
        //TODO 优化批量插入能力
        //TODO 扩容自动安装其他插件？
        List<ComponentGroupConfig> configList = getComponentConfig(component.getId()).getData();
        Map<String, ComponentGroupConfig> oriConfigMap = new HashMap<>(configList.size());
        configList.forEach(oriConf -> {
            oriConfigMap.put(oriConf.getGroupName(), oriConf);
        });

        ScaleHandler scaleHandler = scaleHandlerFactory.getScaleHandler(type);
        for (ComponentGroupConfig config : component.getGroupConfigList()) {
            for (String host : config.getHosts().split(SPLIT)) {
                //如果节点成功则加入,因为有些ignore之类的
                if (groupName2HostNormalStatusMap.get(config.getGroupName()).contains(host)) {
                    scaleHandler.dealComponentHost(component.getId(), host, config);
                }
            }

            if (groupName2HostNormalStatusMap.containsKey(config.getGroupName())) {
                ComponentGroupConfig oriGroupConfig = oriConfigMap.get(config.getGroupName());
                scaleHandler.dealComponentGroupConfig(oriGroupConfig, config, groupName2HostNormalStatusMap.get(config.getGroupName()));
            }
        }
        return Result.success();
    }

    @Override
    public Result<Void> changeComponentConfig(Component component) {
        Map<String, ComponentGroupConfig> oriConfigMap = new HashMap<>();
        getComponentConfig(component.getId()).getData().forEach(oriConfig -> {
            oriConfigMap.put(oriConfig.getGroupName(), oriConfig);
        });
        //用改的配置把老的配置替换带点，然后统一所有分组都版本加1
        for (ComponentGroupConfig config : component.getGroupConfigList()) {
            config.setVersion(oriConfigMap.get(config.getGroupName()).getVersion());
            config.setComponentId(component.getId());
            oriConfigMap.put(config.getGroupName(), config);
        }

        //统一新建配置
        oriConfigMap.keySet().forEach(k -> {
            ComponentGroupConfig config = oriConfigMap.get(k);
            config.createWithoutVersion();
            config.setVersion(String.valueOf(Integer.parseInt(config.getVersion()) + 1));
            componentGroupConfigRepository.saveGroupConfig(config);
        });
        return Result.success();
    }


}
