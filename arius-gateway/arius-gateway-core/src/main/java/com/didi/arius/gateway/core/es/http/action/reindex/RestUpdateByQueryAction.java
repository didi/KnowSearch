package com.didi.arius.gateway.core.es.http.action.reindex;

import java.util.stream.Collectors;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

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
            preIndexAction(queryContext, updateByQueryIndex);
            if (CollectionUtils.isNotEmpty(queryContext.getIndices()) && null != queryContext.getIndexTemplate() && queryContext.getIndexTemplate().getExpression().endsWith("*")) {
                queryContext.setIndices(queryContext.getIndices().stream().map(str -> (StringUtils.isNotBlank(str) && !str.endsWith("*")) ? str + "*" : str).collect(Collectors.toList()));
            }
            doIndexAction(queryContext, queryContext.getIndexTemplate());
        } else {
            throw new IllegalArgumentException("index must not be null when arius gateway in index mode");
        }
    }
}
