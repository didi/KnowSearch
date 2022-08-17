package com.didichuxing.datachannel.arius.admin.metadata.job.shard;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.shard.ShardCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.util.BatchProcessor;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardCatService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESShardDAO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

/**
 * @author lyn
 * @date 2021/09/30
 **/
@Component
public class ShardCatInfoCollector extends AbstractMetaDataJob {

    private static final Integer RETRY_TIMES = 3;
    @Autowired
    private ClusterPhyService clusterPhyService;

    @Autowired
    private ESShardCatService esShardCatService;


    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private ESShardDAO esShardDAO;

    //key: cluster@indexName  value: indexName
    private final Cache<String, Object> notCollectorIndexNameCache = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(10000).build();
    private Map<Long, String>     logicClusterId2NameMap     = Maps.newConcurrentMap();

    private void initLogicClusterId2NameMap() {
        List<ClusterLogic> logicClusterList = clusterLogicService.listAllClusterLogics();
        if (CollectionUtils.isNotEmpty(logicClusterList)) {
            logicClusterId2NameMap = logicClusterList.stream()
                    .collect(Collectors.toConcurrentMap(ClusterLogic::getId, ClusterLogic::getName, (o1, o2) -> o1));
        }
    }

    @Override
    public Object handleJobTask(String params) {

        List<ShardCatCellPO> shardCatCellList     =   Lists.newArrayList();
        List<ClusterPhy>     clusterPhyList       =   clusterPhyService.listAllClusters();
        List<String>         clusterPhyNameList   =   clusterPhyList.stream().map(ClusterPhy::getCluster).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(clusterPhyNameList)) {
            return JOB_SUCCESS;
        }
        initLogicClusterId2NameMap();

        long currentTimeMillis = System.currentTimeMillis();
        BatchProcessor.BatchProcessResult<String, List<ShardCatCellPO>> batchResult
                = new BatchProcessor<String, List<ShardCatCellPO>>()
                .batchList(clusterPhyNameList)
                .processor(this::getShardInfoFromEs)
                .process();
        LOGGER.info("class=IndexCatInfoCollector||method=handleJobTask||timeOut={}", System.currentTimeMillis() - currentTimeMillis);

        if (!batchResult.isSucc() && (batchResult.getErrorMap().size() > 0)) {
            LOGGER.warn(
                    "class=IndexCatInfoCollector||method=handleJobTask||clusterList={}||errMsg=batch result error:{}",
                    ListUtils.strList2String(clusterPhyNameList), batchResult.getErrorMap());
        }
        List<List<ShardCatCellPO>> resultList = batchResult.getResultList();
        for (List<ShardCatCellPO> shardCatCellPOS : resultList) {
            shardCatCellList.addAll(shardCatCellPOS);
        }
        esShardCatService.syncInsertCatShard(shardCatCellList,RETRY_TIMES);
        return JOB_SUCCESS;
    }

    public List<ShardCatCellPO> getShardInfoFromEs(List<String> clusterNameList) {
        List<ShardCatCellPO> catShardCellList = Lists.newArrayList();
        for (String clusterName : clusterNameList) {
            List<ShardCatCellPO> shardCatCells = getShardCatCells(clusterName);
            if (null != shardCatCells && !shardCatCells.isEmpty()) {
                catShardCellList.addAll(shardCatCells);
            }
        }
        return catShardCellList;
    }

    private List<ShardCatCellPO> getShardCatCells(String clusterName) {
        long currentTimeMillis = System.currentTimeMillis();
        List<ShardCatCellPO> shardDistributionVOS = esShardCatService.syncShardDistribution(clusterName,currentTimeMillis);
        return shardDistributionVOS;
    }
}