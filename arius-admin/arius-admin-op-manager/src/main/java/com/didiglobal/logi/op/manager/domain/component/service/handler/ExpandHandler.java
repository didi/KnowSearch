package com.didiglobal.logi.op.manager.domain.component.service.handler;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.MAP_SIZE;
import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SPLIT;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentGroupConfigRepository;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentHostRepository;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentRepository;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.DeleteEnum;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author didi
 * @date 2022-08-24 10:25
 */
@org.springframework.stereotype.Component
public class ExpandHandler implements ScaleHandler {

    @Autowired
    private ComponentHostRepository componentHostRepository;

    @Autowired
    private ComponentGroupConfigRepository componentGroupConfigRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @Override
    public void dealComponentHost(int componentId, String host, ComponentGroupConfig config) {
        ComponentHost componentHost = new ComponentHost();
        componentHost.setHost(host);
        componentHost.setProcessNum(JSON.parseObject(config.getProcessNumConfig()).getInteger(host));
        componentHost.setComponentId(componentId);
        componentHost.setGroupName(config.getGroupName());
        Optional.ofNullable(config.getMachineSpec()).map(JSON::parseObject).map(i -> i.getString(host)).ifPresent(
                componentHost::setMachineSpec);
        componentHost.create();
        //去查询节点是否因为缩容下线了componentId, host, config.getGroupName()
        ComponentHost ch=
            componentHostRepository.selectByComponentIdAndHostAndGroupName(componentId,
            host,
            config.getGroupName(), DeleteEnum.UNINSTALL.getType());
        if (Objects.isNull(ch)) {
            componentHostRepository.saveComponentHost(componentHost);
        }else {
            componentHostRepository.updateComponentHostByComponentIdAndHostAndGroupName(componentHost);
        }
        
        
    }

    @Override
    public void dealComponentGroupConfig(ComponentGroupConfig oriGroupConfig, ComponentGroupConfig newGroupConfig, Set<String> hosts) {
        oriGroupConfig.updateExpandConfig(newGroupConfig, hosts);
        componentGroupConfigRepository.updateGroupConfig(oriGroupConfig);
    }

    @Override
    public Result<Void> check(GeneralScaleComponent scaleComponent) {
        Component dependComponent = componentRepository.getDependComponentById(scaleComponent.getComponentId());
        Map<String, Set<String>> dependGroupToHostMap = new HashMap<>(MAP_SIZE);
        if (null != dependComponent) {
            List<ComponentHost> dependHostList = componentHostRepository.listHostByComponentId(dependComponent.getId());
            dependGroupToHostMap = buildGroupToHostMap(dependHostList);
             return Result.success();
        }

        List<ComponentHost> componentHosts = componentHostRepository.listHostByComponentId(scaleComponent.getComponentId());
        Map<String, Set<String>> groupToHostMap = buildGroupToHostMap(componentHosts);
        for (GeneralGroupConfig config : scaleComponent.getGroupConfigList()) {
            Set<String> hostSet = groupToHostMap.get(config.getGroupName());
            Set<String> dependHostSet = dependGroupToHostMap.get(config.getGroupName());
            if (null != hostSet) {
                for (String host : config.getHosts().split(SPLIT)) {
                    
                    //判断扩容节点是否已经在组件host里面
                    if (hostSet.contains(host)) {
                        return Result.fail(ResultCode.COMPONENT_EXPAND_HOST_EXIST_ERROR);
                    }

                    //判断扩容节点依赖的组件是否包含该节点
                    if (null != dependComponent && null != dependHostSet && !dependHostSet.contains(host)) {
                        return Result.fail(ResultCode.COMPONENT_EXPAND_DEPEND_HOST_NOT_EXIST_ERROR);
                    }
                }
            }
        }

        return Result.success();
    }

    /**
     * 构建group到host的map
     *
     * @param hostList host列表
     * @return Map<String, Set < String>>
     */
    private Map<String, Set<String>> buildGroupToHostMap(List<ComponentHost> hostList) {
        Map<String, Set<String>> groupToHostMap = new HashMap<>(MAP_SIZE);
        hostList.forEach(host -> {
            Set<String> hostSet = groupToHostMap.computeIfAbsent(host.getGroupName(), k -> new HashSet<>());
            hostSet.add(host.getHost());
        });
        return groupToHostMap;
    }
}