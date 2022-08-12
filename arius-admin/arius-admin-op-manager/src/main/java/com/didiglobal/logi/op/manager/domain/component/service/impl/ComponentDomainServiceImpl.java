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
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.*;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.DeleteEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.event.SpringEventPublisher;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.MAP_SIZE;

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
    public Result<Map<String, List<String>>> getComponentConfig(int componentId) {
        Map<String, List<String>> groupToIpList = new HashMap<>(MAP_SIZE);
        List<ComponentGroupConfig> groupConfigList = componentGroupConfigRepository.getConfigByComponentId(componentId);
        for (ComponentGroupConfig config : groupConfigList) {
            if (!StringUtils.isEmpty(config.getHosts())) {
                groupToIpList.put(config.getGroupName(), Arrays.asList(config.getHosts().split(Constants.SPLIT)));
            };
        }
        return Result.success(groupToIpList);
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
        List<ComponentGroupConfig>  configs = componentGroupConfigRepository.listGroupConfig();
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
            Component dependentComponent = componentRepository.getComponentById(componentId);
            dependentComponent.updateContainIds(componentId);
            componentRepository.updateContainIds(dependentComponent.getId(), dependentComponent.getContainComponentIds());
        }

        //不依赖配置的组件，更新自己的配置
        if (null == component.getDependConfigComponentId()) {
            for (ComponentGroupConfig groupConfig : component.getGroupConfigList()) {
                groupConfig.create();
                groupConfig.setComponentId(componentId);
                //创建并保存配置
                int groupId = componentGroupConfigRepository.saveGroupConfig(groupConfig);
                for (String host : groupConfig.getHosts().split(Constants.SPLIT)) {
                    //创建并保存组件host
                    //TODO 考虑下能否批量提交
                    ComponentHost componentHost = new ComponentHost();
                    componentHost.setHost(host);
                    componentHost.setProcessNum(JSON.parseObject(groupConfig.getProcessNumConfig()).getInteger(host));
                    componentHost.setComponentId(componentId);
                    componentHost.setGroupId(groupId);
                    componentHost.create();
                    componentHostRepository.saveComponentHost(componentHost);
                }

            }
        } else {
            changeComponentConfig(component.newDeployComponent());
        }
        return Result.success();
    }

    @Override
    public Result<Component> getComponentById(Integer id) {
        return Result.buildSuccess(componentRepository.getComponentById(id));

    }

    @Override
    public Result<Void> expandComponent(Component component) {
        for (ComponentGroupConfig config : component.getGroupConfigList()) {
            for (String host : config.getHosts().split(Constants.SPLIT)) {
                ComponentHost componentHost = new ComponentHost();
                componentHost.setHost(host);
                componentHost.setProcessNum(JSON.parseObject(config.getProcessNumConfig()).getInteger(host));
                componentHost.setComponentId(component.getId());
                componentHost.setGroupId(config.getId());
                componentHost.create();
                componentHostRepository.saveComponentHost(componentHost);
            }
        }
        return Result.success();
    }

    @Override
    public Result<Void> shrinkComponent(Component component) {
        for (ComponentGroupConfig config : component.getGroupConfigList()) {
            for (String host : config.getHosts().split(Constants.SPLIT)) {
                componentHostRepository.updateComponentHostStatus(component.getId(), host, config.getId(), DeleteEnum.UNINSTALL.getType());
            }
        }
        return Result.success();
    }

    @Override
    public Result<Void> changeComponentConfig(Component component) {
        for (ComponentGroupConfig config : component.getGroupConfigList()) {
            config.setVersion(String.valueOf(Integer.parseInt(config.getVersion()) + 1));
            componentGroupConfigRepository.saveGroupConfig(config);
        }
        return Result.success();
    }


}
