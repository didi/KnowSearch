package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.metrics;

import com.didichuxing.datachannel.arius.admin.biz.metrics.GatewayMetricsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.gateway.GatewayOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.linechart.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

@RestController
@RequestMapping(V3_OP + "/gateway/metrics")
@Api(tags = "Gateway指标监控信息")
public class GatewayMetricsController {

    @Autowired
    private GatewayMetricsManager gatewayMetricsManager;

    @GetMapping("/config/{group}")
    @ApiOperation(value = "获取不同组的指标", notes = "")
    public Result<List<String>> getGatewayMetrics(@PathVariable String group) {
        return gatewayMetricsManager.getGatewayMetricsEnums(group);
    }

    @GetMapping("/dslMd5/list")
    @ApiOperation(value = "获取当前项目下的dslMd5列表", notes = "")
    public Result<List<String>> getDslMd5List(Long startTime, Long endTime, HttpServletRequest request) {
        return gatewayMetricsManager.getDslMd5List(HttpRequestUtils.getAppId(request), startTime, endTime);
    }

    @PostMapping("/overview")
    @ApiOperation(value = "获取gateway概览", notes = "")
    public Result<List<GatewayOverviewMetricsVO>> getGatewayOverviewMetrics(@RequestBody GatewayOverviewDTO dto) {
        dto.validParam();
        return gatewayMetricsManager.getGatewayOverviewMetrics(dto);
    }


    @PostMapping("/node")
    @ApiOperation(value = "获取gateway节点指标信息")
    public Result<List<VariousLineChartMetricsVO>> getGatewayNodeMetrics(@RequestBody GatewayNodeDTO dto,
                                                                         HttpServletRequest request) {
        dto.validParam();
        return gatewayMetricsManager.getGatewayNodeMetrics(dto, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/index")
    @ApiOperation(value = "获取gateway索引指标信息")
    public Result<List<VariousLineChartMetricsVO>> getGatewayIndexMetrics(@RequestBody GatewayIndexDTO dto,
                                                                          HttpServletRequest request) {
        dto.validParam();
        return gatewayMetricsManager.getGatewayIndexMetrics(dto, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/app")
    @ApiModelProperty(value = "获取gateway项目指标信息")
    public Result<List<VariousLineChartMetricsVO>> getGatewayAppMetrics(@RequestBody GatewayAppDTO dto) {
        dto.validParam();
        return gatewayMetricsManager.getGatewayAppMetrics(dto);
    }


    @PostMapping("/dsl")
    @ApiModelProperty(value = "获取gateway查询模版指标信息")
    public Result<List<VariousLineChartMetricsVO>> getGatewayDslMetrics(@RequestBody GatewayDslDTO dto,
                                                                        HttpServletRequest request) {
        dto.validParam();
        return gatewayMetricsManager.getGatewayDslMetrics(dto, HttpRequestUtils.getAppId(request));
    }

}
