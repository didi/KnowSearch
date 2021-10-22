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
public class GwadminTests {

    private Map<String, String> headerParams;
    private static String indexName = "cn_record.arius.template.value";

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
    public void testAppInfoController() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwadmin/appinfo", "GET", null, headerParams, null);
        System.out.println(res);
        JSONArray jr = JSON.parseArray(res);
        assertEquals(jr.size() > 0, true);
    }

    @Test
    public void testCheckIndexModeController() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwadmin/checkIndexMode", "GET", null, headerParams, null);
        System.out.println(res);
    }

    @Test
    public void testCheckIndexModeController2() throws IOException {
        String res = HttpClient.forward("http://127.0.0.1:8200/_gwadmin/checkIndexMode/5", "GET", null, headerParams, null);
        System.out.println(res);
    }

    @Test
    public void testDataCenterInfoController() throws IOException{
        JSONArray jr = HttpClient.forward("http://127.0.0.1:8200/_gwadmin/datacenterinfo", "GET", null, headerParams, null, JSONArray.class);
        System.out.println(jr);
        assertEquals(jr.size() >= 0, true);
    }

    @Test
    public void testIndexTemplateController() throws IOException{
        String s = HttpClient.forward("http://127.0.0.1:8200/_gwadmin/indextemplate/" + indexName, "GET", null, headerParams, null);
        System.out.println(s);
    }

    @Test
    public void testIndextemplateInfoController() throws IOException{
        String s = HttpClient.forward("http://127.0.0.1:8200/_gwadmin/indextemplateInfo", "GET", null, headerParams, null);
        System.out.println(s);
    }

    @Test
    public void testSyncMetadataController() throws IOException{
        String s = HttpClient.forward("http://127.0.0.1:8200/_gwadmin/sync/metadata", "GET", null, headerParams, null);
        System.out.println(s);
    }

}
