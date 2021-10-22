package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler;

import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterConstant.INVALID_VALUE;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostCreateActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostScaleActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ESClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.EcmActionEnum;
import com.didichuxing.datachannel.arius.admin.remote.zeus.ZeusClusterRemoteService;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.ZeusClusterActionEnum;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ecmHostHandler")
public class EcmHostHandler extends AbstractEcmBaseHandle {
    @Autowired
    private ESRoleClusterService     roleClusterService;

    @Autowired
    private ESPluginService          esPluginService;

    @Autowired
    private ESPackageService         esPackageService;

    @Autowired
    private ESClusterPhyService      esClusterPhyService;

    @Autowired
    private ZeusClusterRemoteService zeusClusterRemoteService;

    @PostConstruct
    public void init() {
        esClusterTypeEnum = ESClusterTypeEnum.ES_HOST;
    }

    @Override
    public Result saveESCluster(List<EcmParamBase> ecmParamBaseList) {
        List<HostCreateActionParam> hostCreateActionParamList = new ArrayList<>();
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            HostCreateActionParam hostCreateActionParam = (HostCreateActionParam) ecmParamBase;
            // 检查参数是否合理
            Result fieldCheckResult = hostCreateActionParam.validateFiledIllegal();
            if (fieldCheckResult.failed()) {
                return fieldCheckResult;
            }
            hostCreateActionParamList.add(hostCreateActionParam);
        }

        //保存物理集群信息 es_data_source
        Result<HostCreateActionParam> hostCreateActionParamResult = persistClusterPOAndSupplyField(
            hostCreateActionParamList.get(0));
        if (hostCreateActionParamResult.failed()) {
            return Result.buildFail(hostCreateActionParamResult.getMessage());
        }

        for (HostCreateActionParam hostCreateActionParam : hostCreateActionParamList) {
            hostCreateActionParam.setPhyClusterId(hostCreateActionParamResult.getData().getPhyClusterId());
            hostCreateActionParam.setPlugs(hostCreateActionParamResult.getData().getPlugs());
            hostCreateActionParam.setImageName(hostCreateActionParamResult.getData().getImageName());
        }

        // 保存集群角色信息 es_role_cluster
        for (HostCreateActionParam hostCreateActionParam : hostCreateActionParamList) {
            // 角色集群 信息入库
            ESRoleClusterDTO esRoleClusterDTO = new ESRoleClusterDTO();
            esRoleClusterDTO.setElasticClusterId(hostCreateActionParam.getPhyClusterId());
            if (hostCreateActionParam.getRoleName().startsWith(hostCreateActionParam.getPhyClusterName())) {
                esRoleClusterDTO.setRoleClusterName(hostCreateActionParam.getRoleName());
            } else {
                esRoleClusterDTO.setRoleClusterName(
                    hostCreateActionParam.getPhyClusterName() + "-" + hostCreateActionParam.getRoleName());
            }
            esRoleClusterDTO.setRole(hostCreateActionParam.getRoleName());
            esRoleClusterDTO.setPodNumber(hostCreateActionParam.getNodeNumber());
            esRoleClusterDTO.setPidCount(hostCreateActionParam.getPidCount());
            esRoleClusterDTO.setMachineSpec(hostCreateActionParam.getMachineSpec());
            esRoleClusterDTO.setCfgId(INVALID_VALUE.intValue());
            esRoleClusterDTO.setEsVersion(hostCreateActionParam.getEsVersion());

            roleClusterService.save(esRoleClusterDTO);
        }
        return Result.buildSucc(hostCreateActionParamResult.getData().getPhyClusterId());
    }

    /**
     * 集群基础数据入库：集群  角色 角色对应主机
     * @param  param
     * @return esClusterDTO
     */
    private Result<HostCreateActionParam> persistClusterPOAndSupplyField(HostCreateActionParam param) {
        ESClusterDTO esClusterDTO = ConvertUtil.obj2Obj(param, ESClusterDTO.class);
        esClusterDTO.setCluster(param.getPhyClusterName());
        ESPackage esPackage = esPackageService.getByVersionAndType(param.getEsVersion(), param.getType());
        esClusterDTO.setImageName(esPackage.getUrl());

        esClusterDTO.setPlugIds(buildEsClusterPlugins(esClusterDTO));
        esClusterDTO.setPackageId(esPackage.getId());
        esClusterDTO.setTemplateSrvs(ESClusterConstant.DEFAULT_CLUSTER_TEMPLATE_SRVS);
        esClusterDTO.setHttpAddress(ESClusterConstant.DEFAULT_HTTP_ADDRESS);

        Result clusterResult = esClusterPhyService.createCluster(esClusterDTO, param.getCreator());
        if (clusterResult.failed()) {
            return Result.buildFail(clusterResult.getMessage());
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
            buildArgs(hostCreateActionParam, ZeusClusterActionEnum.NEW.getValue()));
    }

    @Override
    public Result scaleESCluster(EcmParamBase actionParamBase) {
        HostScaleActionParam hostScaleActionParam = (HostScaleActionParam) actionParamBase;
        return zeusClusterRemoteService.createTask(hostScaleActionParam.getHostList(),
            buildArgs(hostScaleActionParam.getHostList(), hostScaleActionParam, hostScaleActionParam.getAction()));
    }

    @Override
    public Result upgradeESCluster(EcmParamBase actionParamBase) {
        HostParamBase hostUpgradeActionParam = (HostParamBase) actionParamBase;
        return zeusClusterRemoteService.createTask(hostUpgradeActionParam.getHostList(), buildArgs(
            hostUpgradeActionParam.getHostList(), hostUpgradeActionParam, ZeusClusterActionEnum.UPDATE.getValue()));
    }

    @Override
    public Result restartESCluster(EcmParamBase actionParamBase) {
        HostParamBase hostParamBase = (HostParamBase) actionParamBase;
        return zeusClusterRemoteService.createTask(hostParamBase.getHostList(),
            buildArgs(hostParamBase.getHostList(), hostParamBase, ZeusClusterActionEnum.RESTART.getValue()));
    }

    @Override
    public Result removeESCluster(EcmParamBase actionParamBase) {
        HostScaleActionParam hostScaleActionParam = (HostScaleActionParam) actionParamBase;
        return zeusClusterRemoteService.createTask(hostScaleActionParam.getHostList(), buildArgs(
            hostScaleActionParam.getHostList(), hostScaleActionParam, ZeusClusterActionEnum.SHRINK.getValue()));
    }

    @Override
    public Result actionNotFinishedTask(EcmParamBase actionParamBase, EcmActionEnum ecmActionEnum, String hostname) {
        if (ValidateUtils.isBlank(hostname)) {
            return zeusClusterRemoteService.actionTask(actionParamBase.getTaskId(), ecmActionEnum.getAction());
        }
        return zeusClusterRemoteService.actionHostTask(actionParamBase.getTaskId(), hostname,
            ecmActionEnum.getAction());
    }

    @Override
    public Result infoESCluster(EcmParamBase actionParamBase) {
        return Result.buildSucc();
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

    private String buildArgs(List<String> masterHostList, HostParamBase hostActionParam, String action) {
        return String.format(
            "--cluster_name=%s,,--masters=%s,,--package_url=%s,,--es_version=%s,,--role=%s,,--pid_count=%d,,--action=%s",
            hostActionParam.getPhyClusterName(), ListUtils.strList2String(masterHostList),
            hostActionParam.getImageName(), hostActionParam.getEsVersion(), hostActionParam.getRoleName(),
            hostActionParam.getPidCount(), action);
    }

    private String buildArgs(HostCreateActionParam hostCreateActionParam, String action) {
        return String.format(
            "--cluster_name=%s,,--masters=%s,,--package_url=%s,,--es_version=%s,,--role=%s,,--pid_count=%d,,--action=%s",
            hostCreateActionParam.getPhyClusterName(),
            ListUtils.strList2String(hostCreateActionParam.getMasterHostList()), hostCreateActionParam.getImageName(),
            hostCreateActionParam.getEsVersion(), hostCreateActionParam.getRoleName(),
            hostCreateActionParam.getPidCount(), action);
    }

    private String buildEsClusterPlugins(ESClusterDTO esClusterDTO) {
        String defaultPlugins = StringUtils.defaultString(esPluginService.getAllSysDefaultPlugins(), "");

        if (StringUtils.isNotEmpty(esClusterDTO.getPlugIds())) {
            defaultPlugins = defaultPlugins + "," + esClusterDTO.getPlugIds();
        }
        return defaultPlugins;
    }
}
