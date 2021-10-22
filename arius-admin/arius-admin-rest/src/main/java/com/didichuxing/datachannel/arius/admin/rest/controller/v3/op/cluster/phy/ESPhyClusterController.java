package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.phy;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterPhyRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterVO;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController("esPhyClusterControllerV3")
@RequestMapping(V3_OP + "/phy/cluster")
@Api(value = "es物理集群集群接口(REST)")
public class ESPhyClusterController {

    @Autowired
    private ESClusterPhyService esClusterPhyService;

    @Autowired
    private ClusterPhyManager   clusterPhyManager;

    @Autowired
    private ESPluginService     esPluginService;

    @Autowired
    private ESPackageService    packageService;

    @Autowired
    private EcmHandleService    ecmHandleService;

    /**
     * 根据物理集群ID获取全部角色
     */
    @GetMapping("{clusterId}/roles")
    @ResponseBody
    @ApiOperation(value = "根据物理集群ID获取全部角色列表", notes = "")
    public Result<List<ESRoleClusterVO>> roleList(@PathVariable Integer clusterId) {
        List<ESRoleCluster> esRoleClusters = esClusterPhyService.listPhysicClusterRoles(clusterId);

        if (ValidateUtils.isNull(esRoleClusters)) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(ConvertUtil.list2List(esRoleClusters, ESRoleClusterVO.class));
    }

    @DeleteMapping("/plugin/{id}")
    @ResponseBody
    @ApiOperation(value = "删除插件接口", notes = "")
    @Deprecated
    public Result<Long> pluginDelete(HttpServletRequest request, @PathVariable Long id) {
        return esPluginService.deletePluginById(id, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/package/{id}")
    @ResponseBody
    @ApiOperation(value = "删除程序包接口", notes = "")
    public Result<Long> packageDelete(HttpServletRequest request, @PathVariable Long id) {
        return packageService.deleteESPackage(id, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/{clusterId}")
    @ResponseBody
    @ApiOperation(value = "删除物理集群接口", notes = "")
    @Deprecated
    public Result clusterRemove(HttpServletRequest request, @PathVariable(value = "clusterId") Long clusterId) {
        return ecmHandleService.deleteESCluster(clusterId, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/join")
    @ResponseBody
    @ApiOperation(value = "接入集群", notes = "当前版本仅限host类型")
    public Result clusterJoin(HttpServletRequest request, @RequestBody ESClusterJoinDTO param) {
        return clusterPhyManager.clusterJoin(param, HttpRequestUtils.getOperator(request));
    }

    @GetMapping("{clusterId}/regioninfo")
    @ResponseBody
    @ApiOperation(value = "获取节点划分列表")
    public Result<List<ESClusterPhyRegionInfoVO>> getClusterPhyRegionInfos(@PathVariable Integer clusterId) {
        return clusterPhyManager.getClusterPhyRegionInfos(clusterId);
    }

    @GetMapping("{clusterLogicType}/list")
    @ResponseBody
    @ApiOperation(value = "获取可用物理集群列表")
    public Result<List<String>> listCanBeAssociatedClustersPhys(@PathVariable Integer clusterLogicType){
        return clusterPhyManager.listCanBeAssociatedClustersPhys(clusterLogicType);
    }

    @GetMapping("{ips}/check")
    @ResponseBody
    @ApiOperation(value = "校验节点ip是否有效")
    public Result checkValidForClusterNodes(@PathVariable List<String> ips){
        return clusterPhyManager.checkValidForClusterNodes(ips);
    }
}
