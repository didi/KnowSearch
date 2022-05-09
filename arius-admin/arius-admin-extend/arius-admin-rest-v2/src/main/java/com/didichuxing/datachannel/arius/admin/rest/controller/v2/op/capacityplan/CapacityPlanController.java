package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.capacityplan;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.vo.CapacityPlanRegionTaskVO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;

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
    private CapacityPlanRegionTaskService capacityPlanRegionTaskService;

    @GetMapping("/area/region/task/list")
    @ResponseBody
    @ApiOperation(value = "查询规划集群region的任务接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })

    public Result<List<CapacityPlanRegionTaskVO>> listClusterRegionTask(@RequestParam("regionId") Long regionId) {
        return Result.buildSucc(ConvertUtil.list2List(capacityPlanRegionTaskService.getTaskByRegionId(regionId),
            CapacityPlanRegionTaskVO.class));
    }
}
