package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

/**
 * 详细介绍类情况.
 *
 * @ClassName ClusterQuickCommandEnum
 * @Author gyp
 * @Date 2022/6/1
 * @Version 1.0
 */
public enum ClusterQuickCommandMethodsEnum {
    /**
     * node_state分析
     */
    SHARDS("_shards", "_nodes/stats"),
    /**
     * indices分布
     */
    INDICES("GET", "_cat/indices"),
    /**
     * shard分布
     */
    SHARD("GET", "_cat/shards"),
    /**
     * pending task分析
     */
    PENDING_TASK("GET", "/_cluster/pending_tasks"),
    /**
     * task任务分析 MissionAnalysis
     */
    TASK_MISSION_ANALYSIS("GET", "_tasks?detailed"),
    /**
     * 热点线程分析
     */
    HOT_THREAD("GET", "_nodes/hot_threads"),
    /**
     * shard分配说明
     */
    SHARD_ASSIGNMENT("GET", "_cluster/allocation/explain"),
    /**
     * 异常shard分配重试 abnormal-shard-allocation-retry
     */
    ABNORMAL_SHARD_RETRY("POST", "/_cluster/reroute?retry_failed=true"),
    /**
     * 清除fielddata内存 clear-fielddata-memory
     */
    CLEAR_FIELDDATA_MEMORY("POST", "_cache/clear?fielddata=true");

    private String method;
    private String uri;

    ClusterQuickCommandMethodsEnum(String method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}