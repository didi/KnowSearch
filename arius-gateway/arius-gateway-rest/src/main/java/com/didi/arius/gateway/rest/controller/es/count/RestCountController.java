package com.didi.arius.gateway.rest.controller.es.count;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.count.RestCountAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * @author fitz
 * @date 2021/5/26 4:14 下午
 */
@Controller
public class RestCountController extends BaseHttpRestController {
    @Autowired
    private RestCountAction restCountAction;

    public RestCountController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(POST, "/_count", this);
        controller.registerHandler(GET, "/_count", this);
        controller.registerHandler(POST, "/{index}/_count", this);
        controller.registerHandler(GET, "/{index}/_count", this);
        controller.registerHandler(POST, "/{index}/{type}/_count", this);
        controller.registerHandler(GET, "/{index}/{type}/_count", this);
    }

    @Override
    protected String name() {
        return restCountAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restCountAction.handleRequest(queryContext);
    }
}
