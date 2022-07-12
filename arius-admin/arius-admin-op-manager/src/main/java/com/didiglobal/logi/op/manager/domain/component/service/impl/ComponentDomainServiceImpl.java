package com.didiglobal.logi.op.manager.domain.component.service.impl;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.event.SpringEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author didi
 * @date 2022-07-12 2:34 下午
 */
@Service
public class ComponentDomainServiceImpl implements ComponentDomainService {

    private SpringEventPublisher publisher;

    @Override
    public Result<Void> submitInstallComponent(Component component) {
        //新建
        component.create();

        //发送事件，领域解耦
        publisher.publish(ComponentEvent.createInstallEvent(component));
        return Result.success();
    }
}
