package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.model.type.ESVersion;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.index.getindex.ESIndicesGetIndexRequest;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.index.stats.ESIndicesStatsRequestBuilder;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.index.stats.IndicesStatsLevel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.model.Client;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.catindices.ESIndicesCatIndicesResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.closeindex.ESIndicesCloseIndexResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.deletebyquery.ESIndicesDeleteByQueryResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.deleteindex.ESIndicesDeleteIndexResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.getalias.AliasIndexNode;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.getindex.ESIndicesGetIndexResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.openindex.ESIndicesOpenIndexResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.putindex.ESIndicesPutIndexResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.refreshindex.ESIndicesRefreshIndexResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.updatemapping.ESIndicesUpdateMappingResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.updatesettings.ESIndicesUpdateSettingsResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.common.MappingConfig;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.common.TypeConfig;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.index.IndexConfig;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public class ESIndexDAO extends BaseESDAO {

    private static final ILog LOGGER = LogFactory.getLog(ESIndexDAO.class);

    /**
     * 创建索引
     * @param cluster 集群
     * @param indexName 索引名字
     * @return result
     */
    public boolean createIndex(String cluster, String indexName) {
        if (exist(cluster, indexName)) {
            LOGGER.warn("index already exist||cluster={}||indexName={}", cluster, indexName);
            return true;
        }

        ESClient client = fetchESClientByCluster(cluster);
        if (client != null) {
            ESIndicesPutIndexResponse response = client.admin().indices().preparePutIndex(indexName).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getAcknowledged();
        } else {
            return false;
        }
    }

    /**
     * 通过集群名称获取对应的ES Client
     * @param clusterName 集群名称
     * @return
     */
    private ESClient fetchESClientByCluster(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        if (client == null) {
            LOGGER.warn("cannot get es client||cluster={}", clusterName);
        }
        return client;
    }

    /**
     * 按着指定的配置创建索引
     * @param cluster 集群
     * @param indexName 索引
     * @param indexConfig 索引配置
     * @return
     */
    public boolean createIndexWithConfig(String cluster, String indexName, IndexConfig indexConfig) {
        if (exist(cluster, indexName)) {
            LOGGER.warn("index already exist||cluster={}||indexName={}", cluster, indexName);
            return true;
        }
        ESClient client = fetchESClientByCluster(cluster);
        if (client != null) {
            indexConfig.setVersion(ESVersion.valueBy(client.getEsVersion()));
            ESIndicesPutIndexResponse response = client.admin().indices().preparePutIndex(indexName)
                    .setIndexConfig(indexConfig).execute().actionGet(5, TimeUnit.MINUTES);
            return response.getAcknowledged();
        } else {
            return false;
        }
    }

    /**
     * 索引是否已存在
     * @param cluster
     * @return
     */
    public boolean exist(String cluster, String expression) {
        Client client = fetchESClientByCluster(cluster);
        if (client != null) {
            return client.admin().indices().prepareExists(expression).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS).isExists();
        }
        return false;
    }

    /**
     * 批量获取索引配置
     * @param cluster 集群
     * @param indexNames 索引名字
     * @return 配置
     */
    public MultiIndexsConfig batchGetIndexConfig(String cluster, List<String> indexNames) {
        ESClient client = esOpClient.getESClient(cluster);

        if (client != null) {
            String indexExpression = String.join(",", indexNames);
            ESIndicesGetIndexResponse getIndexResponse = client.admin().indices().prepareGetIndex(indexExpression).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return getIndexResponse.getIndexsMapping();
        } else {
            return null;
        }
    }

    /**
     * 获取索引的mapping信息
     * @param cluster 集群
     * @param indexName 索引名字
     * @return
     */
    public MappingConfig getIndexMapping(String cluster, String indexName) {
        if (!exist(cluster, indexName)) {
            LOGGER.warn("index not exist||cluster={}||indexName={}", cluster, indexName);
            return null;
        }

        ESClient client = esOpClient.getESClient(cluster);
        ESIndicesGetIndexResponse response = client.admin().indices().prepareGetIndex(indexName).execute()
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getIndexsMapping().getIndexConfig(indexName).getMappings();
    }

    /**
     * 更新索引的mapping信息
     * @param cluster 集群
     * @param indexName 索引名字
     * @return
     */
    public boolean updateIndexMapping(String cluster, String indexName, MappingConfig mappingConfig) {
        if (!exist(cluster, indexName)) {
            LOGGER.warn("index not exist||cluster={}||indexName={}", cluster, indexName);
            return true;
        }

        ESClient client = esOpClient.getESClient(cluster);
        Map<String, TypeConfig> typeConfigMap = mappingConfig.getMapping();
        if (typeConfigMap == null) {
            return false;
        }

        for (String typeName : typeConfigMap.keySet()) {
            ESIndicesUpdateMappingResponse response = client.admin().indices().prepareUpdateMapping()
                .setIndex(indexName).setType(typeName).setTypeConfig(typeConfigMap.get(typeName)).execute()
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

            if (!response.getAcknowledged()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 删除索引
     * @param cluster 集群
     * @param indexName 索引名字
     * @return
     */
    public boolean deleteIndex(String cluster, String indexName) {
        if (!exist(cluster, indexName)) {
            LOGGER.warn("index not exist||cluster={}||indexName={}", cluster, indexName);
            return true;
        }


        ESClient client = esOpClient.getESClient(cluster);
        ESIndicesDeleteIndexResponse response = client.admin().indices().prepareDeleteIndex(indexName).execute()
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        return response.getAcknowledged();
    }

    /**
     * 根据索引模板名称获取物理集群所有分区索引
     * @param cluster 集群名称
     * @param expression 索引模板表达式
     * @return
     */
    public List<CatIndexResult> catIndexByExpression(String cluster, String expression) {
        List<CatIndexResult> indices = new ArrayList<>();

        try {
            ESClient client = fetchESClientByCluster(cluster);
            if (client != null) {
                ESIndicesCatIndicesResponse response = client.admin().indices().prepareCatIndices(expression).execute()
                        .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
                indices.addAll(response.getCatIndexResults());
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESIndexDAO||method=getIndexByExpression||errMsg={}||cluster={}||expression={}",
                    e.getMessage(), cluster, expression, e);
        }

        return indices;
    }

    /**
     * 获取指定集群,指定表达式的索引
     * @param cluster
     * @param expression
     * @return
     */
    public Map<String, IndexNodes> getIndexByExpression(String cluster, String expression) {
        try {
            ESClient client = fetchESClientByCluster(cluster);
            if (client == null) {
                return null;
            }

            ESIndicesStatsResponse response = client.admin().indices().prepareStats(expression).execute()
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getIndicesMap();
        } catch (Exception e) {
            LOGGER.warn("class=ESIndexDAO||method=getIndexByExpression||errMsg={}||cluster={}||expression={}",
                e.getMessage(), cluster, expression, e);
            return null;
        }
    }

    /**
     * 获取指定集群,指定表达式的索引指标，附带全部shard的信息
     * @param cluster
     * @param expression
     * @return
     */
    public Map<String, IndexNodes> getIndexStatsWithShards(String cluster, String expression) {
        try {
            ESClient client = fetchESClientByCluster(cluster);
            if (client == null) {
                return null;
            }

            ESIndicesStatsResponse response = client.admin().indices().prepareStats(expression).setLevel(IndicesStatsLevel.SHARDS).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getIndicesMap();
        } catch (Exception e) {
            LOGGER.warn("class=ESIndexDAO||method=getIndexByExpression||errMsg={}||cluster={}||expression={}",
                    e.getMessage(), cluster, expression, e);
            return null;
        }
    }

    /**
     * 获取指定集群,指定表达式的别名
     * @param cluster
     * @param expression
     * @return
     */
    public Map<String/*index*/, AliasIndexNode> getAliasesByExpression(String cluster, String expression) {
        try {
            ESClient client = esOpClient.getESClient(cluster);
            ESIndicesGetAliasResponse response = client.admin().indices().prepareAlias(expression).execute()
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getM();
        } catch (Exception e) {
            LOGGER.warn("class=ESIndexDAO||method=getAliasesByExpression||errMsg={}||cluster={}||expression={}",
                cluster, e.getMessage(), expression, e);
            return null;
        }
    }

    /**
     * 删除文档
     * @param cluster 集群
     * @param delIndices 索引
     * @param delQueryDsl 语句
     * @return true/false
     */
    public boolean deleteByQuery(String cluster, String delIndices, String delQueryDsl) throws ESOperateException {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            throw new ESOperateException("cannot fetch es client for cluster: " + cluster);
        }

        ESIndicesDeleteByQueryResponse response = client.admin().indices().prepareDeleteByQuery().setIndex(delIndices)
            .setQuery(delQueryDsl).setHighES(true).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        if (response.getRoot().containsKey("error")) {
            throw new ESOperateException(response.getRoot().get("error").toString());
        }

        return true;
    }

    /**
     * 修改索引的setting 幂等
     * @param cluster 集群
     * @param indices indices
     * @param tgtRack tgtRack
     * @return true/false
     */
    public boolean batchUpdateIndexRack(String cluster, List<String> indices, String tgtRack) {
        return putIndexSetting(cluster, indices, INDEX_INCLUDE_RACK, tgtRack, "");
    }

    /**
     * 设置索引只读属性
     * @param cluster 集群
     * @param indexNames 索引列表
     * @param block 配置
     * @return result
     */
    public boolean blockIndexWrite(String cluster, List<String> indexNames, boolean block) {
        return putIndexSetting(cluster, indexNames, INDEX_BLOCKS_WRITE, String.valueOf(block), "false");
    }

    /**
     * refresh索引
     * @param cluster 集群
     * @param indexNames 索引列表
     * @return result
     */
    public boolean refreshIndex(String cluster, List<String> indexNames) {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            return false;
        }

        ESIndicesRefreshIndexResponse response = client.admin().indices()
            .prepareRefreshIndex(String.join(",", indexNames)).execute()
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getFaild() == 0;
    }

    /**
     * 修改索引配置
     * @param cluster 集群
     * @param indices 索引
     * @param settingName 配置名称
     * @param setting 配置
     * @param defaultValue 配置默认值
     * @return true/false
     */
    public boolean putIndexSetting(String cluster, List<String> indices, String settingName, String setting,
                                   String defaultValue) {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            return false;
        }

        MultiIndexsConfig multiIndexsConfig = batchGetIndexConfig(cluster, indices);

        List<String> needOps = Lists.newArrayList();
        for (String indexName : indices) {
            IndexConfig indexConfig = multiIndexsConfig.getIndexConfig(indexName);
            if (indexConfig == null) {
                continue;
            }

            //由于客户端在put-setting的时候会默认加上"index."的前缀 所以这里需要这样搞
            Map<String, String> config = indexConfig.getSettings();
            String src = config.get("index." + settingName);
            if (src == null) {
                src = defaultValue;
            }
            if (src.equals(String.valueOf(setting))) {
                continue;
            }
            needOps.add(indexName);
        }

        if (CollectionUtils.isEmpty(needOps)) {
            return true;
        }

        ESIndicesUpdateSettingsResponse updateSettingsResponse = client.admin().indices()
            .prepareUpdateSettings(String.join(",", needOps)).addSettings(settingName, String.valueOf(setting))
            .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        return updateSettingsResponse.getAcknowledged();
    }

    /**
     * 关闭索引
     * @param cluster 集群
     * @param indices 索引
     * @return result
     */
    public boolean closeIndex(String cluster, List<String> indices) {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            return false;
        }
        ESIndicesCloseIndexResponse response = client.admin().indices().prepareCloseIndex(String.join(",", indices))
            .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        return response.getAcknowledged();
    }

    /**
     * 打开索引
     * @param cluster 集群
     * @param indices 索引
     * @return result
     */
    public boolean openIndex(String cluster, List<String> indices) {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            return false;
        }
        ESIndicesOpenIndexResponse response = client.admin().indices().prepareOpenIndex(String.join(",", indices))
            .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        return response.getAcknowledged();
    }

    /**
     * 获取模板信息
     * @param clusterName       集群名
     * @param indexName         索引名
     * @return result
     */
    public MultiIndexsConfig getIndexConfigs(String clusterName, String indexName) {
        ESClient esClient = esOpClient.getESClient(clusterName);

        ESIndicesGetIndexRequest request = new ESIndicesGetIndexRequest();
        request.setIndices(indexName);

        ESIndicesGetIndexResponse response = null;
        try {
            response = esClient.admin().indices().getIndex(request).actionGet(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("get index fail || clusterName={}||indexName={}|| msg={}", clusterName, indexName,
                    e.getMessage(), e);
        }

        if (response == null) {
            return null;
        }

        if (!EnvUtil.isOnline()) {
            LOGGER.warn("class=ESTemplateDAO||method=getIndexConfigs||response={}", JSON.toJSONString(response));
        }

        return response.getIndexsMapping();
    }

    /**
     * 获取索引IndexNodes信息
     *
     * @param clusterName
     * @return
     */
    @Nullable
    public Map<String, IndexNodes> getIndexNodes(String clusterName, String templateExp) {
        ESClient esClient = esOpClient.getESClient(clusterName);

        if (esClient == null) {
            LOGGER.error("class=ClusterClientPool||method=getIndexNodes||clusterName={}||errMsg=esClient is null",
                    clusterName);
            return null;
        }

        ESIndicesStatsResponse response = null;
        ESIndicesStatsRequestBuilder builder = null;
        try {
            if (StringUtils.isBlank(templateExp)) {
                builder = esClient.admin().indices().prepareStats();
            } else {
                builder = esClient.admin().indices().prepareStats(templateExp);
            }
            response = builder.setStore(true).setDocs(true).execute().actionGet(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class=ClusterClientPool||method=getIndexNodes||clusterName={}||errMsg=get {} index stats error. ",
                    clusterName, templateExp, e);
        }

        if (response == null) {
            return null;
        }

        return response.getIndicesMap();
    }
}
