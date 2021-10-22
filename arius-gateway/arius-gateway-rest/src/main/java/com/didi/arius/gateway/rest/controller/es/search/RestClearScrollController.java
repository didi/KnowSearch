package com.didi.arius.gateway.rest.controller.es.search;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.search.RestClearScrollAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.DELETE;

/**
 * @author fitz
 * @date 2021/5/26 4:27 下午
 */
@Controller
public class RestClearScrollController extends BaseHttpRestController {
    @Autowired
    private RestClearScrollAction restClearScrollAction;
    @Override
    protected void register() {
        controller.registerHandler(DELETE, "/_search/scroll", this);
        controller.registerHandler(DELETE, "/_search/scroll/{scroll_id}", this);
    }

    @Override
    protected String name() {
        return restClearScrollAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restClearScrollAction.handleRequest(queryContext);
    }
}
