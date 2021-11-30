package com.didi.arius.gateway.rest.controller.es;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.action.RestMainAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.HEAD;

/**
 * @author fitz
 * @date 2021/5/26 3:49 下午
 */
@Controller
public class RestMainController extends BaseHttpRestController {
    @Autowired
    private RestMainAction restMainAction;

    public RestMainController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(GET, "/", this);
        controller.registerHandler(HEAD, "/", this);
    }

    @Override
    protected String name() {
        return restMainAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restMainAction.handleRequest(queryContext);
    }
}
