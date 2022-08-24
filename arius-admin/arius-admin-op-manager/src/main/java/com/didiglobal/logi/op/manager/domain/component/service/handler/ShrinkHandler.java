package com.didiglobal.logi.op.manager.domain.component.service.handler;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentGroupConfigRepository;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentHostRepository;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.DeleteEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

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
}
