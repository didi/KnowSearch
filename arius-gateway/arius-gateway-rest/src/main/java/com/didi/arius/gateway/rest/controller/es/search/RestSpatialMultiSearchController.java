package com.didi.arius.gateway.rest.controller.es.search;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.search.RestSpatialMultiSearchAction;
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
public class RestSpatialMultiSearchController extends BaseHttpRestController {
    @Autowired
    private RestSpatialMultiSearchAction restSpatialMultiSearchAction;
    @Override
    protected void register() {
        controller.registerHandler(GET, "/_spatial_msearch", this);
        controller.registerHandler(POST, "/_spatial_msearch", this);
        controller.registerHandler(GET, "/{index}/_spatial_msearch", this);
        controller.registerHandler(POST, "/{index}/_spatial_msearch", this);
        controller.registerHandler(GET, "/{index}/{type}/_spatial_msearch", this);
        controller.registerHandler(POST, "/{index}/{type}/_spatial_msearch", this);
    }

    @Override
    protected String name() {
        return restSpatialMultiSearchAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restSpatialMultiSearchAction.handleRequest(queryContext);
    }
}
