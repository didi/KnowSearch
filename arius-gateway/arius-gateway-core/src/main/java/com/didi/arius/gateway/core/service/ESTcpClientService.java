package com.didi.arius.gateway.core.service;

import com.didi.arius.gateway.common.metadata.ESCluster;
import org.elasticsearch.client.Client;

import java.util.Map;

/**
 * @author fitz
 * @date 2021/5/26 5:26 下午
 */
public interface ESTcpClientService {

    /**
     * 根据cluster名字获取esClient
     * @param clusterName
     * @return
     */
    Client getClient(String clusterName);

    /**
     * 获取admin权限的esClient
     * @return
     */
    Client getAdminClient();

    /**
     * 更新 dataCenterMap
     * @param newDataCenterMap
     */
    void resetClients(Map<String, ESCluster> newDataCenterMap);

    /**
     * 获取 dataCenterMap
     * @return
     */
    Map<String, ESCluster> getDataCenterMap();

    /**
     * set dataCenterMap
     * @param dataCenterMap
     */
    void setDataCenterMap(Map<String, ESCluster> dataCenterMap);
}
