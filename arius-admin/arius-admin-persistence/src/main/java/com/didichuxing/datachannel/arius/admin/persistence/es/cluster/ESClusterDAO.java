package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESResponsePluginInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllAction;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ECSegmentsOnIps;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.elasticsearch.client.request.cat.ESCatAction;
import com.didiglobal.logi.elasticsearch.client.request.cat.ESCatRequest;
import com.didiglobal.logi.elasticsearch.client.request.cluster.getsetting.ESClusterGetSettingsRequest;
import com.didiglobal.logi.elasticsearch.client.request.cluster.health.ESClusterHealthRequest;
import com.didiglobal.logi.elasticsearch.client.request.cluster.updatesetting.ESClusterUpdateSettingsRequestBuilder;
import com.didiglobal.logi.elasticsearch.client.response.cat.ESCatResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.getsetting.ESClusterGetSettingsResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ESClusterNodesResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ESClusterNodesSettingResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.updatesetting.ESClusterUpdateSettingsResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.didiglobal.logi.elasticsearch.client.utils.JsonUtils;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.RED;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.*;

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
                
                responses.setNumberIngestNodes(nodesCountObj.getLongValue(ES_ROLE_INGEST));
                responses.setNumberMasterNodes(nodesCountObj.getLongValue(ES_ROLE_MASTER));
                responses.setNumberDataNodes(nodesCountObj.getLongValue(ES_ROLE_DATA));
                responses.setNumberCoordinatingOnly(nodesCountObj.getLongValue(ES_ROLE_COORDINATING_ONLY));

                long clientNum = nodesCountObj.getLongValue(TOTAL) - nodesCountObj.getLongValue(ES_ROLE_MASTER)
                                 - nodesCountObj.getLongValue(ES_ROLE_DATA);
                //处理特殊情况, 单实例全部角色
                if (clientNum < 0) { clientNum = 0;}
                responses.setNumberClientNodes(clientNum);

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
            }

        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterStats||clusterName={}||errMsg=fail to get",
                clusterName, e);
        }

        return responses;
    }

    private ESClusterStatsResponse initESClusterStatsResponse() {
        ESClusterStatsResponse responses = new ESClusterStatsResponse();
        responses.setStatus(RED.getDesc());
        responses.setMemUsed(new ByteSizeValue(0));
        responses.setMemFree(new ByteSizeValue(0));
        responses.setMemTotal(new ByteSizeValue(0));
        responses.setTotalFs(new ByteSizeValue(0));
        responses.setFreeFs(new ByteSizeValue(0));

        return responses;
    }

    /**
     * 获取物理集群动态配置中的attributes属性
     * @param clusterName 物理集群名称
     * @return 集群配置下的attributes属性集合
     */

    public Set<String> syncGetAllNodesAttributes(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=getClusterStats||clusterName={}||errMsg=esClient is null", clusterName);
            return new HashSet<>();
        }

        Set<String> allNodesAttributes = Sets.newHashSet();
        try {
            DirectRequest directRequest = new DirectRequest("GET", "_cat/nodeattrs?v&h=attr&format=json");
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                    && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                List<JSONObject> attributeObjects = JSONArray.parseArray(directResponse.getResponseContent(), JSONObject.class);
                if (!CollectionUtils.isEmpty(attributeObjects)) {
                    // 对于attributes属性进行去重操作
                    attributeObjects.forEach(attribute -> allNodesAttributes.add(attribute.get("attr").toString()));
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=syncGetAllNodesAttributes||cluster={}||errMsg=attributes is null", clusterName, e);
        }

        return allNodesAttributes;

    }
}