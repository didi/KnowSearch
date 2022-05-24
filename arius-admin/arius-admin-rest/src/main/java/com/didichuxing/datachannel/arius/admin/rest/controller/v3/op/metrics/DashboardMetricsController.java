package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.metrics;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.biz.metrics.DashboardMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardListDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.list.MetricListVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.dashboard.ClusterPhyHealthMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by linyunan on 3/14/22
 */
@RestController()
@RequestMapping(V3_OP + "/dashboard/metrics")
@Api(tags = "dashboard监控信息")
public class DashboardMetricsController {
    @Autowired
    private DashboardMetricsManager dashboardMetricsManager;

    @GetMapping("/health")
    @ResponseBody
    @ApiOperation(value = "获取dashboard大盘健康状态信息")
    public Result<ClusterPhyHealthMetricsVO> getClusterHealthInfo(HttpServletRequest request) {
        return dashboardMetricsManager.getClusterHealthInfo(HttpRequestUtils.getProjectId(request));
    }

    @PostMapping("/top/cluster")
    @ResponseBody
    @ApiOperation(value = "获取dashboard大盘TopN集群相关指标信息")
    public Result<List<VariousLineChartMetricsVO>> getTopClusterMetricsInfo(@RequestBody MetricsDashboardTopNDTO param,
                                                                             HttpServletRequest request) {
        return dashboardMetricsManager.getTopClusterMetricsInfo(param, HttpRequestUtils.getProjectId(request));
    }

    @PostMapping("/top/node")
    @ResponseBody
    @ApiOperation(value = "获取dashboard大盘TopN节点相关指标信息")
    public Result<List<VariousLineChartMetricsVO>> getTopNodeMetricsInfo(@RequestBody MetricsDashboardTopNDTO param,
                                                                            HttpServletRequest request) {
        return dashboardMetricsManager.getTopNodeMetricsInfo(param, HttpRequestUtils.getProjectId(request));
    }

    @PostMapping("/top/template")
    @ResponseBody
    @ApiOperation(value = "获取dashboard大盘TopN模板相关指标信息")
    public Result<List<VariousLineChartMetricsVO>> getTopTemplateMetricsInfo(@RequestBody MetricsDashboardTopNDTO param,
                                                                         HttpServletRequest request) {
        return dashboardMetricsManager.getTopTemplateMetricsInfo(param, HttpRequestUtils.getProjectId(request));
    }

    @PostMapping("/top/index")
    @ResponseBody
    @ApiOperation(value = "获取dashboard大盘TopN索引相关指标信息")
    public Result<List<VariousLineChartMetricsVO>> getTopIndexMetricsInfo(@RequestBody MetricsDashboardTopNDTO param,
                                                                             HttpServletRequest request) {
        return dashboardMetricsManager.getTopIndexMetricsInfo(param, HttpRequestUtils.getProjectId(request));
    }

    @PostMapping("/top/clusterThreadPoolQueue")
    @ResponseBody
    @ApiOperation(value = "获取dashboard大盘TopNES线程池相关指标信息")
    public Result<List<VariousLineChartMetricsVO>> getTopClusterThreadPoolQueueMetricsInfo(@RequestBody MetricsDashboardTopNDTO param,
                                                                          HttpServletRequest request) {
        return dashboardMetricsManager.getTopClusterThreadPoolQueueMetricsInfo(param, HttpRequestUtils.getProjectId(request));
    }

    @PostMapping("/list/cluster")
    @ResponseBody
    @ApiOperation(value = "获取dashboard大盘集群相关list列表指标信息")
    public Result<List<MetricListVO>> getListClusterMetricsInfo(@RequestBody MetricsDashboardListDTO param,
                                                         HttpServletRequest request) {
        return dashboardMetricsManager.getListClusterMetricsInfo(param, HttpRequestUtils.getProjectId(request));
    }

    @PostMapping("/list/node")
    @ResponseBody
    @ApiOperation(value = "获取dashboard大盘节点相关list列表指标信息")
    public Result<List<MetricListVO>> getListNodeMetricsInfo(@RequestBody MetricsDashboardListDTO param,
                                                                HttpServletRequest request) {
        return dashboardMetricsManager.getListNodeMetricsInfo(param, HttpRequestUtils.getProjectId(request));
    }

    @PostMapping("/list/template")
    @ResponseBody
    @ApiOperation(value = "获取dashboard大盘模板相关list列表指标信息")
    public Result<List<MetricListVO>> getListTemplateMetricsInfo(@RequestBody MetricsDashboardListDTO param,
                                                             HttpServletRequest request) {
        return dashboardMetricsManager.getListTemplateMetricsInfo(param, HttpRequestUtils.getProjectId(request));
    }

    @PostMapping("/list/index")
    @ResponseBody
    @ApiOperation(value = "获取dashboard大盘索引相关list列表指标信息")
    public Result<List<MetricListVO>> getListIndexMetricsInfo(@RequestBody MetricsDashboardListDTO param,
                                                                 HttpServletRequest request) {
        return dashboardMetricsManager.getListIndexMetricsInfo(param, HttpRequestUtils.getProjectId(request));
    }
}