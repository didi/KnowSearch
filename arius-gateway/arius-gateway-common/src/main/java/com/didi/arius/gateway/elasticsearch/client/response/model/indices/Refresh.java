package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Refresh {
    @JSONField(name = "total")
    private long total;

    @JSONField(name = "total_time_in_millis")
    private long totalTimeInMillis;

    public Refresh() {
        // pass
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalTimeInMillis() {
        return totalTimeInMillis;
    }

    public void setTotalTimeInMillis(long totalTimeInMillis) {
        this.totalTimeInMillis = totalTimeInMillis;
    }
}
