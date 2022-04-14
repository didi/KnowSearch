package com.didi.arius.gateway.core.dsl;

import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.service.dsl.DslAuditService;
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
public class DslAuditServiceTests {

    private static String CLUSTER = "dc-es02";
    private static String INDEX_NAME = "cn_record.arius.template.value_2021-05";


    @Autowired
    private DslAuditService dslAuditService;

    @Before
    public void setUp() {
    }

    @Test
    public void testAuditDSL() {
        String s = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"name\":\"fitz4\"}},{\"term\":{\"age\":4}}]}}}";
        BytesReference bytesReference = new BytesArray(s);
        String auditDSL = dslAuditService.auditDSL(buildQueryContext(), bytesReference, new String[]{INDEX_NAME});
        System.out.println(auditDSL);
        //V2_60AEA6D86F11C54F7ED8E5FA07BDB4E1
    }

    @Test
    public void testAuditSQL() {
        String sql = String.format("SELECT * from %s limit 1", INDEX_NAME);
        final String s = dslAuditService.auditSQL(buildQueryContext(), sql, new String[]{INDEX_NAME});
        System.out.println(s);
        //V2_F6586E887287C2426F4DDF9C245820AC
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
