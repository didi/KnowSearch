package com.didichuxing.datachannel.arius.admin.biz.worktask.handler.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_DOCKER;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_HOST;

import java.util.List;
import java.util.Objects;

import com.didichuxing.datachannel.arius.admin.biz.worktask.content.ClusterBaseContent;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.worktask.content.ClusterConfigRestartContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * @author ohushenglin_v
 * @date 2022-05-24
 */
@Service("clusterConfigRestartTaskHandler")
public class ClusterConfigRestartTaskHandler extends AbstractClusterTaskHandler {

    @Override
    Result<Void> validateHostParam(String param) {
        ClusterConfigRestartContent content = ConvertUtil.str2ObjByJson(param, ClusterConfigRestartContent.class);

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

        if (AriusObjUtils.isNull(content.getActionType())) {
            return Result.buildParamIllegal("配置操作不存在");
        }

        if (ADD.getCode() == content.getActionType() && CollectionUtils.isEmpty(content.getNewEsConfigs())) {
            return Result.buildParamIllegal("新增配置为空");
        }

        if (EsConfigActionEnum.EDIT.getCode() == content.getActionType()
            && CollectionUtils.isEmpty(content.getOriginalConfigs())) {
            return Result.buildParamIllegal("原始配置为空");
        }

        if (opTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(),
            OpTaskTypeEnum.CLUSTER_RESTART.getType())) {
            return Result.buildParamIllegal("该集群上存在未完成的集群重启任务");
        }

        return Result.buildSucc();
    }
    @Override
    Result<OpTask> buildOpTask(OpTask opTask) {
        ClusterBaseContent content = ConvertUtil.str2ObjByJson(opTask.getExpandData(), ClusterBaseContent.class);
        EcmTaskDTO ecmTaskDTO = new EcmTaskDTO();
        ecmTaskDTO
                .setTitle(content.getPhyClusterName() + OpTaskTypeEnum.valueOfType(opTask.getTaskType()).getMessage());
        ecmTaskDTO.setCreator(opTask.getCreator());
        ecmTaskDTO.setType(content.getType());
        ecmTaskDTO.setPhysicClusterId(ClusterConstant.INVALID_VALUE);

        Result<Void> result = CLUSTER_TYPE_NOT_SUPPORT;
        if (ES_DOCKER.getCode() == content.getType()) {
            result = buildDockerEcmTaskDTO(ecmTaskDTO, opTask.getExpandData(), opTask.getCreator());
        } else if (ES_HOST.getCode() == content.getType()) {
            result = buildHostEcmTaskDTO(ecmTaskDTO, opTask.getExpandData(), opTask.getCreator());
        }
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        opTask.setExpandData(JSON.toJSONString(ecmTaskDTO));
        //这里由于历史原因，需要将配置变更的taskType设置为 CLUSTER_RESTART
        opTask.setTaskType(OpTaskTypeEnum.CLUSTER_RESTART.getType());
        return Result.buildSucc(opTask);
    }

    @Override
    Result<Void> buildHostEcmTaskDTO(EcmTaskDTO ecmTaskDTO, String param, String creator) {
        ClusterConfigRestartContent content = ConvertUtil.str2ObjByJson(param,
            ClusterConfigRestartContent.class);

        ecmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
        ecmTaskDTO.setOrderType(OpTaskTypeEnum.CLUSTER_RESTART.getType());

        ClusterPhy clusterPhy = clusterPhyService.getClusterById(content.getPhyClusterId().intValue());
        ecmTaskDTO.setType(clusterPhy.getType());

        List<String> roleNameList = Lists.newArrayList();
        for (String roleClusterName : JSON.parseArray(content.getRoleOrder(), String.class)) {
            String roleName = roleClusterName.replaceFirst(clusterPhy.getCluster() + "-", "");
            roleNameList.add(roleName);
        }

        Multimap<String, Long> role2ConfigIdsMultiMap = saveEsConfigs(content, creator);
        Result<List<EcmParamBase>> buildEcmParamBasesResult = ecmHandleService.buildEcmParamBaseListWithConfigAction(
            clusterPhy.getId(), roleNameList, role2ConfigIdsMultiMap, content.getActionType());

        if (buildEcmParamBasesResult.failed()) {
            return Result.buildFail(buildEcmParamBasesResult.getMessage());
        }
        ecmTaskDTO.setEcmParamBaseList(buildEcmParamBasesResult.getData());
        return Result.buildSucc();
    }

    /**
     * 保存并得到es配置id
     *
     * @param content  内容
     * @param approver 审批人
     * @return {@link Multimap}<{@link String}, {@link Long}>  Multimap<集群角色, 改动的配置id>
     */
    private Multimap<String, Long> saveEsConfigs(ClusterConfigRestartContent content, String approver) {
        Multimap<String, Long> role2ConfigIdsMultiMap = ArrayListMultimap.create();
        if (ADD.getCode() == content.getActionType()) {
            List<ESConfigDTO> newEsConfigs = ConvertUtil.list2List(content.getNewEsConfigs(), ESConfigDTO.class);
            newEsConfigs.stream().filter(Objects::nonNull).forEach(config -> {
                Result<Long> result = esClusterConfigService.esClusterConfigAction(config, ADD, approver);
                if (result.failed()) {
                    LOGGER.error("class=ClusterConfigAddRestartTaskHandler||method=saveAndGetEsConfigIds||msg="
                                 + "failed to add es config");
                }
                config.setId(result.getData());
            });

            role2ConfigIdsMultiMap = ConvertUtil.list2MulMap(newEsConfigs, ESConfigDTO::getEnginName,
                ESConfigDTO::getId);
        } else if (EDIT.getCode() == content.getActionType()) {
            List<ESConfigDTO> editConfigs = ConvertUtil.list2List(content.getNewEsConfigs(), ESConfigDTO.class);
            editConfigs.stream().filter(Objects::nonNull).forEach(config -> {
                Result<Long> result = esClusterConfigService.esClusterConfigAction(config, EDIT, approver);
                if (result.failed()) {
                    LOGGER.error("class=ClusterConfigAddRestartTaskHandler||method=saveAndGetEsConfigIds||msg="
                                 + "failed to edit es config");
                    return;
                }
                config.setId(result.getData());
            });

            role2ConfigIdsMultiMap = ConvertUtil.list2MulMap(editConfigs, ESConfigDTO::getEnginName,
                ESConfigDTO::getId);
        } else if (DELETE.getCode() == content.getActionType()) {
            role2ConfigIdsMultiMap = ConvertUtil.list2MulMap(content.getOriginalConfigs(), ESConfig::getEnginName,
                ESConfig::getId);
        }

        return role2ConfigIdsMultiMap;
    }
}
