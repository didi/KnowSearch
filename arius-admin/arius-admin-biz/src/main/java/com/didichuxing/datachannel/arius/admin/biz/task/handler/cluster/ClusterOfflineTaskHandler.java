package com.didichuxing.datachannel.arius.admin.biz.task.handler.cluster;

import com.didichuxing.datachannel.arius.admin.biz.task.content.ClusterOfflineContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord.Builder;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import org.springframework.stereotype.Service;

/**
 * 物理集群下线
 * @author ohushenglin_v
 * @date 2022-05-24
 */
@Service("clusterOfflineTaskHandler")
public class ClusterOfflineTaskHandler extends AbstractClusterTaskHandler {
    @Override
    Result<Void> validateHostParam(String param) throws NotFindSubclassException {
        ClusterOfflineContent content = ConvertUtil.obj2ObjByJSON(param, ClusterOfflineContent.class);

        if (AriusObjUtils.isNull(content.getPhyClusterId())) {
            return Result.buildParamIllegal("物理集群id为空");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterById(content.getPhyClusterId().intValue());
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildParamIllegal("物理集群不存在");
        }

        if (opTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(),
            OpTaskTypeEnum.CLUSTER_OFFLINE.getType())) {
            return Result.buildParamIllegal("该集群上存在未完成的任务");
        }

        return Result.buildSucc();
    }

    @Override
    Result<Void> buildHostEcmTaskDTO(EcmTaskDTO ecmTaskDTO, String param, String creator) {
        ClusterOfflineContent content = ConvertUtil.obj2ObjByJSON(param, ClusterOfflineContent.class);
        ecmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
        ecmTaskDTO.setOrderType(OpTaskTypeEnum.CLUSTER_OFFLINE.getType());
        //下线记录操作内容
         final OperateRecord operateRecord = new Builder().userOperation(creator)
                .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                .operationTypeEnum( OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE)
                .content(String.format("下线物理集群：【%s】", content.getPhyClusterName()))
                .bizId(content.getPhyClusterId()).buildDefaultManualTrigger();
        operateRecordService.save(operateRecord);
        return Result.buildSucc();
    }
}