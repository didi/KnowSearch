package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.task;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.TaskTypeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author fengqiongfeng
 * @date 2020/08/24
 */
@Api(tags = "任务相关接口(REST)")
@RestController
@RequestMapping(V3_OP + "/worktask")
public class OpTaskController {
    private static final ILog LOGGER = LogFactory.getLog(OpTaskController.class);

    @Autowired
    private WorkTaskManager workTaskManager;

    @ApiOperation(value = "任务类型", notes = "")
    @GetMapping(value = "/type-enums")
    @ResponseBody
    public Result<List<TaskTypeVO>> getOrderTypes() {
        List<TaskTypeVO> voList = new ArrayList<>();
        for (WorkTaskTypeEnum elem : WorkTaskTypeEnum.values()) {
            voList.add(new TaskTypeVO(elem.getType(), elem.getMessage()));
        }
        return Result.buildSucc(voList);
    }

    /**
     * 提交一个任务
     */
    @PutMapping(path = "/{type}/submit")
    @ResponseBody
    @ApiOperation(value = "提交任务接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "String", name = "type", value = "任务类型", required = true) })
    public Result<WorkTaskVO> submit(HttpServletRequest request,
                                               @PathVariable(value = "type") Integer type,
                                               @RequestBody WorkTaskDTO workTaskDTO) {
        String dataCenter = workTaskDTO.getDataCenter();
        String user = HttpRequestUtils.getOperator(request);

        workTaskDTO.setTaskType(type);
        workTaskDTO.setCreator(user);
        workTaskDTO.setCreateTime(new Date());
        workTaskDTO.setUpdateTime(new Date());

        LOGGER.info("class=OpTaskController||method=OpTaskController.process||workTaskDTO={}||envInfo={}||dataCenter={}",
            JSON.toJSONString(workTaskDTO), EnvUtil.getStr(), dataCenter);

        Result<WorkTask> result = workTaskManager.addTask(workTaskDTO);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        return Result.buildSucc();
    }

    @ApiOperation(value = "任务详情", notes = "")
    @GetMapping(value = "/{taskId}")
    @ResponseBody
    public Result<WorkTaskVO> getOrderDetail(@PathVariable(value = "taskId") Integer taskId) {
        Result<WorkTask> result = workTaskManager.getById(taskId);
        if (result.failed()) {
            return Result.buildFrom(result);
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(result.getData(),WorkTaskVO.class));
    }

    @ApiOperation(value = "任务列表", notes = "")
    @GetMapping(value = "tasks")
    @ResponseBody
    public Result<List<WorkTaskVO>> getTaskList() {
        return Result.buildSucc(ConvertUtil.list2List(
                workTaskManager.list().getData(),WorkTaskVO.class)
        );
    }

}