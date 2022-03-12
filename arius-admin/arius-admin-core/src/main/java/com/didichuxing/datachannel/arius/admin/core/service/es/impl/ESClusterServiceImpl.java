package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ECSegmentsOnIps;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ESClusterNodesResponse;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.*;

/**
 * @author d06679
 * @date 2019/5/8
 */
@Service
public class ESClusterServiceImpl implements ESClusterService {

    private static final ILog LOGGER = LogFactory.getLog(ESClusterServiceImpl.class);

    @Autowired
    private ESClusterDAO      esClusterDAO;

    @Autowired
    private ClusterPhyService clusterPhyService;

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
    public boolean hasSettingExist(String cluster, String settingFlatName) {
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
    public Map<String, List<String>> syncGetNode2PluginsMap(String cluster) {
        return esClusterDAO.getNode2PluginsMap(cluster);
    }

    /**
     * 获取某个集群内索引别名到索引名称的映射
     *
     * @param cluster
     * @return
     */
    @Override
    public Map<String/*alias*/, Set<String>> syncGetAliasMap(String cluster) {
        Map<String, Set<String>> ret = new HashMap<>();

        try {
            ESIndicesGetAliasResponse response = esClusterDAO.getClusterAlias(cluster);
            if(response == null || response.getM() == null) {
                return ret;
            }
            for (String index : response.getM().keySet()) {
                for (String alias : response.getM().get(index).getAliases().keySet()) {
                    if (!ret.containsKey(alias)) {
                        ret.put(alias, new HashSet<>());
                    }
                    ret.get(alias).add(index);
                }
            }
            return ret;
        } catch (Exception t) {
            LOGGER.error("class=ClusterClientPool||method=syncGetAliasMap||clusterName={}||errMsg=fail to get alias", cluster, t);
            return ret;
        }
    }

    @Override
    public int syncGetClientAlivePercent(String cluster, String clientAddresses) {
        if (null == cluster || StringUtils.isBlank(clientAddresses)) {
            return 0;
        }

        List<String> addresses = ListUtils.string2StrList(clientAddresses);

        int alive = 0;
        for (String address : addresses) {
            boolean isAlive = judgeClientAlive(cluster, address);
            if (isAlive) {
                alive++;
            }
        }

        return alive * 100 / addresses.size();
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

        String[] hostAndPortStr = StringUtils.split(address, ":");
        if (null == hostAndPortStr || hostAndPortStr.length <= 1) {
            LOGGER.info("class=ClusterClientPool||method=getNotSniffESClient||addresses={}||msg=clusterClientError",
                address);
            return false;
        }

        String host = hostAndPortStr[0];
        String port = hostAndPortStr[1];
        List<InetSocketTransportAddress> transportAddresses = Lists.newArrayList();
        ESClient esClient = new ESClient();
        try  {
            transportAddresses.add(new InetSocketTransportAddress(InetAddress.getByName(host), Integer.parseInt(port)));
            esClient.addTransportAddresses(transportAddresses.toArray(new TransportAddress[0]));
            esClient.setClusterName(cluster);
            esClient.start();

            ESClusterHealthResponse response = esClient.admin().cluster().prepareHealth().execute().actionGet(30,
                TimeUnit.SECONDS);
            return !response.isTimedOut();
        } catch (Exception e) {
            esClient.close();
            LOGGER.error(
                "class=ESClusterServiceImpl||method=judgeClientAlive||cluster={}||client={}||msg=judgeAlive is excepjudgeClientAlivetion!",
                cluster, address, e);
            return false;
        }finally {
            esClient.close();
        }
    }

	@Override
    public ESClusterHealthResponse syncGetClusterHealth(String clusterName) {
        return esClusterDAO.getClusterHealth(clusterName);
    }

    @Override
    public ClusterHealthEnum syncGetClusterHealthEnum(String clusterName) {
        ESClusterHealthResponse clusterHealthResponse = esClusterDAO.getClusterHealth(clusterName);

        ClusterHealthEnum clusterHealthEnum = ClusterHealthEnum.UNKNOWN;
        if (clusterHealthResponse != null) {
            clusterHealthEnum = ClusterHealthEnum.valuesOf(clusterHealthResponse.getStatus());
        }
        return clusterHealthEnum;
    }

    @Override
    public ESClusterStatsResponse syncGetClusterStats(String clusterName) {
        return esClusterDAO.getClusterStats(clusterName);
    }

    @Override
    public ESClusterGetSettingsAllResponse syncGetClusterSetting(String cluster) {
        return esClusterDAO.getClusterSetting(cluster);
    }

    @Override
    public Map<String, Integer> synGetSegmentsOfIpByCluster(String clusterName) {
        Map<String, Integer> segmentsOnIpMap = Maps.newHashMap();
        for (ECSegmentsOnIps ecSegmentsOnIp : esClusterDAO.getSegmentsOfIpByCluster(clusterName)) {
            if (segmentsOnIpMap.containsKey(ecSegmentsOnIp.getIp())) {
                Integer newSegments = segmentsOnIpMap.get(ecSegmentsOnIp.getIp()) + Integer.parseInt(ecSegmentsOnIp.getSegment());
                segmentsOnIpMap.put(ecSegmentsOnIp.getIp(), newSegments);
            } else {
                segmentsOnIpMap.put(ecSegmentsOnIp.getIp(), Integer.valueOf(ecSegmentsOnIp.getSegment()));
            }
        }
        return segmentsOnIpMap;
    }

    @Override
    public boolean syncPutPersistentConfig(String cluster, Map<String, Object> configMap) {
        return esClusterDAO.putPersistentConfig(cluster, configMap);
    }

    @Override
    public String synGetESVersionByCluster(String cluster) {
        return esClusterDAO.getESVersionByCluster(cluster);
    }

    @Override
    public Map<String, ClusterNodeInfo> syncGetAllSettingsByCluster(String cluster) {
        return esClusterDAO.getAllSettingsByCluster(cluster);
    }

    @Override
    public Map<String, ClusterNodeSettings> syncGetPartOfSettingsByCluster(String cluster) {
        return esClusterDAO.getPartOfSettingsByCluster(cluster);
    }

    @Override
    public Set<String> syncGetAllNodesAttributes(String cluster) {
        return esClusterDAO.syncGetAllNodesAttributes(cluster);
    }

    @Override
    public Result<Set<String>> getClusterRackByHttpAddress(String addresses) {
        Set<String> racks = new HashSet<>();
        ESClient client = new ESClient();
        client.addTransportAddresses(addresses);

        try {
            client.start();
            ESClusterNodesResponse response = client.admin().cluster().prepareNodes().execute().actionGet(10,
                    TimeUnit.SECONDS);
            if (RestStatus.OK.getStatus() == response.getRestStatus().getStatus()) {
                response.getNodes().forEach((key, value) -> {
                    if (value.getRoles().contains(ES_ROLE_DATA)) {
                        if (value.getAttributes() == null) {
                            racks.add("*");
                        } else {
                            racks.add(value.getAttributes().getOrDefault(RACK, "*"));
                        }
                    }
                });
                return Result.buildSucc(racks);
            } else {
                return Result.buildParamIllegal(String.format("通过地址:%s获取rack失败", addresses));
            }
        } catch (Exception e) {
            LOGGER.error("class=ESClusterServiceImpl||method=getClusterRackByHttpAddress||msg=get rack error||httpAddress={}||msg=client start error", addresses);
            return Result.buildParamIllegal(String.format("通过地址:%s获取rack失败", addresses));
        } finally {
            client.close();
        }
    }

    /***************************************** private method ****************************************************/
}
