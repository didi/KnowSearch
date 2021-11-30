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

package com.didi.arius.gateway.elasticsearch.client.request.index.stats;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.elasticsearch.client.request.broadcast.ESBroadcastRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * A request to get indices level nodestats. Allow to enable different nodestats to be returned.
 * <p>
 * By default, all statistics are enabled.
 * <p>
 * All the nodestats to be returned can be cleared using {@link #clear()}, at which point, specific
 * nodestats can be enabled.
 */
public class ESIndicesStatsRequest extends ESBroadcastRequest<ESIndicesStatsRequest> {
    public static final String COMPLETION = "completion";
    public static final String STORE = "store";
    public static final String INDEXING = "indexing";
    public static final String TRANSLOG = "translog";
    public static final String REFRESH = "refresh";
    public static final String SUGGEST = "suggest";
    public static final String RECOVERY = "recovery";
    public static final String WARMER = "warmer";
    public static final String SEGMENTS = "segments";
    public static final String SEARCH = "search";
    public static final String QUERY_CACHE = "query_cache";
    public static final String DOCS = "docs";
    public static final String FLUSH = "flush";
    public static final String FIELDDATA = "fielddata";
    public static final String GET = "get";
    public static final String MERGE = "merge";
    public static final String REQUEST_CACHE = "request_cache";

    private Set<String> flags = new HashSet<>();
    private IndicesStatsLevel level = null;

    /**
     * Sets all flags to return all nodestats.
     */
    public ESIndicesStatsRequest all() {
        flags.add(COMPLETION);
        flags.add(INDEXING);
        flags.add(TRANSLOG);
        flags.add(REFRESH);
        flags.add(SUGGEST);
        flags.add(RECOVERY);
        flags.add(WARMER);
        flags.add(SEGMENTS);
        flags.add(SEARCH);
        flags.add(QUERY_CACHE);
        flags.add(DOCS);
        flags.add(FLUSH);
        flags.add(FIELDDATA);
        flags.add(GET);
        flags.add(MERGE);
        flags.add(REQUEST_CACHE);

        return this;
    }

    /**
     * Clears all nodestats.
     */
    public ESIndicesStatsRequest clear() {
        flags.clear();
        return this;
    }

    public ESIndicesStatsRequest types(String... types) {
        for (String type : types) {
            flags.add(type);
        }
        return this;
    }


    public ESIndicesStatsRequest flag(String type, boolean isSet) {
        if (isSet) {
            flags.add(type);
        } else {
            flags.remove(type);
        }
        return this;
    }

   public ESIndicesStatsRequest setLevel(IndicesStatsLevel level) {
        this.level = level;

        return this;
    }

    public boolean isSet(String type) {
        return flags.contains(type);
    }


    @Override
    public RestRequest toRequest() throws Exception {
        String endpoint = buildEndPoint();
        RestRequest rr = new RestRequest("GET", endpoint, null);

        if(level!=null) {
            rr.addParam("level", level.getStr());
        }

        return rr;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        String respStr = response.getResponseContent();


        return JSON.parseObject(respStr, ESIndicesStatsResponse.class);
    }

    private String buildEndPoint() {
        String type = null;
        if (flags != null && !flags.isEmpty()) {
            type = StringUtils.join(flags, ",");
        }
        if (type != null && type.trim().length() == 0) {
            type = null;
        }

        String index = null;
        if (indices != null && indices.length > 0) {
            index = StringUtils.join(indices, ",");
        }

        if (index == null) {
            if (type == null) {
                return "/_stats";
            } else {
                return "/_stats/" + type.trim();
            }
        } else {
            if (type == null) {
                return "/" + index.trim() + "/_stats";
            } else {
                return "/" + index.trim() + "/_stats/" + type.trim();
            }
        }
    }
}
