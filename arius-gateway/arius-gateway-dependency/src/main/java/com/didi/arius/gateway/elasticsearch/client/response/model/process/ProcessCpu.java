package com.didi.arius.gateway.elasticsearch.client.response.model.process;

import com.alibaba.fastjson.annotation.JSONField;

public class ProcessCpu {
    @JSONField(name = "percent")
    private long percent;

    @JSONField(name = "total_in_millis")
    private long totalInMillis;

    public long getPercent() {
        return percent;
    }

    public void setPercent(long percent) {
        this.percent = percent;
    }

    public long getTotalInMillis() {
        return totalInMillis;
    }

    public void setTotalInMillis(long totalInMillis) {
        this.totalInMillis = totalInMillis;
    }
}
