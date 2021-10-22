package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterDeleteContent;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterDeleteOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;

/**
 * Created by linyunan on 2021-06-11
 */
@Service
public class ClusterDeleteHandler extends BaseWorkOrderHandler {

    @Autowired
    private ClusterContextManager clusterContextManager;

    @Override
    protected Result validateConsoleParam(WorkOrder workOrder) {
        ClusterDeleteContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterDeleteContent.class);

        if (AriusObjUtils.isNull(content.getPhyClusterName())) {
            return Result.buildParamIllegal("物理集群名称为空");
        }

        /*ESClusterPhyContext esClusterPhyContext = clusterContextManager.getESClusterPhyContext(content.getPhyClusterName());
        if (esClusterPhyContext.getAssociatedLogicNum() > 0) {
            return Result.buildParamIllegal("物理集群已关联逻辑集群, 请先解除region绑定");
        }*/

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        ClusterDeleteContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterDeleteContent.class);
        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getPhyClusterName() + workOrderTypeEnum.getMessage();
    }

    @Override
    protected Result validateConsoleAuth(WorkOrder workOrder) {
        if (!isOP(workOrder.getSubmitor())) {
            return Result.buildOpForBidden("非运维人员不能操作集群扩缩容！");
        }

        return Result.buildSucc();
    }

    @Override
    protected Result validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result doProcessAgree(WorkOrder workOrder, String approver) {
        List<String> administrators = getOPList().stream().map(AriusUserInfo::getName).collect(Collectors.toList());
        return Result.buildSucc(
            String.format("请联系管理员【%s】进行后续操作", administrators.get(new Random().nextInt(administrators.size()))));
    }

    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        return ConvertUtil.obj2Obj(extensions, ClusterDeleteOrderDetail.class);
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
