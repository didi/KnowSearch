package com.didichuxing.datachannel.arius.admin;

import com.didichuxing.datachannel.arius.admin.rest.AriusAdminApplication;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author d06679
 * @date 2019/4/11
 */
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AriusAdminApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AriusAdminApplicationTests {
    @Value(value = "${admin.port.web}")
    private int           port;

    @Value(value = "${admin.contextPath}")
    private String        contextPath;

    protected String      baseUrl;

    protected HttpHeaders headers;

    @BeforeEach
    public void setUp() {
        baseUrl = String.format("http://localhost:%d", port) + contextPath;
        System.out.println(String.format("port is : [%d]", port));

        headers = new HttpHeaders();
        headers.add("X-SSO-USER", "zhaoqingrong");
    }

    @Test
    public void test() {
        Assertions.assertTrue(StringUtils.isNotBlank(contextPath));
    }

}