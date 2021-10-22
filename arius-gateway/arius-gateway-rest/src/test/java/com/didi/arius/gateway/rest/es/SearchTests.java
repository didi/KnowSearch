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
public class SearchTests {

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
     * /_search/scroll
     * /_search/scroll/{scroll_id}
     * @throws IOException
     */
    @Test
    public void testRestClearScrollController() throws IOException {
        String res = HttpClient.forward(HOST+ "/_search/scroll/sdf", "DELETE", null, headerParams, null);
        System.out.println(res);
//        JSONObject jsonObject = JSON.parseObject(res);
//        assertEquals(jsonObject.getBoolean("found") != null, true);
    }


    /**
     * /_msearch
     * /{index}/_msearch
     * /{index}/{type}/_msearch
     * /_msearch/template
     * /{index}/_msearch/template
     * /{index}/{type}/_msearch/template
     * @throws IOException
     */
    @Test
    public void testRestMultiSearchController() throws IOException, InterruptedException {
        String formbody = "{\"index\":\"cn_record.arius.template.value_2021-05\"}\n" +
                "{\"query\":{\"match_all\":{}}}\n" +
                "{\"index\":\"cn_record.arius.template.value_2021-05\"}\n" +
                "{\"query\":{\"match\":{\"name\":\"张三\"}}}\n";
        Thread.sleep(1000L);
        String res = HttpClient.forward(HOST+ "/_msearch", "POST", formbody, headerParams, null);
        System.out.println(res);
        //{"responses":[{"took":2,"timed_out":false,"_shards":{"total":16,"successful":16,"failed":0},"hits":{"total":5,"max_score":"1.0","hits":[{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"11","_score":"1.0","_source":{"name":"法外狂徒张三少 ","es_index_time":1623295368686,"age":25,"timestamp":"2021-05-10 10:16:13"}},{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"1","_score":"1.0","_source":{"name":"fitz1","age":1,"es_index_time":1623132760300,"timestamp":"2021-05-23 19:06:13"}},{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"23","_score":"1.0","_source":{"name":"王五x","age":11}},{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"13","_score":"1.0","_source":{"name":"王五","age":11}},{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"21","_score":"1.0","_source":{"name":"张三x"}}]},"status":200},{"took":2,"timed_out":false,"_shards":{"total":16,"successful":16,"failed":0},"hits":{"total":2,"max_score":1.0608165,"hits":[{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"11","_score":1.0608165,"_source":{"name":"法外狂徒张三少 ","es_index_time":1623295368686,"age":25,"timestamp":"2021-05-10 10:16:13"}},{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"21","_score":0.5753642,"_source":{"name":"张三x"}}]},"status":200}]}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(jsonObject.getJSONArray("responses").size() >= 0, true);
    }

    /**
     * /_search
     * /{index}/_search
     * /{index}/{type}/_search
     * /{index}/_search/template
     * /{index}/{type}/_search/template
     * @throws IOException
     */
    @Test
    public void testRestSearchController() throws IOException, InterruptedException {
        //这里主要为了让定时器执行完毕， 刷新内存中的索引信息
        Thread.sleep(1000L);
        String formbody = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"name\":\"fitz1\"}},{\"term\":{\"age\":1}}]}}}";
        String res = HttpClient.forward(HOST+ "/" + INDEX_NAME + "/_search", "POST", formbody, headerParams, null);
        //{"took":1,"timed_out":false,"_shards":{"total":16,"successful":16,"failed":0},"hits":{"total":1,"max_score":1.9999499,"hits":[{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"1","_score":1.9999499,"_source":{"name":"fitz1","age":1,"es_index_time":1623132760300,"timestamp":"2021-05-23 19:06:13"}}]}}
        //{"took":1,"timed_out":false,"_shards":{"total":16,"successful":16,"failed":0},"hits":{"total":0,"max_score":0.0,"hits":[]}}
        System.out.println(res);
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(jsonObject.getJSONObject("hits") != null, true);
    }


    /**
     * /_search/scroll
     * /_search/scroll/{scroll_id}
     * @throws IOException
     */
    @Test
    public void testRestSearchScrollController() throws IOException, InterruptedException {
        //这里主要为了让定时器执行完毕， 刷新内存中的索引信息
        Thread.sleep(3000L);
        //TODO:houxiufeng 这里怎么传？
        String formbody = "{\"scroll\":\"1m\",\"scroll_id\":\"ZGMtZXMwMg==!MTIz\"}";
        //{"error":{"root_cause":[{"type":"illegal_argument_exception","reason":"Cannot parse scroll id"}],"type":"illegal_argument_exception","reason":"Cannot parse scroll id","caused_by":{"type":"array_index_out_of_bounds_exception","reason":"arraycopy: last source index 50 out of bounds for byte[3]"}},"status":400}
        String res = HttpClient.forward(HOST + "/_search/scroll", "POST", formbody, headerParams, null);
        System.out.println(res);
    }


    /**
     * /_spatial_msearch
     * /{index}/_spatial_msearch
     * /{index}/{type}/_spatial_msearch
     * @throws IOException
     */
    @Test
    public void testRestSpatialMultiSearchController() throws IOException {
        //TODO:houxiufeng 这个卡死，为什么。
        String formbody = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"name\":\"fitz4\"}},{\"term\":{\"age\":4}}]}}}";
        String res = HttpClient.forward(HOST+ "/" + INDEX_NAME + "/_spatial_msearch", "POST", formbody, headerParams, null);
        System.out.println(res);
    }

    /**
     * /_spatial_search
     * /{index}/_spatial_search
     * /{index}/{type}/_spatial_search
     * @throws IOException
     */
    @Test
    public void testRestSpatialSearchController() throws IOException {
        //TODO:houxiufeng 这个也报错
        String formbody = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"name\":\"fitz4\"}},{\"term\":{\"age\":4}}]}}}";
        String res = HttpClient.forward(HOST+ "/" + INDEX_NAME + "/_spatial_search", "POST", formbody, headerParams, null);
        //{"error":{"root_cause":[{"type":"runtime_exception","reason":"index config(index.spatial.s2data) not set"}],"type":"runtime_exception","reason":"index config(index.spatial.s2data) not set"},"status":500}
        System.out.println(res);
    }



}
