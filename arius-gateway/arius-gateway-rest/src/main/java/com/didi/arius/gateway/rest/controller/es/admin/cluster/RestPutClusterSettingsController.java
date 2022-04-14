package com.didi.arius.gateway.rest.controller.es.admin.cluster;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.rest.controller.AdminController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Controller;

/**
 * @author fitz
 * @date 2021/5/26 12:54 下午
 */
@Controller
public class RestPutClusterSettingsController extends AdminController {

    public static final String NAME = "restPutClusterSettings";

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.PUT, "/_cluster/settings", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        if (client == null) {
            client = esRestClientService.getAdminClient(actionName);
        }
        directRequest(client, queryContext);
    }
}
