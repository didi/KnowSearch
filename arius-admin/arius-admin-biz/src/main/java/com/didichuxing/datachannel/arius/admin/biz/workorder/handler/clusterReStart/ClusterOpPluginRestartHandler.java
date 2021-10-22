package com.didichuxing.datachannel.arius.admin.biz.workorder.handler.clusterReStart;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.PhyClusterPluginOperationOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.order.OperationTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.biz.workorder.notify.PhyClusterPluginNotify;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.PhyClusterPluginOperationContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.WORK_ORDER_PHY_CLUSTER_PLUGIN;

@Service("clusterOpPluginRestartHandler")
public class ClusterOpPluginRestartHandler extends ClusterOpRestartHandler {

    @Autowired
    private ESClusterPhyService esClusterPhyService;

    @Autowired
    private ESPluginService esPluginService;

    @Autowired
    private ESRoleClusterService esRoleClusterService;

    @Autowired
    private EcmHandleService ecmHandleService;

    @Autowired
    private WorkTaskManager workTaskService;

    @Autowired
    private ESRegionRackService esRegionRackService;

    @Override
    protected Result validateConsoleParam(WorkOrder workOrder) {
        PhyClusterPluginOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                PhyClusterPluginOperationContent.class);

        // 校验插件id
        if (AriusObjUtils.isNull(content.getPluginId())) {
            return Result.buildParamIllegal("插件id为空！");
        }
        ESPluginPO plugin = esPluginService.getESPluginById(content.getPluginId());
        if (AriusObjUtils.isNull(plugin)) {
            return Result.buildParamIllegal("插件不存在！");
        }

        // 校验插件文件名
        // todo name --> url
        if (!content.getPluginFileName().equals(plugin.getUrl())) {
            return Result.buildFail("插件文件名错误！");
        }

        // 校验插件S3地址
        // todo s3url --> url
        if (!content.getS3url().equals(plugin.getS3url())) {
            return Result.buildFail("插件S3地址错误！");
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
        ESPluginPO esPluginPO = esPluginService.getESPluginById(content.getPluginId());
        ESClusterPhy cluster = esClusterPhyService.getClusterById(Integer.parseInt(esPluginPO.getPhysicClusterId()));
        return cluster.getCluster() + " " + esPluginPO.getName() + esPluginPO.getVersion() + " "
                + workOrderTypeEnum.getMessage() + "-" + operationType.getMessage();
    }

    @Override
    protected Result validateConsoleAuth(WorkOrder workOrder) {
        if(!isOP(workOrder.getSubmitor())){
            return Result.buildOpForBidden("非运维人员不能操作物理集群插件的安装和卸载！");
        }

        return Result.buildSucc();
    }

    @Override
    protected Result validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        PhyClusterPluginOperationContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                PhyClusterPluginOperationContent.class);

        // 当该物理集群对应的逻辑集群对应多个物理集群时，提示用户应该在逻辑侧进行操作
        //TODO: 考虑一个物理集群对应多个逻辑集群的情况
        ESPluginPO esPluginPO = esPluginService.getESPluginById(content.getPluginId());
        Long logicClusterId = esRegionRackService.getLogicClusterIdByPhyClusterId(Integer.parseInt(esPluginPO.getPhysicClusterId()));
        List<Integer> physicClusterIds = esRegionRackService.listPhysicClusterId(logicClusterId);
        if (physicClusterIds.size() > 1) {
            return Result.buildFail("因该物理集群与逻辑集群非1对1映射，请至“集群列表”，在该物理集群对应的逻辑集群侧操作");
        }

        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(Integer.parseInt(esPluginPO.getPhysicClusterId()));
        List<ESRoleCluster> esRoleClusterList = esRoleClusterService.getAllRoleClusterByClusterId(esClusterPhy.getId());
        if (CollectionUtils.isEmpty(esRoleClusterList)) {
            return Result.buildFail("物理集群角色不存在");
        }

        List<String> roleNameList = new ArrayList<>();
        for (ESRoleCluster esRoleCluster : esRoleClusterList) {
            roleNameList.add(esRoleCluster.getRole());
        }
        List<EcmParamBase> ecmParamBaseList = ecmHandleService.buildEcmParamBaseList(esClusterPhy.getId(), roleNameList).getData();

        // 生成工单任务
        EcmTaskDTO ecmTaskDTO = new EcmTaskDTO();
        ecmTaskDTO.setPhysicClusterId(Long.parseLong(esPluginPO.getPhysicClusterId()));
        ecmTaskDTO.setWorkOrderId(workOrder.getId());
        ecmTaskDTO.setTitle(workOrder.getTitle());
        ecmTaskDTO.setOrderType(EcmTaskTypeEnum.PLUG_OPERATION.getCode());
        ecmTaskDTO.setCreator(workOrder.getSubmitor());
        ecmTaskDTO.setType(esClusterPhy.getType());
        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);
        ecmTaskDTO.setClusterNodeRole(ListUtils.strList2String(roleNameList));

        WorkTaskDTO workTaskDTO = new WorkTaskDTO();
        workTaskDTO.setExpandData(JSONObject.toJSONString( ecmTaskDTO ));
        workTaskDTO.setTaskType(WorkTaskTypeEnum.CLUSTER_RESTART.getType());
        workTaskDTO.setCreator(workOrder.getSubmitor());
        Result result = workTaskService.addTask(workTaskDTO);
        if(null == result || result.failed()){
            return Result.buildFail("生成物理集群插件操作任务失败!");
        }

        // 发送通知消息
        sendNotify(WORK_ORDER_PHY_CLUSTER_PLUGIN, new PhyClusterPluginNotify(workOrder.getSubmitorAppid(),
                esClusterPhy.getCluster(), approver), Arrays.asList(workOrder.getSubmitor()));

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
    public Result checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isOP(userName)) {
            return Result.buildSucc(true);
        }
        return Result.buildFail( ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }
}
