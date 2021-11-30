package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.metrics;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyIndicesDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster.ESClusterOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.linechart.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;

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
        return clusterPhyMetricsManager.getOverviewMetrics(param, HttpRequestUtils.getAppId(request),
            HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/node")
    @ResponseBody
    @ApiOperation(value = "获取物理集群节点指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyNodesMetrics(@RequestBody MetricsClusterPhyNodeDTO param,
                                                                             HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterPhyNodesMetrics(param, HttpRequestUtils.getAppId(request),
            HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/index")
    @ResponseBody
    @ApiOperation(value = "获取物理集群索引指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyIndicesMetrics(@RequestBody MetricsClusterPhyIndicesDTO param,
                                                                               HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterPhyIndicesMetrics(param, HttpRequestUtils.getAppId(request),
            HttpRequestUtils.getOperator(request));
    }

    @GetMapping("{clusterPhyName}/indices")
    @ResponseBody
    @ApiModelProperty(value = "获取物理集群节点列表")
    public Result<List<String>> getClusterPhyIndexName(@PathVariable String clusterPhyName, HttpServletRequest request) {
        return clusterPhyMetricsManager.getClusterPhyIndexName(clusterPhyName, HttpRequestUtils.getAppId(request));
    }
}
