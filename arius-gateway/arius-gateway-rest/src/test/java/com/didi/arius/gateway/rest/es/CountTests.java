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
public class CountTests {

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
     * /_count
     * /{index}/_count
     * /{index}/{type}/_count
     * @throws IOException
     */
    @Test
    public void testRestCountController() throws IOException {
        String bodyform = "{\"query\":{\"term\":{\"name\":\"fitz1\"}}}";
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_count", "POST", bodyform, headerParams, null);
        //{"_shards":{"failedShard":0,"skippedShard":0,"successfulShard":16,"totalShard":16},"count":1}
        System.out.println(res);
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(jsonObject.getIntValue("count") >= 0, true);
    }

    /**
     * /_count
     * /{index}/_count
     * /{index}/{type}/_count
     * @throws IOException
     */
    @Test
    public void testRestCountController2() throws IOException {
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_count?q=name:fitz1%20AND%20age:1", "GET", null, headerParams, null);
        //{"_shards":{"failedShard":0,"skippedShard":0,"successfulShard":16,"totalShard":16},"count":1}
        System.out.println(res);
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(jsonObject.getIntValue("count") >= 0, true);
    }



}
