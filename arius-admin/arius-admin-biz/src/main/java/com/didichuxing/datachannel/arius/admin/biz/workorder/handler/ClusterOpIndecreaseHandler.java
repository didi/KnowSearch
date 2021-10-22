package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterOpBaseContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterOpIndecreaseDockerContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterOpIndecreaseHostContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.notify.ClusterOpIndecencyNotify;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.WorkOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostScaleActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterOpIndecreaseDockerOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterOpIndecreaseHostOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum.ES_DOCKER;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum.ES_HOST;
import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.WORK_ORDER_CLUSTER_OP_NEW;

@Service("clusterOpIndecreaseHandler")
public class ClusterOpIndecreaseHandler extends BaseWorkOrderHandler {
    protected static final ILog  LOGGER = LogFactory.getLog(ClusterOpIndecreaseHandler.class);

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Autowired
    private ESClusterPhyService  esClusterPhyService;

    @Autowired
    private EcmHandleService     ecmHandleService;

    @Autowired
    private WorkTaskManager workTaskManager;

    @Override
    protected Result validateConsoleParam(WorkOrder workOrder) {

        ClusterOpBaseContent baseContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterOpBaseContent.class);

        if (ES_DOCKER.getCode() == baseContent.getType()) {
            ClusterOpIndecreaseDockerContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                ClusterOpIndecreaseDockerContent.class);

            if (AriusObjUtils.isNull(content.getPhyClusterId())) {
                return Result.buildParamIllegal("物理集群id为空");
            }

            ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(content.getPhyClusterId().intValue());
            if (AriusObjUtils.isNull(esClusterPhy)) {
                return Result.buildParamIllegal("物理集群不存在");
            }

            if (workTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(),
                WorkTaskTypeEnum.CLUSTER_EXPAND.getType())) {
                return Result.buildParamIllegal("该集群上存在未完成的任务");
            }

            return Result.buildSucc();
        } else if (ES_HOST.getCode() == baseContent.getType()) {
            ClusterOpIndecreaseHostContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                ClusterOpIndecreaseHostContent.class);

            if (AriusObjUtils.isNull(content.getPhyClusterId())) {
                return Result.buildParamIllegal("物理集群id为空");
            }

            ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(content.getPhyClusterId().intValue());
            if (AriusObjUtils.isNull(esClusterPhy)) {
                return Result.buildParamIllegal("物理集群不存在");
            }

            if (workTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(),
                WorkTaskTypeEnum.CLUSTER_EXPAND.getType())) {
                return Result.buildParamIllegal("该集群上存在未完成的集群扩缩容任务");
            }

            return Result.buildSucc();
        } else {
            return Result.buildFail("type 类型不对");
        }

    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        ClusterOpBaseContent baseContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterOpBaseContent.class);
        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }

        return baseContent.getPhyClusterName() + workOrderTypeEnum.getMessage();
    }

    @Override
    protected Result validateConsoleAuth(WorkOrder workOrder) {
        if (!ariusUserInfoService.isOPByDomainAccount(workOrder.getSubmitor())) {
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

        EcmTaskDTO esEcmTaskDTO = new EcmTaskDTO();
        esEcmTaskDTO.setWorkOrderId(workOrder.getId());
        esEcmTaskDTO.setTitle(workOrder.getTitle());
        esEcmTaskDTO.setCreator(workOrder.getSubmitor());
        esEcmTaskDTO.setType(clusterOpBaseContent.getType());

        if (ES_DOCKER.getCode() == clusterOpBaseContent.getType()) {
            ClusterOpIndecreaseDockerContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                ClusterOpIndecreaseDockerContent.class);
            esEcmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
            esEcmTaskDTO.setOrderType(content.getOperationType());
            List<EcmParamBase> ecmParamBaseList = WorkOrderTaskConverter.convert2EcmParamBaseList(ES_DOCKER,
                EcmTaskTypeEnum.valueOf(content.getOperationType()), content);
            esEcmTaskDTO.setClusterNodeRole(ListUtils
                .strList2String(ecmParamBaseList.stream().map(EcmParamBase::getRoleName).collect(Collectors.toList())));
            esEcmTaskDTO.setEcmParamBaseList(ecmParamBaseList);
        } else if (ES_HOST.getCode() == clusterOpBaseContent.getType()) {
            ClusterOpIndecreaseHostContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                ClusterOpIndecreaseHostContent.class);
            esEcmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
            esEcmTaskDTO.setOrderType(content.getOperationType());

            List<EcmParamBase> hostScaleParamBaseList = getHostScaleParamBaseList(content.getPhyClusterId().intValue(),
                content.getRoleClusterHosts(), content.getPidCount());

            esEcmTaskDTO.setClusterNodeRole(ListUtils.strList2String(
                hostScaleParamBaseList.stream().map(EcmParamBase::getRoleName).collect(Collectors.toList())));
            esEcmTaskDTO.setEcmParamBaseList(hostScaleParamBaseList);
        } else {
            return Result.buildFail("type 类型不对");
        }

        WorkTaskDTO workTaskDTO = new WorkTaskDTO();
        workTaskDTO.setExpandData(JSONObject.toJSONString(esEcmTaskDTO));
        workTaskDTO.setTaskType(esEcmTaskDTO.getOrderType());
        workTaskDTO.setCreator(workOrder.getSubmitor());
        Result result = workTaskManager.addTask(workTaskDTO);
        if (null == result || result.failed()) {
            return Result.buildFail("生成集群新建操作任务失败!");
        }

        sendNotify(WORK_ORDER_CLUSTER_OP_NEW, new ClusterOpIndecencyNotify(workOrder.getSubmitorAppid(),
            clusterOpBaseContent.getPhyClusterName(), approver), Arrays.asList(workOrder.getSubmitor()));
        return Result.buildSucc();

    }

    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        ClusterOpBaseContent baseContent = ConvertUtil.obj2ObjByJSON(JSON.parse(extensions),
            ClusterOpBaseContent.class);

        if (ES_DOCKER.getCode() == baseContent.getType()) {
            ClusterOpIndecreaseDockerContent content = JSON.parseObject(JSON.parse(extensions).toString(),
                ClusterOpIndecreaseDockerContent.class);

            return ConvertUtil.obj2Obj(content, ClusterOpIndecreaseDockerOrderDetail.class);
        } else if (ES_HOST.getCode() == baseContent.getType()) {
            ClusterOpIndecreaseHostContent content = JSON.parseObject(JSON.parse(extensions).toString(),
                ClusterOpIndecreaseHostContent.class);

            return ConvertUtil.obj2Obj(content, ClusterOpIndecreaseHostOrderDetail.class);
        }
        return null;

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

    private List<EcmParamBase> getHostScaleParamBaseList(Integer phyClusterId, List<ESClusterRoleHost> roleClusterHosts,
                                                         Integer pidCount) {
        List<String> roleNameList = new ArrayList<>();
        for (ESClusterRoleHost clusterRoleHost : roleClusterHosts) {
            if (!roleNameList.contains(clusterRoleHost.getRole())) {
                roleNameList.add(clusterRoleHost.getRole());
            }
        }

        List<EcmParamBase> ecmParamBaseList = ecmHandleService.buildEcmParamBaseList(phyClusterId, roleNameList)
            .getData();
        List<EcmParamBase> hostScaleParamBaseList = new ArrayList<>();
        for (String roleName : roleNameList) {
            List<String> hostnameList = new ArrayList<>();
            for (ESClusterRoleHost clusterRoleHost : roleClusterHosts) {
                if (roleName.equals(clusterRoleHost.getRole())) {
                    if (ValidateUtils.isBlank(clusterRoleHost.getHostname())) {
                        continue;
                    }
                    hostnameList.add(clusterRoleHost.getHostname());
                }
            }
            for (EcmParamBase ecmParamBase : ecmParamBaseList) {
                if (roleName.equals(ecmParamBase.getRoleName())) {
                    HostParamBase hostParamBase = (HostParamBase) ecmParamBase;

                    HostScaleActionParam hostScaleActionParam = ConvertUtil.obj2Obj(hostParamBase,
                        HostScaleActionParam.class);
                    hostScaleActionParam.setPidCount(pidCount);
                    hostScaleActionParam.setHostList(hostnameList);
                    hostScaleActionParam.setNodeNumber(hostnameList.size());

                    hostScaleParamBaseList.add(hostScaleActionParam);
                }
            }
        }

        return hostScaleParamBaseList;
    }
}
