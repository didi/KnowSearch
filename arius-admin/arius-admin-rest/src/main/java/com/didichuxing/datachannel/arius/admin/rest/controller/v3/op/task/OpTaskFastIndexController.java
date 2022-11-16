package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.task;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.task.FastIndexManagerImpl;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexLogsConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexDetailVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.logi.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3 + "/op-task/fast-index")
@Api(tags = "数据迁移任务接口(REST)")
public class OpTaskFastIndexController {
    //查询物理集群列表（可筛选插件）
    //查询索引列表
    //查询索引mapping和setting
    //查询模版mapping和setting
    //查询物理集群列表	GET /v3/cluster/phy/names

    @Autowired
    private FastIndexManagerImpl fastIndexManager;

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "数据迁移提交迁移任务接口", notes = "")
    public Result<WorkTaskVO> submitTask(HttpServletRequest request,
                                         @RequestBody FastIndexDTO fastIndexDTO) throws NotFindSubclassException {
        return fastIndexManager.submitTask(fastIndexDTO, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @DeleteMapping("/{taskId}")
    @ResponseBody
    @ApiOperation(value = "根据任务id取消任务")
    public Result<Void> cancelTask(HttpServletRequest request,
                                   @PathVariable("taskId") Integer taskId) throws NotFindSubclassException {
        return fastIndexManager.cancelTask(taskId, HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/{taskId}")
    @ResponseBody
    @ApiOperation(value = "根据任务id重试任务")
    public Result<Void> restartTask(HttpServletRequest request,
                                    @PathVariable("taskId") Integer taskId) throws NotFindSubclassException {
        return fastIndexManager.restartTask(taskId, HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/{taskId}/rate-limit")
    @ResponseBody
    @ApiOperation(value = "根据任务id修改限流值")
    public Result<Void> modifyTaskRateLimit(HttpServletRequest request, @PathVariable("taskId") Integer taskId,
                                            @RequestBody FastIndexRateLimitDTO fastIndexRateLimitDTO) {
        return fastIndexManager.modifyTaskRateLimit(taskId, fastIndexRateLimitDTO);
    }

    @GetMapping("/{taskId}/detail")
    @ResponseBody
    @ApiOperation(value = "获取任务详情")
    public Result<FastIndexDetailVO> getTaskDetail(@PathVariable("taskId") Integer taskId) {
        return fastIndexManager.getTaskDetail(taskId);
    }

    @PostMapping("/{taskId}/logs")
    @ResponseBody
    @ApiOperation(value = "查询任务日志")
    public Result<String> getTaskLogs(@PathVariable("taskId") Integer taskId,
                                      @RequestBody FastIndexLogsConditionDTO logsConditionDTO) {
        return null;
    }

    @PostMapping("/{taskId}/refresh")
    @ResponseBody
    @ApiOperation(value = "刷新任务状态")
    public Result<Void> refreshTaskState(HttpServletRequest request,
                                         @PathVariable("taskId") Integer taskId) throws NotFindSubclassException {
        return fastIndexManager.refreshTask(taskId);
    }

    @PostMapping("/{taskId}/transfer")
    @ResponseBody
    @ApiOperation(value = "转让模版")
    public Result<Void> transferTemplate(HttpServletRequest request,
                                         @PathVariable("taskId") Integer taskId) throws NotFindSubclassException {
        return fastIndexManager.transferTemplate(taskId);
    }

    @PostMapping("/{taskId}/rollback")
    @ResponseBody
    @ApiOperation(value = "回切转让")
    public Result<Void> rollbackTemplate(HttpServletRequest request,
                                         @PathVariable("taskId") Integer taskId) throws NotFindSubclassException {
        return fastIndexManager.rollbackTemplate(taskId);
    }
}