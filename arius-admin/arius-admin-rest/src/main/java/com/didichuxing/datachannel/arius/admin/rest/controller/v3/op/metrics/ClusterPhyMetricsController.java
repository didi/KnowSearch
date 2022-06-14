package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.metrics;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyIndicesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MultiMetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MultiMetricsClusterPhyNodeTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linyunan
 * @date 2021-07-30
 */
@RestController()
@RequestMapping({ V3_OP + "/phy/cluster/metrics", V3 + "/metrics/cluster" })
@Api(tags = "ES物理集群监控信息")
public class ClusterPhyMetricsController {
    @Autowired
    private ClusterPhyMetricsManager clusterPhyMetricsManager;

    @Autowired
    private ClusterPhyManager clusterPhyManager;
    @Autowired
    private TemplateLogicManager templateLogicManager;


    @GetMapping("/clusters")
    @ResponseBody
    @ApiOperation(value = "根据AppId获取集群名称列表")
    public Result<List<String>> getClusterPhyNames(HttpServletRequest request) {
        return Result.buildSucc(clusterPhyManager.listClusterPhyNameByAppId(HttpRequestUtils.getAppId(request)));
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
    public Result<List<String>> getClusterPhyMetricsTypes(@RequestBody MetricsConfigInfoDTO param,
                                                          HttpServletRequest request) {
        return Result.buildSucc(
            clusterPhyMetricsManager.getDomainAccountConfigMetrics(param, HttpRequestUtil.getOperator(request)));
    }

    @PostMapping("/updateConfigMetrics")
    @ResponseBody
    @ApiOperation(value = "更新账号下已配置指标类型")
    public Result<Integer> updateClusterPhyMetricsTypes(@RequestBody MetricsConfigInfoDTO param, HttpServletRequest request) {
        return clusterPhyMetricsManager.updateDomainAccountConfigMetrics(param, HttpRequestUtil.getOperator(request));
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
    public Result<List<ESClusterTaskDetailVO>> getClusterPhyTaskDetail(@PathVariable String clusterPhyName, @PathVariable String node,
                                                                       @RequestParam("startTime") String startTime,
                                                                       @RequestParam("endTime") String endTime, HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterPhyTaskDetail(clusterPhyName, node, startTime, endTime, HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/node/task")
    @ResponseBody
    @ApiOperation(value = "获取物理集群节点task指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyNodesTaskMetrics(@RequestBody MultiMetricsClusterPhyNodeTaskDTO param,
                                                                                 HttpServletRequest request) {
        return clusterPhyMetricsManager.getMultiClusterMetrics(param, HttpRequestUtil.getProjectId(request),
                HttpRequestUtil.getOperator(request), ClusterPhyTypeMetricsEnum.NODE_TASKS);
    }

    @GetMapping("{clusterPhyName}/phy/indices")
    @ResponseBody
    @ApiModelProperty(value = "获取物理集群索引列表")
    public Result<List<String>> getClusterPhyIndexName(@PathVariable String clusterPhyName, HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterPhyIndexName(clusterPhyName, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("{clusterLogicName}/logic/indices")
    @ResponseBody
    @ApiModelProperty(value = "获取逻辑集群索引列表")
    public Result<List<String>> getClusterLogicIndexName(@PathVariable String clusterLogicName, HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterLogicIndexName(clusterLogicName, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("{clusterPhyName}/phy/templates")
    @ResponseBody
    @ApiOperation(value = "根据物理集群名称获取对应全量逻辑模板列表", notes = "")
    public Result<List<ConsoleTemplateVO>> getLogicTemplatesByPhyCluster(HttpServletRequest request,@PathVariable String clusterPhyName) {
        return templateLogicManager.getTemplateVOByPhyCluster(clusterPhyName, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("{clusterLogicName}/logic/templates")
    @ResponseBody
    @ApiOperation(value = "根据物理集群名称获取对应全量逻辑模板列表", notes = "")
    public Result<List<ConsoleTemplateVO>> getLogicTemplatesByLogicCluster(HttpServletRequest request,@PathVariable String clusterLogicName) {
        return templateLogicManager.getTemplateVOByLogicCluster(clusterLogicName, HttpRequestUtil.getProjectId(request));
    }

}