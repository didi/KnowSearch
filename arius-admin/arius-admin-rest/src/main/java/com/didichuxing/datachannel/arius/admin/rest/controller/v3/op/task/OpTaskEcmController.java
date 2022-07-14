package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.task;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.task.ecm.EcmTaskDetailManager;
import com.didichuxing.datachannel.arius.admin.biz.task.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmTaskBasic;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.EcmTaskBasicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.EcmTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.EcmTaskVO;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.exception.EcmRemoteException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.EcmActionEnum;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author didi
 * @since 2020-08-24
 */
@RestController
@RequestMapping(V3_OP + "/ecm/work-order/task")
@Api(tags = "ECM任务相关接口(REST)")
public class OpTaskEcmController {
    private final EcmTaskManager       ecmTaskManager;
    private final EcmTaskDetailManager ecmTaskDetailManager;

    @Autowired
    public OpTaskEcmController(EcmTaskManager ecmTaskManager, EcmTaskDetailManager ecmTaskDetailManager) {
        this.ecmTaskManager = ecmTaskManager;
        this.ecmTaskDetailManager = ecmTaskDetailManager;
    }

    @PostMapping(path = "{taskId}/create")
    @ResponseBody
    @ApiOperation(value = "启动集群任务", notes = "")
    public Result<EcmOperateAppBase> savaAndActionEcmTask(HttpServletRequest request,
                                                          @PathVariable Long taskId) throws EcmRemoteException {
        return ecmTaskManager.savaAndActionEcmTask(taskId, HttpRequestUtil.getOperator(request));
    }

    @PostMapping(path = "{taskId}/continue")
    @ResponseBody
    @ApiOperation(value = "继续集群任务", notes = "")
    public Result<EcmOperateAppBase> continueWorkOrderTask(HttpServletRequest request,
                                                           @PathVariable Long taskId) throws EcmRemoteException {
        return ecmTaskManager.actionClusterEcmTask(taskId, EcmActionEnum.START, null,
            HttpRequestUtil.getOperator(request));
    }

    @PostMapping(path = "{taskId}/scale")
    @ResponseBody
    @ApiOperation(value = "扩缩容集群任务", notes = "")
    public Result<EcmOperateAppBase> scaleWorkOrderTask(HttpServletRequest request,
                                                        @PathVariable Long taskId) throws EcmRemoteException {
        return ecmTaskManager.actionClusterEcmTask(taskId, HttpRequestUtil.getOperator(request));
    }

    @PostMapping(path = "{taskId}/upgrade")
    @ResponseBody
    @ApiOperation(value = "升级集群任务", notes = "")
    public Result<EcmOperateAppBase> upgradeWorkOrderTask(HttpServletRequest request,
                                                          @PathVariable Long taskId) throws EcmRemoteException {
        return ecmTaskManager.actionClusterEcmTask(taskId, HttpRequestUtil.getOperator(request));
    }

    @PostMapping(path = "{taskId}/restart")
    @ResponseBody
    @ApiOperation(value = "重启集群任务", notes = "")
    public Result<EcmOperateAppBase> restartWorkOrderTask(HttpServletRequest request,
                                                          @PathVariable Long taskId) throws EcmRemoteException {
        return ecmTaskManager.actionClusterEcmTask(taskId, HttpRequestUtil.getOperator(request));
    }

    @PostMapping(path = "{taskId}/pause")
    @ResponseBody
    @ApiOperation(value = "暂停集群任务", notes = "")
    public Result<Void> pauseWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId) {
        return ecmTaskManager.pauseClusterEcmTask(taskId, HttpRequestUtil.getOperator(request));
    }

    @PostMapping(path = "{taskId}/retry")
    @ResponseBody
    @ApiOperation(value = "重试集群任务", notes = "")
    public Result<Void> retryWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId) {
        return ecmTaskManager.retryClusterEcmTask(taskId, HttpRequestUtil.getOperator(request));
    }

    @PostMapping(path = "{taskId}/skip-failed")
    @ResponseBody
    @ApiOperation(value = "跳过失败集群节点任务", notes = "")
    public Result<Void> skipFailedWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId,
                                                @RequestParam("hostname") String hostname) {
        return ecmTaskManager.actionClusterHostEcmTask(taskId, EcmActionEnum.SKIP_FAILED, hostname,
            HttpRequestUtil.getOperator(request));
    }

    @PostMapping(path = "{taskId}/redo-failed")
    @ResponseBody
    @ApiOperation(value = "重做集群失败节点任务", notes = "")
    public Result<Void> redoFailedWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId,
                                                @RequestParam("hostname") String hostname) {
        return ecmTaskManager.actionClusterHostEcmTask(taskId, EcmActionEnum.REDO_FAILED, hostname,
            HttpRequestUtil.getOperator(request));
    }

    @PostMapping(path = "{taskId}/cancel")
    @ResponseBody
    @ApiOperation(value = "取消整个集群任务", notes = "")
    public Result<Void> cancelWorkOrderTask(HttpServletRequest request, @PathVariable Long taskId) {
        return ecmTaskManager.cancelClusterEcmTask(taskId, HttpRequestUtil.getOperator(request));
    }

    @GetMapping(path = "")
    @ResponseBody
    @ApiOperation(value = "任务列表接口", notes = "")
    public Result<List<EcmTaskVO>> listWorkOrderTask() {
        List<EcmTask> ecmTaskPOList = ecmTaskManager.listEcmTask();
        if (AriusObjUtils.isNull(ecmTaskPOList)) {
            return Result.buildNotExist(ResultType.NOT_EXIST.getMessage());
        }

        return Result.buildSucc(ConvertUtil.list2List(ecmTaskPOList, EcmTaskVO.class));
    }

    @GetMapping(path = "/{taskId}/basic-info")
    @ResponseBody
    @ApiOperation(value = "任务基本信息", notes = "")
    public Result<EcmTaskBasicVO> getWorkOrderTaskBasic(@PathVariable Long taskId) {
        Result<EcmTaskBasic> workOrderTaskBasicResult = ecmTaskManager.getEcmTaskBasicByTaskId(taskId);
        if (workOrderTaskBasicResult.failed()) {
            return Result.buildFail(workOrderTaskBasicResult.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(workOrderTaskBasicResult.getData(), EcmTaskBasicVO.class));
    }

    @GetMapping(path = "{taskId}/task-details")
    @ResponseBody
    @ApiOperation(value = "任务详细信息", notes = "")
    public Result<EcmTaskDetailVO> clusterTaskDetails(@PathVariable Long taskId) throws AdminTaskException {
        return Result.buildSucc(
            ConvertUtil.obj2Obj(ecmTaskDetailManager.getEcmTaskDetailInfo(taskId).getData(), EcmTaskDetailVO.class));
    }

    @GetMapping(path = "{detailId}/task-detail/log")
    @ResponseBody
    @ApiOperation(value = "工单任务详情操作日志接口", notes = "")
    public Result<EcmSubTaskLog> clusterTaskDetailLog(HttpServletRequest request, @PathVariable Long detailId) {
        return ecmTaskDetailManager.getTaskDetailLog(detailId, HttpRequestUtil.getOperator(request));
    }
}