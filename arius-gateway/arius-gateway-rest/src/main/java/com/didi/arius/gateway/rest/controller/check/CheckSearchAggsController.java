package com.didi.arius.gateway.rest.controller.check;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.service.dsl.DslAggsAnalyzerService;
import com.didi.arius.gateway.rest.controller.AdminController;
import com.didi.arius.gateway.dsl.DslExtractionUtilV2;
import com.didi.arius.gateway.dsl.bean.ExtractResult;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * @author fitz
 * @date 2021/5/25 2:26 下午
 */
@Controller
public class CheckSearchAggsController extends AdminController {
    public static final String NAME = "checkSearchAggs";

    @Autowired
    private DslAggsAnalyzerService dslAggsAnalyzerService;

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.POST, "/_check/search_aggs/{index}", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        String[] indices = Strings.splitStringByCommaToArray(queryContext.getRequest().param("index"));
        BytesReference source = new BytesArray(queryContext.getPostBody());
        String strSource = "";
        if (source != null && source.length() > 0) {
            strSource = XContentHelper.convertToJson(source, false);
        }

        ExtractResult extractResult = DslExtractionUtilV2.extractDsl(strSource);

        String dslKey = queryContext.getAppid() + "_" + extractResult.getDslTemplateMd5();
        queryContext.setDslTemplateKey(dslKey);

        dslAggsAnalyzerService.analyzeAggs(queryContext, source, indices);

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK));

    }
}
