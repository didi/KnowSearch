package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.capacityplan;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanAreaDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanRegionDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanArea;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionBalanceItem;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionSplitResult;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.vo.CapacityPlanAreaVO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.vo.CapacityPlanRegionTaskItemVO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.vo.CapacityPlanRegionTaskVO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.vo.CapacityPlanRegionVO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionTaskService;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 *
 *
 * @author d06679
 * @date 2017/10/9
 */
@RestController
@RequestMapping(V2_OP + "/capacity/plan")
@Api(tags = "容量规划接口(REST)")
public class CapacityPlanController {

    @Autowired
    private CapacityPlanAreaService       capacityPlanAreaService;

    @Autowired
    private CapacityPlanRegionService     capacityPlanRegionService;

    @Autowired
    private CapacityPlanRegionTaskService capacityPlanRegionTaskService;

    @Autowired
    private RegionRackService regionRackService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    @GetMapping("/area/list")
    @ResponseBody
    @ApiOperation(value = "查询规划Area接口", notes = "")
    public Result<List<CapacityPlanAreaVO>> listAllPlanAreas() {
        List<CapacityPlanAreaVO> result = Lists.newArrayList();

        Map<Long, ClusterLogic> resourceId2ResourceLogicMap =
                ConvertUtil.list2Map(clusterLogicService.listAllClusterLogics(),
                        ClusterLogic::getId);

        List<CapacityPlanArea> planClusters = capacityPlanAreaService.listAllPlanAreas();
        for (CapacityPlanArea planCluster : planClusters) {
            CapacityPlanAreaVO areaVO = ConvertUtil.obj2Obj(planCluster, CapacityPlanAreaVO.class);
            areaVO.setFreeRacks(String.join(",", capacityPlanAreaService.listAreaFreeRacks(
                    planCluster.getResourceId())));

            ClusterLogic resourceLogic = resourceId2ResourceLogicMap.get(planCluster.getResourceId());
            if(null == resourceLogic){continue;}

            areaVO.setResourceName(resourceLogic.getName());

            result.add(areaVO);
        }

        return Result.buildSucc(result);
    }

    @PostMapping("/area/add")
    @ResponseBody
    @ApiOperation(value = "新建规划Area接口", notes = "")
    public Result<Long> addCluster(HttpServletRequest request, @RequestBody CapacityPlanAreaDTO param) {
        return capacityPlanAreaService.createPlanAreaInNotExist(
                param, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/area/edit")
    @ResponseBody
    @ApiOperation(value = "修改规划Area接口", notes = "")
    public Result<Void> editCluster(HttpServletRequest request, @RequestBody CapacityPlanAreaDTO param) {
        return capacityPlanAreaService.modifyPlanArea(param, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/area/delete")
    @ResponseBody
    @ApiOperation(value = "删除规划Area接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "areaId", value = "AreaID", required = true) })
    public Result<Void> delCluster(HttpServletRequest request, @RequestParam("areaId") Long areaId) {
        return capacityPlanAreaService.deletePlanArea(areaId, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/area/initRegion")
    @ResponseBody
    @ApiOperation(value = "初始化Area内所有region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "areaId", value = "AreaID", required = true) })
    public Result<List<CapacityPlanRegion>> initClusterRegion(HttpServletRequest request, @RequestParam("areaId") Long areaId) {
        return capacityPlanAreaService.initRegionsInPlanArea(areaId, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/area/planRegion")
    @ResponseBody
    @ApiOperation(value = "规划Area内所有region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "areaId", value = "AreaID", required = true) })
    public Result<Void> planClusterAllRegion(HttpServletRequest request,
                                       @RequestParam("areaId") Long areaId) throws ESOperateException {
        return capacityPlanAreaService.planRegionsInArea(areaId);
    }

    @PutMapping("/area/checkRegion")
    @ResponseBody
    @ApiOperation(value = "检查Area内所有region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "areaId", value = "AreaID", required = true) })
    public Result<Void> checkClusterAllRegion(HttpServletRequest request, @RequestParam("areaId") Long areaId) {
        return capacityPlanAreaService.checkRegionsInArea(areaId);
    }

    @GetMapping("/area/region/list")
    @ResponseBody
    @ApiOperation(value = "查询规划集群region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "areaId", value = "AreaID", required = true) })
    public Result<List<CapacityPlanRegionVO>> listClusterRegion(@RequestParam("areaId") Long areaId) {
        return Result.buildSucc(ConvertUtil.list2List(capacityPlanRegionService.listRegionsInArea(areaId), CapacityPlanRegionVO.class));
    }

    @PostMapping("/area/region/add")
    @ResponseBody
    @ApiOperation(value = "新建规划集群region接口", notes = "")
    public Result<Long> addClusterRegion(HttpServletRequest request, @RequestBody CapacityPlanRegionDTO param) {

        return regionRackService.createPhyClusterRegion(
            param.getClusterName(), param.getRacks(), param.getShare(), HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/area/region/edit")
    @ResponseBody
    @ApiOperation(value = "修改规划集群region接口", notes = "")
    public Result<Void> editClusterRegion(HttpServletRequest request, @RequestBody CapacityPlanRegionDTO param) {
        return capacityPlanRegionService.editRegion(param, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/area/region/delete")
    @ResponseBody
    @ApiOperation(value = "删除规划集群region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<Void> delClusterRegion(HttpServletRequest request, @RequestParam("regionId") Long regionId) {
        return regionRackService.deletePhyClusterRegion(regionId, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/area/region/plan")
    @ResponseBody
    @ApiOperation(value = "规划集群region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<Void> planClusterRegion(HttpServletRequest request,
                                    @RequestParam("regionId") Long regionId) throws ESOperateException {
        return capacityPlanRegionService.planRegion(regionId);
    }

    @PutMapping("/area/region/check")
    @ResponseBody
    @ApiOperation(value = "检查集群region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<Void> checkClusterRegion(HttpServletRequest request, @RequestParam("regionId") Long regionId) {
        return capacityPlanRegionService.checkRegion(regionId);
    }

    @PutMapping("/area/region/moveShard")
    @ResponseBody
    @ApiOperation(value = "纠正集群region模板和索引资源接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<Void> moveShardClusterRegion(HttpServletRequest request, @RequestParam("regionId") Long regionId) {
        return capacityPlanRegionService.moveShard(regionId, true);
    }

    @PutMapping("/area/region/moveTemplate")
    @ResponseBody
    @ApiOperation(value = "纠正集群region-模板资源接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "booleam", name = "moveIndex", value = "moveIndex", required = true) })
    public Result<Void> moveTemplateClusterRegion(HttpServletRequest request, @RequestParam("regionId") Long regionId,
                                            @RequestParam("moveIndex") Boolean moveIndex) {
        return capacityPlanRegionService.moveShard(regionId, moveIndex);
    }

    @GetMapping("/area/region/task/list")
    @ResponseBody
    @ApiOperation(value = "查询规划集群region的任务接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<List<CapacityPlanRegionTaskVO>> listClusterRegionTask(@RequestParam("regionId") Long regionId) {
        return Result.buildSucc(ConvertUtil.list2List(capacityPlanRegionTaskService.getTaskByRegionId(regionId),
            CapacityPlanRegionTaskVO.class));
    }

    @PutMapping("/area/region/task/execute")
    @ResponseBody
    @ApiOperation(value = "执行初始化规划集群region任务接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "taskId", value = "任务ID", required = true) })
    public Result<Void> exeClusterRegionTask(@RequestParam("taskId") Long taskId) {
        return capacityPlanRegionTaskService.exeInitTask(taskId);
    }

    @PutMapping("/area/region/task/check")
    @ResponseBody
    @ApiOperation(value = "检查规划任务是否完成接口", notes = "")
    public Result<Void> checkClusterRegionTask() {
        return capacityPlanRegionTaskService.checkTasks();
    }

    @GetMapping("/area/region/task/listItem")
    @ResponseBody
    @ApiOperation(value = "查询规划集群region的任务明细接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "taskId", value = "任务ID", required = true) })
    public Result<List<CapacityPlanRegionTaskItemVO>> listClusterRegionTaskItem(@RequestParam("taskId") Long taskId) {
        return Result.buildSucc(ConvertUtil.list2List(capacityPlanRegionTaskService.getTaskItemByTaskId(taskId),
            CapacityPlanRegionTaskItemVO.class));
    }

    @PutMapping("/area/checkMeta")
    @ResponseBody
    @ApiOperation(value = "规划area元数据校验", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "areaId", value = "areaID", required = true) })
    public Result<Void> checkMeta(@RequestParam("areaId") Long areaId) {
        return Result.build(capacityPlanAreaService.correctAreaRegionAndTemplateRacks(areaId));
    }

    @PutMapping("/area/balance")
    @ResponseBody
    @ApiOperation(value = "均匀region", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "areaId", value = "areaID", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "BOOLEAN", name = "exe", value = "是否生效", defaultValue = "false") })
    public Result<List<CapacityPlanRegionBalanceItem>> balanceRegion(@RequestParam("areaId") Long areaId,
                                                                     @RequestParam("exe") boolean exe) {
        return capacityPlanRegionService.balanceRegion(areaId, exe);
    }

    @PutMapping("/area/region/split")
    @ResponseBody
    @ApiOperation(value = "拆分region", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "BOOLEAN", name = "exe", value = "是否生效", defaultValue = "false") })
    public Result<List<CapacityPlanRegionSplitResult>> splitRegion(@RequestParam("regionId") Long regionId,
                                                                   @RequestParam("exe") boolean exe) {
        return capacityPlanRegionService.splitRegion(regionId, exe);
    }
}
