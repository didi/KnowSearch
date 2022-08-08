package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterCreateContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterCreateOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("logicClusterCreateHandler")
public class LogicClusterCreateHandler extends BaseWorkOrderHandler {

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private ClusterLogicManager clusterLogicManager;
    

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        LogicClusterCreateContent content = JSON.parseObject(extensions, LogicClusterCreateContent.class);

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
    }

    /**************************************** protected method ******************************************/

    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        LogicClusterCreateContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterCreateContent.class);

        ESLogicClusterDTO resourceLogicDTO = ConvertUtil.obj2Obj(content, ESLogicClusterDTO.class);
        resourceLogicDTO.setProjectId(workOrder.getSubmitorProjectId());
        resourceLogicDTO.setType(ClusterResourceTypeEnum.PRIVATE.getCode());
        Optional.ofNullable(content.getLogicId())
                .map(Integer::longValue)
                .ifPresent(resourceLogicDTO::setId);
        return clusterLogicService.validateClusterLogicParams(resourceLogicDTO,
                Objects.isNull(content.getLogicId())?
                OperationEnum.ADD:OperationEnum.ADD_BIND_MULTIPLE_PROJECT,
            resourceLogicDTO.getProjectId());
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        LogicClusterCreateContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterCreateContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getName() + workOrderTypeEnum.getMessage();
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
        ESLogicClusterWithRegionDTO esLogicClusterWithRegionDTO = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ESLogicClusterWithRegionDTO.class);
        esLogicClusterWithRegionDTO.setProjectId(workOrder.getSubmitorProjectId());

      
        Result<Void> result = Result.buildSucc();
        if ( //针对行内内置，默认绑定已经存在逻辑集群的时候，系统内置为true，跳过region判定
                Objects.nonNull(esLogicClusterWithRegionDTO.getId()) && Objects.equals(
                        esLogicClusterWithRegionDTO.getBindExistLogicCluster(), Boolean.TRUE)) {
            final ESLogicClusterDTO param = new ESLogicClusterDTO();
            param.setId(esLogicClusterWithRegionDTO.getId());
            param.setProjectId(esLogicClusterWithRegionDTO.getProjectId());
            param.setType(esLogicClusterWithRegionDTO.getType());
            Result<Long> longResult = clusterLogicService.createClusterLogic(param);
            result = Result.buildFrom(longResult);
    
        } else
            // 创建逻辑集群并且批量绑定指定的region,默认是能成功
        if (!CollectionUtils.isEmpty(esLogicClusterWithRegionDTO.getClusterRegionDTOS())) {
            //这里申请逻辑集群
            result = clusterLogicManager.addLogicClusterAndClusterRegions(esLogicClusterWithRegionDTO, approver);
        }
        if (result.success()) {
            //操作记录
            //逻辑集群创建添加操作记录
            operateRecordService.save(new OperateRecord.Builder().operationTypeEnum(OperateTypeEnum.MY_CLUSTER_APPLY)
                    .bizId(esLogicClusterWithRegionDTO.getName())
                    .project(projectService.getProjectBriefByProjectId(workOrder.getSubmitorProjectId()))
                    .content(String.format("申请:【%s】", esLogicClusterWithRegionDTO.getName()))
                    .userOperation(workOrder.getSubmitor())
                    .buildDefaultManualTrigger());
        }

        return Result.buildFrom(result);
    }

    @Override
    protected Result validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }
}