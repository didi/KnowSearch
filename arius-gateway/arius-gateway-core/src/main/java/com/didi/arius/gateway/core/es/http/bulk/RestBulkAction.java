package com.didi.arius.gateway.core.es.http.bulk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.core.es.http.document.RestBaseWriteAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;

import com.didiglobal.logi.log.LogGather;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.support.RestUtils;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.didi.arius.gateway.elasticsearch.client.utils.LogUtils.setWriteLog;

/**
 * <pre>
 * { "index" : { "_index" : "test", "_type" : "type1", "_id" : "1" }
 * { "type1" : { "field1" : "value1" } }
 * { "delete" : { "_index" : "test", "_type" : "type1", "_id" : "2" } }
 * { "create" : { "_index" : "test", "_type" : "type1", "_id" : "1" }
 * { "type1" : { "field1" : "value1" } }
 * </pre>
 */
@Component
public class RestBulkAction extends RestBaseWriteAction {

    @Override
    public String name() {
        return "bulk";
    }

    @Override
    public void handleInterRequest(QueryContext queryContext, final RestRequest request, final RestChannel channel)
            throws Exception {
        String defaultIndex = request.param("index");
        if (defaultIndex == null) {
            String bulkBody = queryContext.getPostBody();
            String[] line = bulkBody.split("\n");
            List<String> bulkItems = Arrays.asList(line);
            Iterator<String> iter = bulkItems.iterator();
            while (iter.hasNext()) {
                String operate = iter.next();
                if (operate.equals("")) {
                    continue;
                }
                JSONObject bulkJson = JSON.parseObject(operate);
                if (bulkJson.keySet().size() != 1) {
                    throw new InvalidParameterException("bulk operate error");
                }

                String key = bulkJson.keySet().iterator().next();
                JSONObject operateJson = bulkJson.getJSONObject(key);
                String index = operateJson.getString("_index");
                if (index != null) {
                    defaultIndex = index;
                    break;
                }
            }
        }

        queryContext.setIndices(Arrays.asList(defaultIndex));
        checkWriteIndicesAndTemplateBlockWrite(queryContext);

        IndexTemplate indexTemplate = getAndCheckIndexTemplate(defaultIndex, queryContext);

        // 获取写入的client
        ESClient writeClient = esClusterService.getWriteClient(indexTemplate, actionName);

        String uri = queryContext.getUri();
        String queryString = queryContext.getQueryString() == null ? "" : queryContext.getQueryString();

        Map<String, String> params = new HashMap<>();
        RestUtils.decodeQueryString(queryString, 0, params);
        if (!Strings.isEmpty(indexTemplate.getIngestPipeline())) {
            params.put("pipeline", indexTemplate.getIngestPipeline());
        }

        DirectRequest directRequest = new DirectRequest(queryContext.getMethod().toString(), uri);
        directRequest.setPostContent(queryContext.getPostBody());
        directRequest.setParams(params);
        directRequest.putHeader("requestId", queryContext.getRequestId());
        directRequest.putHeader("Authorization", request.getHeader("Authorization"));

        LogGather.recordInfoLog( indexTemplate.getExpression() + "_" + OPER_BULK, String.format("%s write index", indexTemplate.getExpression()));

        // 生成listener
        ActionListener<DirectResponse> listener = new RestActionListenerImpl<DirectResponse>(queryContext) {
            @Override
            public void onResponse(DirectResponse response) {
                long currentTime = System.currentTimeMillis();
                setWriteLog(queryContext, indexTemplate, response,
                        currentTime, queryConfig.isWriteLogContentOpen());

                metricsService.addIndexMetrics(indexTemplate.getExpression(), name(), currentTime - queryContext.getRequestTime(), queryContext.getPostBody().length(), response.getResponseContent().length());

                super.onResponse(response);
            }
        };

        writeClient.direct(directRequest, listener);
    }
}
