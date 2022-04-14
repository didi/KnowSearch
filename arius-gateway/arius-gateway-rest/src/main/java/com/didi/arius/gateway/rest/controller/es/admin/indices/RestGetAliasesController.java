package com.didi.arius.gateway.rest.controller.es.admin.indices;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.RestGetAliasesAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.HEAD;

/**
 * @author didi
 * @date 2021-09-24 3:33 下午
 */
@Controller
public class RestGetAliasesController extends BaseHttpRestController {
    @Autowired
    RestGetAliasesAction restGetAliasesAction;

    @Override
    protected void register() {
        controller.registerHandler(GET, "/{index}/_alias", this);
        controller.registerHandler(HEAD, "/{index}/_alias", this);
        controller.registerHandler(GET, "/{index}/_alias/{name}", this);
        controller.registerHandler(HEAD, "/{index}/_alias/{name}", this);
    }

    @Override
    protected String name() {
        return restGetAliasesAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restGetAliasesAction.handleRequest(queryContext);
    }
}
