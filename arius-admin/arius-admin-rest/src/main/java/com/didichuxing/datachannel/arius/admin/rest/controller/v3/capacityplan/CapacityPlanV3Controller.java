package com.didichuxing.datachannel.arius.admin.rest.controller.v3.capacityplan;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionSplitResult;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.vo.CapacityPlanRegionTaskItemVO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.vo.CapacityPlanRegionTaskVO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionTaskService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/14
 * @Comment:
 */
@RestController
@RequestMapping(V3_OP + "/capacity/plan")
@Api(tags = "容量规划接口(REST)")
public class CapacityPlanV3Controller {

    @Autowired
    private CapacityPlanRegionService     capacityPlanRegionService;

    @Autowired
    private CapacityPlanRegionTaskService capacityPlanRegionTaskService;

    @Autowired
    private TemplateSrvManager            templateSrvManager;

    @PutMapping("/region/plan")
    @ResponseBody
    @ApiOperation(value = "规划region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<Void> planClusterRegion(@RequestParam("regionId") Long regionId) throws ESOperateException {
        return capacityPlanRegionService.planRegion(regionId);
    }

    @PutMapping("/region/moveShard")
    @ResponseBody
    @ApiOperation(value = "纠正集群region-模板资源接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<Void> moveShardClusterRegion(@RequestParam("regionId") Long regionId) {
        // 搬迁一个region内所有的索引到当前region的rack
        return capacityPlanRegionService.moveShard(regionId, true);
    }

    @PutMapping("/region/moveTemplate")
    @ResponseBody
    @ApiOperation(value = "纠正集群region-模板资源接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "boolean", name = "moveIndex", value = "moveIndex", required = true) })
    public Result<Void> moveTemplateClusterRegion(@RequestParam("regionId") Long regionId,
                                            @RequestParam("moveIndex") Boolean moveIndex) {
        return capacityPlanRegionService.moveShard(regionId, moveIndex);
    }

    @GetMapping("/region/task/list")
    @ResponseBody
    @ApiOperation(value = "查询规划集群region的任务接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<List<CapacityPlanRegionTaskVO>> listClusterRegionTask(@RequestParam("regionId") Long regionId) {

        // 容量规划任务列表
        return Result.buildSucc(ConvertUtil.list2List(capacityPlanRegionTaskService.getTaskByRegionId(regionId),
            CapacityPlanRegionTaskVO.class));
    }

    @PutMapping("/region/task/execute")
    @ResponseBody
    @ApiOperation(value = "执行初始化规划集群region任务接口", notes = "执行容量规划任务")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "taskId", value = "任务ID", required = true) })
    public Result<Void> exeClusterRegionTask(@RequestParam("taskId") Long taskId) {
        return capacityPlanRegionTaskService.exeInitTask(taskId);
    }

    @PutMapping("/region/task/check")
    @ResponseBody
    @ApiOperation(value = "检查规划任务是否完成接口", notes = "")
    public Result<Void> checkClusterRegionTask() {
        return capacityPlanRegionTaskService.checkTasks();
    }

    @GetMapping("/region/task/listItem")
    @ResponseBody
    @ApiOperation(value = "查询规划集群region的任务明细接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "taskId", value = "任务ID", required = true) })
    public Result<List<CapacityPlanRegionTaskItemVO>> getClusterRegionTasks(@RequestParam("taskId") Long taskId) {
        return Result.buildSucc(ConvertUtil.list2List(capacityPlanRegionTaskService.getTaskItemByTaskId(taskId),
            CapacityPlanRegionTaskItemVO.class));
    }

    @PutMapping("/region/split")
    @ResponseBody
    @ApiOperation(value = "拆分region", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "BOOLEAN", name = "exe", value = "是否生效", defaultValue = "false") })
    public Result<List<CapacityPlanRegionSplitResult>> splitRegion(@RequestParam("regionId") Long regionId,
                                                                   @RequestParam("exe") boolean exe) {

        return capacityPlanRegionService.splitRegion(regionId, exe);
    }

    @PutMapping("/region/check")
    @ResponseBody
    @ApiOperation(value = "检查集群region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<Void> checkClusterRegion(@RequestParam("regionId") Long regionId) {
        return capacityPlanRegionService.checkRegion(regionId);
    }

    @PutMapping("/phyCluster/open")
    @ResponseBody
    @ApiOperation(value = "开启物理集群容量规划功能", notes = "开启物理集群所属的容量规划area的容量规划功能")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "phyClusterName", value = "物理集群名", required = true) })
    public Result<Boolean> openPhyClusterCapacityPlanFlags(HttpServletRequest request,
                                                  @RequestParam("phyClusterName") String phyClusterName) {

        // 容量规划开关统一到索引服务
        return templateSrvManager.checkTemplateSrv(phyClusterName,
            TemplateServiceEnum.TEMPLATE_CAPA_PLAN.getCode().toString(), HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/phyCluster/close")
    @ResponseBody
    @ApiOperation(value = "关闭物理集群容量规划功能", notes = "关闭物理集群所属的容量规划area的容量规划功能")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "phyClusterName", value = "物理集群名", required = true) })
    public Result<Boolean> closePhyClusterCapacityPlanFlags(HttpServletRequest request,
                                                   @RequestParam("phyClusterName") String phyClusterName) {

        return templateSrvManager.delTemplateSrv(phyClusterName,
            TemplateServiceEnum.TEMPLATE_CAPA_PLAN.getCode().toString(), HttpRequestUtils.getOperator(request) );
    }

}