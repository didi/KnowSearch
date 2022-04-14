package com.didi.arius.gateway.rest.controller.es.admin.indices;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.admin.indices.settings.RestPutIndexSettingsAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.elasticsearch.rest.RestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * @author fitz
 * @date 2021/5/26 1:41 下午
 */
@Controller
public class RestPutIndexSettingsController extends BaseHttpRestController {

    @Autowired
    RestPutIndexSettingsAction restPutIndexSettingsAction;

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.PUT, "/{index}/_settings", this);
    }

    @Override
    protected String name() {
        return restPutIndexSettingsAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restPutIndexSettingsAction.handleRequest(queryContext);
    }
}
