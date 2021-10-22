package com.didi.arius.gateway.rest.controller.stat;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.rest.controller.StatController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * @author fitz
 * @date 2021/5/25 5:32 下午
 */
@Controller
public class RequestingTcpStatsController extends StatController {
    public static final String NAME = "requestingTcpStats";
    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/tcp/requesting", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {

        List<String> queryKeys = requestStatsService.getActionKeys();
        List<ActionContext> list = Lists.newArrayListWithCapacity(queryKeys.size());
        for (String key : queryKeys) {
            ActionContext actionContext = requestStatsService.getActionContext(key);
            if (actionContext != null) {
                list.add(actionContext);
            }
        }

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, JSON.toJSONString(list)));
    }
}
