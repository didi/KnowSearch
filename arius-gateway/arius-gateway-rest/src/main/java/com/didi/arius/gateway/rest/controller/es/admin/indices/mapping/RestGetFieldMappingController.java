package com.didi.arius.gateway.rest.controller.es.admin.indices.mapping;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.mapping.get.RestGetFieldMappingAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * @author fitz
 * @date 2021/5/26 3:57 下午
 */
@Controller
public class RestGetFieldMappingController extends BaseHttpRestController {
    @Autowired
    private RestGetFieldMappingAction restGetFieldMappingAction;

    public RestGetFieldMappingController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(GET, "/_mapping/field/{fields}", this);
        controller.registerHandler(GET, "/_mapping/{type}/field/{fields}", this);
        controller.registerHandler(GET, "/{index}/_mapping/field/{fields}", this);
        controller.registerHandler(GET, "/{index}/{type}/_mapping/field/{fields}", this);
        controller.registerHandler(GET, "/{index}/_mapping/{type}/field/{fields}", this);
    }

    @Override
    protected String name() {
        return restGetFieldMappingAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restGetFieldMappingAction.handleRequest(queryContext);
    }
}
