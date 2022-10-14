package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.common.exception.EventException;
import com.didichuxing.datachannel.arius.admin.common.util.EventRetryExecutor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public abstract class ApplicationRetryListener<E extends ApplicationEvent> implements ApplicationListener<E> {

    /**
     * @param event
     */
    @SneakyThrows
    @Override
    public void onApplicationEvent(@NotNull E event) {

        EventRetryExecutor.eventRetryExecute("",  () -> onApplicationRetryEvent(event));

    }

    public abstract boolean onApplicationRetryEvent(E event) throws EventException;

}
