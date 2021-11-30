package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Indexing {

    @JSONField(name = "index_total")
    private long indexTotal;

    @JSONField(name = "index_time_in_millis")
    private long indexTimeInMillis;

    @JSONField(name = "index_current")
    private long indexCurrent;

    @JSONField(name = "index_failed")
    private long indexFailed;

    @JSONField(name = "delete_total")
    private long deleteTotal;

    @JSONField(name = "delete_time_in_millis")
    private long deleteTimeInMillis;

    @JSONField(name = "delete_current")
    private long deleteCurrent;

    @JSONField(name = "noop_update_total")
    private long noopUpdateTotal;

    @JSONField(name = "is_throttled")
    private boolean isThrottled;

    @JSONField(name = "throttle_time_in_millis")
    private long throttleTimeInMillis;

    public Indexing() {
        // pass
    }

    public long getIndexTotal() {
        return indexTotal;
    }

    public void setIndexTotal(long indexTotal) {
        this.indexTotal = indexTotal;
    }

    public long getIndexTimeInMillis() {
        return indexTimeInMillis;
    }

    public void setIndexTimeInMillis(long indexTimeInMillis) {
        this.indexTimeInMillis = indexTimeInMillis;
    }

    public long getIndexCurrent() {
        return indexCurrent;
    }

    public void setIndexCurrent(long indexCurrent) {
        this.indexCurrent = indexCurrent;
    }

    public long getIndexFailed() {
        return indexFailed;
    }

    public void setIndexFailed(long indexFailed) {
        this.indexFailed = indexFailed;
    }

    public long getDeleteTotal() {
        return deleteTotal;
    }

    public void setDeleteTotal(long deleteTotal) {
        this.deleteTotal = deleteTotal;
    }

    public long getDeleteTimeInMillis() {
        return deleteTimeInMillis;
    }

    public void setDeleteTimeInMillis(long deleteTimeInMillis) {
        this.deleteTimeInMillis = deleteTimeInMillis;
    }

    public long getDeleteCurrent() {
        return deleteCurrent;
    }

    public void setDeleteCurrent(long deleteCurrent) {
        this.deleteCurrent = deleteCurrent;
    }

    public long getNoopUpdateTotal() {
        return noopUpdateTotal;
    }

    public void setNoopUpdateTotal(long noopUpdateTotal) {
        this.noopUpdateTotal = noopUpdateTotal;
    }

    public boolean getThrottled() {
        return isThrottled;
    }

    public void setThrottled(boolean throttled) {
        this.isThrottled = throttled;
    }

    public long getThrottleTimeInMillis() {
        return throttleTimeInMillis;
    }

    public void setThrottleTimeInMillis(long throttleTimeInMillis) {
        this.throttleTimeInMillis = throttleTimeInMillis;
    }
}
