package com.didichuxing.datachannel.arius.admin;

import com.didiglobal.logi.security.util.HttpRequestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.didichuxing.datachannel.arius.admin.rest.AriusAdminApplication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
    private Integer               port;
    protected MockMvc               mockMvc;
    @Autowired
    private   WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        // 获取 springboot server 监听的端口号
        // port = applicationContext.getWebServer().getPort();
        System.out.println(String.format("port is : [%d]", port));

        headers = new HttpHeaders();
        headers.add(HttpRequestUtil.USER, "admin");
    }

    @Test
    public void test() {
        Assertions.assertNotNull(port);
    }
}