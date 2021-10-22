package com.didi.arius.gateway.rest.controller.es.document;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.document.RestIndexAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestRequest.Method.PUT;

/**
 * @author fitz
 * @date 2021/5/26 4:18 下午
 */
@Controller
public class RestIndexController extends BaseHttpRestController {
    @Autowired
    private RestIndexAction restIndexAction;
    @Override
    protected void register() {
        controller.registerHandler(POST, "/{index}/_doc", this); // auto id creation
        controller.registerHandler(PUT, "/{index}/_doc/{id}", this);
        controller.registerHandler(POST, "/{index}/_doc/{id}", this);
        controller.registerHandler(PUT, "/{index}/_create/{id}", this);
        controller.registerHandler(POST, "/{index}/_create/{id}/", this);
        controller.registerHandler(POST, "/{index}/{type}", this); // auto id creation
        controller.registerHandler(PUT, "/{index}/{type}/{id}", this);
        controller.registerHandler(POST, "/{index}/{type}/{id}", this);
        controller.registerHandler(PUT, "/{index}/{type}/{id}/_create", this);
        controller.registerHandler(POST, "/{index}/{type}/{id}/_create", this);
    }

    @Override
    protected String name() {
        return restIndexAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restIndexAction.handleRequest(queryContext);

    }
}
