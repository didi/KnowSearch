package com.didi.arius.gateway.rest.controller.es.admin.cat;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.action.cat.RestIndicesAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;
/**
 * @author fitz
 * @date 2021/5/26 3:23 下午
 */
@Controller
public class RestIndicesController extends BaseHttpRestController {
    @Autowired
    private RestIndicesAction restIndicesAction;

    @Override
    protected void register() {
        controller.registerHandler(GET, "/_cat/indices", this);
        controller.registerHandler(GET, "/_cat/indices/{index}", this);
    }

    @Override
    protected String name() {
        return restIndicesAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restIndicesAction.handleRequest(queryContext);

    }
}
