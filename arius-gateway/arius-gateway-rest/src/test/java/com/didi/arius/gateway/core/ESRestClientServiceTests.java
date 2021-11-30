package com.didi.arius.gateway.core;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.RestConsts;
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
        ESClient client = esRestClientService.getClient(CLUSTER, RestConsts.NULL_ACTION);
        System.out.println(JSON.toJSONString(client));
        assertEquals(true, client != null);

    }

    @Test
    public void testGetClientStrict(){
        ESClient client = esRestClientService.getClientStrict(CLUSTER, RestConsts.NULL_ACTION);
        System.out.println(JSON.toJSONString(client));
        assertEquals(true, client != null);

    }

    @Test
    public void testGetAdminClient(){
        ESClient client = esRestClientService.getAdminClient(RestConsts.NULL_ACTION);
        System.out.println(JSON.toJSONString(client));
        assertEquals(true, client != null);

    }

    @Test
    public void testGetESClusterMap(){
        Map<String, ESCluster> esClusterMap = esRestClientService.getESClusterMap();
        System.out.println(JSON.toJSONString(esClusterMap));
        assertEquals(true, esClusterMap != null);

    }

    @Test
    public void testResetClients(){
        ESClient esClient = new ESClient("dc-es02", "7.6.1.302");
        ESCluster esCluster = new ESCluster();
        esCluster.setCluster("dc-es02");
        esCluster.setReadAddress("10.168.56.135:8060");
        esCluster.setHttpAddress("10.168.56.135:8060,10.169.182.134:8060");
        esCluster.setHttpWriteAddress("");
        esCluster.setClient(null);
        esCluster.setEsClient(esClient);
        esCluster.setType(ESCluster.Type.INDEX);
        esCluster.setDataCenter("cn");
        esCluster.setEsVersion("7.6.1.302");
        esCluster.setPassword("password");
        Map<String, ESCluster> map = Maps.newHashMap();
        map.put("dc-es02", esCluster);
        esRestClientService.resetClients(map);


    }



}
