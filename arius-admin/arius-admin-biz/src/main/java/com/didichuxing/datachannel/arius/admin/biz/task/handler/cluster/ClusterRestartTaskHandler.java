package com.didichuxing.datachannel.arius.admin.biz.task.handler.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_HOST;

import java.util.ArrayList;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.content.ClusterRestartContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;

/**
 * 重新启动集群任务处理
 *
 * @author ohushenglin_v
 * @date 2022-05-24
 */
@Service("clusterRestartTaskHandler")
public class ClusterRestartTaskHandler extends AbstractClusterTaskHandler {
    @Override
    Result<Void> initHostParam(OpTask opTask) {
        ClusterRestartContent content = ConvertUtil.str2ObjByJson(opTask.getExpandData(),
            ClusterRestartContent.class);
        content.setType(ES_HOST.getCode());
        opTask.setExpandData(JSON.toJSONString(content));

        return Result.buildSucc();
    }

    @Override
    Result<Void> validateHostParam(String param) throws NotFindSubclassException {
        ClusterRestartContent content = ConvertUtil.str2ObjByJson(param, ClusterRestartContent.class);

        if (AriusObjUtils.isNull(content.getPhyClusterId())) {
            return Result.buildParamIllegal("物理集群id为空");
        }

        if (StringUtils.isBlank(content.getRoleOrder())) {
            return Result.buildParamIllegal("物理集群重启角色顺序为空");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterById(content.getPhyClusterId().intValue());
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildParamIllegal("物理集群不存在");
        }

        if (opTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(),
            OpTaskTypeEnum.CLUSTER_RESTART.getType())) {
            return Result.buildParamIllegal("该集群上存在未完成的集群重启任务");
        }

        return Result.buildSucc();
    }

    @Override
    Result<Void> buildHostEcmTaskDTO(EcmTaskDTO ecmTaskDTO, String param, String creator) {
        ClusterRestartContent content = ConvertUtil.str2ObjByJson(param, ClusterRestartContent.class);

        ecmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
        ecmTaskDTO.setOrderType(OpTaskTypeEnum.CLUSTER_RESTART.getType());

        ClusterPhy clusterPhy = clusterPhyService.getClusterById(content.getPhyClusterId().intValue());
        ecmTaskDTO.setType(clusterPhy.getType());

        List<String> roleNameList = new ArrayList<>();
        for (String roleClusterName : JSON.parseArray(content.getRoleOrder(), String.class)) {
            String roleName = roleClusterName.replaceFirst(clusterPhy.getCluster() + "-", "");
            roleNameList.add(roleName);
        }
        List<EcmParamBase> ecmParamBaseList = ecmHandleService.buildEcmParamBaseList(clusterPhy.getId(), roleNameList)
            .getData();

        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);

        return Result.buildSucc();
    }
}
