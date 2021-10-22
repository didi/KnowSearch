package com.didi.arius.gateway.rest.controller;

import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

import java.util.List;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

/**
 * @author fitz
 * @date 2021/5/25 3:22 下午
 */
public abstract class StatController extends BaseHttpRestController {

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {

        ESClient client = esClusterService.getClient(queryContext);

        if (queryContext.getRequest().param("index") != null) {
            String index = queryContext.getRequest().param("index");
            String[] indicesArr = Strings.splitStringByCommaToArray(index);
            List<String> indices = Lists.newArrayList(indicesArr);
            queryContext.setIndices(indices);

            checkIndices(queryContext);

            if (isIndexType(queryContext)) {
                IndexTemplate indexTemplate = indexTemplateService.getTemplateByIndexTire(indices, queryContext);

                client = esClusterService.getClient(queryContext, indexTemplate);
            }
        }

        handleAriusRequest(queryContext, queryContext.getRequest(), queryContext.getChannel(), client);

    }

    abstract protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception;
}
