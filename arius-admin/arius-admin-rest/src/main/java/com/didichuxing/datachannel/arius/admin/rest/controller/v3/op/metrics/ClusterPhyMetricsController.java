package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.metrics;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

/**
 * @author linyunan
 * @date 2021-07-30
 */
@RestController()
@RequestMapping({ V3 + "/metrics/cluster" })
@Api(tags = "ES物理集群监控信息")
public class ClusterPhyMetricsController {
    @Autowired
    private ClusterPhyMetricsManager clusterPhyMetricsManager;

    @Autowired
    private ClusterPhyManager        clusterPhyManager;

    @GetMapping("/clusters")
    @ResponseBody
    @ApiOperation(value = "根据ProjectId获取集群名称列表")
    public Result<List<String>> getClusterPhyNames(HttpServletRequest request) {
        return Result.buildSucc(clusterPhyManager.listClusterPhyNameByProjectId(HttpRequestUtil.getProjectId(request)));
    }

    @GetMapping("/{type}")
    @ResponseBody
    @ApiOperation(value = "获取物理集群看板指标类型", notes = "type = clusterOverview, clusterNode, clusterNodeIndices")
    public Result<List<String>> getClusterPhyMetricsTypes(@PathVariable String type) {
        return Result.buildSucc(clusterPhyMetricsManager.getMetricsCode2TypeMap(type));
    }

    @PostMapping("/config-metrics")
    @ResponseBody
    @ApiOperation(value = "获取账号下已配置指标类型")
    public Result<List<String>> listClusterPhyMetricsTypes(@RequestBody UserConfigInfoDTO param,
                                                          HttpServletRequest request) {
        return Result
            .buildSucc(clusterPhyMetricsManager.listConfigMetricsByCondition(param, HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request)));
    }

    @PutMapping("/config-metrics")
    @ResponseBody
    @ApiOperation(value = "更新账号下已配置指标类型")
    public Result<Integer> updateClusterPhyMetricsTypes(@RequestBody UserConfigInfoDTO param,
                                                        HttpServletRequest request) {
        return clusterPhyMetricsManager.updateConfigMetricsByCondition(param, HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/overview")
    @ResponseBody
    @ApiOperation(value = "获取物理集群总览指标信息")
    public Result<ESClusterOverviewMetricsVO> getClusterPhyMetrics(@RequestBody MetricsClusterPhyDTO param,
                                                                   HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request), ClusterPhyTypeMetricsEnum.CLUSTER);
    }

    @PostMapping("/node")
    @ResponseBody
    @ApiOperation(value = "获取物理集群节点指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyNodesMetrics(@RequestBody MetricsClusterPhyNodeDTO param,
                                                                             HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request), ClusterPhyTypeMetricsEnum.NODE);
    }

    @PostMapping("/nodes")
    @ResponseBody
    @ApiOperation(value = "获取物理集群多个节点指标信息")
    public Result<List<VariousLineChartMetricsVO>> getMultiClusterPhyNodesMetrics(@RequestBody MultiMetricsClusterPhyNodeDTO param,
                                                                                  HttpServletRequest request) {
        return clusterPhyMetricsManager.getMultiClusterMetrics(param, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request), ClusterPhyTypeMetricsEnum.NODE);
    }

    @PostMapping("/index")
    @ResponseBody
    @ApiOperation(value = "获取物理集群索引指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyIndicesMetrics(@RequestBody MetricsClusterPhyIndicesDTO param,
                                                                               HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request), ClusterPhyTypeMetricsEnum.INDICES);
    }

    @PostMapping("/template")
    @ResponseBody
    @ApiOperation(value = "获取物理集群索引模板指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyTemplateMetrics(@RequestBody MetricsClusterPhyTemplateDTO param,
                                                                                HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request), ClusterPhyTypeMetricsEnum.TEMPLATES);
    }

    @GetMapping("{clusterPhyName}/{node}/task")
    @ResponseBody
    @ApiModelProperty(value = "获取物理集群节点task详情")
    public Result<List<ESClusterTaskDetailVO>> getClusterPhyTaskDetail(@PathVariable String clusterPhyName,
                                                                       @PathVariable String node,
                                                                       @RequestParam("startTime") String startTime,
                                                                       @RequestParam("endTime") String endTime,
                                                                       HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterPhyTaskDetail(clusterPhyName, node, startTime, endTime,
            HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/node/task")
    @ResponseBody
    @ApiOperation(value = "获取物理集群节点task指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyNodesTaskMetrics(@RequestBody MultiMetricsClusterPhyNodeTaskDTO param,
                                                                                 HttpServletRequest request) {
        return clusterPhyMetricsManager.getMultiClusterMetrics(param, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request), ClusterPhyTypeMetricsEnum.NODE_TASKS);
    }
}