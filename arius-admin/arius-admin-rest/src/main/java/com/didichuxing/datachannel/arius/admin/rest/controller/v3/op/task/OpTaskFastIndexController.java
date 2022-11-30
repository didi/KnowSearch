package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.task;

import com.didichuxing.datachannel.arius.admin.biz.task.FastIndexManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexLogsConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastDumpTaskLogVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexDetailVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

@RestController
@RequestMapping(V3 + "/op-task/fast-index")
@Api(tags = "数据迁移任务接口(REST)")
public class OpTaskFastIndexController {

    @Autowired
    private FastIndexManager fastIndexManager;

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "数据迁移提交迁移任务接口")
    public Result<WorkTaskVO> submitTask(HttpServletRequest request, @RequestBody FastIndexDTO fastIndexDTO) {
        return fastIndexManager.submitTask(fastIndexDTO, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @DeleteMapping("/{taskId}")
    @ResponseBody
    @ApiOperation(value = "根据任务id取消任务")
    public Result<Void> cancelTask(HttpServletRequest request, @PathVariable("taskId") Integer taskId) {
        return fastIndexManager.cancelTask(taskId, HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/{taskId}")
    @ResponseBody
    @ApiOperation(value = "根据任务id重试任务")
    public Result<Void> restartTask(HttpServletRequest request, @PathVariable("taskId") Integer taskId) {
        return fastIndexManager.restartTask(taskId, HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/{taskId}/rate-limit")
    @ResponseBody
    @ApiOperation(value = "根据任务id修改限流值")
    public Result<Void> modifyTaskRateLimit(@PathVariable("taskId") Integer taskId,
                                            @RequestBody FastIndexRateLimitDTO fastIndexRateLimitDTO) {
        return fastIndexManager.modifyTaskRateLimit(taskId, fastIndexRateLimitDTO);
    }

    @GetMapping("/{taskId}/detail")
    @ResponseBody
    @ApiOperation(value = "获取任务详情")
    public Result<FastIndexDetailVO> getTaskDetail(@PathVariable("taskId") Integer taskId) {
        return fastIndexManager.getTaskDetail(taskId);
    }

    @PostMapping("/logs")
    @ResponseBody
    @ApiOperation(value = "查询任务日志")
    public PaginationResult<FastDumpTaskLogVO> getTaskLogs(HttpServletRequest request,
                                                           @RequestBody FastIndexLogsConditionDTO logsConditionDTO) throws NotFindSubclassException {
        return fastIndexManager.pageGetTaskLogs(HttpRequestUtil.getProjectId(request), logsConditionDTO);
    }

    @PutMapping("/{taskId}/refresh")
    @ResponseBody
    @ApiOperation(value = "刷新任务状态")
    public Result<Void> refreshTaskState(@PathVariable("taskId") Integer taskId) {
        return fastIndexManager.refreshTask(taskId);
    }

    @PutMapping("/{taskId}/transfer")
    @ResponseBody
    @ApiOperation(value = "转让模版")
    public Result<Void> transferTemplate(@PathVariable("taskId") Integer taskId) {
        return fastIndexManager.transferTemplate(taskId);
    }

    @PutMapping("/{taskId}/rollback")
    @ResponseBody
    @ApiOperation(value = "回切转让")
    public Result<Void> rollbackTemplate(@PathVariable("taskId") Integer taskId) {
        return fastIndexManager.rollbackTemplate(taskId);
    }

    @GetMapping("/{taskId}/brief")
    @ResponseBody
    @ApiOperation(value = "获取模板和索引的下拉框值")
    public Result<List<FastIndexBriefVO>> getTemplateAndIndexBrief(@PathVariable("taskId") Integer taskId) {
        return fastIndexManager.getFastIndexBrief(taskId);
    }

    @GetMapping("/{cluster}/{indexName}/setting")
    @ResponseBody
    @ApiOperation(value = "查询setting")
    public Result<IndexSettingVO> getSetting(@PathVariable String cluster, @PathVariable String indexName,
                                             HttpServletRequest request) {
        return fastIndexManager.getSetting(cluster, indexName, HttpRequestUtil.getProjectId(request));
    }
    @GetMapping("/template/{logicClusterId}/{logicId}/setting")
    @ResponseBody
    @ApiOperation(value = "获取模板Setting接口")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<IndexTemplatePhySetting> getTemplateSettings(@PathVariable("logicId") Integer logicId, @PathVariable("logicClusterId") Long logicClusterId) {
        return fastIndexManager.getTemplateSettings(logicId, logicClusterId);
    }
}