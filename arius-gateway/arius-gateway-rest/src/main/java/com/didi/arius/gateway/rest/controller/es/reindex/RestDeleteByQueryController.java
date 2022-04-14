package com.didi.arius.gateway.rest.controller.es.reindex;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.action.reindex.RestDeleteByQueryAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.POST;

@Controller
public class RestDeleteByQueryController extends BaseHttpRestController {

    @Autowired
    private RestDeleteByQueryAction restDeleteByQueryAction;

    @Override
    protected void register() {
        controller.registerHandler(POST, "/{index}/_delete_by_query", this);
        controller.registerHandler(POST, "/{index}/{type}/_delete_by_query", this);
    }

    @Override
    public String name() {
        return restDeleteByQueryAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restDeleteByQueryAction.handleRequest(queryContext);
    }
}
