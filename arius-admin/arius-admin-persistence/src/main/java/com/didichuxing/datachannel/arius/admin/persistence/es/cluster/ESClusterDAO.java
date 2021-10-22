package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.ES_OPERATE_TIMEOUT;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.REBALANCE;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterStatusEnum;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.cluster.health.ESClusterHealthRequest;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONArray;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.cluster.getsetting.ESClusterGetSettingsRequest;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.cluster.updatesetting.ESClusterUpdateSettingsRequestBuilder;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.getsetting.ESClusterGetSettingsResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.updatesetting.ESClusterUpdateSettingsResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.utils.JsonUtils;
import com.google.common.collect.Sets;

/**
 * @author d06679
 */
@Repository
public class ESClusterDAO extends BaseESDAO {

    @Autowired
    private ESClusterNodeDAO esClusterNodeDAO;

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
     * 返回顺序的集群rack集合
     * @param cluster 集群名称
     * @return rackSet
     */
    public Set<String> getRackSet(String cluster) {
        Set<String> rackSet = Sets.newTreeSet(RackUtils::compareByName);
        for (Map.Entry<String, ClusterNodeSettings> entry : esClusterNodeDAO.getSettingsByCluster(cluster).entrySet()) {
            String rack = entry.getValue().getAttributes().getRack();
            if (StringUtils.isNotBlank(rack)) {
                rackSet.add(rack);
            }
        }
        return rackSet;
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

        for (String configName : configMap.keySet()) {
            updateSettingsRequestBuilder.addPersistent(configName, configMap.get(configName));
        }

        ESClusterUpdateSettingsResponse response = client.admin().cluster()
            .updateSetting(updateSettingsRequestBuilder.request()).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    /**
     * 获取集群的状态
     * @param cluster
     * @return
     */
    public ClusterStatusEnum getClusterStatus(String cluster) {
        ESClusterHealthResponse healthResponse = esOpClient.getESClient(cluster).admin().cluster().prepareHealth()
                .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        if (null == healthResponse) {
            return ClusterStatusEnum.UNKNOWN;
        }

        return ClusterStatusEnum.valuesOf(healthResponse.getStatus());
    }

    /**
     * 获取集群中索引的别名信息
     * @param cluster
     * @return
     */
    public ESIndicesGetAliasResponse getClusterAlias(String cluster){
        ESClient client = esOpClient.getESClient(cluster);

        ESIndicesGetAliasResponse response = client.admin().indices().prepareAlias().execute().actionGet(5, TimeUnit.MINUTES);

        return response;
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
            return esClient.admin().cluster().health(request).actionGet(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=getClusterHealth||clusterName={}||errMsg=query error. ",
                    cluster, e);
            return null;
        }
    }
}