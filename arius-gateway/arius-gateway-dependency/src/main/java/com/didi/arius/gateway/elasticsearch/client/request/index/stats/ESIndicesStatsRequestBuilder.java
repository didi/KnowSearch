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

import com.didi.arius.gateway.elasticsearch.client.request.broadcast.ESBroadcastOperationRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * A request to get indices level nodestats. Allow to enable different nodestats to be returned.
 * <p>
 * By default, the {@link #setDocs(boolean)}, {@link #setStore(boolean)}, {@link #setIndexing(boolean)}
 * are enabled. Other nodestats can be enabled as well.
 * <p>
 * All the nodestats to be returned can be cleared using {@link #clear()}, at which point, specific
 * nodestats can be enabled.
 */
public class ESIndicesStatsRequestBuilder extends ESBroadcastOperationRequestBuilder<ESIndicesStatsRequest, ESIndicesStatsResponse, ESIndicesStatsRequestBuilder> {

    public ESIndicesStatsRequestBuilder(ElasticsearchClient client, ESIndicesStatsAction action) {
        super(client, action, new ESIndicesStatsRequest());
    }

    /**
     * Sets all flags to return all nodestats.
     */
    public ESIndicesStatsRequestBuilder all() {
        request.all();
        return this;
    }

    /**
     * Clears all nodestats.
     */
    public ESIndicesStatsRequestBuilder clear() {
        request.clear();
        return this;
    }

    /**
     * Document types to return nodestats for. Mainly affects {@link #setIndexing(boolean)} when
     * enabled, returning specific indexing nodestats for those types.
     */
    public ESIndicesStatsRequestBuilder setTypes(String... types) {
        request.types(types);
        return this;
    }


    public ESIndicesStatsRequestBuilder setDocs(boolean docs) {
        request.flag( ESIndicesStatsRequest.DOCS, docs);
        return this;
    }

    public ESIndicesStatsRequestBuilder setStore(boolean store) {
        request.flag( ESIndicesStatsRequest.STORE, store);
        return this;
    }

    public ESIndicesStatsRequestBuilder setIndexing(boolean indexing) {
        request.flag( ESIndicesStatsRequest.INDEXING, indexing);
        return this;
    }

    public ESIndicesStatsRequestBuilder setGet(boolean get) {
        request.flag( ESIndicesStatsRequest.GET, get);
        return this;
    }

    public ESIndicesStatsRequestBuilder setSearch(boolean search) {
        request.flag( ESIndicesStatsRequest.SEARCH, search);
        return this;
    }

    public ESIndicesStatsRequestBuilder setMerge(boolean merge) {
        request.flag( ESIndicesStatsRequest.MERGE, merge);
        return this;
    }

    public ESIndicesStatsRequestBuilder setRefresh(boolean refresh) {
        request.flag( ESIndicesStatsRequest.REFRESH, refresh);
        return this;
    }

    public ESIndicesStatsRequestBuilder setFlush(boolean flush) {
        request.flag( ESIndicesStatsRequest.FLUSH, flush);
        return this;
    }

    public ESIndicesStatsRequestBuilder setWarmer(boolean warmer) {
        request.flag( ESIndicesStatsRequest.WARMER, warmer);
        return this;
    }

    public ESIndicesStatsRequestBuilder setQueryCache(boolean queryCache) {
        request.flag( ESIndicesStatsRequest.QUERY_CACHE, queryCache);
        return this;
    }

    public ESIndicesStatsRequestBuilder setFieldData(boolean fieldData) {
        request.flag( ESIndicesStatsRequest.FIELDDATA, fieldData);
        return this;
    }


    // 6.x版本不支持
//    public ESIndicesStatsRequestBuilder setPercolate(boolean percolate) {
//        request.percolate(percolate);
//        return this;
//    }

    public ESIndicesStatsRequestBuilder setSegments(boolean segments) {
        request.flag( ESIndicesStatsRequest.SEGMENTS, segments);
        return this;
    }

    public ESIndicesStatsRequestBuilder setCompletion(boolean completion) {
        request.flag( ESIndicesStatsRequest.COMPLETION, completion);
        return this;
    }


    public ESIndicesStatsRequestBuilder setTranslog(boolean translog) {
        request.flag( ESIndicesStatsRequest.TRANSLOG, translog);
        return this;
    }

    public ESIndicesStatsRequestBuilder setSuggest(boolean suggest) {
        request.flag( ESIndicesStatsRequest.SUGGEST, suggest);
        return this;
    }

    public ESIndicesStatsRequestBuilder setRequestCache(boolean requestCache) {
        request.flag( ESIndicesStatsRequest.REQUEST_CACHE, requestCache);
        return this;
    }

    public ESIndicesStatsRequestBuilder setRecovery(boolean recovery) {
        request.flag( ESIndicesStatsRequest.RECOVERY, recovery);
        return this;
    }

    public ESIndicesStatsRequestBuilder setLevel(IndicesStatsLevel level) {
        request.setLevel(level);
        return this;
    }
}
