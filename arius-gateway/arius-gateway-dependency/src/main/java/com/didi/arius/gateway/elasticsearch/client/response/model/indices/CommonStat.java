package com.didi.arius.gateway.elasticsearch.client.response.model.indices;


import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;

public class CommonStat {
    @JSONField(name = "routing")
    private Routing routing;

    @JSONField(name = "docs")
    private Docs docs;

    @JSONField(name = "store")
    private Store store;

    @JSONField(name = "indexing")
    private Indexing indexing;

    @JSONField(name = "get")
    private Get get;

    @JSONField(name = "search")
    private Search search;

    @JSONField(name = "merges")
    private Merges merges;

    @JSONField(name = "refresh")
    private Refresh refresh;

    @JSONField(name = "flush")
    private Flush flush;

    @JSONField(name = "warmer")
    private Warmer warmer;

    @JSONField(name = "query_cache")
    private QueryCache queryCache;

    @JSONField(name = "fielddata")
    private Fielddata fielddata;

    @JSONField(name = "percolate")
    private Percolate percolate;

    @JSONField(name = "completion")
    private Completion completion;

    @JSONField(name = "segments")
    private Segments segments;

    @JSONField(name = "translog")
    private Translog translog;

    @JSONField(name = "suggest")
    private Suggest suggest;

    @JSONField(name = "request_cache")
    private RequestCache requestCache;

    @JSONField(name = "recovery")
    private Recovery recovery;

    @JSONField(name = "shards")
    private Map<String, List<Map<String, CommonStat>>> shards;

    @JSONField(name = "indices")
    private Map<String, CommonStat> indices;


    public Routing getRouting() {
        return routing;
    }

    public void setRouting(Routing routing) {
        this.routing = routing;
    }

    public Docs getDocs() {
        return docs;
    }

    public void setDocs(Docs docs) {
        this.docs = docs;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Indexing getIndexing() {
        return indexing;
    }

    public void setIndexing(Indexing indexing) {
        this.indexing = indexing;
    }

    public Get getGet() {
        return get;
    }

    public void setGet(Get get) {
        this.get = get;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public Merges getMerges() {
        return merges;
    }

    public void setMerges(Merges merges) {
        this.merges = merges;
    }

    public Refresh getRefresh() {
        return refresh;
    }

    public void setRefresh(Refresh refresh) {
        this.refresh = refresh;
    }

    public Flush getFlush() {
        return flush;
    }

    public void setFlush(Flush flush) {
        this.flush = flush;
    }

    public Warmer getWarmer() {
        return warmer;
    }

    public void setWarmer(Warmer warmer) {
        this.warmer = warmer;
    }

    public QueryCache getQueryCache() {
        return queryCache;
    }

    public void setQueryCache(QueryCache queryCache) {
        this.queryCache = queryCache;
    }

    public Fielddata getFielddata() {
        return fielddata;
    }

    public void setFielddata(Fielddata fielddata) {
        this.fielddata = fielddata;
    }

    public Percolate getPercolate() {
        return percolate;
    }

    public void setPercolate(Percolate percolate) {
        this.percolate = percolate;
    }

    public Completion getCompletion() {
        return completion;
    }

    public void setCompletion(Completion completion) {
        this.completion = completion;
    }

    public Segments getSegments() {
        return segments;
    }

    public void setSegments(Segments segments) {
        this.segments = segments;
    }

    public Translog getTranslog() {
        return translog;
    }

    public void setTranslog(Translog translog) {
        this.translog = translog;
    }

    public Suggest getSuggest() {
        return suggest;
    }

    public void setSuggest(Suggest suggest) {
        this.suggest = suggest;
    }

    public RequestCache getRequestCache() {
        return requestCache;
    }

    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    public Recovery getRecovery() {
        return recovery;
    }

    public void setRecovery(Recovery recovery) {
        this.recovery = recovery;
    }


    public Map<String, List<Map<String, CommonStat>>> getShards() {
        return shards;
    }

    public void setShards(Map<String, List<Map<String, CommonStat>>> shards) {
        this.shards = shards;
    }

    public Map<String, CommonStat> getIndices() {
        return indices;
    }

    public void setIndices(Map<String, CommonStat> indices) {
        this.indices = indices;
    }
}
