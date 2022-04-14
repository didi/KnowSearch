package com.didi.arius.gateway.rest.controller.es.document;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.document.RestDeleteAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.DELETE;

/**
 * @author fitz
 * @date 2021/5/26 4:18 下午
 */
@Controller
public class RestDeleteController extends BaseHttpRestController {
    @Autowired
    private RestDeleteAction restDeleteAction;

    public RestDeleteController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(DELETE, "/{index}/_doc/{id}", this);
        controller.registerHandler(DELETE, "/{index}/{type}/{id}", this);
    }

    @Override
    protected String name() {
        return restDeleteAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restDeleteAction.handleRequest(queryContext);

    }
}
