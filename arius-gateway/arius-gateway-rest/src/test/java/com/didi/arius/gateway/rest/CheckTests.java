package com.didi.arius.gateway.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
public class CheckTests {

    private Map<String, String> headerParams;
    private static String INDEX_NAME = "cn_record.arius.template.value";

    @Before
    public void setUp() {
        String appid = "100000000";
        String appsecret = "azAWiJhxkho33ac";
        String token = Base64.getEncoder().encodeToString((appid + ":" + appsecret).getBytes(StandardCharsets.UTF_8));
        headerParams = new HashMap<>();
        headerParams.put("Authorization", "Basic " + token);
        headerParams.put("CLUSTER-ID", "dc-es02");
    }

    @Test
    public void testCheckSearchAggsController() throws IOException {
        //TODO:houxiufeng 这个怎么用？
        String res = HttpClient.forward("http://127.0.0.1:8200/_check/search_aggs/" + INDEX_NAME, "POST", null, headerParams, null);
        System.out.println(res);
    }

    @Test
    public void testGetMergeMappingController() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_check/mergeMapping/" + INDEX_NAME, "GET", null, headerParams, null);
        System.out.println(res);
    }


}
