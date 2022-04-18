package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudCreateActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudScaleActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.constant.CloudClusterCreateParamConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterService;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.ElasticCloudRemoteService;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.EcmActionEnum;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request.*;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response.ElasticCloudAppStatus;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response.ElasticCloudPod;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response.ElasticCloudStatus;
import com.didichuxing.datachannel.arius.admin.remote.monitor.RemoteMonitorService;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmCloudOpreateActionEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmHostStatusEnum.RUNNING;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.CloudClusterCreateParamConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.INVALID_VALUE;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.SYN_ODIN_STATUS_MAX_RETRY_TIMES;

@NoArgsConstructor
@Service("ecmDockerHandler")
public class EcmDockerHandler extends AbstractEcmBaseHandle {
    @Autowired
    private RoleClusterService roleClusterService;

    @Autowired
    private ESPluginService           esPluginService;

    @Autowired
    private RoleClusterHostService roleClusterHostService;

    @Autowired
    private ElasticCloudRemoteService elasticCloudRemoteService;

    @Autowired
    private ClusterPhyService esClusterPhyService;

    @Autowired
    private ESPackageService          esPackageService;

    @Autowired
    private RemoteMonitorService      odinRemoteService;

    @Value("${cloud.basic.selfServer}")
    private String                    selfServer;

    @PostConstruct
    public void init() {
        esClusterTypeEnum = ESClusterTypeEnum.ES_DOCKER;
    }

    @Override
    public Result<Long> saveESCluster(List<EcmParamBase> ecmParamBaseList) {
        List<ElasticCloudCreateActionParam> elasticCloudCreateActionParamList = new ArrayList<>();

        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            ElasticCloudCreateActionParam elasticCloudCreateActionParam = (ElasticCloudCreateActionParam) ecmParamBase;
            // 检查参数是否合理
            Result<Void> fieldCheckResult = elasticCloudCreateActionParam.validateFiledIllegal();
            if (fieldCheckResult.failed()) {
                return Result.buildFrom(fieldCheckResult);
            }
            elasticCloudCreateActionParamList.add(elasticCloudCreateActionParam);
        }

        //保存物理集群信息 es_data_source
        Result<ElasticCloudCreateActionParam> elasticCloudCreateActionParamResult = persistClusterPOAndSupplyField(
            elasticCloudCreateActionParamList.get(0));
        if (elasticCloudCreateActionParamResult.failed()) {
            return Result.buildFrom(elasticCloudCreateActionParamResult);
        }
        for (ElasticCloudCreateActionParam elasticCloudCreateAction : elasticCloudCreateActionParamList) {
            elasticCloudCreateAction.setPhyClusterId(elasticCloudCreateActionParamResult.getData().getPhyClusterId());
            elasticCloudCreateAction.setPlugs(elasticCloudCreateActionParamResult.getData().getPlugs());
            elasticCloudCreateAction.setImageName(elasticCloudCreateActionParamResult.getData().getImageName());
        }

        // 保存集群角色信息 es_role_cluster
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            ElasticCloudCreateActionParam param = (ElasticCloudCreateActionParam) ecmParamBase;
            ESRoleClusterDTO esRoleClusterDTO = new ESRoleClusterDTO();
            esRoleClusterDTO.setElasticClusterId(param.getPhyClusterId());
            if (param.getRoleName().startsWith(param.getPhyClusterName())) {
                esRoleClusterDTO.setRoleClusterName(param.getRoleName());
            } else {
                esRoleClusterDTO.setRoleClusterName(param.getPhyClusterName() + "-" + param.getRoleName());
            }
            esRoleClusterDTO.setRole(param.getRoleName());
            esRoleClusterDTO.setPodNumber(param.getNodeNumber());
            esRoleClusterDTO.setPidCount(ecmParamBase.getNodeNumber());
            esRoleClusterDTO.setMachineSpec(param.getMachineSpec());
            esRoleClusterDTO.setCfgId(INVALID_VALUE.intValue());
            esRoleClusterDTO.setEsVersion(((ElasticCloudCreateActionParam) ecmParamBase).getEsVersion());

            roleClusterService.save(esRoleClusterDTO);
        }
        return Result.buildSucc(elasticCloudCreateActionParamResult.getData().getPhyClusterId());
    }

    @Override
    public Result<EcmOperateAppBase> startESCluster(EcmParamBase actionParamBase) {
        if (actionParamBase == null) {
            return Result.buildParamIllegal("ECM参数列表为空");
        }

        ElasticCloudCreateActionParam elasticCloudCreateActionParam = (ElasticCloudCreateActionParam) actionParamBase;
        String namespace = elasticCloudCreateActionParam.namespace();

        //创建odin父节点
        Result<Void> result = buildFatherOdinNode(elasticCloudCreateActionParam);
        if (result.failed()) {
            LOGGER.info(
                "class=EcmDockerHandler||method=startESCluster||subMethod=buildFatherOdinNode||clusterId={}||namespace={}",
                actionParamBase.getPhyClusterId(), elasticCloudCreateActionParam.getNsTree());
            return Result.buildFrom(result);
        }

        // 创建odin节点
        Result<Void> createTreeNodeResult = odinRemoteService.createTreeNode(namespace,
            CloudClusterCreateParamConstant.ODIN_CATEGORY_SERVICE, namespace, ODIN_CATEGORY_LEVEL_2);
        if (createTreeNodeResult.failed()) {
               return Result.buildFrom(createTreeNodeResult);
        }

        // 构造容器云请求参数
        Result<ElasticCloudCreateParamDTO> buildParamResult = buildElasticCloudCreateParamDTO(
            elasticCloudCreateActionParam);
        if (buildParamResult.failed()) {
             return Result.buildFrom(buildParamResult);
        }

        // 创建容器云集群
        Result<ElasticCloudAppStatus> createClusterResult = elasticCloudRemoteService
            .createAndStartAll(buildParamResult.getData(), namespace, elasticCloudCreateActionParam.getIdc());
        if (createClusterResult.failed()) {
            return Result.buildFrom(createClusterResult);
        }

        return Result.buildSucc(new EcmOperateAppBase(createClusterResult.getData().getTaskId()));
    }

    @Override
    public Result<EcmOperateAppBase> scaleESCluster(EcmParamBase actionParamBase) {
        return elasticCloudRemoteService.scaleAndExecuteAll((ElasticCloudScaleActionParam) actionParamBase);
    }

    @Override
    public Result<EcmOperateAppBase> upgradeESCluster(EcmParamBase actionParamBase) {
        return elasticCloudRemoteService.upgradeOrRestartByGroup((ElasticCloudCommonActionParam) actionParamBase);
    }

    @Override
    public Result<EcmOperateAppBase> restartESCluster(EcmParamBase actionParamBase) {
        return elasticCloudRemoteService.upgradeOrRestartByGroup((ElasticCloudCommonActionParam) actionParamBase);
    }

    @Override
    public Result<EcmOperateAppBase> removeESCluster(EcmParamBase actionParamBase) {
        ElasticCloudCommonActionParam commonActionParam = (ElasticCloudCommonActionParam) actionParamBase;
        try {
            //等待1秒删除机器
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            LOGGER.error("class=ElasticClusterServiceImpl||method=removeElasticCluster||role={}||errMsg={}",
                    commonActionParam.getRoleName(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.error("class=ElasticClusterServiceImpl||method=removeElasticCluster||role={}||errMsg={}",
                commonActionParam.getRoleName(), e);
        }

        return elasticCloudRemoteService.delete(commonActionParam);
    }

    @Override
    public Result actionNotFinishedTask(EcmParamBase actionParamBase, EcmActionEnum ecmActionEnum, String hostname) {
        return elasticCloudRemoteService.actionNotFinishedTask((ElasticCloudCommonActionParam) actionParamBase,
            ecmActionEnum);
    }

    @Override
    public Result<String> infoESCluster(EcmParamBase actionParamBase) {
        return elasticCloudRemoteService.getClusterInfo((ElasticCloudCommonActionParam) actionParamBase);
    }

    @Override
    public Result<EcmSubTaskLog> getSubTaskLog(Long taskId, String hostname, EcmParamBase actionParamBase) {
        return elasticCloudRemoteService.getTaskLog(taskId, hostname, actionParamBase);
    }

    @Override
    public Result<List<EcmTaskStatus>> getTaskStatus(EcmParamBase actionParamBase, Integer orderType) {
        ElasticCloudAppStatus cloudStatus = fetchRemoteStatus(actionParamBase, orderType);
        if (AriusObjUtils.isNull(cloudStatus)) {
            return Result.buildFail("failed to get the valid odin status, please check odin status");
        }

        // 全量机器的状态
        List<EcmTaskStatus> ecmTaskStatuses = Lists.newArrayList();

        if (isCreateOrScaleAction(cloudStatus)) {
            if (CollectionUtils.isEmpty(cloudStatus.getPods())) {
                int nodeNumber = actionParamBase.getNodeNumber();
                for (int i = 0; i < nodeNumber; i++) {
                    EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
                    ecmTaskStatus.setTaskId(cloudStatus.getTaskId());
                    ecmTaskStatus.setStatusEnum(RUNNING);
                    ecmTaskStatuses.add(ecmTaskStatus);
                }
            } else {
                //新建、扩缩容集群的任务状态, 获取podsStatus
                cloudStatus.getPods().stream().filter(Objects::nonNull).forEach(podStatus -> {
                    EcmTaskStatus ecmTaskStatus = podStatus.convert2EcmTaskStatus(cloudStatus.getTaskId(),
                        cloudStatus.getState());
                    ecmTaskStatuses.add(ecmTaskStatus);
                });
            }
        }

        if (isUpdateAction(cloudStatus)
            && CollectionUtils.isNotEmpty(cloudStatus.getPods())
                && CollectionUtils.isNotEmpty(cloudStatus.getHostStatus())) {
            //重启、升级转化ECMTask状态, 获取hostStatus(ready、updating、updated)
            List<ElasticCloudPod> pods = cloudStatus.getPods();
            cloudStatus.getHostStatus().stream().filter(Objects::nonNull).forEach(hostStatus -> {
                String podIp = pods.stream()
                    .filter(r -> !AriusObjUtils.isNull(r) && r.getPodHostname().equals(hostStatus.getHost()))
                    .map(ElasticCloudPod::getPodIp).collect(Collectors.toList()).get(0);

                EcmTaskStatus ecmTaskStatus = hostStatus.convert2EcmTaskStatus(cloudStatus.getTaskId(),
                    cloudStatus.getState());
                ecmTaskStatus.setPodIp(podIp);
                ecmTaskStatuses.add(ecmTaskStatus);
            });
        }

        return Result.buildSucc(mergeEcmTaskStatus(actionParamBase, ecmTaskStatuses, cloudStatus));
    }

    /***********************************************private***********************************************************/

    /**获取本次子任务需要操作机器的状态*/
    private List<EcmTaskStatus> mergeEcmTaskStatus(EcmParamBase paramBase, List<EcmTaskStatus> remoteStatuses,
                                                   ElasticCloudAppStatus cloudStatus) {
        if (CollectionUtils.isEmpty(remoteStatuses)) {
            return remoteStatuses;
        }

        if (isUpdateAction(cloudStatus)) {
            return remoteStatuses;
        }

        List<EcmTaskStatus> finalEcmTaskStatus = Lists.newArrayList();
        if (isCreateOrScaleAction(cloudStatus)) {
            Set<String> remoteHostsName = remoteStatuses.stream().filter(Objects::nonNull)
                .map(EcmTaskStatus::getHostname).collect(Collectors.toSet());

            List<RoleClusterHost> clusterHostsFromDb = roleClusterHostService
                .getByRoleAndClusterId(paramBase.getPhyClusterId(), paramBase.getRoleName());

            List<String> clusterHostNamesFromDb = clusterHostsFromDb.stream()
                .filter(r -> !AriusObjUtils.isNull(r) && !AriusObjUtils.isBlack(r.getIp()))
                .map(RoleClusterHost::getHostname).collect(Collectors.toList());

            if (clusterHostNamesFromDb.size() > remoteStatuses.size()) {
                //缩容, 获取减少机器的状态
                List<EcmTaskStatus> reduceStatuses = clusterHostNamesFromDb.stream()
                    .filter(host -> !AriusObjUtils.isNullStr(host) && !remoteHostsName.contains(host))
                    .map(host -> buildReduceStatus(host, cloudStatus)).collect(Collectors.toList());
                finalEcmTaskStatus.addAll(reduceStatuses);
            } else {
                //扩容/新增，获取增加机器的状态
                List<EcmTaskStatus> increaseStatuses = remoteStatuses.stream().filter(
                    status -> !AriusObjUtils.isNull(status) && !clusterHostNamesFromDb.contains(status.getHostname()))
                    .collect(Collectors.toList());
                finalEcmTaskStatus.addAll(increaseStatuses);
            }
        }

        return finalEcmTaskStatus;
    }

    private static EcmTaskStatus buildReduceStatus(String host, ElasticCloudAppStatus status) {
        EcmTaskStatus reduceStatus = new EcmTaskStatus();
        reduceStatus.setHostname(host);
        reduceStatus.setTaskId(status.getTaskId());
        reduceStatus.setStatusEnum(RUNNING);
        reduceStatus.setPodIndex(0);
        reduceStatus.setGroup(0);
        reduceStatus.setPodIp("");
        return reduceStatus;
    }

    /**获取远程弹性云状态*/
    private ElasticCloudAppStatus fetchRemoteStatus(EcmParamBase paramBase, Integer orderType) {
        ElasticCloudAppStatus finallyCloudStatus = null;
        int retryTimes = 0;
        while (++retryTimes < SYN_ODIN_STATUS_MAX_RETRY_TIMES) {
            try {
                Thread.sleep(500L);
                Result<ElasticCloudStatus> result = elasticCloudRemoteService.getTaskStatus(paramBase);

                //cpu spin optimization processing
                if (EcmTaskTypeEnum.SHRINK.getCode() != orderType && AriusObjUtils.isNull(result.getData().getStatus())
                    && retryTimes == 15) {
                    LOGGER.error("class=EcmDockerHandler||method=fetchRemoteStatus"
                                 + "||clusterId={}||roleClusterName={}||retryTimes={}||"
                                 + "msg=odin cluster is not exist or failed to get the odin status",
                        paramBase.getPhyClusterId(), paramBase.getPhyClusterName(), retryTimes);
                    break;
                }

                //print repeat times
                LOGGER.info("class=EcmDockerHandler||method=fetchRemoteStatus||clusterId={}||"
                            + "roleClusterName={}||retryTimes={}",
                    paramBase.getPhyClusterId(), paramBase.getPhyClusterName(), retryTimes);

                //Status check
                ElasticCloudAppStatus cloudStatus = getElasticCloudAppStatus(result);
                if (cloudStatus == null) continue;

                //shrink operation needs to wait for completion
                if (hasWaitStatusDone(cloudStatus, orderType)) {
                    finallyCloudStatus = cloudStatus;
                    break;
                }

                finallyCloudStatus = cloudStatus;
                break;

            } catch(InterruptedException e) {
                LOGGER.error("class=EcmDockerHandler||method=postHandle||clusterId={}||roleClusterName={}||error={}",
                        paramBase.getPhyClusterId(), paramBase.getPhyClusterName(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOGGER.error("class=EcmDockerHandler||method=postHandle||clusterId={}||roleClusterName={}||error={}",
                    paramBase.getPhyClusterId(), paramBase.getPhyClusterName(), e);
            }
        }

        if (AriusObjUtils.isNull(finallyCloudStatus)) {
            LOGGER.error("class=EcmDockerHandler||method=postHandle||clusterId={}||roleClusterName={}||"
                         + "msg=failed to get the valid odin status, please check odin status",
                paramBase.getPhyClusterId(), paramBase.getPhyClusterName());
        }

        return finallyCloudStatus;
    }

    private ElasticCloudAppStatus getElasticCloudAppStatus(Result<ElasticCloudStatus> result) {
        if (result.failed()) {
            return null;
        }
        ElasticCloudStatus elasticCloudStatus = result.getData();
        if (AriusObjUtils.isNull(elasticCloudStatus)) {
            return null;
        }
        ElasticCloudAppStatus cloudStatus = elasticCloudStatus.getStatus();
        if (AriusObjUtils.isNull(cloudStatus)) {
            return null;
        }
        return cloudStatus;
    }

    private boolean hasWaitStatusDone(ElasticCloudAppStatus cloudStatus, Integer orderType) {
        return EcmTaskTypeEnum.SHRINK.getCode() == orderType
               && (ClusterConstant.CLOUD_DONE_STATUS.equals(cloudStatus.getState())
                   || ClusterConstant.CLOUD_FAILED_STATUS.equals(cloudStatus.getState()));
    }

    private Result<ElasticCloudCreateParamDTO> buildElasticCloudCreateParamDTO(ElasticCloudCreateActionParam elasticCloudCreateActionParam) {
        if (elasticCloudCreateActionParam == null) {
            return Result.buildParamIllegal("elasticCloudCreateActionParam is empty");
        }

        ElasticCloudCreateParamDTO elasticCloudCreateParamDTO = new ElasticCloudCreateParamDTO();
        elasticCloudCreateParamDTO.setName(
            elasticCloudCreateActionParam.getPhyClusterName() + "-" + elasticCloudCreateActionParam.getRoleName());
        elasticCloudCreateParamDTO.setScene(CLOUD_CLUSTER_SCENE);

        Result<ElasticCloudSpecInfoDTO> spaceInfoResult = buildElasticCloudCreateParamSpecInfoDTO(
            elasticCloudCreateActionParam);
        if (spaceInfoResult.failed()) {
            return Result.buildFrom(spaceInfoResult);
        }
        elasticCloudCreateParamDTO.setSpecInfo(spaceInfoResult.getData());
        elasticCloudCreateParamDTO.setPodCount(elasticCloudCreateActionParam.getNodeNumber());
        elasticCloudCreateParamDTO.setUseNodePort(true);
        elasticCloudCreateParamDTO.setDisableIpQuota(Boolean.FALSE);
        elasticCloudCreateParamDTO.setPrivileged(true);
        elasticCloudCreateParamDTO.setSecurityCaps(new ElasticCloudSecurityCapsDTO(Arrays.asList("SYS_ADMIN")));
        return Result.buildSucc(elasticCloudCreateParamDTO);
    }

    private Result<ElasticCloudSpecInfoDTO> buildElasticCloudCreateParamSpecInfoDTO(ElasticCloudCreateActionParam elasticCloudCreateActionParam) {
        //检查client、data节点是否有master节点
        String masterList = "";
        if (!elasticCloudCreateActionParam.getRoleName().equals(MASTER_NODE.getDesc())) {
            List<String> masterHostNames = roleClusterHostService
                .getHostNamesByRoleAndClusterId(elasticCloudCreateActionParam.getPhyClusterId(), MASTER_NODE.getDesc());
            if (CollectionUtils.isEmpty(masterHostNames)) {
                return Result.buildNotExist("master-node is no exist, failed to create the cluster");
            }
            masterList = ListUtils.strList2String(masterHostNames);
        }

        String[] splitMachineSpec = elasticCloudCreateActionParam.getMachineSpec().split("-");
        //创建集群的磁盘大小，无单位
        int diskSize = Integer.parseInt(splitMachineSpec[2].replace(CloudClusterCreateParamConstant.DISK_SUFFIX, ""));

        ElasticCloudSpecInfoDTO elasticCloudCreateParamSpecInfoDTO = new ElasticCloudSpecInfoDTO();
        elasticCloudCreateParamSpecInfoDTO.setDeployTimeout(CLOUD_CLUSTER_DEPLOY_TIMEOUT_START);
        elasticCloudCreateParamSpecInfoDTO
            .setDeployGroups(new ElasticCloudAppDeployGroupsDTO(CLOUD_CLUSTER_DEPLOY_GROUP_ONE_COUNT,
                CLOUD_CLUSTER_DEPLOY_GROUP_TWO_COUNT, CLOUD_CLUSTER_DEPLOY_GROUP_THREE_COUNT));

        elasticCloudCreateParamSpecInfoDTO
            .setMachineSpec(new ElasticCloudMachineSpecDTO(splitMachineSpec[0], splitMachineSpec[1]));
        elasticCloudCreateParamSpecInfoDTO.setDeployConcurrency(1);

        elasticCloudCreateParamSpecInfoDTO.setVolumes(Arrays.asList(new ElasticCloudVolumeDTO(diskSize,
            CloudClusterCreateParamConstant.VOLUME_TYPE_HOSTPATH, CLOUD_CLUSTER_VOLUME_PATH)));

        elasticCloudCreateParamSpecInfoDTO.setEnvs(Arrays.asList(
            //pod 与k8s 之间的心跳时间
            new ElasticCloudEnvDTO(CLOUD_CLUSTER_ENV_NAME_TICK_TIME, "2000"),
            new ElasticCloudEnvDTO(CLOUD_CLUSTER_ENV_NAME_CLUSTERNAME,
                elasticCloudCreateActionParam.getPhyClusterName()),
            new ElasticCloudEnvDTO(CLOUD_CLUSTER_ENV_NAME_NODEROLE, elasticCloudCreateActionParam.getRoleName()),
            new ElasticCloudEnvDTO(CLOUD_CLUSTER_ENV_NAME_MASTERNODES, masterList),
            new ElasticCloudEnvDTO(CLOUD_CLUSTER_ENV_NAME_T_C, CLOUD_CLUSTER_ENV_NAME_T_C_VALUE),
            new ElasticCloudEnvDTO(CLOUD_CLUSTER_ENV_NAME_ECM_URL, selfServer), new ElasticCloudEnvDTO(
                CLOUD_CLUSTER_ENV_NAME_JOB_ID, elasticCloudCreateActionParam.getPhyClusterId().toString())));
        elasticCloudCreateParamSpecInfoDTO.setImageAddress(elasticCloudCreateActionParam.getImageName());
        return Result.buildSucc(elasticCloudCreateParamSpecInfoDTO);
    }

    private Result<Void> buildFatherOdinNode(ElasticCloudCreateActionParam elasticCloudCreateActionParam) {
        if (elasticCloudCreateActionParam.getRoleName().equals(MASTER_NODE.getDesc())) {
            String namespace = elasticCloudCreateActionParam.getPhyClusterName() + "."
                               + elasticCloudCreateActionParam.getNsTree();
            // 创建odin父节点
            Result<Void> createTreeNodeResult = odinRemoteService.createTreeNode(namespace,
                CloudClusterCreateParamConstant.ODIN_CATEGORY_GROUP, namespace, ODIN_CATEGORY_LEVEL_1);
            if (createTreeNodeResult.failed()) {
                return Result.buildFrom(createTreeNodeResult);
            }
        }
        return Result.buildSucc();
    }

    private Result<ElasticCloudCreateActionParam> persistClusterPOAndSupplyField(ElasticCloudCreateActionParam param) {
        ESClusterDTO esClusterDTO = ConvertUtil.obj2Obj(param, ESClusterDTO.class);
        esClusterDTO.setCluster(param.getPhyClusterName());

        ESPackage esPackage = esPackageService.getByVersionAndType(param.getEsVersion(), param.getType());
        esClusterDTO.setImageName(esPackage.getUrl());
        esClusterDTO.setPackageId(esPackage.getId());

        esClusterDTO.setPlugIds(buildEsClusterPlugins(esClusterDTO));
        esClusterDTO.setHttpAddress(ClusterConstant.DEFAULT_HTTP_ADDRESS);
        esClusterDTO.setTemplateSrvs(ClusterConstant.DEFAULT_CLUSTER_TEMPLATE_SRVS);

        Result<Boolean> clusterResult = esClusterPhyService.createCluster(esClusterDTO, param.getCreator());
        if (clusterResult.failed()) {
            return Result.buildFrom(clusterResult);
        }
        param.setPhyClusterId(esClusterDTO.getId().longValue());
        param.setImageName(esPackage.getUrl());
        param.setPlugs(esClusterDTO.getPlugIds());
        return Result.buildSucc(param);
    }

    private String buildEsClusterPlugins(ESClusterDTO esClusterDTO) {
        String defaultPlugins = StringUtils.defaultString(esPluginService.getAllSysDefaultPluginIds(), "");

        if (StringUtils.isNotEmpty(esClusterDTO.getPlugIds())) {
            defaultPlugins = defaultPlugins + "," + esClusterDTO.getPlugIds();
        }
        return defaultPlugins;
    }

    private boolean isCreateOrScaleAction(ElasticCloudAppStatus cloudStatus) {
        return (CREATE.getAction().equals(cloudStatus.getType()) || SCALE.getAction().equals(cloudStatus.getType()));
    }

    private boolean isUpdateAction(ElasticCloudAppStatus cloudStatus) {
        return UPDATE.getAction().equals(cloudStatus.getType());
    }
}
