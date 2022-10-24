package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.metrics;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.biz.metrics.GatewayMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.gateway.GatewayOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
import com.didiglobal.logi.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping({ V3 + "/metrics/gateway" })
@Api(tags = "Gateway指标监控信息")
public class GatewayMetricsController {

    @Autowired
    private GatewayMetricsManager gatewayMetricsManager;
    @Autowired
    private GatewayManager        gatewayManager;

    @GetMapping("/alive-nodes")
    @ResponseBody
    @ApiOperation(value = "获取gateway存活节点名称列表接口")
    public Result<List<String>> getGatewayAliveNodeNames(HttpServletRequest request) {
        return gatewayManager.getGatewayAliveNodeNames("Normal");
    }

    @GetMapping("/config/{group}")
    @ApiOperation(value = "获取不同组的指标")
    public Result<List<String>> getGatewayMetrics(@PathVariable String group) {
        return gatewayMetricsManager.getGatewayMetricsEnums(group);
    }

    @GetMapping("/dsl-md5")
    @ApiOperation(value = "获取当前项目下的dslMd5列表")
    public Result<List<String>> getDslMd5List(Long startTime, Long endTime, HttpServletRequest request) {
        return gatewayMetricsManager.getDslMd5List(HttpRequestUtil.getProjectId(request), startTime, endTime);
    }

    @PostMapping("/overview")
    @ApiOperation(value = "获取gateway概览")
    public Result<List<GatewayOverviewMetricsVO>> getGatewayOverviewMetrics(@RequestBody GatewayOverviewDTO dto) {
        validateParam(dto);
        return gatewayMetricsManager.getGatewayOverviewMetrics(dto);
    }

    @PostMapping("/node")
    @ApiOperation(value = "获取gateway节点指标信息")
    public Result<List<VariousLineChartMetricsVO>> getGatewayNodeMetrics(@RequestBody GatewayNodeDTO dto,
                                                                         HttpServletRequest request) {
        validateParam(dto);
        return gatewayMetricsManager.getGatewayNodeMetrics(dto, HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/nodes")
    @ApiOperation(value = "获取多节点gateway节点指标信息")
    public Result<List<VariousLineChartMetricsVO>> getMultiGatewayNodesMetrics(@RequestBody MultiGatewayNodesDTO dto,
                                                                               HttpServletRequest request) {
        validateParam(dto);
        return gatewayMetricsManager.getMultiGatewayNodesMetrics(dto, HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/client-node")
    @ApiOperation(value = "获取gatewayNode相关的clientNode指标信息")
    public Result<List<VariousLineChartMetricsVO>> getClientNodeMetrics(@RequestBody ClientNodeDTO dto,
                                                                        HttpServletRequest request) {
        validateParam(dto);
        return gatewayMetricsManager.getClientNodeMetrics(dto, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/client-node-ip")
    @ApiOperation(value = "获取取gatewayNode相关的clientNode ip列表")
    public Result<List<Tuple<String,String>>> getClientNodeIpList(String gatewayNode, Long startTime, Long endTime,
                                                          HttpServletRequest request) {
        return gatewayMetricsManager.getClientNodeIdList(gatewayNode, startTime, endTime,
            HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/index")
    @ApiOperation(value = "获取gateway索引指标信息")
    public Result<List<VariousLineChartMetricsVO>> getGatewayIndexMetrics(@RequestBody GatewayIndexDTO dto,
                                                                          HttpServletRequest request) {
        validateParam(dto);
        return gatewayMetricsManager.getGatewayIndexMetrics(dto, HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/projects")
    @ApiModelProperty(value = "获取gateway项目指标信息")
    public Result<List<VariousLineChartMetricsVO>> getGatewayAppMetrics(@RequestBody GatewayProjectDTO dto) {
        validateParam(dto);
        return gatewayMetricsManager.getGatewayAppMetrics(dto);
    }

    @PostMapping("/dsl")
    @ApiModelProperty(value = "获取gateway查询模版指标信息")
    public Result<List<VariousLineChartMetricsVO>> getGatewayDslMetrics(@RequestBody GatewayDslDTO dto,
                                                                        HttpServletRequest request) {
        validateParam(dto);
        return gatewayMetricsManager.getGatewayDslMetrics(dto, HttpRequestUtil.getProjectId(request));
    }

    private void validateParam(GatewayMetricsDTO dto) {
        dto.validParam();
        //检查是否有不合法的指标传过来
        List<String> metricsByGroup = GatewayMetricsTypeEnum.getMetricsByGroup(dto.getGroup());
        String invalidMetrics = dto.getMetricsTypes().stream().filter(x -> !metricsByGroup.contains(x))
            .collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(invalidMetrics)) {
            throw new RuntimeException("非法指标:" + invalidMetrics);
        }
    }
}