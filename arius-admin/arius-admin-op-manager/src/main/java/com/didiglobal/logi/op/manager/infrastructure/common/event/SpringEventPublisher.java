package com.didiglobal.logi.op.manager.infrastructure.common.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author didi
 * @date 2022-07-12 3:03 下午
 */
@Component
public class SpringEventPublisher {

    @Autowired
    private ApplicationEventPublisher publisher;

    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}

