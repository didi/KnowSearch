package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.po.shard.ShardCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardDistributionVO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.BatchProcessor;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardCatService;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESShardDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private static final ILog LOGGER = LogFactory.getLog(ESIndexCatService.class);
    @Autowired
    private ESShardDAO esShardDAO;

    @Override
    public List<ShardCatCellPO> syncShardDistribution(String cluster,long currentTimeMillis) throws ESOperateException {
        List<ShardCatCellPO> shardCatCellPOS = esShardDAO.catShard(cluster);
        shardCatCellPOS.forEach(shardCatCellPO -> {
            shardCatCellPO.setClusterPhy(cluster);
            shardCatCellPO.setTimestamp(currentTimeMillis);
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

    @Override
    public Boolean syncInsertCatShard(List<ShardCatCellPO> params, int retryCount) {
        BatchProcessor.BatchProcessResult<ShardCatCellPO, Boolean> result = new BatchProcessor<ShardCatCellPO, Boolean>().batchList(
                        params).batchSize(5000).processor(
                        items -> esShardDAO.batchInsert(ConvertUtil.list2List(params, ShardCatCellPO.class)))
                .succChecker(succ -> succ).process();

        if (!result.isSucc()) {
            List<String> clusterList = params.stream().map(ShardCatCellPO::getClusterPhy).distinct()
                    .collect(Collectors.toList());
            LOGGER.error(
                    "class=ESShardCatServiceImpl||method=syncInsertCatShard||cluster={}||errMsg=failed to batchInsert, batch total count = {}, batch failed count={}",
                    ListUtils.strList2String(clusterList),  params.size(),
                    result.getFailAndErrorCount());
        }

        return result.isSucc();
    }

    private List<ShardDistributionVO> buildShardCatCell(List<ShardCatCellPO> v2) {
        List<ShardDistributionVO> shardDistributionVOList = new ArrayList<>();
        v2.forEach(cell->{
            ShardDistributionVO shardDistributionVO = new ShardDistributionVO();
            shardDistributionVO.setDocs(cell.getDocs());
            shardDistributionVO.setIndex(cell.getIndex());
            shardDistributionVO.setIp(cell.getIp());
            shardDistributionVO.setNode(cell.getNode());
            shardDistributionVO.setPrirep(cell.getPrirep());
            shardDistributionVO.setShard(String.valueOf(cell.getShard()));
            shardDistributionVO.setState(cell.getState());
            shardDistributionVO.setStore(SizeUtil.getUnitSize(cell.getStore()));
            shardDistributionVOList.add(shardDistributionVO);
        });
        return shardDistributionVOList;
    }
}