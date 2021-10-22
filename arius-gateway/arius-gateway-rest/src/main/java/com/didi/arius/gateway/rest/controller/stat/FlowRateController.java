package com.didi.arius.gateway.rest.controller.stat;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.flowcontrol.FlowController;
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
 * @date 2021/5/25 4:53 下午
 */
@Controller
public class FlowRateController extends StatController {
    public static final String NAME = "flowrate";
    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/flowrate", this);
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/flowrate/{appid}", this);

    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        int appid = request.paramAsInt("appid", QueryConsts.TOTAL_APPId_ID);
        Map<Integer, FlowController> flowControllerMap = rateLimitService.getFlowControllerMap();
        String res = "";
        if (appid > 0) {
            FlowController flowController = flowControllerMap.get(appid);
            if (flowController != null) {
                res = JSON.toJSONString(flowController);
            }
        } else {
            res = JSON.toJSONString(flowControllerMap);
        }

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, res));
    }
}
