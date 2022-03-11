package com.didichuxing.datachannel.arius.admin;

import com.didichuxing.datachannel.arius.admin.common.util.YamlUtil;
import com.didichuxing.datachannel.arius.admin.rest.AriusAdminApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author d06679
 * @date 2019/4/11
 *
 * 得使用随机端口号，这样行执行单元测试的时候，不会出现端口号占用的情况
 */
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AriusAdminApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AriusAdminApplicationTest {

    protected HttpHeaders headers;

    @LocalServerPort
    private Integer port;

    @BeforeEach
    public void setUp() {
        // 获取 springboot server 监听的端口号
        // port = applicationContext.getWebServer().getPort();
        System.out.println(String.format("port is : [%d]", port));

        headers = new HttpHeaders();
        headers.add("X-SSO-USER", "zhaoqingrong");
    }

    @Test
    public void test() {
        Assertions.assertNotNull(port);
    }
}