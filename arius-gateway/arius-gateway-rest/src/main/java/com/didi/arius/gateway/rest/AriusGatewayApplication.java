package com.didi.arius.gateway.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import com.didi.arius.gateway.common.utils.Convert;

@SpringBootApplication(scanBasePackages = {"com.didi.arius.gateway", "com.didiglobal.knowframework"}, exclude={DataSourceAutoConfiguration.class})
public class AriusGatewayApplication {

    public static void main(String[] args) {
        System.setProperty("hostName", Convert.getIpAddr());
        SpringApplication.run(AriusGatewayApplication.class, args);
    }
    
  
}