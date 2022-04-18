package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterRackInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.LogicClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

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
@RequestMapping({V2_OP + "/resource/item", V2_OP + "/logic/cluster/item", V2_OP + "/logic/cluster/racks" })
@Api(tags = "es集群资源接口(REST)")
public class ESLogicClusterRackController {

    @Autowired
    private RegionRackService regionRackService;

    @Autowired
    private ClusterRegionManager clusterRegionManager;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取逻辑资源Rack列表接口", notes = "")
    @Deprecated
    public Result<List<LogicClusterRackVO>> listLogicClusterRacks(@RequestBody ESLogicClusterRackInfoDTO param) {
        if (null != param.getResourceId() && (null == param.getLogicClusterId() || param.getLogicClusterId() < 1)) {
            param.setLogicClusterId(param.getResourceId());
        }
        if (StringUtils.isNotBlank(param.getCluster()) && StringUtils.isBlank(param.getPhyClusterName())) {
            param.setPhyClusterName(param.getCluster());
        }
        return listLogicClusterRacksInner(param);
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取逻辑资源Rack列表接口", notes = "")
    public Result<List<LogicClusterRackVO>> listLogicClusterRacks1(@RequestParam ESLogicClusterRackInfoDTO param) {
        return listLogicClusterRacksInner(param);
    }

    @GetMapping("/getByResource")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群所有Rack信息", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "resourceId", value = "逻辑集群ID", required = true) })
    @Deprecated
    public Result<List<LogicClusterRackVO>> listLogicClusterRacks(@RequestParam(value = "resourceId") Long resourceId) {
        List<ClusterLogicRackInfo> logicClusterRackInfos = regionRackService.listLogicClusterRacks(resourceId);
        return Result.buildSucc( clusterRegionManager.buildLogicClusterRackVOs(logicClusterRackInfos));
    }

    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "创建逻辑集群Rack信息", notes = "")
    @Deprecated
    public Result<Void> createLogicClusterRack(HttpServletRequest request,
                                         @RequestBody ESLogicClusterRackInfoDTO param) {
        return regionRackService.addRackToLogicCluster(param, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除逻辑集群Rack信息", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "itemId", value = "映射ID", required = true) })
    @Deprecated
    public Result<Boolean> deleteLogicClusterRack(HttpServletRequest request, @RequestParam(value = "itemId") Long itemId) {
        return Result.buildSucc(regionRackService.deleteRackById(itemId));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "通过RackId删除Rack", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "rackId", value = "映射ID", required = true) })
    public Result<Boolean> deleteRackById(HttpServletRequest request, @RequestParam(value = "rackId") Long rackId) {
        return Result.buildSucc(regionRackService.deleteRackById(rackId));
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "创建逻辑集群Rack信息", notes = "")
    public Result<Void> createRack(HttpServletRequest request,
                             @RequestBody ESLogicClusterRackInfoDTO param) {
        return regionRackService.addRackToLogicCluster(param, HttpRequestUtils.getOperator(request));
    }

    public Result<List<LogicClusterRackVO>> listLogicClusterRacksInner(@RequestParam ESLogicClusterRackInfoDTO param) {
        List<ClusterLogicRackInfo> logicClusterRackInfos = regionRackService.listLogicClusterRacks(param);
        return Result.buildSucc(clusterRegionManager.buildLogicClusterRackVOs(logicClusterRackInfos));
    }
}
