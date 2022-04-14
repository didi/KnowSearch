package com.didi.arius.gateway.core.es.http.admin.indices.delete;

import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

@Component("restDeleteIndexAction")
public class RestDeleteIndexAction extends ESAction {

    public static final String NAME = "restDeleteIndex";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        String index = queryContext.getRequest().param("index");
        if (Strings.hasText(index) == false) {
            throw new IllegalArgumentException("index must not be null");
        }

        ESClient client;

        String[] indicesArr = Strings.splitStringByCommaToArray(index);
        List<String> indices = Lists.newArrayList(indicesArr);
        queryContext.setIndices(indices);
        checkIndices(queryContext);
        if (isIndexType(queryContext)) {
            IndexTemplate indexTemplate = getTemplateByIndexTire(indices, queryContext);
            client = esClusterService.getClient(queryContext, indexTemplate, actionName);
        } else {
            client = esClusterService.getClient(queryContext, actionName);
        }

        directRequest(client, queryContext);
    }
}
