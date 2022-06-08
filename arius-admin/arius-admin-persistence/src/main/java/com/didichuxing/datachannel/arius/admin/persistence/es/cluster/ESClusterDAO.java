package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterNodeInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterStateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.IndexRouting;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ShardInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.common.NodeAllocationInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.common.NodeAttrInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESResponsePluginInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllAction;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ECSegmentsOnIps;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterTaskStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ESClusterThreadPO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
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
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ESClusterNodesResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ESClusterNodesSettingResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.updatesetting.ESClusterUpdateSettingsResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.didiglobal.logi.elasticsearch.client.utils.JsonUtils;
import com.google.common.collect.Lists;

/**
 * @author d06679
 */
@Repository
public class ESClusterDAO extends BaseESDAO {

    /**
     * 配置集群re balance开关
     * @param cluster 集群名称
     * @param value  all /  none
     * @return 成功 true   失败 false
     */
    public boolean configReBalanceOperate(String cluster, String value) {
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) { return false;}

        ESClusterUpdateSettingsResponse response = client.admin().cluster().prepareUpdateSettings()
            .addPersistent(REBALANCE, value).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    /**
     * 获取集群 persistent 配置
     * @param cluster 集群名称
     * @return map<flat_setting_name, setting_value>
     */
    public Map<String, Object> getPersistentClusterSettings(String cluster) {
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) { return null;}

        ESClusterGetSettingsRequest request = new ESClusterGetSettingsRequest();
        ESClusterGetSettingsResponse response = client.admin().cluster().getSetting(request)
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return JsonUtils.flatObject(response.getPersistentObj());
    }

    /**
     * 获取集群配置
     * @param cluster 集群名称
     * @return response
     */
    public ESClusterGetSettingsAllResponse getClusterSetting(String cluster) {
        ESClusterGetSettingsAllResponse response = null;
        ESClient client = esOpClient.getESClient(cluster);
        if (null != client) {
            response = client.admin().cluster()
                .execute(ESClusterGetSettingsAllAction.INSTANCE, new ESClusterGetSettingsAllRequest())
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
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
    public boolean putPersistentRemoteClusters(String cluster, String remoteCluster, List<String> tcpAddresses) {
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) { return false;}

        JSONArray addresses = new JSONArray();
        addresses.addAll(tcpAddresses);

        ESClusterUpdateSettingsResponse response = client.admin().cluster().prepareUpdateSettings()
            .addPersistent(remoteCluster, addresses).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    /**
     * put-setting
     * @param cluster 集群
     * @param configMap 配置
     * @return true/false
     */
    public boolean putPersistentConfig(String cluster, Map<String, Object> configMap) {
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) { return false;}

        ESClusterUpdateSettingsRequestBuilder updateSettingsRequestBuilder = client.admin().cluster()
            .prepareUpdateSettings();

        for(Map.Entry<String, Object> entry : configMap.entrySet()){
            String configName = entry.getKey();
            updateSettingsRequestBuilder.addPersistent(configName, configMap.get(configName));
        }

        ESClusterUpdateSettingsResponse response = client.admin().cluster()
            .updateSetting(updateSettingsRequestBuilder.request()).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    /**
     * 获取物理集群下各个节点的插件名称列表
     *
     * @param cluster
     * @return map
     */
    public Map<String/*nodeName*/, List<String>/*pluginName*/> getNode2PluginsMap(String cluster) {
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) { return null;}

        ESCatRequest esCatRequest = new ESCatRequest();
        esCatRequest.setUri("plugins");

        ESCatResponse esCatResponse = client.admin().cluster().execute(ESCatAction.INSTANCE, esCatRequest)
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        List<ESResponsePluginInfo> esResponsePluginInfos = JSON.parseArray(esCatResponse.getResponse().toString(),
            ESResponsePluginInfo.class);
        return ConvertUtil.list2MapOfList(esResponsePluginInfos, ESResponsePluginInfo::getName,
            ESResponsePluginInfo::getComponent);
    }

    /**
     * 获取物理集群下各个节点的资源设置信息
     * @param cluster 物理集群名称
     * @return 集群下的节点资源使用信息列表
     */
    public List<NodeAllocationInfo> getNodeAllocationInfoByCluster(String cluster) {
        ESClient esClient = esOpClient.getESClient(cluster);
        if (esClient == null) {
            LOGGER.warn("class=ESClusterDAO||method=getNodeAllocationInfoByCluster||clusterName={}" +
                    "||errMsg=client is null", cluster);
            return null;
        }

        ESCatRequest esCatRequest = new ESCatRequest();
        esCatRequest.setUri("allocation");

        try {
            ESCatResponse esCatResponse = esClient.admin().cluster().execute(ESCatAction.INSTANCE, esCatRequest)
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return JSON.parseArray(esCatResponse.getResponse().toString(), NodeAllocationInfo.class);
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=getNodeAllocationInfoByCluster||clusterName={}" +
                    "||errMsg=can't get allocation info", cluster);
            return null;
        }
    }

    /**
     * 获取集群中索引的别名信息
     * @param cluster
     * @return
     */
    public ESIndicesGetAliasResponse getClusterAlias(String cluster) {
        ESClient client = esOpClient.getESClient(cluster);
        if  (client == null) {
            return null;
        }
        try {
            return client.admin().indices().prepareAlias().execute().actionGet(ES_OPERATE_TIMEOUT,
                    TimeUnit.MINUTES);

        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterAlias||clusterName={}||errMsg=query error. ", cluster,
                    e);
            return null;
        }
    }

    /**
     * 获取集群状态信息
     *
     * @param cluster
     * @return
     */
    public ESClusterHealthResponse getClusterHealth(String cluster) {
        ESClient esClient = esOpClient.getESClient(cluster);
        if (esClient == null) {
            LOGGER.error("class=ESClusterDAO||method=getClusterHealth||clusterName={}||errMsg=esClient is null",
                cluster);
            return null;
        }

        try {
            ESClusterHealthRequest request = new ESClusterHealthRequest();
            return esClient.admin().cluster().health(request).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterHealth||clusterName={}||errMsg=query error. ", cluster,
                e);
            return null;
        }
    }

    /**
     * 获取部分ES集群nodeSetting
     * @param cluster 集群名称
     * @return client原生对象列表
     */
    public Map<String, ClusterNodeSettings> getPartOfSettingsByCluster(String cluster) {
        try {
            ESClient client = esOpClient.getESClient(cluster);
            if (null == client) { return null;}
            ESClusterNodesSettingResponse response = client.admin().cluster().prepareNodesSetting().execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getNodes();
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=getPartOfSettingsByCluster||cluster={}||mg=get es setting fail", cluster, e);
            return null;
        }
    }

    /**
     * 获取全量ES集群nodeSetting
     * @param cluster
     * @return
     */
    public Map<String, ClusterNodeInfo> getAllSettingsByCluster(String cluster) {
        try {
            ESClient client = esOpClient.getESClient(cluster);
            if (null == client) {
                LOGGER.warn("class=ESClusterDAO||method=getAllSettingsByCluster||cluster={}||mg=ESClient is empty", cluster);
                return null;
            }

            ESClusterNodesResponse response = client.admin().cluster().prepareNodes().execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getNodes();
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=getAllSettingsByCluster||cluster={}||mg=get es setting fail", cluster, e);
            return null;
        }
    }

    public String getESVersionByCluster(String cluster) {
        ESClient client = esOpClient.getESClient(cluster);
        String esVersion = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=getESVersionByCluster||clusterName={}||errMsg=esClient is null", cluster);
            return null;
        }
        try {
            DirectRequest directRequest = new DirectRequest("GET", "");
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                    && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                esVersion = (String) JSONObject.parseObject(directResponse.getResponseContent()).getJSONObject("version").get("number");
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=getESVersionByCluster||cluster={}||mg=get es segments fail", cluster, e);
            return null;
        }
        return esVersion;
    }

    /**
     * 获取集群节点ip下的segment数目
     *
     * @param clusterName 集群名称
     * @return
     */
    public List<ECSegmentsOnIps> getSegmentsOfIpByCluster(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        List<ECSegmentsOnIps> ecSegmentsOnIps = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=getClusterStats||clusterName={}||errMsg=esClient is null", clusterName);
            return new ArrayList<>();
        }
        try {
            DirectRequest directRequest = new DirectRequest("GET", "_cat/nodes?v&h=sc,ip&format=json");
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                    && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                ecSegmentsOnIps = JSONArray.parseArray(directResponse.getResponseContent(), ECSegmentsOnIps.class);
                }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=getSegmentsOfIpByCluster||cluster={}||mg=get es segments fail", clusterName, e);
            return new ArrayList<>();
        }
        return ecSegmentsOnIps;
    }


    public ESClusterStatsResponse getClusterStats(String clusterName) {
        ESClusterStatsResponse responses = initESClusterStatsResponse();
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (Objects.isNull(esClient)) {
            LOGGER.error("class=ESClusterDAO||method=getClusterStats||clusterName={}||errMsg=esClient is null", clusterName);
            return responses;
        }

        try {
            DirectRequest directRequest = new DirectRequest("GET", "_cluster/stats");
            DirectResponse directResponse = esClient.direct(directRequest).actionGet(30, TimeUnit.SECONDS);

            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {

                JSONObject jsonObject = JSON.parseObject(directResponse.getResponseContent());
                responses.setStatus(jsonObject.getObject(STATUS, String.class));

                JSONObject indicesObj = jsonObject.getJSONObject(INDICES);
                responses.setIndexCount(indicesObj.getLongValue(COUNT));

                JSONObject shardsObj = indicesObj.getJSONObject(SHARDS);
                responses.setTotalShard(shardsObj.getLongValue(TOTAL));

                JSONObject docsObj = indicesObj.getJSONObject(DOCS);
                responses.setDocsCount(docsObj.getLongValue(COUNT));

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
            LOGGER.error("class=ESClusterDAO||method=getClusterStats||clusterName={}||errMsg=fail to get",
                clusterName, e);
        }

        return responses;
    }

    public ESClusterStateResponse getClusterState(String cluster) {
        ESClient esClient = esOpClient.getESClient(cluster);
        if (null == esClient) {
            LOGGER.error("class=ESClusterDAO||method=getClusterState||clusterName={}||errMsg=esClient is null", cluster);
            return null;
        }

        try {
            DirectRequest directRequest = new DirectRequest("GET", "_cluster/state/nodes,routing_table");
            DirectResponse directResponse = esClient.direct(directRequest).actionGet(30, TimeUnit.SECONDS);

            if (RestStatus.OK != directResponse.getRestStatus() || StringUtils.isBlank(directResponse.getResponseContent())) {
                LOGGER.error("class=ESClusterDAO||method=getClusterState||clusterName={}||errMsg=get response empty", cluster);
                return null;
            }

            JSONObject jsonObject = JSON.parseObject(directResponse.getResponseContent());

            List<ClusterNodeInfo> nodes = new ArrayList<>();
            JSONObject nodesObj = jsonObject.getJSONObject("nodes");
            for (String nodeName : nodesObj.keySet()) {
                JSONObject node = nodesObj.getJSONObject(nodeName);
                if (null != node) {
                    nodes.add(new ClusterNodeInfo(node.getString("name"), nodeName));
                }
            }

            List<IndexRouting> indicesRouting = new ArrayList<>();
            JSONObject indicesRoutingObj = jsonObject.getJSONObject("routing_table").getJSONObject("indices");
            for (String index : indicesRoutingObj.keySet()) {
                List<ShardInfo> fullShards = new ArrayList<>();
                IndexRouting indexRouting = new IndexRouting(index, fullShards);
                JSONObject shardsObj = indicesRoutingObj.getJSONObject(index).getJSONObject("shards");
                for (String shardGroup : shardsObj.keySet()) {
                    List<ShardInfo> shards = JSONObject.parseArray(shardsObj.getJSONArray(shardGroup).toJSONString(), ShardInfo.class);
                    if (CollectionUtils.isNotEmpty(shards)) {
                        fullShards.addAll(shards);
                    }
                }
                indicesRouting.add(indexRouting);
            }

            return new ESClusterStateResponse(nodes, indicesRouting);
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterState||clusterName={}||errMsg=fail to get", cluster, e);
            return null;
        }
    }

    public List<ESClusterTaskStatsResponse> getClusterTaskStats(String clusterName) {
        List<ESClusterTaskStatsResponse> responses = Lists.newArrayList();
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (null == esClient) { return responses;}

        try {
            DirectRequest taskStatsRequest = new DirectRequest("GET", "_cat/tasks?v&detailed&format=json");
            DirectResponse directResponse = esClient.direct(taskStatsRequest).actionGet(30, TimeUnit.SECONDS);
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
                    response.setRunningTime(TimeValueUtil.parseTimeValue(js.getString(RUNNING_TIME), "task").getMillis());
                    response.setRunningTimeString(js.getString(RUNNING_TIME));
                    responses.add(response);
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterTaskStats||clusterName={}||errMsg=fail to get",
                    clusterName, e);
        }

        return responses;
    }

    private void setRoleNumberToResponses(ESClusterStatsResponse responses, JSONObject nodesCountObj) {
        // role--client/data/master的数目初始化
        long clientNum, dataNum, masterNum;

        // 设置dataNumber和masterNumber，兼容2.3.3低版本的es集群
        if (nodesCountObj.get(ES_ROLE_MASTER_ONLY) != null) {
            // 低版本中存在master_only的key
            dataNum   = nodesCountObj.getLongValue(ES_ROLE_DATA_ONLY) + nodesCountObj.getLongValue(ES_ROLE_MASTER_DATA);
            masterNum = nodesCountObj.getLongValue(ES_ROLE_MASTER_ONLY) + nodesCountObj.getLongValue(ES_ROLE_MASTER_DATA);
            clientNum = nodesCountObj.getLongValue(ES_ROLE_CLIENT);
        } else {
            // 高版本的角色节点数目设置
            dataNum   = nodesCountObj.getLongValue(ES_ROLE_DATA);
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
            LOGGER.error("class=ESClusterDAO||method=getClusterStats||clusterName={}||errMsg=esClient is null", clusterName);
            return null;
        }

        try {
            ESCatRequest esCatRequest = new ESCatRequest();
            esCatRequest.setUri("nodeattrs?h=node,attr,value&s=attr:desc");

            ESCatResponse esCatResponse = client.admin().cluster().execute(ESCatAction.INSTANCE, esCatRequest)
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return JSON.parseArray(esCatResponse.getResponse().toString(), NodeAttrInfo.class);
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=syncGetAllNodesAttributes||cluster={}||errMsg=attributes is null", clusterName, e);
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
            LOGGER.error("class=ESClusterDAO||method=syncGetThreadStatsByCluster||clusterName={}||errMsg=esClient is null", cluster);
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
            LOGGER.error("class=ESClusterDAO||method=syncGetThreadStatsByCluster||cluster={}||errMsg=attributes is null", cluster, e);
        }
        return null;
    }

    public ESClusterHealthResponse getClusterHealthAtIndicesLevel(String physicalClusterName) {
        try {
            ESClient esClient = esOpClient.getESClient(physicalClusterName);
            ESClusterHealthRequest request = new ESClusterHealthRequest();
            return esClient.admin().cluster().health(request.setLevel(IndicesStatsLevel.INDICES)).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class=ClusterClientPool||method=getClusterHealthAtIndicesLevel||clusterName={}||errMsg=query error. ", physicalClusterName, e);
        }
        return null;
    }
}