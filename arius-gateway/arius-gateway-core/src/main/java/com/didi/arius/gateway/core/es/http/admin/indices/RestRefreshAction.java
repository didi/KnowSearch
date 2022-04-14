package com.didi.arius.gateway.core.es.http.admin.indices;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

/**
 * @author zhaoqingrong
 * @date 2021/6/8
 * @desc 招行需求，开放 restRefreshAction 给普通账号
 */
@Component("restRefreshAction")
public class RestRefreshAction extends ESAction {

    @Override
    public String name() {
        return "restRefreshAction";
    }


    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        String refreshIndex = queryContext.getRequest().param("index");

        if (StringUtils.isNotBlank(refreshIndex)) {
            indexAction(queryContext, refreshIndex);
        } else {
            throw new IllegalArgumentException("index must not be null when arius gateway in index mode");
        }
    }
}
