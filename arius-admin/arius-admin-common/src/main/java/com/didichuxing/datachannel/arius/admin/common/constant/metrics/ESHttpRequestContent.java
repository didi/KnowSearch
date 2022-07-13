package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

/**
 * Created by linyunan on 2021-08-11
 */
public class ESHttpRequestContent {

    private ESHttpRequestContent() {
    }

    /**
     * 获取集群搬迁shard
     */
    public static final String GET_MOVING_SHARD            = "_cat/recovery?v&h=i,s,t,st,shost,thost&active_only=true";

    /**
     * 获取集群pendingTask
     */
    public static final String GET_PENDING_TASKS           = "/_cluster/pending_tasks";
    /**
     * 获取集群http信息
     */
    public static final String GET_STATS_HTTP              = "/_nodes/stats/http?level=cluster";
    public static final String GET_STATS_FS                = "/_nodes/stats/fs?level=cluster";

    /**
     * 获取集群索引信息
     */
    public static final String GET_INDICES                 = "/_cat/indices?v&h=index,dc";

    /**
     * 获取集群shard
     */
    public static final String GET_SHARDS                  = "/_cat/shards/";
    /**
     * 获取集群shard
     */
    public static final String GET_CLUSTER_STATS           = "/_cluster/stats/";

    public static final String GET_SHARDS_ALL              = "_cat/shards?v&h=index,prirep,shard,store,ip,node";
    public static final String GET_SHARDS_NODE             = "_cat/shards?v&h=node";

    public static final String GET_TEMPLATE_NAME           = "/_cat/templates?v&h=name";

    public static final String MASTER_TIMEOUT              = "&master_timeout=";

    /**
     * 获取集群模板segments 信息
     */
    public static final String GET_PATH_SEGMENTS           = "/_cat/segments/";

    public static final String GET_PATH_SEGMENTS_PART_INFO = GET_PATH_SEGMENTS + "?v&h=size,size.memory,index";

    /**
     * 获取大索引请求内容
     */
    public static String getBigIndicesRequestContent(String masterTimeout) {
        if (AriusObjUtils.isBlack(masterTimeout)) {
            return GET_INDICES;
        }
        return GET_INDICES + MASTER_TIMEOUT + masterTimeout;
    }

    public static String getShardsAllInfoRequestContent(String masterTimeout) {
        if (AriusObjUtils.isBlack(masterTimeout)) {
            return GET_SHARDS_ALL;
        }
        return GET_SHARDS_ALL + MASTER_TIMEOUT + masterTimeout;
    }

    public static String getShards2NodeRequestContent(String masterTimeout) {
        if (AriusObjUtils.isBlack(masterTimeout)) {
            return GET_SHARDS_NODE;
        }
        return GET_SHARDS_NODE + MASTER_TIMEOUT + masterTimeout;
    }

    public static String getShardToNodeRequestContentByIndexName(String indexName, String masterTimeout) {
        if (AriusObjUtils.isBlack(masterTimeout)) {
            return GET_SHARDS + indexName + "?v&h=node";
        }

        return GET_SHARDS + indexName + "?v&h=node" + MASTER_TIMEOUT + masterTimeout;
    }

    public static String getShards2NodeInfoRequestContent(String indexName, String masterTimeout) {
        if (AriusObjUtils.isBlack(masterTimeout)) {
            return GET_SHARDS + indexName + "?v";
        }

        return GET_SHARDS + indexName + "?v" + MASTER_TIMEOUT + masterTimeout;
    }

    public static String getTemplateNameRequestContent() {
        return GET_TEMPLATE_NAME;
    }

    public static String getSegmentsPartInfoRequestContent() {
        return GET_PATH_SEGMENTS_PART_INFO;
    }
}