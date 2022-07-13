package com.didichuxing.datachannel.arius.admin.core.service.es;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.IndicesDistributionVO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.MultiIndexsConfig;

/**
 * @author d06679
 * @date 2019/4/2
 */
public interface ESIndexService {

    /**
     * 同步创建索引
     * @param cluster 集群名称
     * @param indexName 索引名称
     * @param retryCount 重试次数
     * @return
     * @throws ESOperateException
     */
    boolean syncCreateIndex(String cluster, String indexName, int retryCount) throws ESOperateException;

    /**
     * 同步创建索引
     * @param cluster 集群名称
     * @param indexName 索引名称
     * @param retryCount 重试次数
     * @param indexConfig 索引配置
     * @return
     * @throws ESOperateException
     */
    boolean syncCreateIndex(String cluster, String indexName, IndexConfig indexConfig,
                            int retryCount) throws ESOperateException;

    /**
     * 同步删除索引
     * @param cluster
     * @param indexName
     * @param retryCount
     * @return
     * @throws ESOperateException
     */
    boolean syncDelIndex(String cluster, String indexName, int retryCount) throws ESOperateException;

    /**
     * 根据表达式删除索引
     * @param cluster    集群
     * @param expression 表达式
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    boolean syncDeleteIndexByExpression(String cluster, String expression, int retryCount) throws ESOperateException;

    /**
     * 根据索引名称获取索引的mapping
     * @param cluster    集群
     * @param index      索引名称
     * @return result
     */
    String syncGetIndexMapping(String cluster, String index);

    /**
     * 更新索引mapping
     * @param cluster
     * @param index
     * @param mappingConfig
     * @return
     */
    boolean syncUpdateIndexMapping(String cluster, String index, MappingConfig mappingConfig);

    Map<String, IndexConfig> syncBatchGetIndexConfig(String cluster, List<String> indexList);

    /**
     * 查询集群中的索引
     * @param cluster    集群
     * @param expression 表达式
     * @return 索引集合
     */
    Set<String> syncGetIndexNameByExpression(String cluster, String expression);

    /**
     * 同步
     * @param cluster 集群
     * @param indices 索引列表
     * @param settingName setting名称
     * @param settingValue setting值
     * @param defaultValue 默认值
     * @return
     */
    boolean syncPutIndexSetting(String cluster, List<String> indices, String settingName, String settingValue,
                                String defaultValue, int retryCount) throws ESOperateException;

    /**
     * 更新setting
     * @param cluster
     * @param indices
     * @param settingMap
     * @param retryCount
     * @return
     */
    boolean syncPutIndexSetting(String cluster, List<String> indices, Map<String, String> settingMap,
                                Integer retryCount) throws ESOperateException;

    /**
     * 批量修改多个索引的相同的多个setting
     * @param cluster 物理集群名称
     * @param indices 索引列表
     * @param settings key：setting名称 value：setting数值
     * @param retryCount 重试次数
     */
    boolean syncPutIndexSettings(String cluster, List<String> indices,
                                 Map</*setting名称*/String, /*setting数值*/String> settings,
                                 int retryCount) throws ESOperateException;

    /**
     * 获取索引信息
     * @param cluster 集群
     * @param expression 表达式
     * @return result
     */
    Map<String, IndexNodes> syncGetIndexByExpression(String cluster, String expression);

    /**
     * 获取索引信息
     * @param cluster 集群
     * @param indexNames 索引列表
     * @return result
     */
    Map<String, IndexNodes> syncBatchGetIndices(String cluster, Collection<String> indexNames);

    /**
     * 查询集群中的别名
     * @param cluster    集群
     * @param expression 表达式
     * @return 索引集合
     */
    List<Tuple<String, String>> syncGetIndexAliasesByExpression(String cluster, String expression);

    /**
     * 查询集群中的别名
     *
     * @param cluster 集群
     * @param indices 索引列表
     * @return {@link Map}<{@link String}, {@link List}<{@link String}>>
     */
    Map<String, List<String>> syncGetIndexAliasesByIndices(String cluster, String... indices);

    /**
     * 批量删除索引
     * @param cluster 集群
     * @param shouldDels 索引集合
     * @param retryCount
     * @return int  成功的个数
     */
    int syncBatchDeleteIndices(String cluster, Collection<String> shouldDels, int retryCount);

    /**
     * 批量关闭索引
     * @param cluster 集群
     * @param shouldCloses 索引集合
     * @param retryCount 重试次数
     * @return boolean
     */
    boolean syncBatchCloseIndices(String cluster, List<String> shouldCloses, int retryCount) throws ESOperateException;

    /**
     * 批量开启索引
     * @param cluster 集群
     * @param shouldOpens 索引集合
     * @param retryCount 重试次数
     * @return boolean
     */
    boolean syncBatchOpenIndices(String cluster, List<String> shouldOpens, int retryCount) throws ESOperateException;

    /**
     * 删除文档
     * @param cluster 集群
     * @param delIndices 索引
     * @param delQueryDsl 删除语句
     * @return boolean
     */
    boolean syncDeleteByQuery(String cluster, List<String> delIndices, String delQueryDsl) throws ESOperateException;

    /**
     * 修改索引的region
     * @param cluster
     * @param indices
     * @param tgtRegionId
     * @param retryCount
     * @return
     * @throws ESOperateException
     */
    boolean syncBatchUpdateRegion(String cluster, List<String> indices, Integer tgtRegionId,
                                  int retryCount) throws ESOperateException;

    /**
     * 修改索引只读配置
     * @param cluster 集群
     * @param indices 索引
     * @param block 配置
     * @param retryCount 重试次数
     * @return succCount
     */
    boolean syncBatchBlockIndexWrite(String cluster, List<String> indices, boolean block,
                                     int retryCount) throws ESOperateException;

    /**
     * 修改索引只写配置
     * @param cluster 集群
     * @param indices 索引
     * @param block 配置
     * @param retryCount 重试次数
     * @return succCount
     */
    boolean syncBatchBlockIndexRead(String cluster, List<String> indices, boolean block,
                                    int retryCount) throws ESOperateException;

    /**
     * 校验索引数据是否一致
     * @param cluster1 集群1
     * @param cluster2 集群2
     * @param indexNames 索引名字
     * @return true/false
     */
    boolean ensureDateSame(String cluster1, String cluster2, List<String> indexNames);

    /**
     * close and open index
     * @param cluster 集群
     * @param indices 索引
     * @return result
     */
    boolean reOpenIndex(String cluster, List<String> indices, int retryCount) throws ESOperateException;

    /**
     * 获取索引配置
     * @param cluster 集群名称
     * @param name    索引名称
     * @return
     */
    MultiIndexsConfig syncGetIndexConfigs(String cluster, String name);

    /**
     * 根据集群名称和索引名称获取索引setting信息
     * @param cluster           集群名称
     * @param indexNames        索引列表
     * @param tryTimes          重试次数
     * @return
     */
    Map<String, IndexConfig> syncGetIndexSetting(String cluster, List<String> indexNames, int tryTimes);

    /**
     * cat index
     * @param cluster 集群
     * @param expression 表达式
     * @return list
     */
    List<CatIndexResult> syncCatIndexByExpression(String cluster, String expression);

    /**
     * 获取指定集群全量索引列表信息
     * @param cluster  集群名称
     * @param tryTimes          重试次数
     * @return List<CatIndexResult>
     */
    List<CatIndexResult> syncCatIndex(String cluster, int tryTimes);

    /**
     * 同步获取索引Segment数量
     *
     * @param clusterPhyName 物理集群
     * @return {@link Map}<{@link String}, {@link Tuple}<{@link Long}, {@link Long}>>
     */
    Map<String, Tuple<Long, Long>> syncGetIndicesSegmentCount(String clusterPhyName);

    /**
     * 获取索引主shard个数
     * @param cluster
     * @param indexName
     * @return
     */
    Integer syncGetIndexPrimaryShardNumber(String cluster, String indexName);

    /**
     * 获取索引的IndexNodes信息
     * @param cluster
     * @param templateExp
     * @return
     */
    Map<String, IndexNodes> syncGetIndexNodes(String cluster, String templateExp);

    /**
     * 获取原生集群索引名称列表,不包含特殊索引（带.开头）
     */
    List<String> syncGetIndexName(String clusterName);

    /**
     * 索引是否存在
     * @param cluster   集群名称
     * @param indexName 索引名称
     * @return
     */
    boolean syncIsIndexExist(String cluster, String indexName);

    /**
     * 获取索引checkpoint
     * @param index                  索引名称
     * @param stat                   索引节点信息
     * @param checkpointEqualSeqNo   位点是否相同标识
     * @return
     */
    AtomicLong syncGetTotalCheckpoint(String index, IndexNodes stat, AtomicBoolean checkpointEqualSeqNo);

    /**
     * 新增别名
     *
     * @param cluster       集群
     * @param index         索引
     * @param aliases    要添加别名
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> addAliases(String cluster, String index, List<String> aliases);

    /**
     * 删除别名
     *
     * @param cluster       集群
     * @param index         索引
     * @param aliases 要删除别名
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> deleteAliases(String cluster, String index, List<String> aliases);

    /**
     * rollover
     *
     * @param cluster    集群
     * @param alias      别名
     * @param conditions 条件
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> rollover(String cluster, String alias, String conditions);

    /**
     * forceMerge
     * @param cluster
     * @param index
     * @param maxNumSegments
     * @param onlyExpungeDeletes
     * @return
     */
    Result<Void> forceMerge(String cluster, String index, Integer maxNumSegments, Boolean onlyExpungeDeletes);

    /**
     * shrink
     * @param cluster
     * @param index
     * @param targetIndex
     * @param config
     * @return
     */
    Result<Void> shrink(String cluster, String index, String targetIndex, String config);

    /**
     * split
     * @param cluster
     * @param index
     * @param targetIndex
     * @param config
     * @return
     */
    Result<Void> split(String cluster, String index, String targetIndex, String config);

    /**
     * indices分布
     * @param cluster
     * @return
     */
    List<CatIndexResult> indicesDistribution(String cluster);

    /**
     * 构建索引实时数据(包含block和aliases)
     * @param cluster          集群
     * @param indexCatCellList 索引cat/index基本信息
     */
    List<IndexCatCell> buildIndexAliasesAndBlockInfo(String cluster, List<IndexCatCell> indexCatCellList);
}
