package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Suggest {
    @JSONField(name = "total")
    private long total;

    @JSONField(name = "time_in_millis")
    private long timeInMillis;

    @JSONField(name = "current")
    private long current;

    public Suggest() {
        // pass
    }

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

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }
}
