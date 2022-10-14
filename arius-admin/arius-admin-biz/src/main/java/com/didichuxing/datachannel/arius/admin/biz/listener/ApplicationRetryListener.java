package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.common.exception.EventException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public abstract class ApplicationRetryListener<E extends ApplicationEvent> implements ApplicationListener<E> {

    public abstract boolean onApplicationRetryEvent(E event) throws EventException;

}
