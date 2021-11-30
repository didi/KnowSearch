package com.didi.arius.gateway.elasticsearch.client.response.model.threadpool;

import com.alibaba.fastjson.annotation.JSONField;

public class ThreadPoolNode {

    @JSONField(name = "threads")
    private long threads;

    @JSONField(name = "queue")
    private long queue;

    @JSONField(name = "active")
    private long active;

    @JSONField(name = "rejected")
    private long rejected;

    @JSONField(name = "largest")
    private long largest;

    @JSONField(name = "completed")
    private long completed;

    public ThreadPoolNode() {
        // pass
    }

    public long getThreads() {
        return threads;
    }

    public void setThreads(long threads) {
        this.threads = threads;
    }

    public long getQueue() {
        return queue;
    }

    public void setQueue(long queue) {
        this.queue = queue;
    }

    public long getActive() {
        return active;
    }

    public void setActive(long active) {
        this.active = active;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public long getLargest() {
        return largest;
    }

    public void setLargest(long largest) {
        this.largest = largest;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }
}
