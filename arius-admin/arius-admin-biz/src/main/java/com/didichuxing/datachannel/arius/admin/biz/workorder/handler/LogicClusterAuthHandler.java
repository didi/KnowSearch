package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterAuthContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterAuthOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("logicClusterAuthHandler")
public class LogicClusterAuthHandler extends BaseWorkOrderHandler {

    @Autowired
    private ClusterLogicService clusterLogicService;
    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectClusterLogicAuthService projectClusterLogicAuthService;

    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        LogicClusterAuthContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterAuthContent.class);

        if (AriusObjUtils.isNull(content.getLogicClusterId())) {
            return Result.buildParamIllegal("逻辑集群id为空");
        }

        if (AriusObjUtils.isNull(content.getAuthCode())) {
            return Result.buildParamIllegal("申请的权限为空");
        }

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(content.getLogicClusterId());
        if (clusterLogic == null) {
            return Result.buildParamIllegal("集群不存在");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        LogicClusterAuthContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterAuthContent.class);
        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getLogicClusterName() + workOrderTypeEnum.getMessage();
    }

    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        LogicClusterAuthContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterAuthContent.class);

        Result<Void> result = projectClusterLogicAuthService.ensureSetLogicClusterAuth(workOrder.getSubmitorProjectId(),
            content.getLogicClusterId(), ProjectClusterLogicAuthEnum.valueOf(content.getAuthCode()),
            workOrder.getSubmitor(), workOrder.getSubmitor());

        if (result.success()) {
            operateRecordService.save(new OperateRecord.Builder()
                            .operationTypeEnum(OperateTypeEnum.MY_CLUSTER_APPLY)
                            .bizId(content.getLogicClusterId())
                            .project(projectService.getProjectBriefByProjectId(workOrder.getSubmitorProjectId()))
                            .content(
                        workOrder.getSubmitor() + "申请" + content.getLogicClusterName() + "的"
                        + ProjectClusterLogicAuthEnum
                                                                                   .valueOf(content.getAuthCode()))
                            .userOperation(approver)
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                    .build());
           
        }

        return result;
    }

    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        LogicClusterAuthContent content = JSON.parseObject(extensions, LogicClusterAuthContent.class);

        return ConvertUtil.obj2Obj(content, LogicClusterAuthOrderDetail.class);
    }

    @Override
    public List<UserBriefVO> getApproverList(AbstractOrderDetail detail) {
        return getOPList();
    }

    @Override
    public Result<Void> checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isOP(userName)) {
            return Result.buildSucc();
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }
}