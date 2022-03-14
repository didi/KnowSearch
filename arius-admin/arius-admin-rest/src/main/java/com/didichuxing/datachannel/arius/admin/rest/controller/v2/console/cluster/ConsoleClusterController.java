package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.cluster;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ConsoleLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

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
    @ApiOperation(value = "获取APP拥有的集群列表", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<List<ConsoleClusterVO>> getAppLogicClusters(@RequestParam("appId") Integer appId) {
        return clusterLogicManager.getAppLogicClusters(appId);
    }

    @GetMapping("/listAll")
    @ResponseBody
    @ApiOperation(value = "获取平台所有的集群列表", notes = "")
    public Result<List<ConsoleClusterVO>> getDataCenterLogicClusters(@RequestParam("appId") Integer appId) {
        return clusterLogicManager.getDataCenterLogicClusters(appId);
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取集群详情", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "集群ID", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "APP ID", required = true) })
    public Result<ConsoleClusterVO> getAppLogicClusters(@RequestParam("clusterId") Long clusterId,
                                                        @RequestParam("appId") Integer appId) {
        return clusterLogicManager.getAppLogicClusters(clusterId, appId);
    }

    @PutMapping("/update")
    @ResponseBody
    @ApiOperation(value = "用户编辑集群接口", notes = "支持修改责任人、备注、成本部门")
    public Result<Void> editLogicClusterConfig(HttpServletRequest request,
                                         @RequestBody ConsoleLogicClusterDTO resourceLogicDTO) {
        return clusterLogicService
                .editClusterLogic(ConvertUtil.obj2Obj(resourceLogicDTO, ESLogicClusterDTO.class),
            HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/offlineInfo")
    @ResponseBody
    @ApiOperation(value = "集群下线信息接口", notes = "集群下线信息接口")
    public Result<List<ConsoleAppVO>> getAccessAppsOfLogicCluster(@RequestParam("logicClusterId") Long logicClusterId) {
        return clusterLogicManager.getAccessAppsOfLogicCluster(logicClusterId);
    }

    @GetMapping("/logicTemplates")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群所有逻辑模板列表", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "appId", required = true) })
    public Result<List<ConsoleTemplateVO>> getClusterLogicTemplates(HttpServletRequest request,
                                                                    @RequestParam(value = "clusterId") Long clusterId) {
        return clusterLogicManager.getClusterLogicTemplates(request, clusterId);
    }

    @GetMapping("/nodes")
    @ResponseBody
    @ApiOperation(value = "获取指定罗集群节点列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true) })
    public Result<List<ESRoleClusterHostVO>> getLogicClusterNodes(@RequestParam(value = "clusterId") Long clusterId) {
        return clusterLogicManager.getLogicClusterNodes(clusterId);

    }

    @GetMapping("/dataspec")
    @ResponseBody
    @ApiOperation(value = "获取指定逻辑集群datanode的规格接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true) })
    public Result<Set<ESClusterNodeSepcVO>> getLogicClusterDataNodeSpec(@RequestParam(value = "clusterId") Long clusterId) {
        return clusterLogicManager.getLogicClusterDataNodeSpec(clusterId);
    }

    @GetMapping("machinespec/list")
    @ResponseBody
    @ApiOperation(value = "获取当前集群支持的套餐列表", notes = "")
    public Result<List<ESClusterNodeSepcVO>> listMachineSpec() {
        return clusterLogicManager.listMachineSpec();
    }

    @GetMapping("/plugins")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群插件列表(用户侧获取)", notes = "")
    public Result<List<PluginVO>> pluginList(Long clusterId) {
        return Result.buildSucc(
            ConvertUtil.list2List(clusterLogicService.getClusterLogicPlugins(clusterId), PluginVO.class));
    }

    @PostMapping("/plugin")
    @ResponseBody
    @ApiOperation(value = "添加逻辑集群插件(用户侧添加)", notes = "")
    public Result<Long> addPlugin(HttpServletRequest request, Long logicClusterId, PluginDTO pluginDTO) {
        return clusterLogicService.addPlugin(logicClusterId, pluginDTO, HttpRequestUtils.getOperator(request));
    }
}
