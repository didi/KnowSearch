package com.didi.cloud.fastdump.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * Created by linyunan on 2022/9/6
 */
public abstract class BaseEvent extends ApplicationEvent {
    public BaseEvent(Object source) {
        super(source);
    }
}
