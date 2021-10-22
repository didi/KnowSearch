package com.didichuxing.datachannel.arius.admin.core.service.es;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.ESClusterHealthResponse;

/**
 * @author d06679
 * @date 2019/5/8
 */
public interface ESClusterService {

    /**
     * 关闭集群re balance
     * @param cluster 集群
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    boolean syncCloseReBalance(String cluster, Integer retryCount) throws ESOperateException;

    /**
     * 打开集群re balance
     * @param cluster 集群
     * @param esVersion 版本
     * @return result
     * @throws ESOperateException
     */
    boolean syncOpenReBalance(String cluster, String esVersion) throws ESOperateException;

    /**
     * 配置远端集群
     * @param cluster 集群
     * @param remoteCluster 远端集群
     * @param tcpAddresses tcp地址
     * @param retryCount 重试次数
     * @return true/false
     * @throws ESOperateException
     */
    boolean syncPutRemoteCluster(String cluster, String remoteCluster, List<String> tcpAddresses,
                                 Integer retryCount) throws ESOperateException;

    /**
     * 判断配置否存在
     * @param cluster 集群
     * @param settingFlatName  setting名字
     * @return true/false
     */
    boolean settingExist(String cluster, String settingFlatName);

    /**
     * 配置集群的冷存搬迁配置
     * @param cluster       集群
     * @param inGoing
     * @param outGoing
     * @param moveSpeed
     * @param retryCount    重试次数
     * @return  true/false
     * @throws ESOperateException
     */
    boolean syncConfigColdDateMove(String cluster, int inGoing, int outGoing, String moveSpeed,
                                   int retryCount) throws ESOperateException;

    /**
     * 获取集群状态
     * @param clusteName
     * @return
     */
    ClusterStatusEnum getClusterStatus(String clusteName);

    /**
     * 获取某个集群内索引别名到索引名称的映射
     * @param cluster
     * @return
     */
    Map<String/*alias*/, Set<String>> getAliasMap(String cluster);

    /**
     * 判断es client是否存活
     *
     * @param cluster
     * @param clientAddress
     * @return
     */
    boolean judgeClientAlive(String cluster, String clientAddress);

    /**
     * 获取集群状态信息
     *
     * @param clusterName
     * @return
     */
    ESClusterHealthResponse getClusterHealth(String clusterName);
}
