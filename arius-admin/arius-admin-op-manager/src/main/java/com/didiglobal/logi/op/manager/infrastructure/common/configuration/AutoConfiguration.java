package com.didiglobal.logi.op.manager.infrastructure.common.configuration;

import com.didiglobal.logi.op.manager.infrastructure.common.properties.OpManagerProper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author cjm
 */
@Configuration(value = "opManagerAutoConfiguration")
@EnableConfigurationProperties(OpManagerProper.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.didiglobal.logi.op.manager")
public class AutoConfiguration {

    private final OpManagerProper proper;

    public AutoConfiguration(OpManagerProper proper) {
        this.proper = proper;
    }

    public OpManagerProper getProper() {
        return proper;
    }
}