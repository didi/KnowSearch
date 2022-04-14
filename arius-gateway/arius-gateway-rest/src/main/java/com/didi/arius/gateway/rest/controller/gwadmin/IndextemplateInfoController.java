package com.didi.arius.gateway.rest.controller.gwadmin;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.rest.controller.AdminController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * @author fitz
 * @date 2021/5/25 3:04 下午
 */
@Controller
public class IndextemplateInfoController extends AdminController {

    public static final String NAME = "indextemplateInfo";

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwadmin/indextemplateInfo", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        Map<String, IndexTemplate> indexTemplateMap = indexTemplateService.getIndexTemplateMap();
        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, JSON.toJSONString(indexTemplateMap)));
    }
}
