package com.didichuxing.datachannel.arius.admin.biz.indices;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesBlockSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesOpenOrCloseDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexShardInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;

/**
 * @author lyn
 * @date 2021/09/28
 **/
public interface IndicesManager {
    /**
     * 条件获取索引列表信息 ,携带可读可写标志位
     * @param condition     查询条件
     * @param projectId         项目
     * @return              List<IndexCatInfoVO>
     */
    PaginationResult<IndexCatCellVO> pageGetIndexCatInfoVO(IndicesConditionDTO condition, Integer projectId);

    /**
     * 删除索引
     * @param params     删除索引信息
     * @param projectId      项目
     * @param operator   操作人
     * @return           Boolean
     */
    Result<Boolean> batchDeleteIndex(List<IndicesClearDTO> params, Integer projectId, String operator);

    /**
     * 关闭索引
     * @param params         关闭索引信息
     * @param indexNewStatus true 开启索引标识，false 关闭索引标识
     * @param projectId          项目
     * @param operator       操作人
     * @return               Boolean
     */
    Result<Boolean> batchUpdateIndexStatus(List<IndicesOpenOrCloseDTO> params, boolean indexNewStatus, Integer projectId, String operator);

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
     * @param projectId     项目
     * @param operator  操作人
     * @return          Boolean
     */
    Result<Boolean> batchEditIndexBlockSetting(List<IndicesBlockSettingDTO> params, Integer projectId, String operator);

    /**
     * 获取索引mapping
     * @param clusterPhyName     集群名称
     * @param indexName          索引名称
     * @param projectId              项目
     * @return                   IndexMappingVO
     */
    Result<IndexMappingVO> getIndexMapping(String clusterPhyName, String indexName, Integer projectId);

    /**
     * 获取索引setting信息
     * @param clusterPhyName    集群名称
     * @param indexName         索引名称
     * @param projectId             项目
     * @return                  IndexSettingVO
     */
    Result<IndexSettingVO> getIndexSetting(String clusterPhyName, String indexName, Integer projectId);

    /**
     * 获取索引shard分配信息
     * @param clusterPhyName    集群
     * @param indexName         索引名称
     * @param projectId             项目
     * @return
     */
    Result<List<IndexShardInfoVO>> getIndexShardsInfo(String clusterPhyName, String indexName, Integer projectId);

    /**
     * 获取单个索引的详情信息
     * @param clusterPhyName 物理集群名称
     * @param indexName 索引名称
     * @param projectId 项目
     * @return
     */
    Result<IndexCatCellVO> getIndexCatInfo(String clusterPhyName, String indexName, Integer projectId);
}