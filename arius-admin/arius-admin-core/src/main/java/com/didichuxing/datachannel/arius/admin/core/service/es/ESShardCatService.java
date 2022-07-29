package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.po.shard.ShardCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardDistributionVO;

import java.util.List;

public interface ESShardCatService {
    /**
     * shard分布
     * @param cluster
     * @return
     */
    List<ShardCatCellPO> syncShardDistribution(String cluster);

    /**
     * 分页获取shard信息
     * @param queryCluster
     * @param queryProjectId
     * @param keyword
     * @param from
     * @param size
     * @param sortTerm
     * @param orderByDesc
     * @return
     */
    Tuple<Long, List<ShardDistributionVO>> syncGetCatShardInfo(String queryCluster, Integer queryProjectId,String keyword, long from, Long size, String sortTerm, Boolean orderByDesc);

}
