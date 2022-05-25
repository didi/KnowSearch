package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.phy;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostWithRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3_OP + "/phy/cluster/node")
@Api(tags = "ES物理集群节点接口(REST)")
public class ESPhyClusterNodeController {

    @Autowired
    private ClusterNodeManager clusterNodeManager;

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