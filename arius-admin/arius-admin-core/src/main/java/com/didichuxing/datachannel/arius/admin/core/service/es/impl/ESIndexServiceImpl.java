package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.getBigIndicesRequestContent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexResponse;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.BatchProcessor;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESIndexDAO;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.AliasIndexNode;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author d06679
 * @date 2019/4/2
 */
@Service
public class ESIndexServiceImpl implements ESIndexService {

    private static final ILog LOGGER = LogFactory.getLog(ESIndexServiceImpl.class);

    @Autowired
    private ESIndexDAO        esIndexDAO;

    @Override
    public boolean syncCreateIndex(String cluster, String indexName, int retryCount) throws ESOperateException {
        return createIndexInner(cluster, indexName, retryCount);
    }

    @Override
    public boolean syncDelIndex(String cluster, String indexName, int retryCount) throws ESOperateException {
        return deleteIndexInner(cluster, indexName, retryCount);
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
            LOGGER.warn("class=ESIndexServiceImpl||method=syncBatchDeleteIndices||cluster={}||shouldDels={}||result={}", cluster, shouldDels,
                result);
        }

        return shouldDels.size() - result.getFailAndErrorCount();
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

    /**
     * 修改表达式对应索引的rack
     *
     * @param cluster    cluster
     * @param indices 表达式
     * @param tgtRack tgtRack
     * @param retryCount 重试次数
     * @return true/false
     * @throws ESOperateException
     */
    @Override
    public boolean syncBatchUpdateRack(String cluster, List<String> indices, String tgtRack,
                                       int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("syncUpdateRackByExpression", retryCount,
            () -> esIndexDAO.batchUpdateIndexRack(cluster, indices, tgtRack));
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
        int retryCount = 20;
        while (retryCount-- > 0) {
            try {
                Thread.sleep(5000);
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
    public List<CatIndexResult> syncCatIndex(String clusterPhyName) {
        List<CatIndexResult> catIndexResults = esIndexDAO.catIndices(clusterPhyName);
        return catIndexResults.stream().filter(this::filterOriginalIndices).collect(Collectors.toList());
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
        return indexResponses.stream().map(IndexResponse::getIndex).filter(index -> !index.startsWith("."))
            .collect(Collectors.toList());
    }

    @Override
    public boolean syncIsIndexExist(String cluster, String indexName) {
        return esIndexDAO.existByClusterAndIndexName(cluster, indexName);
    }

    /***************************************** private method ****************************************************/
    private Result<Void> refreshIndex(String cluster, List<String> indexNames) {
        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
            .batchList(indexNames).batchSize(30).processor(items -> esIndexDAO.refreshIndex(cluster, items))
            .succChecker(succ -> succ).process();
        return Result.build(result.isSucc());
    }

    private boolean checkDateSame(String cluster1, String cluster2, List<String> indexNames) {
        Result<Void> refreshIndexResult1 = refreshIndex(cluster1, indexNames);
        if (refreshIndexResult1.failed()) {
            LOGGER.warn("class=ESIndexServiceImpl||method=ensureDateSame||cluster={}||indexNames={}||msg=refresh fail", cluster1, indexNames);
            return false;
        }

        Result<Void> refreshIndexResult2 = refreshIndex(cluster2, indexNames);
        if (refreshIndexResult2.failed()) {
            LOGGER.warn("class=ESIndexServiceImpl||method=ensureDateSame||cluster={}||indexNames={}||msg=refresh fail", cluster2, indexNames);
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
                LOGGER.warn("class=ESIndexServiceImpl||method=ensureDateSame||indexName={}||msg=doc count not match, primary={}, replica={}",
                    index, stat1.getPrimaries().getDocs().getCount(), stat2.getPrimaries().getDocs().getCount());
                return false;
            }

            // 校验checkpoint
            AtomicBoolean checkpointEqualSeqNo = new AtomicBoolean(true);
            AtomicLong totalCheckpoint1 = getTotalCheckpoint1(index, stat1, checkpointEqualSeqNo);
            AtomicLong totalCheckpoint2 = getTotalCheckpoint2(index, stat2, checkpointEqualSeqNo);

            if (!checkpointEqualSeqNo.get()) {
                return false;
            }

            if (totalCheckpoint1.get() != totalCheckpoint2.get()) {
                LOGGER.warn("class=ESIndexServiceImpl||method=ensureDateSame||indexName={}|||msg=checkpoint not match, primary={}, replica={}",
                    index, totalCheckpoint1.get(), totalCheckpoint2.get());
                return false;
            }
        }

        return true;
    }

    private AtomicLong getTotalCheckpoint2(String index, IndexNodes stat2, AtomicBoolean checkpointEqualSeqNo) {
        AtomicLong totalCheckpoint2 = new AtomicLong(0);
        stat2.getShards().forEach((shard, v) -> v.forEach(commonStat -> {
            if (!commonStat.getRouting().isPrimary()) {
                return;
            }

            if (commonStat.getSeqNo().getMaxSeqNo() != commonStat.getSeqNo().getGlobalCheckpoint()) {
                LOGGER.warn(
                    "class=ESIndexServiceImpl||method=ensureDateSame||indexName={}||shard={}||msg=replica maxSeqNo({})!=globalCheckpoint({})",
                    index, shard, commonStat.getSeqNo().getMaxSeqNo(), commonStat.getSeqNo().getGlobalCheckpoint());
                checkpointEqualSeqNo.set(false);
            }

            totalCheckpoint2.addAndGet(commonStat.getSeqNo().getGlobalCheckpoint());
        }));
        return totalCheckpoint2;
    }

    private AtomicLong getTotalCheckpoint1(String index, IndexNodes stat1, AtomicBoolean checkpointEqualSeqNo) {
        AtomicLong totalCheckpoint1 = new AtomicLong(0);
        stat1.getShards().forEach((shard, v) -> v.forEach(commonStat -> {
            if (!commonStat.getRouting().isPrimary()) {
                return;
            }

            if (commonStat.getSeqNo().getMaxSeqNo() != commonStat.getSeqNo().getGlobalCheckpoint()) {
                LOGGER.warn(
                    "class=ESIndexServiceImpl||method=ensureDateSame||indexName={}||shard={}||msg=primary maxSeqNo({})!=globalCheckpoint({})",
                    index, shard, commonStat.getSeqNo().getMaxSeqNo(), commonStat.getSeqNo().getGlobalCheckpoint());
                checkpointEqualSeqNo.set(false);
            }

            totalCheckpoint1.addAndGet(commonStat.getSeqNo().getGlobalCheckpoint());
        }));
        return totalCheckpoint1;
    }

    private boolean createIndexInner(String cluster, String indexName, int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("createIndex", retryCount,
            () -> esIndexDAO.createIndex(cluster, indexName));
    }

    private boolean deleteIndexInner(String cluster, String indexName, int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("deleteIndex", retryCount,
            () -> esIndexDAO.deleteIndex(cluster, indexName));
    }

    private boolean batchDeleteIndicesInner(String cluster, String indices, int retryCount) {
        try {
            syncDeleteIndexByExpression(cluster, indices, retryCount);
            return true;
        } catch (ESOperateException e) {
            LOGGER.info("class=ESIndexServiceImpl||method=batchDeleteIndicesInner||cluster={}||indices={}", cluster,
                indices);
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
