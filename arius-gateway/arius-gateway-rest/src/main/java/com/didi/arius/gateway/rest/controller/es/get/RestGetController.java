package com.didi.arius.gateway.rest.controller.es.get;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.get.RestGetAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * @author fitz
 * @date 2021/5/26 4:27 下午
 */
@Controller
public class RestGetController extends BaseHttpRestController {
    @Autowired
    private RestGetAction restGetAction;

    public RestGetController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(GET, "/{index}/_doc/{id}", this);
        controller.registerHandler(GET, "/{index}/{type}/{id}", this);
    }

    @Override
    protected String name() {
        return restGetAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restGetAction.handleRequest(queryContext);
    }
}
