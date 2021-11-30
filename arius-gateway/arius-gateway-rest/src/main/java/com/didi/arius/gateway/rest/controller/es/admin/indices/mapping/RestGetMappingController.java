package com.didi.arius.gateway.rest.controller.es.admin.indices.mapping;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.mapping.get.RestGetMappingAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * @author fitz
 * @date 2021/5/26 3:59 下午
 */
@Controller
public class RestGetMappingController extends BaseHttpRestController {
    @Autowired
    private RestGetMappingAction restGetMappingAction;

    public RestGetMappingController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(GET, "/{index}/{type}/_mapping", this);
        controller.registerHandler(GET, "/{index}/_mapping", this);
        controller.registerHandler(GET, "/{index}/_mappings", this);
        controller.registerHandler(GET, "/{index}/_mappings/{type}", this);
        controller.registerHandler(GET, "/{index}/_mapping/{type}", this);
        controller.registerHandler(GET, "/_mapping/{type}", this);
        controller.registerHandler(GET, "/_mapping", this);
        controller.registerHandler(GET, "/_mappings", this);
    }

    @Override
    protected String name() {
        return restGetMappingAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restGetMappingAction.handleRequest(queryContext);
    }
}
