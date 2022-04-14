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

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.consts.RestConsts;
import com.didi.arius.gateway.common.metadata.FetchFields;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.JoinLogContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.es.http.ESAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchResponse;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestActions;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

/**
 *
 */
@Component("restSearchAction")
public class RestSearchAction extends ESAction {

    @Override
    public String name() {
        return "search";
    }

    @Override
    public void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel)
            throws Exception {
        handle(queryContext, request, new ESSearchRequest());
    }

    private void handle(QueryContext queryContext, RestRequest request, ESSearchRequest esSearchRequest) {
        long start = System.currentTimeMillis();
        esSearchRequest.indices(Strings.splitStringByCommaToArray(request.param("index")));
        esSearchRequest.types(Strings.splitStringByCommaToArray(request.param("type")));
        esSearchRequest.setTemplateRequest(request.path().endsWith("/template"));
        esSearchRequest.source(RestActions.getRestContent(request));
        Map<String, String> params = request.params();
        params.remove("source");
        params.remove("index");
        params.remove("type");
        params.remove("filter_path");
        params.put(QueryConsts.SEARCH_IGNORE_THROTTLED, "false");
        esSearchRequest.setParams(params);

        esSearchRequest.extraSource(parseSearchExtraSource(request));

        FetchFields fetchFields = formFetchFields(esSearchRequest);
        queryContext.setFetchFields(fetchFields);

        esSearchRequest.putHeader("requestId", queryContext.getRequestId());
        esSearchRequest.putHeader("Authorization", request.getHeader("Authorization"));

        long paramTime = System.currentTimeMillis();

        IndexTemplate indexTemplate = null;
        if (isIndexType(queryContext)) {
            List<String> indices = queryContext.getIndices();
            if (indices.size() == 1) {
                indexTemplate = getTemplateByIndex(indices, queryContext);
            }

            if (indexTemplate != null) {
                if(!isAliasGet(indexTemplate, indices)){
                    String dateFrom = queryContext.getRequest().param(RestConsts.SEARCH_DATE_FROM_PARAMS);
                    String dateTo = queryContext.getRequest().param(RestConsts.SEARCH_DATE_TO_PARAMS);

                    String[] newIndices = getQueryIndices(indexTemplate, dateFrom, dateTo);

                    esSearchRequest.indices(newIndices);
                }
            } else {
                indexTemplate = getTemplateByIndexTire(indices, queryContext);
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

        long indexTemplateTime = System.currentTimeMillis();

        // 日期索引加上*号后缀，支持异常索引修复方案
        Convert.convertIndices(esSearchRequest);

        ESClient readClient = esClusterService.getClient(queryContext, indexTemplate, actionName);

        long getClientTime = System.currentTimeMillis();

        // pre process
        preSearchProcess(queryContext, readClient, esSearchRequest);

        long preProcessTime = System.currentTimeMillis();

        ActionListener<ESSearchResponse> listener = newSearchListener(queryContext);
        readClient.search(esSearchRequest, listener);

        JoinLogContext joinLogContext = queryContext.getJoinLogContext();
        joinLogContext.setParamCost(paramTime - start);
        joinLogContext.setIndexTemplateCost(indexTemplateTime - paramTime);
        joinLogContext.setGetClientCost(getClientTime - indexTemplateTime);
        joinLogContext.setPreProcessCost(preProcessTime - getClientTime);
    }


    public static SearchSourceBuilder parseSearchExtraSource(RestRequest request) {
        SearchSourceBuilder searchSourceBuilder = null;

        String sField = request.param("fields");
        if (sField != null) {
            searchSourceBuilder = new SearchSourceBuilder();

            if (!Strings.hasText(sField)) {
                searchSourceBuilder.noFields();
            } else {
                String[] sFields = Strings.splitStringByCommaToArray(sField);
                if (sFields != null) {
                    for (String field : sFields) {
                        searchSourceBuilder.field(field);
                    }
                }
            }
        }

        FetchSourceContext fetchSourceContext = FetchSourceContext.parseFromRestRequest(request);
        if (fetchSourceContext != null) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            searchSourceBuilder.fetchSource(fetchSourceContext);
        }

        return searchSourceBuilder;
    }

}
