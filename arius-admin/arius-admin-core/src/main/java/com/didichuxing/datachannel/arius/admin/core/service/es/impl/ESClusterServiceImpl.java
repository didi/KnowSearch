package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterStatusEnum;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterDAO;
import com.google.common.collect.Maps;

/**
 * @author d06679
 * @date 2019/5/8
 */
@Service
public class ESClusterServiceImpl implements ESClusterService {

    private static final ILog LOGGER = LogFactory.getLog(ESClusterServiceImpl.class);

    @Autowired
    private ESClusterDAO      esClusterDAO;

    /**
     * 关闭集群re balance
     * @param cluster    集群
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    @Override
    public boolean syncCloseReBalance(String cluster, Integer retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("syncCloseReBalance", retryCount,
            () -> esClusterDAO.configReBalanceOperate(cluster, "none"));
    }

    /**
     * 打开集群rebalance
     *
     * @param cluster   集群
     * @param esVersion 版本
     * @return result
     * @throws ESOperateException
     */
    @Override
    public boolean syncOpenReBalance(String cluster, String esVersion) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("syncOpenReBalance", 3,
            () -> esClusterDAO.configReBalanceOperate(cluster, "all"));
    }

    /**
     * 配置远端集群
     *
     * @param cluster       集群
     * @param remoteCluster 远端集群
     * @param tcpAddresses  tcp地址
     * @param retryCount    重试次数
     * @return true/false
     */
    @Override
    public boolean syncPutRemoteCluster(String cluster, String remoteCluster, List<String> tcpAddresses,
                                        Integer retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("syncPutRemoteCluster", retryCount,
            () -> esClusterDAO.putPersistentRemoteClusters(cluster,
                String.format(ESOperateContant.REMOTE_CLUSTER_FORMAT, remoteCluster), tcpAddresses));
    }

    /**
     * 判断配置否存在
     *
     * @param cluster         集群
     * @param settingFlatName setting名字
     * @return true/false
     */
    @Override
    public boolean settingExist(String cluster, String settingFlatName) {
        Map<String, Object> clusterSettingMap = esClusterDAO.getPersistentClusterSettings(cluster);
        return clusterSettingMap.containsKey(settingFlatName);
    }

    /**
     * 配置集群的冷存搬迁配置
     *
     * @param cluster    集群
     * @param retryCount 重试次数
     * @return true/false
     * @throws ESOperateException
     */
    @Override
    public boolean syncConfigColdDateMove(String cluster, int inGoing, int outGoing, String moveSpeed,
                                          int retryCount) throws ESOperateException {

        Map<String, Object> configMap = Maps.newHashMap();

        if (inGoing > 0) {
            configMap.put(CLUSTER_ROUTING_ALLOCATION_OUTGOING, outGoing);
        }

        if (outGoing > 0) {
            configMap.put(CLUSTER_ROUTING_ALLOCATION_INGOING, inGoing);
        }

        configMap.put(COLD_MAX_BYTES_PER_SEC_KEY, moveSpeed);

        return ESOpTimeoutRetry.esRetryExecute("syncConfigColdDateMove", retryCount,
            () -> esClusterDAO.putPersistentConfig(cluster, configMap));
    }

    @Override
    public ClusterStatusEnum getClusterStatus(String cluster) {
        return esClusterDAO.getClusterStatus(cluster);
    }

    /**
     * 获取某个集群内索引别名到索引名称的映射
     *
     * @param cluster
     * @return
     */
    @Override
    public Map<String/*alias*/, Set<String>> getAliasMap(String cluster) {
        Map<String, Set<String>> ret = new HashMap<>();

        try {
            ESIndicesGetAliasResponse response = esClusterDAO.getClusterAlias(cluster);
            for (String index : response.getM().keySet()) {
                for (String alias : response.getM().get(index).getAliases().keySet()) {
                    if (!ret.containsKey(alias)) {
                        ret.put(alias, new HashSet<>());
                    }

                    ret.get(alias).add(index);
                }
            }

            return ret;
        } catch (Throwable t) {
            //            LOGGER.error("class=ClusterClientPool||method=getAliasMap||clusterName={}||errMsg=fail to get alias", cluster, t);
            return ret;
        }
    }

    /**
     * 判断es client是否存活
     *
     * @param cluster
     * @param address
     * @return
     */
    @Override
    public boolean judgeClientAlive(String cluster, String address) {
        ESClient esClient = getNotSniffESClient(cluster, address);

        if (Objects.isNull(esClient)) {
            return false;
        }

        try {
            ESClusterHealthResponse response = esClient.admin().cluster().prepareHealth().execute().actionGet(30,
                TimeUnit.SECONDS);
            return !response.isTimedOut();

        } catch (Exception e) {
            LOGGER.error(
                "class=ClusterClientPool||method=judgeAlive||cluster={}||client={}||msg=judgeAlive is exception!",
                cluster, address, e);
            return false;
        } finally {
            if (null != esClient) {
                esClient.close();
            }
        }
    }

    @Override
    public ESClusterHealthResponse getClusterHealth(String clusterName) {
        return esClusterDAO.getClusterHealth(clusterName);
    }

    /***************************************** private method ****************************************************/
    /**
     * 根据client地址 获得连接es client实例
     *
     * @param cluster
     * @param addresses
     * @return
     */
    private ESClient getNotSniffESClient(String cluster, String addresses) {
        if (StringUtils.isBlank(addresses) || StringUtils.isBlank(cluster)) {
            return null;
        }

        try {
            Map<String, Integer> addressMap = toAddressMap(addresses);
            if (MapUtils.isEmpty(addressMap)) {
                LOGGER.info("class=ClusterClientPool||method=getNotSniffESClient||addresses={}||msg=clusterClientError",
                    addresses);
                return null;
            }

            List<InetSocketTransportAddress> transportAddresses = Lists.newArrayList();
            for (String host : addressMap.keySet()) {
                int port = addressMap.get(host);
                transportAddresses
                    .add(new InetSocketTransportAddress(InetAddress.getByName(host), Integer.valueOf(port)));
            }

            TransportAddress[] transportAddressArr = new TransportAddress[transportAddresses.size()];

            ESClient esClient = new ESClient();
            esClient.addTransportAddresses(transportAddresses.toArray(transportAddressArr));
            esClient.setClusterName(cluster);
            esClient.start();

            return esClient;

        } catch (UnknownHostException e) {
            LOGGER.info("class=ClusterClientPool||method=getNotSniffESClient||addresses={}||msg=exception", addresses,
                e);
        }

        return null;
    }

    private Map<String, Integer> toAddressMap(String addresses) {
        if (StringUtils.isEmpty(addresses)) {
            return null;
        }

        String[] addressList = addresses.split(",");
        Map<String, Integer> addressMap = Maps.newHashMap();
        for (String address : addressList) {
            String[] hostAndPort = address.trim().split(":");
            addressMap.put(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
        }
        return addressMap;
    }
}
