package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterPlugOperationContent;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterPlugOperationOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.order.OperationTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicClusterAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.notify.LogicClusterPluginNotify;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.WORK_ORDER_LOGIC_CLUSTER_PLUGIN;

@Service("logicClusterPlugOperationHandler")
public class LogicClusterPlugOperationHandler extends BaseWorkOrderHandler {

    @Autowired
    private AppLogicClusterAuthService appLogicClusterAuthService;

    @Autowired
    private ESRegionRackService        rackService;

    @Autowired
    private ESClusterPhyService        esClusterPhyService;

    @Autowired
    private ESClusterLogicService      esClusterLogicService;

    @Autowired
    private EcmTaskManager             ecmTaskManager;

    @Autowired
    private ESRoleClusterService       esRoleClusterService;

    @Autowired
    private EcmHandleService           ecmHandleService;

    protected static final ILog        LOGGER = LogFactory.getLog(LogicClusterPlugOperationHandler.class);

    @Override
    protected Result validateConsoleParam(WorkOrder workOrder) {
        LogicClusterPlugOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPlugOperationContent.class);

        if (AriusObjUtils.isNull(content.getLogicClusterId())) {
            return Result.buildParamIllegal("物理集群id为空！");
        }

        ESClusterLogic esClusterLogic = esClusterLogicService.getLogicClusterById(content.getLogicClusterId());
        if (esClusterLogic == null) {
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
    protected Result validateConsoleAuth(WorkOrder workOrder) {
        LogicClusterPlugOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPlugOperationContent.class);

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
        LogicClusterPlugOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterPlugOperationContent.class);

        List<Integer> clusterStrIdList = rackService.listPhysicClusterId(content.getLogicClusterId());
        for (Integer clusterId : clusterStrIdList) {
            Result result = editClusterAndSave2WorkOrderTask(clusterId, workOrder, content);
            if (result.failed()) {
                return result;
            }
        }

        sendNotify(WORK_ORDER_LOGIC_CLUSTER_PLUGIN,
            new LogicClusterPluginNotify(workOrder.getSubmitorAppid(), content.getLogicClusterName(), approver),
            Arrays.asList(workOrder.getSubmitor()));
        return Result.buildSucc();
    }

    private Result<EcmTaskDTO> editClusterAndSave2WorkOrderTask(Integer clusterId, WorkOrder workOrder,
                                                                LogicClusterPlugOperationContent content) {
        EcmTaskDTO esEcmTaskDTO = new EcmTaskDTO();

        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(Integer.valueOf(clusterId));
        esEcmTaskDTO.setPhysicClusterId(esClusterPhy.getId().longValue());

        List<ESRoleCluster> esRoleClusterList = esRoleClusterService.getAllRoleClusterByClusterId(esClusterPhy.getId());
        if (CollectionUtils.isEmpty(esRoleClusterList)) {
            return Result.buildFail("物理集群角色不存在");
        }

        List<String> roleNameList = new ArrayList<>();
        for (ESRoleCluster esRoleCluster : esRoleClusterList) {
            roleNameList.add(esRoleCluster.getRole());
        }

        esEcmTaskDTO.setWorkOrderId(workOrder.getId());
        esEcmTaskDTO.setTitle(workOrder.getTitle());
        esEcmTaskDTO.setOrderType(EcmTaskTypeEnum.PLUG_OPERATION.getCode());
        esEcmTaskDTO.setEcmParamBaseList(ecmHandleService.buildEcmParamBaseList(clusterId, roleNameList).getData());
        esEcmTaskDTO.setClusterNodeRole(ListUtils.strList2String(roleNameList));
        esEcmTaskDTO.setCreator(workOrder.getSubmitor());
        esEcmTaskDTO.setType(esClusterPhy.getType());
        ecmTaskManager.saveEcmTask(esEcmTaskDTO);

        List<Long> plugIdList = ListUtils.string2LongList(esClusterPhy.getPlugIds());
        if (OperationTypeEnum.INSTALL.getCode().equals(content.getOperationType())) {
            plugIdList.addAll(ListUtils.string2LongList(content.getPlugIds()));
        } else {
            plugIdList.removeAll(ListUtils.string2LongList(content.getPlugIds()));
        }
        esClusterPhy.setPlugIds(ListUtils.longList2String(plugIdList.stream().distinct().collect(Collectors.toList())));
        esClusterPhyService.editCluster(ConvertUtil.obj2Obj(esClusterPhy, ESClusterDTO.class), workOrder.getSubmitor());
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
    public Result checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isOP(userName)) {
            return Result.buildSucc(true);
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }
}
