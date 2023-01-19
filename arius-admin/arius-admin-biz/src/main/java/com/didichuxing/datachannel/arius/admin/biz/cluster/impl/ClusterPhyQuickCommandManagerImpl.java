package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyQuickCommandManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyQuickCommandIndicesQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyQuickCommandShardsQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.shard.ShardCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.*;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.*;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

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

    private static final ILog LOGGER            = LogFactory.getLog(ClusterPhyQuickCommandManagerImpl.class);
    
    @Autowired
    protected ClusterPhyService clusterPhyService;
    
    @Autowired
    protected ESClusterService     esClusterService;
    @Autowired
    protected ESClusterNodeService esClusterNodeService;
    
    @Autowired
    protected ESIndexService esIndexService;
    
    @Autowired
    protected ESShardCatService esShardCatService;
    
    @Autowired
    protected ESShardService esShardService;
    
    @Autowired
    private HandleFactory handleFactory;
    
    @Override
    public Result<List<NodeStateVO>> nodeStateAnalysis(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        return Result.buildSucc(esClusterNodeService.syncNodeStateAnalysis(cluster));
    }
    
    @Override
    public Result<List<IndicesDistributionVO>> indicesDistribution(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        // 把 List<CatIndexResult> 转为 List<IndicesDistributionVO>
        List<CatIndexResult> catIndexResultList = esIndexService.syncIndicesDistribution(cluster);
        return Result.buildSucc(ConvertUtil.list2List(catIndexResultList, IndicesDistributionVO.class));
    }
    
    @Override
    public Result<List<PendingTaskAnalysisVO>> pendingTaskAnalysis(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        List<PendingTaskAnalysisVO> vos = esClusterService.syncPendingTaskAnalysis(cluster);
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
        List<TaskMissionAnalysisVO> vos = esClusterService.syncTaskMissionAnalysis(cluster);
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
        return Result.buildSucc(esClusterService.syncHotThreadAnalysis(cluster));
    }
    
    @Override
    public Result<ShardAssignmentDescriptionVO> shardAssignmentDescription(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        ShardAssignmentDescriptionVO shardAssignmentDescriptionVO = new ShardAssignmentDescriptionVO();
        try {
            shardAssignmentDescriptionVO = esShardService.syncShardAssignmentDescription(cluster);
        } catch (ESOperateException e) {
            LOGGER.error("class=IndicesManagerImpl||method=editMapping||errMsg={}", e.getMessage(), e);
            return Result.buildFail(e.getMessage() + ":" + e.getCause());
        }
        if (shardAssignmentDescriptionVO == null) {
            return Result.buildFail();
        }
        return Result.buildSucc(shardAssignmentDescriptionVO);
    }
    
    @Override
    public Result<Void> abnormalShardAllocationRetry(String cluster) {
        Result<Void> checkResult = checkClusterExistence(cluster);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        boolean result = esClusterService.syncAbnormalShardAllocationRetry(cluster);
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
        boolean result = esClusterService.syncClearFieldDataMemory(cluster);
        if (result) {
            return Result.buildSucc();
        }
        return Result.buildFail();
    }
    
    @Override
    public List<IndicesDistributionVO> indicesDistributionPage(
            ClusterPhyQuickCommandIndicesQueryDTO condition, Integer projectId) {
        List<IndicesDistributionVO> indicesDistributionVOS = new ArrayList<>();
        List<CatIndexResult> catIndexResultList = esIndexService.syncIndicesDistribution(condition.getCluster());
        catIndexResultList.forEach(catIndexResult -> {
            IndicesDistributionVO indicesDistributionVO = ConvertUtil.obj2Obj(catIndexResult, IndicesDistributionVO.class);
            indicesDistributionVO.setPriStoreSize(SizeUtil.getUnitSize(catIndexResult.getPriStoreSize()));
            indicesDistributionVO.setStoreSize(SizeUtil.getUnitSize(catIndexResult.getStoreSize()));
            if (StringUtils.isNotBlank(condition.getKeyword())) {
                String index = Objects.isNull(indicesDistributionVO.getIndex())?"":indicesDistributionVO.getIndex();
                if (index.contains(condition.getKeyword())) {
                    indicesDistributionVOS.add(indicesDistributionVO);
                }
            } else {
                indicesDistributionVOS.add(indicesDistributionVO);
            }
        });
        return indicesDistributionVOS;
    }
    
    @Override
    public List<ShardDistributionVO> shardDistributionPage(ClusterPhyQuickCommandShardsQueryDTO condition,Integer projectId)
            throws ESOperateException {
        List<ShardCatCellPO> shardCatCellPOS =  esShardCatService.syncShardDistribution(condition.getCluster(),System.currentTimeMillis());
        List<ShardDistributionVO> shardDistributionVOS = new ArrayList<>();
        shardCatCellPOS.forEach(shardCatCellPO -> {
            ShardDistributionVO shardDistributionVO = ConvertUtil.obj2Obj(shardCatCellPO, ShardDistributionVO.class);
            shardDistributionVO.setStore(SizeUtil.getUnitSize(shardCatCellPO.getStore()));
            if (StringUtils.isNotBlank(condition.getKeyword())) {
                String node = Objects.isNull(shardDistributionVO.getNode())?"":shardDistributionVO.getNode();
                String index = Objects.isNull(shardDistributionVO.getIndex())?"":shardDistributionVO.getIndex();
                if (node.contains(condition.getKeyword()) || index.contains(condition.getKeyword())) {
                    shardDistributionVOS.add(shardDistributionVO);
                }
            }else {
                shardDistributionVOS.add(shardDistributionVO);
            }
        });
        return shardDistributionVOS;
    }
    
    private Result<Void> checkClusterExistence(String cluster) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(cluster);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail(String.format("集群[%s]不存在", cluster));
        }
        return Result.buildSucc();
    }
}