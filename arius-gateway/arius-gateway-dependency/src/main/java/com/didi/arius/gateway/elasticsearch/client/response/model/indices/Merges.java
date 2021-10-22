package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Merges {
    @JSONField(name = "current")
    private long current;

    @JSONField(name = "current_docs")
    private long currentDocs;

    @JSONField(name = "current_size_in_bytes")
    private long currentSizeInBytes;

    @JSONField(name = "total")
    private long total;

    @JSONField(name = "total_time_in_millis")
    private long totalTimeInMillis;

    @JSONField(name = "total_docs")
    private long totalDocs;

    @JSONField(name = "total_size_in_bytes")
    private long totalSizeInBytes;

    @JSONField(name = "total_stopped_time_in_millis")
    private long totalStoppedTimeInMillis;

    @JSONField(name = "total_throttled_time_in_millis")
    private long totalThrottledTimeInMillis;

    @JSONField(name = "total_auto_throttle_in_bytes")
    private long totalAutoThrottleInBytes;

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getCurrentDocs() {
        return currentDocs;
    }

    public void setCurrentDocs(long currentDocs) {
        this.currentDocs = currentDocs;
    }

    public long getCurrentSizeInBytes() {
        return currentSizeInBytes;
    }

    public void setCurrentSizeInBytes(long currentSizeInBytes) {
        this.currentSizeInBytes = currentSizeInBytes;
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

    public long getTotalDocs() {
        return totalDocs;
    }

    public void setTotalDocs(long totalDocs) {
        this.totalDocs = totalDocs;
    }

    public long getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public void setTotalSizeInBytes(long totalSizeInBytes) {
        this.totalSizeInBytes = totalSizeInBytes;
    }

    public long getTotalStoppedTimeInMillis() {
        return totalStoppedTimeInMillis;
    }

    public void setTotalStoppedTimeInMillis(long totalStoppedTimeInMillis) {
        this.totalStoppedTimeInMillis = totalStoppedTimeInMillis;
    }

    public long getTotalThrottledTimeInMillis() {
        return totalThrottledTimeInMillis;
    }

    public void setTotalThrottledTimeInMillis(long totalThrottledTimeInMillis) {
        this.totalThrottledTimeInMillis = totalThrottledTimeInMillis;
    }

    public long getTotalAutoThrottleInBytes() {
        return totalAutoThrottleInBytes;
    }

    public void setTotalAutoThrottleInBytes(long totalAutoThrottleInBytes) {
        this.totalAutoThrottleInBytes = totalAutoThrottleInBytes;
    }
}
