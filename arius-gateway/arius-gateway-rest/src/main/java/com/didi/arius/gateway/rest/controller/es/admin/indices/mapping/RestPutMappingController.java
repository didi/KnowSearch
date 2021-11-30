package com.didi.arius.gateway.rest.controller.es.admin.indices.mapping;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.RestPutMappingAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestRequest.Method.PUT;

/**
 * @author didi
 * @date 2021-09-24 3:54 下午
 */
@Controller
public class RestPutMappingController extends BaseHttpRestController {

    @Autowired
    RestPutMappingAction restPutMappingAction;

    @Override
    protected void register() {
        controller.registerHandler(PUT, "/{index}/_mapping/", this);
        controller.registerHandler(PUT, "/{index}/{type}/_mapping", this);
        controller.registerHandler(PUT, "/{index}/_mapping/{type}", this);

        controller.registerHandler(POST, "/{index}/_mapping/", this);
        controller.registerHandler(POST, "/{index}/{type}/_mapping", this);
        controller.registerHandler(POST, "/{index}/_mapping/{type}", this);

        controller.registerHandler(PUT, "/{index}/_mappings/", this);
        controller.registerHandler(PUT, "/{index}/{type}/_mappings", this);
        controller.registerHandler(PUT, "/{index}/_mappings/{type}", this);

        controller.registerHandler(POST, "/{index}/_mappings/", this);
        controller.registerHandler(POST, "/{index}/{type}/_mappings", this);
        controller.registerHandler(POST, "/{index}/_mappings/{type}", this);

    }

    @Override
    protected String name() {
        return restPutMappingAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restPutMappingAction.handleRequest(queryContext);
    }
}
