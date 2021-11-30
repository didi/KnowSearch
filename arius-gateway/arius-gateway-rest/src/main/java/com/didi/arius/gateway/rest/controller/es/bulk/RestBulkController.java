package com.didi.arius.gateway.rest.controller.es.bulk;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.bulk.RestBulkAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestRequest.Method.PUT;

/**
 * @author fitz
 * @date 2021/5/26 4:10 下午
 */
@Controller
public class RestBulkController extends BaseHttpRestController {
    @Autowired
    private RestBulkAction restBulkAction;

    public RestBulkController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(POST, "/_bulk", this);
        controller.registerHandler(PUT, "/_bulk", this);
        controller.registerHandler(POST, "/{index}/_bulk", this);
        controller.registerHandler(PUT, "/{index}/_bulk", this);
        controller.registerHandler(POST, "/{index}/{type}/_bulk", this);
        controller.registerHandler(PUT, "/{index}/{type}/_bulk", this);
    }

    @Override
    protected String name() {
        return restBulkAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restBulkAction.handleRequest(queryContext);
    }
}
