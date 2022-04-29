package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.metrics;

import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * Created by linyunan on 2021-07-30
 */
@RestController()
@RequestMapping(V3_OP + "/phy/cluster/metrics")
@Api(tags = "ES物理集群监控信息")
public class ClusterPhyMetricsController {

    @Autowired
    private ClusterPhyMetricsManager clusterPhyMetricsManager;

    @GetMapping("/{type}")
    @ResponseBody
    @ApiOperation(value = "获取物理集群看板指标类型", notes = "type = clusterOverview, clusterNode, clusterNodeIndices")
    public Result<List<String>> getClusterPhyMetricsTypes(@PathVariable String type) {
        return Result.buildSucc(clusterPhyMetricsManager.getMetricsCode2TypeMap(type));
    }

    @PostMapping("/configMetrics")
    @ResponseBody
    @ApiOperation(value = "获取账号下已配置指标类型")
    public Result<List<String>> getClusterPhyMetricsTypes(@RequestBody MetricsConfigInfoDTO param,
                                                          HttpServletRequest request) {
        return Result.buildSucc(
            clusterPhyMetricsManager.getDomainAccountConfigMetrics(param, HttpRequestUtils.getOperator(request)));
    }

    @PostMapping("/updateConfigMetrics")
    @ResponseBody
    @ApiOperation(value = "更新账号下已配置指标类型")
    public Result<Integer> updateClusterPhyMetricsTypes(@RequestBody MetricsConfigInfoDTO param, HttpServletRequest request) {
        return clusterPhyMetricsManager.updateDomainAccountConfigMetrics(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/overview")
    @ResponseBody
    @ApiOperation(value = "获取物理集群总览指标信息")
    public Result<ESClusterOverviewMetricsVO> getClusterPhyMetrics(@RequestBody MetricsClusterPhyDTO param,
                                                                      HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, HttpRequestUtils.getAppId(request),
                HttpRequestUtils.getOperator(request), ClusterPhyTypeMetricsEnum.CLUSTER);
    }

    @PostMapping("/node")
    @ResponseBody
    @ApiOperation(value = "获取物理集群节点指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyNodesMetrics(@RequestBody MetricsClusterPhyNodeDTO param,
                                                                             HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, HttpRequestUtils.getAppId(request),
                HttpRequestUtils.getOperator(request), ClusterPhyTypeMetricsEnum.NODE);
    }

    @PostMapping("/nodes")
    @ResponseBody
    @ApiOperation(value = "获取物理集群多个节点指标信息")
    public Result<List<VariousLineChartMetricsVO>> getMultiClusterPhyNodesMetrics(@RequestBody MultiMetricsClusterPhyNodeDTO param,
                                                                             HttpServletRequest request) {
        return clusterPhyMetricsManager.getMultiClusterMetrics(param, HttpRequestUtils.getAppId(request),
                HttpRequestUtils.getOperator(request), ClusterPhyTypeMetricsEnum.NODE);
    }

    @PostMapping("/index")
    @ResponseBody
    @ApiOperation(value = "获取物理集群索引指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyIndicesMetrics(@RequestBody MetricsClusterPhyIndicesDTO param,
                                                                               HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, HttpRequestUtils.getAppId(request),
                HttpRequestUtils.getOperator(request), ClusterPhyTypeMetricsEnum.INDICES);
    }

    @PostMapping("/template")
    @ResponseBody
    @ApiOperation(value = "获取物理集群索引模板指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyTemplateMetrics(@RequestBody MetricsClusterPhyTemplateDTO param,
                                                                               HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, HttpRequestUtils.getAppId(request),
                HttpRequestUtils.getOperator(request), ClusterPhyTypeMetricsEnum.TEMPLATES);
    }

    @GetMapping("{clusterPhyName}/indices")
    @ResponseBody
    @ApiModelProperty(value = "获取物理集群节点列表")
    public Result<List<String>> getClusterPhyIndexName(@PathVariable String clusterPhyName, HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterPhyIndexName(clusterPhyName, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("{clusterPhyName}/{node}/task")
    @ResponseBody
    @ApiModelProperty(value = "获取物理集群节点task详情")
    public Result<List<ESClusterTaskDetailVO>> getClusterPhyTaskDetail(@PathVariable String clusterPhyName, @PathVariable String node,
                                                                       @RequestParam("startTime") String startTime,
                                                                       @RequestParam("endTime") String endTime, HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterPhyTaskDetail(clusterPhyName, node, startTime, endTime, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/node/task")
    @ResponseBody
    @ApiOperation(value = "获取物理集群节点task指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyNodesTaskMetrics(@RequestBody MultiMetricsClusterPhyNodeTaskDTO param,
                                                                                 HttpServletRequest request) {
        return clusterPhyMetricsManager.getMultiClusterMetrics(param, HttpRequestUtils.getAppId(request),
                HttpRequestUtils.getOperator(request), ClusterPhyTypeMetricsEnum.NODE_TASKS);
    }
}
