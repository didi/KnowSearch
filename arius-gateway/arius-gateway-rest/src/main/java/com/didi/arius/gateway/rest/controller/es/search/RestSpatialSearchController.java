package com.didi.arius.gateway.rest.controller.es.search;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.search.RestSpatialSearchAction;
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
public class RestSpatialSearchController extends BaseHttpRestController {
    @Autowired
    private RestSpatialSearchAction restSpatialSearchAction;

    public RestSpatialSearchController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(GET, "/_spatial_search", this);
        controller.registerHandler(POST, "/_spatial_search", this);
        controller.registerHandler(GET, "/{index}/_spatial_search", this);
        controller.registerHandler(POST, "/{index}/_spatial_search", this);
        controller.registerHandler(GET, "/{index}/{type}/_spatial_search", this);
        controller.registerHandler(POST, "/{index}/{type}/_spatial_search", this);
    }

    @Override
    protected String name() {
        return restSpatialSearchAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restSpatialSearchAction.handleRequest(queryContext);
    }
}
