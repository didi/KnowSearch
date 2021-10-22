package com.didi.arius.gateway.rest.controller.es.get;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.get.RestMultiGetAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * @author fitz
 * @date 2021/5/26 4:27 下午
 */
@Controller
public class RestMultiGetController extends BaseHttpRestController {
    @Autowired
    private RestMultiGetAction restMultiGetAction;
    @Override
    protected void register() {
        controller.registerHandler(GET, "/_mget", this);
        controller.registerHandler(POST, "/_mget", this);
        controller.registerHandler(GET, "/{index}/_mget", this);
        controller.registerHandler(POST, "/{index}/_mget", this);
        controller.registerHandler(GET, "/{index}/{type}/_mget", this);
        controller.registerHandler(POST, "/{index}/{type}/_mget", this);
    }

    @Override
    protected String name() {
        return restMultiGetAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restMultiGetAction.handleRequest(queryContext);
    }
}
