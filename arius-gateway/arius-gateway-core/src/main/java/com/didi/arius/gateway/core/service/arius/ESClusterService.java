package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.MetaVersion;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.elasticsearch.client.ESClient;

import java.util.Map;

public interface ESClusterService {
    /**
     * 获取 esClusterMap
     * @return
     */
    Map<String, ESCluster> listESCluster();

    /**
     * 根据cluster 获取 MetaVersion
     * @param cluster
     * @return
     */
    MetaVersion getMetaVersionByCluster(String cluster);

    /**
     * 更新集群列表
     */
    void resetESClusaterInfo();

    /**
     * 根据请求上下文获取esClient
     * @param queryContext
     * @return
     */
    ESClient getClient(QueryContext queryContext);

    /**
     * 根据请求上下文获取esClient, 会根据索引模版判定
     * @param queryContext
     * @param indexTemplate
     * @return
     */
    ESClient getClient(QueryContext queryContext, IndexTemplate indexTemplate);

    /**
     * 根据cluster获取esClient, 并且加入queryContext
     * @param queryContext
     * @param clusterName
     * @return
     */
    ESClient getClientFromCluster(QueryContext queryContext, String clusterName);

    /**
     * 获取具有写入权限的esClient
     * @param indexTemplate
     * @return
     */
    ESClient getWriteClient(IndexTemplate indexTemplate);
}
