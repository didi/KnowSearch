package com.didi.arius.gateway.rest;

import com.didi.arius.gateway.common.utils.Convert;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"com.didi.arius.gateway", "com.didiglobal.knowframework"}, exclude={DataSourceAutoConfiguration.class})
public class AriusGatewayApplication {

    public static void main(String[] args) {
        System.setProperty("hostName", Convert.getHostName());
        SpringApplication.run(AriusGatewayApplication.class, args);
    }

}
