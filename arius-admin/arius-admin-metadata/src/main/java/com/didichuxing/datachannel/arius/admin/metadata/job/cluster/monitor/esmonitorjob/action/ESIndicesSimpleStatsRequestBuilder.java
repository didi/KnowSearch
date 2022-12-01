package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.action;

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

import com.didiglobal.knowframework.elasticsearch.client.request.broadcast.ESBroadcastOperationRequestBuilder;
import com.didiglobal.knowframework.elasticsearch.client.request.index.stats.IndicesStatsLevel;
import org.elasticsearch.client.ElasticsearchClient;

import static com.didiglobal.knowframework.elasticsearch.client.request.index.stats.ESIndicesStatsRequest.*;

/**
 * A request to get indices level nodestats. Allow to enable different nodestats to be returned.
 * <p>
 * By default, the {@link #setDocs(boolean)}, {@link #setStore(boolean)}, {@link #setIndexing(boolean)}
 * are enabled. Other nodestats can be enabled as well.zx
 * <p>
 * All the nodestats to be returned can be cleared using {@link #clear()}, at which point, specific
 * nodestats can be enabled.
 */
public class ESIndicesSimpleStatsRequestBuilder extends
                                                ESBroadcastOperationRequestBuilder<ESIndicesSimpleStatsRequest, ESIndicesSimpleStatsResponse, ESIndicesSimpleStatsRequestBuilder> {

    public ESIndicesSimpleStatsRequestBuilder(ElasticsearchClient client, ESIndicesSimpleStatsAction action) {
        super(client, action, new ESIndicesSimpleStatsRequest());
    }

    /**
     * Sets all flags to return all nodestats.
     */
    public ESIndicesSimpleStatsRequestBuilder all() {
        request.all();
        return this;
    }

    /**
     * Clears all nodestats.
     */
    public ESIndicesSimpleStatsRequestBuilder clear() {
        request.clear();
        return this;
    }

    /**
     * Document types to return nodestats for. Mainly affects {@link #setIndexing(boolean)} when
     * enabled, returning specific indexing nodestats for those types.
     */
    public ESIndicesSimpleStatsRequestBuilder setTypes(String... types) {
        request.types(types);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setDocs(boolean docs) {
        request.flag(DOCS, docs);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setStore(boolean store) {
        request.flag(STORE, store);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setIndexing(boolean indexing) {
        request.flag(INDEXING, indexing);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setGet(boolean get) {
        request.flag(GET, get);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setSearch(boolean search) {
        request.flag(SEARCH, search);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setMerge(boolean merge) {
        request.flag(MERGE, merge);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setRefresh(boolean refresh) {
        request.flag(REFRESH, refresh);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setFlush(boolean flush) {
        request.flag(FLUSH, flush);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setWarmer(boolean warmer) {
        request.flag(WARMER, warmer);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setQueryCache(boolean queryCache) {
        request.flag(QUERY_CACHE, queryCache);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setFieldData(boolean fieldData) {
        request.flag(FIELDDATA, fieldData);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setSegments(boolean segments) {
        request.flag(SEGMENTS, segments);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setCompletion(boolean completion) {
        request.flag(COMPLETION, completion);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setTranslog(boolean translog) {
        request.flag(TRANSLOG, translog);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setSuggest(boolean suggest) {
        request.flag(SUGGEST, suggest);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setRequestCache(boolean requestCache) {
        request.flag(REQUEST_CACHE, requestCache);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setRecovery(boolean recovery) {
        request.flag(RECOVERY, recovery);
        return this;
    }

    public ESIndicesSimpleStatsRequestBuilder setLevel(IndicesStatsLevel level) {
        request.setLevel(level);
        return this;
    }
}
