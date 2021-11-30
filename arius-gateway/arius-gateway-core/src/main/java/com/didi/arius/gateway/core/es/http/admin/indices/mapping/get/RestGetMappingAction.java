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

package com.didi.arius.gateway.core.es.http.admin.indices.mapping.get;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.core.es.http.StatAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.didi.arius.gateway.common.consts.RestConsts.*;
import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

/**
 *
 */
@Component
public class RestGetMappingAction extends StatAction {

    @Override
    public String name() {
        return "restGetMapping";
    }

    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client)
            throws Exception {

        String index = request.param(INDEX);
        if (index == null) {
            sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, XContentType.JSON.restContentType(), "{}"));
            return;
        }

        if (isIndexType(queryContext)) {
            String[] indicesArr = Strings.splitStringByCommaToArray(request.param(INDEX));
            List<String> indices = Lists.newArrayList(indicesArr);
            IndexTemplate indexTemplate = getTemplateByIndexTire(indices, queryContext);

            client = esClusterService.getClient(queryContext, indexTemplate, actionName);
        }

        if (queryContext.isFromKibana() && !queryContext.isNewKibana() && !client.getEsVersion().startsWith(QueryConsts.ES_VERSION_2_PREFIX)) {
            ESClient finalClient = client;
            RestActionListenerImpl<DirectResponse> listener = new RestActionListenerImpl<DirectResponse>(queryContext) {
                @Override
                public void onResponse(DirectResponse response) {
                    if (response.getRestStatus() == RestStatus.OK) {
                        JSONObject res = JSON.parseObject(response.getResponseContent());

                        dealMapping(res, finalClient);

                        response.setResponseContent(res.toJSONString());
                    }

                    super.onResponse(response);
                }
            };

            directRequest(client, queryContext, listener);
        } else {
            directRequest(client, queryContext);
        }
    }

    private void dealMapping(JSONObject res, ESClient finalClient) {
        // for kibana
        // 遍历索引
        for (Map.Entry<String, Object> entry : res.entrySet()) {
            JSONObject index = (JSONObject) entry.getValue();
            JSONObject mappings = index.getJSONObject("mappings");

            if (mappings == null || mappings.size() == 0) {
                continue;
            }

            if (finalClient.getEsVersion().startsWith(QueryConsts.ES_VERSION_7_PREFIX)) {
                //7.x single type
                String text = mappings.toJSONString();
                Set<String> keySet = new HashSet<>(mappings.keySet());
                keySet.forEach(mappings::remove);
                mappings.put("_doc", JSON.parseObject(text));
            }

            // 遍历mappings
            for (Map.Entry<String, Object> inEntry : mappings.entrySet()) {
                JSONObject type = (JSONObject) inEntry.getValue();
                JSONObject properties = type.getJSONObject("properties");

                if (properties == null) {
                    continue;
                }

                //遍历type的properties
                for (Map.Entry<String, Object> typeEntry : properties.entrySet()) {
                    JSONObject fieldType = (JSONObject) typeEntry.getValue();
                    dealType(fieldType);

                    //如果type包含fields，则继续处理fields
                    dealFields(fieldType);
                }
            }
        }
    }

    private void dealType(JSONObject fieldType) {
        if (fieldType.containsKey("type")) {
            String strType = fieldType.getString("type");
            String isIndex = fieldType.getString(INDEX);
            if (strType.equalsIgnoreCase("text")) {
                fieldType.put("type", STRING_NAME);
                if (isIndex == null || isIndex.equals("true")) {
                    fieldType.put(INDEX, "analyzed");
                }
            } else if (strType.equalsIgnoreCase("keyword")) {
                fieldType.put("type", STRING_NAME);
                if (isIndex == null || isIndex.equals("true")) {
                    fieldType.put(INDEX, "not_analyzed");
                }
            }
        }
    }

    private void dealFields(JSONObject fieldType) {
        if (fieldType.containsKey("fields")) {
            for (Map.Entry<String, Object> fieldsEntry : fieldType.getJSONObject("fields").entrySet()) {
                JSONObject fields = (JSONObject) fieldsEntry.getValue();
                dealType(fields);
            }
        }
    }
}
