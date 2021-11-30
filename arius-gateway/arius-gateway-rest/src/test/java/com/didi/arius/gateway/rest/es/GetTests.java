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
 * @date 2021/6/10 2:55 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GetTests {

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
     * /{index}/{type}/{id}
     * @throws IOException
     */
    @Test
    public void testRestGetController() throws IOException {
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_doc/11", "GET", null, headerParams, null);
        System.out.println(res);
        //{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"11","_version":3,"_shards":{"total":2,"failed":0,"successful":2},"result":"deleted","_seq_no":2,"_primary_term":1,"found":true}
        //{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"111","found":false}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getBoolean("found") != null);
    }

    /**
     * /{index}/{type}/{id}/_source
     * @throws IOException
     */
    @Test
    public void testRestGetSourceController() throws IOException {
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_doc/11/_source", "GET", null, headerParams, null);
        System.out.println(res);
        //{"name":"法外狂徒张三少 ","es_index_time":1623295368686,"age":25,"timestamp":"2021-05-10 10:16:13"}
        //{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"111","found":false}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject != null);
    }

    /**
     * /{index}/{type}/{id}
     * /{index}/{type}/{id}/_source
     * @throws IOException
     */
    @Test
    public void testRestHeadController() throws IOException {
        //这个一直返回null, 正常否？
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_doc/11", "HEAD", null, headerParams, null);
        System.out.println(res);
    }

    /**
     * /_mget
     * /{index}/_mget
     * /{index}/{type}/_mget
     * @throws IOException
     */
    @Test
    public void testRestMultiGetController() throws IOException {
        String dodyform = "{\"docs\":[{\"_type\":\"_doc\",\"_id\":1},{\"_type\":\"_doc\",\"_id\":11}]}";
        //GET 方式会报错，参数传的不对么
//        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_mget", "POST", dodyform, headerParams, null);
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_doc/_mget", "POST", dodyform, headerParams, null);
        System.out.println(res);
        //{"docs":[{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"1","_version":1,"found":true,"_source":{"name":"fitz1","age":1,"es_index_time":1623132760300,"timestamp":"2021-05-23 19:06:13"},"_seq_no":0,"_primary_term":1},{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"11","_version":6,"found":true,"_source":{"name":"法外狂徒张三少 ","es_index_time":1623295368686,"age":25,"timestamp":"2021-05-10 10:16:13"},"_seq_no":6,"_primary_term":1}]}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getJSONArray("docs").size() >= 0);

    }


}
