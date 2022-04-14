package com.didi.arius.gateway.rest.controller.es.sql;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.sql.SQLExplainAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.elasticsearch.rest.RestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class RestSQLExplainController extends BaseHttpRestController {

    @Autowired
    private SQLExplainAction sqlExplainAction;

    public RestSQLExplainController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler( RestRequest.Method.GET, "/_sql/explain", this);
        controller.registerHandler( RestRequest.Method.POST, "/_sql/explain", this);
    }

    @Override
    protected String name() {
        return sqlExplainAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        sqlExplainAction.handleRequest(queryContext);
    }
}
