package com.didichuxing.datachannel.arius.admin.core.service.es;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexShardInfo;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

public interface ESIndexCatService {

    /**
     * 分页获取CatIndex信息
     *
     * @param cluster     cluster
     * @param index       索引名称
     * @param health      索引健康状态
     * @param projectId   应用id
     * @param from        起始点
     * @param size        当前页数量
     * @param sortTerm    排序字段
     * @param orderByDesc 降序标识
     * @param showMetadata 是否展示元数据信息
     * @return Tuple<Long, List < IndexCatCell>>   key1 -> 命中总数, key2 索引列表
     */
    Tuple<Long, List<IndexCatCell>> syncGetCatIndexInfo(String cluster, String index, String health, String status,
                                                        Integer projectId, Long from, Long size, String sortTerm,
                                                        Boolean orderByDesc, Boolean showMetadata);

    /**
     * 更新索引删除标识
     * @param cluster           集群名称
     * @param indexNameList     索引名称
     * @param retryCount        重试次数
     * @return
     */
    int syncUpdateCatIndexDeleteFlag(String cluster, List<String> indexNameList, int retryCount);

    /**
     * 更新索引关闭标识
     * @param cluster           集群名称
     * @param indexNameList     索引名称
     * @param status    "open" 开启索引标识，"close" 关闭索引标识
     * @param retryCount        重试次数
     * @return
     */
    int syncUpdateCatIndexStatus(String cluster, List<String> indexNameList, String status, int retryCount);

    /**
     * 获取索引shard(主)在节点中的分布详情
     * @param cluster   集群名称
     * @param indexName 索引名称
     * @return
     */
    List<IndexShardInfo> syncGetIndexShardInfo(String cluster, String indexName) throws ESOperateException;


    /**
     * 同步插入文档至cat_index元数据索引
     * @param params         入参
     * @param retryCount     重试次数
     * @return               Boolean
     */
    Boolean syncInsertCatIndex(List<IndexCatCellDTO> params, int retryCount);
    Boolean syncUpsertCatIndex(List<IndexCatCellDTO> params, int retryCount);

    /**
     * 获取通过平台创建的索引(不经过模板)IndexCatCellDTO信息，作用于平台索引管理新建索引侧
     */
     List<IndexCatCell> syncGetPlatformCreateCatIndexList( Integer searchSize);

    /**
     * 根据逻辑集群获取索引
     * @param name
     * @return
     */
    List<IndexCatCellDTO> syncGetIndexByCluster(String name, Integer projectId);
    
    Result<List<IndexCatCellDTO>> syncGetSegmentsIndexList(String cluster, Collection<String> indexList);
    
    List<String> syncGetIndexListByProjectId(Integer projectId,
                                             String clusterLogic);
    
    Map</*clusterPhy*/String,/*docCount*/Integer> syncGetByClusterPhyList(List<String> clusterPhyList);
    
    IndexCatCell syncGetCatIndexInfoById(/* clusterPhy*/String clusterPhy,/*IndexName*/ String index);
    
   
    /**
     * 通过projectId和模糊索引获取索引列表
     *
     * @param projectId 项目编号
     * @param clusterLogicName 集群名称，即配置文件中的集群名称。
     * @param index 索引名称
     * @return List<String>
     */
    List<String> syncGetIndexListByProjectIdAndFuzzyIndexAndClusterLogic(Integer projectId, String clusterLogicName, String index);

    /**
     * 通过项目id、集群物理名、模糊索引获取索引列表
     *
     * @param clusterPhyName 集群的物理名称，例如“es-cluster-01”
     * @param index          索引名称
     * @return List<String>
     */
    List<String> syncGetIndexListByProjectIdAndFuzzyIndexAndClusterPhy( String clusterPhyName,
                                                                       String index);

    List<IndexCatCell> syncGetAllCatIndexNameListByClusters(Integer searchSize,List<String> phyClusterNames);
}