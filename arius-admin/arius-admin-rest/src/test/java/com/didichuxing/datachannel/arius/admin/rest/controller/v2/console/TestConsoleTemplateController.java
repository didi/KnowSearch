package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class TestConsoleTemplateController extends AriusAdminApplicationTests {

    private static final String URL_CONSOLE_TEMPLATE = "/v2/console/template";

    @Test
    public void testIndicesList(){
        ResponseEntity<Result> resp = restTemplate.getForEntity(baseUrl + URL_CONSOLE_TEMPLATE + "/indices/list?appId=1587", Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<Tuple<String/*index*/, String/*cluster*/>>> result = JSON.parseObject(resp.getBody().toString(),
                new TypeReference<Result<List<Tuple<String/*index*/, String/*cluster*/>>>>() {
                }.getType());
        Assert.assertTrue(result.getData().size() > 0);
    }

    @Test
    public void testAliasesList(){
        ResponseEntity<Result> resp = restTemplate.getForEntity(baseUrl + URL_CONSOLE_TEMPLATE + "/aliases/list?appId=1587", Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<Tuple<String/*index*/, String/*cluster*/>>> result = JSON.parseObject(resp.getBody().toString(),
                new TypeReference<Result<List<Tuple<String/*index*/, String/*cluster*/>>>>() {
                }.getType());
        Assert.assertTrue(result.getData().size() > 0);
    }
}
