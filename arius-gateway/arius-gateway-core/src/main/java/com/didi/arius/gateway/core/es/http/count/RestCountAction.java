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

package com.didi.arius.gateway.core.es.http.count;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.consts.RestConsts;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.es.http.ESAction;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.support.RestActions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

/**
 *
 */
@Component("restCountAction")
public class RestCountAction extends ESAction {

	public static final String NAME = "count";
	
	@Override
	public String name() {
		return NAME;
	}
	

    @Override
	public void handleInterRequest(QueryContext queryContext, final RestRequest request, final RestChannel channel)
			throws Exception {
        ESSearchRequest esSearchRequest = new ESSearchRequest();
        esSearchRequest.indices(Strings.splitStringByCommaToArray(request.param("index")));
        esSearchRequest.types(Strings.splitStringByCommaToArray(request.param("type")));
        esSearchRequest.setTemplateRequest(request.path().endsWith("/template"));
        esSearchRequest.source(RestActions.getRestContent(request));
        Map<String, String> params = request.params();
        params.remove("source");
        params.remove("index");
        params.remove("type");
        params.put("size", "0");
        esSearchRequest.setParams(params);

        esSearchRequest.putHeader("requestId", queryContext.getRequestId());
        esSearchRequest.putHeader("Authorization", queryContext.getRequest().getHeader("Authorization"));

        IndexTemplate indexTemplate = null;
        if (isIndexType(queryContext)) {
            List<String> indices = queryContext.getIndices();
            if (indices.size() == 1) {
                indexTemplate = getTemplateByIndex(indices, queryContext);
            }

            if (indexTemplate != null) {
                String dateFrom = queryContext.getRequest().param(RestConsts.SEARCH_DATE_FROM_PARAMS);
                String dateTo = queryContext.getRequest().param(RestConsts.SEARCH_DATE_TO_PARAMS);

                String[] newIndices = getQueryIndices(indexTemplate, dateFrom, dateTo);

                esSearchRequest.indices(newIndices);
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

        // 日期索引加上*号后缀，支持异常索引修复方案
        Convert.convertIndices(esSearchRequest);

        ESClient readClient = esClusterService.getClient(queryContext, indexTemplate);

        // pre process
        preSearchProcess(queryContext, readClient, esSearchRequest);

        RestActionListenerImpl<ESSearchResponse> listener = new RestActionListenerImpl<ESSearchResponse>(queryContext) {
            @Override
            public void onResponse(ESSearchResponse response) {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("count", response.getHits().getTotal());
                jsonObject.put("_shards", response.getShards());

                super.onResponse(new BytesRestResponse(RestStatus.OK, XContentType.JSON.restContentType(), jsonObject.toJSONString()));
            }
        };
        readClient.search(esSearchRequest, listener);
    }
}
