package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.phy;

import com.alibaba.fastjson.JSONArray;
import com.didichuxing.datachannel.arius.admin.biz.cluster.QuickCommandManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostWithRegionInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

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
    private QuickCommandManager quickCommandManager;
    
    @PutMapping("/{cluster}/node-state-analysis")
    @ResponseBody
    @ApiOperation(value = "node_state分析")
    public Result<List<Map>> nodeStateAnalysis(@PathVariable String cluster) {
        return quickCommandManager.nodeStateAnalysis(cluster);
    }

    @PutMapping("/{cluster}/indices-distribution")
    @ResponseBody
    @ApiOperation(value = "indices分布")
    public Result<JSONArray> indicesDistribution(@PathVariable String cluster) {
        return quickCommandManager.indicesDistribution(cluster);
    }

    @PutMapping("/{cluster}/shard-distribution")
    @ResponseBody
    @ApiOperation(value = "shard分布")
    public Result<JSONArray> shardDistribution(@PathVariable String cluster) {
        return quickCommandManager.shardDistribution(cluster);
    }

    @PutMapping("/{cluster}/pending-task-analysis")
    @ResponseBody
    @ApiOperation(value = "pending task分析")
    public Result<List<Map>> pendingTaskAnalysis(@PathVariable String cluster) {
        return quickCommandManager.pendingTaskAnalysis(cluster);
    }

    @PutMapping("/{cluster}/task-mission-analysis")
    @ResponseBody
    @ApiOperation(value = "task任务分析")
    public Result<List<Map>> taskMissionAnalysis(@PathVariable String cluster) {
        return quickCommandManager.taskMissionAnalysis(cluster);
    }

    @PutMapping("/{cluster}/hot-thread-analysis")
    @ResponseBody
    @ApiOperation(value = "热点线程分析")
    public Result<String> hotThreadAnalysis(@PathVariable String cluster) {
        return quickCommandManager.hotThreadAnalysis(cluster);
    }

    @PutMapping("/{cluster}/shard-assignment-description")
    @ResponseBody
    @ApiOperation(value = "shard分配说明")
    public Result<Map> shardAssignmentDescription(@PathVariable String cluster) {
        return quickCommandManager.shardAssignmentDescription(cluster);
    }

    @PutMapping("/{cluster}/abnormal-shard-allocation-retry")
    @ResponseBody
    @ApiOperation(value = "异常shard分配重试")
    public Result<Void> abnormalShardAllocationRetry(@PathVariable String cluster) {
        return quickCommandManager.abnormalShardAllocationRetry(cluster);
    }

    @PutMapping("/{cluster}/clear-fielddata-memory")
    @ResponseBody
    @ApiOperation(value = "清除fielddata内存")
    public Result<Void> clearFielddataMemory(@PathVariable String cluster) {
        return quickCommandManager.clearFielddataMemory(cluster);
    }

}