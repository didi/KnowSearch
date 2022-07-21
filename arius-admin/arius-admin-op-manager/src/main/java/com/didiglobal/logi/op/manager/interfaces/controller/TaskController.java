package com.didiglobal.logi.op.manager.interfaces.controller;

import com.didiglobal.logi.op.manager.application.TaskService;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import com.didiglobal.logi.op.manager.interfaces.vo.GeneralGroupConfigHostVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author didi
 * @date 2022-07-13 3:20 下午
 */
@RestController
@Api(value = "任务中心api")
@RequestMapping(Constants.API_PREFIX_V3 + "/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    @PostMapping("/execute/{taskId}")
    @ApiOperation(value = "")
    public Result<Void> execute(@PathVariable Integer taskId) {
        return taskService.execute(taskId);
    }

    @GetMapping("/task-config")
    @ApiOperation(value = "")
    public Result<GeneralGroupConfigHostVO> execute(@RequestParam(value = "taskId", required = true) Integer taskId,
                                                    @RequestParam(value = "groupName", required = true) String groupName, HttpServletRequest request) {
        Result res = taskService.getGroupConfig(taskId, groupName);
        if (res.isSuccess()) {
            res.setData(ComponentAssembler.toGeneralGroupConfigVO((GeneralGroupConfig) res.getData(),
                    request.getRemoteAddr()));
        }
        return res;
    }
}
