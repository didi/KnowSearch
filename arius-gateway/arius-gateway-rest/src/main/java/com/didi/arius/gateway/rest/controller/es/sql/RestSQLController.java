package com.didi.arius.gateway.rest.controller.es.sql;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.sql.SQLAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.elasticsearch.rest.RestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class RestSQLController extends BaseHttpRestController {

    @Autowired
    private SQLAction sqlAction;

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_sql", this);
		controller.registerHandler(RestRequest.Method.POST, "/_sql", this);
    }

    @Override
    public String name() {
        return sqlAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        sqlAction.handleRequest(queryContext);
    }
}
