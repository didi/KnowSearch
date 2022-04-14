package com.didi.arius.gateway.rest.controller.stat;


import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metadata.TemplateInfo;
import com.didi.arius.gateway.rest.controller.StatController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * @author fitz
 * @date 2021/5/25 4:27 下午
 */
@Controller
public class AliasInfoController extends StatController {
    public static final String NAME = "aliasInfo";
    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/alias/info", this);
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/alias/info/{cluster}", this);
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        String cluster = request.param("cluster");
        String res = "";

        if (cluster == null) {
            res = JSON.toJSONString(indexTemplateService.getTemplateAliasMap());
        } else {
            Map<String, TemplateInfo> templateInfoMap = indexTemplateService.getTemplateAliasMap().get(cluster);
            if (templateInfoMap != null) {
                res = JSON.toJSONString(templateInfoMap);
            }
        }

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, res));
    }

    @Override
    protected String name() {
        return NAME;
    }
}
