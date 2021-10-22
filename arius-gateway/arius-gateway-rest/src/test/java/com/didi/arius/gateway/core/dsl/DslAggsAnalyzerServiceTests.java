package com.didi.arius.gateway.core.dsl;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.*;
import com.didi.arius.gateway.core.service.dsl.DslAggsAnalyzerService;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.rest.RestRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class DslAggsAnalyzerServiceTests {

    private static String CLUSTER = "dc-es02";
    private static String INDEX_NAME = "cn_record.arius.template.value_2021-05";


    @Autowired
    private DslAggsAnalyzerService dslAggsAnalyzerService;

    @Before
    public void setUp() {
    }

    @Test
    public void testAnalyze() {
        String s = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"name\":\"fitz4\"}},{\"term\":{\"age\":4}}]}}}";
        BytesReference bytesReference = new BytesArray(s);
        final boolean analyze = dslAggsAnalyzerService.analyze(buildQueryContext(), bytesReference, new String[]{INDEX_NAME}, CLUSTER);
        System.out.println(analyze);
    }

    @Test
    public void testAnalyzeAggs() {
        String s = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"name\":\"fitz4\"}},{\"term\":{\"age\":4}}]}}}";
        BytesReference bytesReference = new BytesArray(s);
        final boolean analyze = dslAggsAnalyzerService.analyzeAggs(buildQueryContext(), bytesReference, new String[]{INDEX_NAME});
        System.out.println(analyze);
    }

    @Test
    public void testCheckAggs() throws IOException {
        String s = "{\"size\":0,\"aggs\":{\"group_by_age\":{\"terms\":{\"field\":\"age\",\"order\":{\"_count\":\"asc\"}}}}}";
        BytesReference bytesReference = new BytesArray(s);
        String strSource = XContentHelper.convertToJson(bytesReference, false);
        JsonParser parser = new JsonParser();
        JsonObject jsonSource = parser.parse(strSource).getAsJsonObject();

        JsonObject aggsObject = getAggsObject(jsonSource);
        final AggsBukcetInfo aggsBukcetInfo = dslAggsAnalyzerService.checkAggs(aggsObject, 0, Maps.newHashMap(), new AggsAnalyzerContext());
        System.out.println(JSON.toJSONString(aggsBukcetInfo));
        //{"bucketNumber":0,"bucketType":"BUCKET","lastBucket":true,"lastBucketNumber":0,"memUsed":0}
        assertEquals(aggsBukcetInfo != null, true);
    }

    @Test
    public void testMergeMappings() {
        final Map<String, FieldInfo> stringFieldInfoMap = dslAggsAnalyzerService.mergeMappings(new String[]{INDEX_NAME}, CLUSTER);
        System.out.println(JSON.toJSONString(stringFieldInfoMap));
    }

    private JsonObject getAggsObject(JsonObject parent) {
        JsonElement aggs = parent.get("aggs");
        if (aggs == null) {
            aggs = parent.get("aggregations");
        }

        if (aggs == null) {
            return null;
        }

        return aggs.getAsJsonObject();
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
