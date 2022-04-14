package com.didichuxing.datachannel.arius.admin.task.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author zhangliang
 * @version : AriusAdminTaskConfiguration.java, v 0.1 2022年03月23日 15:48 zhangliang Exp $
 */
@Configuration
@ComponentScan(basePackages = {"com.didichuxing.datachannel.arius.admin.task", "com.didichuxing.datachannel.arius.admin.persistence", "com.didichuxing.datachannel.arius.admin.client", "com.didichuxing.datachannel.arius.admin.common", "com.didichuxing.datachannel.arius.admin.core", "com.didichuxing.datachannel.arius.admin.metadata", "com.didichuxing.datachannel.arius.admin.extend", "com.didichuxing.datachannel.arius.admin.biz", "com.didichuxing.datachannel.arius.admin.remote"})
@PropertySource("classpath:application.properties")
@EnableAutoConfiguration
public class AriusAdminTaskConfiguration {
}