package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EsConfigAction;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EsPluginAction;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.constant.ESCloudClusterCreateParamConstant;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler.AbstractEcmBaseHandle;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler.EcmDockerHandler;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler.EcmHostHandler;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.EcmActionEnum;
import com.didichuxing.datachannel.arius.admin.remote.monitor.RemoteMonitorService;
import com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean.OdinTreeNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.CLUSTER;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EXE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum.ES_DOCKER;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum.ES_HOST;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESCloudClusterCreateParamConstant.ODIN_CATEGORY_LEVEL_1;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESCloudClusterCreateParamConstant.ODIN_CATEGORY_LEVEL_2;
import static java.util.Objects.nonNull;

/**
 * ES集群表 服务实现类
 * @author didi
 * @since 2020-08-24
 */
@Service
public class EcmHandleServiceImpl implements EcmHandleService {

    private final static Logger                 LOGGER                           = LoggerFactory
        .getLogger(EcmHandleServiceImpl.class);

    @Autowired
    private ESClusterPhyService                 esClusterPhyService;

    @Autowired
    private ESRoleClusterService                esRoleClusterService;

    @Autowired
    private ESRoleClusterHostService            esRoleClusterHostService;

    @Autowired
    private EcmDockerHandler                    ecmDockerHandler;

    @Autowired
    private EcmHostHandler                      ecmHostHandler;

    @Autowired
    private OperateRecordService                operateRecordService;

    @Autowired
    private ESPackageService                    esPackageService;

    @Autowired
    private RemoteMonitorService                remoteMonitorService;

    @Autowired
    private AriusUserInfoService                ariusUserInfoService;

    @Autowired
    private RemoteMonitorService                odinRemoteService;

    @Autowired
    private AriusTaskThreadPool                 ariusTaskThreadPool;

    private Map<Integer, AbstractEcmBaseHandle> ecmBaseHandleMap                 = new HashMap<>();

    private static final int                    DELETE_ODIN_TREE_MAX_RETRY_TIMES = 1 << 6;

    @PostConstruct
    public void init() {
        LOGGER.info("class=EcmHandleServiceImpl||method=init||EcmHandleServiceImpl init start.");
        ecmBaseHandleMap.put(ecmHostHandler.getEsClusterTypeEnum().getCode(), ecmHostHandler);
        ecmBaseHandleMap.put(ecmDockerHandler.getEsClusterTypeEnum().getCode(), ecmDockerHandler);
        LOGGER.info("class=EcmHandleServiceImpl||method=init||EcmHandleServiceImpl init finished.");
    }

    @Override
    public Result saveESCluster(List<EcmParamBase> ecmParamBaseList) {
        Result checkResult = checkValidForEsCluster(ecmParamBaseList);
        if (checkResult.failed()) {
            return checkResult;
        }

        AbstractEcmBaseHandle ecmBaseHandle = getByClusterType(ecmParamBaseList.get(0).getType());
        if (null == ecmBaseHandle) {
            return Result.buildNotExist("未知类型，请确认类型为(docker/host)");
        }

        Result saveResult = ecmBaseHandle.saveESCluster(ecmParamBaseList);
        if (saveResult.failed()) {
            return Result.buildFail(saveResult.getMessage());
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
    public Result deleteESCluster(Long clusterId, String operator) {
        Result checkResult = validityCheck(clusterId.intValue(), operator);
        if (checkResult.failed()) {
            return checkResult;
        }

        ESClusterPhy clusterPhy = esClusterPhyService.getClusterById(clusterId.intValue());
        if (clusterPhy == null) {
            return Result.buildFail("clusterPhy is empty");
        }
        List<ESRoleCluster> allRoles = esRoleClusterService.getAllRoleClusterByClusterId(clusterId.intValue());
        if (CollectionUtils.isEmpty(allRoles)) {
            return Result.buildFail("the role of cluster is empty");
        }

        Result<List<ElasticCloudCommonActionParam>> deleteOdinResult = deleteOdinMachine(clusterPhy, allRoles,
            operator);

        if (deleteOdinResult.success()) {
            ariusTaskThreadPool
                .run(() -> deleteOdinTreeNodeAndLocalDbInfo(deleteOdinResult.getData(), clusterPhy, operator));
        }

        return Result.buildSucc(deleteOdinResult.getMessage());
    }

    @Override
    public Result scaleESCluster(EcmParamBase actionParamBase, String operator) {
        // 补充参数
        Result<EcmParamBase> actionParamBaseResult = supplyCommonActionParamBase(actionParamBase.getPhyClusterId(),
            actionParamBase.getRoleName(), actionParamBase);
        if (actionParamBaseResult.failed()) {
            return Result.buildFail(actionParamBaseResult.getMessage());
        }

        // 调用接口
        return callESClusterBaseHandle("集群扩缩容", actionParamBase.getPhyClusterId(), operator,
            actionParamBaseResult.getData(),
            (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle.scaleESCluster(actionParamBaseResult.getData()));
    }

    @Override
    public Result upgradeESCluster(EcmParamBase ecmParamBase, String operator) {
        // 构造请求参数
        Result<EcmParamBase> actionParamBaseResult = supplyCommonActionParamBase(ecmParamBase.getPhyClusterId(),
            ecmParamBase.getRoleName(), ecmParamBase);
        if (actionParamBaseResult.failed()) {
            return Result.buildFail(actionParamBaseResult.getMessage());
        }

        // 接口调用
        return callESClusterBaseHandle("集群升级", ecmParamBase.getPhyClusterId(), operator,
            actionParamBaseResult.getData(), (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle
                .upgradeESCluster(actionParamBaseResult.getData()));
    }

    @Override
    public Result restartESCluster(EcmParamBase ecmParamBase, String operator) {
        // 构造请求参数
        Result<EcmParamBase> actionParamBaseResult = supplyCommonActionParamBase(ecmParamBase.getPhyClusterId(),
            ecmParamBase.getRoleName(), ecmParamBase);
        if (actionParamBaseResult.failed()) {
            return Result.buildFail(actionParamBaseResult.getMessage());
        }

        // 接口调用
        return callESClusterBaseHandle("集群重启", ecmParamBase.getPhyClusterId(), operator,
            actionParamBaseResult.getData(), (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle
                .restartESCluster(actionParamBaseResult.getData()));
    }

    @Override
    public Result actionUnfinishedESCluster(EcmActionEnum ecmActionEnum, EcmParamBase ecmParamBase, String hostname,
                                            String operator) {
        // 构造请求参数
        Result<EcmParamBase> actionParamBaseResult = supplyCommonActionParamBase(ecmParamBase.getPhyClusterId(),
            ecmParamBase.getRoleName(), ecmParamBase);
        if (actionParamBaseResult.failed()) {
            return Result.buildFail(actionParamBaseResult.getMessage());
        }

        // 接口调用
        return callESClusterBaseHandle("集群" + ecmActionEnum.getAction(), ecmParamBase.getPhyClusterId(), operator,
            actionParamBaseResult.getData(), (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle
                .actionNotFinishedTask(actionParamBaseResult.getData(), ecmActionEnum, hostname));
    }

    @Override
    public Result infoESCluster(Long clusterId, String operator) {
        // 构造请求参数
        Result<EcmParamBase> actionParamBaseResult = buildActionParamBase(null, clusterId, null);
        if (actionParamBaseResult.failed()) {
            return Result.buildFail(actionParamBaseResult.getMessage());
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
    public Result<OdinTreeNode> getOdinTreeNode(String username) {
        OdinTreeNode odinTreeNode = remoteMonitorService.getOdinTreeNode(username);
        if (odinTreeNode == null) {
            Result.buildFail();
        }
        return Result.buildSucc(odinTreeNode);
    }

    @Override
    public Result<List<EcmParamBase>> buildEcmParamBaseListWithConfigAction(Integer phyClusterId,
                                                                            List<String> roleNameList,
                                                                            Multimap<String, Long> role2ConfigIdsMultiMap,
                                                                            Integer actionType) {
        return buildEcmParamBaseListInner(phyClusterId, roleNameList, role2ConfigIdsMultiMap, actionType,null);
    }

    @Override
    public Result<List<EcmParamBase>> buildEcmParamBaseListWithEsPluginAction(Integer phyClusterId,
                                                                              List<String> roleNameList,
                                                                              Long esPluginId,
                                                                              Integer actionType) {
        return buildEcmParamBaseListInner(phyClusterId, roleNameList, null, actionType, esPluginId);
    }

    @Override
    public Result<List<EcmParamBase>> buildEcmParamBaseList(Integer phyClusterId, List<String> roleNameList) {
        return buildEcmParamBaseListInner(phyClusterId, roleNameList, null, null,null);
    }

    private Result<List<EcmParamBase>> buildEcmParamBaseListInner(Integer phyClusterId, List<String> roleNameList,
                                                                  Multimap<String, Long> role2ConfigIdsMultiMap,
                                                                  Integer actionType, Long pluginId) {
        if (ValidateUtils.isEmptyList(roleNameList)) {
            return Result.buildSucc(new ArrayList<>());
        }

        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(phyClusterId);
        if (ValidateUtils.isNull(esClusterPhy)) {
            return Result.buildFail(String.format("%d对应的物理集群不存在", phyClusterId));
        }

        List<String> masterHostList = esRoleClusterHostService
            .getHostNamesByRoleAndClusterId(esClusterPhy.getId().longValue(), MASTER_NODE.getDesc());

        List<EcmParamBase> ecmParamBaseList = new ArrayList<>();
        for (String roleName : roleNameList) {
            String newRoleName = roleName;
            if (newRoleName.startsWith(esClusterPhy.getCluster())) {
                newRoleName = newRoleName.replaceFirst(esClusterPhy.getCluster() + "-", "");
            }

            if (ES_DOCKER.getCode() == esClusterPhy.getType()) {
                ecmParamBaseList
                    .add(buildElasticCloudParamBase(esClusterPhy, newRoleName, role2ConfigIdsMultiMap, actionType));
            } else if (ES_HOST.getCode() == esClusterPhy.getType()) {
                ecmParamBaseList.add(
                    buildHostParamBase(esClusterPhy, newRoleName, masterHostList, role2ConfigIdsMultiMap, pluginId ,actionType));
            }
        }
        return Result.buildSucc(ecmParamBaseList);
    }

    /**************************************** private method ****************************************************/

    private EcmParamBase buildHostParamBase(ESClusterPhy esClusterPhy, String roleName, List<String> masterHostList,
                                            Multimap<String, Long> role2ConfigIdsMultiMap, Long pluginId, Integer actionType) {
        HostParamBase hostParamBase = new HostParamBase();
        hostParamBase.setPhyClusterId(esClusterPhy.getId().longValue());
        hostParamBase.setPhyClusterName(esClusterPhy.getCluster());
        hostParamBase.setRoleName(roleName);
        hostParamBase.setType(esClusterPhy.getType());

        // ES集群配置回调设置, 在事件回调处调用
        if (!AriusObjUtils.isNull(role2ConfigIdsMultiMap) && !AriusObjUtils.isNull(actionType)) {
            List<Long> configs = (List<Long>) role2ConfigIdsMultiMap.get(roleName);
            hostParamBase.setEsConfigAction(new EsConfigAction(actionType, configs));
        }

        // ES集群插件回调设置, 在事件回调处调用
        if (!AriusObjUtils.isNull(pluginId)) {
            hostParamBase.setEsPluginAction(new EsPluginAction(actionType, pluginId));
        }

        ESRoleCluster esRoleCluster = esRoleClusterService.getByClusterIdAndRole(esClusterPhy.getId().longValue(),
            roleName);
        ESPackage esPackage = esPackageService.getByVersionAndType(esRoleCluster.getEsVersion(), ES_HOST.getCode());

        hostParamBase.setPidCount(esRoleCluster.getPidCount());
        hostParamBase.setEsVersion(esRoleCluster.getEsVersion());
        hostParamBase.setImageName(esPackage.getUrl());

        List<String> hostList = esRoleClusterHostService
            .getHostNamesByRoleAndClusterId(esClusterPhy.getId().longValue(), roleName);
        hostParamBase.setHostList(hostList);
        hostParamBase.setNodeNumber(hostList.size());
        hostParamBase.setMasterHostList(masterHostList);
        return hostParamBase;
    }

    private EcmParamBase buildElasticCloudParamBase(ESClusterPhy esClusterPhy, String roleName,
                                                    Multimap<String, Long> role2ConfigIdsMultiMap, Integer actionType) {
        ElasticCloudCommonActionParam elasticCloudCommonActionParam = ConvertUtil.obj2Obj(esClusterPhy,
            ElasticCloudCommonActionParam.class);

        if (!AriusObjUtils.isNull(role2ConfigIdsMultiMap) && !AriusObjUtils.isNull(actionType)) {
            List<Long> configs = (List<Long>) role2ConfigIdsMultiMap.get(roleName);
            elasticCloudCommonActionParam.setEsConfigActions(new EsConfigAction(actionType, configs));
        }

        elasticCloudCommonActionParam.setPhyClusterId(esClusterPhy.getId().longValue());
        elasticCloudCommonActionParam.setPhyClusterName(esClusterPhy.getCluster());

        elasticCloudCommonActionParam.setRoleName(roleName);
        elasticCloudCommonActionParam.setMachineRoom(esClusterPhy.getIdc());

        ESRoleCluster esRoleCluster = esRoleClusterService.getByClusterIdAndRole(esClusterPhy.getId().longValue(),
            roleName);
        ESPackage esPackage = esPackageService.getByVersionAndType(esRoleCluster.getEsVersion(), ES_DOCKER.getCode());

        elasticCloudCommonActionParam.setNodeNumber(esRoleCluster.getPidCount());
        elasticCloudCommonActionParam.setEsVersion(esRoleCluster.getEsVersion());
        elasticCloudCommonActionParam.setImageName(esPackage.getUrl());
        return elasticCloudCommonActionParam;
    }

    private Result<EcmParamBase> supplyCommonActionParamBase(Long clusterId, String roleName,
                                                             EcmParamBase actionParamBase) {
        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(clusterId.intValue());
        if (null == esClusterPhy) {
            return Result.buildNotExist(String.format("%d对应的物理集群不存在", clusterId.intValue()));
        }

        AbstractEcmBaseHandle esClusterBaseHandle = getByClusterType(esClusterPhy.getType());
        if (null == esClusterBaseHandle) {
            return Result.buildNotExist("未知类型，请确认类型为(docker/host)");
        }

        actionParamBase.setPhyClusterId(clusterId);
        actionParamBase.setPhyClusterName(esClusterPhy.getCluster());
        if (ValidateUtils.isBlank(roleName) || ESClusterTypeEnum.ES_HOST.getCode() == esClusterPhy.getType()) {
            return Result.buildSucc(actionParamBase);
        }

        // 弹性云集群 补充角色信息
        String newRoleName = roleName.startsWith(esClusterPhy.getCluster()) ? roleName
            : esClusterPhy.getCluster() + "-" + roleName;
        ElasticCloudCommonActionParam elasticCloudCommonActionParam = (ElasticCloudCommonActionParam) actionParamBase;
        ESRoleCluster esRoleCluster = esRoleClusterService.getByClusterIdAndClusterRole(clusterId, newRoleName);
        if (null == esRoleCluster) {
            return Result.buildNotExist(String.format("%d对应的物理集群%s角色不存在", clusterId, roleName));
        }
        elasticCloudCommonActionParam.setMachineRoom(esClusterPhy.getIdc());
        elasticCloudCommonActionParam.setNsTree(esClusterPhy.getNsTree());
        elasticCloudCommonActionParam.setRoleName(esRoleCluster.getRole());

        return Result.buildSucc(elasticCloudCommonActionParam);
    }

    private Result<EcmParamBase> buildActionParamBase(EcmActionEnum ecmActionEnum, Long clusterId, String roleName) {
        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(clusterId.intValue());
        if (null == esClusterPhy) {
            return Result.buildNotExist(String.format("%d对应的物理集群不存在", clusterId));
        }

        ESRoleCluster esRoleCluster = esRoleClusterService.getByClusterIdAndClusterRole(clusterId, roleName);
        if (null == esRoleCluster) {
            return Result.buildNotExist(String.format("%d对应的物理集群%s角色不存在", clusterId, roleName));
        }

        AbstractEcmBaseHandle esClusterBaseHandle = getByClusterType(esClusterPhy.getType());
        if (null == esClusterBaseHandle) {
            return Result.buildNotExist("未知类型，请确认类型为(docker/host)");
        }
        ElasticCloudCommonActionParam elasticCloudCommonActionParam = new ElasticCloudCommonActionParam(clusterId,
            roleName);

        elasticCloudCommonActionParam.setPhyClusterId(clusterId);
        elasticCloudCommonActionParam.setPhyClusterName(esClusterPhy.getCluster());
        elasticCloudCommonActionParam.setType(esClusterPhy.getType());

        elasticCloudCommonActionParam.setRoleName(esRoleCluster.getRole());
        elasticCloudCommonActionParam.setNsTree(esClusterPhy.getNsTree());
        elasticCloudCommonActionParam.setMachineRoom(esClusterPhy.getIdc());

        return Result.buildSucc(elasticCloudCommonActionParam);
    }

    private Result callESClusterBaseHandle(String methodName, Long clusterId, String operator,
                                           EcmParamBase ecmParamBase,
                                           BiFunction<EcmParamBase, AbstractEcmBaseHandle, Result> function) {
        return callESClusterBaseHandle(methodName, clusterId, operator, ecmParamBase, function, true);
    }

    private Result callESClusterBaseHandle(String methodName, Long clusterId, String operator,
                                           EcmParamBase ecmParamBase,
                                           BiFunction<EcmParamBase, AbstractEcmBaseHandle, Result> function,
                                           boolean recordOperate) {
        // 获取对应的handler
        AbstractEcmBaseHandle abstractEcmBaseHandle = getByClusterType(ecmParamBase.getType());
        if (null == abstractEcmBaseHandle) {
            return Result.buildNotExist("未知类型，请确认类型为(docker/host)");
        }

        // 执行方法进行处理
        Result result = function.apply(ecmParamBase, abstractEcmBaseHandle);

        LOGGER.info(
            "class=ESClusterHandleServiceImpl||method=callESClusterBaseHandle||methodName={}||clusterId={}||result={}",
            methodName, clusterId, JSON.toJSONString(result));

        // 操作记录
        if (recordOperate) {
            operateRecordService.save(CLUSTER, EXE, String.valueOf(clusterId),
                String.format("物理集群 %s 开始进行 %s 操作", clusterId, methodName), operator);
        }

        return result;
    }

    private AbstractEcmBaseHandle getByClusterType(Integer clusterType) {
        return ecmBaseHandleMap.get(clusterType);
    }

    private void deleteOdinTreeNodeAndLocalDbInfo(List<ElasticCloudCommonActionParam> elasticCloudActionParams,
                                                  ESClusterPhy clusterPhy, String operator) {

        AtomicBoolean deleteAllTreeNodeFlag = deleteOdinTreeNode(elasticCloudActionParams);
        if (deleteAllTreeNodeFlag.get()) {
            LOGGER.info("class=ElasticClusterServiceImpl||method=delOdinTree||clusterId={}||clusterName={}||"
                        + "msg=success to delete cluster treeNode!",
                clusterPhy.getId(), clusterPhy.getCluster());
            //删除odin节点后同步删除本地
            deleteLocalClusterInfo(clusterPhy, operator);
        }
    }

    private AtomicBoolean deleteOdinTreeNode(List<ElasticCloudCommonActionParam> actionParam) {
        //重复删除多次直到成功
        int retryTime = 0;
        AtomicBoolean deleteAllTreeNodeFlag = new AtomicBoolean(false);
        while (retryTime++ < DELETE_ODIN_TREE_MAX_RETRY_TIMES) {
            actionParam.stream().filter(Objects::nonNull).forEach(param -> {
                try {
                    //等待2s删除odin节点上机器后, 尝试删除tree节点
                    Thread.sleep(2 * 1000L);
                } catch (Exception e) {
                    LOGGER.error("class=ElasticClusterServiceImpl||method=deleteOdinTreeNode||errMsg={}", e);
                }

                // 删除odin单个子节点
                String ns = param.getRoleName() + "." + param.getPhyClusterName() + "." + param.getNsTree();
                odinRemoteService.deleteTreeNode(ns, ESCloudClusterCreateParamConstant.ODIN_CATEGORY_SERVICE, ns,
                    ODIN_CATEGORY_LEVEL_2);

                if (param.getRoleName().equals(ESClusterNodeRoleEnum.DATA_NODE.getDesc())) {
                    // 删除odin父节点
                    String fatherNs = param.getPhyClusterName() + "." + param.getNsTree();
                    Result deleteFatherTreeNodeResult = odinRemoteService.deleteTreeNode(fatherNs,
                        ESCloudClusterCreateParamConstant.ODIN_CATEGORY_GROUP, fatherNs, ODIN_CATEGORY_LEVEL_1);

                    if (deleteFatherTreeNodeResult.success()) {
                        LOGGER.info("class=ElasticClusterServiceImpl||method=delOdinTree||clusterName={}||role={}||"
                                    + "msg=success to delete cluster treeNode",
                            param.getPhyClusterName(), param.getRoleName());
                        deleteAllTreeNodeFlag.set(true);
                    }
                }
            });

            LOGGER.info("class=ElasticClusterServiceImpl||method=delOdinTree||clusterName={}||retryTime={}||"
                        + "msg=try to delete the Odin cluster",
                actionParam.get(0).getPhyClusterName(), retryTime);

            if (deleteAllTreeNodeFlag.get()) {
                return deleteAllTreeNodeFlag;
            }
        }

        return deleteAllTreeNodeFlag;
    }

    private void deleteLocalClusterInfo(ESClusterPhy clusterPhy, String operator) {
        Result deleteClusterResult = esClusterPhyService.deleteClusterById(clusterPhy.getId(), operator);
        if (deleteClusterResult.failed()) {
            LOGGER
                .error("class=ElasticClusterServiceImpl||method=deleteLocalClusterInfo||clusterId={}||clusterName={}||"
                       + "msg=failed to delete local db cluster info",
                    clusterPhy.getId(), clusterPhy.getCluster());
        }

        //逻辑删除
        Result deleteRoleClusterResult = esRoleClusterService.deleteRoleClusterByClusterId(clusterPhy.getId());
        if (deleteRoleClusterResult.failed()) {
            LOGGER.error("class=ElasticClusterServiceImpl||method=deleteLocalClusterInfo||clusterName={}||"
                         + "msg=failed to delete local db role cluster info",
                clusterPhy.getCluster());
        }

        //逻辑删除
        Result deleteRoleClusterHostResult = esRoleClusterHostService.deleteByCluster(clusterPhy.getCluster());
        if (deleteRoleClusterHostResult.failed()) {
            LOGGER.error(
                "class=ElasticClusterServiceImpl||method=deleteLocalClusterInfo||roleClusterName=={}||roleClusterName={}||"
                         + "msg=failed to delete role host cluster info",
                clusterPhy.getCluster(), clusterPhy.getCluster());
        }
    }

    private Result validityCheck(Integer clusterId, String operator) {
        if (!ariusUserInfoService.isOPByDomainAccount(operator)) {
            return Result.buildFail("User has no permissions");
        }

        //TODO:集群上是否关联模板资源
        return Result.buildSucc();
    }

    private Result<List<ElasticCloudCommonActionParam>> deleteOdinMachine(ESClusterPhy clusterPhy,
                                                                          List<ESRoleCluster> allRoles,
                                                                          String operator) {
        List<ElasticCloudCommonActionParam> elasticCloudActionParams = Lists.newArrayList();
        allRoles.stream().filter(role -> nonNull(role) && role.getRole() != null).forEach(role -> {
            // 构造请求参数
            Result<EcmParamBase> actionParamBaseResult = buildActionParamBase(EcmActionEnum.REMOVE,
                clusterPhy.getId().longValue(), clusterPhy.getCluster() + "-" + role.getRole());
            if (actionParamBaseResult.failed()) {
                LOGGER.error(
                    "class=EcmHandleServiceImpl||method=deleteESCluster||msg=failed to build the actionParamBaseResult");
            }

            elasticCloudActionParams.add((ElasticCloudCommonActionParam) actionParamBaseResult.getData());
            // 调用接口，删除单个odin节点上的机器
            Result r = callESClusterBaseHandle("集群移除", clusterPhy.getId().longValue(), operator,
                actionParamBaseResult.getData(), (withoutUsed, esClusterBaseHandle) -> esClusterBaseHandle
                    .removeESCluster(actionParamBaseResult.getData()));

            if (r.failed()) {
                LOGGER.error("class=EcmHandleServiceImpl||method=deleteESCluster||clusterName={}||" + "msg={}",
                    actionParamBaseResult.getData().getPhyClusterName(), r.getMessage());
            }
        });

        return Result.buildSucc(elasticCloudActionParams);
    }

    private Result checkValidForEsCluster(List<EcmParamBase> ecmParamBaseList) {
        if (ValidateUtils.isEmptyList(ecmParamBaseList)) {
            return Result.buildFail("ecm参数为空");
        }

        ESClusterPhy clusterByName = esClusterPhyService.getClusterByName(ecmParamBaseList.get(0).getPhyClusterName());
        if (!AriusObjUtils.isNull(clusterByName)) {
            Result.buildDuplicate("集群信息已存在");
        }

        return Result.buildSucc();
    }
}
