package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterPlugOperationContent;
import com.didichuxing.datachannel.arius.admin.biz.task.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterPlugOperationOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.OperationTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

@Service("logicClusterPlugOperationHandler")
public class LogicClusterPlugOperationHandler extends BaseWorkOrderHandler {

    @Autowired
    private ProjectClusterLogicAuthService projectClusterLogicAuthService;

    @Autowired
    private ClusterRegionService           clusterRegionService;

    @Autowired
    private ClusterPhyService              esClusterPhyService;

    @Autowired
    private ClusterLogicService            clusterLogicService;

    @Autowired
    private EcmTaskManager                 ecmTaskManager;

    @Autowired
    private ClusterRoleService             clusterRoleService;

    @Autowired
    private EcmHandleService               ecmHandleService;

    protected static final ILog            LOGGER = LogFactory.getLog(LogicClusterPlugOperationHandler.class);

    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        LogicClusterPlugOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPlugOperationContent.class);

        if (AriusObjUtils.isNull(content.getLogicClusterId())) {
            return Result.buildParamIllegal("物理集群id为空！");
        }

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(content.getLogicClusterId());
        if (clusterLogic == null) {
            return Result.buildParamIllegal("集群不存在");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        LogicClusterPlugOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPlugOperationContent.class);
        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        OperationTypeEnum typeEnum = OperationTypeEnum.valueOfCode(content.getOperationType());
        return content.getLogicClusterName() + " " + content.getPlugName() + workOrderTypeEnum.getMessage() + "-"
               + typeEnum.getMessage();
    }

    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        LogicClusterPlugOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPlugOperationContent.class);

        ProjectClusterLogicAuthEnum logicClusterAuthEnum = projectClusterLogicAuthService
            .getLogicClusterAuthEnum(workOrder.getSubmitorProjectId(), content.getLogicClusterId());

        switch (logicClusterAuthEnum) {
            case OWN:
            case ALL:
                return Result.buildSucc();
            case ACCESS:
                return Result.buildParamIllegal("您的projectId无该集群的管理权限进行插件安装");
            case NO_PERMISSIONS:
            default:
                return Result.buildParamIllegal("您的projectId无该集群的相关权限");
        }
    }

    @Override
    protected Result<Void> validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        LogicClusterPlugOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPlugOperationContent.class);

        List<Integer> clusterStrIdList = clusterRegionService.listPhysicClusterId(content.getLogicClusterId());
        for (Integer clusterId : clusterStrIdList) {
            Result<EcmTaskDTO> result = editClusterAndSave2WorkOrderTask(clusterId, workOrder, content);
            if (result.failed()) {
                return Result.buildFrom(result);
            }
        }
        return Result.buildSucc();
    }

    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        LogicClusterPlugOperationContent content = JSON.parseObject(extensions, LogicClusterPlugOperationContent.class);

        return ConvertUtil.obj2Obj(content, LogicClusterPlugOperationOrderDetail.class);
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

    private Result<EcmTaskDTO> editClusterAndSave2WorkOrderTask(Integer clusterId, WorkOrder workOrder,
                                                                LogicClusterPlugOperationContent content) {
        EcmTaskDTO esEcmTaskDTO = new EcmTaskDTO();

        ClusterPhy clusterPhy = esClusterPhyService.getClusterById(clusterId);
        esEcmTaskDTO.setPhysicClusterId(clusterPhy.getId().longValue());

        List<ClusterRoleInfo> clusterRoleInfoList = clusterRoleService.getAllRoleClusterByClusterId(clusterPhy.getId());
        if (CollectionUtils.isEmpty(clusterRoleInfoList)) {
            return Result.buildFail("物理集群角色不存在");
        }

        List<String> roleNameList = new ArrayList<>();
        for (ClusterRoleInfo clusterRoleInfo : clusterRoleInfoList) {
            roleNameList.add(clusterRoleInfo.getRole());
        }

        esEcmTaskDTO.setWorkOrderId(workOrder.getId());
        esEcmTaskDTO.setTitle(workOrder.getTitle());
        esEcmTaskDTO.setOrderType(OpTaskTypeEnum.CLUSTER_RESTART.getType());
        esEcmTaskDTO.setEcmParamBaseList(ecmHandleService.buildEcmParamBaseList(clusterId, roleNameList).getData());
        esEcmTaskDTO.setClusterNodeRole(ListUtils.strList2String(roleNameList));
        esEcmTaskDTO.setCreator(workOrder.getSubmitor());
        esEcmTaskDTO.setType(clusterPhy.getType());
        ecmTaskManager.saveEcmTask(esEcmTaskDTO);

        List<Long> plugIdList = ListUtils.string2LongList(clusterPhy.getPlugIds());
        if (OperationTypeEnum.INSTALL.getCode().equals(content.getOperationType())) {
            plugIdList.addAll(ListUtils.string2LongList(content.getPlugIds()));
        } else {
            plugIdList.removeAll(ListUtils.string2LongList(content.getPlugIds()));
        }
        clusterPhy.setPlugIds(ListUtils.longList2String(plugIdList.stream().distinct().collect(Collectors.toList())));
        esClusterPhyService.editCluster(ConvertUtil.obj2Obj(clusterPhy, ClusterPhyDTO.class), workOrder.getSubmitor());
        return Result.buildSucc();
    }
}