package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.getBigIndicesRequestContent;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.BLOCKS;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.DEFAULTS;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.INDEX;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.READ;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.WRITE;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexResponse;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexBlockEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.BatchProcessor;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESIndexDAO;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.elasticsearch.client.request.index.putalias.PutAliasNode;
import com.didiglobal.logi.elasticsearch.client.request.index.putalias.PutAliasType;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.AliasIndexNode;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2019/4/2
 */
@Service
public class ESIndexServiceImpl implements ESIndexService {

    private static final ILog      LOGGER = LogFactory.getLog(ESIndexServiceImpl.class);

    @Autowired
    private ESIndexDAO             esIndexDAO;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Override
    public boolean syncCreateIndex(String cluster, String indexName, int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("createIndex", retryCount,
            () -> esIndexDAO.createIndex(cluster, indexName));
    }

    @Override
    public boolean syncCreateIndex(String cluster, String indexName, IndexConfig indexConfig,
                                   int retryCount) throws ESOperateException {
        return esIndexDAO.createIndexWithConfig(cluster, indexName, indexConfig,retryCount);
    }

    @Override
    public boolean syncDelIndex(String cluster, String indexName, int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("deleteIndex", retryCount,
            () -> esIndexDAO.deleteIndex(cluster, indexName));
    }

    /**
     * 根据表达式删除索引
     * @param cluster    集群
     * @param expression 表达式
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    @Override
    public boolean syncDeleteIndexByExpression(String cluster, String expression,
                                               int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("deleteIndexByExpression", retryCount,
            () -> esIndexDAO.deleteIndex(cluster, expression));
    }

    /**
     * 跟进索引名称获取索引的mapping
     * @param cluster    集群
     * @param index      索引名称
     * @return result
     * @throws ESOperateException
     */
    @Override
    public String syncGetIndexMapping(String cluster, String index) {
        MappingConfig mappingConfig = esIndexDAO.getIndexMapping(cluster, index);

        if (null == mappingConfig) {
            LOGGER.warn(
                "class=ESIndexServiceImpl||method=syncGetIndexMapping||errMsg=index mapping is null||cluster={}||index={}",
                cluster, index);
            return "";
        }
        return mappingConfig.toJson().toString();
    }

    @Override
    public boolean syncUpdateIndexMapping(String cluster, String index, MappingConfig mappingConfig) throws ESOperateException {
        return esIndexDAO.updateIndexMapping(cluster, index, mappingConfig);
    }

    @Override
    public Map<String, IndexConfig> syncBatchGetIndexConfig(String cluster, List<String> indexList) {
        MultiIndexsConfig multiIndexsConfig = esIndexDAO.batchGetIndexConfig(cluster, indexList);
        if (null == multiIndexsConfig) {
            return Maps.newConcurrentMap();
        }
        return multiIndexsConfig.getIndexConfigMap();
    }

    /**
     * 查询集群中的索引
     * @param cluster    集群
     * @param expression 表达式
     * @return 索引集合
     */
    @Override
    public Set<String> syncGetIndexNameByExpression(String cluster, String expression) {
        LOGGER.info("class=ESIndexServiceImpl||method=syncGetIndexNameByExpression||cluster={}||expression={}", cluster,
            expression);

        Map<String, IndexNodes> indexNodesMap = esIndexDAO.getIndexByExpression(cluster, expression);
        if (indexNodesMap == null || indexNodesMap.isEmpty()) {
            LOGGER.warn(
                "class=ESIndexServiceImpl||method=syncGetIndexNameByExpression||errMsg=no index||cluster={}||expression={}",
                cluster, expression);
            return Sets.newHashSet();
        }

        LOGGER.info(
            "class=ESIndexServiceImpl||method=syncGetIndexNameByExpression||cluster={}||expression={}||indices={}",
            cluster, expression, JSON.toJSONString(indexNodesMap));

        return indexNodesMap.keySet();
    }

    /**
     * 更新索引列表对应settings中值
     * @param cluster 集群名称
     * @param indices 索引列表
     * @param settingName setting名称
     * @param settingValue setting值
     * @param defaultValue 默认值
     * @param retryCount 重试次数
     * @return
     * @throws ESOperateException
     */
    @Override
    public boolean syncPutIndexSetting(String cluster, List<String> indices, String settingName, String settingValue,
                                       String defaultValue, int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("putIndexSetting", retryCount,
            () -> esIndexDAO.putIndexSetting(cluster, indices, settingName, settingValue, defaultValue));
    }

    @Override
    public boolean syncPutIndexSetting(String cluster, List<String> indices, Map<String, String> settingMap,
                                       Integer retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("putIndexSettingBatch", retryCount,
            () -> esIndexDAO.putIndexSetting(cluster, indices, settingMap));
    }

    @Override
    public boolean syncPutIndexSettings(String cluster, List<String> indices, Map<String, String> settings,
                                        int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("putIndexSettings", retryCount,
            () -> esIndexDAO.putIndexSettings(cluster, indices, settings));
    }

    /**
     * 获取索引信息
     *
     * @param cluster    集群
     * @param expression 表达式
     * @return result
     */
    @Override
    public Map<String, IndexNodes> syncGetIndexByExpression(String cluster, String expression) {
        return esIndexDAO.getIndexByExpression(cluster, expression);
    }

    /**
     * 获取索引信息
     *
     * @param cluster 集群
     * @param indexNames 索引列表
     * @return result
     */
    @Override
    public Map<String, IndexNodes> syncBatchGetIndices(String cluster, Collection<String> indexNames) {
        BatchProcessor.BatchProcessResult<String, Map<String, IndexNodes>> result = new BatchProcessor<String, Map<String, IndexNodes>>()
            .batchList(indexNames).batchSize(30)
            .processor(items -> esIndexDAO.getIndexStatsWithShards(cluster, String.join(",", items))).process();
        return ConvertUtil.mergeMapList(result.getResultList());
    }

    /**
     * 查询集群中的别名
     * @param cluster    集群
     * @param expression 表达式
     * @return 索引集合
     */
    @Override
    public List<Tuple<String, String>> syncGetIndexAliasesByExpression(String cluster, String expression) {
        Map<String/*index*/, AliasIndexNode> aliasIndexNodeMap = esIndexDAO.getAliasesByExpression(cluster, expression);
        if (aliasIndexNodeMap == null) {
            LOGGER.warn(
                "class=ESIndexServiceImpl||method=syncGetIndexNameByExpression||msg=no alias||cluster={}||expression={}",
                cluster, expression);
            return new ArrayList<>();
        }

        List<Tuple<String, String>> ret = new ArrayList<>();

        for (Map.Entry<String, AliasIndexNode> entry : aliasIndexNodeMap.entrySet()) {
            String index = entry.getKey();
            AliasIndexNode aliasIndexNode = aliasIndexNodeMap.get(index);
            Map<String, JSONObject> aliases = aliasIndexNode.getAliases();

            if (null != aliases) {
                aliases.keySet().forEach(a -> ret.add(new Tuple<>(index, a)));
            }
        }

        return ret;
    }

    @Override
    public Map<String, List<String>> syncGetIndexAliasesByIndices(String cluster, String... indices) {
        Map<String/*index*/, AliasIndexNode> aliasIndexNodeMap = esIndexDAO.getAliasesByIndices(cluster, indices);
        if (aliasIndexNodeMap == null) {
            LOGGER.warn(
                "class=ESIndexServiceImpl||method=syncGetIndexNameByExpression||msg=no alias||cluster={}||expression={}",
                cluster, indices);
            return new HashMap<>();
        }

        Map<String, List<String>> ret = new HashMap<>();
        aliasIndexNodeMap.forEach((index, aliasIndexNode) -> {
            Optional.ofNullable(aliasIndexNode.getAliases()).map(Map::keySet).ifPresent(set -> {
                List<String> aliases = ret.getOrDefault(index, Lists.newArrayList());
                aliases.addAll(set);
                ret.put(index, aliases);
            });
        });

        return ret;
    }

    /**
     * 批量删除索引
     * @param cluster    集群
     * @param shouldDels 索引集合
     * @param retryCount 重试次数
     * @return result
     */
    @Override
    public int syncBatchDeleteIndices(String cluster, Collection<String> shouldDels, int retryCount) {
        if (CollectionUtils.isEmpty(shouldDels)) {
            return 0;
        }

        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
            .batchList(shouldDels).batchSize(10)
            .processor(items -> batchDeleteIndicesInner(cluster, String.join(",", items), retryCount))
            .succChecker(succ -> succ).process();

        if (!result.isSucc()) {
            LOGGER.warn("class=ESIndexServiceImpl||method=syncBatchDeleteIndices||cluster={}||shouldDels={}||result={}",
                cluster, shouldDels, result);
        }

        return shouldDels.size() - result.getFailAndErrorCount();
    }

    @Override
    public boolean syncBatchCloseIndices(String cluster, List<String> shouldCloses,
                                         int retryCount) throws ESOperateException {
        if (CollectionUtils.isEmpty(shouldCloses)) {
            return true;
        }

        return ESOpTimeoutRetry.esRetryExecute("closeIndex", retryCount,
            () -> esIndexDAO.closeIndex(cluster, shouldCloses));
    }

    @Override
    public boolean syncBatchOpenIndices(String cluster, List<String> shouldOpens,
                                        int retryCount) throws ESOperateException {
        if (CollectionUtils.isEmpty(shouldOpens)) {
            return true;
        }

        return ESOpTimeoutRetry.esRetryExecute("openIndex", retryCount,
            () -> esIndexDAO.openIndex(cluster, shouldOpens));
    }

    /**
     * 删除文档
     *
     * @param cluster     集群
     * @param delIndices  索引
     * @param delQueryDsl 删除语句
     * @return
     */
    @Override
    public boolean syncDeleteByQuery(String cluster, List<String> delIndices,
                                     String delQueryDsl) throws ESOperateException {
        return esIndexDAO.deleteByQuery(cluster, String.join(",", delIndices), delQueryDsl);
    }

    @Override
    public boolean syncBatchUpdateRegion(String cluster, List<String> indices, Integer tgtRegionId,
                                         int retryCount) throws ESOperateException {
        Set<String> nodeNames = new HashSet<>();
        Result<List<ClusterRoleHost>> clusterRoleHostResult = clusterRoleHostService.listByRegionId(tgtRegionId);
        if (clusterRoleHostResult.failed()) {
            return false;
        }
        clusterRoleHostResult.getData().stream()
            .forEach(clusterRoleHost -> nodeNames.add(clusterRoleHost.getNodeSet()));

        return ESOpTimeoutRetry.esRetryExecute("syncBatchUpdateRegion", retryCount,
            () -> esIndexDAO.batchUpdateIndexRegion(cluster, indices, nodeNames));
    }

    /**
     * 修改索引只读配置
     *
     * @param cluster    集群
     * @param indices    索引
     * @param block   配置
     * @param retryCount 重试次数
     * @return true/false
     */
    @Override
    public boolean syncBatchBlockIndexWrite(String cluster, List<String> indices, boolean block,
                                            int retryCount) throws ESOperateException {
        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
            .batchList(indices).batchSize(30).processor(items -> {
                try {
                    return ESOpTimeoutRetry.esRetryExecute("syncBatchBlockIndexWrite", retryCount,
                        () -> esIndexDAO.blockIndexWrite(cluster, items, block));
                } catch (ESOperateException e) {
                    return false;
                }
            }).succChecker(succ -> succ).process();

        return result.isSucc();
    }

    @Override
    public boolean syncBatchBlockIndexRead(String cluster, List<String> indices, boolean block,
                                           int retryCount) throws ESOperateException {
        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
            .batchList(indices).batchSize(30).processor(items -> {
                try {
                    return ESOpTimeoutRetry.esRetryExecute("syncBatchBlockIndexRead", retryCount,
                        () -> esIndexDAO.blockIndexRead(cluster, items, block));
                } catch (ESOperateException e) {
                    return false;
                }
            }).succChecker(succ -> succ).process();

        return result.isSucc();
    }

    /**
     * 校验索引数据是否一致
     *
     * @param cluster1   集群1
     * @param cluster2   集群2
     * @param indexNames 索引名字
     * @return true/false
     */
    @Override
    public boolean ensureDateSame(String cluster1, String cluster2, List<String> indexNames) {
        int retryCount = 1;
        while (retryCount-- > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("class=ESIndexServiceImpl||method=ensureDateSame||msg=sleep interrupted", e);
            }

            if (checkDateSame(cluster1, cluster2, indexNames)) {
                return true;
            }
        }

        return false;
    }

    /**
     * close and open index
     *
     * @param cluster 集群
     * @param indices 索引
     * @return result
     */
    @Override
    public boolean reOpenIndex(String cluster, List<String> indices, int retryCount) throws ESOperateException {
        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
            .batchList(indices).batchSize(30).processor(items -> {
                try {
                    if (ESOpTimeoutRetry.esRetryExecute("reOpenIndex-close", retryCount,
                        () -> esIndexDAO.closeIndex(cluster, items))) {
                        return ESOpTimeoutRetry.esRetryExecute("reOpenIndex-open", retryCount,
                            () -> esIndexDAO.openIndex(cluster, items));
                    } else {
                        return false;
                    }
                } catch (ESOperateException e) {
                    return false;
                }
            }).succChecker(succ -> succ).process();

        return result.isSucc();
    }

    /**
     * cat index
     *
     * @param cluster    集群
     * @param expression 表达式
     * @return list
     */
    @Override
    public List<CatIndexResult> syncCatIndexByExpression(String cluster, String expression) {
        List<CatIndexResult> catIndexResults = esIndexDAO.catIndexByExpression(cluster, expression);

        LOGGER.info("class=ESIndexServiceImpl||method=syncCatIndexByExpression||cluster={}||expression={}||indices={}",
            cluster, expression, JSON.toJSONString(catIndexResults));

        return catIndexResults;
    }

    @Override
    public List<CatIndexResult> syncCatIndex(String clusterPhyName, int tryTimes) {
        int retryCount = tryTimes < 0 ? 1 : tryTimes;
        List<CatIndexResult> catIndexResultList = Lists.newArrayList();
        while (retryCount-- > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("class=ESIndexServiceImpl||method=ensureDateSame||msg=sleep interrupted", e);
            }
            catIndexResultList.addAll(esIndexDAO.catIndices(clusterPhyName));
            if (CollectionUtils.isNotEmpty(catIndexResultList)) {
                break;
            }
        }

        return catIndexResultList.stream().filter(this::filterOriginalIndices).collect(Collectors.toList());
    }

    /**
     * 过滤原始索引
     */
    private boolean filterOriginalIndices(CatIndexResult catIndexResult) {
        if (null == catIndexResult) {
            return false;
        }

        return StringUtils.isNotBlank(catIndexResult.getIndex()) && !catIndexResult.getIndex().startsWith(".");
    }

    /**
     * 获取索引配置
     * @param cluster 集群名称
     * @param name 索引名称
     * @return
     */
    @Override
    public MultiIndexsConfig syncGetIndexConfigs(String cluster, String name) {
        return esIndexDAO.getIndexConfigs(cluster, name);
    }

    @Override
    public Map<String, IndexConfig> syncGetIndexSetting(String cluster, List<String> indexNames, int tryTimes) {
        return esIndexDAO.getIndicesSetting(cluster, indexNames, tryTimes);
    }

    /**
     * 获取索引主shard个数
     * @param clusterName
     * @param indexName
     * @return
     */
    @Override
    public Integer syncGetIndexPrimaryShardNumber(String clusterName, String indexName) {
        Integer primaryShardNumber = null;

        MultiIndexsConfig multiIndexsConfig = syncGetIndexConfigs(clusterName, indexName);
        if (multiIndexsConfig != null && multiIndexsConfig.getIndexConfigMap() != null) {
            for (Map.Entry<String, IndexConfig> entry : multiIndexsConfig.getIndexConfigMap().entrySet()) {
                Map<String, String> settings = entry.getValue().getSettings();
                if (settings != null) {
                    try {
                        primaryShardNumber = getPrimaryShardNumber(primaryShardNumber, settings);
                    } catch (NumberFormatException e) {
                        LOGGER.error(
                            "class=ESIndexServiceImpl||method=getIndexPrimaryShardNumberByCLusterName||clusterName={}||indexName={}||errMsg=fail to parse {}. ",
                            clusterName, indexName, settings.get("index.number_of_shards"), e);
                    }
                }
            }
        }

        return primaryShardNumber;
    }

    @Override
    public Map<String, IndexNodes> syncGetIndexNodes(String clusterName, String templateExp) {
        return esIndexDAO.getIndexNodes(clusterName, templateExp);
    }

    @Override
    public List<String> syncGetIndexName(String clusterName) {
        String indicesRequestContent = getBigIndicesRequestContent("20s");

        DirectResponse directResponse = esIndexDAO.getDirectResponse(clusterName, "Get", indicesRequestContent);

        List<IndexResponse> indexResponses = Lists.newArrayList();

        if (directResponse.getRestStatus() == RestStatus.OK
            && StringUtils.isNoneBlank(directResponse.getResponseContent())) {

            indexResponses = ConvertUtil.str2ObjArrayByJson(directResponse.getResponseContent(), IndexResponse.class);
        }
        return indexResponses.stream().map(IndexResponse::getIndex).collect(Collectors.toList());
    }

    @Override
    public boolean syncIsIndexExist(String cluster, String indexName) {
        return esIndexDAO.existByClusterAndIndexName(cluster, indexName,3);
    }

    @Override
    public AtomicLong syncGetTotalCheckpoint(String index, IndexNodes stat, AtomicBoolean checkpointEqualSeqNo) {
        AtomicLong totalCheckpoint = new AtomicLong(0);
        if (stat == null) {
            return totalCheckpoint;
        }

        stat.getShards().forEach((shard, v) -> v.forEach(commonStat -> {
            if (!commonStat.getRouting().isPrimary()) {
                return;
            }

            if (null == commonStat.getSeqNo()) {
                LOGGER.warn(
                    "class=ESIndexServiceImpl||method=syncGetTotalCheckpoint||indexName={}||shard={}||msg=commonStat seqNo is empty",
                    index, shard);
                return;
            }

            if (commonStat.getSeqNo().getMaxSeqNo() != commonStat.getSeqNo().getGlobalCheckpoint()) {
                LOGGER.warn(
                    "class=ESIndexServiceImpl||method=syncGetTotalCheckpoint||indexName={}||shard={}||msg=primary maxSeqNo({})!=globalCheckpoint({})",
                    index, shard, commonStat.getSeqNo().getMaxSeqNo(), commonStat.getSeqNo().getGlobalCheckpoint());
                if (null != checkpointEqualSeqNo) {
                    checkpointEqualSeqNo.set(false);
                }
            }

            totalCheckpoint.addAndGet(commonStat.getSeqNo().getGlobalCheckpoint());
        }));
        return totalCheckpoint;
    }

    @Override
    public Result<Void> addAliases(String cluster, String index, List<String> aliases) {
        if (!esIndexDAO.exist(cluster, index)) {
            return Result.buildParamIllegal(String.format("索引【%s】不存在", index));
        }
        if (CollectionUtils.isEmpty(aliases)) {
            return Result.buildParamIllegal("要操作的别名不存在");
        }
        List<PutAliasNode> putAliasNodeList = new ArrayList<>();
        Map<String, List<String>> aliasIndexNodeMap = syncGetIndexAliasesByIndices(cluster, index);
        Set<String> aliasSet = new HashSet<>();
        Optional.ofNullable(aliasIndexNodeMap.get(index)).ifPresent(aliasSet::addAll);

        aliases.stream().filter(StringUtils::isNotBlank).forEach(aliasName -> {
            PutAliasNode putAliasNode = new PutAliasNode();
            putAliasNode.setIndex(index);
            putAliasNode.setAlias(aliasName);
            putAliasNode.setType(PutAliasType.ADD);
            if (!aliasSet.contains(aliasName)) {
                putAliasNodeList.add(putAliasNode);
            }
        });

        if (CollectionUtils.isNotEmpty(putAliasNodeList)) {
            return esIndexDAO.editAlias(cluster, putAliasNodeList,3);
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Void> deleteAliases(String cluster, String index, List<String> aliases) {
        if (!esIndexDAO.exist(cluster, index)) {
            return Result.buildParamIllegal(String.format("索引【%s】不存在", index));
        }
        if (CollectionUtils.isEmpty(aliases)) {
            return Result.buildParamIllegal("要操作的别名不存在");
        }
        List<PutAliasNode> putAliasNodeList = new ArrayList<>();
        Map<String, List<String>> aliasIndexNodeMap = syncGetIndexAliasesByIndices(cluster, index);
        Set<String> aliasSet = new HashSet<>();
        Optional.ofNullable(aliasIndexNodeMap.get(index)).map(aliasSet::addAll);
        Set<String> notExistsAlias = new HashSet<>();

        aliases.stream().filter(StringUtils::isNotBlank).forEach(aliasName -> {
            PutAliasNode putAliasNode = new PutAliasNode();
            putAliasNode.setIndex(index);
            putAliasNode.setAlias(aliasName);
            putAliasNode.setType(PutAliasType.REMOVE);
            if (!aliasSet.contains(aliasName)) {
                notExistsAlias.add(aliasName);
            }
            putAliasNodeList.add(putAliasNode);
        });
        if (!notExistsAlias.isEmpty()) {
            return Result.buildParamIllegal(String.format("要删除的别名【%s】不存在", StringUtils.join(notExistsAlias, ",")));
        }
        return esIndexDAO.editAlias(cluster, putAliasNodeList,3);
    }

    @Override
    public Result<Void> rollover(String cluster, String alias, String conditions) {
        return esIndexDAO.rollover(cluster, alias, conditions);
    }

    @Override
    public Result<Void> forceMerge(String cluster, String index, Integer maxNumSegments, Boolean onlyExpungeDeletes) {
        return esIndexDAO.forceMerge(cluster, index, maxNumSegments, onlyExpungeDeletes);
    }

    @Override
    public Result<Void> shrink(String cluster, String index, String targetIndex, String config) {
        return esIndexDAO.shrink(cluster, index, targetIndex, config);
    }

    @Override
    public Result<Void> split(String cluster, String index, String targetIndex, String config) {
        return esIndexDAO.split(cluster, index, targetIndex, config);
    }

    @Override
    public List<CatIndexResult> indicesDistribution(String cluster) {
        List<CatIndexResult> catIndexResultList = esIndexDAO.catIndices(cluster);
        return catIndexResultList;
    }

    @Override
    public List<IndexCatCell> buildIndexAliasesAndBlockInfo(String cluster, List<IndexCatCell> indexCatCellList) {
        if (CollectionUtils.isNotEmpty(indexCatCellList)) {
            List<String> indexNameList = indexCatCellList.stream().map(IndexCatCell::getIndex)
                .collect(Collectors.toList());
            Map<String, IndexConfig> name2IndexConfigMap = this.syncGetIndexSetting(cluster, indexNameList, 3);

            Map<String, List<String>> aliasMap = this.syncGetIndexAliasesByIndices(cluster,
                indexNameList.toArray(new String[0]));
            indexCatCellList.forEach(indexCatCell -> {
                indexCatCell.setAliases(aliasMap.getOrDefault(indexCatCell.getIndex(), Lists.newArrayList()));

                IndexConfig indexConfig = name2IndexConfigMap.get(indexCatCell.getIndex());
                Tuple<Boolean, Boolean> writeAndReadBlockFromMerge = getWriteAndReadBlock(indexConfig);

                indexCatCell
                    .setReadFlag(writeAndReadBlockFromMerge.getV1() != null && writeAndReadBlockFromMerge.getV1());
                indexCatCell
                    .setWriteFlag(writeAndReadBlockFromMerge.getV2() != null && writeAndReadBlockFromMerge.getV2());
            });
        } else {
            LOGGER.warn(
                "class=IndicesPageSearchHandle||method=buildBlockInfo||cluster={}||index={}||errMsg=index is empty",
                cluster);
        }
        return indexCatCellList;
    }



    /***************************************** private method ****************************************************/

    private Tuple<Boolean, Boolean> getWriteAndReadBlock(IndexConfig indexConfig) {
        Tuple<Boolean, Boolean> writeAndReadBlockFromMerge = new Tuple<>();
        //build from es setUp settings
        Tuple<Boolean, Boolean> writeAndReadBlockFromSetUpSettingTuple = new Tuple<>();
        Optional.ofNullable(indexConfig).map(IndexConfig::getSettings).filter(MapUtils::isNotEmpty)
            .map(JSON::toJSONString).map(JSON::parseObject).ifPresent(settingsObj -> {
                writeAndReadBlockFromSetUpSettingTuple.setV1(settingsObj.getBoolean(READ));
                writeAndReadBlockFromSetUpSettingTuple.setV2(settingsObj.getBoolean(WRITE));
            });
        //build from es default settings
        Tuple<Boolean, Boolean> writeAndReadBlockFromDefaultSettingTuple = new Tuple<>();
        Optional.ofNullable(indexConfig).map(config -> config.getOther(DEFAULTS)).map(Object::toString)
            .map(JSON::parseObject).map(defaultObj -> defaultObj.getJSONObject(INDEX))
            .map(indexSettings -> indexSettings.getJSONObject(BLOCKS)).ifPresent(blocksObj -> {
                if (null != blocksObj.get(IndexBlockEnum.READ.getType())) {
                    writeAndReadBlockFromDefaultSettingTuple.setV1(blocksObj.getBoolean(IndexBlockEnum.READ.getType()));
                }
                if (null != blocksObj.get(IndexBlockEnum.WRITE.getType())) {
                    writeAndReadBlockFromDefaultSettingTuple
                        .setV2(blocksObj.getBoolean(IndexBlockEnum.WRITE.getType()));
                }
            });

        //set read block info
        if (null != writeAndReadBlockFromSetUpSettingTuple.getV1()) {
            writeAndReadBlockFromMerge.setV1(writeAndReadBlockFromSetUpSettingTuple.getV1());
        } else if (null != writeAndReadBlockFromDefaultSettingTuple.getV1()) {
            writeAndReadBlockFromMerge.setV1(writeAndReadBlockFromDefaultSettingTuple.getV1());
        }

        //set write block info
        if (null != writeAndReadBlockFromSetUpSettingTuple.getV2()) {
            writeAndReadBlockFromMerge.setV2(writeAndReadBlockFromSetUpSettingTuple.getV2());
        } else if (null != writeAndReadBlockFromDefaultSettingTuple.getV2()) {
            writeAndReadBlockFromMerge.setV2(writeAndReadBlockFromDefaultSettingTuple.getV2());
        }

        return writeAndReadBlockFromMerge;
    }

    private Result<Void> refreshIndex(String cluster, List<String> indexNames) {
        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
            .batchList(indexNames).batchSize(30).processor(items -> esIndexDAO.refreshIndex(cluster, items))
            .succChecker(succ -> succ).process();
        return Result.build(result.isSucc());
    }

    private boolean checkDateSame(String cluster1, String cluster2, List<String> indexNames) {
        Result<Void> refreshIndexResult1 = refreshIndex(cluster1, indexNames);
        if (refreshIndexResult1.failed()) {
            LOGGER.warn("class=ESIndexServiceImpl||method=ensureDateSame||cluster={}||indexNames={}||msg=refresh fail",
                cluster1, indexNames);
            return false;
        }

        Result<Void> refreshIndexResult2 = refreshIndex(cluster2, indexNames);
        if (refreshIndexResult2.failed()) {
            LOGGER.warn("class=ESIndexServiceImpl||method=ensureDateSame||cluster={}||indexNames={}||msg=refresh fail",
                cluster2, indexNames);
            return false;
        }

        Map<String, IndexNodes> indexStat1 = syncBatchGetIndices(cluster1, indexNames);
        Map<String, IndexNodes> indexStat2 = syncBatchGetIndices(cluster2, indexNames);

        for (String index : indexNames) {
            IndexNodes stat1 = indexStat1.get(index);
            IndexNodes stat2 = indexStat2.get(index);

            if (stat1 == null || stat2 == null) {
                LOGGER.warn("class=ESIndexServiceImpl||method=ensureDateSame||indexName={}||msg=index miss", index);
                return false;
            }

            if (stat1.getPrimaries().getDocs().getCount() != stat2.getPrimaries().getDocs().getCount()) {
                LOGGER.warn(
                    "class=ESIndexServiceImpl||method=ensureDateSame||indexName={}||msg=doc count not match, primary={}, replica={}",
                    index, stat1.getPrimaries().getDocs().getCount(), stat2.getPrimaries().getDocs().getCount());
                return false;
            }

            // 校验checkpoint
            AtomicBoolean checkpointEqualSeqNo = new AtomicBoolean(true);
            AtomicLong totalCheckpoint1 = syncGetTotalCheckpoint(index, stat1, checkpointEqualSeqNo);
            AtomicLong totalCheckpoint2 = syncGetTotalCheckpoint(index, stat2, checkpointEqualSeqNo);

            if (!checkpointEqualSeqNo.get()) {
                return false;
            }

            if (totalCheckpoint1.get() != totalCheckpoint2.get()) {
                LOGGER.warn(
                    "class=ESIndexServiceImpl||method=ensureDateSame||indexName={}|||msg=checkpoint not match, primary={}, replica={}",
                    index, totalCheckpoint1.get(), totalCheckpoint2.get());
                return false;
            }
        }

        return true;
    }

    private boolean batchDeleteIndicesInner(String cluster, String indices, int retryCount) {
        try {
            syncDeleteIndexByExpression(cluster, indices, retryCount);
            return true;
        } catch (ESOperateException e) {
            LOGGER.error("class=ESIndexServiceImpl||method=batchDeleteIndicesInner||cluster" + "={}||indices={}",
                cluster, indices, e);
        }
        return false;
    }

    private Integer getPrimaryShardNumber(Integer primaryShardNumber, Map<String, String> settings) {
        Integer shardNo = Integer.parseInt(settings.get("index.number_of_shards"));
        if (primaryShardNumber == null) {
            primaryShardNumber = shardNo;
        } else {
            // 得到多个索引时获取到最大的主shard个数
            primaryShardNumber = Math.max(primaryShardNumber, shardNo);
        }
        return primaryShardNumber;
    }
}