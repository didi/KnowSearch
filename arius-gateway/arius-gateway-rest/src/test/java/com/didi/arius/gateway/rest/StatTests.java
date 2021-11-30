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
public class StatTests {

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


    /**
     * /_gwstat/action/request
     * /_gwstat/action/request/{action}
     */
    @Test
    public void testActionRequestStatsController() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/action/request", "GET", null, headerParams, null);
//        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/action/request/test", "GET", null, headerParams, null);
        System.out.println(res);
    }

    /**
     * /_gwstat/alias/info
     * /_gwstat/alias/info/{cluster}
     */
    @Test
    public void testAliasInfoController() throws IOException {
//        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/alias/info", "GET", null, headerParams, null);
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/alias/info/dc-es02", "GET", null, headerParams, null);
        System.out.println(res);
    }

    /**
     * /_gwstat/app/request
     * /_gwstat/app/request/{appid}
     * /_gwstat/app/request/{appid}/{searchid}
     */
    @Test
    public void testAppRequestStatsController() throws IOException {
//        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/app/request", "GET", null, headerParams, null);
//        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/app/request/5", "GET", null, headerParams, null);
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/app/request/5/all", "GET", null, headerParams, null);
        System.out.println(res);
    }

    /**
     * /_gwstat/flowrate
     * /_gwstat/flowrate/{appid}
     */
    @Test
    public void testFlowRateController() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/flowrate", "GET", null, headerParams, null);
//        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/flowrate/5", "GET", null, headerParams, null);
        System.out.println(res);
    }

    /**
     * /_gwstat/rateLimitDetail
     * /_gwstat/rateLimitDetail/{appid}
     */
    @Test
    public void testRateLimitDetailController() throws IOException {
//        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/rateLimitDetail", "GET", null, headerParams, null);
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/rateLimitDetail/5", "GET", null, headerParams, null);
        System.out.println(res);
    }

    /**
     * /_gwstat/rateLimitInfo
     */
    @Test
    public void testRateLimitInfoController() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/rateLimitInfo", "GET", null, headerParams, null);
        System.out.println(res);
    }

    /**
     * /_gwstat/requesting
     */
    @Test
    public void testRequestingStatsController() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/requesting", "GET", null, headerParams, null);
        System.out.println(res);
        JSONArray jr = JSON.parseArray(res);
        assertEquals(true, jr.size() > 0);
    }


    /**
     * /_gwstat/tcp/requesting
     */
    @Test
    public void testRequestingTcpStatsController() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/tcp/requesting", "GET", null, headerParams, null);
        System.out.println(res);
        JSONArray jr = JSON.parseArray(res);
        assertEquals(true, jr.size() >= 0);
    }

    /**
     * /_gwstat/hotthreads
     * /_gwstat/hot_threads
     */
    @Test
    public void testRestHotThreadsController() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/hotthreads", "GET", null, headerParams, null);
        System.out.println(res);
    }

    /**
     * /_gwstat/template/info
     * /_gwstat/template/info/{cluster}
     */
    @Test
    public void testTemplateInfoController() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/template/info", "GET", null, headerParams, null);
//        String res = HttpClient.forward("http://127.0.0.1:8200/_gwstat/template/info/dc-es02", "GET", null, headerParams, null);
        System.out.println(res);
    }




}
