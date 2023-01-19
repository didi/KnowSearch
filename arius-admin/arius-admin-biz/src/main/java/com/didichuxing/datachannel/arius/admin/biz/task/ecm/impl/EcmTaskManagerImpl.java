package com.didichuxing.datachannel.arius.admin.biz.task.ecm.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmHostStatusEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskStatusEnum.CANCEL;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.CLIENT_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_DOCKER;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_HOST;
import static com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.ZeusClusterActionEnum.EXPAND;
import static com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.ZeusClusterActionEnum.SHRINK;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.task.ecm.EcmTaskDetailManager;
import com.didichuxing.datachannel.arius.admin.biz.task.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.WorkOrderManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.OpOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmTaskBasic;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostCreateActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostScaleActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.BaseClusterHostOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.ecm.EcmTaskPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.detail.OrderDetailBaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmHostStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.event.ecm.EcmTaskEditEvent;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterPhyHealthEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.exception.EcmRemoteException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusScheduleThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.task.EcmTaskDAO;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.EcmActionEnum;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.NoArgsConstructor;

/**
 * ES工单任务管理
 * @author didi
 * @since 2020-08-24
 */
@Service
@NoArgsConstructor
public class EcmTaskManagerImpl implements EcmTaskManager {
    private static final ILog LOGGER                = LogFactory.getLog(EcmTaskManagerImpl.class);
    public static final long        DEFAULT_WORK_ORDER_ID = -1L;

    @Value("${es.client.cluster.port}")
    private String                  esClusterClientPort;

    @Autowired
    private EcmTaskDAO              ecmTaskDao;

    @Autowired
    private EcmHandleService        ecmHandleService;

    @Autowired
    private ClusterPhyService       clusterPhyService;

    @Autowired
    private ClusterPhyManager       clusterPhyManager;

    @Autowired
    private ClusterRoleService      clusterRoleService;

    @Autowired
    private ClusterRoleHostService  clusterRoleHostService;

    @Autowired
    private EcmTaskDetailManager    ecmTaskDetailManager;

    @Autowired
    private ESClusterService        esClusterService;

    @Autowired
    private AriusScheduleThreadPool ariusScheduleThreadPool;

    @Autowired
    private WorkOrderManager        workOrderManager;
    @Autowired
    private ProjectService       projectService;
    @Autowired
    private OperateRecordService operateRecordService;

    @Override
    public boolean existUnClosedEcmTask(Long phyClusterId) {
        List<EcmTaskPO> notFinishedTasks = ecmTaskDao.listUndoEcmTaskByClusterId(phyClusterId);
        return !AriusObjUtils.isEmptyList(notFinishedTasks);
    }

    @Override
    public Result<Long> saveEcmTask(EcmTaskDTO ecmTaskDTO) {
        if (CollectionUtils.isEmpty(ecmTaskDTO.getEcmParamBaseList())) {
            return Result.buildFail("工单数据为空");
        }
        //过滤掉nodeNumber 为0的数据
        List<EcmParamBase> filteredEcmParamBaseList = ecmTaskDTO.getEcmParamBaseList().stream()
            .filter(elem -> elem.getNodeNumber() != null && elem.getNodeNumber() > 0).collect(Collectors.toList());

        EcmTaskPO ecmTaskPO = ConvertUtil.obj2Obj(ecmTaskDTO, EcmTaskPO.class);
        ecmTaskPO.setClusterNodeRole(ListUtils.strList2String(
            filteredEcmParamBaseList.stream().map(EcmParamBase::getRoleName).collect(Collectors.toList())));
        ecmTaskPO.setHandleData(ConvertUtil.obj2Json(filteredEcmParamBaseList));
        //默认状态都是 待执行
        ecmTaskPO.setStatus(EcmTaskStatusEnum.WAITING.getValue());
        //设置默认的工单ID
        if (null == ecmTaskPO.getWorkOrderId()) {
            ecmTaskPO.setWorkOrderId(DEFAULT_WORK_ORDER_ID);
        }
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
    public List<EcmTask> listRunningEcmTask() {
        return ConvertUtil.list2List(ecmTaskDao.listRunningTasks(), EcmTask.class);
    }

    @Override
    public Result<EcmTaskBasic> getEcmTaskBasicByTaskId(Long taskId) {
        EcmTaskPO ecmTaskPO = ecmTaskDao.getById(taskId);
        if (ecmTaskPO == null) {
            return Result.buildFail("任务不存在");
        }
        EcmTask ecmTask = ConvertUtil.obj2Obj(ecmTaskPO, EcmTask.class);
        EcmTaskBasic ecmTaskBasic = ConvertUtil.obj2Obj(ecmTaskPO, EcmTaskBasic.class);

        if (Objects.equals(OpTaskTypeEnum.CLUSTER_NEW.getType(), ecmTaskPO.getOrderType())
            && ESClusterTypeEnum.ES_HOST.getCode() == ecmTaskBasic.getType()) {
            // 集群新建的工单, 该信息从参数中获取
            Map<String, EcmParamBase> ecmParamBaseMap = OpOrderTaskConverter.convert2EcmParamBaseMap(ecmTask);
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
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(ecmTaskPO.getPhysicClusterId().intValue());
        if (clusterPhy != null) {
            ecmTaskBasic.setClusterName(clusterPhy.getCluster());
            ecmTaskBasic.setIdc(clusterPhy.getIdc());
            ecmTaskBasic.setNsTree(clusterPhy.getNsTree());
            ecmTaskBasic.setDesc(clusterPhy.getDesc());
            ecmTaskBasic.setEsVersion(clusterPhy.getEsVersion());
            ecmTaskBasic.setImageName(clusterPhy.getImageName());
        }

        // 获取任务的创建时间和更新时间
        ecmTaskBasic.setCreateTime(ecmTaskPO.getCreateTime());
        List<EcmTaskDetail> ecmTaskDetails = ecmTaskDetailManager.getEcmTaskDetailInOrder(taskId);
        Optional<Date> optionalDate = ecmTaskDetails.stream().map(EcmTaskDetail::getUpdateTime).distinct()
            .max(Date::compareTo);
        optionalDate.ifPresent(ecmTaskBasic::setUpdateTime);

        return Result.buildSucc(ecmTaskBasic);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<EcmOperateAppBase> savaAndActionEcmTask(Long taskId, String operator) throws EcmRemoteException {
        //1. 校验ECM任务有效性
        EcmTask ecmTask = getEcmTask(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildFail("Ecm任务不存在");
        }
        if (EcmTaskStatusEnum.RUNNING.getValue().equals(ecmTask.getStatus())) {
            return Result.buildFail("任务正在执行中, 请勿重复操作");
        }
        List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        if (CollectionUtils.isEmpty(ecmParamBaseList)) {
            return Result.buildFail("转化工单数据失败");
        }

        //2. 创建ES物理集群信息
        Result<Long> saveResult = ecmHandleService.saveESCluster(ecmParamBaseList);
        if (saveResult.failed()) {
            return Result.buildFrom(saveResult);
        }

        //3. 回写ES集群Id到到ECMTask
        ecmTask.setPhysicClusterId(saveResult.getData());
        ecmTask.setHandleData(ConvertUtil.obj2Json(ecmParamBaseList));
        updateEcmTask(ecmTask);

        //4. 启动任务, 运行新建master节点的ECM任务
        Result<EcmOperateAppBase> actionRet = actionEcmTaskForMasterNode(ecmParamBaseList, taskId, operator);
        if (actionRet.failed()) {
            //用于回滚物理集群保留信息
            throw new EcmRemoteException(actionRet.getMessage());
        }

        return actionRet;
    }

    @Override
    public Result<Void> retryClusterEcmTask(Long taskId, String operator) {
        //1. 校验ECM任务有效性
        EcmTask ecmTask = getEcmTask(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildFail("Ecm任务不存在");
        }
        List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        if (CollectionUtils.isEmpty(ecmParamBaseList)) {
            return Result.buildFail("转化工单数据失败");
        }

        //2.将角色列表中的zeus任务id设置为空
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            HostParamBase hostParamBase = (HostParamBase) ecmParamBase;
            hostParamBase.setTaskId(null);
        }
        ecmTask.setHandleData(JSONArray.toJSONString(ecmParamBaseList));

        //3.删除task_detail表中的数据
        ecmTaskDetailManager.deleteEcmTaskDetailsByTaskOrder(taskId);

        //4.将arius_work_task和es_work_order_task中对应任务的状态设置为waiting
        ecmTask.setStatus(EcmTaskStatusEnum.WAITING.getValue());
        updateEcmTask(ecmTask);

        return Result.buildSucc();
    }

    @Override
    public Result<EcmOperateAppBase> actionClusterEcmTask(Long taskId, String operator) throws EcmRemoteException {
        //1. 校验ECM任务有效性
        EcmTask ecmTask = getEcmTask(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildFail("Ecm任务不存在");
        }

        if (EcmTaskStatusEnum.RUNNING.getValue().equals(ecmTask.getStatus())) {
            return Result.buildParamIllegal("任务正在执行中, 请勿重复操作");
        }

        List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        if (CollectionUtils.isEmpty(ecmParamBaseList)) {
            return Result.buildFail("转化工单数据失败");
        }

        //2. 枚举处理每个角色任务信息
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            //2.1 过滤已运行任务
            if (!AriusObjUtils.isNull(ecmParamBase.getTaskId())
                && isTaskActed(EcmTaskStatusEnum.SUCCESS, ecmParamBase, ecmTask.getOrderType(), operator)) {
                // 当前任务已经触发执行 & 任务已经完成
                continue;
            } else if (!AriusObjUtils.isNull(ecmParamBase.getTaskId())
                       && !isTaskActed(EcmTaskStatusEnum.SUCCESS, ecmParamBase, ecmTask.getOrderType(), operator)) {
                // 当前任务已经触发执行 & 任务未完成, 此时直接return成功
                return Result.buildSucc();
            }

            //2.2 运行ECM任务
            Result<EcmOperateAppBase> ret = runEcmTask(ecmParamBase, ecmTask.getOrderType(), operator);
            if (ret.failed()) {
                throw new EcmRemoteException(ret.getMessage());
            }

            //回写taskId至DB
            ecmParamBase.setTaskId(ret.getData().getTaskId());
            ecmTask.setStatus(EcmTaskStatusEnum.RUNNING.getValue());
            ecmTask.setHandleData(ConvertUtil.obj2Json(ecmParamBaseList));
            updateEcmTask(ecmTask);

            return Result.buildSucc(ret.getData());
        }

        return Result.buildSucc();
    }

    @Override
    public Result<EcmOperateAppBase> actionClusterEcmTask(Long taskId, EcmActionEnum ecmActionEnum, String hostname,
                                                          String operator) throws EcmRemoteException {
        EcmTaskPO ecmTask = ecmTaskDao.getById(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildParamIllegal("集群任务不存在");
        }

        //接口幂等判断
        if (EcmTaskStatusEnum.RUNNING.getValue().equals(ecmTask.getStatus())) {
            return Result.buildFail("当前集群任务正在执行, 请勿重复操作");
        }

        List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter
            .convert2EcmParamBaseList(ConvertUtil.obj2Obj(ecmTask, EcmTask.class));
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            if (!AriusObjUtils.isNull(ecmParamBase.getTaskId())
                && isTaskActed(EcmTaskStatusEnum.SUCCESS, ecmParamBase, ecmTask.getOrderType(), operator)) {
                // 当前任务已经触发执行 & 任务已经完成
                continue;
            } else if (!AriusObjUtils.isNull(ecmParamBase.getTaskId())
                       && !isTaskActed(EcmTaskStatusEnum.SUCCESS, ecmParamBase, ecmTask.getOrderType(), operator)) {
                // 当前任务已经触发执行 & 任务未执行完成
                // 更新ecm任务状态从pause变为running
                ecmTask.setStatus(EcmTaskStatusEnum.RUNNING.getValue());
                updateEcmTask(ConvertUtil.obj2Obj(ecmTask, EcmTask.class));
                return ecmHandleService.actionUnfinishedESCluster(ecmActionEnum, ecmParamBase, hostname, operator);
            }

            // 任务未触发
            if (EcmActionEnum.START.equals(ecmActionEnum)) {
                // 下一个任务未触发执行的情况下, 收到continue之后, 则继续执行后续动作
                return actionClusterEcmTask(taskId, operator);
            }

            // 其他情况直接返回操作失败
            return Result.buildFail("任务已处于暂停状态, 操作无效");
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Void> cancelClusterEcmTask(Long taskId, String operator) {
        EcmTask ecmTask = getEcmTask(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildParamIllegal("集群任务不存在");
        }

        List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter
            .convert2EcmParamBaseList(ConvertUtil.obj2Obj(ecmTask, EcmTask.class));
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            if (!AriusObjUtils.isNull(ecmParamBase) && AriusObjUtils.isNull(ecmParamBase.getTaskId())) {
                // 将没有进行创建zeus任务的以cancel状态插入到task_detail表中
                saveTaskDetailInfoWithoutZeusTaskId(ecmParamBase, taskId, CANCEL);
            } else if (!AriusObjUtils.isNull(ecmParamBase) && !AriusObjUtils.isNull(ecmParamBase.getTaskId())) {
                ecmHandleService.actionUnfinishedESCluster(EcmActionEnum.CANCEL, ecmParamBase, null, operator);
            }
        }

        //修改工单任务
        ecmTask.setStatus(EcmTaskStatusEnum.CANCEL.getValue());
        updateEcmTask(ecmTask);
        return Result.buildSucc();
    }

    @Override
    public Result<Void> pauseClusterEcmTask(Long taskId, String operator) {
        EcmTaskPO ecmTask = ecmTaskDao.getById(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildParamIllegal("集群任务不存在");
        }

        //接口幂等判断
        if (!EcmTaskStatusEnum.RUNNING.getValue().equals(ecmTask.getStatus())) {
            return Result.buildFail("当前集群任务并非处于running状态, 无法进行暂停操作");
        }

        List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter
            .convert2EcmParamBaseList(ConvertUtil.obj2Obj(ecmTask, EcmTask.class));
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            if (!AriusObjUtils.isNull(ecmParamBase)
                && isTaskActed(EcmTaskStatusEnum.RUNNING, ecmParamBase, ecmTask.getOrderType(), operator)) {
                ecmHandleService.actionUnfinishedESCluster(EcmActionEnum.PAUSE, ecmParamBase, null, operator);
            }
        }

        // 任务未触发，不做任何操作，将任务的状态设置为暂停
        ecmTask.setStatus(EcmTaskStatusEnum.PAUSE.getValue());
        updateEcmTask(ConvertUtil.obj2Obj(ecmTask, EcmTask.class));
        return Result.buildSucc();
    }

    @Override
    public EcmTask getEcmTask(Long id) {
        return ConvertUtil.obj2Obj(ecmTaskDao.getById(id), EcmTask.class);
    }

    @Override
    public boolean updateEcmTask(EcmTask ecmTask) {
        int ret = ecmTaskDao.update(ConvertUtil.obj2Obj(ecmTask, EcmTaskPO.class));
        if (ret > 0) {
            SpringTool.publish(new EcmTaskEditEvent(this, ecmTask));
        }
        return ret > 0;
    }

    @Override
    public EcmTaskPO getRunningEcmTaskByClusterId(Integer physicClusterId) {
        return ecmTaskDao.getUsefulEcmTaskByClusterId(physicClusterId);
    }

    @Override
    public Result<Void> actionClusterHostEcmTask(Long taskId, EcmActionEnum ecmActionEnum, String hostname,
                                                 String operator) {
        EcmTaskPO ecmTask = ecmTaskDao.getById(taskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildParamIllegal("集群任务不存在");
        }

        List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter
            .convert2EcmParamBaseList(ConvertUtil.obj2Obj(ecmTask, EcmTask.class));
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            if (!AriusObjUtils.isNull(ecmParamBase)) {
                ecmHandleService.actionUnfinishedESCluster(ecmActionEnum, ecmParamBase, hostname, operator);
            }
        }

        // 将集群任务设置为RUNNING状态
        ecmTask.setStatus(EcmTaskStatusEnum.RUNNING.getValue());
        updateEcmTask(ConvertUtil.obj2Obj(ecmTask, EcmTask.class));
        return Result.buildSucc();
    }

    @Override
    public Result<EcmTask> getUsefulEcmTaskByClusterName(String clusterName) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterName);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail("物理名称对应的物理信息不存在");
        }
        return Result
            .buildSucc(ConvertUtil.obj2Obj(ecmTaskDao.getUsefulEcmTaskByClusterId(clusterPhy.getId()), EcmTask.class));
    }

    @Override
    public Result<String> getEcmTaskOrderDetailInfo(String cluster) {
        if (AriusObjUtils.isBlack(cluster)) {
            return Result.buildFail("cluster name 为空");
        }

        Result<EcmTask> usefulWorkOrderTaskByClusterName = getUsefulEcmTaskByClusterName(cluster);
        if (usefulWorkOrderTaskByClusterName.failed()) {
            return Result.buildFail("无法获取物理集群信息");
        }
        EcmTask task = usefulWorkOrderTaskByClusterName.getData();
        if (AriusObjUtils.isNull(task)) {
            return Result.buildFail("当前集群没有待执行的工单任务");
        }

        List<EcmParamBase> ecmParamBases = OpOrderTaskConverter.convert2EcmParamBaseList(task);
        if (CollectionUtils.isEmpty(ecmParamBases)) {
            return Result.buildFail("当前任务没有工单数据");
        }
        OrderDetailBaseVO orderDetailBaseVO = workOrderManager.getById(task.getWorkOrderId()).getData();

        return Result.buildSucc(orderDetailBaseVO.getDetail(), "ecm任务对应的工单任务详细信息");
    }

    @Override
    public EcmTaskStatusEnum refreshEcmTask(EcmTask ecmTask) throws AdminTaskException {
        if ((SUCCESS.getValue().equals(ecmTask.getStatus()) || CANCEL.getValue().equals(ecmTask.getStatus()))) {
            return EcmTaskStatusEnum.SUCCESS;
        }

        List<EcmParamBase> ecmParamBases = OpOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        ecmParamBases.forEach(ecmParam -> ecmParam.setWorkOrderId(ecmTask.getId()));
        Set<EcmTaskStatusEnum> subOrderTaskStatus = Sets.newHashSet();

        long startTime = System.currentTimeMillis();
        ecmParamBases.forEach(ecmParam -> subOrderTaskStatus.add(doRefreshEcmTask(ecmParam, ecmTask)));
        LOGGER.info(
            "class=EcmTaskManagerImpl||method=refreshEcmTask||clusterId={}" + "||orderType={}||consumingTime={}",
            ecmTask.getPhysicClusterId(), ecmTask.getOrderType(), System.currentTimeMillis() - startTime);

        EcmTaskStatusEnum mergedStatusEnum = EcmTaskStatusEnum.calTaskStatus(subOrderTaskStatus);

        if (postProcess(ecmTask, mergedStatusEnum).failed()) {
            mergedStatusEnum = EcmTaskStatusEnum.FAILED;
        }

        ecmTask.setStatus(mergedStatusEnum.getValue());
        updateEcmTask(ecmTask);
        return mergedStatusEnum;
    }

    /*************************************** private method ***************************************/
    /**
     * 灰度启动master角色的ES实例
     * @param ecmParamBaseList    ES角色列表
     * @param taskId              任务Id
     * @param operator            操作人
     * @return
     */
    private Result<EcmOperateAppBase> actionEcmTaskForMasterNode(List<EcmParamBase> ecmParamBaseList, Long taskId,
                                                                 String operator) {
        EcmTask ecmTask = getEcmTask(taskId);
        if (null == ecmTask) {
            return Result.buildFail("ECM任务为空");
        }
        Map<String, EcmParamBase> role2EcmParamBaseMap = ConvertUtil.list2Map(ecmParamBaseList,
            EcmParamBase::getRoleName, ecmParamBase -> ecmParamBase);

        EcmParamBase ecmParamBase = role2EcmParamBaseMap.get(MASTER_NODE.getDesc());

        Result<EcmOperateAppBase> runEcmTaskForMasterNodeRet = runEcmTask(ecmParamBase, ecmTask.getOrderType(),
            operator);
        if (runEcmTaskForMasterNodeRet.success()) {
            //回写taskId至DB
            ecmParamBase.setTaskId(runEcmTaskForMasterNodeRet.getData().getTaskId());
            ecmTask.setStatus(EcmTaskStatusEnum.RUNNING.getValue());
            ecmTask.setHandleData(ConvertUtil.obj2Json(ecmParamBaseList));
            updateEcmTask(ecmTask);

            //更新es role cluster note数量
            updateRoleClusterNumber(ecmTask, ecmParamBase);
            return Result.buildSucc(runEcmTaskForMasterNodeRet.getData());
        }

        return Result.buildFail();
    }

    private Result<EcmOperateAppBase> runEcmTask(EcmParamBase ecmParamBase, Integer orderType, String operator) {
        Result<EcmOperateAppBase> result;
        if (Objects.equals(OpTaskTypeEnum.CLUSTER_NEW.getType(), orderType)) {
            result = ecmHandleService.startESCluster(ecmParamBase, operator);
        } else if (Objects.equals(OpTaskTypeEnum.CLUSTER_EXPAND.getType(), orderType)) {
            if (ecmParamBase instanceof HostScaleActionParam) {
                HostScaleActionParam hostScaleActionParam = (HostScaleActionParam) ecmParamBase;
                hostScaleActionParam.setAction(EXPAND.getValue());
            }

            result = ecmHandleService.scaleESCluster(ecmParamBase, operator);
        } else if (Objects.equals(OpTaskTypeEnum.CLUSTER_SHRINK.getType(), orderType)) {
            if (ecmParamBase instanceof HostScaleActionParam) {
                HostScaleActionParam hostScaleActionParam = (HostScaleActionParam) ecmParamBase;
                hostScaleActionParam.setAction(SHRINK.getValue());
            }

            result = ecmHandleService.scaleESCluster(ecmParamBase, operator);
        } else if (Objects.equals(OpTaskTypeEnum.CLUSTER_RESTART.getType(), orderType)) {
            result = ecmHandleService.restartESCluster(ecmParamBase, operator);
        } else if (Objects.equals(OpTaskTypeEnum.CLUSTER_UPGRADE.getType(), orderType)) {
            result = ecmHandleService.upgradeESCluster(ecmParamBase, operator);
        } else {
            return Result.buildFail("任务类型未知, 类型Code:" + orderType);
        }
        return result;
    }

    /**任务执行过程进行EcmTask任务相关信息回写操作*/
    private EcmTaskStatusEnum doRefreshEcmTask(EcmParamBase ecmParam, EcmTask ecmTask) {
        if (AriusObjUtils.isNull(ecmParam.getTaskId())) {
            return EcmTaskStatusEnum.PAUSE;
        }
        Result<List<EcmTaskStatus>> taskStatus;
        List<EcmTaskStatus> remoteStatuses;
        try {
            //1.获取状态
            taskStatus = ecmHandleService.getESClusterStatus(ecmParam, ecmTask.getOrderType(), null);
            if (taskStatus.failed()) {
                return EcmTaskStatusEnum.FAILED;
            }

            remoteStatuses = taskStatus.getData();
            if (CollectionUtils.isEmpty(remoteStatuses)) {
                return EcmTaskStatusEnum.SUCCESS;
            }

            if (!checkEcmTaskStatusValid(remoteStatuses)) {
                return EcmTaskStatusEnum.RUNNING;
            }
            //2.更新taskDetail表
            updateTaskDetailByTaskStatus(ecmParam, remoteStatuses);

        } catch (Exception e) {
            LOGGER.error("class=EcmTaskManagerImpl||method=doRefreshEcmTask||ecmTaskId={}||msg={}", ecmTask.getId(),
                e.getStackTrace());
            return EcmTaskStatusEnum.FAILED;
        }

        //5.计算最终状态
        Set<EcmTaskStatusEnum> ecmHostStatus = remoteStatuses.stream().map(r -> convertStatus(r.getStatusEnum()))
            .collect(Collectors.toSet());
        return EcmTaskStatusEnum.calTaskStatus(ecmHostStatus);
    }

    /**检查执行的任务是否处于指定的状态*/
    private boolean isTaskActed(EcmTaskStatusEnum ecmTaskStatusEnum, EcmParamBase ecmParamBase, Integer orderType,
                                String operator) {
        Result<List<EcmTaskStatus>> result = ecmHandleService.getESClusterStatus(ecmParamBase, orderType, operator);
        if (result.failed()) {
            // 获取任务状态失败, 则直接返回false
            return false;
        }

        Set<EcmTaskStatusEnum> statusEnumSet = new HashSet<>();
        for (EcmTaskStatus ecmTaskStatus : result.getData()) {
            statusEnumSet.add(convertStatus(ecmTaskStatus.getStatusEnum()));
        }

        EcmTaskStatusEnum getEcmTaskStatusEnum = EcmTaskStatusEnum.calTaskStatus(statusEnumSet);
        return ecmTaskStatusEnum.equals(getEcmTaskStatusEnum);
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

    /**
     * 任务完成后续回写更新操作
     * @param ecmTask             ECM任务
     * @param mergedStatusEnum    任务状态
     * @return
     */
    private Result<Void> postProcess(EcmTask ecmTask, EcmTaskStatusEnum mergedStatusEnum) throws AdminTaskException {
        if (!SUCCESS.getValue().equals(mergedStatusEnum.getValue()) && !hasRemoteTaskFailed(mergedStatusEnum)) {
            return Result.buildSucc();
        }

        List<EcmParamBase> ecmParamBases = OpOrderTaskConverter.convert2EcmParamBaseList(ecmTask);

        //1. 任务失败, 清理集群role表数据、data_source表数据
        //cleanUpUselessClusterInfoFromDB(mergedStatusEnum, ecmParamBases);
        if (hasRemoteTaskFailed(mergedStatusEnum)) {
            return Result.buildSucc();
        }

        //2. 更新集群读写地址, 其中重启，升级 不需要更新读写地址
        if (updateClusterAddressWhenIsValid(ecmTask, mergedStatusEnum, ecmParamBases).failed()) {
            return Result.buildFail();
        }

        //3.根据ecm任务中内容插入es_cluster_role_host_info表中
        saveOrEditHostInfoFromEcmTask(ecmTask, mergedStatusEnum);

        //4. 设置30s的延迟时间，采集节点数据入集群host表中
        delayCollectNodeSettingsTask(ecmParamBases);

        //5. 升级, 更新集群版本
        updateEsClusterVersion(mergedStatusEnum, ecmTask);

        return Result.buildSucc();
    }

    /**
     * 集群的新建，扩容，缩容任务需要修改es_role_host表信息
     * @param ecmTask ecm任务
     * @param mergedStatusEnum 总的任务执行情况
     */
    private void saveOrEditHostInfoFromEcmTask(EcmTask ecmTask,
                                               EcmTaskStatusEnum mergedStatusEnum) throws AdminTaskException {
        if (!EcmTaskStatusEnum.SUCCESS.equals(mergedStatusEnum)) {
            return;
        }

        switch (OpTaskTypeEnum.valueOfType(ecmTask.getOrderType())) {
            case CLUSTER_EXPAND:
            case CLUSTER_NEW:
                addHostInfoFromTaskOrder(ecmTask);
                break;
            case CLUSTER_SHRINK:
                deleteRoleClusterAndHost(mergedStatusEnum, ecmTask);
                break;
            default:
                break;
        }
    }

    private void addHostInfoFromTaskOrder(EcmTask ecmTask) throws AdminTaskException {
        // 从ecm任务的工单中获取节点全量的信息
        Result<OrderDetailBaseVO> getOrderDetailResult = workOrderManager.getById(ecmTask.getWorkOrderId());
        if (getOrderDetailResult.failed()) {
            return;
        }
        BaseClusterHostOrderDetail baseClusterHostOrderDetail = (JSONObject
            .parseObject(getOrderDetailResult.getData().getDetail(), BaseClusterHostOrderDetail.class));

        // 保存全量节点信息到DB
        clusterRoleHostService.createClusterNodeSettings(baseClusterHostOrderDetail.getRoleClusterHosts(),
            baseClusterHostOrderDetail.getPhyClusterName());

        // 更新es_cluster_role_info中的podNumber
        for (EcmParamBase ecmParamBase : OpOrderTaskConverter.convert2EcmParamBaseList(ecmTask)) {
            HostParamBase hostParamBase = (HostParamBase) ecmParamBase;
            if (CollectionUtils.isEmpty(hostParamBase.getHostList())) {
                continue;
            }

            ClusterRoleInfo clusterRoleInfo = clusterRoleService
                .getByClusterNameAndRole(baseClusterHostOrderDetail.getPhyClusterName(), hostParamBase.getRoleName());
            if (clusterRoleInfo == null) {
                continue;
            }

            updatePodNumbers(ecmTask, clusterRoleInfo);
        }
    }

    private Result<Void> updateClusterAddressWhenIsValid(EcmTask ecmTask, EcmTaskStatusEnum mergedStatusEnum,
                                                         List<EcmParamBase> ecmParamBases) {
        if (!hasCallBackAddress(mergedStatusEnum, ecmTask)) {
            return Result.buildSucc();
        }
        try {
            boolean succ = ESOpTimeoutRetry.esRetryExecuteWithGivenTime("集群读写地址有效性检测",
                ClusterConstant.DEFAULT_RETRY_TIMES, () -> hasValidEsClusterReadAndWriteAddress(ecmTask, ecmParamBases),
                ClusterConstant::defaultRetryTime);
            if (succ) {
                updateClusterReadAndWriteAddress(ecmTask, ecmParamBases);
                // 物理集群读写地址更新完毕之后进行es-client的刷新
                SpringTool.publish(new ClusterPhyHealthEvent(this, getClusterPhyNameFromEcmParamBases(ecmParamBases)));
                return Result.buildSucc();
            }
        } catch (Exception e) {
            LOGGER.error("class=EcmTaskManagerImpl||method=postProcess||errMsg={}", e.getMessage());
        }
        return Result.buildFail();
    }

    private void delayCollectNodeSettingsTask(List<EcmParamBase> ecmParamBases) {
        ariusScheduleThreadPool.submitScheduleAtFixedDelayTask(() -> {
            String clusterPhyName = getClusterPhyNameFromEcmParamBases(ecmParamBases);
            try {
                clusterRoleHostService.collectClusterNodeSettings(clusterPhyName);
            } catch (AdminTaskException e) {
                e.printStackTrace();
            }
        }, 30, 600);
    }

    private boolean hasValidEsClusterReadAndWriteAddress(EcmTask ecmTask, List<EcmParamBase> ecmParamBases) {
        List<String> clusterAddress = Lists.newArrayList();
        if (ES_DOCKER.getCode() == ecmTask.getType()) {
            //docker类型待开发
        } else if (ES_HOST.getCode() == ecmTask.getType()) {
            clusterAddress = buildClusterReadAndWriteAddressForHost(ecmTask, ecmParamBases);
        }

        return esClusterService.syncGetClientAlivePercent(getClusterPhyNameFromEcmParamBases(ecmParamBases), null,
            ListUtils.strList2String(clusterAddress)) > 0;
    }

    private String getClusterPhyNameFromEcmParamBases(List<EcmParamBase> ecmParamBases) {
        String clusterPhyName = null;
        for (EcmParamBase ecmParamBase : ecmParamBases) {
            if (StringUtils.isNotBlank(ecmParamBase.getPhyClusterName())) {
                clusterPhyName = ecmParamBase.getPhyClusterName();
            }
        }

        return clusterPhyName;
    }

    private void updateClusterReadAndWriteAddress(EcmTask ecmTask, List<EcmParamBase> ecmParamBases) {
        if (ES_DOCKER.getCode() == ecmTask.getType()) {
            //docker类型待开发
        } else if (ES_HOST.getCode() == ecmTask.getType()) {
            List<String> clusterPhyAddress = buildClusterReadAndWriteAddressForHost(ecmTask, ecmParamBases);
            if (CollectionUtils.isNotEmpty(clusterPhyAddress)) {
                ClusterPhyDTO esClusterDTO = new ClusterPhyDTO();
                esClusterDTO.setId(ecmTask.getPhysicClusterId().intValue());
                esClusterDTO.setHttpAddress(ListUtils.strList2String(clusterPhyAddress));
                esClusterDTO.setHttpWriteAddress(ListUtils.strList2String(clusterPhyAddress));
                clusterPhyManager.editCluster(esClusterDTO, AriusUser.SYSTEM.getDesc());
            }
        }
    }

    private List<String> buildClusterReadAndWriteAddressForHost(EcmTask ecmTask, List<EcmParamBase> ecmParamBases) {
        if (ecmTask.getOrderType().equals(OpTaskTypeEnum.CLUSTER_NEW.getType())) {
            return buildClusterReadAndWriteAddressForHostWhenCreate(ecmParamBases);
        }

        if (ecmTask.getOrderType().equals(OpTaskTypeEnum.CLUSTER_SHRINK.getType())
            || ecmTask.getOrderType().equals(OpTaskTypeEnum.CLUSTER_EXPAND.getType())) {
            return buildClusterReadAndWriteAddressForHostWhenScale(ecmTask.getOrderType(), ecmTask.getPhysicClusterId(),
                ecmParamBases);
        }

        if (ecmTask.getOrderType().equals(OpTaskTypeEnum.CLUSTER_RESTART.getType())) {
            return buildClusterReadAndWriteAddressForHostWhenRestart(ecmTask.getPhysicClusterId());
        }

        return new ArrayList<>();
    }

    private List<String> buildClusterReadAndWriteAddressForHostWhenRestart(Long physicClusterId) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(Math.toIntExact(physicClusterId));
        if (AriusObjUtils.isNull(clusterPhy) || AriusObjUtils.isNull(clusterPhy.getHttpAddress())) {
            return Lists.newArrayList();
        }

        // 获取的当前集群可以使用的http地址，作为es服务有效性检测的地址池子
        return ListUtils.string2StrList(clusterPhy.getHttpAddress());
    }

    private List<String> buildClusterReadAndWriteAddressForHostWhenCreate(List<EcmParamBase> ecmParamBases) {
        List<String> clusterPhyAddress = Lists.newArrayList();
        List<HostParamBase> hostParamBases = ConvertUtil.list2List(ecmParamBases, HostParamBase.class);
        List<HostParamBase> builds = hostParamBases.stream()
            .filter(hostParam -> filterValidHttpAddressEcmParamBase(CLIENT_NODE.getDesc(), hostParam))
            .collect(Collectors.toList());
        //没有client角色, 用master角色节点作为http读写地址
        if (CollectionUtils.isEmpty(builds)) {
            builds = hostParamBases.stream()
                .filter(hostParam -> filterValidHttpAddressEcmParamBase(MASTER_NODE.getDesc(), hostParam))
                .collect(Collectors.toList());
        }

        for (HostParamBase hostParamBase : builds) {
            List<String> hostList = hostParamBase.getHostList();
            hostList.forEach(host -> clusterPhyAddress.add(host + ":" + hostParamBase.getPort()));
        }

        return clusterPhyAddress;
    }

    private List<String> buildClusterReadAndWriteAddressForHostWhenScale(Integer orderType, Long physicClusterId,
                                                                         List<EcmParamBase> ecmParamBases) {
        // 获取集群原有的clientnode和masternode的地址和端口号
        List<String> clientHttpAddresses = getAddressesByByRoleAndClusterId(physicClusterId, CLIENT_NODE.getDesc());
        List<String> masterHttpAddresses = getAddressesByByRoleAndClusterId(physicClusterId, MASTER_NODE.getDesc());

        // 扩缩容的时候会在原始的角色地址列表中修改缩容的地址端口
        List<HostParamBase> hostParamBases = ConvertUtil.list2List(ecmParamBases, HostParamBase.class);
        for (HostParamBase hostParamBase : hostParamBases) {
            if (CollectionUtils.isEmpty(hostParamBase.getHostList())) {
                continue;
            }

            // 获取扩缩容中当前角色的地址端口列表
            List<String> shouldOperateAddresses = hostParamBase.getHostList().stream()
                .map(hostname -> hostname + ":" + hostParamBase.getPort()).collect(Collectors.toList());

            // 根据扩缩容和角色的类型对masternode和clientnode做对应删除增加操作
            if (hostParamBase.getRoleName().equals(CLIENT_NODE.getDesc())) {
                if (orderType.equals(OpTaskTypeEnum.CLUSTER_SHRINK.getType())) {
                    clientHttpAddresses.removeAll(shouldOperateAddresses);
                }

                if (orderType.equals(OpTaskTypeEnum.CLUSTER_EXPAND.getType())) {
                    clientHttpAddresses.addAll(shouldOperateAddresses);
                }
            }

            if (hostParamBase.getRoleName().equals(MASTER_NODE.getDesc())) {
                if (orderType.equals(OpTaskTypeEnum.CLUSTER_SHRINK.getType())) {
                    masterHttpAddresses.removeAll(shouldOperateAddresses);
                }

                if (orderType.equals(OpTaskTypeEnum.CLUSTER_EXPAND.getType())) {
                    masterHttpAddresses.addAll(shouldOperateAddresses);
                }
            }
        }

        // 如果client节点信息不为空，则使用client节点的ip地址, 否则使用matser节点信息
        if (!CollectionUtils.isEmpty(clientHttpAddresses)) {
            return clientHttpAddresses;
        } else {
            return masterHttpAddresses;
        }
    }

    private List<String> getAddressesByByRoleAndClusterId(Long clusterId, String role) {
        List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.getByRoleAndClusterId(clusterId, role);
        if (!CollectionUtils.isEmpty(clusterRoleHosts)) {
            return clusterRoleHosts.stream()
                .map(roleClusterHost -> roleClusterHost.getHostname() + ":" + roleClusterHost.getPort())
                .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    private boolean filterValidHttpAddressEcmParamBase(String role, HostParamBase hostParamBase) {
        if (null == role) {
            return false;
        }

        return role.equals(hostParamBase.getRoleName()) && CollectionUtils.isNotEmpty(hostParamBase.getHostList())
               && null != hostParamBase.getPort();
    }

    /**
     * 远程任务执行失败、取消、杀死判断为任务执行失败, 清理已插入集群Table的数据
     * @param mergedStatusEnum 远程任务状态
     * @return boolean
     */
    private boolean hasRemoteTaskFailed(EcmTaskStatusEnum mergedStatusEnum) {
        return FAILED.getValue().equals(mergedStatusEnum.getValue())
               || CANCELLED.getValue().equals(mergedStatusEnum.getValue())
               || KILL_FAILED.getValue().equals(mergedStatusEnum.getValue());
    }

    /**
     * 缩容操作，任务操作成功则硬删除对应集群角色的表数据
     *
     * @param mergedStatusEnum
     * @param ecmTask
     */
    private void deleteRoleClusterAndHost(EcmTaskStatusEnum mergedStatusEnum, EcmTask ecmTask) {
        for (EcmParamBase ecmParamBase : OpOrderTaskConverter.convert2EcmParamBaseList(ecmTask)) {
            HostParamBase hostParamBase = (HostParamBase) ecmParamBase;
            if (CollectionUtils.isEmpty(hostParamBase.getHostList())) {
                continue;
            }

            ClusterRoleInfo clusterRoleInfo = clusterRoleService
                .getByClusterNameAndRole(hostParamBase.getPhyClusterName(), hostParamBase.getRoleName());
            if (null == clusterRoleInfo) {
                continue;
            }

            // 删除es_cluster_role_host_info数据
            clusterRoleHostService.deleteByHostNameAndRoleId(hostParamBase.getHostList(), clusterRoleInfo.getId());

            // 更新es_cluster_role_info数据中pod的数量 角色节点数目小于角色缩容数目相同，则返回
            if (clusterRoleInfo.getPodNumber() < hostParamBase.getHostList().size()) {
                return;
            }

            // 更新es_cluster_role_info数据中pod的数量 角色节点数目大于角色缩容数目，则做数目的更新
            updatePodNumbers(ecmTask, clusterRoleInfo);
        }
    }

    private void updatePodNumbers(EcmTask ecmTask, ClusterRoleInfo clusterRoleInfo) {
        ClusterRoleInfo updateClusterRoleInfo = new ClusterRoleInfo();
        updateClusterRoleInfo.setElasticClusterId(ecmTask.getPhysicClusterId());
        updateClusterRoleInfo.setRole(clusterRoleInfo.getRole());
        updateClusterRoleInfo.setPodNumber(clusterRoleHostService.getPodNumberByRoleId(clusterRoleInfo.getId()));
        Result<Void> result = clusterRoleService.updatePodByClusterIdAndRole(updateClusterRoleInfo);
        if (result.failed()) {
            LOGGER.error("class=EcmTaskManagerImpl||method=deleteRoleCluster||clusterId={}||role={}"
                         + "msg=failed to update roleCluster",
                ecmTask.getPhysicClusterId(), clusterRoleInfo.getRole());
        }
    }

    /**
     * 升级操作，回写集群版本到集群和角色
     * @param mergedStatusEnum
     * @param ecmTask
     */
    private void updateEsClusterVersion(EcmTaskStatusEnum mergedStatusEnum, EcmTask ecmTask) {
        if (!SUCCESS.getValue().equals(mergedStatusEnum.getValue())) {
            return;
        }

        if (!Objects.equals(OpTaskTypeEnum.CLUSTER_UPGRADE.getType(), ecmTask.getOrderType())) {
            return;
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterById(ecmTask.getPhysicClusterId().intValue());
        if (AriusObjUtils.isNull(clusterPhy) || AriusObjUtils.isBlack(clusterPhy.getCluster())) {
            LOGGER.error("class=EcmTaskManagerImpl||method=callBackEsClusterVersion||clusterId={}||"
                         + "msg=the es cluster or the cluster name is empty",
                ecmTask.getPhysicClusterId());
            return;
        }

        List<EcmParamBase> ecmParamBases = OpOrderTaskConverter.convert2EcmParamBaseList(ecmTask);

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
            Result<Void> result = clusterRoleService.updateVersionByClusterIdAndRole(ecmTask.getPhysicClusterId(), role,
                tuple.getV2());
            if (null != result && result.failed()) {
                LOGGER.error(
                    "class=EcmTaskManagerImpl||method=callBackEsClusterVersion||clusterId={}||role={}||version={}"
                             + "msg=failed to edit role cluster",
                    ecmTask.getPhysicClusterId(), role, tuple.getV2());
            }
        }

        //2、更新集群的版本
        ClusterPhyDTO esClusterDTO = new ClusterPhyDTO();
        esClusterDTO.setId(ecmTask.getPhysicClusterId().intValue());
        esClusterDTO.setImageName(tuple.getV1());
        esClusterDTO.setEsVersion(tuple.getV2());
        Result<Boolean> result = clusterPhyService.editCluster(esClusterDTO, AriusUser.SYSTEM.getDesc());
        if (null != result && result.failed()) {
            LOGGER.error("class=EcmTaskManagerImpl||method=callBackEsClusterVersion||clusterId={}||"
                         + "msg=failed to edit cluster",
                ecmTask.getPhysicClusterId());
        }
    }

    private <T> Tuple<String, String> getImageAndVersion(List<EcmParamBase> ecmParamBases, Function<T, String> funImage,
                                                         Function<T, String> funVersion, Class<T> type) {
        List<T> params = ConvertUtil.list2List(ecmParamBases, type);
        String changeImageName = params.stream()
            .filter(r -> !AriusObjUtils.isNull(r) && !AriusObjUtils.isBlack(funImage.apply(r))).map(funImage).findAny()
            .orElse(null);

        String changeEsVersion = params.stream()
            .filter(r -> !AriusObjUtils.isNull(r) && !AriusObjUtils.isBlack(funVersion.apply(r))).map(funVersion)
            .findAny().orElse(null);

        return new Tuple<>(changeImageName, changeEsVersion);
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

    private boolean hasCallBackAddress(EcmTaskStatusEnum mergedStatusEnum, EcmTask ecmTask) {
        return SUCCESS.getValue().equals(mergedStatusEnum.getValue())
               && (Objects.equals(OpTaskTypeEnum.CLUSTER_NEW.getType(), ecmTask.getOrderType())
                   || Objects.equals(OpTaskTypeEnum.CLUSTER_EXPAND.getType(), ecmTask.getOrderType())
                   || Objects.equals(OpTaskTypeEnum.CLUSTER_SHRINK.getType(), ecmTask.getOrderType())
                   || Objects.equals(OpTaskTypeEnum.CLUSTER_RESTART.getType(), ecmTask.getOrderType()));
    }

    private void updateRoleClusterNumber(EcmTask ecmTask, EcmParamBase ecmParamBase) {
        if (hasCallBackRoleNumber(ecmTask)) {
            ClusterRoleInfo clusterRoleInfo = ConvertUtil.obj2Obj(ecmParamBase, ClusterRoleInfo.class);
            clusterRoleInfo.setElasticClusterId(ecmParamBase.getPhyClusterId());
            clusterRoleInfo.setPodNumber(ecmParamBase.getNodeNumber());
            clusterRoleInfo.setRole(ecmParamBase.getRoleName());
            Result<Void> updateResult = clusterRoleService.updatePodByClusterIdAndRole(clusterRoleInfo);
            if (updateResult.failed()) {
                LOGGER.error("class=EcmTaskManagerImpl||method=updateRoleClusterNumber||clusterId={}"
                             + "||msg=failed to update es role number",
                    ecmTask.getPhysicClusterId());
            }
        }
    }

    private boolean hasCallBackRoleNumber(EcmTask ecmTask) {
        return Objects.equals(OpTaskTypeEnum.CLUSTER_EXPAND.getType(), ecmTask.getOrderType())
               || Objects.equals(OpTaskTypeEnum.CLUSTER_SHRINK.getType(), ecmTask.getOrderType());
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

    private void saveTaskDetailInfoWithoutZeusTaskId(EcmParamBase ecmParamBase, Long taskId,
                                                     EcmTaskStatusEnum taskStatusEnum) {
        HostParamBase hostParamBase = (HostParamBase) ecmParamBase;
        for (String hostname : hostParamBase.getHostList()) {
            EcmTaskDetail ecmTaskDetail = new EcmTaskDetail();
            ecmTaskDetail.setWorkOrderTaskId(taskId);
            ecmTaskDetail.setStatus(taskStatusEnum.getValue());
            ecmTaskDetail.setHostname(hostname);
            ecmTaskDetail.setRole(ecmParamBase.getRoleName());
            ecmTaskDetail.setGrp(0);
            ecmTaskDetail.setIdx(0);
            ecmTaskDetail.setTaskId(0L);
            ecmTaskDetailManager.saveEcmTaskDetail(ecmTaskDetail);
        }
    }
}