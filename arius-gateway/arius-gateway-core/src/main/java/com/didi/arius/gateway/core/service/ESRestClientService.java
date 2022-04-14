package com.didi.arius.gateway.core.service;

import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.elasticsearch.client.ESClient;

import java.util.Map;

/**
 * @author fitz
 * @date 2021/5/26 5:25 下午
 */
public interface ESRestClientService {
    /**
     * 根据cluster名字获取esClient
     * @See getClientStrict
     * @param clusterName
     * @return
     */
    ESClient getClient(String clusterName, String actionName);

    /**
     *
     * 根据cluster名字获取esClient
     * @param clusterName
     * @return
     */
    ESClient getClientStrict(String clusterName, String actionName);

    /**
     * 获取admin权限的esClient
     * @return
     */
    ESClient getAdminClient(String actionName);

    /**
     * 更新Map中的esClient客户端，并关掉废弃的esClient
     * @param newDataCenterMap
     */
    void resetClients(Map<String, ESCluster> newDataCenterMap);

    /**
     * 获取esClusterMap
     * @return
     */
    Map<String, ESCluster> getESClusterMap();
}
