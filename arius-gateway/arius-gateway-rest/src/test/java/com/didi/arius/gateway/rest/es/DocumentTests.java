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
public class DocumentTests {

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
     * /{index}/_doc/{id}
     * /{index}/{type}/{id}
     * @throws IOException
     */
    @Test
    public void testRestDeleteController() throws IOException {
        /**
         * 这里出现一个注册错误，但不影响执行结果，后面看看
         * Error registering metric:service=arius,name=gateway_index_cn_record.arius.template.value*_delete
         * javax.management.RuntimeOperationsException: null
         */
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_doc/31", "DELETE", null, headerParams, null);
        System.out.println(res);
        //{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"41","_version":3,"_shards":{"total":2,"failed":0,"successful":2},"result":"deleted","_seq_no":2,"_primary_term":1,"found":true}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getJSONObject("_shards") != null);
    }

    /**
     * /{index}/_doc
     * /{index}/_doc/{id}
     * /{index}/_create/{id}
     * /{index}/{type}/{id}/_create
     * @throws IOException
     */
    @Test
    public void testRestIndexController() throws IOException {
        /**
         * 这里出现一个注册错误，但不影响执行结果，后面看看
         * Error registering metric:service=arius,name=gateway_index_cn_record.arius.template.value*_index
         * javax.management.RuntimeOperationsException: null
         */
        String bodyform = "{\"name\":\"fitz2\",\"age\":22,\"timestamp\":\"2021-06-10 10:06:13\"}";
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_doc", "POST", bodyform, headerParams, null);
        System.out.println(res);
        //{"_index":"cn_record.arius.template.value_2021-06","_type":"_doc","_id":"DKHM83kBmWfLM8BR_GVW","_version":1,"_shards":{"total":2,"failed":0,"successful":2},"result":"created","_seq_no":0,"_primary_term":1,"created":true}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getJSONObject("_shards") != null);
    }

    /**
     * /{index}/_doc
     * /{index}/_doc/{id}
     * /{index}/_create/{id}
     * /{index}/{type}/{id}/_create
     * @throws IOException
     */
    @Test
    public void testRestIndexController2() throws IOException {
        /**
         * 这里出现一个注册错误，但不影响执行结果，后面看看
         * Error registering metric:service=arius,name=gateway_index_cn_record.arius.template.value*_index
         * javax.management.RuntimeOperationsException: null
         */
        String bodyform = "{\"name\":\"fitz3\",\"age\":23,\"timestamp\":\"2021-06-10 10:16:13\"}";
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_doc/14", "POST", bodyform, headerParams, null);
        System.out.println(res);
        //{"_index":"cn_record.arius.template.value_2021-06","_type":"_doc","_id":"DKHM83kBmWfLM8BR_GVW","_version":1,"_shards":{"total":2,"failed":0,"successful":2},"result":"created","_seq_no":0,"_primary_term":1,"created":true}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getJSONObject("_shards") != null);
    }

    /**
     * /{index}/_doc
     * /{index}/_doc/{id}
     * /{index}/_create/{id}
     * /{index}/{type}/{id}/_create
     * @throws IOException
     */
    @Test
    public void testRestIndexController3() throws IOException {
        /**
         * 这里出现一个注册错误，但不影响执行结果，后面看看
         * Error registering metric:service=arius,name=gateway_index_cn_record.arius.template.value*_index
         * javax.management.RuntimeOperationsException: null
         */
        String bodyform = "{\"name\":\"fitz4\",\"age\":24,\"timestamp\":\"2021-06-10 10:16:13\"}";
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_create/15", "POST", bodyform, headerParams, null);
        System.out.println(res);
        //{"_index":"cn_record.arius.template.value_2021-06","_type":"_doc","_id":"15","_version":1,"_shards":{"total":2,"failed":0,"successful":2},"result":"created","_seq_no":0,"_primary_term":1,"created":true}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getJSONObject("_shards") != null);
    }

    /**
     * /{index}/_doc
     * /{index}/_doc/{id}
     * /{index}/_create/{id}
     * /{index}/{type}/{id}/_create
     * @throws IOException
     */
    @Test
    public void testRestIndexController4() throws IOException {
        /**
         * 这里出现一个注册错误，但不影响执行结果，后面看看
         * Error registering metric:service=arius,name=gateway_index_cn_record.arius.template.value*_index
         * javax.management.RuntimeOperationsException: null
         */
        String bodyform = "{\"name\":\"fitz5\",\"age\":25,\"timestamp\":\"2021-06-10 10:16:13\"}";
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_doc/16/_create", "POST", bodyform, headerParams, null);
        System.out.println(res);
        //{"_index":"cn_record.arius.template.value_2021-06","_type":"_doc","_id":"16","_version":1,"_shards":{"total":2,"failed":0,"successful":2},"result":"created","_seq_no":1,"_primary_term":1,"created":true}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getJSONObject("_shards") != null);
    }

    /**
     * /{index}/_update/{id}
     * /{index}/{type}/{id}/_update
     * @throws IOException
     */
    @Test
    public void testRestUpdateController() throws IOException {
        /**
         * 这里出现一个注册错误，但不影响执行结果，后面看看
         * Error registering metric:service=arius,name=gateway_index_cn_record.arius.template.value*_update
         * javax.management.RuntimeOperationsException: null
         */
        String bodyform = "{\"doc\":{\"name\":\"法外狂徒张三 \",\"age\":24,\"timestamp\":\"2021-05-10 10:16:13\"}}";
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_update/11", "POST", bodyform, headerParams, null);
        System.out.println(res);
        //{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"11","_version":2,"_shards":{"total":2,"failed":0,"successful":2},"result":"updated","_seq_no":2,"_primary_term":1}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getJSONObject("_shards") != null);
    }

    /**
     * /{index}/_update/{id}
     * /{index}/{type}/{id}/_update
     * @throws IOException
     */
    @Test
    public void testRestUpdateController2() throws IOException {
        /**
         * 这里出现一个注册错误，但不影响执行结果，后面看看
         * Error registering metric:service=arius,name=gateway_index_cn_record.arius.template.value*_update
         * javax.management.RuntimeOperationsException: null
         */
        String bodyform = "{\"doc\":{\"name\":\"法外狂徒张三少 \",\"age\":25,\"timestamp\":\"2021-05-10 10:16:13\"}}";
        String res = HttpClient.forward(HOST+"/" + INDEX_NAME +"/_doc/11/_update", "POST", bodyform, headerParams, null);
        System.out.println(res);
        //{"_index":"cn_record.arius.template.value_2021-05","_type":"_doc","_id":"11","_version":5,"_shards":{"total":2,"failed":0,"successful":2},"result":"updated","_seq_no":5,"_primary_term":1}
        JSONObject jsonObject = JSON.parseObject(res);
        assertEquals(true, jsonObject.getJSONObject("_shards") != null);
    }




}
