package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.JoinLogicClusterContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterCreateOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("logicClusterJoinHandler")
public class LogicClusterJoinHandler extends BaseWorkOrderHandler {
    @Autowired
    private ClusterLogicManager clusterLogicManager;
    
    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        JoinLogicClusterContent content = JSON.parseObject(extensions, JoinLogicClusterContent.class);
        return ConvertUtil.obj2Obj(content, LogicClusterCreateOrderDetail.class);
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
    
    /**
     * 工单是否自动审批
     *
     * @param workOrder 工单类型
     * @return result
     */
    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }    /**************************************** protected method ******************************************/
    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        JoinLogicClusterContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                JoinLogicClusterContent.class);
        ESLogicClusterDTO resourceLogicDTO = ConvertUtil.obj2Obj(content, ESLogicClusterDTO.class);
        resourceLogicDTO.setProjectId(workOrder.getSubmitorProjectId());
        resourceLogicDTO.setId(content.getId());
        return clusterLogicManager.validateClusterLogicParams(resourceLogicDTO, OperationEnum.ADD_BIND_MULTIPLE_PROJECT,
                resourceLogicDTO.getProjectId());
    }
    
    @Override
    protected String getTitle(WorkOrder workOrder) {
        JoinLogicClusterContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                JoinLogicClusterContent.class);
        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        
        return content.getId() + workOrderTypeEnum.getMessage();
    }
    
    /**
     * 验证用户是否有该工单权限
     *
     * @param workOrder 工单内容
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        return Result.buildSucc();
    }
    
    /**
     * 处理工单 这里分为两种，一种是分配逻辑集群，一种是申请逻辑集群，其中申请逻辑集群不会绑定region，会联系同学在运维侧进行操作
     */
    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        ESLogicClusterDTO esLogicClusterDTO = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                ESLogicClusterDTO.class);
        esLogicClusterDTO.setProjectId(workOrder.getSubmitorProjectId());
        Result<Void> result = clusterLogicManager.joinClusterLogic(esLogicClusterDTO.getId(),
                workOrder.getSubmitorProjectId());
        if (result.success()) {
            ClusterLogicVO clusterLogic = clusterLogicManager.getClusterLogic(esLogicClusterDTO.getId(),
                    workOrder.getSubmitorProjectId());
            //操作记录
            // 逻辑集群创建添加操作记录
            operateRecordService.save(new OperateRecord.Builder().operationTypeEnum(OperateTypeEnum.MY_CLUSTER_APPLY)
                    .bizId(esLogicClusterDTO.getId())
                    .project(projectService.getProjectBriefByProjectId(workOrder.getSubmitorProjectId()))
                    .content(String.format("申请:【%s】", clusterLogic.getName())).userOperation(workOrder.getSubmitor())
                    .buildDefaultManualTrigger());
        }
        return Result.buildFrom(result);
    }
    
    @Override
    protected Result validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }
}