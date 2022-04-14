package com.didi.arius.gateway.rest.controller.stat;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metrics.ActionMetric;
import com.didi.arius.gateway.rest.controller.StatController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Controller;

import java.util.concurrent.ConcurrentMap;

/**
 * @author fitz
 * @date 2021/5/25 4:05 下午
 */
@Controller
public class ActionRequestStatsController extends StatController {
    public static final String NAME = "actionRequestStats";

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/action/request", this);
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/action/request/{action}", this);
    }

    @Override
    protected String name() {
        return null;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        String action = request.param("action");
        String res = "";

        if (action != null) {
            ActionMetric actionMetric = requestStatsService.getActionMetricMap().get(action);
            if (actionMetric != null) {
                res = JSON.toJSONString(actionMetric);
            }
        } else {
            ConcurrentMap<String, ActionMetric> actionMetricMap = requestStatsService.getActionMetricMap();
            res = JSON.toJSONString(actionMetricMap);
        }

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, res));

    }
}
