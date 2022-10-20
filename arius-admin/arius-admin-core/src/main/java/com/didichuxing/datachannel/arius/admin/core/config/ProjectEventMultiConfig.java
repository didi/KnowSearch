package com.didichuxing.datachannel.arius.admin.core.config;

import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusOpThreadPool;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.util.ErrorHandler;

/**
 * 设置时间广播执行的线程池,所有的事件都会异步执行
 * 所以事件的监听者在处理消息时,如果处理失败,需要打error日志,通过监控感知;
 * 事件的发布线程不会感知,保证核心流程不受事件发布影响
 * @author d06679
 * @date 2019/5/24
 */
@Configuration
public class ProjectEventMultiConfig {

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(AriusOpThreadPool ariusOpThreadPool) {
        SimpleApplicationEventMulticaster applicationEventMulticaster = new SimpleApplicationEventMulticaster();
        applicationEventMulticaster.setTaskExecutor(ariusOpThreadPool);
        applicationEventMulticaster.setErrorHandler(new ErrorHandler() {
            @Override
            public void handleError(Throwable t) {
                LogFactory.getLog(ProjectEventMultiConfig.class).error("class=ProjectEventMultiConfig||method=ApplicationEventMulticaster||ErrorMsg={}",t.getMessage());
            }
        });
        return applicationEventMulticaster;
    }
}