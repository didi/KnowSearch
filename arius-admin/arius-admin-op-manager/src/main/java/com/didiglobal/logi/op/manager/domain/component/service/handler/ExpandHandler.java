package com.didiglobal.logi.op.manager.domain.component.service.handler;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentGroupConfigRepository;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentHostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author didi
 * @date 2022-08-24 10:25
 */
@Component
public class ExpandHandler implements ScaleHandler{

    @Autowired
    private ComponentHostRepository componentHostRepository;

    @Autowired
    private ComponentGroupConfigRepository componentGroupConfigRepository;

    @Override
    public void dealComponentHost(int componentId, String host, ComponentGroupConfig config) {
        ComponentHost componentHost = new ComponentHost();
        componentHost.setHost(host);
        componentHost.setProcessNum(JSON.parseObject(config.getProcessNumConfig()).getInteger(host));
        componentHost.setComponentId(componentId);
        componentHost.setGroupName(config.getGroupName());
        componentHost.create();
        componentHostRepository.saveComponentHost(componentHost);
    }

    @Override
    public void dealComponentGroupConfig(ComponentGroupConfig oriGroupConfig, ComponentGroupConfig newGroupConfig, Set<String> hosts) {
        oriGroupConfig.updateExpandConfig(newGroupConfig, hosts);
        componentGroupConfigRepository.updateGroupConfig(oriGroupConfig);
    }
}
