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

package com.didi.arius.gateway.core.es.http.action.fieldstats;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.CommonUtil;
import com.didi.arius.gateway.core.es.http.StatAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 */
@Component
public class RestFieldStatsAction extends StatAction {

    public static final String INDEX = "index";

    @Override
    public String name() {
        return "restFieldStats";
    }


    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client)
            throws Exception {
        if (CommonUtil.isIndexType(queryContext)) {
            String[]  indicesArr = Strings.splitStringByCommaToArray(request.param(INDEX));
            List<String> indices = Lists.newArrayList(indicesArr);
            IndexTemplate indexTemplate = getTemplateByIndexTire(indices, queryContext);

            client = esClusterService.getClient(queryContext, indexTemplate, actionName);
        }

        if (CommonUtil.isSearchKibana(queryContext.getUri(), queryContext.getIndices())) {
            sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, XContentType.JSON.restContentType(), "{\"_shards\":{\"total\":0,\"successful\":0,\"failed\":0},\"indices\":{}}"));
        } else if ( queryContext.isFromKibana()
                && !queryContext.isNewKibana()
                && !client.getEsVersion().startsWith(QueryConsts.ES_VERSION_2_PREFIX)
                && request.param(INDEX) != null
                && request.param("level") != null && request.param("level").equals("indices")) {

            // for kibana
            String index = request.param(INDEX);

            String timeStamp = "";
            try {
                JSONObject source = JSON.parseObject(queryContext.getPostBody());
                JSONArray fields = source.getJSONArray("fields");
                timeStamp = fields.getString(0);
            } catch (Exception e) {
                logger.info("source parse error, souce={}", queryContext.getPostBody());
            }

            String result = String.format("{\n" +
                    "  \"_shards\": {\n" +
                    "    \"total\": 1,\n" +
                    "    \"successful\": 1,\n" +
                    "    \"failed\": 0\n" +
                    "  },\n" +
                    "  \"indices\": {\n" +
                    "    \"%s\": {\n" +
                    "      \"fields\": {\n" +
                    "        \"%s\": {\n" +
                    "          \"max_doc\": 1,\n" +
                    "          \"doc_count\": 1,\n" +
                    "          \"density\": 1,\n" +
                    "          \"sum_doc_freq\": 1,\n" +
                    "          \"sum_total_term_freq\": -1,\n" +
                    "          \"min_value\": 1,\n" +
                    "          \"min_value_as_string\": \"1970-03-03 16:00:00 +0000\",\n" +
                    "          \"max_value\": 33108537600000,\n" +
                    "          \"max_value_as_string\": \"3019-03-04 00:00:00 +0000\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}", index, timeStamp);

            sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, XContentType.JSON.restContentType(), result));
        } else {
            directRequest(client, queryContext);
        }
    }
}
