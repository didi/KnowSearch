package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.phy;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostWithRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author ohushenglin_v
 * @date 2022-05-30
 */
@RestController
@RequestMapping({ V3_OP + "/phy/cluster/node", V3 + "/cluster/phy/node" })
@Api(tags = "ES物理集群节点接口(REST)")
public class ESPhyClusterNodeController {

    @Autowired
    private ClusterNodeManager clusterNodeManager;

    @GetMapping("/{clusterId}")
    @ResponseBody
    @ApiOperation(value = "获取节点列表")
    public Result<List<ESClusterRoleHostVO>> getClusterPhyRegionInfos(@PathVariable Integer clusterId) {
        return clusterNodeManager.listClusterPhyNode(clusterId);
    }
    @GetMapping("/{clusterId}/instance")
    @ResponseBody
    @ApiOperation(value = "获取节点data实例列表")
    public Result<List<ESClusterRoleHostVO>> getClusterPhyInstance(@PathVariable Integer clusterId) {
        return clusterNodeManager.listClusterPhyInstance(clusterId);
    }

    @GetMapping("/{clusterId}/region/")
    @ResponseBody
    @ApiOperation(value = "获取可划分至region的节点信息")
    public Result<List<ESClusterRoleHostWithRegionInfoVO>> listDivide2ClusterNodeInfo(@PathVariable Long clusterId) {
        return clusterNodeManager.listDivide2ClusterNodeInfo(clusterId);
    }

    @PostMapping("/divide/region")
    @ResponseBody
    @ApiOperation(value = "节点划分且创建region")
    public Result<List<Long>> createMultiNode2Region(HttpServletRequest request, @RequestBody List<ClusterRegionWithNodeInfoDTO> params) {
        return clusterNodeManager.createMultiNode2Region(params, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/divide/region")
    @ResponseBody
    @ApiOperation(value = "编辑多个region中的节点信息（扩缩容）")
    public Result<Boolean> editMultiNode2Region(HttpServletRequest request, @RequestBody List<ClusterRegionWithNodeInfoDTO> params) {
        return clusterNodeManager.editMultiNode2Region(params, HttpRequestUtils.getOperator(request));
    }

}