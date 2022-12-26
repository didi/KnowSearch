package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.COMMA;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.didichuxing.datachannel.arius.admin.common.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.setting.ESIndicesGetAllSettingRequest;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NullESClientException;
import com.didichuxing.datachannel.arius.admin.common.function.BiFunctionWithESOperateException;
import com.didichuxing.datachannel.arius.admin.common.function.FunctionWithESOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.knowframework.elasticsearch.client.ESClient;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.knowframework.elasticsearch.client.model.Client;
import com.didiglobal.knowframework.elasticsearch.client.model.exception.ESAlreadyExistsException;
import com.didiglobal.knowframework.elasticsearch.client.model.exception.ESIndexNotFoundException;
import com.didiglobal.knowframework.elasticsearch.client.model.type.ESVersion;
import com.didiglobal.knowframework.elasticsearch.client.request.index.exists.ESIndicesExistsRequest;
import com.didiglobal.knowframework.elasticsearch.client.request.index.getindex.ESIndicesGetIndexRequest;
import com.didiglobal.knowframework.elasticsearch.client.request.index.putalias.PutAliasNode;
import com.didiglobal.knowframework.elasticsearch.client.request.index.stats.ESIndicesStatsRequestBuilder;
import com.didiglobal.knowframework.elasticsearch.client.request.index.stats.IndicesStatsLevel;
import com.didiglobal.knowframework.elasticsearch.client.request.index.updatemapping.ESIndicesUpdateMappingRequestBuilder;
import com.didiglobal.knowframework.elasticsearch.client.request.index.updatesettings.ESIndicesUpdateSettingsRequestBuilder;
import com.didiglobal.knowframework.elasticsearch.client.response.ESAcknowledgedResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.catindices.ESIndicesCatIndicesResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.closeindex.ESIndicesCloseIndexResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.deletebyquery.ESIndicesDeleteByQueryResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.deleteindex.ESIndicesDeleteIndexResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.exists.ESIndicesExistsResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.getalias.AliasIndexNode;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.getindex.ESIndicesGetIndexResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.openindex.ESIndicesOpenIndexResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.putalias.ESIndicesPutAliasResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.putindex.ESIndicesPutIndexResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.refreshindex.ESIndicesRefreshIndexResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.updatemapping.ESIndicesUpdateMappingResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.updatesettings.ESIndicesUpdateSettingsResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.TypeConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public class ESIndexDAO extends BaseESDAO {
    
    public static final String FAILED_MSG = "%s 执行失败, 请检查参数与索引配置";
    public static final String MAX_NUM_SEGMENTS = "max_num_segments";
    public static final String ONLY_EXPUNGE_DELETES = "only_expunge_deletes";
    public static final String ROLLOVER_API         = "/_rollover";
    public static final  String ALIAS_API              = "/%s/_alias";
    public static final String CAT_INDIES = "/_cat/indices/%s?v=true&format=json&filter_path=index";
    public static final String INDEX = "index";
    public static final String DELETE_INDEX = "%s?ignore_unavailable=true";
    public static final String ACKNOWLEDGED = "acknowledged";
    /**
     * 创建索引
     * @param cluster 集群
     * @param indexName 索引名字
     * @return result
     */
    public boolean createIndex(String cluster, String indexName) throws ESOperateException {
        if (exist(cluster, indexName)) {
            LOGGER.warn("class=ESIndexDAO||method=createIndex||index already exist||cluster={}||indexName={}", cluster,
                    indexName);
            return true;
        }
    
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }
        try {
        
            ESIndicesPutIndexResponse response = client.admin().indices().preparePutIndex(indexName).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getAcknowledged();
        } catch (Exception e) {
            LOGGER.error("class=ESIndexDAO||method=createIndex||cluster={}||indexName={}", cluster);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return false;
    }

    /**
     * 通过集群名称获取对应的ES Client
     * @param clusterName 集群名称
     * @return
     */
    private ESClient fetchESClientByCluster(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        if (client == null) {
            LOGGER.warn("class=ESIndexDAO||method=fetchESClientByCluster||cannot get es client,cluster={}",
                clusterName);
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
    public boolean createIndexWithConfig(String cluster, String indexName, IndexConfig indexConfig,Integer tryTimes)
            throws ESOperateException {
        
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            return Boolean.FALSE;
        }
        indexConfig.setVersion(ESVersion.valueBy(client.getEsVersion()));
        BiFunctionWithESOperateException<Long, TimeUnit, ESIndicesPutIndexResponse> esIndicesExistsResponseBiFunction = (timeout, unit) -> {
            try {
                return client.admin().indices().preparePutIndex(indexName).setIndexConfig(indexConfig).execute()
                        .actionGet(timeout, unit);
            } catch (Exception e) {
                LOGGER.error("class=ESIndexDAO||method=createIndexWithConfig||cluster={}||indexName={}", cluster);
                ParsingExceptionUtils.abnormalTermination(e);
            }
            return null;
        };
        
        ESIndicesPutIndexResponse response = esIndicesExistsResponseBiFunction.apply(Long.valueOf(ES_OPERATE_TIMEOUT), TimeUnit.SECONDS );
        
        return Optional.ofNullable(response).map(ESAcknowledgedResponse::getAcknowledged).orElse(Boolean.FALSE);
    }

    /**
     * 根据表达式判断索引是否已存在
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
     * 根据索引名称判断索引是否已存在
     * @param cluster    集群名称
     * @param indexName  索引名称
     * @return
     */
    public boolean existByClusterAndIndexName(String cluster, String indexName) throws NullESClientException {
        Client client = fetchESClientByCluster(cluster);
         if ( client==null) {
             throw new NullESClientException(cluster);
        }
        ESIndicesExistsRequest esIndicesExistsRequest = new ESIndicesExistsRequest();
        esIndicesExistsRequest.setIndex(indexName);
        BiFunction<Long,TimeUnit,ESIndicesExistsResponse> esIndicesExistsResponseBiFunction=(timeout,unit)->{
            try {
              return   client.admin().indices().exists(esIndicesExistsRequest).actionGet(timeout,unit);
            }catch (Exception e){
                 LOGGER.error("class=ESIndexDAO||method=existByClusterAndIndexName||cluster={}||indexName={}",
                cluster);
                 return null;
            }
        };
        ESIndicesExistsResponse response = esIndicesExistsResponseBiFunction.apply(Long.valueOf(ES_OPERATE_TIMEOUT),
                TimeUnit.SECONDS);
                
        
        return Optional.ofNullable(response).map(ESIndicesExistsResponse::isExists).orElse(Boolean.FALSE);
    }

    /**
     * 批量获取索引配置
     * @param cluster 集群
     * @param indexNames 索引名字
     * @return 配置
     */
    public MultiIndexsConfig batchGetIndexConfig(String cluster, List<String> indexNames) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }
        try {
            String indexExpression = String.join(",", indexNames);
            ESIndicesGetIndexResponse getIndexResponse = client.admin().indices().prepareGetIndex(indexExpression)
                    .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return getIndexResponse.getIndexsMapping();
        } catch (Exception e) {
            LOGGER.error("class=ESIndexDAO||method=batchGetIndexConfig||cluster={}||indexName={}||msg=index not exist",
                    cluster, String.join(",", indexNames),e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return null;
    }

    /**
     * 获取索引的mapping信息
     * @param cluster 集群
     * @param indexName 索引名字
     * @return
     */
    public MappingConfig getIndexMapping(String cluster, String indexName) {
        if (!exist(cluster, indexName)) {
            LOGGER.warn("class=ESIndexDAO||method=getIndexMapping||cluster={}||indexName={}||msg=index not exist",
                cluster, indexName);
            return null;
        }

        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) {
            return null;
        }
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
    public boolean updateIndexMapping(String cluster, String indexName, MappingConfig mappingConfig) throws ESOperateException {
        if (!exist(cluster, indexName)) {
            LOGGER.warn("class=ESIndexDAO||method=updateIndexMapping||cluster={}||indexName={}||msg=index not exist",
                cluster, indexName);
            return true;
        }

        ESClient client = esOpClient.getESClient(cluster);
        Map<String, TypeConfig> typeConfigMap = mappingConfig.getMapping();
        if (typeConfigMap == null) {
            return false;
        }

        for (Map.Entry<String, TypeConfig> entry : typeConfigMap.entrySet()) {
            String typeName = entry.getKey();
            ESIndicesUpdateMappingRequestBuilder builder = client.admin().indices().prepareUpdateMapping();
            builder.setIndex(indexName).setType(typeName).setTypeConfig(typeConfigMap.get(typeName));
            // es 集群版本7 需要带includeTypeName 参数，方可以更新index mapping
            if (Integer.parseInt(client.getEsVersion().split("\\.")[0]) == 7) {
                builder.setIncludeTypeName(true);
            }
            //es集群版本8 不需要type参数
            if (Integer.parseInt(client.getEsVersion().split("\\.")[0]) == 8) {
                builder.setIsNeedType(false);
            }
            try {
                ESIndicesUpdateMappingResponse response = builder.execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
                if (!response.getAcknowledged().booleanValue()) {
                    return false;
                }
            } catch (Exception e) {
                LOGGER.error("class=ESIndexDAO||method=updateIndexMapping||msg=update index mapping fail||cluster={}||indexName={}", cluster
                        ,indexName,e);
                ParsingExceptionUtils.abnormalTermination(e);
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
    public boolean deleteIndex(String cluster, String indexName) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null){
            LOGGER.warn(
                    "class={}||method=deleteIndex||clusterName={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster);
            throw new NullESClientException(cluster);
        }
        if (!exist(cluster, indexName)) {
            LOGGER.warn("class=ESIndexDAO||method=deleteIndex||cluster={}||indexName={}||msg=index not exist", cluster,
                indexName);
            return true;
        }
        try{
            ESIndicesDeleteIndexResponse response = client.admin().indices().prepareDeleteIndex(indexName).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getAcknowledged();
        } catch (Exception e) {
            LOGGER.error("class=ESIndexDAO||method=deleteIndex||cluster={}||indexName={}", cluster,
                    indexName,e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return false;
    }
    
    /**
     * > 按表达式删除索引
     *
     * @param cluster 集群名称，与配置文件中的集群名称一致。
     * @param expression 索引名称或索引名称表达式。
     * @return boolean
     */
    public boolean deleteByExpression(String cluster, String expression) throws ESOperateException {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }
        try {
            DirectRequest directRequest = new DirectRequest("GET", String.format(CAT_INDIES, expression));
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            List<String> indexNameList = Optional.ofNullable(directResponse)
                    .filter(response -> response.getRestStatus() == RestStatus.OK)
                    .map(DirectResponse::getResponseContent).filter(StringUtils::isNotBlank).map(JSON::parseArray)
                    .filter(json -> !json.isEmpty()).orElse(new JSONArray()).stream().filter(Objects::nonNull)
                    .map(json -> ((JSONObject) json).getString(INDEX)).distinct().collect(Collectors.toList());

            if (CollectionUtils.isEmpty(indexNameList)) {
                return true;
            }
            FunctionWithESOperateException<List<String>, Boolean> deleteFunc = indexList -> {
                try {
                    DirectRequest directRequestDelete = new DirectRequest("DELETE",
                            String.format(DELETE_INDEX, String.join(",", indexList)));
                    DirectResponse directResponseDelete = client.direct(directRequestDelete)
                            .actionGet(30, TimeUnit.SECONDS);
                    return Optional.ofNullable(directResponseDelete)
                            .filter(response -> response.getRestStatus() == RestStatus.OK)
                            .map(DirectResponse::getResponseContent).map(JSON::parseObject)
                            .map(json -> json.getBoolean(ACKNOWLEDGED)).orElse(false);
                } catch (Exception e) {
                    LOGGER.warn("class={}||method=deleteByExpression||cluster={}||expression={}",
                            getClass().getSimpleName(), cluster, String.join(",", indexList), e);

                    ParsingExceptionUtils.abnormalTermination(e);

                }
                return false;
            };
    
            BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>().batchList(
                    indexNameList).batchSize(30).processor(deleteFunc).succChecker(succ -> succ).process();
    
            Optional<Exception> first = result.getErrorMap().values().stream().findFirst();
            if (first.isPresent()) {
                throw new ESOperateException(first.get().getMessage());
            }
    
            return result.getResultList().stream().allMatch(Boolean.TRUE::equals);
        
        } catch (Exception e) {
            if (e instanceof ESIndexNotFoundException) {
                return true;
            }
            LOGGER.warn("class={}||method=deleteByExpression||cluster={}||expression={}", getClass().getSimpleName(),
                    cluster, expression, e);
            ParsingExceptionUtils.abnormalTermination(e);

        }
        return false;
        
    }

    /**
     * 根据索引模板名称获取物理集群所有分区索引
     * @param cluster 集群名称
     * @param expression 索引模板表达式
     * @return
     */
    public List<CatIndexResult> catIndexByExpression(String cluster, String expression) {
        List<CatIndexResult> indices = Lists.newArrayList();
        ESClient client = fetchESClientByCluster(cluster);
        if (client != null) {
            BiFunction<Long, TimeUnit, ESIndicesCatIndicesResponse> catIndicesResponseBiFunction = (timeout, unit) -> {
                try {
                    return client.admin().indices().prepareCatIndices(expression).execute()
                            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
                } catch (Exception e) {
                    LOGGER.warn("class=ESIndexDAO||method=catIndexByExpression||errMsg={}||cluster={}||expression={}",
                            e.getMessage(), cluster, expression, e);
                    return null;
                }
            };
            ESIndicesCatIndicesResponse esIndicesCatIndicesResponse =
                    catIndicesResponseBiFunction.apply(Long.valueOf(ES_OPERATE_TIMEOUT), TimeUnit.SECONDS);
            Optional.ofNullable(esIndicesCatIndicesResponse).map(ESIndicesCatIndicesResponse::getCatIndexResults).ifPresent(indices::addAll);
        }
         return indices;
    }

    /**
     * 获取集群中全量索引
     * @param cluster
     * @return
     */
    public List<CatIndexResult> catIndices(String cluster) {
        List<CatIndexResult> indices = Lists.newArrayList();

        try {
            ESClient client = fetchESClientByCluster(cluster);
            if (client != null) {
                ESIndicesCatIndicesResponse response = client.admin().indices().prepareCatIndices().execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
                indices.addAll(response.getCatIndexResults());
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESIndexDAO||method=catIndexByExpression||errMsg={}||cluster={}", e.getMessage(), cluster,
                e);
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
            LOGGER.error("class=ESIndexDAO||method=getIndexByExpression||cluster={}||expression={}",
                 cluster, expression, e);
            return null;
        }
    }

    /**
     * 获取指定集群,指定表达式的索引指标，附带全部shard的信息
     * @param cluster
     * @param expression
     * @return
     */
    public Map<String, IndexNodes> getIndexStatsWithShards(String cluster, String expression)throws ESOperateException {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }
        ESIndicesStatsResponse response = null;
        try {
        
            response = client.admin().indices().prepareStats(expression).setLevel(IndicesStatsLevel.SHARDS).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("class=ESIndexDAO||method=getIndexByExpression||errMsg={}||cluster={}||expression={}",
                    e.getMessage(), cluster, expression, e);

            ParsingExceptionUtils.abnormalTermination(e);

        }
        return Optional.ofNullable(response).map(ESIndicesStatsResponse::getIndicesMap).orElse(Maps.newHashMap());
    
    }

    public Map<String, IndexNodes> getIndexStats(String cluster, String expression) {
        try {
            ESClient client = fetchESClientByCluster(cluster);
            if (client == null) {
                return Maps.newHashMap();
            }
            ESIndicesStatsResponse response;
            if (StringUtils.isNotBlank(expression)) {
                response = client.admin().indices().prepareStats(expression).setLevel(IndicesStatsLevel.INDICES)
                    .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            } else {
                response = client.admin().indices().prepareStats().setLevel(IndicesStatsLevel.INDICES).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            }
            return response.getIndicesMap();
        } catch (Exception e) {
            LOGGER.warn("class=ESIndexDAO||method=getIndexByExpression||errMsg={}||cluster={}||expression={}",
                e.getMessage(), cluster, expression, e);
            return Maps.newHashMap();
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
     * 获取指定集群,指定多个索引
     * @param cluster
     * @param indices
     * @return
     */
    public Map<String/*index*/, AliasIndexNode> getAliasesByIndices(String cluster, String... indices) {
         ESClient client = esOpClient.getESClient(cluster);
         if (client==null){
             return null;
         }
        BiFunction<Long, TimeUnit, ESIndicesGetAliasResponse> responseBiFunction = (time, unit) -> {
            try {
                return client.admin().indices().prepareAlias(indices).execute().actionGet(time, unit);
            } catch (Exception e) {
                LOGGER.warn("class=ESIndexDAO||method=getAliasesByExpression||cluster={}||indices={}",
                        cluster, indices, e);
                return null;
            }
        };
        ESIndicesGetAliasResponse response =responseBiFunction.apply(Long.valueOf(ES_OPERATE_TIMEOUT),TimeUnit.SECONDS);
        
        
        
        return Optional.ofNullable(response).map(ESIndicesGetAliasResponse::getM).orElse(null);
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
            throw new NullESClientException( cluster);
        }

        ESIndicesDeleteByQueryResponse response = client.admin().indices().prepareDeleteByQuery().setIndex(delIndices)
            .setQuery(delQueryDsl).setHighES(true).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        if (response.getRoot().containsKey("error")) {
            throw new ESOperateException(response.getRoot().get("error").toString());
        }

        return true;
    }

    public boolean batchUpdateIndexRegion(String cluster, List<String> indices, Set<String> nodeNames)  throws ESOperateException{
        return putIndexSetting(cluster, indices, TEMPLATE_INDEX_INCLUDE_NODE_NAME, String.join(COMMA, nodeNames), "");
    }

    /**
     * 设置索引只读属性
     * @param cluster 集群
     * @param indexNames 索引列表
     * @param block 配置
     * @return result
     */
    public boolean blockIndexWrite(String cluster, List<String> indexNames, boolean block)  throws ESOperateException{
        return putIndexSetting(cluster, indexNames, INDEX_BLOCKS_WRITE, String.valueOf(block), "false");
    }

    /**
     * 设置索引只写属性
     * @param cluster 集群
     * @param indexNames 索引列表
     * @param block 配置
     * @return result
     */
    public boolean blockIndexRead(String cluster, List<String> indexNames, boolean block)  throws ESOperateException{
        return putIndexSetting(cluster, indexNames, INDEX_BLOCKS_READ, String.valueOf(block), "false");
    }

    /**
     * refresh索引
     * @param cluster 集群
     * @param indexNames 索引列表
     * @return result
     */
    public boolean refreshIndex(String cluster, List<String> indexNames) throws ESOperateException{
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }
    
        ESIndicesRefreshIndexResponse response = null;
        try {
            response = client.admin().indices().prepareRefreshIndex(String.join(",", indexNames)).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class={}||method=refreshIndex||clusterName={}||indexName={}", getClass().getSimpleName(),
                    cluster, String.join(",", indexNames), e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
    
        return Optional.ofNullable(response).map(r -> r.getFaild() == 0).orElse(false);
    }

    /**
     * 修改索引配置，多个setting数值的设置
     * @param cluster 物理集群名称
     * @param settings key：setting名称 value：setting数值
     */
    public boolean putIndexSettings(String cluster, List<String> indices,
                                    Map</*setting名称*/String, /*setting数值*/String> settings)
            throws ESOperateException {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null || MapUtils.isEmpty(settings)) {
            LOGGER.warn(
                    "class=ESTemplateDAO||method=putIndexSettings||get settings fail||clusterName={}||indexNames={}",
                    cluster, indices);
            throw new NullESClientException(cluster);
        }
    
        ESIndicesUpdateSettingsResponse esIndicesUpdateSettingsResponse = null;
        try {
            ESIndicesUpdateSettingsRequestBuilder updateSettingsRequestBuilder = client.admin().indices()
                    .prepareUpdateSettings(String.join(",", indices));
            // 依次添加需要设置的 setting 的字段的名称和数值
            settings.forEach(updateSettingsRequestBuilder::addSettings);
            esIndicesUpdateSettingsResponse = updateSettingsRequestBuilder.execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class=ESTemplateDAO||method=putIndexSettings||get index fail||clusterName={}||indexName={}",
                    cluster, e);
            ParsingExceptionUtils.abnormalTermination(e);
        
        }
        

        return Optional.ofNullable(esIndicesUpdateSettingsResponse).map(ESIndicesUpdateSettingsResponse::getAcknowledged
        ).orElse(false);
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
                                   String defaultValue) throws ESOperateException{
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }

        MultiIndexsConfig multiIndexsConfig = batchGetIndexConfig(cluster, indices);

        List<String> needOps = Lists.newArrayList();
        for (Map.Entry<String, IndexConfig> indexConfigEntry : multiIndexsConfig.getIndexConfigMap().entrySet()) {
            IndexConfig indexConfig = indexConfigEntry.getValue();

            //由于客户端在put-setting的时候会默认加上"index."的前缀 所以这里需要这样搞
            Map<String, String> config = indexConfig.getSettings();
            String src = config.get(INDEX_SETTING_PRE + settingName);
            if (settingName.startsWith(INDEX_SETTING_PRE)) {
                src = config.get(settingName);
            }

            if (src == null) {
                src = defaultValue;
            }
            if (src.equals(String.valueOf(setting))) {
                continue;
            }
            needOps.add(indexConfigEntry.getKey());
        }

        if (CollectionUtils.isEmpty(needOps)) {
            return true;
        }
    
        ESIndicesUpdateSettingsResponse updateSettingsResponse = null;
        try {
        
            updateSettingsResponse = client.admin().indices().prepareUpdateSettings(String.join(",", needOps))
                    .addSettings(settingName, String.valueOf(setting)).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class={}||method=putIndexSetting||clusterName={}||indexName={}", getClass().getSimpleName(),
                    cluster, String.join(",", indices), e);
            ParsingExceptionUtils.abnormalTermination(e);
        
        }
        return Optional.ofNullable(updateSettingsResponse).map(ESIndicesUpdateSettingsResponse::getAcknowledged)
                .orElse(false);
        
    }

    public boolean putIndexSetting(String cluster, List<String> indices, Map<String, String> settingMap)
            throws ESOperateException {
        ESClient client = fetchESClientByCluster(cluster);
        if (null == client) {
            return false;
        }

        MultiIndexsConfig multiIndexsConfig = batchGetIndexConfig(cluster, indices);
        List<String> needOps = Lists.newArrayList();
        for (Map.Entry<String, IndexConfig> indexConfigEntry : multiIndexsConfig.getIndexConfigMap().entrySet()) {
            IndexConfig indexConfig = indexConfigEntry.getValue();

            Map<String, String> config = indexConfig.getSettings();
            Boolean modifyFlag = Boolean.FALSE;
            for (Map.Entry<String, String> settingEntry : settingMap.entrySet()) {
                String settingName = settingEntry.getKey();
                String settingValue = settingEntry.getValue();
                String src = config.get(INDEX_SETTING_PRE + settingName);
                if (settingName.startsWith(INDEX_SETTING_PRE)) {
                    src = config.get(settingName);
                }

                if (!settingValue.equals(src)) {
                    modifyFlag = Boolean.TRUE;
                }
            }
            if (modifyFlag) {
                needOps.add(indexConfigEntry.getKey());
            }
        }

        if (CollectionUtils.isEmpty(needOps)) {
            return true;
        }
        try {
        
            ESIndicesUpdateSettingsRequestBuilder updateSettingsRequestBuilder = client.admin().indices()
                    .prepareUpdateSettings(String.join(",", needOps));
            for (Map.Entry<String, String> settingEntry : settingMap.entrySet()) {
                updateSettingsRequestBuilder.addSettings(settingEntry.getKey(), settingEntry.getValue());
            }
            return updateSettingsRequestBuilder.execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS)
                    .getAcknowledged();
        } catch (Exception e) {
            LOGGER.error("class=ESIndexDAO||method=putIndexSetting||cluster={}||indexName={}",
                    cluster, String.join(",", indices));
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return false;
    }

    /**
     * 关闭索引
     * @param cluster 集群
     * @param indices 索引
     * @return result
     */
    public boolean closeIndex(String cluster, List<String> indices) throws ESOperateException {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }
        try {
        
            ESIndicesCloseIndexResponse response = client.admin().indices().prepareCloseIndex(String.join(",", indices))
                    .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getAcknowledged();
        } catch (Exception e) {
            LOGGER.warn("class={}||method=closeIndex||clusterName={}||indexName={}||msg={}", getClass().getSimpleName(),
                    cluster, String.join(",", indices), e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return false;
    }

    /**
     * 打开索引
     * @param cluster 集群
     * @param indices 索引
     * @return result
     */
    public boolean openIndex(String cluster, List<String> indices) throws ESOperateException{
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }
        try {
        
            ESIndicesOpenIndexResponse response = client.admin().indices().prepareOpenIndex(String.join(",", indices))
                    .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getAcknowledged();
        } catch (Exception e) {
            LOGGER.warn("class={}||method=closeIndex||clusterName={}||indexName={}||msg={}", getClass().getSimpleName(),
                    cluster, String.join(",", indices), e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return false;
    }

    /**
     * 获取模板信息
     * @param clusterName       集群名
     * @param indexName         索引名
     * @return result
     */
    public MultiIndexsConfig getIndexConfigs(String clusterName, String indexName) {
        ESClient esClient = esOpClient.getESClient(clusterName);

        if (null == esClient) {
            return null;
        }

        ESIndicesGetIndexRequest request = new ESIndicesGetIndexRequest();
        request.setIndices(indexName);

        ESIndicesGetIndexResponse response = null;
        try {
            response = esClient.admin().indices().getIndex(request).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn(
                "class=ESTemplateDAO||method=getIndexConfigs||get index fail||clusterName={}||indexName={}||msg={}",
                clusterName, indexName, e.getMessage(), e);
        }

        if (response == null) {
            return null;
        }

        LOGGER.debug("class=ESTemplateDAO||method=getIndexConfigs||response={}", JSON.toJSONString(response));


        return response.getIndexsMapping();
    }

    /**
     * 根据集群名称和索引名称获取索引setting信息
     * @param clusterName       集群名称
     * @param indexNames        索引列表
     * @param tryTimes
     * @return
     */
    public Map<String, IndexConfig> getIndicesSetting(String clusterName, List<String> indexNames, int tryTimes) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (null == esClient) {
            return Maps.newHashMap();
        }

        ESIndicesGetAllSettingRequest request = new ESIndicesGetAllSettingRequest();
        request.setDefaultSettingFlag(true);
        request.mapping(false);
        request.alias(false);
        request.setIndices(ListUtils.strList2StringArray(indexNames));

        ESIndicesGetIndexResponse response = null;
        try {
            do {
                response = esClient.admin().indices().getIndex(request).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            } while (tryTimes-- > 0 && null == response);
        } catch (Exception e) {
            LOGGER.warn(
                "class=ESTemplateDAO||method=getIndexConfigs||get index fail||clusterName={}||indexName={}||msg={}",
                clusterName, e.getMessage(), e);
        }

        if (response == null) {
            return Maps.newHashMap();
        }



        return response.getIndexsMapping().getIndexConfigMap();
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
            response = builder.setStore(true).setDocs(true).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error(
                "class=ClusterClientPool||method=getIndexNodes||clusterName={}||errMsg=get {} index stats error. ",
                clusterName, templateExp, e);
        }

        if (response == null) {
            return null;
        }

        return response.getIndicesMap();
    }

    /**
     * 编辑索引别名
     * @param aliases
     * @return result
     */
    public boolean editAlias(String cluster, List<PutAliasNode> aliases) throws ESOperateException {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }
        if (CollectionUtils.isEmpty(aliases)) {
            return true;
        }
        String[] indeies = aliases.stream().map(PutAliasNode::getIndex).distinct().toArray(String[]::new);
        final List<String> aliasLit = aliases.stream().map(PutAliasNode::getAlias).distinct()
                .collect(Collectors.toList());
        // 这里有两种情况，第一种：直接删除成功，但是因为集群不稳定导致了返回异常；第二种：删除失败；
        BiFunctionWithESOperateException<Long, TimeUnit, ESIndicesPutAliasResponse> esIndicesPutAliasResponseBiFunction = (time, unit) -> {
            try {
                return client.admin().indices().preparePutAlias().addPutAliasNodes(aliases).execute()
                        .actionGet(time, unit);
            } catch (Exception e) {
                LOGGER.error("class=ESIndexDAO||method=editAlias||clusterName={}", cluster, e);
                ParsingExceptionUtils.abnormalTermination(e);

            }
            return null;
        };
        ESIndicesPutAliasResponse response = esIndicesPutAliasResponseBiFunction.apply(Long.valueOf(ES_OPERATE_TIMEOUT),
                TimeUnit.SECONDS);
    
        final Boolean acknowledged = Optional.ofNullable(response).map(ESIndicesPutAliasResponse::getAcknowledged)
                .orElse(Boolean.FALSE);
        if (Boolean.FALSE.equals(acknowledged)) {
            // 针对第一种情况进行别名存在的情况判断，不存在则认为删除成功
            final Map<String, AliasIndexNode> aliasesByIndices = getAliasesByIndices(cluster, indeies);
            if (Objects.isNull(aliasesByIndices)) {
                return false;
            }
            // 如果不包含，则都删除成功了
            return aliasesByIndices.values().stream().map(AliasIndexNode::getAliases).map(Map::keySet)
                    .flatMap(Collection::stream).noneMatch(aliasLit::contains);
        
        }
    
        return acknowledged;
    }

    public Result<Void> rollover(String cluster, String alias, String conditions) {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            LOGGER.warn("class=ESIndexDAO||method=rollover||errMsg=es client not found");
            return Result.buildFail("所属集群异常，请检查集群是否正常");
        }
        

        try {
            DirectRequest directRequest = new DirectRequest(HttpMethod.POST.name(), alias + ROLLOVER_API);
            if (StringUtils.isNotBlank(conditions)) {
                directRequest.setPostContent(conditions);
            }
            DirectResponse directResponse = client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT,
                TimeUnit.SECONDS);
            return Result.buildWithMsg(RestStatus.OK == directResponse.getRestStatus(),
                    RestStatus.OK == directResponse.getRestStatus() ? String.format("别名 %s 执行 rollover 成功", alias)
                            : directResponse.getResponseContent());
        }catch (ESAlreadyExistsException e){
            return Result.buildFail(String.format("%s 需要先删除此索引",
                    ParsingExceptionUtils.getESErrorMessageByException(e)));
        }
        catch (Exception e) {
            final String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
            if (Objects.nonNull(exception)) {
                return Result.buildFail(exception);
            }
            
            
            LOGGER.warn("class=ESIndexDAO||method=rollover||errMsg=index rollover fail");
            return Result.buildFail(String.format(FAILED_MSG, "rollover"));
        }
    }
    
    
    public int countIndexByAlias(String cluster, String alias) throws ESOperateException {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            LOGGER.warn("class=ESIndexDAO||method=rollover||errMsg=es client not found");
            throw new ESOperateException(String.format("所属集群 %s 异常，请检查集群是否正常", cluster));
        }
        DirectRequest directRequest = new DirectRequest(HttpMethod.GET.name(), String.format(ALIAS_API, alias));
        try {
            DirectResponse directResponse = client.direct(directRequest)
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            if (RestStatus.OK != directResponse.getRestStatus()) {
                throw new ESOperateException(directResponse.getResponseContent());
            }
            return JSONObject.parseObject(directResponse.getResponseContent()).values().size();
            
        } catch (Exception e) {
            LOGGER.warn("class=ESIndexDAO||method=countIndexByAlias||errMsg=index countIndexByAlias fail");
            ParsingExceptionUtils.abnormalTermination(e);

        }
        return 0;
    }
    

    public Result<Void> forceMerge(String cluster, String index, Integer maxNumSegments, Boolean onlyExpungeDeletes) {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            LOGGER.warn("class={}||method=forceMerge||cluster={}||index={}||errMsg=es client not found",
                    getClass().getSimpleName(), client, index);
            return Result.buildFail();
        }

        try {
            Map<String, String> params = new HashMap<>();
            if (Boolean.TRUE.equals(onlyExpungeDeletes)) {
                params.put(ONLY_EXPUNGE_DELETES, Boolean.TRUE.toString());
            } else if (null != maxNumSegments) {
                params.put(MAX_NUM_SEGMENTS, maxNumSegments.toString());
            }

            DirectRequest directRequest = new DirectRequest(HttpMethod.POST.name(), index + "/_forcemerge");
            directRequest.setParams(params);
            DirectResponse directResponse = client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT,
                TimeUnit.SECONDS);
            return Result.buildWithMsg(RestStatus.OK == directResponse.getRestStatus(),
                directResponse.getResponseContent());
        } catch (Exception e) {
            final String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
            if (Objects.nonNull(exception)) {
                return Result.buildFail(exception);
            }
            LOGGER.warn("class=ESIndexDAO||method=forceMerge||errMsg=index forceMerge fail");
            return Result.buildFail(String.format(FAILED_MSG, "forceMerge"));
        }
    }

    public Result<Void> shrink(String cluster, String index, String targetIndex, String config) {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            LOGGER.warn("class=ESIndexDAO||method=forceMerge||errMsg=es client not found");
            return Result.buildFail();
        }
    
        try {
            DirectRequest directRequest = new DirectRequest(HttpMethod.POST.name(), index + "/_shrink/" + targetIndex);
            directRequest.setPostContent(config);
            DirectResponse directResponse = client.direct(directRequest)
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return Result.buildWithMsg(RestStatus.OK == directResponse.getRestStatus(),
                    directResponse.getResponseContent());
        } catch (ESAlreadyExistsException e) {
            return Result.buildFail(String.format("%s 需要先删除此索引",
                    ParsingExceptionUtils.getESErrorMessageByException(e)));
        } catch (Exception e) {
            final String exception = ParsingExceptionUtils.getESErrorMessageByException(
                    e);
        
            if (Objects.nonNull(exception)) {
                return Result.buildFail(exception);
            }
            LOGGER.warn("class=ESIndexDAO||method=shrink||errMsg=index shrink fail");
            return Result.buildFail(String.format(FAILED_MSG, "shrink"));
        }
    }

    public Result<Void> split(String cluster, String index, String targetIndex, String config) {
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            LOGGER.warn("class=ESIndexDAO||method=forceMerge||errMsg=es client not found");
            return Result.buildFail();
        }
        if (Double.parseDouble(client.getEsVersion().substring(0, 2))<=6.0) {
            return Result.buildFail(String.format("es %s 不支持 split 功能", client.getEsVersion()));
        }
        try {
            DirectRequest directRequest = new DirectRequest(HttpMethod.POST.name(), index + "/_split/" + targetIndex);
            directRequest.setPostContent(config);
            DirectResponse directResponse = client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT,
                TimeUnit.SECONDS);
            return Result.buildWithMsg(RestStatus.OK == directResponse.getRestStatus(),
                directResponse.getResponseContent());
        } catch (ESAlreadyExistsException e) {
            return Result.buildFail(String.format("%s 需要先删除此索引",
                    ParsingExceptionUtils.getESErrorMessageByException(e)));
        } catch (Exception e) {
            final String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
            if (Objects.nonNull(exception)) {
                return Result.buildFail(exception);
            }
            LOGGER.warn("class=ESIndexDAO||method=split||errMsg=index split fail");
            return Result.buildFail(String.format(FAILED_MSG, "split"));
        }
    }
 
}