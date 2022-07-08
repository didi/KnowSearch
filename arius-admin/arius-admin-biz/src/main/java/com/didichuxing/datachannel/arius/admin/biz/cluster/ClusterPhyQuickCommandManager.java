package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyQuickCommandQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.*;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import java.util.List;

/**
 * 快捷指令.
 *
 * @ClassName QuickCommandManager
 * @Author gyp
 * @Date 2022/6/1
 * @Version 1.0
 */
public interface ClusterPhyQuickCommandManager {

    /**
     * node_state分析
     * @param clusterId
     * @return
     */
    Result<List<NodeStateVO>> nodeStateAnalysis(String clusterId);

    /**
     * indices分布
     * @param cluster
     * @return
     */
    Result<List<IndicesDistributionVO>> indicesDistribution(String cluster);

    /**
     * shard分布
     * @param cluster
     * @return
     */
    Result<List<ShardDistributionVO>> shardDistribution(String cluster);

    /**
     * pending task分析
     * @param cluster
     * @return
     */
    Result<List<PendingTaskAnalysisVO>> pendingTaskAnalysis(String cluster);

    /**
     * task任务分析
     * @param cluster
     * @return
     */
    Result<List<TaskMissionAnalysisVO>> taskMissionAnalysis(String cluster);

    /**
     * 热点线程分析
     * @param cluster
     * @return
     */
    Result<String> hotThreadAnalysis(String cluster);

    /**
     * shard分配说明
     * @param cluster
     * @return
     */
    Result<ShardAssignmentDescriptionVO> shardAssignmentDescription(String cluster);

    /**
     * 异常shard分配重试
     * @param cluster
     * @return
     */
    Result<Void> abnormalShardAllocationRetry(String cluster);

    /**
     * 清除fielddata内存
     * @param cluster
     * @return
     */
    Result<Void> clearFieldDataMemory(String cluster);

    /**
     * 条件获取索引列表信息 ,携带可读可写标志位
     * @param condition     查询条件
     * @param projectId         项目
     * @return              List<IndexCatInfoVO>
     */
    PaginationResult<IndicesDistributionVO> indicesDistributionPage(ClusterPhyQuickCommandQueryDTO condition, Integer projectId) throws NotFindSubclassException;
}