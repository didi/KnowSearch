package com.didi.arius.gateway.rest.controller.gwadmin;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.service.arius.AppService;
import com.didi.arius.gateway.rest.controller.AdminController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class AppInfoController extends AdminController {

    private static final String NAME = "appInfo";

    @Autowired
    private AppService appService;

    @Override
    public void register() {
        controller.registerHandler( RestRequest.Method.GET, "/_gwadmin/appinfo", this);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        Map<Integer, AppDetail> appDetails = appService.getAppDetails();

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, JSON.toJSONString(appDetails.values())));
    }
}
