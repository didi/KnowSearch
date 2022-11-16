package com.didiglobal.logi.op.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author didi
 * @date 2022-07-04 6:14 下午
 */

@EnableScheduling
@EnableAsync
@SpringBootApplication(scanBasePackages = "com.didiglobal.logi.op.manager")
public class OpManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpManagerApplication.class, args);
    }

}