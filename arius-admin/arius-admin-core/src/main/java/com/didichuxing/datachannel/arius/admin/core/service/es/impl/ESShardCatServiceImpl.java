package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.po.shard.ShardCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardDistributionVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardCatService;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESShardDAO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 详细介绍类情况.
 *
 * @ClassName ESShardCatServiceImpl
 * @Author gyp
 * @Date 2022/7/11
 * @Version 1.0
 */
@Service
public class ESShardCatServiceImpl implements ESShardCatService {
    @Autowired
    private ESShardDAO esShardDAO;

    @Override
    public List<ShardCatCellPO> shardDistribution(String cluster) {
        List<ShardCatCellPO> shardCatCellPOS = esShardDAO.catShard(cluster);
        shardCatCellPOS.forEach(shardCatCellPO -> {
            shardCatCellPO.setClusterPhy(cluster);
        });
        return shardCatCellPOS;
    }

    @Override
    public Tuple<Long, List<ShardDistributionVO>> syncGetCatShardInfo(String queryCluster, Integer queryProjectId, String keyword, long from, Long size, String sortTerm, Boolean orderByDesc) {
        Tuple<Long, List<ShardCatCellPO>> hitTotal2catIndexInfoTuplePO = esShardDAO.getCatShardInfo(queryCluster,
                queryProjectId, keyword, from, size, sortTerm, orderByDesc);
        if (null == hitTotal2catIndexInfoTuplePO) {
            return null;
        }

        Tuple<Long, List<ShardDistributionVO>> hitTotal2catIndexInfoTuple = new Tuple<>();
        hitTotal2catIndexInfoTuple.setV1(hitTotal2catIndexInfoTuplePO.getV1());
        hitTotal2catIndexInfoTuple.setV2(buildShardCatCell(hitTotal2catIndexInfoTuplePO.getV2()));
        return hitTotal2catIndexInfoTuple;
    }

    private List<ShardDistributionVO> buildShardCatCell(List<ShardCatCellPO> v2) {
        
        return ConvertUtil.list2List(v2,ShardDistributionVO.class);
    }
}