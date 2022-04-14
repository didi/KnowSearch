package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.cluster;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType.NOT_SUPPORT_ERROR;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

/**
 * 逻辑集群Controller
 *
 * @author d06679
 * @date 2017/10/23
 */
@RestController
@RequestMapping({ V2_OP + "/resource", V2_OP + "/logic/cluster" })
@Api(tags = "es集群逻辑集群接口(REST)")
public class ESLogicClusterController {

    @Autowired
    private ClusterLogicService     clusterLogicService;

    @Autowired
    private ClusterLogicNodeService clusterLogicNodeService;

    @Autowired
    private TemplateLogicManager    templateLogicManager;

    @Autowired
    private ClusterNodeManager      clusterNodeManager;

    @Autowired
    private ClusterLogicManager     clusterLogicManager;

    @Autowired
    private TemplateSrvManager      templateSrvManager;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取所有逻辑集群列表接口", notes = "")
    public Result<List<ConsoleClusterVO>> queryAllLogicClusters(@RequestBody ESLogicClusterDTO param,
                                                                HttpServletRequest request) {
        return Result.buildSucc(clusterLogicManager.getConsoleClusterVOS(param, HttpRequestUtils.getAppId(request)));
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取指定逻辑集群接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "resourceId", value = "逻辑集群ID", required = true) })
    public Result<ConsoleClusterVO> getLogicClusterById(@RequestParam("resourceId") Long resourceId,
                                                        HttpServletRequest request) {
        return Result.buildSucc(
            clusterLogicManager.getConsoleClusterVOByIdAndAppId(resourceId, HttpRequestUtils.getAppId(request)));
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除逻辑集群接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "resourceId", value = "逻辑集群ID", required = true) })
    public Result<Void> deleteLogicClusterById(HttpServletRequest request,
                                         @RequestParam(value = "resourceId") Long resourceId) throws AdminOperateException {
        return clusterLogicManager.deleteLogicCluster(resourceId, HttpRequestUtils.getOperator(request),
            HttpRequestUtils.getAppId(request));
    }

    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建带有region信息的逻辑集群接口", notes = "")
    public Result<Long> createLogicCluster(HttpServletRequest request, @RequestBody ESLogicClusterDTO param) {
        return clusterLogicManager.addLogicCluster(param, HttpRequestUtils.getOperator(request),
            HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "编辑逻辑集群接口", notes = "")
    public Result<Void> modifyLogicCluster(HttpServletRequest request, @RequestBody ESLogicClusterDTO param) {
        return clusterLogicManager.editLogicCluster(param, HttpRequestUtils.getOperator(request),HttpRequestUtils.getAppId(request));
    }

    @GetMapping("/nodes")
    @ResponseBody
    @ApiOperation(value = "获取指定逻辑集群列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true) })
    public Result<List<ESRoleClusterHostVO>> getLogicClusterNodes(@RequestParam(value = "clusterId") Long clusterId) {
        return Result.buildSucc(clusterNodeManager
            .convertClusterLogicNodes(clusterLogicNodeService.getLogicClusterNodesIncludeNonDataNodes(clusterId)));
    }

    @PutMapping("/checkMeta")
    @ResponseBody
    @ApiOperation(value = "元数据校验接口", notes = "")
    public Result<Void> checkAllLogicClustersMeta() {
        return Result.build(NOT_SUPPORT_ERROR);
    }

    @GetMapping("/logicTemplates")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群所有逻辑模板列表", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true) })
    public Result<List<ConsoleTemplateVO>> getClusterLogicTemplates(HttpServletRequest request,
                                                                    @RequestParam(value = "clusterId") Long clusterId) {
        return Result.buildSucc(
            templateLogicManager.getConsoleTemplateVOSForClusterLogic(clusterId, HttpRequestUtils.getAppId(request)));
    }

    // *************************************** RESTFUL  API ***************************************

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群列表接口", notes = "")
    public Result<List<ConsoleClusterVO>> getLogicClusters(@RequestParam ESLogicClusterDTO param) {
        return Result.buildSucc(
            clusterLogicManager.batchBuildOpClusterVOs(clusterLogicService.listClusterLogics(param), null));
    }

    @GetMapping("/{clusterId}")
    @ResponseBody
    @ApiOperation(value = "获取指定逻辑集群接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true) })
    public Result<ConsoleClusterVO> fetchLogicClusterById(@PathVariable(value = "clusterId") Long clusterId) {
        return Result.buildSucc(
            clusterLogicManager.buildOpClusterVO(clusterLogicService.getClusterLogicById(clusterId), null));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "删除逻辑集群接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true) })
    public Result<Void> removeLogicClusterById(HttpServletRequest request,
                                         @RequestParam(value = "clusterId") Long clusterId) throws AdminOperateException {
        return clusterLogicService.deleteClusterLogicById(clusterId, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "新建逻辑集群接口", notes = "")
    public Result<Void> addLogicCluster(HttpServletRequest request, @RequestBody ESLogicClusterWithRegionDTO param) {
        return clusterLogicManager.addLogicClusterAndClusterRegions(param, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "编辑逻辑集群接口", notes = "")
    public Result<Void> updateLogicCluster(HttpServletRequest request, @RequestBody ESLogicClusterDTO param) {
        return clusterLogicService.editClusterLogic(param, HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/{clusterId}/nodes")
    @ResponseBody
    @ApiOperation(value = "获取指定逻辑集群列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true) })
    public Result<List<ESRoleClusterHostVO>> getClusterNodes(@PathVariable(value = "clusterId") Long clusterId) {
        return Result.buildSucc(clusterNodeManager
            .convertClusterLogicNodes(clusterLogicNodeService.getLogicClusterNodesIncludeNonDataNodes(clusterId)));
    }

    @GetMapping("/{clusterId}/templatesrv")
    @ResponseBody
    @ApiOperation(value = "获取集群当前支持的索引服务", notes = "")
    public Result<List<ESClusterTemplateSrvVO>> list(@PathVariable(value = "clusterId") Long clusterId) {
        Result<List<ClusterTemplateSrv>> listResult = templateSrvManager.getLogicClusterTemplateSrv(clusterId);

        if (listResult.failed()) {
            return Result.buildFail(listResult.getMessage());
        }

        return Result.buildSucc(ConvertUtil.list2List(listResult.getData(), ESClusterTemplateSrvVO.class));
    }
}
