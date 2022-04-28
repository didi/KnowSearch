package com.didichuxing.datachannel.arius.admin.biz.workorder.handler.clusterReStart;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.PhyClusterPluginOperationContent;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.PhyClusterPluginOperationOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.OperationTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service("clusterOpPluginRestartHandler")
public class ClusterOpPluginRestartHandler extends ClusterOpRestartHandler {

    @Autowired
    private ESPluginService esPluginService;

    @Autowired
    private RoleClusterService roleClusterService;

    @Autowired
    private WorkTaskManager workTaskService;

    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private EcmTaskManager ecmTaskManager;

    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        PhyClusterPluginOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                PhyClusterPluginOperationContent.class);

        // 校验插件id
        if (AriusObjUtils.isNull(content.getPluginId())) {
            return Result.buildParamIllegal("插件id为空！");
        }
        PluginPO plugin = esPluginService.getESPluginById(content.getPluginId());
        if (AriusObjUtils.isNull(plugin)) {
            return Result.buildParamIllegal("插件不存在！");
        }

        // 校验操作类型
        if (AriusObjUtils.isNull(content.getOperationType())) {
            return Result.buildParamIllegal("未定义插件操作类型");
        }

        OperationTypeEnum operationType = OperationTypeEnum.valueOfCode(content.getOperationType());
        if (!operationType.equals(OperationTypeEnum.INSTALL) && !operationType.equals(OperationTypeEnum.UNINSTALL)) {
            return Result.buildParamIllegal("插件操作类型不合法(合法的操作类型包括安装和卸载)");
        }

        if (workTaskManager.existUnClosedTask(Integer.parseInt(plugin.getPhysicClusterId()), WorkTaskTypeEnum.CLUSTER_RESTART.getType())) {
            return Result.buildParamIllegal("该集群上存在未完成的集群重启任务");
        }

        // 集群存在等待执行或正在执行的插件操作任务
        if (null != ecmTaskManager.getRunningEcmTaskByClusterId(Integer.parseInt(plugin.getPhysicClusterId()))) {
            return Result.buildFail("该集群上仍存在待执行或者正在执行的插件操作任务，请完成该任务后再提交");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        PhyClusterPluginOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                PhyClusterPluginOperationContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        OperationTypeEnum operationType = OperationTypeEnum.valueOfCode(content.getOperationType());
        PluginPO pluginPO = esPluginService.getESPluginById(content.getPluginId());
        ClusterPhy cluster = esClusterPhyService.getClusterById(Integer.parseInt(pluginPO.getPhysicClusterId()));
        return cluster.getCluster() + " " + pluginPO.getName() + pluginPO.getVersion() + " "
                + workOrderTypeEnum.getMessage() + "-" + operationType.getMessage();
    }

    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        if(!isOP(workOrder.getSubmitor())){
            return Result.buildOpForBidden("非运维人员不能操作物理集群插件的安装和卸载！");
        }

        return Result.buildSucc();
    }

    @Override
    protected Result<Void> validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        PhyClusterPluginOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                PhyClusterPluginOperationContent.class);

        // 当该物理集群对应的逻辑集群对应多个物理集群时，提示用户应该在逻辑侧进行操作
        PluginPO pluginPO = esPluginService.getESPluginById(content.getPluginId());

        ClusterPhy clusterPhy = esClusterPhyService.getClusterById(Integer.parseInt(pluginPO.getPhysicClusterId()));
        List<RoleCluster> roleClusterList = roleClusterService.getAllRoleClusterByClusterId(
				clusterPhy.getId());
        if (CollectionUtils.isEmpty(roleClusterList)) {
            return Result.buildFail("物理集群角色不存在");
        }

        List<String> roleNameList = new ArrayList<>();
        for (RoleCluster roleCluster : roleClusterList) {
            roleNameList.add(roleCluster.getRole());
        }
        List<EcmParamBase> ecmParamBaseList = ecmHandleService.buildEcmParamBaseListWithEsPluginAction(clusterPhy.getId(),
                roleNameList, content.getPluginId(), content.getOperationType()).getData();

        // 生成工单任务
        EcmTaskDTO ecmTaskDTO = new EcmTaskDTO();
        ecmTaskDTO.setPhysicClusterId(Long.parseLong(pluginPO.getPhysicClusterId()));
        ecmTaskDTO.setWorkOrderId(workOrder.getId());
        ecmTaskDTO.setTitle(workOrder.getTitle());
        ecmTaskDTO.setOrderType(EcmTaskTypeEnum.RESTART.getCode());
        ecmTaskDTO.setCreator(workOrder.getSubmitor());
        ecmTaskDTO.setType(clusterPhy.getType());
        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);
        ecmTaskDTO.setClusterNodeRole(ListUtils.strList2String(roleNameList));

        WorkTaskDTO workTaskDTO = new WorkTaskDTO();
        workTaskDTO.setExpandData(JSON.toJSONString(ecmTaskDTO));
        workTaskDTO.setTaskType(WorkTaskTypeEnum.CLUSTER_RESTART.getType());
        workTaskDTO.setCreator(workOrder.getSubmitor());
        Result<WorkTask> result = workTaskService.addTask(workTaskDTO);
        if(null == result || result.failed()){
            return Result.buildFail("生成物理集群插件操作任务失败!");
        }

        return Result.buildSucc();
    }

    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        PhyClusterPluginOperationContent content = JSON.parseObject(extensions,
                PhyClusterPluginOperationContent.class);
        return ConvertUtil.obj2Obj(content, PhyClusterPluginOperationOrderDetail.class);
    }

    @Override
    public List<AriusUserInfo> getApproverList(AbstractOrderDetail detail) {
        return getOPList();
    }

    @Override
    public Result<Void> checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isOP(userName)) {
            return Result.buildSucc();
        }
        return Result.buildFail( ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }
}
