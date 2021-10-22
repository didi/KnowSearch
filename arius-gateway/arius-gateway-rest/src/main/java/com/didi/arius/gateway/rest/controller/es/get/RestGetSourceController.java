package com.didi.arius.gateway.rest.controller.es.get;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.get.RestGetSourceAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * @author fitz
 * @date 2021/5/26 4:27 下午
 */
@Controller
public class RestGetSourceController extends BaseHttpRestController {
    @Autowired
    private RestGetSourceAction restGetSourceAction;
    @Override
    protected void register() {
        controller.registerHandler(GET, "/{index}/{type}/{id}/_source", this);
    }

    @Override
    protected String name() {
        return restGetSourceAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restGetSourceAction.handleRequest(queryContext);
    }
}
