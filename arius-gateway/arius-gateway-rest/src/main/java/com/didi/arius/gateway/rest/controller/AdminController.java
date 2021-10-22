package com.didi.arius.gateway.rest.controller;

import com.didi.arius.gateway.common.exception.AccessForbiddenException;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.AppUtil;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

public abstract class AdminController extends BaseHttpRestController {

    @Override
    public void handleRequest(QueryContext queryContext) throws Exception {
        if (!AppUtil.isAdminAppid(queryContext.getAppDetail())) {
            throw new AccessForbiddenException("action(" + queryContext.getUri() + ") forbidden");
        }

        ESClient client = esClusterService.getClient(queryContext);

        handleAriusRequest(queryContext, queryContext.getRequest(), queryContext.getChannel(), client);
    }

    abstract protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception;
}
