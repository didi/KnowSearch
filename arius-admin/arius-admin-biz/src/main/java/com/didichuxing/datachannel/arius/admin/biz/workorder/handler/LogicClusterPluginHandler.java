package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterPluginContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.notify.LogicClusterPluginNotify;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterPluginOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicClusterAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.WORK_ORDER_LOGIC_CLUSTER_PLUGIN;

@Service("logicClusterPluginHandler")
public class LogicClusterPluginHandler extends BaseWorkOrderHandler {

    @Autowired
    private ESClusterLogicService      esClusterLogicService;

    @Autowired
    private AppLogicClusterAuthService appLogicClusterAuthService;

    @Override
    protected Result validateConsoleParam(WorkOrder workOrder) {
        LogicClusterPluginContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPluginContent.class);

        ESClusterLogic esClusterLogic = esClusterLogicService.getLogicClusterById(content.getLogicClusterId());
        if (esClusterLogic == null) {
            return Result.buildParamIllegal("集群不存在");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        LogicClusterPluginContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPluginContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getLogicClusterName() + workOrderTypeEnum.getMessage();
    }

    @Override
    protected Result validateConsoleAuth(WorkOrder workOrder) {
        LogicClusterPluginContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPluginContent.class);

        AppLogicClusterAuthEnum logicClusterAuthEnum = appLogicClusterAuthService
            .getLogicClusterAuthEnum(workOrder.getSubmitorAppid(), content.getLogicClusterId());

        switch (logicClusterAuthEnum) {
            case OWN:
            case ALL:
                return Result.buildSucc();
            case ACCESS:
                return Result.buildParamIllegal("您的appid无该集群的管理权限进行插件安装");
            case NO_PERMISSIONS:
            default:
                return Result.buildParamIllegal("您的appid无该集群的相关权限");
        }
    }

    @Override
    protected Result validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        LogicClusterPluginContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPluginContent.class);

        sendNotify(WORK_ORDER_LOGIC_CLUSTER_PLUGIN,
            new LogicClusterPluginNotify(workOrder.getSubmitorAppid(), content.getLogicClusterName(), approver),
            Arrays.asList(workOrder.getSubmitor()));

        return Result.buildSucc();
    }

    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        LogicClusterPluginContent content = JSON.parseObject(extensions, LogicClusterPluginContent.class);

        return ConvertUtil.obj2Obj(content, LogicClusterPluginOrderDetail.class);
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
