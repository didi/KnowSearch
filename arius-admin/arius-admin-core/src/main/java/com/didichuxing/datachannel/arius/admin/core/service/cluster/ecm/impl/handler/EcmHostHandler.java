package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.INVALID_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EsConfigAction;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostCreateActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostScaleActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.remote.zeus.ZeusClusterRemoteService;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.EcmActionEnum;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.ZeusClusterActionEnum;

@Service("ecmHostHandler")
public class EcmHostHandler extends AbstractEcmBaseHandle {
    @Autowired
    private ClusterRoleService clusterRoleService;

    @Autowired
    private ESPluginService          esPluginService;

    @Autowired
    private ESPackageService         esPackageService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private ClusterPhyService esClusterPhyService;

    @Autowired
    private ZeusClusterRemoteService zeusClusterRemoteService;

    @PostConstruct
    public void init() {
        esClusterTypeEnum = ESClusterTypeEnum.ES_HOST;
    }

    @Override
    public Result<Long> saveESCluster(List<EcmParamBase> ecmParamBaseList) {
        List<HostCreateActionParam> hostCreateActionParamList = new ArrayList<>();
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            HostCreateActionParam hostCreateActionParam = (HostCreateActionParam) ecmParamBase;
            // 检查参数是否合理
            Result<Void> fieldCheckResult = hostCreateActionParam.validateFiledIllegal();
            if (fieldCheckResult.failed()) {
                return Result.buildFrom(fieldCheckResult);
            }
            hostCreateActionParamList.add(hostCreateActionParam);
        }

        //保存物理集群信息 es_cluster_phy_info
        Result<HostCreateActionParam> hostCreateActionParamResult = persistClusterPOAndSupplyField(
            hostCreateActionParamList.get(0));
        if (hostCreateActionParamResult.failed()) {
            return Result.buildFrom(hostCreateActionParamResult);
        }

        for (HostCreateActionParam hostCreateActionParam : hostCreateActionParamList) {
            hostCreateActionParam.setPhyClusterId(hostCreateActionParamResult.getData().getPhyClusterId());
            hostCreateActionParam.setPlugs(hostCreateActionParamResult.getData().getPlugs());
            hostCreateActionParam.setImageName(hostCreateActionParamResult.getData().getImageName());
        }

        // 保存集群角色信息 es_cluster_role_info
        for (HostCreateActionParam hostCreateActionParam : hostCreateActionParamList) {
            // 角色集群 信息入库
            ESClusterRoleDTO esClusterRoleDTO = new ESClusterRoleDTO();
            esClusterRoleDTO.setElasticClusterId(hostCreateActionParam.getPhyClusterId());
            if (hostCreateActionParam.getRoleName().startsWith(hostCreateActionParam.getPhyClusterName())) {
                esClusterRoleDTO.setRoleClusterName(hostCreateActionParam.getRoleName());
            } else {
                esClusterRoleDTO.setRoleClusterName(
                    hostCreateActionParam.getPhyClusterName() + "-" + hostCreateActionParam.getRoleName());
            }
            esClusterRoleDTO.setRole(hostCreateActionParam.getRoleName());
            esClusterRoleDTO.setPodNumber(0);
            esClusterRoleDTO.setPidCount(hostCreateActionParam.getPidCount());
            esClusterRoleDTO.setMachineSpec(hostCreateActionParam.getMachineSpec());
            esClusterRoleDTO.setCfgId(INVALID_VALUE.intValue());
            esClusterRoleDTO.setEsVersion(hostCreateActionParam.getEsVersion());

            clusterRoleService.save(esClusterRoleDTO);
        }
        return Result.buildSucc(hostCreateActionParamResult.getData().getPhyClusterId());
    }

    /**
     * 集群基础数据入库：集群  角色 角色对应主机
     * @param  param
     * @return esClusterDTO
     */
    private Result<HostCreateActionParam> persistClusterPOAndSupplyField(HostCreateActionParam param) {
        ClusterPhyDTO esClusterDTO = ConvertUtil.obj2Obj(param, ClusterPhyDTO.class);
        esClusterDTO.setCluster(param.getPhyClusterName());
        ESPackage esPackage = esPackageService.getByVersionAndType(param.getEsVersion(), param.getType());
        esClusterDTO.setImageName(esPackage.getUrl());

        esClusterDTO.setPlugIds(buildEsClusterPlugins(esClusterDTO));
        esClusterDTO.setPackageId(esPackage.getId());
        esClusterDTO.setTemplateSrvs(ClusterConstant.DEFAULT_CLUSTER_TEMPLATE_SRVS);
        esClusterDTO.setHttpAddress(ClusterConstant.DEFAULT_HTTP_ADDRESS);
        if (StringUtils.isBlank(esClusterDTO.getDataCenter())) {
            esClusterDTO.setDataCenter(EnvUtil.getDC().getCode());
        }
        if (StringUtils.isBlank(esClusterDTO.getIdc())) {
            esClusterDTO.setIdc(ClusterConstant.DEFAULT_CLUSTER_IDC);
        }
        esClusterDTO.setRunMode(ClusterConstant.DEFAULT_RUN_MODEL);

        Result<Boolean> clusterResult = esClusterPhyService.createCluster(esClusterDTO, param.getCreator());
        if (clusterResult.failed()) {
            return Result.buildFrom(clusterResult);
        }
        param.setPhyClusterId(esClusterDTO.getId().longValue());
        param.setImageName(esPackage.getUrl());
        param.setPlugs(esClusterDTO.getPlugIds());
        return Result.buildSucc(param);
    }

    @Override
    public Result<EcmOperateAppBase> startESCluster(EcmParamBase ecmParamBase) {
        HostCreateActionParam hostCreateActionParam = (HostCreateActionParam) ecmParamBase;
        return zeusClusterRemoteService.createTask(hostCreateActionParam.getHostList(),
                buildArgs(hostCreateActionParam,
                        ZeusClusterActionEnum.NEW.getValue(),
                        hostCreateActionParam.getHostList(),
                        getInitialRackValue(ecmParamBase, ZeusClusterActionEnum.NEW)));
    }

    @Override
    public Result scaleESCluster(EcmParamBase actionParamBase) {
        HostScaleActionParam hostScaleActionParam = (HostScaleActionParam) actionParamBase;
        return zeusClusterRemoteService.createTask(hostScaleActionParam.getHostList(),
                buildArgs(hostScaleActionParam,
                        hostScaleActionParam.getAction(),
                        hostScaleActionParam.getHostList(),
                        getInitialRackValue(actionParamBase, ZeusClusterActionEnum.valueFrom(hostScaleActionParam.getAction()))));
    }

    @Override
    public Result upgradeESCluster(EcmParamBase actionParamBase) {
        HostParamBase hostUpgradeActionParam = (HostParamBase) actionParamBase;
        return zeusClusterRemoteService.createTask(hostUpgradeActionParam.getHostList(), buildArgs(
            hostUpgradeActionParam.getMasterHostList(), hostUpgradeActionParam, ZeusClusterActionEnum.UPDATE.getValue(), null));
    }

    @Override
    public Result<EcmOperateAppBase> restartESCluster(EcmParamBase actionParamBase) {
        HostParamBase hostParamBase = (HostParamBase) actionParamBase;
        return zeusClusterRemoteService.createTask(hostParamBase.getHostList(),
            buildArgs(hostParamBase.getMasterHostList(), hostParamBase, ZeusClusterActionEnum.RESTART.getValue(), getConfigActionType(hostParamBase.getEsConfigAction())));
    }

    @Override
    public Result<EcmOperateAppBase> removeESCluster(EcmParamBase actionParamBase) {
        HostScaleActionParam hostScaleActionParam = (HostScaleActionParam) actionParamBase;
        return zeusClusterRemoteService.createTask(hostScaleActionParam.getHostList(), buildArgs(
            hostScaleActionParam.getMasterHostList(), hostScaleActionParam, ZeusClusterActionEnum.SHRINK.getValue(), null));
    }

    @Override
    public Result actionNotFinishedTask(EcmParamBase actionParamBase, EcmActionEnum ecmActionEnum, String hostname) {
        if (AriusObjUtils.isBlank(hostname)) {
            return zeusClusterRemoteService.actionTask(actionParamBase.getTaskId(), ecmActionEnum.getAction());
        }
        return zeusClusterRemoteService.actionHostTask(actionParamBase.getTaskId(), hostname,
            ecmActionEnum.getAction());
    }

    @Override
    public Result<String> infoESCluster(EcmParamBase actionParamBase) {
        return Result.buildSuccWithMsg("");
    }

    @Override
    public Result<EcmSubTaskLog> getSubTaskLog(Long taskId, String hostname, EcmParamBase actionParamBase) {
        return zeusClusterRemoteService.getTaskLog(taskId.intValue(), hostname);
    }

    @Override
    public Result<List<EcmTaskStatus>> getTaskStatus(EcmParamBase ecmParamBase, Integer orderType) {
        return zeusClusterRemoteService.getZeusTaskStatus(ecmParamBase.getTaskId());
    }

    /*********************** private method ***********************************/

    private String buildArgs(List<String> masterHostList, HostParamBase hostActionParam, String action, Integer configActionType) {
        return String.format(
                "--cluster_name=%s,,--port=%s,,--masters=%s,,--package_url=%s,,--es_version=%s,,--role=%s,,--pid_count=%d,,--action=%s,,--config_action=%s",
                hostActionParam.getPhyClusterName(), hostActionParam.getPort(),
                ListUtils.strList2String(masterHostList), hostActionParam.getImageName(),
                hostActionParam.getEsVersion(), hostActionParam.getRoleName(),
                hostActionParam.getPidCount(), action, configActionType);
    }

    private String buildArgs(HostParamBase hostParamBase, String action, List<String> hostLists, String initialRack) {
        return String.format(
                "--cluster_name=%s,,--port=%s,,--masters=%s,,--package_url=%s,,--es_version=%s,,--role=%s,,--pid_count=%d,,--action=%s,,--hosts=%s,,--initial_rack=%s",
                hostParamBase.getPhyClusterName(), hostParamBase.getPort(),
                ListUtils.strList2String(hostParamBase.getMasterHostList()), hostParamBase.getImageName(),
                hostParamBase.getEsVersion(), hostParamBase.getRoleName(),
                hostParamBase.getPidCount(), action, ListUtils.strList2String(hostLists), initialRack);
    }

    private String buildEsClusterPlugins(ClusterPhyDTO esClusterDTO) {
        String defaultPlugins = StringUtils.defaultString(esPluginService.getAllSysDefaultPluginIds(), "");

        if (StringUtils.isNotEmpty(esClusterDTO.getPlugIds())) {
            defaultPlugins = defaultPlugins + "," + esClusterDTO.getPlugIds();
        }
        return defaultPlugins;
    }

    private Integer getConfigActionType(EsConfigAction esConfigAction) {
        if(null == esConfigAction || esConfigAction.getActionType().equals(EsConfigActionEnum.UNKNOWN.getCode())) {
            return null;
        }
        return esConfigAction.getActionType();
    }

    private String getInitialRackValue(EcmParamBase actionParamBase, ZeusClusterActionEnum zeusClusterActionEnum) {
        HostParamBase hostParamBase = (HostParamBase) actionParamBase;
        if (!actionParamBase.getRoleName().equals(ESClusterNodeRoleEnum.DATA_NODE.getDesc())
                || CollectionUtils.isEmpty(hostParamBase.getHostList())) {
            return null;
        }

        String initialRack = null;
        if (zeusClusterActionEnum.equals(ZeusClusterActionEnum.EXPAND)
                || zeusClusterActionEnum.equals(ZeusClusterActionEnum.NEW)) {
            // 根据物理集群id和数据节点的角色名称获取所有的当前集群绑定的主机列表
            List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.getByRoleAndClusterId(hostParamBase.getPhyClusterId(),
                    ESClusterNodeRoleEnum.DATA_NODE.getDesc());

            float initialRackValue = 0;
            if (CollectionUtils.isEmpty(clusterRoleHosts)) {
                // 当新建或者扩容前data角色列表为空时，设置基准值
                initialRackValue += 0.5;
            } else {
                // 根据rack的大小进行排序并且获取新的data节点类型的rack起始值
                for (ClusterRoleHost clusterRoleHost : clusterRoleHosts) {
                    if (!Pattern.matches("r.*", clusterRoleHost.getRack())) {
                        // 如果rack 不是r* 形式，则跳过不进行计算, 这里为了与前人逻辑保持一致，hold住特殊case 如cold rack
                        continue;
                    }
                    int roleHostRackValue = Integer.parseInt(clusterRoleHost.getRack().replaceFirst("r", ""));
                    if (roleHostRackValue > initialRackValue) {
                        initialRackValue = roleHostRackValue;
                    } else if (roleHostRackValue == initialRackValue) {
                        initialRackValue += 0.5;
                    }
                }
            }

            initialRack = String.valueOf(initialRackValue);
        }
        return initialRack;
    }
}
