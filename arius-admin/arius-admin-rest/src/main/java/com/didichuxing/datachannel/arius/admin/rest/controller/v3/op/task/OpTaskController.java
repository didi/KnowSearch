package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.task;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.task.TaskTypeVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
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
@Api(tags = "OP-任务相关接口(REST)")
@RestController
@RequestMapping(V3_OP + "/worktask")
public class OpTaskController {
    private static final ILog LOGGER = LogFactory.getLog(OpTaskController.class);

    @Autowired
    private WorkTaskManager workTaskManager;

    @ApiOperation(value = "任务类型", notes = "")
    @RequestMapping(value = "/type-enums", method = RequestMethod.GET)
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
    @RequestMapping(path = "/{type}/submit", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "提交任务接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "String", name = "type", value = "任务类型", required = true) })
    public Result<WorkTaskVO> submit(HttpServletRequest request,
                                               @PathVariable(value = "type") Integer type,
                                               @RequestBody WorkTaskDTO workTaskDTO) throws AdminOperateException {
        String dataCenter = workTaskDTO.getDataCenter();
        String user = HttpRequestUtils.getOperator(request);

        workTaskDTO.setTaskType(type);
        workTaskDTO.setCreator(user);
        workTaskDTO.setCreateTime(new Date());
        workTaskDTO.setUpdateTime(new Date());

        LOGGER.info("method=OpTaskController.process||workTaskDTO={}||envInfo={}||dataCenter={}",
            JSON.toJSONString(workTaskDTO), EnvUtil.getStr(), dataCenter);

        Result result = workTaskManager.addTask(workTaskDTO);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        return Result.buildSucc();
    }

    @ApiOperation(value = "任务详情", notes = "")
    @RequestMapping(value = "/{taskId}", method = RequestMethod.GET)
    @ResponseBody
    public Result<WorkTaskVO> getOrderDetail(@PathVariable(value = "taskId") Integer taskId) {
        Result result = workTaskManager.getById(taskId);
        if (result.failed()) {
            return result;
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(result.getData(),WorkTaskVO.class));
    }


    @ApiOperation(value = "任务列表", notes = "")
    @RequestMapping(value = "tasks", method = RequestMethod.GET)
    @ResponseBody
    public Result<List<WorkTaskVO>> getTaskList() {
        return Result.buildSucc(ConvertUtil.list2List(
                workTaskManager.list().getData(),WorkTaskVO.class)
        );
    }

}