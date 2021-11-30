package com.didi.arius.gateway.rest.controller.es.admin.cluster;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.action.admin.cluster.health.RestClusterHealthAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.elasticsearch.rest.RestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class RestClusterHealthController extends BaseHttpRestController {

    @Autowired
    private RestClusterHealthAction restClusterHealthAction;

    public RestClusterHealthController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler( RestRequest.Method.GET, "/_cluster/health", this);
        controller.registerHandler(RestRequest.Method.GET, "/_cluster/health/{index}", this);
    }

    @Override
    public String name() {
        return restClusterHealthAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restClusterHealthAction.handleRequest(queryContext);
    }
}
