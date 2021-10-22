package com.didichuxing.datachannel.arius.admin.core.service.es;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.index.MultiIndexsConfig;

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
     * 跟进索引名称获取索引的mapping
     * @param cluster    集群
     * @param index      索引名称
     * @return result
     */
    String syncGetIndexMapping(String cluster, String index);

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
     * 批量删除索引
     * @param cluster 集群
     * @param shouldDels 索引集合
     * @param retryCount
     * @return int  成功的个数
     */
    int syncBatchDeleteIndices(String cluster, Collection<String> shouldDels, int retryCount);

    /**
     * 删除文档
     * @param cluster 集群
     * @param delIndices 索引
     * @param delQueryDsl 删除语句
     * @return
     */
    boolean syncDeleteByQuery(String cluster, List<String> delIndices, String delQueryDsl) throws ESOperateException;

    /**
     * 修改表达式对应索引的rack
     * @param cluster cluster
     * @param indices 表达式
     * @param tgtRack tgtRack
     * @param retryCount 重试次数
     * @return true/false
     * @throws ESOperateException
     */
    boolean syncBatchUpdateRack(String cluster, List<String> indices, String tgtRack,
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
     * @param name 索引名称
     * @return
     */
    MultiIndexsConfig syncGetIndexConfigs(String cluster, String name);

    /**
     * cat index
     * @param cluster 集群
     * @param expression 表达式
     * @return list
     */
    List<CatIndexResult> syncCatIndexByExpression(String cluster, String expression);

    /**
     * 获取索引主shard个数
     * @param clusterName
     * @param indexName
     * @return
     */
    Integer getIndexPrimaryShardNumber(String clusterName, String indexName);

    /**
     * 获取索引的IndexNodes信息
     * @param clusterName
     * @param templateExp
     * @return
     */
    Map<String, IndexNodes> getIndexNodes(String clusterName, String templateExp);
}
