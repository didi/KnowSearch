package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyQuickCommandManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.*;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardService;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 快捷指令实现.
 *
 * @ClassName QuickCommandManagerImpl
 * @Author gyp
 * @Date 2022/6/1
 * @Version 1.0
 */
@Component
public class ClusterPhyQuickCommandManagerImpl implements ClusterPhyQuickCommandManager {

    @Autowired
    protected ClusterPhyService clusterPhyService;

    @Autowired
    protected ESClusterService esClusterService;
    @Autowired
    protected ESClusterNodeService esClusterNodeService;

    @Autowired
    protected ESIndexService esIndexService;
    @Autowired
    protected ESShardService esShardService;


    @Override
    public Result<List<NodeStateVO>> nodeStateAnalysis(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        return Result.buildSucc(esClusterNodeService.nodeStateAnalysis(cluster));
    }

    @Override
    public Result<List<IndicesDistributionVO>> indicesDistribution(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        // 把 List<CatIndexResult> 转为 List<IndicesDistributionVO>
        List<CatIndexResult> catIndexResultList = esIndexService.indicesDistribution(cluster);
        return Result.buildSucc( catIndexResultList.stream().map(catIndexResult -> {
            IndicesDistributionVO vo = new IndicesDistributionVO();
            BeanUtils.copyProperties(catIndexResult, vo);
            return vo;
        }).collect(Collectors.toList()));
    }

    @Override
    public Result<List<ShardDistributionVO>> shardDistribution(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        return Result.buildSucc(esShardService.shardDistribution(cluster));
    }

    @Override
    public Result<List<PendingTaskAnalysisVO>> pendingTaskAnalysis(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        List<PendingTaskAnalysisVO> vos = esClusterService.pendingTaskAnalysis(cluster);
        if (vos == null) {
            return Result.buildFail();
        }
        return Result.buildSucc(vos);
    }

    @Override
    public Result<List<TaskMissionAnalysisVO>> taskMissionAnalysis(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        List<TaskMissionAnalysisVO> vos = esClusterService.taskMissionAnalysis(cluster);
        if (vos == null) {
            return Result.buildFail();
        }
        return Result.buildSucc(vos);
    }

    @Override
    public Result<String> hotThreadAnalysis(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        return Result.buildSucc(esClusterService.hotThreadAnalysis(cluster));
    }

    @Override
    public Result<ShardAssignmentDescriptionVO> shardAssignmentDescription(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        ShardAssignmentDescriptionVO vo = esShardService.shardAssignmentDescription(cluster);
        if (vo == null) {
            return Result.buildFail();
        }
        return Result.buildSucc(vo);
    }

    @Override
    public Result<Void> abnormalShardAllocationRetry(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        boolean result = esClusterService.abnormalShardAllocationRetry(cluster);
        if (result) {
            return Result.buildSucc();
        }
        return Result.buildFail();
    }

    @Override
    public Result<Void> clearFieldDataMemory(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        boolean result = esClusterService.clearFieldDataMemory(cluster);
        if (result) {
            return Result.buildSucc();
        }
        return Result.buildFail();
    }

    private Result<Void> checkClusterExistence(String cluster) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(cluster);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail(String.format("集群[%s]不存在", cluster));
        }
        return Result.buildSucc();
    }
}