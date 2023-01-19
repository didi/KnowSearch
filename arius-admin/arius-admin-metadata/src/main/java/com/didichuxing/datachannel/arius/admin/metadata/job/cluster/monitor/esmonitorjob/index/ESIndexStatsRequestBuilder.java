package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.index;

import static com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.index.ESIndexStatsRequest.*;

import org.elasticsearch.client.ElasticsearchClient;

import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node.ESBroadcastTimeoutRequestBuilder;
import com.didiglobal.knowframework.elasticsearch.client.request.index.stats.IndicesStatsLevel;

public class ESIndexStatsRequestBuilder extends
                                        ESBroadcastTimeoutRequestBuilder<ESIndexStatsRequest, ESIndexStatsResponse, ESIndexStatsRequestBuilder> {

    public ESIndexStatsRequestBuilder(ElasticsearchClient client, ESIndexStatsAction action) {
        super(client, action, new ESIndexStatsRequest());
    }

    /**
     * Sets all flags to return all nodestats.
     */
    public ESIndexStatsRequestBuilder all() {
        request.all();
        return this;
    }

    /**
     * Clears all nodestats.
     */
    public ESIndexStatsRequestBuilder clear() {
        request.clear();
        return this;
    }

    /**
     * Document types to return nodestats for. Mainly affects {@link #setIndexing(boolean)} when
     * enabled, returning specific indexing nodestats for those types.
     */
    public ESIndexStatsRequestBuilder setTypes(String... types) {
        request.types(types);
        return this;
    }

    public ESIndexStatsRequestBuilder setDocs(boolean docs) {
        request.flag(DOCS, docs);
        return this;
    }

    public ESIndexStatsRequestBuilder setStore(boolean store) {
        request.flag(STORE, store);
        return this;
    }

    public ESIndexStatsRequestBuilder setIndexing(boolean indexing) {
        request.flag(INDEXING, indexing);
        return this;
    }

    public ESIndexStatsRequestBuilder setGet(boolean get) {
        request.flag(GET, get);
        return this;
    }

    public ESIndexStatsRequestBuilder setSearch(boolean search) {
        request.flag(SEARCH, search);
        return this;
    }

    public ESIndexStatsRequestBuilder setMerge(boolean merge) {
        request.flag(MERGE, merge);
        return this;
    }

    public ESIndexStatsRequestBuilder setRefresh(boolean refresh) {
        request.flag(REFRESH, refresh);
        return this;
    }

    public ESIndexStatsRequestBuilder setFlush(boolean flush) {
        request.flag(FLUSH, flush);
        return this;
    }

    public ESIndexStatsRequestBuilder setWarmer(boolean warmer) {
        request.flag(WARMER, warmer);
        return this;
    }

    public ESIndexStatsRequestBuilder setQueryCache(boolean queryCache) {
        request.flag(QUERY_CACHE, queryCache);
        return this;
    }

    public ESIndexStatsRequestBuilder setFieldData(boolean fieldData) {
        request.flag(FIELDDATA, fieldData);
        return this;
    }

    public ESIndexStatsRequestBuilder setSegments(boolean segments) {
        request.flag(SEGMENTS, segments);
        return this;
    }

    public ESIndexStatsRequestBuilder setCompletion(boolean completion) {
        request.flag(COMPLETION, completion);
        return this;
    }

    public ESIndexStatsRequestBuilder setTranslog(boolean translog) {
        request.flag(TRANSLOG, translog);
        return this;
    }

    public ESIndexStatsRequestBuilder setSuggest(boolean suggest) {
        request.flag(SUGGEST, suggest);
        return this;
    }

    public ESIndexStatsRequestBuilder setRequestCache(boolean requestCache) {
        request.flag(REQUEST_CACHE, requestCache);
        return this;
    }

    public ESIndexStatsRequestBuilder setRecovery(boolean recovery) {
        request.flag(RECOVERY, recovery);
        return this;
    }

    public ESIndexStatsRequestBuilder setLevel(IndicesStatsLevel level) {
        request.setLevel(level);
        return this;
    }

    public ESIndexStatsRequestBuilder setTimeout(String timeout) {
        request.timeout(timeout);
        return this;
    }
}
