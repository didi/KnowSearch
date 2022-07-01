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
 * @desc 招行需求，开放 restDeleteByQueryAction 给普通账号
 */
@Component("restDeleteByQueryAction")
public class RestDeleteByQueryAction extends ESAction {

    @Override
    public String name() {
        return "restDeleteByQueryAction";
    }

    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        String deleteQueryIndex = queryContext.getRequest().param("index");

        if (StringUtils.isNotBlank(deleteQueryIndex)) {
            indexAction(queryContext, deleteQueryIndex, "/_delete_by_query");
        } else {
            throw new IllegalArgumentException("index must not be null when arius gateway in index mode");
        }
    }
}
