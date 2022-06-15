package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;

@RestController
@RequestMapping(V2_CONSOLE + "/cluster")
@Api(tags = "Console-用户侧集群接口(REST)")
public class ConsoleClusterController {

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private ClusterLogicManager     clusterLogicManager;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取APP拥有的集群列表【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<List<ClusterLogicVO>> getAppLogicClusters(@RequestParam("appId") Integer appId) {
        return clusterLogicManager.getAppLogicClusters(appId);
    }

    @GetMapping("/listAll")
    @ResponseBody
    @ApiOperation(value = "获取平台所有的集群列表【三方接口】",tags = "【三方接口】" )
    public Result<List<ClusterLogicVO>> getDataCenterLogicClusters(@RequestParam(value = "appId",required = false) Integer appId) {
        return clusterLogicManager.getLogicClustersByProjectId(appId);
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取集群详情" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "集群ID", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "APP ID", required = true) })
    public Result<ClusterLogicVO> getAppLogicClusters(@RequestParam("clusterId") Long clusterId,
                                                      @RequestParam("appId") Integer appId) {
        return clusterLogicManager.getAppLogicClusters(clusterId, appId);
    }

    @GetMapping("/logicTemplates")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群所有逻辑模板列表" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "appId", required = true) })
    public Result<List<ConsoleTemplateVO>> getClusterLogicTemplates(HttpServletRequest request,
                                                                    @RequestParam(value = "clusterId") Long clusterId) {
        return clusterLogicManager.getClusterLogicTemplates(request, clusterId);
    }

    @GetMapping("machinespec/list")
    @ResponseBody
    @ApiOperation(value = "获取当前集群支持的套餐列表【三方接口】",tags = "【三方接口】" )
    public Result<List<ESClusterNodeSepcVO>> listMachineSpec() {
        return clusterLogicManager.listMachineSpec();
    }

    @GetMapping("/plugins")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群插件列表(用户侧获取)" )
    public Result<List<PluginVO>> pluginList(Long clusterId) {
        return Result.buildSucc(
            ConvertUtil.list2List(clusterLogicService.getClusterLogicPlugins(clusterId), PluginVO.class));
    }
}
