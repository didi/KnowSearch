package com.didichuxing.datachannel.arius.admin.biz.workorder.handler.clusterrestart;

import static com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum.DELETE;
import static com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum.EDIT;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.clusterOpRestart.ClusterOpConfigRestartContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.AriusOpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.AriusOpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.clusterOpRestart.ClusterOpConfigRestartOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.AriusOpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESClusterConfigService;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lyn
 * @date 2021-01-21
 */
@Service("clusterOpConfigRestartHandler")
public class ClusterOpConfigRestartHandler extends BaseClusterOpRestartHandler {

    @Autowired
    private ESClusterConfigService esClusterConfigService;

    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        ClusterOpConfigRestartContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterOpConfigRestartContent.class);

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

        if (ariusOpTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(), AriusOpTaskTypeEnum.CLUSTER_RESTART.getType())) {
            return Result.buildParamIllegal("该集群上存在未完成的集群重启任务");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        ClusterOpConfigRestartContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterOpConfigRestartContent.class);
        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getPhyClusterName() + EsConfigActionEnum.valueOf(content.getActionType()).getDesc();
    }

    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        ClusterOpConfigRestartContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterOpConfigRestartContent.class);

        EcmTaskDTO ecmTaskDTO = new EcmTaskDTO();
        ecmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
        ecmTaskDTO.setWorkOrderId(workOrder.getId());
        ecmTaskDTO.setTitle(workOrder.getTitle());

        ecmTaskDTO.setOrderType(EcmTaskTypeEnum.RESTART.getCode());
        ecmTaskDTO.setCreator(workOrder.getSubmitor());

        ClusterPhy clusterPhy = esClusterPhyService.getClusterById(content.getPhyClusterId().intValue());
        ecmTaskDTO.setType(clusterPhy.getType());

        List<String> roleNameList = Lists.newArrayList();
        for (String roleClusterName : JSON.parseArray(content.getRoleOrder(), String.class)) {
            String roleName = roleClusterName.replaceFirst(clusterPhy.getCluster() + "-", "");
            roleNameList.add(roleName);
        }

        Multimap<String, Long> role2ConfigIdsMultiMap = saveAndGetEsConfigIds(content, approver);
        List<EcmParamBase> ecmParamBaseList = ecmHandleService.buildEcmParamBaseListWithConfigAction(
            clusterPhy.getId(), roleNameList, role2ConfigIdsMultiMap, content.getActionType()).getData();

        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);

        AriusOpTaskDTO ariusOpTaskDTO = new AriusOpTaskDTO();
        ariusOpTaskDTO.setExpandData(JSON.toJSONString(ecmTaskDTO));
        ariusOpTaskDTO.setTaskType(AriusOpTaskTypeEnum.CLUSTER_RESTART.getType());
        ariusOpTaskDTO.setCreator(workOrder.getSubmitor());
        Result<AriusOpTask> result = ariusOpTaskManager.addTask(ariusOpTaskDTO);
        if (null == result || result.failed()) {
            return Result.buildFail("生成集群新建操作任务失败!");
        }

        return Result.buildSucc();
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        ClusterOpConfigRestartContent content = JSON.parseObject(extensions, ClusterOpConfigRestartContent.class);
        if (EDIT.getCode() == content.getActionType()) {
            buildOriginalConfigsData(content.getNewEsConfigs());
        }

        return ConvertUtil.obj2Obj(content, ClusterOpConfigRestartOrderDetail.class);
    }

    private void buildOriginalConfigsData(List<ESConfig> newEsConfigs) {
        newEsConfigs.stream().filter(Objects::nonNull).forEach(config -> {
            ESConfig originalConfig = esClusterConfigService.getEsConfigById(config.getId());
            if (AriusObjUtils.isNull(originalConfig)) {
                LOGGER.error(
                    "class=ClusterOpConfigRestartHandler||method=getOrderDetail||msg=" + "original config is empty");
                return;
            }

            config.setOriginalConfigData(originalConfig.getConfigData());
        });
    }

    /**落配置信息入DB*/
    private Multimap</*集群角色*/String, /*改动的配置id*/Long> saveAndGetEsConfigIds(ClusterOpConfigRestartContent content, String approver) {
        Multimap<String, Long> role2ConfigIdsMultiMap = ArrayListMultimap.create();
        if (ADD.getCode() == content.getActionType()) {
            List<ESConfigDTO> newEsConfigs = ConvertUtil.list2List(content.getNewEsConfigs(), ESConfigDTO.class);
            newEsConfigs.stream().filter(Objects::nonNull).forEach(config -> {
                Result<Long> result = esClusterConfigService.esClusterConfigAction(config, ADD, approver);
                if (result.failed()) {
                    LOGGER.error("class=ClusterOpConfigRestartHandler||method=saveAndGetEsConfigIds||msg="
                                 + "failed to add es config");
                }
                config.setId(result.getData());
            });

            role2ConfigIdsMultiMap = ConvertUtil.list2MulMap(newEsConfigs, ESConfigDTO::getEnginName,
                ESConfigDTO::getId);
        }

        if (EDIT.getCode() == content.getActionType()) {
            List<ESConfigDTO> editConfigs = ConvertUtil.list2List(content.getNewEsConfigs(), ESConfigDTO.class);
            editConfigs.stream().filter(Objects::nonNull).forEach(config -> {
                Result<Long> result = esClusterConfigService.esClusterConfigAction(config, EDIT, approver);
                if (result.failed()) {
                    LOGGER.error("class=ClusterOpConfigRestartHandler||method=saveAndGetEsConfigIds||msg="
                                 + "failed to edit es config");
                    return;
                }
                config.setId(result.getData());
            });

            role2ConfigIdsMultiMap = ConvertUtil.list2MulMap(editConfigs, ESConfigDTO::getEnginName,
                ESConfigDTO::getId);
        }

        if (DELETE.getCode() == content.getActionType()) {
            role2ConfigIdsMultiMap = ConvertUtil.list2MulMap(content.getOriginalConfigs(), ESConfig::getEnginName,
                ESConfig::getId);
        }

        return role2ConfigIdsMultiMap;
    }
}