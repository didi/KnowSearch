package com.didichuxing.datachannel.arius.admin.biz.workorder.handler.clusterrestart;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.clusterOpRestart.ClusterOpRestartContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.clusterOpRestart.ClusterOpRestartOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.AriusOpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author lyn
 * @date 2021-01-21
 */
@Service("clusterOpRestartHandler")
public class ClusterOpNormalRestartHandler extends BaseClusterOpRestartHandler {

    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        ClusterOpRestartContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterOpRestartContent.class);

        if (AriusObjUtils.isNull(content.getPhyClusterId())) {
            return Result.buildParamIllegal("物理集群id为空");
        }

        if (StringUtils.isBlank(content.getRoleOrder())) {
            return Result.buildParamIllegal("物理集群重启角色顺序为空");
        }

        ClusterPhy clusterPhy = esClusterPhyService.getClusterById(content.getPhyClusterId().intValue());
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildParamIllegal("物理集群不存在");
        }

        if (opTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(), AriusOpTaskTypeEnum.CLUSTER_RESTART.getType())) {
            return Result.buildParamIllegal("该集群上存在未完成的集群重启任务");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        ClusterOpRestartContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterOpRestartContent.class);
        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getPhyClusterName() + workOrderTypeEnum.getMessage();
    }

    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        ClusterOpRestartContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterOpRestartContent.class);

        EcmTaskDTO ecmTaskDTO = new EcmTaskDTO();
        ecmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
        ecmTaskDTO.setWorkOrderId(workOrder.getId());
        ecmTaskDTO.setTitle(workOrder.getTitle());

        ecmTaskDTO.setOrderType(EcmTaskTypeEnum.RESTART.getCode());
        ecmTaskDTO.setCreator(workOrder.getSubmitor());

        ClusterPhy clusterPhy = esClusterPhyService.getClusterById(content.getPhyClusterId().intValue());
        ecmTaskDTO.setType(clusterPhy.getType());

        List<String> roleNameList = new ArrayList<>();
        for (String roleClusterName : JSON.parseArray(content.getRoleOrder(), String.class)) {
            String roleName = roleClusterName.replaceFirst(clusterPhy.getCluster() + "-", "");
            roleNameList.add(roleName);
        }
        List<EcmParamBase> ecmParamBaseList = ecmHandleService.buildEcmParamBaseList(clusterPhy.getId(), roleNameList)
            .getData();

        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);

        OpTaskDTO opTaskDTO = new OpTaskDTO();
        opTaskDTO.setTaskType(AriusOpTaskTypeEnum.CLUSTER_RESTART.getType());
        opTaskDTO.setExpandData(JSON.toJSONString(ecmTaskDTO));
        opTaskDTO.setCreator(workOrder.getSubmitor());
        Result<OpTask> result = opTaskManager.addTask(opTaskDTO);
        if (null == result || result.failed()) {
            return Result.buildFail("生成集群新建操作任务失败!");
        }

        return Result.buildSucc();
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        ClusterOpRestartContent content = JSON.parseObject(extensions, ClusterOpRestartContent.class);

        return ConvertUtil.obj2Obj(content, ClusterOpRestartOrderDetail.class);
    }
}