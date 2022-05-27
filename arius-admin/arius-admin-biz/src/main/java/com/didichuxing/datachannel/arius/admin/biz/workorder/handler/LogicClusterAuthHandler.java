package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.CLUSTER;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterAuthContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterAuthOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("logicClusterAuthHandler")
public class LogicClusterAuthHandler extends BaseWorkOrderHandler {

    @Autowired
    private ClusterLogicService clusterLogicService;

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

        Result<Void> result = projectClusterLogicAuthService.ensureSetLogicClusterAuth(workOrder.getSubmitorAppid(),
            content.getLogicClusterId(), ProjectClusterLogicAuthEnum.valueOf(content.getAuthCode()),
            workOrder.getSubmitor(), workOrder.getSubmitor());

        if (null != result && result.success()) {
            operateRecordService
                .save(CLUSTER, OperationEnum.ADD, content.getLogicClusterId(),
                        workOrder.getSubmitor() + "申请" + content.getLogicClusterName() + "的"
                        + ProjectClusterLogicAuthEnum
                                                                                   .valueOf(content.getAuthCode()),
                    approver);
        }

        return Result.buildFrom(result);
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
    public List<AriusUserInfo> getApproverList(AbstractOrderDetail detail) {
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