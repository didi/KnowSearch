package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_TIMEOUT;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.VERSION;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.VERSION_INNER_NUMBER;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.VERSION_NUMBER;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.common.NodeAttrInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ECSegmentOnIp;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterTaskStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterThreadStats;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ESClusterThreadPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.PendingTaskAnalysisVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.TaskMissionAnalysisVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterConnectionStatus;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ESClusterNodesSettingResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ESClusterServiceImpl implements ESClusterService {

    private static final ILog LOGGER                = LogFactory.getLog(ESClusterServiceImpl.class);

    @Autowired
    private ESClusterDAO      esClusterDAO;

    private final String      ACKNOWLEDGED          = "acknowledged";
    private final String      SHARDS                = "_shards";
    private final String      FAILED                = "failed";
    private final String      DESCRIPTION           = "description";
    private final String      START_TIME_IN_MILLIS  = "start_time_in_millis";
    private final String      RUNNING_TIME_IN_NANOS = "running_time_in_nanos";
    private final String      ACTION                = "action";
    private final String      NAME                  = "name";
    private final String      NODES                 = "nodes";
    private final String      TASKS                 = "tasks";

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
        if (CollectionUtils.isEmpty(tcpAddresses)) { return false;}

        return ESOpTimeoutRetry.esRetryExecute("syncPutRemoteCluster", retryCount,
            () -> esClusterDAO.putPersistentRemoteClusters(cluster,
                String.format(ESOperateConstant.REMOTE_CLUSTER_FORMAT, remoteCluster), tcpAddresses));
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
        if (null == clusterSettingMap) {
            return false;
        }
        return clusterSettingMap.containsKey(settingFlatName);
    }

    @Override
    public Map<String, List<String>> syncGetNode2PluginsMap(String cluster) {
        return esClusterDAO.getNode2PluginsMap(cluster, 3);
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
            ESIndicesGetAliasResponse response = esClusterDAO.getClusterAlias(cluster, 3);
            if (response == null || response.getM() == null) {
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
            LOGGER.error("class=ClusterClientPool||method=syncGetAliasMap||clusterName={}||errMsg=fail to get alias",
                cluster, t);
            return ret;
        }
    }

    @Override
    public int syncGetClientAlivePercent(String cluster, String password, String clientAddresses) {
        if (null == cluster || StringUtils.isBlank(clientAddresses)) {
            return 0;
        }

        List<String> addresses = ListUtils.string2StrList(clientAddresses);

        int alive = 0;
        for (String address : addresses) {
            boolean isAlive = judgeClientAlive(cluster, password, address);
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
    public boolean judgeClientAlive(String cluster, String password, String address) {

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
        try {
            transportAddresses.add(new InetSocketTransportAddress(InetAddress.getByName(host), Integer.parseInt(port)));
            esClient.addTransportAddresses(transportAddresses.toArray(new TransportAddress[0]));
            esClient.setClusterName(cluster);
            if (StringUtils.isNotBlank(password)) {
                esClient.setPassword(password);
            }
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
        } finally {
            esClient.close();
        }
    }

    @Override
    public ESClusterHealthResponse syncGetClusterHealth(String clusterName) {
        return esClusterDAO.getClusterHealth(clusterName, 3);
    }

    @Override
    public List<ESClusterTaskStatsResponse> syncGetClusterTaskStats(String clusterName) {
        return esClusterDAO.getClusterTaskStats(clusterName);
    }

    @Override
    public ClusterHealthEnum syncGetClusterHealthEnum(String clusterName) {
        ESClusterHealthResponse clusterHealthResponse = esClusterDAO.getClusterHealth(clusterName, 3);

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
        for (ECSegmentOnIp ecSegmentsOnIp : esClusterDAO.getSegmentsOfIpByCluster(clusterName)) {
            if (segmentsOnIpMap.containsKey(ecSegmentsOnIp.getIp())) {
                Integer newSegments = segmentsOnIpMap.get(ecSegmentsOnIp.getIp())
                                      + Integer.parseInt(ecSegmentsOnIp.getSegment());
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
        return esClusterDAO.getESVersionByCluster(cluster, 3);
    }

    @Override
    public Map<String, ClusterNodeInfo> syncGetAllSettingsByCluster(String cluster) {
        return esClusterDAO.getAllSettingsByCluster(cluster, 3);
    }

    @Override
    public Map<String, ClusterNodeSettings> syncGetPartOfSettingsByCluster(String cluster) {
        return esClusterDAO.getPartOfSettingsByCluster(cluster, 3);
    }

    @Override
    public Set<String> syncGetAllNodesAttributes(String cluster) {
        Set<String> nodeAttributes = Sets.newHashSet();
        List<NodeAttrInfo> nodeAttrInfos = esClusterDAO.syncGetAllNodesAttributes(cluster);
        if (CollectionUtils.isEmpty(nodeAttrInfos)) {
            return nodeAttributes;
        }

        //对于所有的节点属性进行去重的操作
        nodeAttrInfos.forEach(nodeAttrInfo -> nodeAttributes.add(nodeAttrInfo.getAttribute()));

        return nodeAttributes;
    }

    @Override
    public Result<Void> checkSameCluster(String password, List<String> addresses) {
        Set<String> clusters = new HashSet<>();
        for (String address : addresses) {
            ESClient client = new ESClient();
            try {
                if (StringUtils.isNotBlank(password)) {
                    client.setPassword(password);
                }
                client.addTransportAddresses(address);
                client.start();
                ESClusterNodesSettingResponse response = client.admin().cluster().prepareNodesSetting().execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
                if (RestStatus.OK.getStatus() == response.getRestStatus().getStatus()) {
                    clusters.add(response.getClusterName());
                }
            } catch (Exception e) {
                LOGGER.error(
                    "class=ESClusterServiceImpl||method=getClusterRackByHttpAddress||msg=get rack error||httpAddress={}||msg=client start error",
                    addresses);
            } finally {
                client.close();
            }
        }
        return clusters.size() > 1 ? Result.buildFail() : Result.buildSucc();
    }

    @Override
    public String synGetESVersionByHttpAddress(String addresses, String password) {
        ESClient client = new ESClient();
        client.addTransportAddresses(addresses);

        if (StringUtils.isNotBlank(password)) {
            client.setPassword(password);
        }
        String esVersion = null;
        try {
            client.start();
            DirectRequest directRequest = new DirectRequest("GET", "");
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                JSONObject version = JSONObject.parseObject(directResponse.getResponseContent()).getJSONObject(VERSION);
                esVersion = version.getString(VERSION_NUMBER);
                if (version.containsKey(VERSION_INNER_NUMBER)) {
                    String innerVersion = version.getString(VERSION_INNER_NUMBER).split("\\.")[0].trim();
                    esVersion = Strings.isNullOrEmpty(innerVersion) ? esVersion : esVersion + "." + innerVersion;
                }
            }
            return esVersion;
        } catch (Exception e) {
            LOGGER.warn(
                "class=ESClusterServiceImpl||method=synGetESVersionByHttpAddress||address={}||mg=get es segments fail",
                addresses, e);
            return null;
        } finally {
            client.close();
        }
    }

    @Override
    public ClusterConnectionStatus checkClusterPassword(String addresses, String password) {
        ESClient client = new ESClient();
        client.addTransportAddresses(addresses);
        if (StringUtils.isNotBlank(password)) {
            client.setPassword(password);
        }

        try {
            client.start();
            DirectRequest directRequest = new DirectRequest("GET", "");
            client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            return ClusterConnectionStatus.NORMAL;
        } catch (Exception e) {
            LOGGER.warn(
                "class=ESClusterServiceImpl||method=checkClusterWithoutPassword||address={}||mg=get es segments fail",
                addresses, e);
            if (e.getCause().getMessage().contains("Unauthorized")) {
                return ClusterConnectionStatus.UNAUTHORIZED;
            } else {
                return ClusterConnectionStatus.DISCONNECTED;
            }
        } finally {
            client.close();
        }
    }

    @Override
    public ESClusterThreadStats syncGetThreadStatsByCluster(String cluster) {
        List<ESClusterThreadPO> threadStats = esClusterDAO.syncGetThreadStatsByCluster(cluster);
        ESClusterThreadStats esClusterThreadStats = new ESClusterThreadStats(cluster, 0L, 0L, 0L, 0L, 0L, 0L);
        if (threadStats != null) {
            esClusterThreadStats
                .setManagement(threadStats.stream().filter(thread -> "management".equals(thread.getThreadName()))
                    .mapToLong(ESClusterThreadPO::getQueueNum).sum());
            esClusterThreadStats
                .setRefresh(threadStats.stream().filter(thread -> "refresh".equals(thread.getThreadName()))
                    .mapToLong(ESClusterThreadPO::getQueueNum).sum());
            esClusterThreadStats.setFlush(threadStats.stream().filter(thread -> "flush".equals(thread.getThreadName()))
                .mapToLong(ESClusterThreadPO::getQueueNum).sum());
            esClusterThreadStats
                .setMerge(threadStats.stream().filter(thread -> "force_merge".equals(thread.getThreadName()))
                    .mapToLong(ESClusterThreadPO::getQueueNum).sum());
            esClusterThreadStats
                .setSearch(threadStats.stream().filter(thread -> "search".equals(thread.getThreadName()))
                    .mapToLong(ESClusterThreadPO::getQueueNum).sum());
            esClusterThreadStats.setWrite(threadStats.stream().filter(thread -> "write".equals(thread.getThreadName()))
                .mapToLong(ESClusterThreadPO::getQueueNum).sum());
        }
        return esClusterThreadStats;
    }

    @Override
    public ESClusterHealthResponse syncGetClusterHealthAtIndicesLevel(String phyClusterName) {
        return esClusterDAO.getClusterHealthAtIndicesLevel(phyClusterName);
    }

    @Override
    public List<PendingTaskAnalysisVO> pendingTaskAnalysis(String cluster) {
        String response = esClusterDAO.pendingTask(cluster);
        return Optional.ofNullable(response).map(JSONObject::parseObject)
            .map(jsonObject -> jsonObject.getJSONArray(TASKS))
            .map(tasks -> JSONObject.parseArray(tasks.toJSONString(), PendingTaskAnalysisVO.class))
            .orElse(new ArrayList<>());
    }

    @Override
    public List<TaskMissionAnalysisVO> taskMissionAnalysis(String cluster) {
        String response = esClusterDAO.taskMission(cluster);
        return Optional.ofNullable(response).map(JSONObject::parseObject)
            .map(jsonObject -> buildTaskMission(jsonObject)).orElse(new ArrayList<>());
    }

    @Override
    public String hotThreadAnalysis(String cluster) {
        String response = esClusterDAO.hotThread(cluster);
        return response;
    }

    @Override
    public boolean abnormalShardAllocationRetry(String cluster) {
        String response = esClusterDAO.abnormalShardAllocationRetry(cluster);
        return Optional.ofNullable(response).map(JSONObject::parseObject)
            .map(jsonObject -> jsonObject.getBoolean(ACKNOWLEDGED)).orElse(false);
    }

    @Override
    public boolean clearFieldDataMemory(String cluster) {
        String response = esClusterDAO.clearFieldDataMemory(cluster);
        return Optional.ofNullable(response).map(JSONObject::parseObject)
            .map(jsonObject -> jsonObject.getJSONObject(SHARDS)).map(shards -> shards.getInteger(FAILED))
            .map(failed -> failed.equals(0)).orElse(false);
    }

    @Override
    public List<String> syncGetTcpAddress(String cluster) {
        return esClusterDAO.getNodeTcpAddress(cluster);
    }

    private List<TaskMissionAnalysisVO> buildTaskMission(JSONObject responseJson) {
        List<TaskMissionAnalysisVO> vos = new ArrayList<>();
        JSONObject nodes = responseJson.getJSONObject(NODES);
        nodes.keySet().forEach(key -> {
            Optional.ofNullable((JSONObject) nodes.get(key)).map(o -> o.getJSONObject(TASKS)).ifPresent(nodeTasks -> {
                nodeTasks.forEach((key1, val) -> {
                    JSONObject nodeInfo = (JSONObject) val;
                    String nodeName = nodes.getJSONObject(key).getString(NAME);
                    TaskMissionAnalysisVO taskMissionAnalysisVO = new TaskMissionAnalysisVO();
                    Optional.ofNullable(nodeInfo).ifPresent(o -> {
                        taskMissionAnalysisVO.setAction(o.getString(ACTION));
                        taskMissionAnalysisVO.setNode(nodeName);
                        taskMissionAnalysisVO.setDescription(o.getString(DESCRIPTION));
                        taskMissionAnalysisVO.setStartTimeInMillis(o.getLong(START_TIME_IN_MILLIS));
                        taskMissionAnalysisVO.setRunningTimeInNanos(o.getInteger(RUNNING_TIME_IN_NANOS));
                        vos.add(taskMissionAnalysisVO);
                    });
                });
            });
        });
        return vos;
    }

}