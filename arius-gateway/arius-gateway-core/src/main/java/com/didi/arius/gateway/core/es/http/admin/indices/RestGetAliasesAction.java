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
 * @desc 招行需求，开放 restGetAliasesAction 给普通账号
 */
@Component("restGetAliasesAction")
public class RestGetAliasesAction extends ESAction {

    @Override
    public String name() {
        return "restGetAliasesAction";
    }

    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        String checkAliasIndex = queryContext.getRequest().param("index");

        if (StringUtils.isNotBlank(checkAliasIndex)) {
            indexAction(queryContext, checkAliasIndex);
        } else {
            throw new IllegalArgumentException("index must not be null when arius gateway in index mode");
        }
    }
}
