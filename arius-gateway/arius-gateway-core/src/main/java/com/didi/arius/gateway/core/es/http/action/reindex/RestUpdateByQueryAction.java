package com.didi.arius.gateway.core.es.http.action.reindex;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;

/**
 * @author zhaoqingrong
 * @date 2021/6/8
 * @desc 招行需求，开放 restUpdateByQueryAction 给普通账号
 */
@Component("restUpdateByQueryAction")
public class RestUpdateByQueryAction extends ESAction {

    @Override
    public String name() {
        return "restUpdateByQueryAction";
    }

    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        String updateByQueryIndex = queryContext.getRequest().param("index");

        if (StringUtils.isNotBlank(updateByQueryIndex)) {
            indexAction(queryContext, updateByQueryIndex, "/_update_by_query");
        } else {
            throw new IllegalArgumentException("index must not be null when arius gateway in index mode");
        }
    }
}
