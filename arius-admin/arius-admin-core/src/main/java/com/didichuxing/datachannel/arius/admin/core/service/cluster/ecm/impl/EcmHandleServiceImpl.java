package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_DOCKER;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_HOST;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EsConfigAction;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EsPluginAction;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.ESClusterMethodNameEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler.AbstractEcmBaseHandle;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler.EcmDockerHandler;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler.EcmHostHandler;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.EcmActionEnum;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.didiglobal.knowframework.security.service.UserService;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import lombok.NoArgsConstructor;

/**
 * ES集群表 服务实现类
 * @author didi
 * @since 2020-08-24
 */
@Service
@NoArgsConstructor
public class EcmHandleServiceImpl implements EcmHandleService {

    private static final ILog LOGGER                           = LogFactory
            .getLog(EcmHandleServiceImpl.class);

    @Autowired
    private ClusterPhyService                   esClusterPhyService;

    @Autowired
    private ClusterRoleService                  clusterRoleService;

    @Autowired
    private ClusterRoleHostService              clusterRoleHostService;

    @Autowired
    private EcmDockerHandler                    ecmDockerHandler;

    @Autowired
    private EcmHostHandler                      ecmHostHandler;

    @Autowired
    private OperateRecordService                operateRecordService;

    @Autowired
    private ESPackageService                    esPackageService;

    @Autowired
    private UserService                         userService;
    @Autowired
    private ProjectService                      projectService;

    private AriusTaskThreadPool                 ariusTaskThreadPool;

    private Map<Integer, AbstractEcmBaseHandle> ecmBaseHandleMap                 = new HashMap<>();

    private static final int                    DELETE_ODIN_TREE_MAX_RETRY_TIMES = 1 << 6;

    private static final String                 CLUSTER_NOT_EXIST                = "%d对应的物理集群不存在";

    private static final String                 UNKNOWN_TYPE                     = "未知类型，请确认类型为(docker/host)";

    @PostConstruct
    public void init() {
        ariusTaskThreadPool = new AriusTaskThreadPool();
        ariusTaskThreadPool.init(10, "EcmHandleServiceImpl", 100);
        ecmBaseHandleMap.put(ecmHostHandler.getEsClusterTypeEnum().getCode(), ecmHostHandler);
        ecmBaseHandleMap.put(ecmDockerHandler.getEsClusterTypeEnum().getCode(), ecmDockerHandler);
    }

    @Override
    public Result<Long> saveESCluster(List<EcmParamBase> ecmParamBaseList) {
        Result<Void> checkResult = checkValidForEsCluster(ecmParamBaseList);
        if (checkResult.failed()) {
            return Result.buildFrom(checkResult);
        }

        AbstractEcmBaseHandle ecmBaseHandle = getByClusterType(ecmParamBaseList.get(0).getType());
        if (null == ecmBaseHandle) {
            return Result.buildNotExist(UNKNOWN_TYPE);
        }

        Result<Long> saveResult = ecmBaseHandle.saveESCluster(ecmParamBaseList);
        if (saveResult.failed()) {
            return Result.buildFrom(saveResult);
        }
        return Result.buildSucc(saveResult.getData());
    }

    @Override
    public Result<EcmOperateAppBase> startESCluster(EcmParamBase ecmParamBase, String operator) {
        // 接口调用
        return callESClusterBaseHandle("集群启动", ecmParamBase.getPhyClusterId(), operator, ecmParamBase,
            (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle.startESCluster(ecmParamBase));
    }

    @Override
    public Result<Void> deleteESCluster(Long clusterId, String operator) {
        return Result.buildSucc();
    }

    @Override
    public Result<EcmOperateAppBase> scaleESCluster(EcmParamBase actionParamBase, String operator) {
        // 补充参数
        Result<EcmParamBase> actionParamBaseResult = supplyCommonActionParamBase(actionParamBase.getPhyClusterId(),
            actionParamBase.getRoleName(), actionParamBase);
        if (actionParamBaseResult.failed()) {
            return Result.buildFrom(actionParamBaseResult);
        }

        // 调用接口
        return callESClusterBaseHandle("集群扩缩容", actionParamBase.getPhyClusterId(), operator,
            actionParamBaseResult.getData(),
            (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle.scaleESCluster(actionParamBaseResult.getData()));
    }

    @Override
    public Result<EcmOperateAppBase> upgradeESCluster(EcmParamBase ecmParamBase, String operator) {
        // 构造请求参数
        Result<EcmParamBase> actionParamBaseResult = supplyCommonActionParamBase(ecmParamBase.getPhyClusterId(),
            ecmParamBase.getRoleName(), ecmParamBase);

        if (actionParamBaseResult.failed()) {
            return Result.buildFrom(actionParamBaseResult);
        }

        // 接口调用
        return callESClusterBaseHandle("集群升级", ecmParamBase.getPhyClusterId(), operator,
            actionParamBaseResult.getData(), (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle
                .upgradeESCluster(actionParamBaseResult.getData()));
    }

    @Override
    public Result<EcmOperateAppBase> restartESCluster(EcmParamBase ecmParamBase, String operator) {
        // 构造请求参数
        Result<EcmParamBase> actionParamBaseResult = supplyCommonActionParamBase(ecmParamBase.getPhyClusterId(),
            ecmParamBase.getRoleName(), ecmParamBase);
        if (actionParamBaseResult.failed()) {
            return Result.buildFrom(actionParamBaseResult);
        }

        // 接口调用
        return callESClusterBaseHandle("集群重启", ecmParamBase.getPhyClusterId(), operator,
            actionParamBaseResult.getData(), (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle
                .restartESCluster(actionParamBaseResult.getData()));
    }

    @Override
    public Result<EcmOperateAppBase> actionUnfinishedESCluster(EcmActionEnum ecmActionEnum, EcmParamBase ecmParamBase,
                                                               String hostname, String operator) {
        // 构造请求参数
        Result<EcmParamBase> actionParamBaseResult = supplyCommonActionParamBase(ecmParamBase.getPhyClusterId(),
            ecmParamBase.getRoleName(), ecmParamBase);
        if (actionParamBaseResult.failed()) {
            return Result.buildFrom(actionParamBaseResult);
        }

        // 接口调用
        return callESClusterBaseHandle("集群" + ecmActionEnum.getAction(), ecmParamBase.getPhyClusterId(), operator,
            actionParamBaseResult.getData(), (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle
                .actionNotFinishedTask(actionParamBaseResult.getData(), ecmActionEnum, hostname));
    }

    @Override
    public Result<String> infoESCluster(Long clusterId, String operator) {
        // 构造请求参数
        Result<EcmParamBase> actionParamBaseResult = buildActionParamBase(clusterId, null);
        if (actionParamBaseResult.failed()) {
            return Result.buildFrom(actionParamBaseResult);
        }

        return callESClusterBaseHandle("集群信息获取", clusterId, operator, actionParamBaseResult.getData(),
            (actionParamBase, esClusterBaseHandle) -> esClusterBaseHandle.infoESCluster(actionParamBase));
    }

    @Override
    public Result<EcmSubTaskLog> getSubTaskLog(EcmParamBase ecmParamBase, String hostname, String operator) {
        return callESClusterBaseHandle("集群日志收集", ecmParamBase.getPhyClusterId(), operator, ecmParamBase,
            (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle
                .getSubTaskLog(ecmParamBase.getTaskId().longValue(), hostname, ecmParamBase));
    }

    @Override
    public Result<List<EcmTaskStatus>> getESClusterStatus(EcmParamBase ecmParamBase, Integer orderType,
                                                          String operator) {
        return callESClusterBaseHandle("集群状态", ecmParamBase.getPhyClusterId(), operator, ecmParamBase,
            (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle.getTaskStatus(ecmParamBase, orderType), false);
    }

    @Override
    public Result<List<EcmParamBase>> buildEcmParamBaseListWithConfigAction(Integer phyClusterId,
                                                                            List<String> roleNameList,
                                                                            Multimap<String, Long> role2ConfigIdsMultiMap,
                                                                            Integer actionType) {
        return buildEcmParamBaseListInner(phyClusterId, roleNameList, role2ConfigIdsMultiMap, actionType, null);
    }

    @Override
    public Result<List<EcmParamBase>> buildEcmParamBaseListWithEsPluginAction(Integer phyClusterId,
                                                                              List<String> roleNameList,
                                                                              Long esPluginId, Integer actionType) {
        return buildEcmParamBaseListInner(phyClusterId, roleNameList, null, actionType, esPluginId);
    }

    @Override
    public Result<List<EcmParamBase>> buildEcmParamBaseList(Integer phyClusterId, List<String> roleNameList) {
        return buildEcmParamBaseListInner(phyClusterId, roleNameList, null, null, null);
    }

    private Result<List<EcmParamBase>> buildEcmParamBaseListInner(Integer phyClusterId, List<String> roleNameList,
                                                                  Multimap<String, Long> role2ConfigIdsMultiMap,
                                                                  Integer actionType, Long pluginId) {
        if (AriusObjUtils.isEmptyList(roleNameList)) {
            return Result.buildSucc(new ArrayList<>());
        }

        ClusterPhy clusterPhy = esClusterPhyService.getClusterById(phyClusterId);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail(String.format(CLUSTER_NOT_EXIST, phyClusterId));
        }

        List<String> masterHostList = clusterRoleHostService
            .getHostNamesByRoleAndClusterId(clusterPhy.getId().longValue(), MASTER_NODE.getDesc());

        List<EcmParamBase> ecmParamBaseList = new ArrayList<>();
        for (String roleName : roleNameList) {
            String newRoleName = roleName;
            if (newRoleName.startsWith(clusterPhy.getCluster())) {
                newRoleName = newRoleName.replaceFirst(clusterPhy.getCluster() + "-", "");
            }

            if (ES_DOCKER.getCode() == clusterPhy.getType()) {
                Result<EcmParamBase> buildElasticCloudParamBase = buildElasticCloudParamBase(clusterPhy, newRoleName,
                    role2ConfigIdsMultiMap, actionType);
                if (buildElasticCloudParamBase.failed()) {
                    return Result.buildFrom(buildElasticCloudParamBase);
                }
                ecmParamBaseList.add(buildElasticCloudParamBase.getData());
            } else if (ES_HOST.getCode() == clusterPhy.getType()) {
                Result<EcmParamBase> buildHostParamBase = buildHostParamBase(clusterPhy, newRoleName, masterHostList,
                    role2ConfigIdsMultiMap, pluginId, actionType);
                if (buildHostParamBase.failed()) {
                    return Result.buildFrom(buildHostParamBase);
                }
                ecmParamBaseList.add(buildHostParamBase.getData());
            }
        }
        return Result.buildSucc(ecmParamBaseList);
    }

    /**************************************** private method ****************************************************/

    private Result<EcmParamBase> buildHostParamBase(ClusterPhy clusterPhy, String roleName, List<String> masterHostList,
                                                    Multimap<String, Long> role2ConfigIdsMultiMap, Long pluginId,
                                                    Integer actionType) {
        HostParamBase hostParamBase = new HostParamBase();
        hostParamBase.setPhyClusterId(clusterPhy.getId().longValue());
        hostParamBase.setPhyClusterName(clusterPhy.getCluster());
        hostParamBase.setRoleName(roleName);
        hostParamBase.setType(clusterPhy.getType());
        hostParamBase.setMasterHostList(masterHostList);

        // ES集群配置回调设置, 在事件回调处调用
        if (!AriusObjUtils.isNull(role2ConfigIdsMultiMap) && !AriusObjUtils.isNull(actionType)) {
            List<Long> configs = (List<Long>) role2ConfigIdsMultiMap.get(roleName);
            hostParamBase.setEsConfigAction(new EsConfigAction(actionType, configs));
        }

        // ES集群插件回调设置, 在事件回调处调用
        if (!AriusObjUtils.isNull(pluginId)) {
            hostParamBase.setEsPluginAction(new EsPluginAction(actionType, pluginId));
        }

        ClusterRoleInfo clusterRoleInfo = clusterRoleService.getByClusterIdAndRole(clusterPhy.getId().longValue(),
            roleName);
        if (null == clusterRoleInfo) {
            hostParamBase.setEsVersion(clusterPhy.getEsVersion());
            // 根据物理集群的版本号获取对应的镜像地址
            ESPackage esPackage = esPackageService.getByVersionAndType(clusterPhy.getEsVersion(), clusterPhy.getType());
            if (AriusObjUtils.isNull(esPackage)) {
                return Result.buildParamIllegal(String.format("传入的版本号: %s 有误", clusterPhy.getEsVersion()));
            }
            hostParamBase.setImageName(esPackage.getUrl());
            return Result.buildSucc(hostParamBase);
        }

        // 根绝role_cluster获取对应ecm操作的es集群版本号
        ESPackage esPackage = esPackageService.getByVersionAndType(clusterRoleInfo.getEsVersion(), ES_HOST.getCode());
        if (AriusObjUtils.isNull(esPackage)) {
            return Result.buildParamIllegal(String.format("传入的版本号: %s 有误", clusterRoleInfo.getEsVersion()));
        }

        hostParamBase.setPidCount(clusterRoleInfo.getPidCount());
        hostParamBase.setEsVersion(clusterRoleInfo.getEsVersion());
        hostParamBase.setImageName(esPackage.getUrl());

        List<String> hostList = clusterRoleHostService.getHostNamesByRoleAndClusterId(clusterPhy.getId().longValue(),
            roleName);
        hostParamBase.setHostList(hostList);
        if (!CollectionUtils.isEmpty(hostList)) {
            hostParamBase.setNodeNumber(hostList.size());
        } else {
            hostParamBase.setNodeNumber(0);
        }

        return Result.buildSucc(hostParamBase);
    }

    private Result<EcmParamBase> buildElasticCloudParamBase(ClusterPhy clusterPhy, String roleName,
                                                            Multimap<String, Long> role2ConfigIdsMultiMap,
                                                            Integer actionType) {
        ElasticCloudCommonActionParam elasticCloudCommonActionParam = ConvertUtil.obj2Obj(clusterPhy,
            ElasticCloudCommonActionParam.class);

        if (!AriusObjUtils.isNull(role2ConfigIdsMultiMap) && !AriusObjUtils.isNull(actionType)) {
            List<Long> configs = (List<Long>) role2ConfigIdsMultiMap.get(roleName);
            elasticCloudCommonActionParam.setEsConfigActions(new EsConfigAction(actionType, configs));
        }

        elasticCloudCommonActionParam.setPhyClusterId(clusterPhy.getId().longValue());
        elasticCloudCommonActionParam.setPhyClusterName(clusterPhy.getCluster());

        elasticCloudCommonActionParam.setRoleName(roleName);
        elasticCloudCommonActionParam.setMachineRoom(clusterPhy.getIdc());

        ClusterRoleInfo clusterRoleInfo = clusterRoleService.getByClusterIdAndRole(clusterPhy.getId().longValue(),
            roleName);
        ESPackage esPackage = esPackageService.getByVersionAndType(clusterRoleInfo.getEsVersion(), ES_DOCKER.getCode());
        if (AriusObjUtils.isNull(esPackage)) {
            return Result.buildParamIllegal(String.format("传入的版本号: %s 有误", clusterRoleInfo.getEsVersion()));
        }

        elasticCloudCommonActionParam.setNodeNumber(clusterRoleInfo.getPidCount());
        elasticCloudCommonActionParam.setEsVersion(clusterRoleInfo.getEsVersion());
        elasticCloudCommonActionParam.setImageName(esPackage.getUrl());

        return Result.buildSucc(elasticCloudCommonActionParam);
    }

    private Result<EcmParamBase> supplyCommonActionParamBase(Long clusterId, String roleName,
                                                             EcmParamBase actionParamBase) {
        ClusterPhy clusterPhy = esClusterPhyService.getClusterById(clusterId.intValue());
        if (null == clusterPhy) {
            return Result.buildNotExist(String.format(CLUSTER_NOT_EXIST, clusterId.intValue()));
        }

        AbstractEcmBaseHandle esClusterBaseHandle = getByClusterType(clusterPhy.getType());
        if (null == esClusterBaseHandle) {
            return Result.buildNotExist(UNKNOWN_TYPE);
        }

        if (actionParamBase instanceof HostParamBase) {
            // 增加对应角色的端口号注入
            HostParamBase hostParamBase = (HostParamBase) actionParamBase;
            Result<String> result = getPortFromHost(clusterId, hostParamBase.getRoleName());
            if (result.failed()) {
                return Result.buildFrom(result);
            }
            hostParamBase.setPort(result.getData());
        }

        actionParamBase.setPhyClusterId(clusterId);
        actionParamBase.setPhyClusterName(clusterPhy.getCluster());
        if (AriusObjUtils.isBlank(roleName) || ESClusterTypeEnum.ES_HOST.getCode() == clusterPhy.getType()) {
            return Result.buildSucc(actionParamBase);
        }

        // 弹性云集群 补充角色信息
        String newRoleName = roleName.startsWith(clusterPhy.getCluster()) ? roleName
            : clusterPhy.getCluster() + "-" + roleName;
        ElasticCloudCommonActionParam elasticCloudCommonActionParam = (ElasticCloudCommonActionParam) actionParamBase;
        ClusterRoleInfo clusterRoleInfo = clusterRoleService.getByClusterIdAndClusterRole(clusterId, newRoleName);
        if (null == clusterRoleInfo) {
            return Result.buildNotExist(String.format("%d对应的物理集群%s角色不存在", clusterId, roleName));
        }
        elasticCloudCommonActionParam.setMachineRoom(clusterPhy.getIdc());
        elasticCloudCommonActionParam.setNsTree(clusterPhy.getNsTree());
        elasticCloudCommonActionParam.setRoleName(clusterRoleInfo.getRole());

        return Result.buildSucc(elasticCloudCommonActionParam);
    }

    private Result<String> getPortFromHost(Long clusterId, String roleName) {
        List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.getByRoleAndClusterId(clusterId, roleName);
        if (CollectionUtils.isEmpty(clusterRoleHosts)) {
            return Result.buildSucc(ClusterConstant.DEFAULT_PORT, "获取默认配置端口");
        }
        return Result.buildSucc(clusterRoleHosts.get(0).getPort(), "角色端口获取成功");
    }

    private Result<EcmParamBase> buildActionParamBase(Long clusterId, String roleName) {
        ClusterPhy clusterPhy = esClusterPhyService.getClusterById(clusterId.intValue());
        if (null == clusterPhy) {
            return Result.buildNotExist(String.format(CLUSTER_NOT_EXIST, clusterId));
        }

        ClusterRoleInfo clusterRoleInfo = clusterRoleService.getByClusterIdAndClusterRole(clusterId, roleName);
        if (null == clusterRoleInfo) {
            return Result.buildNotExist(String.format("%d对应的物理集群%s角色不存在", clusterId, roleName));
        }

        AbstractEcmBaseHandle esClusterBaseHandle = getByClusterType(clusterPhy.getType());
        if (null == esClusterBaseHandle) {
            return Result.buildNotExist(UNKNOWN_TYPE);
        }
        ElasticCloudCommonActionParam elasticCloudCommonActionParam = new ElasticCloudCommonActionParam(clusterId,
            roleName);

        elasticCloudCommonActionParam.setPhyClusterId(clusterId);
        elasticCloudCommonActionParam.setPhyClusterName(clusterPhy.getCluster());
        elasticCloudCommonActionParam.setType(clusterPhy.getType());

        elasticCloudCommonActionParam.setRoleName(clusterRoleInfo.getRole());
        elasticCloudCommonActionParam.setNsTree(clusterPhy.getNsTree());
        elasticCloudCommonActionParam.setMachineRoom(clusterPhy.getIdc());

        return Result.buildSucc(elasticCloudCommonActionParam);
    }

    private <T> Result<T> callESClusterBaseHandle(String methodName, Long clusterId, String operator,
                                                  EcmParamBase ecmParamBase,
                                                  BiFunction<EcmParamBase, AbstractEcmBaseHandle, Result<T>> function) {
        return callESClusterBaseHandle(methodName, clusterId, operator, ecmParamBase, function, true);
    }

    private <T> Result<T> callESClusterBaseHandle(String methodName, Long clusterId, String operator,
                                                  EcmParamBase ecmParamBase,
                                                  BiFunction<EcmParamBase, AbstractEcmBaseHandle, Result<T>> function,
                                                  boolean recordOperate) {
        // 获取对应的handler
        AbstractEcmBaseHandle abstractEcmBaseHandle = getByClusterType(ecmParamBase.getType());
        if (null == abstractEcmBaseHandle) {
            return Result.buildNotExist(UNKNOWN_TYPE);
        }

        // 执行方法进行处理
        Result<T> result = function.apply(ecmParamBase, abstractEcmBaseHandle);

        String infoLog = String.format(
            "class=ESClusterHandleServiceImpl||method=callESClusterBaseHandle||methodName={%s}||clusterId={%d}||result={%s}",
            methodName, clusterId, JSON.toJSONString(result));
        LOGGER.info(infoLog);

        // 操作记录
        if (recordOperate) {
            ESClusterMethodNameEnum.getOperateTypeEnum(methodName)
                .ifPresent(operateTypeEnum -> operateRecordService
                    .save(new OperateRecord.Builder().operationTypeEnum(operateTypeEnum)
                        .content(String.format("物理集群 %s 开始进行 %s 操作", clusterId, methodName))
                        .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                        .triggerWayEnum(TriggerWayEnum.SCHEDULING_TASKS).bizId(clusterId).userOperation(operator).build()));

          
        }

        return result;
    }

    private AbstractEcmBaseHandle getByClusterType(Integer clusterType) {
        return ecmBaseHandleMap.get(clusterType);
    }

    private void deleteLocalClusterInfo(ClusterPhy clusterPhy, String operator, Integer projectId) {
        Result<Boolean> deleteClusterResult = esClusterPhyService.deleteClusterById(clusterPhy.getId(), projectId);
        if (deleteClusterResult.failed()) {
            LOGGER
                .error("class=ElasticClusterServiceImpl||method=deleteLocalClusterInfo||clusterId={}||clusterName={}||"
                       + "msg=failed to delete local db cluster info",
                    clusterPhy.getId(), clusterPhy.getCluster());
        }

        //逻辑删除
        Result<Void> deleteRoleClusterResult = clusterRoleService.deleteRoleClusterByClusterId(clusterPhy.getId(),
            projectId);
        if (deleteRoleClusterResult.failed()) {
            LOGGER.error("class=ElasticClusterServiceImpl||method=deleteLocalClusterInfo||clusterName={}||"
                         + "msg=failed to delete local db role cluster info",
                clusterPhy.getCluster());
        }

        //逻辑删除
        Result<Void> deleteRoleClusterHostResult = clusterRoleHostService.deleteByCluster(clusterPhy.getCluster(),
            projectId);
        if (deleteRoleClusterHostResult.failed()) {
            LOGGER.error(
                "class=ElasticClusterServiceImpl||method=deleteLocalClusterInfo||roleClusterName=={}||roleClusterName={}||"
                         + "msg=failed to delete role host cluster info",
                clusterPhy.getCluster(), clusterPhy.getCluster());
        }
    }

    private Result<Void> validityCheck(Integer clusterId, String operator) {
        if (userService.getUserBriefByUserName(operator) == null) {
            return Result.buildFail("User has no permissions");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(
                "class=EcmHandleServiceImpl||method=validityCheck||debugMsg={clusterId={%d}, operator={%s}},",
                clusterId, operator));
        }

        Result<Boolean> checkClusterPhyExitTemplateResult = checkClusterPhyExitTemplate(clusterId);
        if (checkClusterPhyExitTemplateResult.failed()) {
            return Result.buildFrom(checkClusterPhyExitTemplateResult);
        }

        return Result.buildSucc();
    }

    private Result<Boolean> checkClusterPhyExitTemplate(Integer clusterId) {
        //检查集群上是否关联模板资源
        return Result.buildSucc();
    }

    private Result<List<ElasticCloudCommonActionParam>> deleteOdinMachine(ClusterPhy clusterPhy,
                                                                          List<ClusterRoleInfo> allRoles,
                                                                          String operator) {
        List<ElasticCloudCommonActionParam> elasticCloudActionParams = Lists.newArrayList();
        allRoles.stream().filter(role -> nonNull(role) && role.getRole() != null).forEach(role -> {
            // 构造请求参数
            Result<EcmParamBase> actionParamBaseResult = buildActionParamBase(clusterPhy.getId().longValue(),
                clusterPhy.getCluster() + "-" + role.getRole());
            if (actionParamBaseResult.failed()) {
                LOGGER.error(
                    "class=EcmHandleServiceImpl||method=deleteESCluster||msg=failed to build the actionParamBaseResult");
            }

            elasticCloudActionParams.add((ElasticCloudCommonActionParam) actionParamBaseResult.getData());
            // 调用接口，删除单个odin节点上的机器
            Result<EcmOperateAppBase> r = callESClusterBaseHandle("集群移除", clusterPhy.getId().longValue(), operator,
                actionParamBaseResult.getData(), (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle
                    .removeESCluster(actionParamBaseResult.getData()));

            if (r.failed()) {
                LOGGER.error("class=EcmHandleServiceImpl||method=deleteESCluster||clusterName={}||" + "msg={}",
                    actionParamBaseResult.getData().getPhyClusterName(), r.getMessage());
            }
        });

        return Result.buildSucc(elasticCloudActionParams);
    }

    private Result<Void> checkValidForEsCluster(List<EcmParamBase> ecmParamBaseList) {
        if (CollectionUtils.isEmpty(ecmParamBaseList)) {
            return Result.buildFail("ecm参数为空");
        }

        ClusterPhy clusterByName = esClusterPhyService.getClusterByName(ecmParamBaseList.get(0).getPhyClusterName());
        if (!AriusObjUtils.isNull(clusterByName)) {
            return Result.buildDuplicate("集群信息已存在");
        }

        return Result.buildSucc();
    }
}