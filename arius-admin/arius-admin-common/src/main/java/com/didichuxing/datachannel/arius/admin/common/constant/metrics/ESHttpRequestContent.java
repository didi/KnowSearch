package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

/**
 * Created by linyunan on 2021-08-11
 */
public class ESHttpRequestContent {

    private ESHttpRequestContent(){}

    /**
     * 获取集群搬迁shard
     */
    public static final String GET_MOVING_SHARD  = "_cat/recovery?v&h=i,s,t,st,shost,thost&active_only=true";

    /**
     * 获取集群pendingTask
     */
    public static final String GET_PENDING_TASKS = "/_cluster/pending_tasks";

    /**
     * 获取集群索引信息
     */
    public static final String GET_INDICES       = "/_cat/indices?v&h=index,dc";

    /**
     * 获取集群shard
     */
    public static final String GET_SHARDS        = "_cat/shards/";

    public static final String GET_BIG_SHARDS    = "_cat/shards?v&h=index,prirep,shard,store,ip,node";

    public static final String GET_TEMPLATE_NAME = "/_cat/templates?v&h=name";

    private static final String MASTER_TIMEOUT = "&master_timeout=";

    /**
     * 获取大索引请求内容
     */
    public static String getBigIndicesRequestContent(String masterTimeout) {
        if (AriusObjUtils.isBlack(masterTimeout)) {
            return GET_INDICES;
        }
        return GET_INDICES + MASTER_TIMEOUT + masterTimeout;
    }

    public static String getBigShardsRequestContent(String masterTimeout) {
        if (AriusObjUtils.isBlack(masterTimeout)) {
            return GET_BIG_SHARDS;
        }
        return GET_BIG_SHARDS + MASTER_TIMEOUT + masterTimeout;
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
}