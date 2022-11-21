package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.phy;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterNodeInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostWithRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ohushenglin_v
 * @date 2022-05-30
 */
@RestController
@RequestMapping({ V3 + "/cluster/phy/node" })
@Api(tags = "ES物理集群节点接口(REST)")
public class ESPhyClusterNodeController {

    @Autowired
    private ClusterNodeManager clusterNodeManager;

    @Autowired
    private ClusterPhyManager  clusterPhyManager;

    @GetMapping("/{clusterId}")
    @ResponseBody
    @ApiOperation(value = "获取节点列表")
    public Result<List<ESClusterRoleHostVO>> getClusterPhyRegionInfos(@PathVariable Integer clusterId) {
        return clusterNodeManager.listClusterPhyNode(clusterId);
    }


    @GetMapping("/{clusterId}/region/")
    @ResponseBody
    @Deprecated
    @ApiOperation(value = "获取可划分至region的节点信息")
    public Result<List<ESClusterRoleHostWithRegionInfoVO>> listDivide2ClusterNodeInfo(@PathVariable Long clusterId) {
        return clusterNodeManager.listDivide2ClusterNodeInfo(clusterId);
    }

    @GetMapping("/{clusterId}/region/{divideType}")
    @ResponseBody
    @ApiOperation(value = "获取可划分至region的节点信息")
    public Result<List<ESClusterRoleHostWithRegionInfoVO>> listDivide2ClusterNodeInfo(@PathVariable Long clusterId,
                                                                                      @PathVariable String divideType) {
        return clusterNodeManager.listDivide2ClusterNodeInfoWithDivideType(clusterId, divideType);
    }

    @PostMapping("/divide-region-check")
    @ResponseBody
    @ApiOperation(value = "节点划分校验")
    public Result<Boolean> checkMultiNode2Region(HttpServletRequest request,
                                                     @RequestBody List<ClusterRegionWithNodeInfoDTO> params) throws AdminOperateException {

        return clusterNodeManager.checkMultiNode2Region(params, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/divide-region")
    @ResponseBody
    @ApiOperation(value = "节点划分且创建region")
    public Result<List<Long>> createMultiNode2Region(HttpServletRequest request,
                                                     @RequestBody List<ClusterRegionWithNodeInfoDTO> params) throws AdminOperateException {

        return clusterNodeManager.createMultiNode2Region(params, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/divide-region")
    @ResponseBody
    @ApiOperation(value = "编辑多个region中的节点信息（扩缩容）")
    public Result<Boolean> editMultiNode2Region(HttpServletRequest request,
                                                @RequestBody List<ClusterRegionWithNodeInfoDTO> params) throws AdminOperateException {

        return clusterNodeManager.editMultiNode2Region(params, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request), OperationEnum.EDIT);
    }

    @GetMapping("/{clusterPhyName}/names")
    @ResponseBody
    @ApiOperation(value = "获取物理集群下的节点名称")
    public Result<List<String>> listClusterPhyNodeName(@PathVariable String clusterPhyName) {
        return Result.buildSucc(clusterPhyManager.listClusterPhyNodeName(clusterPhyName));
    }

    @GetMapping("/names")
    @ResponseBody
    @ApiOperation(value = "根据projectId获取物理集群下的节点名称")
    public Result<List<String>> getAppNodeNames(HttpServletRequest request) {
        return Result.buildSucc(clusterPhyManager.listNodeNameByProjectId(HttpRequestUtil.getProjectId(request)));
    }

    @PutMapping("/{clusterPhyName}/collect-settings")
    @ResponseBody
    @ApiOperation(value = "采集集群节点配置信息接口【三方接口】", tags = "【三方接口】")
    @ApiImplicitParam(type = "String", name = "phyClusterName", value = "物理集群名称", required = true)
    public Result<Void> collectNodeSettings(@PathVariable String clusterPhyName) throws AdminTaskException {
        return Result.build(clusterNodeManager.collectNodeSettings(clusterPhyName));
    }
    
    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "批量下线离线节点")
    @ApiImplicitParam(type = "String", name = "phyClusterName", value = "物理集群名称", required = true)
    public Result<Void> delete(@RequestBody List<Integer> ids, HttpServletRequest request) {
        return clusterNodeManager.delete(ids, HttpRequestUtil.getProjectId(request),
                        HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/{clusterPhyName}/infos")
    @ResponseBody
    @ApiOperation(value = "获取物理集群下的节点信息")
    public Result<List<ClusterNodeInfoVO>> listClusterLogicNodeInfos(@PathVariable String clusterPhyName) {
        return clusterNodeManager.listClusterPhyNodeInfosByName(clusterPhyName);
    }

    @GetMapping("/machineSpecs")
    @ResponseBody
    @ApiOperation(value = "获取当前平台所有的机器规格")
    public Result<List<String>> listAllMachineSpecs() {
        return clusterNodeManager.listAllMachineSpecs();
    }
}