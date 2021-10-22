/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.didi.arius.gateway.core.es.http.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.consts.RestConsts;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.FetchFields;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.es.http.ESAction;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESMultiSearchRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESMultiSearchResponse;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchResponse;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.util.concurrent.AtomicArray;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.support.RestActions;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;


/**
 *
 */
@Component("restMultiSearchAction")
public class RestMultiSearchAction extends ESAction {

    public static final String NAME = "multiSearch";

    @Override
    public String name() {
        return NAME;
    }

    private final boolean allowExplicitIndex = true;

    @Override
    public void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        handle(queryContext, request, new ESMultiSearchRequest());
    }

    private void handle(QueryContext queryContext, RestRequest request, ESMultiSearchRequest esMultiSearchRequest) throws Exception {
        Map<String, String> params = new HashMap<>();
        if (request.hasParam("typed_keys")) {
            params.put("typed_keys", request.param("typed_keys"));
        }
        params.put(QueryConsts.SEARCH_IGNORE_THROTTLED, "false");

        String[] indices = Strings.splitStringByCommaToArray(request.param("index"));
        String[] types = Strings.splitStringByCommaToArray(request.param("type"));
        String path = request.path();
        boolean isTemplateRequest = isTemplateRequest(path);
        esMultiSearchRequest.setTemplateRequest(isTemplateRequest);

        IndicesOptions indicesOptions = IndicesOptions.fromRequest(request, IndicesOptions.strictExpandOpenAndForbidClosed());
        esMultiSearchRequest.add(RestActions.getRestContent(request), isTemplateRequest, indices, types, request.param("search_type"), request.param("routing"), indicesOptions, allowExplicitIndex);

        // multi search body may contain index, so this will check indices again
        addIndices(queryContext, esMultiSearchRequest);
        checkIndices(queryContext);

        final List<FetchFields> fetchFieldsList = new ArrayList<>(esMultiSearchRequest.requests().size());

        String dateFrom = queryContext.getRequest().param(RestConsts.SEARCH_DATE_FROM_PARAMS);
        String dateTo = queryContext.getRequest().param(RestConsts.SEARCH_DATE_TO_PARAMS);

        ActionListener<ESMultiSearchResponse> multiListener = new RestActionListenerImpl<ESMultiSearchResponse>(queryContext) {
            @Override
            public void onResponse(ESMultiSearchResponse esMultiSearchResponse) {
                statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_search_query||requestId={}||cost={}", queryContext.getRequestId(), (System.currentTimeMillis() - queryContext.getRequestTime()));

                for (ESMultiSearchResponse.Item item : esMultiSearchResponse.getResponses()) {
                    ESSearchResponse esSearchResponse = item.getResponse();

                    if (esSearchResponse == null) {
                        // statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_search_response||appid={}||requestId={}", queryContext.getAppid(), queryContext.getRequestId());
                        continue;
                    }

                    logSearchResponse(queryContext, esSearchResponse);
                }
                super.onResponse(esMultiSearchResponse);
            }
        };

        // 并发search请求
        final AtomicInteger counter = new AtomicInteger(esMultiSearchRequest.requests().size());
        final AtomicArray<ESMultiSearchResponse.Item> responses = new AtomicArray<>(esMultiSearchRequest.requests().size());

        for (int i = 0; i < esMultiSearchRequest.requests().size(); ++i) {
            ESSearchRequest esSearchRequest = esMultiSearchRequest.requests().get(i);
            esSearchRequest.putHeader("requestId", queryContext.getRequestId());
            esSearchRequest.putHeader("Authorization", queryContext.getRequest().getHeader("Authorization"));
            esSearchRequest.setParams(params);

            List<String> itemIndices = Arrays.asList(esSearchRequest.indices());
            IndexTemplate indexTemplate = null;
            if (isIndexType(queryContext, itemIndices)) {
                if (itemIndices.size() == 1) {
                    indexTemplate = getTemplateByIndex(itemIndices, queryContext);
                }

                if (indexTemplate != null) {
                    String[] newIndices = getQueryIndices(indexTemplate, dateFrom, dateTo);

                    esSearchRequest.indices(newIndices);
                } else {
                    indexTemplate = getTemplateByIndexTire(itemIndices, queryContext);
                }

                // 该索引模板需要根据type名称进行映射到对应的索引模板
                if (isNeedChangeIndexName(queryContext, indexTemplate)) {

                    Tuple<IndexTemplate/*dest template*/, String[]/*dest indexNames*/> changeResult =
                            handleChangeIndexName(queryContext, indexTemplate, esSearchRequest.indices(), esSearchRequest.types());

                    // 替换查询语句中的索引名称
                    esSearchRequest.indices(changeResult.v2());
                    // 再替换索引模板对象
                    indexTemplate = changeResult.v1();
                }
            }

            if (esSearchRequest.indices().length == 1
                    && indexTemplate != null
                    && esSearchRequest.indices()[0].equals(indexTemplate.getName())) {
                String[] newIndices = getQueryIndices(indexTemplate, dateFrom, dateTo);

                esSearchRequest.indices(newIndices);
            }

            // 日期索引加上*号后缀，支持异常索引修复方案
            Convert.convertIndices(esSearchRequest);

            ESClient readClient = esClusterService.getClient(queryContext, indexTemplate);
            queryContext.setClusterName(readClient.getClusterName());

            // pre process
            preSearchProcess(queryContext, readClient, esSearchRequest);

            FetchFields fetchFields = formFetchFields(esSearchRequest);
            fetchFieldsList.add(fetchFields);

            final int index = i;

            ActionListener<ESSearchResponse> listener = new RestActionListenerImpl<ESSearchResponse>(queryContext) {
                @Override
                public void onResponse(ESSearchResponse esSearchResponse) {
                    if (false == Strings.isEmpty(esSearchResponse.getScrollId())
                            && queryContext.getAppDetail().getSearchType() == AppDetail.RequestType.Index) {
                        String encode = Base64.getEncoder().encodeToString(readClient.getClusterName().getBytes());
                        esSearchResponse.setScrollId(encode + SCROLL_SPLIT + esSearchResponse.getScrollId());
                    }

                    ESMultiSearchResponse.Item item = new ESMultiSearchResponse.Item(esSearchResponse, null);
                    item.setStatus(esSearchResponse.getRestStatus().getStatus());

                    responses.set(index, item);
                    if (counter.decrementAndGet() == 0) {
                        finish(responses, multiListener);
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    try {
                        String strException = exceptionToJsonString(e);
                        JSONObject err = JSON.parseObject(strException);
                        ESMultiSearchResponse.Item item = new ESMultiSearchResponse.Item(null, err.get("error"));
                        item.setStatus(err.getIntValue("status"));

                        responses.set(index, item);
                    } catch (Throwable ex) {
                        ESMultiSearchResponse.Item item = new ESMultiSearchResponse.Item(null, "unknow");
                        item.setStatus(RestStatus.INTERNAL_SERVER_ERROR.getStatus());
                        responses.set(index, item);
                    }

                    if (counter.decrementAndGet() == 0) {
                        finish(responses, multiListener);
                    }
                }
            };

            readClient.search(esSearchRequest, listener);
        }
    }

    private boolean isTemplateRequest(String path) {
        return (path != null && path.endsWith("/template"));
    }

    private void addIndices(QueryContext queryContext, ESMultiSearchRequest esMultiSearchRequest) {
        for (ESSearchRequest request : esMultiSearchRequest.requests()) {
            String[] indices = request.indices();
            for (String index : indices) {
                queryContext.addIndex(index);
            }
        }
    }

    private void finish(AtomicArray<ESMultiSearchResponse.Item> responses, ActionListener<ESMultiSearchResponse> listener) {
        ESMultiSearchResponse esMultiSearchResponse = new ESMultiSearchResponse();
        esMultiSearchResponse.setRestStatus(RestStatus.OK);
        ESMultiSearchResponse.Item[] responseArr = responses.toArray(new ESMultiSearchResponse.Item[responses.length()]);
        esMultiSearchResponse.setResponses(Arrays.asList(responseArr));
        listener.onResponse(esMultiSearchResponse);
    }
}
