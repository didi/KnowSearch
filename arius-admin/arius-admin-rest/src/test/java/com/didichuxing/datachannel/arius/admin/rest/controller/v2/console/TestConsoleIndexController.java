package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;


public class TestConsoleIndexController extends AriusAdminApplicationTests {

    private static final String URL_CONSOLE_INDEX = "/v2/console/index";

    @Test
    public void testIndexMapping(){
        ResponseEntity<Result> resp = restTemplate.getForEntity(baseUrl + URL_CONSOLE_INDEX + "/mapping/get?cluster=elk-new-6&index=cn_mysql_manhattan_useraccount_2018-06-06", Result.class);
        System.out.println(resp.getBody().toString());
        Result<String> result = JSON.parseObject(resp.getBody().toString(),
                new TypeReference<Result<String>>() {
                }.getType());
        Assert.assertTrue(!result.getData().isEmpty());
    }
}
