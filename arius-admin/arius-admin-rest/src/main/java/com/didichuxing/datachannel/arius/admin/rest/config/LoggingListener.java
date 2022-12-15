package com.didichuxing.datachannel.arius.admin.rest.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

/**
 * 读取yml配置传递到log4jXml中
 *
 * @author slhu
 */
@Component
public class LoggingListener implements ApplicationListener<ApplicationEvent>, Ordered {

    private static final String SPRING_LOGI_JOB_ES_PREFIX = "spring.logi-job.elasticsearch-";
    private static final String[] CONFIG_KEYS = {"address", "port", "user", "password", "index-name", "type-name"};

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationEnvironmentPreparedEvent) {
            ConfigurableEnvironment environment = ((ApplicationEnvironmentPreparedEvent) applicationEvent).getEnvironment();
            for (String config : CONFIG_KEYS) {
                //提供给日志文件读取配置的key，使用时需要在前面加上 sys:
                String key = SPRING_LOGI_JOB_ES_PREFIX + config;
                String val = environment.getProperty(key);
                if (StringUtils.isNotBlank(val)) {
                    System.setProperty(key, val);
                }
            }
        }
    }

    @Override
    public int getOrder() {
        // 当前监听器的启动顺序需要在日志配置监听器的前面，所以此处减 1
        return LoggingApplicationListener.DEFAULT_ORDER - 1;
    }

}