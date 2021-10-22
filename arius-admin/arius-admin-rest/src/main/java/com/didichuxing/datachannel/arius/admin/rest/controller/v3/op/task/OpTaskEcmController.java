package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.task;

import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskDetailManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmTaskBasic;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm.EcmTaskBasicVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm.EcmTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm.EcmTaskVO;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.EcmActionEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author didi
 * @since 2020-08-24
 */
@RestController
@RequestMapping(V3_OP + "/ecm/work-order/task")
@Api(tags = "ECM-CLUSTER-WORK-ORDER-TASK接口(REST)")
public class OpTaskEcmController {
    private final EcmTaskManager ecmTaskManager;
    private final EcmTaskDetailManager ecmTaskDetailManager;

    @Autowired
    public OpTaskEcmController(EcmTaskManager ecmTaskManager,
                               EcmTaskDetailManager ecmTaskDetailManager) {
        this.ecmTaskManager = ecmTaskManager;
        this.ecmTaskDetailManager = ecmTaskDetailManager;
    }

    @RequestMapping(path = "{taskId}/create", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "启动集群任务工单", notes = "")
    public Result startWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId) {
        return ecmTaskManager.createClusterEcmTask(taskId, HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "{taskId}/scale", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "扩缩容工单集群任务", notes = "")
    public Result scaleWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId) {
        return ecmTaskManager.actionClusterEcmTask(taskId, HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "{taskId}/upgrade", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "升级工单集群任务", notes = "")
    public Result upgradeWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId) {
        return ecmTaskManager.actionClusterEcmTask(taskId, HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "{taskId}/restart", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "重启工单集群任务", notes = "")
    public Result restartWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId) {
        return ecmTaskManager.actionClusterEcmTask(taskId, HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "{taskId}/pause", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "暂停工单集群任务", notes = "")
    public Result pauseWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId) {
        return ecmTaskManager.actionClusterEcmTask(taskId, EcmActionEnum.PAUSE,
            null, HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "{taskId}/continue", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "继续工单集群任务", notes = "")
    public Result continueWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId) {
        return ecmTaskManager.actionClusterEcmTask(taskId, EcmActionEnum.CONTINUE,
            null, HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "{taskId}/skip-failed", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "跳过工单失败集群节点任务", notes = "")
    public Result skipFailedWorkOrderTask(HttpServletRequest request,
                                          @PathVariable Long taskId,
                                          @RequestParam("hostname") String hostname) {
        return ecmTaskManager.actionClusterEcmTask(taskId, EcmActionEnum.SKIP_FAILED,
            hostname, HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "{taskId}/redo-failed", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "重做集群失败节点任务", notes = "")
    public Result redoFailedWorkOrderTask(HttpServletRequest request,
                                          @PathVariable Long taskId,
                                          @RequestParam("hostname") String hostname) {
        return ecmTaskManager.actionClusterEcmTask(taskId, EcmActionEnum.REDO_FAILED,
            hostname, HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "{taskId}/cancel", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "取消工单集群节点任务", notes = "")
    public Result cancelWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId) {
        return ecmTaskManager.cancelClusterEcmTask(taskId,HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "任务列表接口", notes = "")
    public Result<List<EcmTaskVO>> listWorkOrderTask() {
        List<EcmTask> ecmTaskPOList = ecmTaskManager.listEcmTask();
        if (ValidateUtils.isNull( ecmTaskPOList )) {
            return Result.buildNotExist(ResultType.NOT_EXIST.getMessage());
        }

        return Result.buildSucc(ConvertUtil.list2List( ecmTaskPOList, EcmTaskVO.class));
    }

    @RequestMapping(path = "/{taskId}/basic-info", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "任务基本信息", notes = "")
    public Result<EcmTaskBasicVO> getWorkOrderTaskBasic(@PathVariable Long taskId) {
        Result<EcmTaskBasic> workOrderTaskBasicResult = ecmTaskManager.getEcmTaskBasicByTaskId(taskId);
        if (workOrderTaskBasicResult.failed()) {
            return Result.buildFail(workOrderTaskBasicResult.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(workOrderTaskBasicResult.getData(), EcmTaskBasicVO.class));
    }

    @RequestMapping(path = "{taskId}/task-details", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "任务详细信息", notes = "")
    public Result<EcmTaskDetailVO> clusterTaskDetails(@PathVariable Long taskId) {
        return Result.buildSucc(ConvertUtil.obj2Obj( ecmTaskDetailManager.getEcmTaskDetailInfo(taskId).getData()
                , EcmTaskDetailVO.class));
    }

    @RequestMapping(path = "{detailId}/task-detail/log", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "工单任务详情操作日志接口", notes = "")
    public Result clusterTaskDetailLog(HttpServletRequest request, @PathVariable Long detailId) {
        return ecmTaskDetailManager.getTaskDetailLog(detailId, HttpRequestUtils.getOperator(request));
    }
}
