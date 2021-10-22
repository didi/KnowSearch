package com.didi.arius.gateway.core.dsl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.service.dsl.DslRewriterService;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.rest.RestRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class DslRewriterServiceTests {

    private static String CLUSTER = "dc-es02";


    @Autowired
    private DslRewriterService dslRewriterService;

    @Before
    public void setUp() {
    }

    @Test
    public void testRewriteRequest() throws Exception {
        String s = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"name\":\"fitz4\"}},{\"term\":{\"age\":4}}]}}}";
        JSONObject source = JSON.parseObject(s);
        final BytesReference bytesReference = dslRewriterService.rewriteRequest(buildQueryContext(), "6", source);
        String dsl = new String(bytesReference.toBytes());
        System.out.println(dsl);
        //{"query":{"bool":{"must":[{"term":{"name":"fitz4"}},{"term":{"age":4}}]}}}
    }

    @Test
    public void testDoTypedKey() {
        String s = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"name\":\"fitz4\"}},{\"term\":{\"age\":4}}]}}}";
        JSONObject source = JSON.parseObject(s);
        dslRewriterService.doTypedKey(buildQueryContext(), source);
    }

    @Test
    public void testRewriteRequest2() throws Exception {
        String s = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"name\":\"fitz4\"}},{\"term\":{\"age\":4}}]}}}";
        BytesReference bytesReference = new BytesArray(s);
        final BytesReference res = dslRewriterService.rewriteRequest(buildQueryContext(), "6", bytesReference);
        String dsl = new String(res.toBytes());
        System.out.println(dsl);
        //{"query":{"bool":{"must":[{"term":{"name":"fitz4"}},{"term":{"age":4}}]}}}
    }


    private QueryContext buildQueryContext() {
        QueryContext queryContext = new QueryContext();
        AppDetail appDetail = new AppDetail();
        appDetail.setCluster(CLUSTER);
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
