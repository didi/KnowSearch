package com.didi.arius.gateway.rest.controller.es.search;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.search.RestMultiSearchAction;
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
public class RestMultiSearchController extends BaseHttpRestController {
    @Autowired
    private RestMultiSearchAction restMultiSearchAction;

    public RestMultiSearchController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(GET, "/_msearch", this);
        controller.registerHandler(POST, "/_msearch", this);
        controller.registerHandler(GET, "/{index}/_msearch", this);
        controller.registerHandler(POST, "/{index}/_msearch", this);
        controller.registerHandler(GET, "/{index}/{type}/_msearch", this);
        controller.registerHandler(POST, "/{index}/{type}/_msearch", this);

        controller.registerHandler(GET, "/_msearch/template", this);
        controller.registerHandler(POST, "/_msearch/template", this);
        controller.registerHandler(GET, "/{index}/_msearch/template", this);
        controller.registerHandler(POST, "/{index}/_msearch/template", this);
        controller.registerHandler(GET, "/{index}/{type}/_msearch/template", this);
        controller.registerHandler(POST, "/{index}/{type}/_msearch/template", this);
    }

    @Override
    protected String name() {
        return restMultiSearchAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restMultiSearchAction.handleRequest(queryContext);
    }
}
