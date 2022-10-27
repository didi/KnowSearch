package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandMethodsEnum.ABNORMAL_SHARD_RETRY;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandMethodsEnum.CLEAR_FIELDDATA_MEMORY;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandMethodsEnum.HOT_THREAD;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandMethodsEnum.PENDING_TASK;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandMethodsEnum.TASK_MISSION_ANALYSIS;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.common.NodeAllocationInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.common.NodeAttrInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESResponsePluginInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllAction;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ECSegmentOnIp;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterTaskStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ESClusterThreadPO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NullESClientException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ParsingExceptionUtils;
import com.didichuxing.datachannel.arius.admin.common.util.TimeValueUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.elasticsearch.client.request.cat.ESCatAction;
import com.didiglobal.logi.elasticsearch.client.request.cat.ESCatRequest;
import com.didiglobal.logi.elasticsearch.client.request.cluster.getsetting.ESClusterGetSettingsRequest;
import com.didiglobal.logi.elasticsearch.client.request.cluster.health.ESClusterHealthRequest;
import com.didiglobal.logi.elasticsearch.client.request.cluster.updatesetting.ESClusterUpdateSettingsRequestBuilder;
import com.didiglobal.logi.elasticsearch.client.request.index.stats.IndicesStatsLevel;
import com.didiglobal.logi.elasticsearch.client.response.cat.ESCatResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.getsetting.ESClusterGetSettingsResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ESClusterNodesResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ESClusterNodesSettingResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.updatesetting.ESClusterUpdateSettingsResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.didiglobal.logi.elasticsearch.client.utils.JsonUtils;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Repository;

/**
 * @author d06679
 */
@Repository
public class ESClusterDAO extends BaseESDAO {
    private final static String REMOTE_TARGET_CLUSTER="_remote/info?filter_path=%s.%s";
    private final static String REMOTE_TARGET_CLUSTER_COUNT="%s:*/_count?ignore_unavailable=true";
    private final static String CONNECTED="connected";

    /**
     * 配置集群re balance开关
     * @param cluster 集群名称
     * @param value  all /  none
     * @return 成功 true   失败 false
     */
    public boolean configReBalanceOperate(String cluster, String value) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) {
            LOGGER.warn(
                    "class={}||method=configReBalanceOperate||clusterName={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster);
            throw new NullESClientException(cluster);
        }
        try {
            ESClusterUpdateSettingsResponse response = client.admin().cluster().prepareUpdateSettings()
                    .addPersistent(REBALANCE, value).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

            return response.getAcknowledged();
        } catch (Exception e) {
            LOGGER.error("class={}||method=configReBalanceOperate||clusterName={}||value={}",
                    getClass().getSimpleName(), cluster, value,e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return false;
    }

    /**
     * 获取集群 persistent 配置
     * @param cluster 集群名称
     * @return map<flat_setting_name, setting_value>
     */
    public Map<String, Object> getPersistentClusterSettings(String cluster) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) {
            LOGGER.warn(
                    "class={}||method=getPersistentClusterSettings||clusterName={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster);
            throw new NullESClientException(cluster);
        }

        try {
            ESClusterGetSettingsRequest request = new ESClusterGetSettingsRequest();
            ESClusterGetSettingsResponse response = client.admin().cluster().getSetting(request)
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

            return JsonUtils.flatObject(response.getPersistentObj());
        } catch (Exception e) {
            LOGGER.error("class={}||method=getPersistentClusterSettings||clusterName={}",
                    getClass().getSimpleName(), cluster,e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return null;
    }

    /**
     * 获取集群配置
     * @param cluster 集群名称
     * @return response
     */
    public ESClusterGetSettingsAllResponse getClusterSetting(String cluster) throws ESOperateException {
        ESClusterGetSettingsAllResponse response = null;
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) {
            throw new NullESClientException(cluster);
        }
        try {
        
            response = client.admin().cluster()
                    .execute(ESClusterGetSettingsAllAction.INSTANCE, new ESClusterGetSettingsAllRequest())
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterSetting||clusterName={}", cluster, e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return response;
    }

    /**
     * put-setting
     * @param cluster 集群
     * @param remoteCluster 集群名字
     * @param tcpAddresses 地址
     * @return true/false
     */
    public boolean putPersistentRemoteClusters(String cluster, String remoteCluster, List<String> tcpAddresses)
            throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) {
          throw new NullESClientException(cluster);
        }

        JSONArray addresses = new JSONArray();
        addresses.addAll(tcpAddresses);
        try {
        
            ESClusterUpdateSettingsResponse response = client.admin().cluster().prepareUpdateSettings()
                    .addPersistent(remoteCluster, addresses).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        
            return response.getAcknowledged();
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=putPersistentRemoteClusters||clusterName={}", cluster, e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return false;
    }

    /**
     * put-setting
     * @param cluster 集群
     * @param configMap 配置
     * @return true/false
     */
    public boolean putPersistentConfig(String cluster, Map<String, Object> configMap) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) {
            LOGGER.warn("class={}||method=putPersistentConfig||clusterName={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster);
            throw new NullESClientException(cluster);
        }

        try {
            ESClusterUpdateSettingsRequestBuilder updateSettingsRequestBuilder = client.admin().cluster()
                    .prepareUpdateSettings();

            for (Map.Entry<String, Object> entry : configMap.entrySet()) {
                String configName = entry.getKey();
                updateSettingsRequestBuilder.addPersistent(configName, configMap.get(configName));
            }

            ESClusterUpdateSettingsResponse response = client.admin().cluster()
                    .updateSetting(updateSettingsRequestBuilder.request()).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

            return response.getAcknowledged();
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=putPersistentConfig||clusterName={}", cluster,e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return false;
    }

    /**
     * 获取物理集群下各个节点的插件名称列表
     *
     * @param cluster
     * @return map
     */
    public Map<String/*nodeName*/, List<String>/*pluginName*/> getNode2PluginsMap(String cluster, Integer tryTimes) throws ESOperateException{
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) {
            LOGGER.warn(
                    "class=ESClusterDAO||method=getNode2PluginsMap||clusterName={}||errMsg=esClient is null",
                    cluster);
            throw new NullESClientException(cluster);
        }

        ESCatRequest esCatRequest = new ESCatRequest();
        esCatRequest.setUri("plugins");
        ESCatResponse esCatResponse = null;
        try {
            do {
                esCatResponse = client.admin().cluster().execute(ESCatAction.INSTANCE, esCatRequest)
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            } while (tryTimes-- > 0 && null == esCatResponse);
        } catch (Exception e) {
            LOGGER.warn(
                "class=ESClusterDAO||method=getNode2PluginsMap||clusterName={}" + "||errMsg=can't get node  plugin",
                cluster);
            ParsingExceptionUtils.abnormalTermination(e);
        }

        return Optional.ofNullable(esCatResponse).map(ESCatResponse::getResponse).map(Object::toString)
            .map(esCatResponseString2ESResponsePluginInfoListFunc).map(eSResponsePluginInfoList2MapFunc).orElse(null);

    }

    private final Function<List<ESResponsePluginInfo>, Map<String/*nodeName*/, List<String>/*pluginName*/>> eSResponsePluginInfoList2MapFunc                 = eSResponsePluginInfos -> ConvertUtil
        .list2MapOfList(eSResponsePluginInfos, ESResponsePluginInfo::getName, ESResponsePluginInfo::getComponent);

    private final Function<String, List<ESResponsePluginInfo>>                                              esCatResponseString2ESResponsePluginInfoListFunc = esCatResponse -> JSON
        .parseArray(esCatResponse, ESResponsePluginInfo.class);

    /**
     * 获取物理集群下各个节点的资源设置信息
     * @param cluster 物理集群名称
     * @return 集群下的节点资源使用信息列表
     */
    public List<NodeAllocationInfo> getNodeAllocationInfoByCluster(String cluster, Integer tryTimes) throws ESOperateException {
        ESClient esClient = esOpClient.getESClient(cluster);
        if (esClient == null) {
            LOGGER.warn(
                "class=ESClusterDAO||method=getNodeAllocationInfoByCluster||clusterName={}" + "||errMsg=client is null",
                cluster);
            throw new NullESClientException(cluster);
        }

        ESCatRequest esCatRequest = new ESCatRequest();
        esCatRequest.setUri("allocation");
        ESCatResponse esCatResponse = null;
        try {
            do {
                esCatResponse = esClient.admin().cluster().execute(ESCatAction.INSTANCE, esCatRequest)
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            } while (tryTimes-- > 0 && null == esCatResponse);

        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=getNodeAllocationInfoByCluster||clusterName={}"
                        + "||errMsg=can't get allocation info",
                cluster);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return Optional.ofNullable(esCatResponse).map(ESCatResponse::getResponse).map(Object::toString)
            .map(esCatResponseString2NodeAllocationInfoListFunc).orElse(null);
    }

    private final Function<String, List<NodeAllocationInfo>> esCatResponseString2NodeAllocationInfoListFunc = esCatResponse -> JSON
        .parseArray(esCatResponse, NodeAllocationInfo.class);

    /**
     * 获取集群中索引的别名信息
     *
     * @param cluster
     * @param tryTimes
     * @return
     */
    public ESIndicesGetAliasResponse getClusterAlias(String cluster, Integer tryTimes) {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            return null;
        }
        ESIndicesGetAliasResponse esIndicesGetAliasResponse = null;
        try {
            do {
                esIndicesGetAliasResponse = client.admin().indices().prepareAlias().execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.MINUTES);
            } while (tryTimes-- > 0 && null == esIndicesGetAliasResponse);

        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterAlias||clusterName={}||errMsg=query error. ", cluster,
                e);
            return null;
        }
        return esIndicesGetAliasResponse;
    }

    /**
    * 获取集群状态信息
    *
    * @param cluster
    * @return
    */
    public ESClusterHealthResponse getClusterHealth(String cluster, Integer tryTimes) {
        ESClient esClient = esOpClient.getESClient(cluster);
        if (esClient == null) {
            LOGGER.error("class=ESClusterDAO||method=getClusterHealth||clusterName={}||errMsg=esClient is null",
                cluster);
            return null;
        }
        ESClusterHealthResponse esClusterHealthResponse = null;
        Long minTimeoutNum = 1L;
        Long maxTimeoutNum = tryTimes.longValue();
        try {
            ESClusterHealthRequest request = new ESClusterHealthRequest();
            do {
                esClusterHealthResponse = esClient.admin().cluster().health(request)
                    .actionGet(/*降低因为抖动导致的等待时常,等待时常从低到高进行重试*/minTimeoutNum * ES_OPERATE_MIN_TIMEOUT, TimeUnit.SECONDS);
                minTimeoutNum++;
                if (minTimeoutNum > maxTimeoutNum) {
                    minTimeoutNum = maxTimeoutNum;
                }
            } while (tryTimes-- > 0 && null == esClusterHealthResponse);
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterHealth||clusterName={}||errMsg=query error. ", cluster,
                e);
            return null;
        }
        return esClusterHealthResponse;
    }

    public Map<String, ClusterNodeSettings> getPartOfSettingsByCluster(String cluster, Integer tryTimes) {
        ESClusterNodesSettingResponse response = null;

        try {
            ESClient client = esOpClient.getESClient(cluster);
            if (null == client) {
                return null;
            }

            do {
                response = client.admin().cluster().prepareNodesSetting().execute().actionGet(ES_OPERATE_TIMEOUT,
                    TimeUnit.SECONDS);
            } while (tryTimes-- > 0 && null == response);

        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=getPartOfSettingsByCluster||cluster={}||mg=get es setting fail",
                cluster, e);
            return null;
        }
        return Optional.ofNullable(response).map(ESClusterNodesSettingResponse::getNodes).orElse(null);
    }

    /**
    * 获取全量ES集群nodeSetting
    * @param cluster
    * @return
    */
    public Map<String, ClusterNodeInfo> getAllSettingsByCluster(String cluster, Integer tryTimes) {
        ESClusterNodesResponse response = null;
        try {
            ESClient client = esOpClient.getESClient(cluster);
            if (null == client) {
                LOGGER.warn("class=ESClusterDAO||method=getAllSettingsByCluster||cluster={}||mg=ESClient is empty",
                    cluster);
                return null;
            }
            do {
                response = client.admin().cluster().prepareNodes().execute().actionGet(ES_OPERATE_TIMEOUT,
                    TimeUnit.SECONDS);
            } while (tryTimes-- > 0 && null == response);
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=getAllSettingsByCluster||cluster={}||mg=get es setting fail",
                cluster, e);
            return null;
        }
        return Optional.ofNullable(response).map(ESClusterNodesResponse::getNodes).orElse(null);
    }

    public String getESVersionByCluster(String cluster, Integer tryTimes) {
        ESClient client = esOpClient.getESClient(cluster);
        String esVersion = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=getESVersionByCluster||clusterName={}||errMsg=esClient is null",
                cluster);
            return null;
        }
        DirectResponse directResponse = null;
        try {
            DirectRequest directRequest = new DirectRequest("GET", "");

            do {
                directResponse = client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            } while (tryTimes-- > 0 && null == directResponse);
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=getESVersionByCluster||cluster={}||mg=get es segments fail",
                cluster, e);
            return null;
        }
        if (directResponse.getRestStatus() == RestStatus.OK
            && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            esVersion = (String) JSONObject.parseObject(directResponse.getResponseContent()).getJSONObject("version")
                .get("number");
        }
        return esVersion;
    }

    /**
     * 获取集群节点ip下的segment数目
     *
     * @param clusterName 集群名称
     * @return
     */
    public List<ECSegmentOnIp> getSegmentsOfIpByCluster(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        List<ECSegmentOnIp> ecSegmentOnIps = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=getClusterStats||clusterName={}||errMsg=esClient is null",
                clusterName);
            return new ArrayList<>();
        }
        try {
            DirectRequest directRequest = new DirectRequest("GET", "_cat/nodes?v&h=sc,ip&format=json");
            DirectResponse directResponse = client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                ecSegmentOnIps = JSONArray.parseArray(directResponse.getResponseContent(), ECSegmentOnIp.class);
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=getSegmentsOfIpByCluster||cluster={}||mg=get es segments fail",
                clusterName, e);
            return new ArrayList<>();
        }
        return ecSegmentOnIps;
    }

    public ESClusterStatsResponse getClusterStats(String clusterName) {
        ESClusterStatsResponse responses = initESClusterStatsResponse();
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (Objects.isNull(esClient)) {
            LOGGER.error("class=ESClusterDAO||method=getClusterStats||clusterName={}||errMsg=esClient is null",
                clusterName);
            return responses;
        }

        try {
            DirectRequest directRequest = new DirectRequest("GET", "_cluster/stats");
            DirectResponse directResponse = esClient.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

            //获取
            DirectRequest directHealthRequest = new DirectRequest("GET", "_cat/health?format=json");
            DirectResponse directHealthResponse = esClient.direct(directHealthRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {

                if (directHealthResponse.getRestStatus() == RestStatus.OK
                        && StringUtils.isNoneBlank(directHealthResponse.getResponseContent())) {

                    JSONArray jsonArray = JSON.parseArray(directHealthResponse.getResponseContent());
                    jsonArray.stream()
                            .filter(Objects::nonNull)
                            .filter(j -> j instanceof JSONObject)
                            .map(j -> (JSONObject) j)
                            .map(jsonObject -> Long.parseLong(jsonObject.getString(SHARDS)) +
                                    Long.parseLong(jsonObject.getString(UNASSIGN)))
                            .findFirst()
                            .ifPresent(responses::setTotalShard);
                    jsonArray.stream()
                            .filter(Objects::nonNull)
                            .filter(j -> j instanceof JSONObject)
                            .map(j -> (JSONObject) j)
                            .map(jsonObject -> Long.parseLong(jsonObject.getString(PENDING_TASKS)))
                            .findFirst()
                            .ifPresent(responses::setPendingTasks);
                    jsonArray.stream()
                            .filter(Objects::nonNull)
                            .filter(j -> j instanceof JSONObject)
                            .map(j -> (JSONObject) j)
                            .map(jsonObject -> Long.parseLong(jsonObject.getString(UNASSIGN)))
                            .findFirst()
                            .ifPresent(responses::setUnassignedShardNum);
                }

                JSONObject jsonObject = JSON.parseObject(directResponse.getResponseContent());
                responses.setStatus(jsonObject.getObject(STATUS, String.class));

                JSONObject indicesObj = jsonObject.getJSONObject(INDICES);
                responses.setIndexCount(indicesObj.getLongValue(COUNT));


                JSONObject docsObj = indicesObj.getJSONObject(DOCS);
                responses.setDocsCount(docsObj.getLongValue(COUNT));

                JSONObject storeObj = indicesObj.getJSONObject(STORE);
                responses.setIndicesStoreSize(new ByteSizeValue(storeObj.getLongValue(SIZE_IN_BYTES)));

                JSONObject nodesObj = jsonObject.getJSONObject(NODES);
                JSONObject nodesCountObj = nodesObj.getJSONObject(COUNT);
                responses.setTotalNodes(nodesCountObj.getLongValue(TOTAL));

                setRoleNumberToResponses(responses, nodesCountObj);

                JSONObject osObj = nodesObj.getJSONObject(OS);
                JSONObject memObj = osObj.getJSONObject(MEM);
                responses.setMemTotal(new ByteSizeValue(memObj.getLongValue(TOTAL_IN_BYTES)));
                responses.setMemFree(new ByteSizeValue(memObj.getLongValue(FREE_IN_BYTES)));
                responses.setMemUsed(new ByteSizeValue(memObj.getLongValue(USED_IN_BYTES)));
                responses.setMemFreePercent(memObj.getIntValue(FREE_PERCENT));
                responses.setMemUsedPercent(memObj.getIntValue(USED_PERCENT));

                JSONObject fsObj = nodesObj.getJSONObject(FS);
                responses.setTotalFs(new ByteSizeValue(fsObj.getLongValue(TOTAL_IN_BYTES)));
                responses.setFreeFs(new ByteSizeValue(fsObj.getLongValue(FREE_IN_BYTES)));

                JSONObject jvmObj = nodesObj.getJSONObject(JVM);
                JSONObject jvmHeapObj = jvmObj.getJSONObject(MEM);
                responses.setTotalHeapMem(new ByteSizeValue(jvmHeapObj.getLongValue(HEAP_MAX_IN_BYTES)));
                responses.setUsedHeapMem(new ByteSizeValue(jvmHeapObj.getLongValue(HEAP_USED_IN_BYTES)));
            }

        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterStats||clusterName={}||errMsg=fail to get", clusterName,
                e);
        }

        return responses;
    }

    public List<ESClusterTaskStatsResponse> getClusterTaskStats(String clusterName) throws ESOperateException {
        List<ESClusterTaskStatsResponse> responses = Lists.newArrayList();
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (null == esClient) {
            LOGGER.error("class=ESClusterDAO||method=getClusterTaskStats||clusterName={}||errMsg=esClient is null", clusterName);
            throw new NullESClientException(clusterName);
        }

        try {
            DirectRequest taskStatsRequest = new DirectRequest("GET", "_cat/tasks?v&detailed&format=json");
            DirectResponse directResponse = esClient.direct(taskStatsRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                JSONArray jsonArray = JSON.parseArray(directResponse.getResponseContent());
                for (int i = 0; i < jsonArray.size(); i++) {
                    ESClusterTaskStatsResponse response = new ESClusterTaskStatsResponse();
                    JSONObject js = jsonArray.getJSONObject(i);
                    response.setAction(js.getString(ACTION));
                    response.setDescription(js.getString(DESCRIPTION));
                    response.setIp(js.getString(IP));
                    response.setNode(js.getString(NODE));
                    response.setTaskId(js.getString(TASK_ID));
                    response.setStartTime(Long.parseLong(js.getString(START_TIME)));
                    response.setType(js.getString(TYPE));
                    response.setParentTaskId(js.getString(PARENT_TASK_ID));
                    response
                        .setRunningTime(TimeValueUtil.parseTimeValue(js.getString(RUNNING_TIME), "task").getMillis());
                    response.setRunningTimeString(js.getString(RUNNING_TIME));
                    responses.add(response);
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterTaskStats||clusterName={}||errMsg=fail to get",
                clusterName, e);
            ParsingExceptionUtils.abnormalTermination(e);
        }

        return responses;
    }

    private void setRoleNumberToResponses(ESClusterStatsResponse responses, JSONObject nodesCountObj) {
        // role--client/data/master的数目初始化
        long clientNum, dataNum, masterNum;

        // 设置dataNumber和masterNumber，兼容2.3.3低版本的es集群
        if (nodesCountObj.get(ES_ROLE_MASTER_ONLY) != null) {
            // 低版本中存在master_only的key
            dataNum = nodesCountObj.getLongValue(ES_ROLE_DATA_ONLY) + nodesCountObj.getLongValue(ES_ROLE_MASTER_DATA);
            masterNum = nodesCountObj.getLongValue(ES_ROLE_MASTER_ONLY)
                        + nodesCountObj.getLongValue(ES_ROLE_MASTER_DATA);
            clientNum = nodesCountObj.getLongValue(ES_ROLE_CLIENT);
        } else {
            // 高版本的角色节点数目设置
            dataNum = nodesCountObj.getLongValue(ES_ROLE_DATA);
            masterNum = nodesCountObj.getLongValue(ES_ROLE_MASTER);
            clientNum = nodesCountObj.getLongValue(TOTAL) - dataNum - masterNum;
        }

        // 对于clientNumber的极端值处理，处理特殊情况, 单实例全部角色
        if (clientNum < 0) {
            clientNum = 0;
        }

        responses.setNumberClientNodes(clientNum);
        responses.setNumberDataNodes(dataNum);
        responses.setNumberMasterNodes(masterNum);
        responses.setNumberIngestNodes(nodesCountObj.getLongValue(ES_ROLE_INGEST));
        responses.setNumberCoordinatingOnly(nodesCountObj.getLongValue(ES_ROLE_COORDINATING_ONLY));
    }

    private ESClusterStatsResponse initESClusterStatsResponse() {
        ESClusterStatsResponse responses = new ESClusterStatsResponse();
        responses.setStatus(ClusterHealthEnum.UNKNOWN.getDesc());
        responses.setMemUsed(new ByteSizeValue(0));
        responses.setMemFree(new ByteSizeValue(0));
        responses.setMemTotal(new ByteSizeValue(0));
        responses.setTotalFs(new ByteSizeValue(0));
        responses.setFreeFs(new ByteSizeValue(0));

        return responses;
    }

    /**
     * 获取物理集群动态配置中的attr属性
     * @param clusterName 物理集群名称
     * @return 集群配置下的attributesInfo属性集合
     */
    public List<NodeAttrInfo> syncGetAllNodesAttributes(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=getClusterStats||clusterName={}||errMsg=esClient is null",
                clusterName);
            return null;
        }

        try {
            ESCatRequest esCatRequest = new ESCatRequest();
            esCatRequest.setUri("nodeattrs?h=node,attr,value&s=attr:desc");

            ESCatResponse esCatResponse = client.admin().cluster().execute(ESCatAction.INSTANCE, esCatRequest)
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return JSON.parseArray(esCatResponse.getResponse().toString(), NodeAttrInfo.class);
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=syncGetAllNodesAttributes||cluster={}||errMsg=attributes is null",
                clusterName, e);
        }

        return null;
    }

    /**
     * 获取物理集群线程池相关统计信息
     * @param cluster 物理集群的名称
     * @return 集群线程池ESClusterThreadStats 属性
     */
    public List<ESClusterThreadPO> syncGetThreadStatsByCluster(String cluster) {
        ESClient client = esOpClient.getESClient(cluster);
        if (Objects.isNull(client)) {
            LOGGER.error(
                "class=ESClusterDAO||method=syncGetThreadStatsByCluster||clusterName={}||errMsg=esClient is null",
                cluster);
            return null;
        }

        try {
            ESCatRequest esCatRequest = new ESCatRequest();
            esCatRequest.setUri("thread_pool");

            ESCatResponse esCatResponse = client.admin().cluster().execute(ESCatAction.INSTANCE, esCatRequest)
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            if (esCatResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(esCatResponse.getResponse().toString())) {
                return ConvertUtil.str2ObjArrayByJson(esCatResponse.getResponse().toString(), ESClusterThreadPO.class);
            }
        } catch (Exception e) {
            LOGGER.error(
                "class=ESClusterDAO||method=syncGetThreadStatsByCluster||cluster={}||errMsg=attributes is null",
                cluster, e);
        }
        return null;
    }

    public ESClusterHealthResponse getClusterHealthAtIndicesLevel(String physicalClusterName) {
        try {
            ESClient esClient = esOpClient.getESClient(physicalClusterName);
            ESClusterHealthRequest request = new ESClusterHealthRequest();
            return esClient.admin().cluster().health(request.setLevel(IndicesStatsLevel.INDICES))
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error(
                "class=ClusterClientPool||method=getClusterHealthAtIndicesLevel||clusterName={}||errMsg=query error. ",
                physicalClusterName, e);
        }
        return null;
    }

    public String pendingTask(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        String ecSegmentsOnIps = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=pendingTask||clusterName={}||errMsg=esClient is null",
                clusterName);
            return null;
        }
        try {
            DirectRequest directRequest = new DirectRequest(PENDING_TASK.getMethod(), PENDING_TASK.getUri());
            DirectResponse directResponse = client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                ecSegmentsOnIps = directResponse.getResponseContent();
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=pendingTask||cluster={}||mg=get es segments fail", clusterName, e);
            return null;
        }
        return ecSegmentsOnIps;
    }

    public String taskMission(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        String result = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=taskMission||clusterName={}||errMsg=esClient is null",
                clusterName);
            return null;
        }
        try {
            DirectRequest directRequest = new DirectRequest(TASK_MISSION_ANALYSIS.getMethod(),
                TASK_MISSION_ANALYSIS.getUri());
            DirectResponse directResponse = client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                result = directResponse.getResponseContent();
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=taskMission||cluster={}||mg=get es segments fail", clusterName, e);
            return null;
        }
        return result;
    }

    public String hotThread(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        String result = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=hotThread||clusterName={}||errMsg=esClient is null", clusterName);
            return null;
        }
        try {
            DirectRequest directRequest = new DirectRequest(HOT_THREAD.getMethod(), HOT_THREAD.getUri());
            DirectResponse directResponse = client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                result = directResponse.getResponseContent();
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=hotThread||cluster={}||mg=get es segments fail", clusterName, e);
            return null;
        }
        return result;
    }

    public String clearFieldDataMemory(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        String result = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=shardAssignment||clusterName={}||errMsg=esClient is null",
                clusterName);
            return null;
        }
        try {
            DirectRequest directRequest = new DirectRequest(CLEAR_FIELDDATA_MEMORY.getMethod(),
                CLEAR_FIELDDATA_MEMORY.getUri());
            DirectResponse directResponse = client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                result = directResponse.getResponseContent();
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=shardAssignment||cluster={}||mg=get es segments fail", clusterName,
                e);
            return null;
        }
        return result;
    }

    public String abnormalShardAllocationRetry(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        String result = null;
        if (Objects.isNull(client)) {
            LOGGER.error(
                "class=ESClusterDAO||method=abnormalShardAllocationRetry||clusterName={}||errMsg=esClient is null",
                clusterName);
            return null;
        }
        try {
            DirectRequest directRequest = new DirectRequest(ABNORMAL_SHARD_RETRY.getMethod(),
                ABNORMAL_SHARD_RETRY.getUri());
            DirectResponse directResponse = client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                result = directResponse.getResponseContent();
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=abnormalShardAllocationRetry||cluster={}||mg=get es segments fail",
                clusterName, e);
            return null;
        }
        return result;
    }

    /**
     * 这里获取所有节点的tcp地址，暂时不做角色区分
     * @param cluster
     * @return
     */
    public List<String> getNodeTcpAddress(String cluster) {
        ESClient client = esOpClient.getESClient(cluster);
        if (Objects.isNull(client)) {
            LOGGER.error(
                    "class=ESClusterDAO||method=abnormalShardAllocationRetry||clusterName={}||errMsg=esClient is null",
                    cluster);
            return new ArrayList<>();
        }
        ESClusterNodesStatsResponse nodesStatsResponse = client.admin().cluster().prepareNodeStats().execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        Map<String, ClusterNodeStats> nodes = nodesStatsResponse.getNodes();
        return nodes.values().stream().map(ClusterNodeStats::getTransportAddress).distinct().collect(Collectors.toList());
    }
    
    public boolean isConnectionStatus(String cluster) throws ESOperateException {
        final boolean actualRunning = esOpClient.isActualRunning(cluster);
        if (!actualRunning){
            throw new ESOperateException(String.format("无法连接到es client %s",cluster));
        }
    
        return  actualRunning;
    }
    
    /**
     *  检查目标集群是否连接到当前集群
     *
     * @param cluster 要操作的集群名称
     * @param targetCluster 目标集群的名称
     * @return boolean
     */
    public boolean checkTargetClusterConnected(String cluster, String targetCluster) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }
        DirectResponse directResponse = null;
        try {
            directResponse = getDirectResponse(cluster, "GET",
                    String.format(REMOTE_TARGET_CLUSTER, targetCluster, CONNECTED));
        } catch (Exception e) {
            LOGGER.error(
                    "class=ESClusterDAO||method=checkTargetClusterConnected||clusterName={}||errMsg=esClient is null",
                    cluster);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        Function<JSONObject,Boolean> jsonObjectFunc=jsonObject -> {
            //如果是空的，则直接为false
            if (jsonObject.isEmpty()){
                return false;
            }
            
           return jsonObject.getJSONObject(targetCluster).getBoolean(CONNECTED) || tryRemoteClusterCountIndicesAndIgnoreException(cluster
                   , targetCluster);
        };
        
        return Optional.ofNullable(directResponse).filter(d -> d.getRestStatus() == RestStatus.OK)
                .map(DirectResponse::getResponseContent)
                .map(JSON::parseObject)
                .map(jsonObjectFunc)
                
                .orElse(false);
    }
    
    /**
     * > 它尝试获取目标集群中的索引计数，只是为了进行主集群到从集群正常的健康性校验:
     * <pre>
     *     1. 无需关注底层异常，这是由于如果集群不通，那么会触发Gateway Time-out，
     *      那么也可以证明集群是不通的，所以无需关注
     *     2.如果远程集群_count报错后，我们无需理会它，它会自动为remote信息刷新，
     *     从而使得remote信息是刷新的，且由此可以证明集群是联通的
     *     3.如果远程集群报了connect_exception信息，则证明集群配置是错误的或者不是联通的，那么可以证明集群是不联通的
     * </pre>
     *
     * @param cluster 索引所属的集群的名称。
     * @param targetCluster 目标集群的名称。
     */
    private boolean tryRemoteClusterCountIndicesAndIgnoreException(String cluster, String targetCluster)  {
        DirectResponse directResponse;
        try {
            DirectRequest directRequest = new DirectRequest("GET",
                    String.format(REMOTE_TARGET_CLUSTER_COUNT, targetCluster));
            directResponse = esOpClient.getESClient(cluster).direct(directRequest).actionGet(3, TimeUnit.SECONDS);
        } catch (Exception exception) {
            final String messageByException = ParsingExceptionUtils.getESErrorMessageByException(
                    exception);
            if (StringUtils.equals(messageByException,ParsingExceptionUtils.CLUSTER_ERROR)){
                return false;
            }
            if (StringUtils.endsWith(messageByException, ParsingExceptionUtils.CONNECT_EXCEPTION)) {
                return false;
            }
            if (exception instanceof ElasticsearchTimeoutException) {
                return false;
            }
            return true;
        }
        return Optional.ofNullable(directResponse).map(d -> d.getRestStatus() == RestStatus.OK)
                .orElse(false);
        
    }
}