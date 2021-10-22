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

@Controller
public class IndexTemplateController extends AdminController {

    private static final String NAME = "indextemplate";

    @Override
    public void register() {
        controller.registerHandler( RestRequest.Method.GET, "/_gwadmin/indextemplate/{template}", this);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        String template = request.param("template");

        IndexTemplate indexTemplate = indexTemplateService.getIndexTemplate(template);
        if (indexTemplate == null) {
            sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK));
            return;
        }

        sendDirectResponse(queryContext, new BytesRestResponse( RestStatus.OK, JSON.toJSONString(indexTemplate)));
    }
}
