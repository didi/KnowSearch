package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicNodeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 逻辑集群Controller
 *
 * @author d06679
 * @date 2017/10/23
 */
@RestController
@RequestMapping({ V2_OP })
@Api(tags = "es集群逻辑集群接口(REST)")
public class ESLogicClusterController {

    @Autowired
    private ClusterLogicNodeService clusterLogicNodeService;

    @Autowired
    private ClusterNodeManager      clusterNodeManager;

    @Autowired
    private ClusterLogicManager     clusterLogicManager;

    @PostMapping("/resource/list")
    @ResponseBody
    @ApiOperation(value = "获取所有逻辑集群列表接口【三方接口】",tags = "【三方接口】" )

    public Result<List<ConsoleClusterVO>> queryAllLogicClusters(@RequestBody ESLogicClusterDTO param,
                                                                HttpServletRequest request) {
        return Result.buildSucc(clusterLogicManager.getConsoleClusterVOS(param, HttpRequestUtils.getAppId(request)));
    }

    @GetMapping("/resource/get")
    @ResponseBody
    @ApiOperation(value = "获取指定逻辑集群接口【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "resourceId", value = "逻辑集群ID", required = true) })

    public Result<ConsoleClusterVO> getLogicClusterById(@RequestParam("resourceId") Long resourceId,
                                                        HttpServletRequest request) {
        return Result.buildSucc(
            clusterLogicManager.getConsoleClusterVOByIdAndAppId(resourceId, HttpRequestUtils.getAppId(request)));
    }

    @DeleteMapping("/resource/del")
    @ResponseBody
    @ApiOperation(value = "删除逻辑集群接口【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "resourceId", value = "逻辑集群ID", required = true) })

    public Result<Void> deleteLogicClusterById(HttpServletRequest request,
                                         @RequestParam(value = "resourceId") Long resourceId) throws AdminOperateException {
        return clusterLogicManager.deleteLogicCluster(resourceId, HttpRequestUtils.getOperator(request),
            HttpRequestUtils.getAppId(request));
    }

    @PutMapping("/resource/add")
    @ResponseBody
    @ApiOperation(value = "新建带有region信息的逻辑集群接口【三方接口】",tags = "【三方接口】" )

    public Result<Long> createLogicCluster(HttpServletRequest request, @RequestBody ESLogicClusterDTO param) {
        return clusterLogicManager.addLogicCluster(param, HttpRequestUtils.getOperator(request),
            HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/resource/edit")
    @ResponseBody
    @ApiOperation(value = "编辑逻辑集群接口【三方接口】",tags = "【三方接口】" )

    public Result<Void> modifyLogicCluster(HttpServletRequest request, @RequestBody ESLogicClusterDTO param) {
        return clusterLogicManager.editLogicCluster(param, HttpRequestUtils.getOperator(request),HttpRequestUtils.getAppId(request));
    }

    @GetMapping("/logic/cluster/nodes")
    @ResponseBody
    @ApiOperation(value = "获取指定逻辑集群列表接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true) })

    public Result<List<ESClusterRoleHostVO>> getLogicClusterNodes(@RequestParam(value = "clusterId") Long clusterId) {
        return Result.buildSucc(clusterNodeManager
            .convertClusterLogicNodes(clusterLogicNodeService.getLogicClusterNodesIncludeNonDataNodes(clusterId)));
    }
}
