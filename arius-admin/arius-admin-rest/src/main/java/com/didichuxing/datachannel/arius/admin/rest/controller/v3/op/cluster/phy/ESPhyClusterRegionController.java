package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.phy;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionWithNodeInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping({ V3 + "/cluster/phy/region" })
@Api(tags = "ES物理集群region接口(REST)")
public class ESPhyClusterRegionController {

    @Autowired
    private ClusterRegionService    clusterRegionService;

    @Autowired
    private ClusterRoleHostService  clusterRoleHostService;

    @Autowired
    private IndexTemplatePhyService physicalService;

    @Autowired
    private ClusterRegionManager    clusterRegionManager;

    @GetMapping("{cluster}/{clusterLogicType}")
    @ResponseBody
    @ApiOperation(value = "获取物理集群region列表接口", notes = "支持各种纬度检索集群Region信息")
    @Deprecated
    public Result<List<ClusterRegionVO>> listPhyClusterRegions(@PathVariable("cluster") String cluster,
                                                               @PathVariable("clusterLogicType") Integer clusterLogicType) {
        return listPhyClusterRegionsAfterFilter(cluster, clusterLogicType, null);
    }

    @GetMapping("/bind")
    @ResponseBody
    @ApiOperation(value = "获取物理集群region列表接口", notes = "支持各种纬度检索集群Region信息")
    @Deprecated
    public Result<List<ClusterRegionVO>> listPhyClusterRegions(@RequestParam("cluster") String cluster,
                                                               @RequestParam("clusterLogicType") Integer clusterLogicType,
                                                               @RequestParam("clusterLogicId") Long clusterLogicId) {

        return listPhyClusterRegionsAfterFilter(cluster, clusterLogicType, clusterLogicId);
    }

    @DeleteMapping("/{regionId}")
    @ResponseBody
    @ApiOperation(value = "删除物理集群region接口", notes = "")
    public Result<Void> removeRegion(HttpServletRequest request, @PathVariable("regionId") Long regionId) {
        return clusterRegionService.deletePhyClusterRegion(regionId, HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/{clusterName}")
    @ResponseBody
    @ApiOperation(value = "根据物理集群名称获region信息，包含region中的数据节点信息")
    public Result<List<ClusterRegionWithNodeInfoVO>> listClusterRegionWithNodeInfoByClusterName(HttpServletRequest request, @PathVariable String clusterName) {
        return clusterRegionManager.listClusterRegionWithNodeInfoByClusterName(clusterName);
    }

    @GetMapping("/{clusterName}/dcdr")
    @ResponseBody
    @ApiOperation(value = "获取可分配至dcdr的物理集群名称获region列表", notes = "不包含空region")
    public Result<List<ClusterRegionVO>> listNoEmptyClusterRegionByClusterName(@PathVariable String clusterName) {
        return clusterRegionManager.listNoEmptyClusterRegionByClusterName(clusterName);
    }

    @GetMapping("/{regionId}/nodes")
    @ResponseBody
    @ApiOperation(value = "获取region下的节点列表", notes = "")
    @Deprecated
    public Result<List<ESClusterRoleHostVO>> getRegionNodes(@PathVariable Long regionId) {

        ClusterRegion region = clusterRegionService.getRegionById(regionId);
        if (region == null) {
            return Result.buildFail("region不存在");
        }
        Result<List<ClusterRoleHost>> ret = clusterRoleHostService.listByRegionId(region.getId().intValue());
        if (ret.success()) {
            return Result.buildSucc(ConvertUtil.list2List(ret.getData(), ESClusterRoleHostVO.class));
        }
        return Result.buildFail();

    }

    @GetMapping("/{regionId}/templates")
    @ResponseBody
    @ApiOperation(value = "获取Region物理模板列表接口")
    @Deprecated
    public Result<List<IndexTemplatePhysicalVO>> getRegionPhysicalTemplates(@PathVariable Long regionId) {
        Result<List<IndexTemplatePhy>> ret = physicalService.listByRegionId(regionId.intValue());
        if (ret.failed()) { return Result.buildFrom(ret);}
        return Result.buildSucc(ConvertUtil.list2List(ret.getData(), IndexTemplatePhysicalVO.class));
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