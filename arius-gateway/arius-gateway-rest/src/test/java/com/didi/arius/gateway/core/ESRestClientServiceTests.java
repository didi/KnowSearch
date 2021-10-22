package com.didi.arius.gateway.core;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class ESRestClientServiceTests {

    private static String CLUSTER = "dc-es02";

    @Autowired
    private ESRestClientService esRestClientService;

    @Before
    public void setUp() {
    }

    @Test
    public void testGetClient(){
        ESClient client = esRestClientService.getClient(CLUSTER);
        System.out.println(JSON.toJSONString(client));
        assertEquals(client != null, true);

    }

    @Test
    public void testGetClientStrict(){
        ESClient client = esRestClientService.getClientStrict(CLUSTER);
        System.out.println(JSON.toJSONString(client));
        assertEquals(client != null, true);

    }

    @Test
    public void testGetAdminClient(){
        ESClient client = esRestClientService.getAdminClient();
        System.out.println(JSON.toJSONString(client));
        assertEquals(client != null, true);

    }

    @Test
    public void testGetESClusterMap(){
        Map<String, ESCluster> esClusterMap = esRestClientService.getESClusterMap();
        System.out.println(JSON.toJSONString(esClusterMap));
        assertEquals(esClusterMap != null, true);

    }

}
