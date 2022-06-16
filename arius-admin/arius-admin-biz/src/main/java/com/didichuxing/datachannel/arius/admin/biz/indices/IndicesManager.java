package com.didichuxing.datachannel.arius.admin.biz.indices;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesBlockSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage.IndexCatCellWithConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv.IndexForceMergeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv.IndexRolloverDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexShardInfoVO;

/**
 * @author lyn
 * @date 2021/09/28
 **/
public interface IndicesManager {
    /**
     * 条件获取索引列表信息 ,携带可读可写标志位
     * @param condition     查询条件
     * @param appId         项目
     * @return              List<IndexCatInfoVO>
     */
    PaginationResult<IndexCatCellVO> pageGetIndex(IndexQueryDTO condition, Integer appId);

    /**
     * 创建索引
     * @param indexCreateDTO
     * @param appId
     * @return
     */
    Result<Void> createIndex(IndexCatCellWithConfigDTO indexCreateDTO, Integer appId);

    /**
     * 删除索引
     * @param params     删除索引信息
     * @param appId      项目
     * @param operator   操作人
     * @return           Boolean
     */
    Result<Void> deleteIndex(List<IndexCatCellDTO> params, Integer appId, String operator);

    /**
     * 关闭索引
     * @param params         关闭索引信息
     * @param indexNewStatus true 开启索引标识，false 关闭索引标识
     * @param appId          项目
     * @param operator       操作人
     * @return               Boolean
     */
    Result<Boolean> batchUpdateIndexStatus(List<IndexCatCellDTO> params, boolean indexNewStatus, Integer appId, String operator);

    /**
     * 配合删除真实集群索引使用
     *
     * 1. 批量设置存储索引cat/index信息的元数据索引中的文档标志位（deleteFlag）为true
     * 2. 采集任务不采集已删除索引
     *
     * @param cluster          物理集群
     * @param indexNameList    索引名称列表
     * @return
     */
    Result<Boolean> batchSetIndexFlagInvalid(String cluster, List<String> indexNameList);

    /**
     * 编辑索引setting阻塞信息
     * @param params    索引信息列表
     * @param appId     项目
     * @param operator  操作人
     * @return          Boolean
     */
    Result<Boolean> batchEditIndexBlockSetting(List<IndicesBlockSettingDTO> params, Integer appId, String operator);

    /**
     * 获取索引mapping
     *
     * @param cluster   集群
     * @param indexName 索引名称
     * @param appId     项目
     * @return IndexMappingVO
     */
    Result<IndexMappingVO> getMapping(String cluster, String indexName, Integer appId);

    /**
     * 更新索引mapping
     * @param param
     * @param appId
     * @return
     */
    Result<Void> editMapping(IndexCatCellWithConfigDTO param, Integer appId);

    /**
     * 获取索引setting信息
     *
     * @param appId     项目
     * @param cluster   集群
     * @param indexName 索引名称
     * @return IndexSettingVO
     */
    Result<IndexSettingVO> getSetting(String cluster, String indexName, Integer appId);

    /**
     * 更新索引setting
     * @param param
     * @param appId
     * @return
     */
    Result<Void> editSetting(IndexCatCellWithConfigDTO param, Integer appId);

    /**
     * 获取索引shard分配信息
     * @param cluster 集群
     * @param indexName         索引名称
     * @param appId             项目
     * @return
     */
    Result<List<IndexShardInfoVO>> getIndexShardsInfo(String cluster, String indexName, Integer appId);

    /**
     * 获取单个索引的详情信息
     * @param cluster 集群名称
     * @param indexName 索引名称
     * @param appId 项目
     * @return
     */
    Result<IndexCatCellVO> getIndexCatInfo(String cluster, String indexName, Integer appId);

    /**
     * 编辑别名
     * @param param
     * @param editFlag True 新增别名， False 删除别名，若未指定别名，则默认删除所有别名
     * @return
     */
    Result<Void> editAlias(IndexCatCellWithConfigDTO param, Boolean editFlag, Integer appId);

    /**
     * rollover
     * @param param
     * @return
     */
    Result<Void> rollover(IndexRolloverDTO param);

    /**
     * shrink
     * @param param
     * @return
     */
    Result<Void> shrink(IndexCatCellWithConfigDTO param);

    /**
     * split
     * @param param
     * @return
     */
    Result<Void> split(IndexCatCellWithConfigDTO param);

    /**
     * forceMerge
     * @param param
     * @return
     */
    Result<Void> forceMerge(IndexForceMergeDTO param);

    /**
     * 获取物理集群中的索引列表
     */
    Result<List<String>> getClusterPhyIndexName(String clusterPhyName, Integer appId);

    /**
     * 获取逻辑集群下的索引列表
     * @param clusterLogicName
     * @param appId
     * @return
     */
    Result<List<String>> getClusterLogicIndexName(String clusterLogicName, Integer appId);

    /**
     * 判断索引是否存在
     *
     * @param cluster   集群
     * @param indexName 索引名称
     * @param appId     应用程序id
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> isExists(String cluster, String indexName, Integer appId);
}
