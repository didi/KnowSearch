package com.didi.arius.gateway.rest.controller.check;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.FieldInfo;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.service.dsl.DslAggsAnalyzerService;
import com.didi.arius.gateway.rest.controller.AdminController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * @author fitz
 * @date 2021/5/25 2:54 下午
 */
@Controller
public class GetMergeMappingController extends AdminController {

    public static final String NAME = "getMergeMapping";

    @Autowired
    private DslAggsAnalyzerService dslAggsAnalyzerService;

    public GetMergeMappingController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_check/mergeMapping/{index}", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        String[] indices = Strings.splitStringByCommaToArray(request.param("index"));
        Map<String, FieldInfo> mappings = dslAggsAnalyzerService.mergeMappings(indices, null);
        String strMappings = JSON.toJSONString(mappings);

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, XContentType.JSON.restContentType(), strMappings));

    }
}
