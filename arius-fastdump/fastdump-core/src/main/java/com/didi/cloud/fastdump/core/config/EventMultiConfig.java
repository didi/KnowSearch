package com.didi.cloud.fastdump.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

import com.didi.cloud.fastdump.common.threadpool.FastDumpOpThreadPool;

/**
 * 设置时间广播执行的线程池,所有的事件都会异步执行
 * 所以事件的监听者在处理消息时,如果处理失败,需要打error日志,通过监控感知;
 * 事件的发布线程不会感知,保证核心流程不受事件发布影响
 */
@Configuration
public class EventMultiConfig {
    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(FastDumpOpThreadPool fastDumpOpThreadPool) {
        SimpleApplicationEventMulticaster applicationEventMulticaster = new SimpleApplicationEventMulticaster();
        applicationEventMulticaster.setTaskExecutor(fastDumpOpThreadPool);
        return applicationEventMulticaster;
    }
}