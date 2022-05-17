package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.task;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.worktask.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.TaskTypeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.constant.task.AriusOpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
    private OpTaskManager opTaskManager;

    @ApiOperation(value = "任务类型", notes = "")
    @GetMapping(value = "/type-enums")
    @ResponseBody
    public Result<List<TaskTypeVO>> getOrderTypes() {
        List<TaskTypeVO> voList = new ArrayList<>();
        for (AriusOpTaskTypeEnum elem : AriusOpTaskTypeEnum.values()) {
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
                                               @RequestBody OpTaskDTO workTaskDTO) {
        String dataCenter = workTaskDTO.getDataCenter();
        String user = HttpRequestUtils.getOperator(request);

        workTaskDTO.setTaskType(type);
        workTaskDTO.setCreator(user);
        workTaskDTO.setCreateTime(new Date());
        workTaskDTO.setUpdateTime(new Date());

        LOGGER.info("class=OpTaskController||method=OpTaskController.process||workTaskDTO={}||envInfo={}||dataCenter={}",
            JSON.toJSONString(workTaskDTO), EnvUtil.getStr(), dataCenter);

        Result<OpTask> result = opTaskManager.addTask(workTaskDTO);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        return Result.buildSucc();
    }

    @ApiOperation(value = "任务详情", notes = "")
    @GetMapping(value = "/{taskId}")
    @ResponseBody
    public Result<WorkTaskVO> getOrderDetail(@PathVariable(value = "taskId") Integer taskId) {
        Result<OpTask> result = opTaskManager.getById(taskId);
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
                opTaskManager.list().getData(),WorkTaskVO.class)
        );
    }

}