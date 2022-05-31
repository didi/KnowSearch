package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterLogicTransferContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterLogicTransferOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by linyunan on 2021-06-17
 */
@Service
public class LogicClusterTransferHandler extends BaseWorkOrderHandler {

    @Autowired
    private ProjectService projectService;

  

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        ClusterLogicTransferContent clusterLogicTransferContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterLogicTransferContent.class);

        Integer sourceProjectId = clusterLogicTransferContent.getSourceProjectId();
        Integer targetProjectId = clusterLogicTransferContent.getTargetProjectId();
        if (AriusObjUtils.isNull(sourceProjectId)) {
            return Result.buildParamIllegal("原项目Id为空");
        }
        if (AriusObjUtils.isNull(targetProjectId)) {
            return Result.buildParamIllegal("目标项目Id为空");
        }

       
        if (projectService.checkProjectExist(sourceProjectId)) {
            return Result.buildParamIllegal("原项目不存在");
        }

       
        if (projectService.checkProjectExist(targetProjectId)) {
            return Result.buildParamIllegal("目标项目不存在");
        }

        if (StringUtils.isBlank(clusterLogicTransferContent.getTargetResponsible())) {
            return Result.buildParamIllegal("负责人为空");
        }
       
        if (!AuthConstant.SUPER_USER_NAME.equals(clusterLogicTransferContent.getTargetResponsible())) {
            return Result.buildParamIllegal("负责人非法");
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

        if (AriusObjUtils.isNull(content.getSourceProjectId())) {
            return Result.buildParamIllegal("原projectId为空");
        }

        if (AriusObjUtils.isNull(content.getTargetProjectId())) {
            return Result.buildParamIllegal("目标projectId为空");
        }

        if (content.getTargetProjectId().equals(content.getSourceProjectId())) {
            return Result.buildFail("无效转让, 原始项目Id和目标项目ID相同");
        }

        if (projectService.checkProjectExist(workOrder.getSubmitorProjectId())) {
            return Result.buildSucc();
        }

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(content.getClusterLogicId());
        if (!clusterLogic.getProjectId().equals(workOrder.getSubmitorProjectId())) {
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
            content.getTargetProjectId(), content.getTargetResponsible(), workOrder.getSubmitor());

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
    public List<UserBriefVO> getApproverList(AbstractOrderDetail detail) {
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