package com.didiglobal.logi.op.manager;

import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainServiceImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author didi
 * @date 2022-07-04 6:14 下午
 */
@MapperScan(value = "com.didiglobal.logi.op.manager.infrastructure.db.mapper")
@SpringBootApplication(scanBasePackages = "com.didiglobal.logi.op.manager")
public class OpManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpManagerApplication.class, args);
    }

}
