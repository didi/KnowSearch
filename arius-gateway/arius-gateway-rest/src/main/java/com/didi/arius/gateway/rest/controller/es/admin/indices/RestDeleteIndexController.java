package com.didi.arius.gateway.rest.controller.es.admin.indices;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.delete.RestDeleteIndexAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.elasticsearch.rest.RestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * @author fitz
 * @date 2021/5/26 1:36 下午
 */
@Controller
public class RestDeleteIndexController extends BaseHttpRestController {

    @Autowired
    RestDeleteIndexAction restDeleteIndexAction;

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.DELETE, "/{index}", this);
    }

    @Override
    protected String name() {
        return restDeleteIndexAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restDeleteIndexAction.handleRequest(queryContext);
    }


}
