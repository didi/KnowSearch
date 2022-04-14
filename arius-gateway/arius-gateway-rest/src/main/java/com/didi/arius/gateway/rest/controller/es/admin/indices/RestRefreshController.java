package com.didi.arius.gateway.rest.controller.es.admin.indices;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.RestRefreshAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * @author didi
 * @date 2021-09-24 4:18 下午
 */
@Controller
public class RestRefreshController extends BaseHttpRestController {

    @Autowired
    private RestRefreshAction restRefreshAction;

    @Override
    protected void register() {
        controller.registerHandler(POST, "/{index}/_refresh", this);
        controller.registerHandler(GET, "/{index}/_refresh", this);
    }

    @Override
    protected String name() {
        return restRefreshAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restRefreshAction.handleRequest(queryContext);
    }
}
