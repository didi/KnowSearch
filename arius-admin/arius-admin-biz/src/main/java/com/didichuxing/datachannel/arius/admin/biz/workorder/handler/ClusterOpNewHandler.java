package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.CLIENT_NODE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum.ES_DOCKER;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum.ES_HOST;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterConstant.MASTER_NODE_MIN_NUMBER;
import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.WORK_ORDER_CLUSTER_OP_NEW;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterOpBaseContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterOpNewDockerContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterOpNewHostContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.notify.ClusterOpNewNotify;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.WorkOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleDocker;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterOpNewDockerOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterOpNewHostOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ESClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;

@Service("clusterOpNewHandler")
public class ClusterOpNewHandler extends BaseWorkOrderHandler {

    @Autowired
    private WorkTaskManager     workTaskManager;

    @Autowired
    private ESClusterPhyService esClusterPhyService;


    @Override
    protected Result validateConsoleParam(WorkOrder workOrder) {
        ClusterOpBaseContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterOpBaseContent.class);
        
        Result doValidateHostTypeResult = validateClusterMasterNodeNumber(content, workOrder);
        if (doValidateHostTypeResult.failed()) {
            return doValidateHostTypeResult;
        }

        if (AriusObjUtils.isNull(content.getPhyClusterName())) {
            return Result.buildParamIllegal("物理集群名称为空");
        }

        if (esClusterPhyService.isClusterExists(content.getPhyClusterName())) {
            return Result.buildParamIllegal("物理集群名称不能重复！");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        ClusterOpBaseContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterOpBaseContent.class);
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
    protected Result doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        ClusterOpBaseContent clusterOpBaseContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterOpBaseContent.class);

        EcmTaskDTO ecmTaskDTO = new EcmTaskDTO();
        ecmTaskDTO.setWorkOrderId(workOrder.getId());
        ecmTaskDTO.setTitle(workOrder.getTitle());
        ecmTaskDTO.setOrderType(EcmTaskTypeEnum.NEW.getCode());
        ecmTaskDTO.setType(clusterOpBaseContent.getType());
        ecmTaskDTO.setCreator(workOrder.getSubmitor());
        ecmTaskDTO.setPhysicClusterId(ESClusterConstant.INVALID_VALUE);

        // 工单数据 转 handle data
        List<EcmParamBase> ecmParamBaseList = null;
        if (ES_DOCKER.getCode() == clusterOpBaseContent.getType()) {
            ecmParamBaseList = WorkOrderTaskConverter.convert2EcmParamBaseList(ESClusterTypeEnum.ES_DOCKER,
                EcmTaskTypeEnum.NEW,
                ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterOpNewDockerContent.class));
        } else if (ES_HOST.getCode() == clusterOpBaseContent.getType()) {
            ecmParamBaseList = WorkOrderTaskConverter.convert2EcmParamBaseList(ESClusterTypeEnum.ES_HOST,
                EcmTaskTypeEnum.NEW,
                ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterOpNewHostContent.class));
        } else {
            return Result.buildFail("集群类型(Docker|Host)错误");
        }

        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);

        WorkTaskDTO workTaskDTO = new WorkTaskDTO();
        workTaskDTO.setTaskType(WorkTaskTypeEnum.CLUSTER_NEW.getType());
        workTaskDTO.setCreator(workOrder.getSubmitor());
        workTaskDTO.setExpandData(ConvertUtil.obj2Json(ecmTaskDTO));

        Result result = workTaskManager.addTask(workTaskDTO);
        if (null == result || result.failed()) {
            return Result.buildFail("生成集群新建操作任务失败!");
        }

        if (EnvUtil.isOnline()) {
            sendNotify(WORK_ORDER_CLUSTER_OP_NEW, new ClusterOpNewNotify(workOrder.getSubmitorAppid(),
                clusterOpBaseContent.getPhyClusterName(), approver), Arrays.asList(workOrder.getSubmitor()));
        }

        return Result.buildSucc();
    }

    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        ClusterOpBaseContent clusterOpBaseContent = ConvertUtil.obj2ObjByJSON(JSON.parse(extensions),
            ClusterOpBaseContent.class);

        if (ES_DOCKER.getCode() == clusterOpBaseContent.getType()) {
            ClusterOpNewDockerContent content = JSON.parseObject(JSON.parse(extensions).toString(),
                ClusterOpNewDockerContent.class);

            return ConvertUtil.obj2Obj(content, ClusterOpNewDockerOrderDetail.class);
        } else if (ES_HOST.getCode() == clusterOpBaseContent.getType()) {
            ClusterOpNewHostContent content = JSON.parseObject(JSON.parse(extensions).toString(),
                ClusterOpNewHostContent.class);

            return ConvertUtil.obj2Obj(content, ClusterOpNewHostOrderDetail.class);
        } else {
            return null;
        }
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
    
    /*******************************************private************************************************/

    private Result validateClusterMasterNodeNumber(ClusterOpBaseContent content, WorkOrder workOrder) {

        if (ES_HOST.getCode() == content.getType()) {
            content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterOpNewHostContent.class);

            List<ESClusterRoleHost> roleClusterHosts = ((ClusterOpNewHostContent) content).getRoleClusterHosts();
            if (CollectionUtils.isEmpty(roleClusterHosts)) {
                return Result.buildParamIllegal("集群角色为空");
            }

            Set<String> hostRoles = roleClusterHosts.stream().map(ESClusterRoleHost::getRole)
                .collect(Collectors.toSet());
            if (!hostRoles.contains(MASTER_NODE.getDesc())) {
                return Result.buildParamIllegal(String.format("集群缺少类型为%s的节点", MASTER_NODE.getDesc()));
            }

            if (!hostRoles.contains(DATA_NODE.getDesc())) {
                return Result.buildParamIllegal(String.format("集群缺少类型为%s的节点", DATA_NODE.getDesc()));
            }

            if (!hostRoles.contains(CLIENT_NODE.getDesc())) {
                return Result.buildParamIllegal(String.format("集群缺少类型为%s的节点", CLIENT_NODE.getDesc()));
            }

            List<ESClusterRoleHost> masterNodes = roleClusterHosts.stream()
                .filter(r -> MASTER_NODE.getDesc().equals(r.getRole())).collect(Collectors.toList());

            if (masterNodes.size() < MASTER_NODE_MIN_NUMBER) {
                return Result.buildParamIllegal("masternode角色ip个数要求大于等于3, 且不重复");
            }

        } else if (ES_DOCKER.getCode() == content.getType()) {
            content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterOpNewDockerContent.class);

            List<ESClusterRoleDocker> roleClusterDockers = ((ClusterOpNewDockerContent) content).getRoleClusters();
            if (CollectionUtils.isEmpty(roleClusterDockers)) {
                return Result.buildParamIllegal("集群角色为空");
            }

            Set<String> dockerRoles = roleClusterDockers.stream().map(ESClusterRoleDocker::getRole)
                .collect(Collectors.toSet());
            if (!dockerRoles.contains(MASTER_NODE.getDesc())) {
                return Result.buildParamIllegal(String.format("集群缺少类型为%s的节点", MASTER_NODE.getDesc()));
            }

            if (!dockerRoles.contains(DATA_NODE.getDesc())) {
                return Result.buildParamIllegal(String.format("集群缺少类型为%s的节点", DATA_NODE.getDesc()));
            }

            if (!dockerRoles.contains(CLIENT_NODE.getDesc())) {
                return Result.buildParamIllegal(String.format("集群缺少类型为%s的节点", CLIENT_NODE.getDesc()));
            }

            Integer masterNodesNumber = roleClusterDockers
                                        .stream()
                                        .filter(r -> MASTER_NODE.getDesc().equals(r.getRole()))
                                        .map(ESClusterRoleDocker::getPodNumber)
                                        .collect(Collectors.toList()).get(0);

            if (masterNodesNumber < MASTER_NODE_MIN_NUMBER) {
                return Result.buildParamIllegal("masternode角色pod数量要求大于等于3, 且不重复");
            }
        }

        return Result.buildSucc();
    }
}
