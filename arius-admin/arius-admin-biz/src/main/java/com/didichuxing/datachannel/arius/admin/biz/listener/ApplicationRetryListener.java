package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.common.exception.EventException;
import com.didichuxing.datachannel.arius.admin.common.util.EventRetryExecutor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.function.Predicate;

public abstract class ApplicationRetryListener<E extends ApplicationEvent> implements ApplicationListener<E> {

    /**
     * @param event
     */
    @SneakyThrows
    @Override
    public void onApplicationEvent(@NotNull E event) {
        // onApplicationRetryEvent 如果抛出异常则进行重试
        EventRetryExecutor.eventRetryExecute( "ApplicationRetryListener",
                () -> {onApplicationRetryEvent(event); return null;},
                3);
    }

    /**
     * 支持重试的时间处理逻辑, 如果抛出异常则进行重试
     * @param event
     * @return
     * @throws EventException
     */
    public abstract void onApplicationRetryEvent(E event) throws EventException;
}
