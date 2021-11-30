package com.didi.arius.gateway.rest.controller.es.admin.indices;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.RestAnalyzeAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * @author zhaoqingrong
 * @date 2021/6/8
 * @desc 招行需求，开放 restAnalyzeAction 给普通账号
 */
@Controller
public class RestAnalyzeController extends BaseHttpRestController {

    @Autowired
    RestAnalyzeAction restAnalyzeAction;

    public RestAnalyzeController() {
        //pass
    }

    @Override
    protected void register() {
        controller.registerHandler(GET, "/{index}/_analyze", this);
        controller.registerHandler(POST, "/{index}/_analyze", this);
    }

    @Override
    protected String name() {
        return restAnalyzeAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restAnalyzeAction.handleRequest(queryContext);
    }

}
