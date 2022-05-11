package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.phy;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PhyClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanRegionDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 新版逻辑集群Controller
 * 新的逻辑集群是由Region组成的，而不是物理Rack
 *
 * @author wangshu
 * @date 2020/09/20
 */
@RestController
@RequestMapping(V3_OP + "/phy/cluster/region")
@Api(tags = "ES物理集群region接口(REST)")
public class ESPhyClusterRegionController {

    @Autowired
    private ClusterPhyService         clusterPhyService;

    @Autowired
    private RegionRackService         regionRackService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private IndexTemplatePhyService physicalService;

    @Autowired
    private ClusterRegionManager      clusterRegionManager;

    @Autowired
    private CapacityPlanRegionService capacityPlanRegionService;

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取物理集群region列表接口", notes = "支持各种纬度检索集群Region信息")
    public Result<List<ClusterRegionVO>> listPhyClusterRegions(@RequestParam("cluster") String cluster,
                                                               @RequestParam("clusterLogicType") Integer clusterLogicType) {
        return listPhyClusterRegionsAfterFilter(cluster, clusterLogicType, null);
    }

    @GetMapping("/bind")
    @ResponseBody
    @ApiOperation(value = "获取物理集群region列表接口", notes = "支持各种纬度检索集群Region信息")
    public Result<List<ClusterRegionVO>> listPhyClusterRegions(@RequestParam("cluster") String cluster,
                                                               @RequestParam("clusterLogicType") Integer clusterLogicType,
                                                               @RequestParam("clusterLogicId") Long clusterLogicId) {

        return listPhyClusterRegionsAfterFilter(cluster, clusterLogicType, clusterLogicId);
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建物理集群region接口", notes = "")
    public Result<Long> createRegion(HttpServletRequest request, @RequestBody CapacityPlanRegionDTO param) {

        return regionRackService.createPhyClusterRegion(param.getClusterName(), param.getRacks(), param.getShare(),
            HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/phyClusterRacks")
    @ResponseBody
    @ApiOperation(value = "获取物理集群可划分至region的Racks信息", notes = "")
    public Result<List<PhyClusterRackVO>> listPhyClusterRacks(@RequestParam("cluster") String cluster) {
        return Result.buildSucc(clusterRegionManager.buildCanDividePhyClusterRackVOs(cluster));
    }

    @PutMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "修改容量规划region接口", notes = "同时可修改物理集群region的racks")
    public Result<Void> editClusterRegion(HttpServletRequest request, @RequestBody CapacityPlanRegionDTO param) {

        // 当前接口只更改两部分内容：
        // 1. racks（属于物理集群region部分）
        // 2. share、configJson（属于容量规划部分）
        // capacityPlanRegionService.editRegion()中的修改操作包含了对两张表的修改
        return capacityPlanRegionService.editRegion(param, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除物理集群region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<Void> removeRegion(HttpServletRequest request, @RequestParam("regionId") Long regionId) {
        return regionRackService.deletePhyClusterRegion(regionId, HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/{regionId}/nodes")
    @ResponseBody
    @ApiOperation(value = "获取region下的节点列表", notes = "")
    public Result<List<ESClusterRoleHostVO>> getRegionNodes(@PathVariable Long regionId) {

        ClusterRegion region = regionRackService.getRegionById(regionId);
        if (region == null) {
            return Result.buildFail("region不存在");
        }

        List<ClusterRoleHost> hosts = clusterRoleHostService.listRacksNodes(region.getPhyClusterName(),
            region.getRacks());

        return Result.buildSucc(ConvertUtil.list2List(hosts, ESClusterRoleHostVO.class));

    }

    @GetMapping("/{regionId}/templates")
    @ResponseBody
    @ApiOperation(value = "获取Region物理模板列表接口")
    public Result<List<IndexTemplatePhysicalVO>> getRegionPhysicalTemplates(@PathVariable Long regionId) {
        return Result.buildSucc(
            ConvertUtil.list2List(physicalService.getTemplateByRegionId(regionId), IndexTemplatePhysicalVO.class));
    }

    @GetMapping("/{clusterPhyName}/rack")
    @ResponseBody
    @ApiOperation(value = "获取物理集群下的rack列表")
    public Result<Set<String>> getClusterPhyRacks(@PathVariable String clusterPhyName) {
        return Result.buildSucc(clusterPhyService.getClusterRacks(clusterPhyName));
    }

    /**
     * 根据逻辑集群的类型进行过滤
     * @param phyCluster          物理集群名称
     * @param clusterLogicType 逻辑集群类型
     * @param clusterLogicId   逻辑集群id，没有指定的时候为null
     * @return 过滤后形成的region视图列表
     */
    private Result<List<ClusterRegionVO>> listPhyClusterRegionsAfterFilter(String phyCluster, Integer clusterLogicType, Long clusterLogicId) {
        if (StringUtils.isBlank(phyCluster) || AriusObjUtils.isNull(clusterLogicType)) {
            return Result.buildSucc(new ArrayList<>());
        }

        //根据逻辑集群类型筛选物理集群下可以使用的region
        List<ClusterRegion> regions = clusterRegionManager.filterClusterRegionByLogicClusterType(clusterLogicId, phyCluster, clusterLogicType);
        return Result.buildSucc(clusterRegionManager.buildLogicClusterRegionVO(regions));
    }
}