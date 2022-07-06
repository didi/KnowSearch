package com.didichuxing.datachannel.arius.admin.biz.worktask.handler.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_HOST;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.worktask.content.ClusterUpdateContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;

/**
 * 集群升级任务处理
 *
 * @author ohushenglin_v
 * @date 2022-05-24
 */
@Service("clusterUpgradeTaskHandler")
public class ClusterUpgradeTaskHandler extends AbstractClusterTaskHandler {
    @Override
    Result<Void> initHostParam(OpTask opTask) {
        ClusterUpdateContent clusterOpIndecreaseHostContent = ConvertUtil.str2ObjByJson(opTask.getExpandData(),
            ClusterUpdateContent.class);
        clusterOpIndecreaseHostContent.setType(ES_HOST.getCode());
        opTask.setExpandData(JSON.toJSONString(clusterOpIndecreaseHostContent));

        return Result.buildSucc();
    }

    @Override
    Result<Void> validateHostParam(String param) throws NotFindSubclassException {
        ClusterUpdateContent content = ConvertUtil.str2ObjByJson(param, ClusterUpdateContent.class);

        if (AriusObjUtils.isNull(content.getPhyClusterId())) {
            return Result.buildParamIllegal("物理集群id为空");
        }

        if (StringUtils.isBlank(content.getRoleOrder())) {
            return Result.buildParamIllegal("物理集群升级角色顺序为空");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterById(content.getPhyClusterId().intValue());
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildParamIllegal("物理集群不存在");
        }

        if (opTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(),
            OpTaskTypeEnum.CLUSTER_UPGRADE.getType())) {
            return Result.buildParamIllegal("该集群上存在未完成的任务");
        }
        Result<List<EcmParamBase>> ecmParamBaseResult = ecmHandleService.buildEcmParamBaseList(
            content.getPhyClusterId().intValue(), ConvertUtil.str2ObjArrayByJson(content.getRoleOrder(), String.class));
        if (ecmParamBaseResult.failed()) {
            return Result.buildFail(ecmParamBaseResult.getMessage());
        }
        return Result.buildSucc();
    }

    @Override
    Result<Void> buildHostEcmTaskDTO(EcmTaskDTO ecmTaskDTO, String param, String creator) {
        ClusterUpdateContent content = ConvertUtil.str2ObjByJson(param, ClusterUpdateContent.class);
        ecmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
        ecmTaskDTO.setOrderType(OpTaskTypeEnum.CLUSTER_UPGRADE.getType());

        Result<List<EcmParamBase>> ecmParamBaseResult = ecmHandleService.buildEcmParamBaseList(
            content.getPhyClusterId().intValue(), ConvertUtil.str2ObjArrayByJson(content.getRoleOrder(), String.class));
        List<EcmParamBase> ecmParamBaseList = ecmParamBaseResult.getData();
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            // 补充version信息
            ESPackage esPackage = esPackageService.getByVersionAndType(content.getEsVersion(), ecmParamBase.getType());

            if (ecmParamBase.getType().equals(ES_HOST.getCode())) {
                ((HostParamBase) ecmParamBase).setEsVersion(content.getEsVersion());
                if (!AriusObjUtils.isNull(esPackage) && !AriusObjUtils.isBlack(esPackage.getUrl())) {
                    ((HostParamBase) ecmParamBase).setImageName(esPackage.getUrl());
                }
            } else {
                ((ElasticCloudCommonActionParam) ecmParamBase).setEsVersion(content.getEsVersion());
                if (!AriusObjUtils.isNull(esPackage) && !AriusObjUtils.isBlack(esPackage.getUrl())) {
                    ((ElasticCloudCommonActionParam) ecmParamBase).setImageName(esPackage.getUrl());
                }
            }
        }

        ecmTaskDTO.setType(content.getType());
        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);
        return Result.buildSucc();
    }
}
