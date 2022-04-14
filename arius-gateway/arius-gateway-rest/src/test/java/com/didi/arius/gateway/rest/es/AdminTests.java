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
public class AdminTests {

    private Map<String, String> headerParams;
    private static String INDEX_NAME = "cn_record.arius.template.value";
    private static String HOST = "http://127.0.0.1:8200";

    @Before
    public void setUp() {
        String appid = "1";
        String appsecret = "azAWiJhxkho33ac";
        String token = Base64.getEncoder().encodeToString((appid + ":" + appsecret).getBytes(StandardCharsets.UTF_8));
        headerParams = new HashMap<>();
        headerParams.put("Authorization", "Basic " + token);
        headerParams.put("CLUSTER-ID", "dc-es02");
    }

    /**
     * /_cat/indices
     * /_cat/indices/{index}
     * @throws IOException
     */
    @Test
    public void testRestIndicesController() throws IOException {
        String res = HttpClient.forward(HOST+"/_cat/indices", "GET", null, headerParams, null);
        assertEquals("not support!", res);
        String res2 = HttpClient.forward(HOST+"/_cat/indices/" + INDEX_NAME, "GET", null, headerParams, null);
        System.out.println(res2);
    }

    /**
     * /_cluster/health
     * /_cluster/health/{index}
     * @throws IOException
     */
    @Test
    public void testRestClusterHealthController() throws IOException {
        String res = HttpClient.forward(HOST+"/_cluster/health", "GET", null, headerParams, null);
        System.out.println(res);
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getIntValue("active_shards") > 0);


        String res2 = HttpClient.forward(HOST+"/_cluster/health/" + INDEX_NAME, "GET", null, headerParams, null);
        System.out.println(res2);
        JSONObject jsonObject2 = JSON.parseObject(res2);
        assertEquals(true, jsonObject2.getIntValue("active_shards") > 0);
    }

    /**
     * /_nodes
     * /_nodesclean
     * /_nodes/{nodeId}
     * /_nodes/{nodeId}/{metrics}
     * /_nodes/{nodeId}/info/{metrics}
     * @throws IOException
     */
    @Test
    public void testRestNodesInfoController() throws IOException {
//        String res = HttpClient.forward(HOST+"/_nodes", "GET", null, headerParams, null);
//        System.out.println(res);
//        JSONObject jsonObject = JSON.parseObject(res);
//        Integer total = jsonObject.getJSONObject("_nodes").getInteger("total");
//        assertEquals(total > 0, true);

//        String res = HttpClient.forward(HOST+"/_nodes/QkU741SpRTKzJ5gRrUxEtw/jvm", "GET", null, headerParams, null);
        String res = HttpClient.forward(HOST+"/_nodes/QkU741SpRTKzJ5gRrUxEtw/info/jvm", "GET", null, headerParams, null);
        System.out.println(res);
        JSONObject jsonObject = JSON.parseObject(res);
        Integer total = jsonObject.getJSONObject("_nodes").getInteger("total");
        assertEquals(true, total > 0);
    }

    /**
     * /_cluster/settings
     * @throws IOException
     */
    @Test
    public void testRestPutClusterSettingsController() throws IOException {
        String bodyform = "{\"persistent\":{\"cluster.routing.allocation.enable\": \"all\"}}";
        String res = HttpClient.forward(HOST+"/_cluster/settings", "PUT", bodyform, headerParams, null);
        System.out.println(res);
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getBoolean("acknowledged"));
    }

    /**
     * /_field_stats
     * /{index}/_field_stats
     * @throws IOException
     */
    @Test
    public void testRestFieldStatsController() throws IOException {
        //TODO:houxiufeng
        //报错 GET {"error":{"root_cause":[{"type":"index_out_of_bounds_exception","reason":"Index: 0, Size: 0"}],"type":"index_out_of_bounds_exception","reason":"Index: 0, Size: 0"},"status":500}
        //报错 POST {"error":{"root_cause":[{"type":"invalid_type_name_exception","reason":"Document mapping type name can't start with '_', found: [_field_stats]"}],"type":"invalid_type_name_exception","reason":"Document mapping type name can't start with '_', found: [_field_stats]"},"status":400}
//        String bodyform = "{\"fields\":[\"rating\"]}";
//        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_field_stats", "POST", bodyform, headerParams, null);
        String res = HttpClient.forward(HOST+"/_field_stats?fields=rating", "GET", null, headerParams, null);
        System.out.println(res);
    }

    /**
     * /_mapping/field/{fields}
     * /_mapping/{type}/field/{fields}
     * /{index}/_mapping/field/{fields}
     * /{index}/{type}/_mapping/field/{fields}
     * /{index}/_mapping/{type}/field/{fields}
     * @throws IOException
     */
    @Test
    public void testRestGetFieldMappingController() throws IOException {
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME + "/_mapping/field/name,age", "GET", null, headerParams, null);
        System.out.println(res);
        JSONObject jsonObject = JSON.parseObject(res);
        JSONObject indexJson = jsonObject.getJSONObject(INDEX_NAME);
        assertEquals(true, indexJson != null);

    }

    /**
     * /{index}/{type}/_mapping
     * /{index}/_mapping
     * /{index}/_mappings
     * /{index}/_mappings/{type}
     * /{index}/_mapping/{type}
     * /_mapping/{type}
     * /_mapping
     * /_mappings
     * @throws IOException
     */
    @Test
    public void testRestGetMappingController() throws IOException {
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME + "/_mapping", "GET", null, headerParams, null);
        System.out.println(res);
        JSONObject jsonObject = JSON.parseObject(res);
        JSONObject indexJson = jsonObject.getJSONObject(INDEX_NAME);
        assertEquals(true, indexJson != null);

    }

    /**
     * /{index}
     * @throws IOException
     */
    @Test
    public void testRestCreateIndexController() throws IOException {
        //TODO:houxiufeng 这个为什么不能创建
        String index = "student";
        String bodyform = "{\"mappings\":{\"properties\":{\"name\":{\"type\":\"text\"},\"age\":{\"type\":\"integer\"},\"birthday\":{\"type\":\"date\"}}}}";
        String res = HttpClient.forward(HOST+"/" + index, "PUT", bodyform, headerParams, null);
        System.out.println(res);
//        JSONObject jsonObject = JSON.parseObject(res);
//        JSONObject indexJson = jsonObject.getJSONObject(INDEX_NAME);
//        assertEquals(indexJson != null, true);

    }

    /**
     * /{index}
     * @throws IOException
     */
    @Test
    public void testRestDeleteIndexController() throws IOException {
        String index = "cn_record.arius.template.value_2021-05";
        String res = HttpClient.forward(HOST+"/" + index, "DELETE", null, headerParams, null);
        System.out.println(res);
        JSONObject jsonObject = JSON.parseObject(res);
        Boolean b = jsonObject.getBoolean("acknowledged");
        assertEquals(true, b);

    }

    /**
     * /{index}/_settings
     * @throws IOException
     */
    @Test
    public void testRestPutIndexSettingsController() throws IOException {
        //TODO:houxiufeng, 这个也失败了，为什么
        String index = "cn_record.arius.template.value_2021-05";
        String bodyform = "{\"properties\":{\"age\":{\"type\":\"long\"},\"es_index_time\":{\"type\":\"long\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"timestamp\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}}}}";
        String res = HttpClient.forward(HOST+"/" + index + "/_settings", "PUT", bodyform, headerParams, null);
        System.out.println(res);

    }



}
