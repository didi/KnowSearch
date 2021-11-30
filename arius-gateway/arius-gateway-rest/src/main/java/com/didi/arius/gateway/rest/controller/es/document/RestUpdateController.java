package com.didi.arius.gateway.rest.controller.es.document;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.document.RestUpdateAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * @author fitz
 * @date 2021/5/26 4:18 下午
 */
@Controller
public class RestUpdateController extends BaseHttpRestController {
    @Autowired
    private RestUpdateAction restUpdateAction;

    public RestUpdateController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(POST, "/{index}/_update/{id}", this);
        controller.registerHandler(POST, "/{index}/{type}/{id}/_update", this);
    }

    @Override
    protected String name() {
        return restUpdateAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restUpdateAction.handleRequest(queryContext);

    }
}
