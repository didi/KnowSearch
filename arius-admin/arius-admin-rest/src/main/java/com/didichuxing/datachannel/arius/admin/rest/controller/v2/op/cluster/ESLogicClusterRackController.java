package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterRackInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.LogicClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 逻辑集群Rack Controller
 *
 * ES 3.0之后逻辑集群是由Region组成的，不感知具体的Rack信息，所以这块逻辑后续要
 * 被清理掉。
 *
 * @author wangshu
 * @date 2020/09/22
 */
@RestController
@RequestMapping({ V2_OP + "/resource/item", V2_OP + "/logic/cluster/item", V2_OP + "/logic/cluster/racks" })
@Api(value = "es集群资源接口(REST)")
@Deprecated
public class ESLogicClusterRackController {

    @Autowired
    private ESRegionRackService esRegionRackService;

    @Autowired
    private ClusterRegionManager clusterRegionManager;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取逻辑资源列表接口", notes = "")
    @Deprecated
    public Result<List<LogicClusterRackVO>> queryLogicClusterRacks(@RequestBody ESLogicClusterRackInfoDTO param) {

        List<ESClusterLogicRackInfo> logicClusterRackInfos = esRegionRackService.listLogicClusterRacks(param);

        return Result.buildSucc( clusterRegionManager.buildLogicClusterRackVOs(logicClusterRackInfos));
    }

    @GetMapping("/getByResource")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群所有Rack信息", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "resourceId", value = "逻辑集群ID", required = true) })
    @Deprecated
    public Result<List<LogicClusterRackVO>> listLogicClusterRacks(@RequestParam(value = "resourceId") Long resourceId) {

        List<ESClusterLogicRackInfo> logicClusterRackInfos = esRegionRackService.listLogicClusterRacks(resourceId);

        return Result.buildSucc( clusterRegionManager.buildLogicClusterRackVOs(logicClusterRackInfos));
    }

    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "创建逻辑集群Rack信息", notes = "")
    @Deprecated
    public Result createLogicClusterRack(HttpServletRequest request,
                                         @RequestBody ESLogicClusterRackInfoDTO param) throws AdminOperateException {
        return esRegionRackService.addRackToLogicCluster(param, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除逻辑集群Rack信息", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "itemId", value = "映射ID", required = true) })
    @Deprecated
    public Result deleteLogicClusterRack(HttpServletRequest request, @RequestParam(value = "itemId") Long itemId) {
        return Result.buildSucc(esRegionRackService.deleteRackById(itemId));
    }

    // *************************************** RESTFUL  API ***************************************

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取逻辑资源列表接口", notes = "")
    public Result<List<LogicClusterRackVO>> listLogicClusterRacks(@RequestParam ESLogicClusterRackInfoDTO param) {

        List<ESClusterLogicRackInfo> logicClusterRackInfos = esRegionRackService.listLogicClusterRacks(param);

        return Result.buildSucc( clusterRegionManager.buildLogicClusterRackVOs(logicClusterRackInfos));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "通过RackId删除Rack", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "rackId", value = "映射ID", required = true) })
    public Result deleteRackById(HttpServletRequest request, @RequestParam(value = "rackId") Long rackId) {
        return Result.buildSucc(esRegionRackService.deleteRackById(rackId));
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "创建逻辑集群Rack信息", notes = "")
    public Result createRack(HttpServletRequest request,
                             @RequestBody ESLogicClusterRackInfoDTO param) throws AdminOperateException {
        return esRegionRackService.addRackToLogicCluster(param, HttpRequestUtils.getOperator(request));
    }

}
