package com.didi.arius.gateway.rest.controller.es.admin.indices;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.create.RestCreateIndexAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.elasticsearch.rest.RestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * @author fitz
 * @date 2021/5/26 1:16 下午
 */
@Controller
public class RestCreateIndexController extends BaseHttpRestController {

    @Autowired
    RestCreateIndexAction restCreateIndexAction;

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.PUT, "/{index}", this);
        controller.registerHandler(RestRequest.Method.POST, "/{index}", this);
    }

    @Override
    protected String name() {
        return restCreateIndexAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restCreateIndexAction.handleRequest(queryContext);
    }
}
