package com.didi.arius.gateway.rest.es;

import com.didi.arius.gateway.common.utils.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fitz
 * @date 2021/6/10 2:55 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RestCommonTests {

    private Map<String, String> headerParams;
    private static String INDEX_NAME = "cn_record.arius.template.value_2021-05";
    private static String HOST = "http://127.0.0.1:8200";

    @Before
    public void setUp() {
//        String appid = "100000000";
//        String appsecret = "azAWiJhxkho33ac";
        String appid = "5";
        String appsecret = "helloworld";
        String token = Base64.getEncoder().encodeToString((appid + ":" + appsecret).getBytes(StandardCharsets.UTF_8));
        headerParams = new HashMap<>();
        headerParams.put("Authorization", "Basic " + token);
        headerParams.put("CLUSTER-ID", "dc-es02");
    }

    /**
     * /{index}
     * @throws IOException
     */
    @Test
    public void testRestCommonController() throws IOException {
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME, "GET", null, headerParams, null);
        System.out.println(res);
    }

    /**
     * /{index}/_settings
     * @throws IOException
     */
    @Test
    public void testRestCommonController2() throws IOException {
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME + "/_settings", "GET", null, headerParams, null);
        System.out.println(res);
    }



}
