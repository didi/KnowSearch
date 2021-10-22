package com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmHostStatusEnum.RUNNING;
import static com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmHostStatusEnum.SUCCESS;
import static com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskStatusEnum.CANCEL;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.CLIENT_NODE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.ZeusClusterActionEnum.EXPAND;
import static com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.ZeusClusterActionEnum.SHRINK;

import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.WorkOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskDetailManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmTaskBasic;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmTaskDetail;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostCreateActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostScaleActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmHostStatusEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.ecm.EcmTaskPO;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.event.ecm.EcmTaskEditEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.OdinRemoteException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.task.EcmTaskDAO;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.EcmActionEnum;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * ES工单任务管理
 * @author didi
 * @since 2020-08-24
 */
@Service
public class EcmTaskManagerImpl implements EcmTaskManager {
    private final static Logger      LOGGER = LoggerFactory.getLogger(EcmTaskManagerImpl.class);
    
    @Value("${es.client.cluster.port}")
    private String                   esClusterClientPort;

    @Autowired
    private EcmTaskDAO               ecmTaskDao;

    @Autowired
    private EcmHandleService         ecmHandleService;

    @Autowired
    private ESClusterPhyService      esClusterPhyService;

    @Autowired
    private ESRoleClusterService     esRoleClusterService;

    @Autowired
    private ESRoleClusterHostService esRoleClusterHostService;

    @Autowired
    private EcmTaskDetailManager     ecmTaskDetailManager;

    @Override
    public boolean existUnClosedEcmTask(Long phyClusterId) {
        List<EcmTaskPO> notFinishedTasks = ecmTaskDao.listUndoWorkOrderTaskByClusterId(phyClusterId);
        if (ValidateUtils.isEmptyList(notFinishedTasks)) {
            return false;
        }
        return true;
    }

    @Override
    public Result<Long> saveEcmTask(EcmTaskDTO ecmTaskDTO) {
        //过滤掉nodeNumber 为0的数据
        List<EcmParamBase> filteredEcmParamBaseList = ecmTaskDTO.getEcmParamBaseList().stream()
            .filter(elem -> elem.getNodeNumber() != null && elem.getNodeNumber() > 0).collect(Collectors.toList());

        EcmTaskPO ecmTaskPO = ConvertUtil.obj2Obj(ecmTaskDTO, EcmTaskPO.class);
        ecmTaskPO.setClusterNodeRole(ListUtils.strList2String(
            filteredEcmParamBaseList.stream().map(EcmParamBase::getRoleName).collect(Collectors.toList())));
        ecmTaskPO.setHandleData(ConvertUtil.obj2Json(filteredEcmParamBaseList));
        //默认状态都是 待执行
        ecmTaskPO.setStatus(EcmTaskStatusEnum.WAITING.getValue());
        if (ecmTaskDao.save(ecmTaskPO) < 1) {
            // 存储失败
            return Result.buildFail(ecmTaskPO.getTitle());
        }
        return Result.buildSucc(ecmTaskPO.getId());
    }

    @Override
    public List<EcmTask> listEcmTask() {
        return ConvertUtil.list2List(ecmTaskDao.listAll(), EcmTask.class);
    }

    @Override
    public Result<EcmTaskBasic> getEcmTaskBasicByTaskId(Long taskId) {
        EcmTaskPO ecmTaskPO = ecmTaskDao.getById(taskId);
        if (ecmTaskPO == null) {
            return Result.buildFail("任务不存在");
        }
        EcmTask ecmTask = ConvertUtil.obj2Obj(ecmTaskPO, EcmTask.class);
        EcmTaskBasic ecmTaskBasic = ConvertUtil.obj2Obj(ecmTaskPO, EcmTaskBasic.class);

        if (EcmTaskTypeEnum.NEW.getCode() == ecmTaskPO.getOrderType()
            && ESClusterTypeEnum.ES_HOST.getCode() == ecmTaskBasic.getType()) {
            // 集群新建的工单, 该信息从参数中获取
            Map<String, EcmParamBase> ecmParamBaseMap = WorkOrderTaskConverter.convert2EcmParamBaseMap(ecmTask);
            HostCreateActionParam ecmCreateParamBase = (HostCreateActionParam) ecmParamBaseMap
                .getOrDefault(MASTER_NODE.getDesc(), new HostCreateActionParam());
            ecmTaskBasic.setClusterName(ecmCreateParamBase.getPhyClusterName());
            ecmTaskBasic.setIdc(ecmCreateParamBase.getIdc());
            ecmTaskBasic.setNsTree(ecmCreateParamBase.getNsTree());
            ecmTaskBasic.setDesc(ecmCreateParamBase.getDesc());
            ecmTaskBasic.setEsVersion(ecmCreateParamBase.getEsVersion());
            ecmTaskBasic.setImageName(ecmCreateParamBase.getImageName());
            return Result.buildSucc(ecmTaskBasic);
        }

        // 集群已经新建完成, 集群信息已经入库
        ESClusterPhy clusterPhy = esClusterPhyService.getClusterById(ecmTaskPO.getPhysicClusterId().intValue());
        if (clusterPhy != null) {
            ecmTaskBasic.setClusterName(clusterPhy.getCluster());
            ecmTaskBasic.setIdc(clusterPhy.getIdc());
            ecmTaskBasic.setNsTree(clusterPhy.getNsTree());
            ecmTaskBasic.setDesc(clusterPhy.getDesc());
            ecmTaskBasic.setEsVersion(clusterPhy.getEsVersion());
            ecmTaskBasic.setImageName(clusterPhy.getImageName());
        }
        return Result.buildSucc(ecmTaskBasic);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createClusterEcmTask(Long taskId, String operator) {
        //1. 校验ECM任务有效性
        EcmTask ecmTask = getEcmTask(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildFail("Ecm任务不存在");
        }
        if (EcmTaskStatusEnum.RUNNING.getValue().equals(ecmTask.getStatus())) {
            return Result.buildFail("任务正在执行中, 请勿重复操作");
        }
        List<EcmParamBase> ecmParamBases = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        if (CollectionUtils.isEmpty(ecmParamBases)) {
            return Result.buildFail("转化工单数据失败");
        }

        //2. 创建admin物理集群信息
        Result saveResult = ecmHandleService.saveESCluster(ecmParamBases);
        if (saveResult.failed()) {
            return saveResult;
        }

        //3. 更新ecm工单物理集群ID赋值
        ecmTask.setPhysicClusterId((Long) saveResult.getData());
        ecmTask.setHandleData(ConvertUtil.obj2Json(ecmParamBases));
        updateEcmTask(ecmTask);

        //4. 启动任务
        return actionClusterEcmTask(taskId, operator);
    }

    @Override
    public Result actionClusterEcmTask(Long taskId, String operator) {
        //TODO: LYN 与startClusterEcmTask 存在重复代码, 代码复用优化
        //1. 校验ECM任务有效性
        EcmTask ecmTask = getEcmTask(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildFail("Ecm任务不存在");
        }

        if (EcmTaskStatusEnum.RUNNING.getValue().equals(ecmTask.getStatus())) {
            return Result.buildParamIllegal("任务正在执行中, 请勿重复操作");
        }

        List<EcmParamBase> ecmParamBaseList = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        if (CollectionUtils.isEmpty(ecmParamBaseList)) {
            return Result.buildFail("转化工单数据失败");
        }

        //2. 枚举处理每个角色任务信息
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            //2.1 过滤已运行任务
            if (!AriusObjUtils.isNull(ecmParamBase.getTaskId())
                && isStartedTaskFinished(ecmParamBase, ecmTask.getOrderType(), operator)) {
                // 当前任务已经触发执行 & 任务已经完成
                continue;
            } else if (!AriusObjUtils.isNull(ecmParamBase.getTaskId())
                       && !isStartedTaskFinished(ecmParamBase, ecmTask.getOrderType(), operator)) {
                // 当前任务已经触发执行 & 任务未完成, 此时直接return成功
                return Result.buildSucc();
            }

            //2.2 运行ECM任务
            Result<EcmOperateAppBase> ret = runEcmTask(ecmParamBase, ecmTask, operator);
            if (ret.failed()) {
                throw new OdinRemoteException(ret.getMessage());
            }

            //回写taskId至DB
            ecmParamBase.setTaskId(ret.getData().getTaskId());
            ecmTask.setStatus(EcmTaskStatusEnum.RUNNING.getValue());
            ecmTask.setHandleData(ConvertUtil.obj2Json(ecmParamBaseList));
            updateEcmTask(ecmTask);

            //更新es role cluster note数量
            updateRoleClusterNumber(ecmTask, ecmParamBase);
            return Result.buildSucc(ret.getData());
        }

        return Result.buildSucc();
    }

    @Override
    public Result actionClusterEcmTask(Long taskId, EcmActionEnum ecmActionEnum, String hostname, String operator) {
        EcmTaskPO ecmTask = ecmTaskDao.getById(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildParamIllegal("集群任务不存在");
        }

        //接口幂等判断
        if (EcmTaskStatusEnum.RUNNING.getValue().equals(ecmTask.getStatus())) {
            return Result.buildFail("当前集群任务正在执行, 请勿重复操作");
        }

        List<EcmParamBase> ecmParamBaseList = WorkOrderTaskConverter
            .convert2EcmParamBaseList(ConvertUtil.obj2Obj(ecmTask, EcmTask.class));
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            if (!ValidateUtils.isNull(ecmParamBase.getTaskId())
                && isStartedTaskFinished(ecmParamBase, ecmTask.getOrderType(), operator)) {
                // 当前任务已经触发执行 & 任务已经完成
                continue;
            } else if (!ValidateUtils.isNull(ecmParamBase.getTaskId())
                       && !isStartedTaskFinished(ecmParamBase, ecmTask.getOrderType(), operator)) {
                // 当前任务已经触发执行 & 任务未执行完成
                return ecmHandleService.actionUnfinishedESCluster(ecmActionEnum, ecmParamBase, hostname, operator);
            }

            // 任务未触发
            if (EcmActionEnum.CONTINUE.equals(ecmActionEnum)) {
                // 下一个任务未触发执行的情况下, 收到continue之后, 则继续执行后续动作
                return actionClusterEcmTask(taskId, operator);
            }

            // 其他情况直接返回操作失败
            return Result.buildFail("任务已处于暂停状态, 操作无效");
        }
        return Result.buildSucc();
    }

    @Override
    public Result cancelClusterEcmTask(Long taskId, String operator) {
        EcmTask ecmTask = getEcmTask(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildParamIllegal("集群任务不存在");
        }

        ecmTask.setStatus(EcmTaskStatusEnum.CANCEL.getValue());

        //修改工单任务
        updateEcmTask(ecmTask);
        return Result.buildSucc();
    }

    @Override
    public EcmTask getEcmTask(Long id) {
        return ConvertUtil.obj2Obj(ecmTaskDao.getById(id), EcmTask.class);
    }

    @Override
    public int updateEcmTask(EcmTask ecmTask) {
        int ret = ecmTaskDao.update(ConvertUtil.obj2Obj(ecmTask, EcmTaskPO.class));

        if (ret > 0) {
            SpringTool.publish(new EcmTaskEditEvent(this, ecmTask));
        }
        return ret;
    }

    @Override
    public Result<EcmTaskStatusEnum> refreshEcmTask(EcmTask ecmTask) {
        if ((SUCCESS.getValue().equals(ecmTask.getStatus()) || CANCEL.getValue().equals(ecmTask.getStatus()))) {
            return Result.buildSucc(EcmTaskStatusEnum.SUCCESS);
        }

        List<EcmParamBase> ecmParamBases = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        ecmParamBases.forEach(r -> r.setWorkOrderId(ecmTask.getId()));
        Set<EcmTaskStatusEnum> subOrderTaskStatus = Sets.newCopyOnWriteArraySet();

        try {
            long startTime = System.currentTimeMillis();
            ecmParamBases.parallelStream()
                .forEach(ecmParam -> subOrderTaskStatus.add(doRefreshEcmTask(ecmParam, ecmTask)));
            LOGGER.info(
                "class=EcmTaskManagerImpl||method=refreshEcmTask||clusterId={}" + "||orderType={}||consumingTime={}",
                ecmTask.getPhysicClusterId(), ecmTask.getOrderType(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LOGGER.error("class=EcmTaskManagerImpl||method=refreshEcmTask||ecmTaskId={}||msg={}", ecmTask.getId(),
                e.getStackTrace());
        }

        EcmTaskStatusEnum mergedStatusEnum = EcmTaskStatusEnum.calTaskStatus(subOrderTaskStatus);
        ecmTask.setStatus(mergedStatusEnum.getValue());
        updateEcmTask(ecmTask);

        return postProcess(ecmTask, mergedStatusEnum);
    }

    /*************************************** private method ***************************************/
    private Result<EcmOperateAppBase> runEcmTask(EcmParamBase ecmParamBase, EcmTask ecmTask, String operator) {
        Result<EcmOperateAppBase> result;
        if (EcmTaskTypeEnum.NEW.getCode() == ecmTask.getOrderType()) {
            result = ecmHandleService.startESCluster(ecmParamBase, operator);
        } else if (EcmTaskTypeEnum.EXPAND.getCode() == ecmTask.getOrderType()) {
            if (ecmParamBase instanceof HostScaleActionParam) {
                HostScaleActionParam hostScaleActionParam = (HostScaleActionParam) ecmParamBase;
                hostScaleActionParam.setAction(EXPAND.getValue());
            }

            result = ecmHandleService.scaleESCluster(ecmParamBase, operator);
        } else if (EcmTaskTypeEnum.SHRINK.getCode() == ecmTask.getOrderType()) {
            if (ecmParamBase instanceof HostScaleActionParam) {
                HostScaleActionParam hostScaleActionParam = (HostScaleActionParam) ecmParamBase;
                hostScaleActionParam.setAction(SHRINK.getValue());
            }

            result = ecmHandleService.scaleESCluster(ecmParamBase, operator);
        } else if (EcmTaskTypeEnum.RESTART.getCode() == ecmTask.getOrderType()
                   || EcmTaskTypeEnum.PLUG_OPERATION.getCode() == ecmTask.getOrderType()) {
            result = ecmHandleService.restartESCluster(ecmParamBase, operator);
        } else if (EcmTaskTypeEnum.UPGRADE.getCode() == ecmTask.getOrderType()) {
            result = ecmHandleService.upgradeESCluster(ecmParamBase, operator);
        } else {
            return Result.buildFail("任务类型未知, 类型Code:" + ecmTask.getOrderType());
        }
        return result;
    }

    /**任务执行过程进行EcmTask任务相关信息回写操作*/
    private EcmTaskStatusEnum doRefreshEcmTask(EcmParamBase ecmParam, EcmTask ecmTask) {
        if (AriusObjUtils.isNull(ecmParam.getTaskId())) {
            return EcmTaskStatusEnum.PAUSE;
        }

        //1.获取状态
        Result<List<EcmTaskStatus>> taskStatus = ecmHandleService.getESClusterStatus(ecmParam, ecmTask.getOrderType(),
            null);
        if (taskStatus.failed()) {
            return EcmTaskStatusEnum.FAILED;
        }

        if (CollectionUtils.isEmpty(taskStatus.getData())) {
            return EcmTaskStatusEnum.SUCCESS;
        }

        if (!checkEcmTaskStatusValid(taskStatus.getData())) {
            return EcmTaskStatusEnum.RUNNING;
        }

        //2.更新taskDetail表
        List<EcmTaskStatus> remoteStatuses = taskStatus.getData();
        updateTaskDetailByTaskStatus(ecmParam, remoteStatuses);

        //3.写入host表
        if (hasAddHostInDb(ecmTask, remoteStatuses)) {
            handleAddHostToDb(ecmParam, remoteStatuses);
        }

        //4.处理缩容逻辑, 清理关联集群信息
        if (EcmTaskTypeEnum.SHRINK.getCode() == ecmTask.getOrderType()) {
            handleShrinkCluster(remoteStatuses, ecmParam, ecmTask);
        }

        //5.计算最终状态
        Set<EcmTaskStatusEnum> ecmHostStatus = remoteStatuses.parallelStream().filter(Objects::nonNull)
            .map(r -> convertStatus(r.getStatusEnum())).collect(Collectors.toSet());
        return EcmTaskStatusEnum.calTaskStatus(ecmHostStatus);
    }

    /**更新集群读写地址*/
    private void updateClusterRWAddress(Long clusterId) {
        List<ESRoleCluster> roleClusters = esRoleClusterService.getAllRoleClusterByClusterId(clusterId.intValue());
        if (CollectionUtils.isEmpty(roleClusters)) {
            LOGGER.error("class=EcmTaskManagerImpl||method=updateClusterRWAddress||clusterId={}"
                         + "||msg=the role clusters is empty",
                clusterId);
        }

        Long roleId = roleClusters.stream()
            .filter(r -> !AriusObjUtils.isNull(r) && CLIENT_NODE.getDesc().equals(r.getRole()))
            .map(ESRoleCluster::getId).findAny().orElse(null);
        if (AriusObjUtils.isNull(roleId)) {
            LOGGER.error("class=EcmTaskManagerImpl||method=updateClusterRWAddress||clusterId={}"
                         + "||msg=the client node of the clusterId is empty",
                clusterId);
        }

        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(clusterId.intValue());
        if (AriusObjUtils.isNull(esClusterPhy)) {
            LOGGER.error("class=EcmTaskManagerImpl||method=updateClusterRWAddress||clusterId={}"
                         + "||msg=the esClusterPhy is empty",
                clusterId);
        }

        List<ESRoleClusterHost> hosts = esRoleClusterHostService.getByRoleClusterId(roleId);
        if (CollectionUtils.isEmpty(hosts)) {
            LOGGER.error("class=EcmTaskManagerImpl||method=updateClusterRWAddress||clusterId={}"
                         + "||msg=the ecm role cluster host is empty",
                clusterId);
        }

        String ipString = buildAddressIpString(hosts);
        esClusterPhy.setHttpAddress(ipString);
        esClusterPhy.setHttpWriteAddress(ipString);
        Result result = esClusterPhyService.editCluster(ConvertUtil.obj2Obj(esClusterPhy, ESClusterDTO.class),
            AriusUser.SYSTEM.getDesc());
        if (result.failed()) {
            LOGGER.error("class=EcmTaskManagerImpl||method=updateClusterRWAddress||clusterId={}"
                         + "||msg=failed to edit the cluster rw address",
                clusterId);
        }
    }

    private boolean isStartedTaskFinished(List<EcmTaskStatus> ecmTaskStatuses) {
        Set<EcmTaskStatusEnum> statusEnumSet = new HashSet<>();
        for (EcmTaskStatus ecmTaskStatus : ecmTaskStatuses) {
            statusEnumSet.add(convertStatus(ecmTaskStatus.getStatusEnum()));
        }

        EcmTaskStatusEnum ecmTaskStatusEnum = EcmTaskStatusEnum.calTaskStatus(statusEnumSet);
        if (EcmTaskStatusEnum.SUCCESS.equals(ecmTaskStatusEnum)) {
            return true;
        }
        return false;
    }

    /**已执行的任务是否执行完成 */
    private boolean isStartedTaskFinished(EcmParamBase ecmParamBase, Integer orderType, String operator) {
        Result<List<EcmTaskStatus>> result = ecmHandleService.getESClusterStatus(ecmParamBase, orderType, operator);
        if (result.failed()) {
            // 获取任务状态失败, 则直接返回false
            return false;
        }

        return isStartedTaskFinished(result.getData());
    }

    /**更新taskDetail表*/
    private void updateTaskDetailByTaskStatus(EcmParamBase ecmParam, List<EcmTaskStatus> remoteStatuses) {
        Map<Long, EcmTaskStatus> id2ExistDetailMap = Maps.newHashMap();
        List<EcmTaskDetail> taskDetailsFromDb = ecmTaskDetailManager.getByOrderIdAndRoleAndTaskId(
            ecmParam.getWorkOrderId().intValue(), ecmParam.getRoleName(), ecmParam.getTaskId());

        //获取已经存在Detail表的EcmTask
        remoteStatuses.stream().filter(Objects::nonNull)
            .forEach(r -> taskDetailsFromDb.stream().filter(Objects::nonNull).forEach(detailFromDb -> {
                if (detailFromDb.getHostname().equals(r.getHostname())) {
                    id2ExistDetailMap.put(detailFromDb.getId(), r);
                }
            }));

        if (MapUtils.isNotEmpty(id2ExistDetailMap)) {
            for (Map.Entry<Long, EcmTaskStatus> e : id2ExistDetailMap.entrySet()) {
                ecmTaskDetailManager.editEcmTaskDetail(buildEcmTaskDetail(e.getValue(), e.getKey(), ecmParam, EDIT));
            }
        } else {
            remoteStatuses.stream().filter(Objects::nonNull).forEach(
                status -> ecmTaskDetailManager.saveEcmTaskDetail(buildEcmTaskDetail(status, null, ecmParam, ADD)));
        }
    }

    /**任务完成后续回写更新操作*/
    private Result<EcmTaskStatusEnum> postProcess(EcmTask ecmTask, EcmTaskStatusEnum mergedStatusEnum) {
        if (!SUCCESS.getValue().equals(mergedStatusEnum.getValue())) {
            return Result.buildSucc(mergedStatusEnum);
        }

        //重启，升级 odinIp不变
        if (hasCallBackRWAddress(mergedStatusEnum, ecmTask)) {
            updateClusterRWAddress(ecmTask.getPhysicClusterId());
        }

        callBackEsClusterVersion(ecmTask);

        checkClusterFinalStatus(ecmTask);

        return Result.buildSucc(mergedStatusEnum);
    }

    /**升级操作，回写集群版本到集群和角色*/
    private void callBackEsClusterVersion(EcmTask ecmTask) {
        if (EcmTaskTypeEnum.UPGRADE.getCode() != ecmTask.getOrderType()) {
            return;
        }

        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(ecmTask.getPhysicClusterId().intValue());
        if (AriusObjUtils.isNull(esClusterPhy) && AriusObjUtils.isBlack(esClusterPhy.getCluster())) {
            LOGGER.error("class=EcmTaskManagerImpl||method=callBackEsClusterVersion||clusterId={}||"
                         + "msg=the es cluster or the cluster name is empty",
                ecmTask.getPhysicClusterId());
            return;
        }

        List<EcmParamBase> ecmParamBases = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);

        Tuple<String, String> tuple = new Tuple<>();
        if (ecmTask.getType().equals(ESClusterTypeEnum.ES_HOST.getCode())) {
            tuple = getImageAndVersion(ecmParamBases, HostParamBase::getImageName, HostParamBase::getEsVersion,
                HostParamBase.class);
        }

        if (ecmTask.getType().equals(ESClusterTypeEnum.ES_DOCKER.getCode())) {
            tuple = getImageAndVersion(ecmParamBases, ElasticCloudCommonActionParam::getImageName,
                ElasticCloudCommonActionParam::getEsVersion, ElasticCloudCommonActionParam.class);
        }

        //1、更新集群角色的版本
        for (String role : ecmTask.getClusterNodeRole().split(",")) {
            Result result = esRoleClusterService.updateVersionByClusterIdAndRole(ecmTask.getPhysicClusterId(), role,
                tuple.getV2());
            if (null != result && result.failed()) {
                LOGGER.error(
                    "class=EcmTaskManagerImpl||method=callBackEsClusterVersion||clusterId={}||role={}||version={}"
                             + "msg=failed to edit role cluster",
                    ecmTask.getPhysicClusterId(), role, tuple.getV2());
            }
        }

        //2、更新集群的版本
        ESClusterDTO esClusterDTO = new ESClusterDTO();
        esClusterDTO.setId(ecmTask.getPhysicClusterId().intValue());
        esClusterDTO.setImageName(tuple.getV1());
        esClusterDTO.setEsVersion(tuple.getV2());
        Result result = esClusterPhyService.editCluster(esClusterDTO, AriusUser.SYSTEM.getDesc());
        if (null != result && result.failed()) {
            LOGGER.error("class=EcmTaskManagerImpl||method=callBackEsClusterVersion||clusterId={}||"
                         + "msg=failed to edit cluster",
                ecmTask.getPhysicClusterId());
        }
    }

    private <T> Tuple getImageAndVersion(List<EcmParamBase> ecmParamBases, Function<T, String> funImage,
                                         Function<T, String> funVersion, Class<T> type) {
        List<T> params = ConvertUtil.list2List(ecmParamBases, type);
        String changeImageName = params.stream()
            .filter(r -> !AriusObjUtils.isNull(r) && !AriusObjUtils.isBlack(funImage.apply(r))).map(funImage).findAny()
            .orElse(null);

        String changeEsVersion = params.stream()
            .filter(r -> !AriusObjUtils.isNull(r) && !AriusObjUtils.isBlack(funVersion.apply(r))).map(funVersion)
            .findAny().orElse(null);

        return new Tuple(changeImageName, changeEsVersion);
    }

    /**检查集群最后状态*/
    private void checkClusterFinalStatus(EcmTask ecmTask) {
        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(ecmTask.getPhysicClusterId().intValue());
        if (AriusObjUtils.isNull(esClusterPhy) && AriusObjUtils.isBlack(esClusterPhy.getCluster())) {
            LOGGER.error("class=EcmTaskManagerImpl||method=checkClusterFinalStatus||clusterId={}||"
                         + "msg=the esClusterPhy or clusterName is empty",
                ecmTask.getPhysicClusterId());
        }

        try {
            ClusterStatusEnum esStatus = esClusterPhyService.getEsStatus(esClusterPhy.getCluster());
            if (AriusObjUtils.isNull(esStatus)) {
                LOGGER.error("class=EcmTaskManagerImpl||method=checkClusterFinalStatus||clusterId={}||"
                             + "msg=failed to get es cluster status",
                    ecmTask.getPhysicClusterId());
            }

            if (ClusterStatusEnum.RED.getCode().equals(esStatus.getCode())) {
                LOGGER.error("class=EcmTaskManagerImpl||method=checkClusterFinalStatus||clusterId={}||"
                             + "msg=the cluster status is red",
                    ecmTask.getPhysicClusterId());
            }

            if (ClusterStatusEnum.YELLOW.getCode().equals(esStatus.getCode())) {
                LOGGER.warn("class=EcmTaskManagerImpl||method=checkClusterFinalStatus||clusterId={}||"
                            + "msg=the cluster status is yellow",
                    ecmTask.getPhysicClusterId());
            }

            if (ClusterStatusEnum.GREEN.getCode().equals(esStatus.getCode())) {
                LOGGER.info("class=EcmTaskManagerImpl||method=checkClusterFinalStatus||clusterId={}||"
                            + "msg=the cluster status is green",
                    ecmTask.getPhysicClusterId());
            }

        } catch (Exception e) {
            LOGGER.error("class=EcmTaskManagerImpl||method=checkClusterFinalStatus||clusterId={}||" + "msg={}",
                ecmTask.getPhysicClusterId(), e.getStackTrace());
        }
    }

    /**写入host表*/
    private Result handleAddHostToDb(EcmParamBase ecmParamBase, List<EcmTaskStatus> remoteStatuses) {
        ESRoleCluster esRoleCluster = esRoleClusterService.getByClusterIdAndClusterRole(ecmParamBase.getPhyClusterId(),
            ecmParamBase.getPhyClusterName() + "-" + ecmParamBase.getRoleName());
        if (AriusObjUtils.isNull(esRoleCluster)) {
            LOGGER.error("class=EcmTaskManagerImpl||method=handleAddHostToDb||clusterName={}||"
                         + "msg=the esRoleCluster is empty",
                ecmParamBase.getPhyClusterName());
            return Result.buildFail();
        }

        remoteStatuses.stream().filter(Objects::nonNull).forEach(status -> {
            ESRoleClusterHost esRoleClusterHost = esRoleClusterHostService.getByHostName(status.getHostname());
            if (!AriusObjUtils.isNull(esRoleClusterHost)) {
                ESRoleClusterHostDTO esRoleClusterHostDTO = ConvertUtil.obj2Obj(
                    buildRoleClusterHost(status, esRoleCluster, ecmParamBase, esRoleClusterHost),
                    ESRoleClusterHostDTO.class);
                esRoleClusterHostService.editNode(esRoleClusterHostDTO);
                return;
            }

            //若表中有之前删除的host, 置位有效
            ESRoleClusterHost deleteHost = esRoleClusterHostService
                .getDeleteHostByHostNameAnRoleId(status.getHostname(), esRoleCluster.getId());
            if (!AriusObjUtils.isNull(deleteHost)) {
                rebuildRoleClusterHost(status, deleteHost, esRoleCluster);
                esRoleClusterHostService.setHostValid(deleteHost);
                return;
            }

            Result<Long> saveResult = esRoleClusterHostService
                .save(buildRoleClusterHost(status, esRoleCluster, ecmParamBase, null));
            if (saveResult.failed()) {
                LOGGER.error(
                    "class=EcmTaskManagerImpl||method=callBackEsClusterInfo||" + "clusterName{}||" + "hostName||msg={}",
                    ecmParamBase.getPhyClusterName(), status.getHostname(), saveResult.getMessage());
            }
        });

        return Result.buildSucc();
    }

    /**处理缩容逻辑, 清理关联集群信息
     * 1.弹性云:    获取到running状态(缩容完成), 直接删除本地host机器, 删除成功至状态为success
     * 2.宙斯/夜莺: 获取到success的状态后删除*/
    private Result handleShrinkCluster(List<EcmTaskStatus> remoteStatuses, EcmParamBase ecmParamBase, EcmTask ecmTask) {
        if (ecmTask.getType() == ESClusterTypeEnum.ES_DOCKER.getCode()) {
            remoteStatuses.stream().filter(Objects::nonNull).forEach(status -> {
                ESRoleClusterHost host = esRoleClusterHostService.getByHostName(status.getHostname());
                if (AriusObjUtils.isNull(host)) {
                    setTaskDetailStatusToSucc(status, ecmParamBase);
                    return;
                }

                Result delResult = esRoleClusterHostService.deleteById(host.getId());
                if (delResult.failed()) {
                    LOGGER.error("class=EcmTaskManagerImpl||method=handleShrinkCluster||"
                                 + "clusterName{}||hostName={}||msg=failed to delete role cluster host",
                        ecmParamBase.getPhyClusterName(), status.getHostname());
                    status.setStatusEnum(RUNNING);
                    return;
                }
                setTaskDetailStatusToSucc(status, ecmParamBase);
            });
        }

        if (ecmTask.getType() == ESClusterTypeEnum.ES_HOST.getCode()) {
            List<EcmTaskStatus> statuses = remoteStatuses.stream()
                .filter(r -> !AriusObjUtils.isNull(r) && SUCCESS.getValue().equals(r.getStatusEnum().getValue()))
                .collect(Collectors.toList());

            if (statuses.size() == remoteStatuses.size()) {
                statuses.forEach(status -> {
                    ESRoleClusterHost host = esRoleClusterHostService.getByHostName(status.getHostname());
                    if (AriusObjUtils.isNull(host)) {
                        return;
                    }

                    Result result = esRoleClusterHostService.deleteById(host.getId());
                    if (result.failed()) {
                        status.setStatusEnum(RUNNING);
                    }
                });
            }
        }

        return Result.buildSucc();
    }

    private ESRoleClusterHost buildRoleClusterHost(EcmTaskStatus status, ESRoleCluster esRoleCluster,
                                                   EcmParamBase actionParamBase, ESRoleClusterHost roleClusterHost) {

        ESRoleClusterHost esRoleClusterHost = new ESRoleClusterHost();
        if (!AriusObjUtils.isNull(roleClusterHost) && !AriusObjUtils.isNull(roleClusterHost.getId())) {
            esRoleClusterHost.setId(roleClusterHost.getId());
            esRoleClusterHost.setRoleClusterId(esRoleCluster.getId());
            esRoleClusterHost.setHostname(status.getHostname());
            esRoleClusterHost.setIp(status.getPodIp());
            esRoleClusterHost.setCluster(actionParamBase.getPhyClusterName());
            esRoleClusterHost.setRole(ESClusterNodeRoleEnum.getByDesc(esRoleCluster.getRole()).getCode());
            return esRoleClusterHost;
        } else {
            esRoleClusterHost.setRoleClusterId(esRoleCluster.getId());
            esRoleClusterHost.setHostname(status.getHostname());
            esRoleClusterHost.setIp(status.getPodIp());
            esRoleClusterHost.setCluster(actionParamBase.getPhyClusterName());
            esRoleClusterHost.setPort("");
            esRoleClusterHost.setRack("");
            esRoleClusterHost.setRole(ESClusterNodeRoleEnum.getByDesc(esRoleCluster.getRole()).getCode());
            esRoleClusterHost.setStatus(ESClusterNodeStatusEnum.ONLINE.getCode());
            esRoleClusterHost.setNodeSet("");
            return esRoleClusterHost;
        }
    }

    private void setTaskDetailStatusToSucc(EcmTaskStatus status, EcmParamBase ecmParamBase) {
        EcmTaskDetail ecmTaskDetail = ecmTaskDetailManager.getByWorkOderIdAndHostName(ecmParamBase.getWorkOrderId(),
            status.getHostname());
        if (AriusObjUtils.isNull(ecmTaskDetail)) {
            LOGGER.error("class=EcmTaskManagerImpl||method=setTaskDetailStatusToSucc||clusterId={}||"
                         + "msg=the ecm task detail is empty, failed to update status",
                ecmParamBase.getPhyClusterId());
        }

        ecmTaskDetail.setStatus(SUCCESS.getValue());
        Result result = ecmTaskDetailManager.editEcmTaskDetail(ecmTaskDetail);
        if (result.failed()) {
            LOGGER.error("class=EcmTaskManagerImpl||method=setTaskDetailStatusToSucc||" + "clusterName{}||"
                         + "hostName||msg=failed to edit the ecm task detail",
                ecmParamBase.getPhyClusterName(), status.getHostname(), result.getMessage());
            status.setStatusEnum(RUNNING);
            return;
        }

        status.setStatusEnum(SUCCESS);
    }

    private boolean hasAddHostInDb(EcmTask ecmTask, List<EcmTaskStatus> statuses) {
        //全部节点运行成功后插入
        List<EcmHostStatusEnum> status = statuses.stream()
            .filter(r -> !AriusObjUtils.isNull(r) && SUCCESS.getValue().equals(r.getStatusEnum().getValue()))
            .map(EcmTaskStatus::getStatusEnum).collect(Collectors.toList());

        return (EcmTaskTypeEnum.NEW.getCode() == ecmTask.getOrderType()
                || EcmTaskTypeEnum.EXPAND.getCode() == ecmTask.getOrderType())
               && status.size() == statuses.size();
    }

    private EcmTaskDetail buildEcmTaskDetail(EcmTaskStatus status, Long detailId, EcmParamBase ecmParamBase,
                                             OperationEnum operation) {
        EcmTaskDetail ecmTaskDetail = new EcmTaskDetail();
        if (ADD.getCode() == operation.getCode()) {
            ecmTaskDetail.setWorkOrderTaskId(ecmParamBase.getWorkOrderId());
            ecmTaskDetail.setStatus(status.getStatusEnum().getValue());
            ecmTaskDetail.setHostname(status.getHostname());
            ecmTaskDetail.setRole(ecmParamBase.getRoleName());
            ecmTaskDetail.setGrp(status.getGroup());
            ecmTaskDetail.setIdx(status.getPodIndex());
            ecmTaskDetail.setTaskId(status.getTaskId().longValue());
        } else if (EDIT.getCode() == operation.getCode()) {
            ecmTaskDetail.setId(detailId);
            ecmTaskDetail.setStatus(status.getStatusEnum().getValue());
            ecmTaskDetail.setGrp(status.getGroup());
            ecmTaskDetail.setIdx(status.getPodIndex());
        }
        return ecmTaskDetail;
    }

    private void rebuildRoleClusterHost(EcmTaskStatus status, ESRoleClusterHost deleteHost,
                                        ESRoleCluster esRoleCluster) {
        deleteHost.setRole(ESClusterNodeRoleEnum.getByDesc(esRoleCluster.getRole()).getCode());
        deleteHost.setRoleClusterId(esRoleCluster.getId());
        deleteHost.setIp(status.getPodIp());
    }

    private String buildAddressIpString(List<ESRoleClusterHost> hosts) {
        Set<String> ips = Sets.newHashSet();
        hosts.stream().map(ESRoleClusterHost::getIp).forEach(ip -> ips.add(ip + ":" + esClusterClientPort));
        return ListUtils.strSet2String(ips);
    }

    private boolean hasCallBackRWAddress(EcmTaskStatusEnum mergedStatusEnum, EcmTask ecmTask) {
        return SUCCESS.getValue().equals(mergedStatusEnum.getValue())
               && (EcmTaskTypeEnum.NEW.getCode() == ecmTask.getOrderType()
                   || EcmTaskTypeEnum.EXPAND.getCode() == ecmTask.getOrderType()
                   || EcmTaskTypeEnum.SHRINK.getCode() == ecmTask.getOrderType());
    }

    private void updateRoleClusterNumber(EcmTask ecmTask, EcmParamBase ecmParamBase) {
        if (hasCallBackRoleNumber(ecmTask)) {
            ESRoleCluster esRoleCluster = ConvertUtil.obj2Obj(ecmParamBase, ESRoleCluster.class);
            esRoleCluster.setElasticClusterId(ecmParamBase.getPhyClusterId());
            esRoleCluster.setPodNumber(ecmParamBase.getNodeNumber());
            esRoleCluster.setRole(ecmParamBase.getRoleName());
            Result updateResult = esRoleClusterService.updatePodByClusterIdAndRole(esRoleCluster);
            if (updateResult.failed()) {
                LOGGER.error("class=EcmTaskManagerImpl||method=updateRoleClusterNumber||clusterId={}"
                             + "||msg=failed to update es role number",
                    ecmTask.getPhysicClusterId());
            }
        }
    }

    private boolean hasCallBackRoleNumber(EcmTask ecmTask) {
        return EcmTaskTypeEnum.EXPAND.getCode() == ecmTask.getOrderType()
               || EcmTaskTypeEnum.SHRINK.getCode() == ecmTask.getOrderType();
    }

    private EcmTaskStatusEnum convertStatus(EcmHostStatusEnum ecmHostStatusEnum) {
        if (EcmHostStatusEnum.SUCCESS.equals(ecmHostStatusEnum)) {
            return EcmTaskStatusEnum.SUCCESS;
        }
        if (EcmHostStatusEnum.UPDATED.equals(ecmHostStatusEnum)) {
            return EcmTaskStatusEnum.SUCCESS;
        }
        if (EcmHostStatusEnum.KILL_FAILED.equals(ecmHostStatusEnum)
            || EcmHostStatusEnum.TIMEOUT.equals(ecmHostStatusEnum)
            || EcmHostStatusEnum.FAILED.equals(ecmHostStatusEnum)) {
            return EcmTaskStatusEnum.FAILED;
        }
        if (EcmHostStatusEnum.KILLING.equals(ecmHostStatusEnum)
            || EcmHostStatusEnum.RUNNING.equals(ecmHostStatusEnum)) {
            return EcmTaskStatusEnum.RUNNING;
        }
        if (EcmHostStatusEnum.WAITING.equals(ecmHostStatusEnum)) {
            return EcmTaskStatusEnum.WAITING;
        }
        if (EcmHostStatusEnum.READY.equals(ecmHostStatusEnum)) {
            return EcmTaskStatusEnum.PAUSE;
        }
        if (EcmHostStatusEnum.IGNORE.equals(ecmHostStatusEnum)) {
            return EcmTaskStatusEnum.IGNORE;
        }
        if (EcmHostStatusEnum.CANCELLED.equals(ecmHostStatusEnum)) {
            return EcmTaskStatusEnum.CANCEL;
        }
        return EcmTaskStatusEnum.UNKNOWN;
    }

    private boolean checkEcmTaskStatusValid(List<EcmTaskStatus> ecmTaskStatuses) {
        for (EcmTaskStatus status : ecmTaskStatuses) {
            if (AriusObjUtils.isBlack(status.getPodIp()) && AriusObjUtils.isBlack(status.getHostname())) {
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }
}
