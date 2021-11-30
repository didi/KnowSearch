package com.didi.arius.gateway.rest.controller.es.admin.fieldstats;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.action.fieldstats.RestFieldStatsAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * @author fitz
 * @date 2021/5/26 3:35 下午
 */
@Controller
public class RestFieldStatsController extends BaseHttpRestController {

    @Autowired
    private RestFieldStatsAction restFieldStatsAction;

    public RestFieldStatsController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(GET, "/_field_stats", this);
        controller.registerHandler(POST, "/_field_stats", this);
        controller.registerHandler(GET, "/{index}/_field_stats", this);
        controller.registerHandler(POST, "/{index}/_field_stats", this);

    }

    @Override
    protected String name() {
        return restFieldStatsAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restFieldStatsAction.handleRequest(queryContext);
    }
}
