package com.didi.arius.gateway.rest.controller.es.settings;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.settings.RestGetSettingsAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;

@Controller
public class RestGetSettingsController extends BaseHttpRestController {

    @Autowired
    RestGetSettingsAction restGetSettingsAction;

    @Override
    protected void register() {
        controller.registerHandler(GET, "/{index}/_settings", this);
        controller.registerHandler(GET, "/{index}/_settings/{name}", this);
        controller.registerHandler(GET, "/{index}/_setting/{name}", this);
    }

    @Override
    public String name() {
        return restGetSettingsAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restGetSettingsAction.handleRequest(queryContext);
    }
}
