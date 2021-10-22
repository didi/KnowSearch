package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Get {
    @JSONField(name = "total")
    private long total;

    @JSONField(name = "time_in_millis")
    private long timeInMillis;

    @JSONField(name = "exists_total")
    private long existsTotal;

    @JSONField(name = "exists_time_in_millis")
    private long existsTimeInMillis;

    @JSONField(name = "missing_total")
    private long missingTotal;

    @JSONField(name = "missing_time_in_millis")
    private long missingTimeInMillis;

    @JSONField(name = "current")
    private long current;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public long getExistsTotal() {
        return existsTotal;
    }

    public void setExistsTotal(long existsTotal) {
        this.existsTotal = existsTotal;
    }

    public long getExistsTimeInMillis() {
        return existsTimeInMillis;
    }

    public void setExistsTimeInMillis(long existsTimeInMillis) {
        this.existsTimeInMillis = existsTimeInMillis;
    }

    public long getMissingTotal() {
        return missingTotal;
    }

    public void setMissingTotal(long missingTotal) {
        this.missingTotal = missingTotal;
    }

    public long getMissingTimeInMillis() {
        return missingTimeInMillis;
    }

    public void setMissingTimeInMillis(long missingTimeInMillis) {
        this.missingTimeInMillis = missingTimeInMillis;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }
}
