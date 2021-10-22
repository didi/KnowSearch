package com.didi.arius.gateway.rest.controller.es.get;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.get.RestHeadAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.HEAD;

/**
 * @author fitz
 * @date 2021/5/26 4:27 下午
 */
@Controller
public class RestHeadController extends BaseHttpRestController {
    @Autowired
    private RestHeadAction restHeadAction;
    @Override
    protected void register() {
        controller.registerHandler(HEAD, "/{index}/{type}/{id}", this);
        controller.registerHandler(HEAD, "/{index}/{type}/{id}/_source", this);
    }

    @Override
    protected String name() {
        return restHeadAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restHeadAction.handleRequest(queryContext);
    }
}
