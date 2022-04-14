package com.didi.arius.gateway.rest.controller.gwadmin;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.service.arius.DslTemplateService;
import com.didi.arius.gateway.core.service.arius.GateWayHeartBeatService;
import com.didi.arius.gateway.rest.controller.StatController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * @author fitz
 * @date 2021/5/25 3:50 下午
 */
@Controller
public class SyncMetadataController extends StatController {
    public static final String NAME = "syncMetadata";

    @Autowired
    private GateWayHeartBeatService gateWayHeartBeatService;

    @Autowired
    private DslTemplateService dslTemplateService;

    public SyncMetadataController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwadmin/sync/metadata", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        appService.resetAppInfo();
        dslTemplateService.resetDslInfo();
        dynamicConfigService.resetDynamicConfigInfo();
        esClusterService.resetESClusaterInfo();
        gateWayHeartBeatService.resetHeartBeatInfo();
        indexTemplateService.resetIndexTemplateInfo();

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK));

    }
}
