package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterAuthContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.notify.LogicClusterAuthNotify;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterAuthOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicClusterAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.CLUSTER;
import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.WORK_ORDER_LOGIC_CLUSTER_AUTH;

@Service("logicClusterAuthHandler")
public class LogicClusterAuthHandler extends BaseWorkOrderHandler {

    @Autowired
    private ESClusterLogicService      esClusterLogicService;

    @Autowired
    private AppLogicClusterAuthService appLogicClusterAuthService;

    @Override
    protected Result validateConsoleParam(WorkOrder workOrder) {
        LogicClusterAuthContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterAuthContent.class);

        if (AriusObjUtils.isNull(content.getLogicClusterId())) {
            return Result.buildParamIllegal("逻辑集群id为空");
        }

        if (AriusObjUtils.isNull(content.getAuthCode())) {
            return Result.buildParamIllegal("申请的权限为空");
        }

        ESClusterLogic esClusterLogic = esClusterLogicService.getLogicClusterById(content.getLogicClusterId());
        if (esClusterLogic == null) {
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
    protected Result validateConsoleAuth(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        LogicClusterAuthContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterAuthContent.class);

        Result result = appLogicClusterAuthService.ensureSetLogicClusterAuth(workOrder.getSubmitorAppid(),
            content.getLogicClusterId(), AppLogicClusterAuthEnum.valueOf(content.getAuthCode()),
            workOrder.getSubmitor(), workOrder.getSubmitor());

        if (null != result && result.success()) {
            operateRecordService
                .save(CLUSTER, OperationEnum.ADD, content.getLogicClusterId(),
                    workOrder.getSubmitor() + "申请" + content.getLogicClusterName() + "的"
                                                                               + AppLogicClusterAuthEnum
                                                                                   .valueOf(content.getAuthCode()),
                    approver);

            sendNotify(WORK_ORDER_LOGIC_CLUSTER_AUTH,
                new LogicClusterAuthNotify(workOrder.getSubmitorAppid(), content.getLogicClusterName(), approver),
                Arrays.asList(workOrder.getSubmitor()));
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
    public List<AriusUserInfo> getApproverList(AbstractOrderDetail detail) {
        return getOPList();
    }

    @Override
    public Result checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isOP(userName)) {
            return Result.buildSucc(true);
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }
}
