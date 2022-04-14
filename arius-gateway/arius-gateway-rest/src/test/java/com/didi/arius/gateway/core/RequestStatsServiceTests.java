package com.didi.arius.gateway.core;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.service.RequestStatsService;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import org.elasticsearch.rest.RestStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class RequestStatsServiceTests {


    @Autowired
    private RequestStatsService requestStatsService;

    @Before
    public void setUp() {
    }

    @Test
    public void testDealRequest() {
        requestStatsService.dealRequest();
    }

    @Test
    public void testPutQueryContext() {
        requestStatsService.putQueryContext("key1", new QueryContext());
    }

    @Test
    public void testGetQueryContext() {
        requestStatsService.putQueryContext("key1", new QueryContext());
        final QueryContext key1 = requestStatsService.getQueryContext("key1");
        assertEquals(true, key1 != null);
    }

    @Test
    public void testGetQueryKeys() {
        final List<String> queryKeys = requestStatsService.getQueryKeys();
        System.out.println(JSON.toJSONString(queryKeys));
        assertEquals(true, queryKeys.size() >= 0);
    }

    @Test
    public void testRemoveQueryContext() {
        requestStatsService.putQueryContext("key1", new QueryContext());
        final QueryContext key1 = requestStatsService.getQueryContext("key1");
        assertEquals(true, key1 != null);
        requestStatsService.removeQueryContext("key1");
        assertEquals(true, requestStatsService.getQueryContext("key1") == null);
    }

    @Test
    public void testPutActionContext() {
        requestStatsService.putActionContext("key1", new ActionContext());
    }

    @Test
    public void testGetActionContext() {
        requestStatsService.putActionContext("key1", new ActionContext());
        final ActionContext key1 = requestStatsService.getActionContext("key1");
        assertEquals(true, key1 != null);
    }


    @Test
    public void testRemoveActionContext() {
        requestStatsService.putActionContext("key1", new ActionContext());
        final ActionContext key1 = requestStatsService.getActionContext("key1");
        assertEquals(true, key1 != null);
        requestStatsService.removeActionContext("key1");
        assertEquals(true, requestStatsService.getActionContext("key1") == null);
    }

    @Test
    public void testGetActionKeys() {
        final List<String> queryKeys = requestStatsService.getActionKeys();
        System.out.println(JSON.toJSONString(queryKeys));
        assertEquals(true, queryKeys.size() >= 0);
    }

    @Test
    public void testStatsAdd() {
        requestStatsService.statsAdd("common", 5, "all", 10L, RestStatus.OK);
    }



}
