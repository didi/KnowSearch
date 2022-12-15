package com.didi.arius.gateway.rest;

import com.didi.arius.gateway.common.utils.Convert;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.didi.arius.gateway"})
public class AriusGatewayApplication {

    public static void main(String[] args) {
        System.setProperty("hostName", Convert.getIpAddr());
        SpringApplication.run(AriusGatewayApplication.class, args);
    }
    
  
}