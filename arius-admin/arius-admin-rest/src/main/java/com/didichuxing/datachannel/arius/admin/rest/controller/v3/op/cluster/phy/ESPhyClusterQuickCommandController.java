package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.phy;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyQuickCommandManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyQuickCommandIndicesQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyQuickCommandShardsQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.*;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

/**
 * 物理集群快捷命令
 *
 * @ClassName ESPhyClusterQuickCommandController
 * @Author gyp
 * @Date 2022/6/1
 * @Version 1.0
 */

@RestController
@RequestMapping({ V3 + "/cluster/phy" })
@Api(tags = "ES物理集群集群快捷命令接口(REST)")
public class ESPhyClusterQuickCommandController {

    @Autowired
    private ClusterPhyQuickCommandManager clusterPhyQuickCommandManager;

    @PutMapping("/{cluster}/node-state-analysis")
    @ResponseBody
    @ApiOperation(value = "node_state分析")
    public Result<List<NodeStateVO>> nodeStateAnalysis(@PathVariable String cluster) {
        return clusterPhyQuickCommandManager.nodeStateAnalysis(cluster);
    }


    @PostMapping("/indices-distribution")
    @ResponseBody
    @ApiOperation(value = "indices分布")
    public Result<List<IndicesDistributionVO>> indicesDistribution(HttpServletRequest request,
                                                                       @RequestBody ClusterPhyQuickCommandIndicesQueryDTO condition) throws NotFindSubclassException {
        return Result.buildSucc(clusterPhyQuickCommandManager.indicesDistributionPage(condition, HttpRequestUtil.getProjectId(request)));
    }

    @PostMapping("/shard-distribution")
    @ResponseBody
    @ApiOperation(value = "shard分布")
    public Result<List<ShardDistributionVO>> shardDistribution(HttpServletRequest request,
                                                               @RequestBody ClusterPhyQuickCommandShardsQueryDTO condition) throws ESOperateException{
        return Result.buildSucc(clusterPhyQuickCommandManager.shardDistributionPage(condition, HttpRequestUtil.getProjectId(request)));
    }

    @PutMapping("/{cluster}/pending-task-analysis")
    @ResponseBody
    @ApiOperation(value = "pending task分析")
    public Result<List<PendingTaskAnalysisVO>> pendingTaskAnalysis(@PathVariable String cluster) {
        return clusterPhyQuickCommandManager.pendingTaskAnalysis(cluster);
    }

    @PutMapping("/{cluster}/task-mission-analysis")
    @ResponseBody
    @ApiOperation(value = "task任务分析")
    public Result<List<TaskMissionAnalysisVO>> taskMissionAnalysis(@PathVariable String cluster) {
        return clusterPhyQuickCommandManager.taskMissionAnalysis(cluster);
    }

    @PutMapping("/{cluster}/hot-thread-analysis")
    @ResponseBody
    @ApiOperation(value = "热点线程分析")
    public Result<String> hotThreadAnalysis(@PathVariable String cluster) {
        return clusterPhyQuickCommandManager.hotThreadAnalysis(cluster);
    }

    @PutMapping("/{cluster}/shard-assignment-description")
    @ResponseBody
    @ApiOperation(value = "shard分配说明")
    public Result<ShardAssignmentDescriptionVO> shardAssignmentDescription(@PathVariable String cluster) {
        return clusterPhyQuickCommandManager.shardAssignmentDescription(cluster);
    }

    @PutMapping("/{cluster}/abnormal-shard-allocation-retry")
    @ResponseBody
    @ApiOperation(value = "异常shard分配重试")
    public Result<Void> abnormalShardAllocationRetry(@PathVariable String cluster) {
        return clusterPhyQuickCommandManager.abnormalShardAllocationRetry(cluster);
    }

    @PutMapping("/{cluster}/clear-field-data-memory")
    @ResponseBody
    @ApiOperation(value = "清除fieldData内存")
    public Result<Void> clearFieldDataMemory(@PathVariable String cluster) {
        return clusterPhyQuickCommandManager.clearFieldDataMemory(cluster);
    }
}