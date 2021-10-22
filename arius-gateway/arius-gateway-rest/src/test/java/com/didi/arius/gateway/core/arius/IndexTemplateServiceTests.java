package com.didi.arius.gateway.core.arius;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metadata.TemplateInfo;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import com.google.common.collect.Lists;
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
public class IndexTemplateServiceTests {

    private static String INDEX_NAME = "cn_record.arius.template.value";
    private static String INDEX_NAME2 = "cn_record.arius.template.value_2021-05";
    private static String CLUSTER_NAME = "dc-es02";

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Before
    public void setUp() {
    }

    @Test
    public void testGetTemplateExpressionMap() throws InterruptedException {
        Thread.sleep(1000L);
        final Map<String, Map<String, TemplateInfo>> templateExpressionMap = indexTemplateService.getTemplateExpressionMap();
        System.out.println(JSON.toJSONString(templateExpressionMap));
    }

    @Test
    public void testGetTemplateAliasMap() throws InterruptedException {
        Thread.sleep(1000L);
        final Map<String, Map<String, TemplateInfo>> templateExpressionMap = indexTemplateService.getTemplateAliasMap();
        System.out.println(JSON.toJSONString(templateExpressionMap));
    }

    @Test
    public void testGetIndexTemplate() throws InterruptedException {
        Thread.sleep(1000L);
        final IndexTemplate indexTemplate = indexTemplateService.getIndexTemplate(INDEX_NAME);
        System.out.println(JSON.toJSONString(indexTemplate));
        assertEquals(indexTemplate != null, true);
    }

    @Test
    public void testGetIndexTemplateByTire() throws InterruptedException {
        Thread.sleep(2000L);
        final IndexTemplate indexTemplate = indexTemplateService.getIndexTemplateByTire(INDEX_NAME);
        System.out.println(JSON.toJSONString(indexTemplate));
        assertEquals(indexTemplate != null, true);
    }

    @Test
    public void testGetIndexTemplateMap() throws InterruptedException {
        Thread.sleep(2000L);
        final Map<String, IndexTemplate> indexTemplateMap = indexTemplateService.getIndexTemplateMap();
        System.out.println(JSON.toJSONString(indexTemplateMap));
    }

    @Test
    public void testResetIndexTemplateInfo(){
        indexTemplateService.resetIndexTemplateInfo();
    }

    @Test
    public void testGetIndexAlias() throws InterruptedException {
        Thread.sleep(2000L);
        final String indexAlias = indexTemplateService.getIndexAlias(INDEX_NAME);
        System.out.println(indexAlias);
    }

    @Test
    public void testCheckIndex() throws InterruptedException {
        Thread.sleep(2000L);
        final boolean b = indexTemplateService.checkIndex(INDEX_NAME+"_2021-05", Lists.newArrayList(INDEX_NAME+"*"));
        assertEquals(b, true);
        final boolean b1 = indexTemplateService.checkIndex(INDEX_NAME+"_2021-05", Lists.newArrayList(INDEX_NAME+"asdf*"));
        assertEquals(b1, false);

    }

    @Test
    public void testGetIndexVersion() throws InterruptedException {
        Thread.sleep(2000L);
        final int indexVersion = indexTemplateService.getIndexVersion(INDEX_NAME, CLUSTER_NAME);
        System.out.println(indexVersion);

    }

    @Test
    public void testGetTemplateByIndexTire() throws InterruptedException {
        Thread.sleep(2000L);
        final IndexTemplate templateByIndexTire = indexTemplateService.getTemplateByIndexTire(Lists.newArrayList(INDEX_NAME, INDEX_NAME2), buildQueryContext());
        System.out.println(JSON.toJSONString(templateByIndexTire));
        assertEquals(templateByIndexTire != null, true);

    }


    private QueryContext buildQueryContext() {
        QueryContext queryContext = new QueryContext();
        AppDetail appDetail = new AppDetail();
        appDetail.setCluster(CLUSTER_NAME);
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
