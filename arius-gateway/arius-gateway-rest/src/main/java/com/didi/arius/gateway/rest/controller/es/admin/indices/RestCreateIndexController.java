package com.didi.arius.gateway.rest.controller.es.admin.indices;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.rest.controller.AdminController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Controller;

import java.util.List;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

/**
 * @author fitz
 * @date 2021/5/26 1:16 下午
 */
@Controller
public class RestCreateIndexController extends AdminController {
    public static final String NAME = "restCreateIndex";

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.PUT, "/{index}", this);
        controller.registerHandler(RestRequest.Method.POST, "/{index}", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {

        String index = request.param("index");
        if (Strings.hasText(index) == false) {
            throw new IllegalArgumentException("index must not be null");
        }

        String[] indicesArr = Strings.splitStringByCommaToArray(index);
        List<String> indices = Lists.newArrayList(indicesArr);
        queryContext.setIndices(indices);
        checkIndices(queryContext);
        if (isIndexType(queryContext)) {
            IndexTemplate indexTemplate = indexTemplateService.getTemplateByIndexTire(indices, queryContext);
            client = esClusterService.getClient(queryContext, indexTemplate);
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
}
