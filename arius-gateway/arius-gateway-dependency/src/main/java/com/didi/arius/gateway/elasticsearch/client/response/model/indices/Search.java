package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Search {
    @JSONField(name = "open_contexts")
    private long openContexts;

    @JSONField(name = "query_total")
    private long queryTotal;

    @JSONField(name = "query_time_in_millis")
    private long queryTimeInMillis;

    @JSONField(name = "query_current")
    private long queryCurrent;

    @JSONField(name = "fetch_total")
    private long fetchTotal;

    @JSONField(name = "fetch_time_in_millis")
    private long fetchTimeInMillis;

    @JSONField(name = "fetch_current")
    private long fetchCurrent;

    @JSONField(name = "scroll_total")
    private long scrollTotal;

    @JSONField(name = "scroll_time_in_millis")
    private long scrollTimeInMillis;

    @JSONField(name = "scroll_current")
    private long scrollCurrent;

    public long getOpenContexts() {
        return openContexts;
    }

    public void setOpenContexts(long openContexts) {
        this.openContexts = openContexts;
    }

    public long getQueryTotal() {
        return queryTotal;
    }

    public void setQueryTotal(long queryTotal) {
        this.queryTotal = queryTotal;
    }

    public long getQueryTimeInMillis() {
        return queryTimeInMillis;
    }

    public void setQueryTimeInMillis(long queryTimeInMillis) {
        this.queryTimeInMillis = queryTimeInMillis;
    }

    public long getQueryCurrent() {
        return queryCurrent;
    }

    public void setQueryCurrent(long queryCurrent) {
        this.queryCurrent = queryCurrent;
    }

    public long getFetchTotal() {
        return fetchTotal;
    }

    public void setFetchTotal(long fetchTotal) {
        this.fetchTotal = fetchTotal;
    }

    public long getFetchTimeInMillis() {
        return fetchTimeInMillis;
    }

    public void setFetchTimeInMillis(long fetchTimeInMillis) {
        this.fetchTimeInMillis = fetchTimeInMillis;
    }

    public long getFetchCurrent() {
        return fetchCurrent;
    }

    public void setFetchCurrent(long fetchCurrent) {
        this.fetchCurrent = fetchCurrent;
    }

    public long getScrollTotal() {
        return scrollTotal;
    }

    public void setScrollTotal(long scrollTotal) {
        this.scrollTotal = scrollTotal;
    }

    public long getScrollTimeInMillis() {
        return scrollTimeInMillis;
    }

    public void setScrollTimeInMillis(long scrollTimeInMillis) {
        this.scrollTimeInMillis = scrollTimeInMillis;
    }

    public long getScrollCurrent() {
        return scrollCurrent;
    }

    public void setScrollCurrent(long scrollCurrent) {
        this.scrollCurrent = scrollCurrent;
    }
}
