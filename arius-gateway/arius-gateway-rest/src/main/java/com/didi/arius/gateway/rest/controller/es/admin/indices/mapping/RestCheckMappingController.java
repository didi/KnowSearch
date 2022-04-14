package com.didi.arius.gateway.rest.controller.es.admin.indices.mapping;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.RestCheckMappingAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestRequest.Method.PUT;

@Controller
public class RestCheckMappingController extends BaseHttpRestController {

    @Autowired
    RestCheckMappingAction restCheckMappingAction;

    @Override
    protected void register() {
        controller.registerHandler(PUT, "/{index}/{type}/_mapping/check", this);
        controller.registerHandler(POST, "/{index}/{type}/_mapping/check", this);
    }

    @Override
    public String name() {
        return restCheckMappingAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restCheckMappingAction.handleRequest(queryContext);
    }
}
