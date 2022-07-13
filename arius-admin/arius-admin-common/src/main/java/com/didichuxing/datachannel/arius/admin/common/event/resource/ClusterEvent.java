package com.didichuxing.datachannel.arius.admin.common.event.resource;

import org.springframework.context.ApplicationEvent;

/**
 * Created by linyunan on 2021-06-03
 */
public abstract class ClusterEvent extends ApplicationEvent {

    protected ClusterEvent(Object source) {
        super(source);
    }
}
