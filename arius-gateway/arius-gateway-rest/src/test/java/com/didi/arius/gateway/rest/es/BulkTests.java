package com.didi.arius.gateway.rest.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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

import static org.junit.Assert.assertEquals;

/**
 * @author fitz
 * @date 2021/6/2 2:55 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BulkTests {

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
     * /_bulk
     * @throws IOException
     */
    @Test
    public void testRestBulkController() throws IOException {
        String bodyform = "{ \"index\" : { \"_index\" : \"cn_record.arius.template.value_2021-05\", \"_id\" : \"11\" } }\n" +
                "{ \"name\" : \"张三\" }\n" +
                "{ \"index\" : { \"_index\" : \"cn_record.arius.template.value_2021-05\", \"_id\" : \"12\" } }\n" +
                "{ \"name\" : \"李四\", \"age\": 10 }\n" +
                "{ \"index\" : { \"_index\" : \"cn_record.arius.template.value_2021-05\", \"_id\" : \"13\" } }\n" +
                "{ \"name\" : \"王五\", \"age\": 11 }\n";

        String bodyform2 = "{ \"index\" : { \"_index\" : \"cn_record.arius.template.value_2021-05\", \"_id\" : \"21\" } }\n" +
                "{ \"name\" : \"张三x\" }\n" +
                "{ \"delete\" : { \"_index\" : \"cn_record.arius.template.value_2021-05\", \"_id\" : \"12\" } }\n" +
                "{ \"create\" : { \"_index\" : \"cn_record.arius.template.value_2021-05\", \"_id\" : \"23\" } }\n" +
                "{ \"name\" : \"王五x\", \"age\": 11 }\n";
        String res = HttpClient.forward(HOST+"/_bulk", "POST", bodyform2, headerParams, null);
        System.out.println(res);
    }

    /**
     * /{index}/_bulk
     * @throws IOException
     */
    @Test
    public void testRestBulkController2() throws IOException {
        String bodyform = "{ \"index\" : { \"_id\" : \"31\" } }\n" +
                "{ \"name\" : \"张三y\" }\n";

        String res = HttpClient.forward(HOST+ "/" + INDEX_NAME + "/_bulk", "POST", bodyform, headerParams, null);
        System.out.println(res);
    }

    /**
     * /{index}/{type}/_bulk
     * @throws IOException
     */
    @Test
    public void testRestBulkController3() throws IOException {
        String bodyform = "{ \"index\" : { \"_id\" : \"41\" } }\n" +
                "{ \"name\" : \"张三z\" }\n";

        String res = HttpClient.forward(HOST+ "/" + INDEX_NAME + "/_doc/_bulk", "POST", bodyform, headerParams, null);
        System.out.println(res);
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(false, jsonObject.getBoolean("errors"));
    }




}
