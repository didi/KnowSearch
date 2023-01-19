package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.task;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.op.manager.ESClusterExpandWithPluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.op.manager.ESClusterShrinkWithPluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.OpTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.TaskTypeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author fengqiongfeng
 * @date 2020/08/24
 */
@Api(tags = "任务相关接口(REST)")
@RestController
@RequestMapping({ V3 + "/op-task" })
public class OpTaskController {
    private static final ILog LOGGER = LogFactory.getLog(OpTaskController.class);

    @Autowired
    private OpTaskManager     opTaskManager;

    @ApiOperation(value = "任务类型", notes = "")
    @GetMapping(value = "/type-enums")
    @ResponseBody
    public Result<List<TaskTypeVO>> getOrderTypes() {
        List<TaskTypeVO> voList = new ArrayList<>();
        for (OpTaskTypeEnum elem : OpTaskTypeEnum.values()) {
            voList.add(new TaskTypeVO(elem.getType(), elem.getMessage()));
        }
        return Result.buildSucc(voList);
    }

    @ApiOperation(value = "任务详情", notes = "")
    @GetMapping(value = "/{taskId}")
    @ResponseBody
    public Result<WorkTaskVO> getOrderDetail(@PathVariable(value = "taskId") Integer taskId) {
        Result<OpTask> result = opTaskManager.getById(taskId);
        if (result.failed()) {
            return Result.buildFrom(result);
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(result.getData(), WorkTaskVO.class));
    }

    @ApiOperation(value = "任务列表", notes = "")
    @GetMapping(value = "tasks")
    @ResponseBody
    @Deprecated
    public Result<List<WorkTaskVO>> getTaskList() {
        return Result.buildSucc(ConvertUtil.list2List(opTaskManager.list().getData(), WorkTaskVO.class));
    }

    @ApiOperation(value = "任务中心分页列表", notes = "")
    @PostMapping(value = "page")
    @ResponseBody
    public PaginationResult<OpTaskVO> pageGetTasks(@RequestBody OpTaskQueryDTO queryDTO, HttpServletRequest request) throws NotFindSubclassException {
        return opTaskManager.pageGetTasks(HttpRequestUtil.getProjectId(request), queryDTO);
    }

    @PostMapping(path = "/{type}")
    @ResponseBody
    @ApiOperation(value = "提交任务接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "String", name = "type", value = "任务类型", required = true) })
    public Result<WorkTaskVO> addTask(HttpServletRequest request, @PathVariable(value = "type") String code,
                                      @RequestBody OpTaskDTO opTaskDTO) throws NotFindSubclassException {
        String dataCenter = opTaskDTO.getDataCenter();
        String user = HttpRequestUtil.getOperator(request);

        opTaskDTO.setTaskType(OpTaskTypeEnum.valueOfPath(code).getType());
        opTaskDTO.setCreator(user);
        LOGGER.info(
            "class=OpTaskController||method=OpTaskController.addTask||workTaskDTO={}||envInfo={}||dataCenter={}",
            JSON.toJSONString(opTaskDTO), EnvUtil.getStr(), dataCenter);

        Result<OpTask> result = opTaskManager.addTask(opTaskDTO, AuthConstant.SUPER_PROJECT_ID);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(result.getData(), WorkTaskVO.class));
    }
    
    @PostMapping(path = "/es-cluster-shrink")
    @ResponseBody
    @ApiOperation(value = "提交 es 缩容任务接口", notes = "")
    public Result<List<WorkTaskVO>> addTaskESShrink(HttpServletRequest request,
        @RequestBody ESClusterShrinkWithPluginDTO data) throws NotFindSubclassException {
        return opTaskManager.addTaskESShrink(data, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
        
    }
    
    @PostMapping(path = "/es-cluster-expand")
    @ResponseBody
    @ApiOperation(value = "提交 es 扩容任务接口", notes = "")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "path", dataType = "String", name = "type", value = "任务类型", required = true)})
    public Result<List<WorkTaskVO>> addTaskESExpand(HttpServletRequest request,
        @RequestBody ESClusterExpandWithPluginDTO data) throws NotFindSubclassException {
        return opTaskManager.addTaskESExpand(data, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }
    
    
    @ApiOperation(value = "ecm 任务类型", notes = "")
    @GetMapping(value = "/ecm-type-enums")
    @ResponseBody
    public Result<List<TaskTypeVO>> getECMOrderTypes() {
        List<TaskTypeVO> voList = new ArrayList<>();
        for (OpTaskTypeEnum elem : OpTaskTypeEnum.opManagerTask()) {
            voList.add(new TaskTypeVO(elem.getType(), elem.getMessage()));
        }
        return Result.buildSucc(voList);
    }

}