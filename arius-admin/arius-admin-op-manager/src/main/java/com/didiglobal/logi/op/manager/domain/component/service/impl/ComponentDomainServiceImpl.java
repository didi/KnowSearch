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
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.event.SpringEventPublisher;
import com.didiglobal.logi.op.manager.infrastructure.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                componentHost.getCreateTime();
                componentHost.setComponentId(componentId);
                componentHost.setGroupId(groupId);
                componentHost.create();
                componentHostRepository.saveComponentHost(componentHost);
            }

        }


        return null;
    }
}
