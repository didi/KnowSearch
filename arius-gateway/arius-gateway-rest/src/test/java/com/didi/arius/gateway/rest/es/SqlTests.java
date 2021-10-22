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
public class SqlTests {

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
     * /_sql
     * @throws IOException
     */
    @Test
    public void testRestSQLController() throws IOException, InterruptedException {
        //这里主要为了让定时器执行完毕， 刷新内存中的索引信息
        Thread.sleep(2000L);
        String sql = String.format("SELECT * from %s limit 1" ,INDEX_NAME);
        String res = HttpClient.forward(HOST+"/_sql", "POST", sql, headerParams, null);
        System.out.println(res);
        //{"took":1,"timed_out":false,"_shards":{"total":16,"successful":16,"failed":0},"hits":{"total":5,"max_score":1.0,"hits":[{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"1","_score":1.0,"_source":{"name":"fitz1","age":1,"es_index_time":1623132760300,"timestamp":"2021-05-23 19:06:13"}}]}}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(jsonObject.getJSONObject("hits") != null, true);
    }

    /**
     * /_sql/explain
     * @throws IOException
     */
    @Test
    public void testRestSQLExplainController() throws IOException, InterruptedException {
        //这里主要为了让定时器执行完毕， 刷新内存中的索引信息
        Thread.sleep(2000L);
        String sql = String.format("SELECT * from %s limit 1" ,INDEX_NAME);
        String res = HttpClient.forward(HOST+"/_sql/explain", "POST", sql, headerParams, null);
        //{
        //  "from" : 0,
        //  "size" : 1
        //}
        System.out.println(res);
    }


}
