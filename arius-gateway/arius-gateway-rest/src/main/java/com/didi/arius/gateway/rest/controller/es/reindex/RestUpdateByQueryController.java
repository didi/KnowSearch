package com.didi.arius.gateway.rest.controller.es.reindex;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.action.reindex.RestUpdateByQueryAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.POST;

@Controller
public class RestUpdateByQueryController extends BaseHttpRestController {

    @Autowired
    RestUpdateByQueryAction restUpdateByQueryAction;

    @Override
    protected void register() {
        controller.registerHandler(POST, "/{index}/_update_by_query", this);
        controller.registerHandler(POST, "/{index}/{type}/_update_by_query", this);
    }

    @Override
    public String name() {
        return restUpdateByQueryAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restUpdateByQueryAction.handleRequest(queryContext);
    }
}
