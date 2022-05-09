package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterPlugOperationContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterPlugOperationOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.AriusWorkOrderInfoPO;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.OperationTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

@Service("logicClusterPlugOperationHandler")
public class LogicClusterPlugOperationHandler extends BaseWorkOrderHandler {

    @Autowired
    private AppClusterLogicAuthService appClusterLogicAuthService;

    @Autowired
    private RegionRackService rackService;

    @Autowired
    private ClusterPhyService esClusterPhyService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private EcmTaskManager             ecmTaskManager;

    @Autowired
    private RoleClusterService roleClusterService;

    @Autowired
    private EcmHandleService ecmHandleService;

    protected static final ILog        LOGGER = LogFactory.getLog(LogicClusterPlugOperationHandler.class);

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

        AppClusterLogicAuthEnum logicClusterAuthEnum = appClusterLogicAuthService
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
    protected Result<Void> validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        LogicClusterPlugOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPlugOperationContent.class);

        List<Integer> clusterStrIdList = rackService.listPhysicClusterId(content.getLogicClusterId());
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
    public List<AriusUserInfo> getApproverList(AbstractOrderDetail detail) {
        return getOPList();
    }

    @Override
    public Result<Void> checkAuthority(AriusWorkOrderInfoPO orderPO, String userName) {
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

        List<RoleCluster> roleClusterList = roleClusterService.getAllRoleClusterByClusterId(
                clusterPhy.getId());
        if (CollectionUtils.isEmpty(roleClusterList)) {
            return Result.buildFail("物理集群角色不存在");
        }

        List<String> roleNameList = new ArrayList<>();
        for (RoleCluster roleCluster : roleClusterList) {
            roleNameList.add(roleCluster.getRole());
        }

        esEcmTaskDTO.setWorkOrderId(workOrder.getId());
        esEcmTaskDTO.setTitle(workOrder.getTitle());
        esEcmTaskDTO.setOrderType(EcmTaskTypeEnum.RESTART.getCode());
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
        esClusterPhyService.editCluster(ConvertUtil.obj2Obj(clusterPhy, ESClusterDTO.class), workOrder.getSubmitor());
        return Result.buildSucc();
    }
}