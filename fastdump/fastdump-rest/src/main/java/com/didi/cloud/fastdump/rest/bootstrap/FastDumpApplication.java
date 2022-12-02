package com.didi.cloud.fastdump.rest.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by linyunan on 2022/7/25
 */
@SpringBootApplication(scanBasePackages = {"com.didi.cloud.fastdump"})
public class FastDumpApplication {
    public static void main(String[] args){
        SpringApplication.run(FastDumpApplication.class, args);
    }
}
