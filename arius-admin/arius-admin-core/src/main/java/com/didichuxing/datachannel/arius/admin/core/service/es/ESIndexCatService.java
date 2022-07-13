package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexShardInfo;
import java.util.List;

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
     * @return Tuple<Long, List < IndexCatCell>>   key1 -> 命中总数, key2 索引列表
     */
    Tuple<Long, List<IndexCatCell>> syncGetCatIndexInfo(String cluster, String index, String health, String status,
                                                        Integer projectId, Long from, Long size, String sortTerm,
                                                        Boolean orderByDesc);

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
    List<IndexShardInfo> syncGetIndexShardInfo(String cluster, String indexName);
}