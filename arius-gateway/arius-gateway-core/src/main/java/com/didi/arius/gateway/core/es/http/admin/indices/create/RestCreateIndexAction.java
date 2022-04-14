package com.didi.arius.gateway.core.es.http.admin.indices.create;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

@Component("restCreateIndexAction")
public class RestCreateIndexAction extends ESAction {

    public static final String NAME = "restCreateIndex";

    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        String index = queryContext.getRequest().param("index");
        if (Strings.hasText(index) == false) {
            throw new IllegalArgumentException("index must not be null");
        }

        ESClient client;

        String[] indicesArr = Strings.splitStringByCommaToArray(index);
        List<String> createIndices = Lists.newArrayList(indicesArr);
        queryContext.setIndices(createIndices);
        checkIndices(queryContext);
        if (isIndexType(queryContext)) {
            IndexTemplate indexTemplate = getTemplateByIndexTire(createIndices, queryContext);
            client = esClusterService.getClient(queryContext, indexTemplate, actionName);
        } else {
            client = esClusterService.getClient(queryContext, actionName);
        }

        RestActionListenerImpl<DirectResponse> listener = new RestActionListenerImpl<DirectResponse>(queryContext) {
            @Override
            public void onResponse(DirectResponse response) {
                if (response.getRestStatus() == RestStatus.OK) {
                    JSONObject res = JSON.parseObject(response.getResponseContent());
                    if (false == res.containsKey("index")) {
                        res.put("index", index);
                        res.put("shards_acknowledged", true);
                    }

                    response.setResponseContent(res.toJSONString());
                }

                super.onResponse(response);
            }
        };

        directRequest(client, queryContext, listener);
    }

    @Override
    public String name() {
        return NAME;
    }
}
