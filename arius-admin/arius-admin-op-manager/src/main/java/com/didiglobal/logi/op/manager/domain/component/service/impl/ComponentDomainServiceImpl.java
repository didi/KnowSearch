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
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.*;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.DeleteEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.event.SpringEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    public Result<Void> submitInstallComponent(GeneralInstallComponent installComponent) {
        //TODO zeus的校验，是否安装了zeus
        //发送事件，领域解耦
        publisher.publish(ComponentEvent.createInstallEvent(installComponent));
        return Result.success();
    }

    @Override
    public Result<Void> submitScaleComponent(GeneralScaleComponent scaleComponent) {
        //发送事件，领域解耦
        publisher.publish(ComponentEvent.createScaleEvent(scaleComponent));
        return Result.success();
    }

    @Override
    public Result<Void> submitConfigChangeComponent(GeneralConfigChangeComponent changeComponent) {
        //发送事件，领域解耦
        publisher.publish(ComponentEvent.createConfigChangeEvent(changeComponent));
        return Result.success();
    }

    @Override
    public Result<Void> submitRestartComponent(GeneralBaseOperationComponent restartComponent) {
        //发送事件，领域解耦
        publisher.publish(ComponentEvent.createRestartEvent(restartComponent));
        return Result.success();
    }

    @Override
    public Result<Void> submitUpgradeComponent(GeneralUpgradeComponent upgradeComponent) {
        //发送事件，领域解耦
        publisher.publish(ComponentEvent.createUpdateEvent(upgradeComponent));
        return Result.success();
    }

    @Override
    public Result<Void> submitExecuteFunctionComponent(GeneralExecuteComponentFunction executeComponentFunction) {
        //发送事件，领域解耦
        publisher.publish(ComponentEvent.createExecuteFunctionEvent(executeComponentFunction));
        return Result.success();
    }

    @Override
    public Result<List<ComponentGroupConfig>> getComponentConfig(int componentId) {
        return Result.success(componentGroupConfigRepository.getConfigByComponentId(componentId));
    }

    @Override
    public Result<Void> updateComponent(Component component) {
        componentRepository.updateComponent(component);
        return Result.success();
    }

    @Override
    public Result<List<Component>> listComponent() {
        List<Component> componentList = componentRepository.listAllComponent();
        List<ComponentHost> hosts = componentHostRepository.listComponentHost();
        List<ComponentGroupConfig> configs = componentGroupConfigRepository.listGroupConfig();
        Map<String, List<ComponentHost>> componentIdToHostMap = new HashMap<>(componentList.size());
        Map<String, List<ComponentGroupConfig>> componentIdToGroupConfigMap = new HashMap<>(componentList.size());
        hosts.forEach(componentHost -> {
            List<ComponentHost> hostList = componentIdToHostMap.get(componentHost.getComponentId().toString());
            if (null == hostList) {
                hostList = new ArrayList<>();
                componentIdToHostMap.put(componentHost.getComponentId().toString(), hostList);
            }
            hostList.add(componentHost);
        });
        configs.forEach(groupConfig -> {
            List<ComponentGroupConfig> groupConfigList = componentIdToGroupConfigMap.get(groupConfig.getComponentId().toString());
            if (null == groupConfigList) {
                groupConfigList = new ArrayList<>();
                componentIdToGroupConfigMap.put(groupConfig.getComponentId().toString(), groupConfigList);
            }
            groupConfigList.add(groupConfig);
        });
        componentList.forEach(component -> {
            component.setHostList(componentIdToHostMap.get(component.getId().toString()));
        });
        return Result.success(componentList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> createComponent(Component component) {
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
            for (String host : groupConfig.getHosts().split(Constants.SPLIT)) {
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
        Map<String , ComponentGroupConfig> oriConfigMap = new HashMap<>(configList.size());
        configList.forEach(oriConf -> {
            oriConfigMap.put(oriConf.getGroupName(), oriConf);
        });

        ScaleHandler scaleHandler = scaleHandlerFactory.getScaleHandler(type);
        for (ComponentGroupConfig config : component.getGroupConfigList()) {
            for (String host : config.getHosts().split(Constants.SPLIT)) {
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
        for (ComponentGroupConfig config : component.getGroupConfigList()) {
            config.createWithoutVersion();
            config.setVersion(String.valueOf(Integer.parseInt(null == config.getVersion() ? "1" : config.getVersion()) + 1));
            componentGroupConfigRepository.saveGroupConfig(config);
        }
        return Result.success();
    }


}
