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
 * @desc 招行需求，开放 restIndexDeleteAliasesAction 给普通账号
 */
@Component("restIndexDeleteAliasesAction")
public class RestIndexDeleteAliasesAction extends ESAction {


    @Override
    public String name() {
        return "restIndexDeleteAliasesAction";
    }

    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        String index = queryContext.getRequest().param("index");
        String name = queryContext.getRequest().param("name");

        if (StringUtils.isNotBlank(index)) {
            String[] indicesArr = Strings.splitStringByCommaToArray(index);
            List<String> deleteAliasIndices = Lists.newArrayList(indicesArr);
            queryContext.setIndices(deleteAliasIndices);
            checkIndices(queryContext);

            IndexTemplate indexTemplate = getTemplateByIndexTire(deleteAliasIndices, queryContext);

            if (!indexTemplateService.delTemplateAlias(queryContext.getAppid(), indexTemplate.getId(), indexTemplate.getName(), name)) {
                throw new IllegalArgumentException("index must not be null when arius gateway in index mode");
            }

            ESClient client = esClusterService.getClient(queryContext, indexTemplate, actionName);

            directRequest(client, queryContext);
        } else {
            throw new IllegalArgumentException("index must not be null when arius gateway in index mode");
        }
    }
}
