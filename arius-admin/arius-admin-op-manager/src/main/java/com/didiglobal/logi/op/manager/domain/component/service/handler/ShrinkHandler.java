package com.didiglobal.logi.op.manager.domain.component.service.handler;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentGroupConfigRepository;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentHostRepository;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.DeleteEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SPLIT;

/**
 * @author didi
 * @date 2022-08-24 10:30
 */
@Component
public class ShrinkHandler implements ScaleHandler {

    @Autowired
    private ComponentHostRepository componentHostRepository;

    @Autowired
    private ComponentGroupConfigRepository componentGroupConfigRepository;

    @Override
    public void dealComponentHost(int componentId, String host, ComponentGroupConfig config) {
        componentHostRepository.unInstallComponentHost(componentId, host, config.getGroupName(), DeleteEnum.UNINSTALL.getType());
    }

    @Override
    public void dealComponentGroupConfig(ComponentGroupConfig oriGroupConfig, ComponentGroupConfig newGroupConfig, Set<String> hosts) {
        oriGroupConfig.updateShrinkConfig(newGroupConfig, hosts);
        componentGroupConfigRepository.updateGroupConfig(oriGroupConfig);
    }

    @Override
    public Result<Void> check(GeneralScaleComponent scaleComponent) {
        List<ComponentHost> componentHosts = componentHostRepository.listHostByComponentId(scaleComponent.getComponentId());
        Map<String, Set<String>> groupToHostMap = new HashMap<>(componentHosts.size());
        componentHosts.forEach(host -> {
            Set<String> hostSet = groupToHostMap.computeIfAbsent(host.getGroupName(), k -> new HashSet<>());
            hostSet.add(host.getHost());
        });
        for (GeneralGroupConfig config : scaleComponent.getGroupConfigList()) {
            Set<String> hostSet = groupToHostMap.get(config.getGroupName());
            if (null != hostSet) {
                for (String host : config.getHosts().split(SPLIT)) {
                    if (!hostSet.contains(host)) {
                        return Result.fail(ResultCode.COMPONENT_SHRINK_HOST_NOT_EXIST_ERROR);
                    }
                }
            }
        }
        return Result.success();
    }
}
