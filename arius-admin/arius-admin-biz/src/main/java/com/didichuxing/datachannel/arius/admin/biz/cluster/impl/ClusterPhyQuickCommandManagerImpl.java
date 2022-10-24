package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyQuickCommandManager;
import com.didichuxing.datachannel.arius.admin.biz.page.QuickCommandIndicesDistributionPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.page.QuickCommandShardsDistributionPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyQuickCommandIndicesQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyQuickCommandShardsQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.*;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.*;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
    public PaginationResult<IndicesDistributionVO> indicesDistributionPage(
            ClusterPhyQuickCommandIndicesQueryDTO condition, Integer projectId) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(
                PageSearchHandleTypeEnum.QUICK_COMMAND_INDEX.getPageSearchType());
        if (baseHandle instanceof QuickCommandIndicesDistributionPageSearchHandle) {
            QuickCommandIndicesDistributionPageSearchHandle handle = (QuickCommandIndicesDistributionPageSearchHandle) baseHandle;
            return handle.doPage(condition, projectId);
        }
        
        return PaginationResult.buildFail("获取索引分页信息失败");
    }
    
    @Override
    public PaginationResult<ShardDistributionVO> shardDistributionPage(ClusterPhyQuickCommandShardsQueryDTO condition,
                                                                       Integer projectId)
            throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(
                PageSearchHandleTypeEnum.QUICK_COMMAND_SHARD.getPageSearchType());
        if (baseHandle instanceof QuickCommandShardsDistributionPageSearchHandle) {
            QuickCommandShardsDistributionPageSearchHandle handle = (QuickCommandShardsDistributionPageSearchHandle) baseHandle;
            return handle.doPage(condition, projectId);
        }
        
        return PaginationResult.buildFail("获取索引分页信息失败");
    }
    
    private Result<Void> checkClusterExistence(String cluster) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(cluster);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail(String.format("集群[%s]不存在", cluster));
        }
        return Result.buildSucc();
    }
}