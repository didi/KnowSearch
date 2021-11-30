package com.didi.arius.gateway.core.es.http.admin.indices;

import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhaoqingrong
 * @date 2021/6/8
 * @desc 招行需求，开放 restIndexPutAliasAction 给普通账号
 * 给索引设置别名的时候需要同步的给admin的索引模板也设置下别名，这样通过别名查询的时候就有权限了
 */
@Component("restIndexPutAliasAction")
public class RestIndexPutAliasAction extends ESAction {

    @Override
    public String name() {
        return "restIndexPutAliasAction";
    }

    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        String index = queryContext.getRequest().param("index");
        String name = queryContext.getRequest().param("name");

        if (StringUtils.isNotBlank(index)) {
            String[] indicesArr = Strings.splitStringByCommaToArray(index);
            List<String> aliasIndices = Lists.newArrayList(indicesArr);
            queryContext.setIndices(aliasIndices);
            checkIndices(queryContext);

            IndexTemplate indexTemplate = getTemplateByIndexTire(aliasIndices, queryContext);

            if (!indexTemplateService.addTemplateAlias(queryContext.getAppid(), indexTemplate.getId(), indexTemplate.getName(), name)) {
                throw new IllegalArgumentException("index must not be null when arius gateway in index mode");
            }

            ESClient client = esClusterService.getClient(queryContext, indexTemplate, actionName);

            directRequest(client, queryContext);
        } else {
            throw new IllegalArgumentException("index must not be null when arius gateway in index mode");
        }
    }
}
