package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterIndecreaseContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterIndecreaseOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
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
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("logicClusterIndecreaseHandler")
public class LogicClusterIndecreaseHandler extends BaseWorkOrderHandler {

    @Autowired
    private ClusterLogicService            clusterLogicService;

    @Autowired
    private ProjectClusterLogicAuthService projectClusterLogicAuthService;

    @Autowired
    private ClusterNodeManager             clusterNodeManager;
  

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

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        LogicClusterIndecreaseContent content = JSON.parseObject(extensions, LogicClusterIndecreaseContent.class);
        LogicClusterIndecreaseOrderDetail logicClusterIndecreaseOrderDetail = ConvertUtil.obj2Obj(content,
                LogicClusterIndecreaseOrderDetail.class);
        //添加原有的节点数目
        Optional.ofNullable(content.getLogicClusterId())
                .map(clusterLogicService::getClusterLogicByIdThatNotContainsProjectId)
                .map(ClusterLogic::getDataNodeNum)
                .ifPresent(logicClusterIndecreaseOrderDetail::setOldDataNodeNu);
        return logicClusterIndecreaseOrderDetail;
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

    /**************************************** protected method ******************************************/

    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        LogicClusterIndecreaseContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterIndecreaseContent.class);

        if (AriusObjUtils.isNull(content.getLogicClusterId())) {
            return Result.buildParamIllegal("集群id为空");
        }

        
        if (!clusterLogicService.existClusterLogicById(content.getLogicClusterId())) {
            return Result.buildParamIllegal("集群不存在");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        LogicClusterIndecreaseContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterIndecreaseContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getLogicClusterName() + workOrderTypeEnum.getMessage();
    }

    /**
     * 验证用户是否有该工单权限
     * 要求只有集群所属的projectId才能操作
     *
     * @param workOrder 工单内容
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        LogicClusterIndecreaseContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterIndecreaseContent.class);

        ProjectClusterLogicAuthEnum logicClusterAuthEnum = projectClusterLogicAuthService
            .getLogicClusterAuthEnum(workOrder.getSubmitorProjectId(), content.getLogicClusterId());

        switch (logicClusterAuthEnum) {
            case ALL:
            case OWN:
                return Result.buildSucc();
            case ACCESS:
                return Result.buildParamIllegal("您的projectId无该集群的扩缩容权限");
            case NO_PERMISSIONS:
            default:
                return Result.buildParamIllegal("您的projectId无该集群的相关权限");
        }
    }

    /**
     * 验证平台参数
     *
     * @param workOrder 工单内容
     * @return result
     */
    @Override
    protected Result<Void> validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    /**
     * 处理工单
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        LogicClusterIndecreaseContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterIndecreaseContent.class);
        //执行前的节点结果
        final Result<List<ESClusterRoleHostVO>> resultBefore = clusterNodeManager.listClusterLogicNode(
                content.getLogicClusterId().intValue());
        Long beforeSize = 0L;
        if (resultBefore.success() && CollectionUtils.isNotEmpty(resultBefore.getData())) {
            beforeSize = resultBefore.getData().stream().map(ESClusterRoleHostVO::getId).distinct()
                   .count();
        
        }
       
        
        List<ClusterRegionWithNodeInfoDTO> clusterRegionWithNodeInfoDTOList = content.getRegionWithNodeInfo();

        Result<Boolean> regionEditResult = clusterNodeManager.editMultiNode2Region(clusterRegionWithNodeInfoDTOList,
            approver, workOrder.getSubmitorProjectId(), OperationEnum.EDIT );
        if (regionEditResult.failed()) {
            return Result.buildFrom(regionEditResult);
        }
       //执行后的节点结果
         final Result<List<ESClusterRoleHostVO>> resultAfter = clusterNodeManager.listClusterLogicNode(
                content.getLogicClusterId().intValue());
        Long afterSize=0L;
        if (resultAfter.success() && CollectionUtils.isNotEmpty(resultAfter.getData())) {
            afterSize = resultAfter.getData().stream().map(ESClusterRoleHostVO::getId).distinct().count();
            // 更新逻辑集群下的节点数目
            Long logicClusterId = content.getLogicClusterId();
            ESLogicClusterDTO esLogicClusterDTO = new ESLogicClusterDTO();
            esLogicClusterDTO.setId(logicClusterId);
            esLogicClusterDTO.setDataNodeNum(Math.toIntExact(afterSize));
            clusterLogicService.editClusterLogicNotCheck(esLogicClusterDTO);
        
        }
        
        operateRecordService.save(new OperateRecord.Builder().bizId(content.getLogicClusterId())
            .operationTypeEnum(OperateTypeEnum.MY_CLUSTER_CAPACITY).triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
            .content(String.format("%s：【%d】->【%d】",afterSize>beforeSize?"扩容":"缩容",beforeSize,afterSize))
            .userOperation(workOrder.getSubmitor())
            .project(projectService.getProjectBriefByProjectId(workOrder.getSubmitorProjectId())).build());
        return Result.buildFrom(regionEditResult);
    }
}