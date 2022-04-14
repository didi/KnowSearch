package com.didi.arius.gateway.rest.controller.es.admin.indices;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.RestIndexDeleteAliasesAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.DELETE;

/**
 * @author didi
 * @date 2021-09-24 3:36 下午
 */
@Controller
public class RestIndexDeleteAliasesController extends BaseHttpRestController {

    @Autowired
    RestIndexDeleteAliasesAction restIndexDeleteAliasesAction;

    @Override
    protected void register() {
        controller.registerHandler(DELETE, "/{index}/_alias/{name}", this);
        controller.registerHandler(DELETE, "/{index}/_aliases/{name}", this);
    }

    @Override
    protected String name() {
        return restIndexDeleteAliasesAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restIndexDeleteAliasesAction.handleRequest(queryContext);
    }
}
