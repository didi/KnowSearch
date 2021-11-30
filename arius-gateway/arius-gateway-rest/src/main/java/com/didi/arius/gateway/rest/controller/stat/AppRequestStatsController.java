package com.didi.arius.gateway.rest.controller.stat;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metrics.AppMetric;
import com.didi.arius.gateway.common.metrics.SearchMetric;
import com.didi.arius.gateway.rest.controller.StatController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Controller;

/**
 * @author fitz
 * @date 2021/5/25 4:33 下午
 */
@Controller
public class AppRequestStatsController extends StatController {
    public static final String NAME = "appRequestStats";

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/app/request", this);
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/app/request/{appid}", this);
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/app/request/{appid}/{searchid}", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        int appid = request.paramAsInt("appid", QueryConsts.TOTAL_APPID_ID);
        String searchId = request.param("searchid");
        String res = "";

        if (appid > 0) {
            AppMetric appMetric = requestStatsService.getAppMetricMap().get(appid);
            if (appMetric != null) {
                if (searchId != null) {
                    SearchMetric searchMetric = appMetric.getSearchsMetricMap().get(searchId);
                    if (searchMetric != null) {
                        res = JSON.toJSONString(searchMetric);
                    }
                } else {
                    res = JSON.toJSONString(appMetric);
                }
            }
        } else {
            res = JSON.toJSONString(requestStatsService.getAppMetricMap());
        }

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, res));

    }
}
