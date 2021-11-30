package com.didi.arius.gateway.rest.controller.es.admin.indices;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.RestIndexPutAliasAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestRequest.Method.PUT;

/**
 * @author didi
 * @date 2021-09-24 3:52 下午
 */
@Controller
public class RestIndexPutAliasController extends BaseHttpRestController {

    @Autowired
    RestIndexPutAliasAction restIndexPutAliasAction;

    @Override
    protected void register() {
        controller.registerHandler(PUT, "/{index}/_alias/{name}", this);
        controller.registerHandler(PUT, "/{index}/_aliases/{name}", this);
        controller.registerHandler(PUT, "/{index}/_alias", this);
        controller.registerHandler(POST, "/{index}/_alias/{name}", this);
        controller.registerHandler(POST, "/{index}/_aliases/{name}", this);
        controller.registerHandler(PUT, "/{index}/_aliases", this);
    }

    @Override
    protected String name() {
        return restIndexPutAliasAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restIndexPutAliasAction.handleRequest(queryContext);
    }
}
