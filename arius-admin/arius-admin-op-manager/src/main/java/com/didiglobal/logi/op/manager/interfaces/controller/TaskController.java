package com.didiglobal.logi.op.manager.interfaces.controller;

import com.didiglobal.logi.op.manager.application.TaskService;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskLogEnum;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import com.didiglobal.logi.op.manager.interfaces.vo.GeneralGroupConfigHostVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/execute/{taskId}")
    @ApiOperation(value = "执行任务")
    public Result<Void> execute(@PathVariable Integer taskId) {
        return taskService.execute(taskId);
    }

    @PostMapping("/{action}/{taskId}")
    @ApiOperation(value = "对主任务进行相应的操作")
    public Result<Void> operateTask(@PathVariable String action, @PathVariable Integer taskId) {
        return taskService.operateTask(taskId, action);
    }

    @PostMapping("/retry/{taskId}")
    @ApiOperation(value = "重试任务")
    public Result<Void> retryTask(@PathVariable Integer taskId) {
        return taskService.retryTask(taskId);
    }

    @PostMapping("/{action}/{taskId}/{host}")
    @ApiOperation(value = "对任务子节点进行相应的操作")
    public Result<Void> operateHost(@PathVariable String action, @PathVariable Integer taskId, @PathVariable String host,
                                    @RequestParam(value = "groupName") String groupName) {
        return taskService.operateHost(taskId, action, host, groupName);
    }

    @GetMapping("/task-config")
    @ApiOperation(value = "zeus获取任务配置的接口")
    public Result<GeneralGroupConfigHostVO> getConfig(@RequestParam(value = "taskId", required = true) Integer taskId,
                                                      @RequestParam(value = "groupName", required = true) String groupName,
                                                      @RequestParam(value = "host", required = true) String host) {
        Result res = taskService.getGroupConfig(taskId, groupName);
        if (res.isSuccess()) {
            GeneralGroupConfig config = (GeneralGroupConfig) res.getData();
            return ComponentAssembler.toGeneralGroupConfigVO(config, host);
        }
        return res;
    }


    @GetMapping("/task-stdouts")
    @ApiOperation(value = "zeus查看任务执行完成后的标准输出")
    public Result<String> getTaskStdOuts(@RequestParam(value = "taskId", required = true) Integer taskId,
                                         @RequestParam(value = "hostname", required = true) String hostname) {
        return taskService.getTaskLog(taskId, hostname, TaskLogEnum.STDOUT);
    }

    @GetMapping("/task-stderrs")
    @ApiOperation(value = "zeus查看任务执行完成后的错误输出")
    public Result<String> getTaskStdErrs(@RequestParam(value = "taskId", required = true) Integer taskId,
                                         @RequestParam(value = "hostname", required = true) String hostname) {
        return taskService.getTaskLog(taskId, hostname, TaskLogEnum.STDERR);
    }
}
