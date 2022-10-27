package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.task;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexDetailVO;

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

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "数据迁移提交迁移任务接口", notes = "")
    public Result<WorkTaskVO> fastIdexSubmit(HttpServletRequest request, @RequestBody FastIndexDTO fastIndexDTO) {
        return null;
    }

    @DeleteMapping("/{taskId}")
    @ResponseBody
    @ApiOperation(value = "根据任务id取消任务")
    public Result<Void> cancelTask(HttpServletRequest request, @PathVariable("taskId") Integer taskId) {
        return null;
    }

    @PutMapping("/{taskId}")
    @ResponseBody
    @ApiOperation(value = "根据任务id重试任务")
    public Result<Void> restartTask(HttpServletRequest request, @PathVariable("taskId") Integer taskId) {
        return null;
    }

    @PutMapping("/rate-limit")
    @ResponseBody
    @ApiOperation(value = "根据任务id修改限流值")
    public Result<Void> modifyTaskRateLimit(HttpServletRequest request,
                                            @RequestBody FastIndexRateLimitDTO fastIndexRateLimitDTO) {
        return null;
    }

    @GetMapping("/{taskId}/detail")
    @ResponseBody
    @ApiOperation(value = "获取任务详情")
    public Result<FastIndexDetailVO> getTaskDetail(@PathVariable("taskId") Integer taskId) {
        return null;
    }

    @PostMapping("/{taskId}/refresh")
    @ResponseBody
    @ApiOperation(value = "刷新任务状态")
    public Result<Void> refreshTaskState(HttpServletRequest request, @PathVariable("taskId") Integer taskId) {
        return null;
    }
}