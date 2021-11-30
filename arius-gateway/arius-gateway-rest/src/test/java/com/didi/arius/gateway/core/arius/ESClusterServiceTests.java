package com.didi.arius.gateway.core.arius;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.RestConsts;
import com.didi.arius.gateway.common.metadata.*;
import com.didi.arius.gateway.core.service.arius.ESClusterService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.rest.RestRequest;
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
public class ESClusterServiceTests {

    private static String clusterName = "dc-es02";

    @Autowired
    private ESClusterService esClusterService;

    @Before
    public void setUp() {
    }

    @Test
    public void testGetDetailLogFlag(){
        final Map<String, ESCluster> stringESClusterMap = esClusterService.listESCluster();
        System.out.println(JSON.toJSONString(stringESClusterMap));
        assertEquals(true, stringESClusterMap != null);
    }

    @Test
    public void testResetESClusaterInfo(){
        esClusterService.resetESClusaterInfo();
    }

    @Test
    public void testGetClient(){
        QueryContext queryContext = buildQueryContext();
        final ESClient client = esClusterService.getClient(queryContext, RestConsts.NULL_ACTION);
        System.out.println(JSON.toJSONString(client));
        assertEquals(true, client != null);
    }

    @Test
    public void testGetClient2(){
        QueryContext queryContext = buildQueryContext();
        IndexTemplate indexTemplate = new IndexTemplate();
        TemplateClusterInfo templateClusterInfo = new TemplateClusterInfo();
        templateClusterInfo.setCluster(clusterName);
        indexTemplate.setMasterInfo(templateClusterInfo);
        final ESClient client = esClusterService.getClient(queryContext, indexTemplate, RestConsts.NULL_ACTION);
        System.out.println(JSON.toJSONString(client));
        assertEquals(true, client != null);
    }

    @Test
    public void testGetClientFromCluster(){
        QueryContext queryContext = buildQueryContext();
        final ESClient client = esClusterService.getClientFromCluster(queryContext, clusterName, RestConsts.NULL_ACTION);
        System.out.println(JSON.toJSONString(client));
        assertEquals(true, client != null);
    }

    @Test
    public void testGetWriteClient(){
        IndexTemplate indexTemplate = new IndexTemplate();
        TemplateClusterInfo templateClusterInfo = new TemplateClusterInfo();
        templateClusterInfo.setCluster(clusterName);
        indexTemplate.setMasterInfo(templateClusterInfo);
        final ESClient client = esClusterService.getWriteClient(indexTemplate, RestConsts.NULL_ACTION);
        System.out.println(JSON.toJSONString(client));
        assertEquals(true, client != null);
    }


    private QueryContext buildQueryContext() {
        QueryContext queryContext = new QueryContext();
        AppDetail appDetail = new AppDetail();
        appDetail.setCluster(clusterName);
        queryContext.setAppDetail(appDetail);
        RestRequest request = new RestRequest() {
            @Override
            public Method method() {
                return null;
            }

            @Override
            public String uri() {
                return null;
            }

            @Override
            public String rawPath() {
                return "/_gwadmin/appinfo";
            }

            @Override
            public boolean hasContent() {
                return false;
            }

            @Override
            public BytesReference content() {
                return null;
            }

            @Override
            public String header(String s) {
                return null;
            }

            @Override
            public Iterable<Map.Entry<String, String>> headers() {
                return null;
            }

            @Override
            public boolean hasParam(String s) {
                return false;
            }

            @Override
            public String param(String s) {
                return null;
            }

            @Override
            public Map<String, String> params() {
                return null;
            }

            @Override
            public String param(String s, String s1) {
                return null;
            }
        };
        queryContext.setRequest(request);
        return queryContext;
    }




}
