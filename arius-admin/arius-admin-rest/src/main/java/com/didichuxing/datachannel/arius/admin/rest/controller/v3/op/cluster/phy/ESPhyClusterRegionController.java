package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.phy;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicSpecCondition;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionWithNodeInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 新版逻辑集群Controller
 * 新的逻辑集群是由Region组成的
 */
@RestController
@RequestMapping({ V3 + "/cluster/phy/region" })
@Api(tags = "ES物理集群region接口(REST)")
public class ESPhyClusterRegionController {

    @Autowired
    private ClusterRegionManager clusterRegionManager;
    @Autowired
    private ClusterNodeManager   clusterNodeManager;

    @Deprecated
    @GetMapping("{cluster}/{clusterLogicType}")
    @ResponseBody
    @ApiOperation(value = "获取物理集群region列表接口，不包含cold region", notes = "支持各种纬度检索集群Region信息")
    public Result<List<ClusterRegionVO>> listPhyClusterRegionsByLogicClusterTypeAndCluster(@PathVariable("cluster") String cluster,
                                                                                           @PathVariable("clusterLogicType") Integer clusterLogicType) {
        return clusterRegionManager.listPhyClusterRegionsByLogicClusterTypeAndCluster(cluster, clusterLogicType);
    }

    @PutMapping("{cluster}/{clusterLogicType}")
    @ResponseBody
    @ApiOperation(value = "根据申请的逻辑集群规格获取满足条件的物理集群region列表，不包含cold region", notes = "支持各种纬度检索集群Region信息")
    public Result<List<ClusterRegionVO>> listPhyClusterRegionsByCondition(@PathVariable("cluster") String cluster,
                                                                          @PathVariable("clusterLogicType") Integer clusterLogicType,
                                                                          @RequestBody ClusterLogicSpecCondition condition) {
        return clusterRegionManager.listPhyClusterRegionsByCondition(cluster, clusterLogicType, condition);
    }

    @GetMapping("/{regionId}/nodes")
    @ResponseBody
    @ApiOperation(value = "获取region下的节点列表", notes = "")
    public Result<List<ESClusterRoleHostVO>> getRegionNodes(@PathVariable Long regionId) {

        return clusterNodeManager.listClusterRoleHostByRegionId(regionId);
    }

    @DeleteMapping("/{regionId}")
    @ResponseBody
    @ApiOperation(value = "删除物理集群region接口", notes = "")
    public Result<Void> removeRegion(HttpServletRequest request,
                                     @PathVariable("regionId") Long regionId) throws AdminOperateException {
        return clusterRegionManager.deletePhyClusterRegion(regionId, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{clusterName}")
    @ResponseBody
    @ApiOperation(value = "根据物理集群名称获region信息，包含region中的数据节点信息")
    public Result<List<ClusterRegionWithNodeInfoVO>> listClusterRegionWithNodeInfoByClusterName(HttpServletRequest request,
                                                                                                @PathVariable String clusterName) {
        return clusterRegionManager.listClusterRegionWithNodeInfoByClusterName(clusterName);
    }

    @GetMapping("/divideType/{clusterId}")
    @ResponseBody
    @ApiOperation(value = "获取当前集群支持的所有attribute划分方式")
    public Result<Set<String>> getClusterAttributeDivideType(@PathVariable Long clusterId) {
        return clusterRegionManager.getClusterAttributeDivideType(clusterId);
    }

    @GetMapping("/divide/{divideType}/{clusterName}")
    @ResponseBody
    @ApiOperation(value = "根据物理集群名称和划分方式获region信息，包含region中的数据节点信息")
    public Result<List<ClusterRegionWithNodeInfoVO>> listClusterRegionInfoWithDivideType(@PathVariable String clusterName,
                                                                                              @PathVariable String divideType) {
        return clusterRegionManager.listClusterRegionInfoWithDivideType(clusterName, divideType);
    }

    @GetMapping("/{clusterName}/dcdr")
    @ResponseBody
    @ApiOperation(value = "获取可分配至dcdr的物理集群名称region列表", notes = "不包含空region")
    public Result<List<ClusterRegionVO>> listNotEmptyClusterRegionByClusterName(@PathVariable String clusterName) {
        return clusterRegionManager.listNotEmptyClusterRegionByClusterName(clusterName);
    }
}