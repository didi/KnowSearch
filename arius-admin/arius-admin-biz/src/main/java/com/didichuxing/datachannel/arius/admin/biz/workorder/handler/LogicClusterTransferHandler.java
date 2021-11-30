package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.WORK_ORDER_CLUSTER_LOGIC_TRANSFER;

import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterLogicTransferContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.notify.ClusterLogicTransferNotify;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterLogicTransferOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;

/**
 * Created by linyunan on 2021-06-17
 */
@Service
public class LogicClusterTransferHandler extends BaseWorkOrderHandler {

    @Autowired
    private AppService            appService;

    @Autowired
    private AriusUserInfoService  ariusUserInfoService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        ClusterLogicTransferContent clusterLogicTransferContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterLogicTransferContent.class);

        Integer sourceAppId = clusterLogicTransferContent.getSourceAppId();
        Integer targetAppId = clusterLogicTransferContent.getTargetAppId();
        if (AriusObjUtils.isNull(sourceAppId)) {
            return Result.buildParamIllegal("原项目Id为空");
        }
        if (AriusObjUtils.isNull(targetAppId)) {
            return Result.buildParamIllegal("目标项目Id为空");
        }

        App sourceApp = appService.getAppById(sourceAppId);
        if (AriusObjUtils.isNull(sourceApp)) {
            return Result.buildParamIllegal("原项目不存在");
        }

        App targetApp = appService.getAppById(targetAppId);
        if (AriusObjUtils.isNull(targetApp)) {
            return Result.buildParamIllegal("目标项目不存在");
        }

        List<String> responsibles = ListUtils.string2StrList(clusterLogicTransferContent.getTargetResponsible());
        if (CollectionUtils.isEmpty(responsibles)) {
            return Result.buildParamIllegal("负责人为空");
        }

        for (String responsible : responsibles) {
            if (!ariusUserInfoService.isExist(responsible)) {
                return Result.buildParamIllegal("负责人非法");
            }
        }

        Long clusterLogicId = clusterLogicTransferContent.getClusterLogicId();
        if (!clusterLogicService.isClusterLogicExists(clusterLogicId)) {
            return Result.buildParamIllegal("逻辑集群不存在");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        ClusterLogicTransferContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterLogicTransferContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getClusterLogicName() + workOrderTypeEnum.getMessage();
    }

    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        ClusterLogicTransferContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterLogicTransferContent.class);

        if (AriusObjUtils.isNull(content.getSourceAppId())) {
            return Result.buildParamIllegal("原appId为空");
        }

        if (AriusObjUtils.isNull(content.getTargetAppId())) {
            return Result.buildParamIllegal("目标appId为空");
        }

        if (content.getTargetAppId().equals(content.getSourceAppId())) {
            return Result.buildFail("无效转让, 原始项目Id和目标项目ID相同");
        }

        if (appService.isSuperApp(workOrder.getSubmitorAppid())) {
            return Result.buildSucc();
        }

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(content.getClusterLogicId());
        if (!clusterLogic.getAppId().equals(workOrder.getSubmitorAppid())) {
            return Result.buildOpForBidden("您无权对该集群进行转让操作");
        }

        return Result.buildSucc();
    }

    @Override
    protected Result<Void> validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) {
        ClusterLogicTransferContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                                              ClusterLogicTransferContent.class);
        
        Result<Void> result = clusterLogicService.transferClusterLogic(content.getClusterLogicId(),
            content.getTargetAppId(), content.getTargetResponsible(), workOrder.getSubmitor());

        if (result.success()){
            ClusterLogicTransferNotify build = ClusterLogicTransferNotify
                                                .builder()
                                                .clusterLogicName(content.getClusterLogicName())
                                                .sourceAppId(content.getSourceAppId())
                                                .targetAppId(content.getTargetAppId())
                                                .currentAppId(workOrder.getSubmitorAppid())
                                                .targetResponsible(content.getTargetResponsible())
                                                .build();

            sendNotify(WORK_ORDER_CLUSTER_LOGIC_TRANSFER, build, Collections.singletonList(workOrder.getSubmitor()));
        }

        return Result.buildFrom(result);
    }

    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        return ConvertUtil.obj2ObjByJSON(JSON.parse(extensions), ClusterLogicTransferOrderDetail.class);
    }

    @Override
    public List<AriusUserInfo> getApproverList(AbstractOrderDetail detail) {
        return getOPList();
    }

    @Override
    public Result<Void> checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isRDOrOP(userName)) {
            return Result.buildSucc();
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }
}
