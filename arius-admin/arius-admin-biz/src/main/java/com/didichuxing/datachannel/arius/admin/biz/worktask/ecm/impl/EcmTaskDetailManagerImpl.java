package com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.impl;

import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.WorkOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskDetailManager;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmTaskDetailProgress;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.ecm.EcmTaskDetailPO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.task.EcmTaskDetailDAO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *ES工单任务管理详情 服务实现类
 * @author didi
 * @since 2020-09-24
 */
@Service
public class EcmTaskDetailManagerImpl implements EcmTaskDetailManager {
    private static final Logger      LOGGER = LoggerFactory.getLogger( EcmTaskDetailManagerImpl.class);

    @Autowired
    private EcmTaskDetailDAO         ecmTaskDetailDAO;

    @Autowired
    private ClusterRoleService clusterRoleService;

    @Autowired
    private EcmTaskManager ecmTaskManager;

    @Autowired
    private EcmHandleService ecmHandleService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Override
    public int replace(EcmTaskDetail ecmTaskDetail) {
        return ecmTaskDetailDAO.replace(ConvertUtil.obj2Obj(ecmTaskDetail, EcmTaskDetailPO.class));
    }

    @Override
    public Result<Long> saveEcmTaskDetail(EcmTaskDetail esEcmTaskDetail) {
        EcmTaskDetailPO ecmTaskDetailPo = ConvertUtil.obj2Obj(esEcmTaskDetail, EcmTaskDetailPO.class);
        initEcmTaskDetailParam(ecmTaskDetailPo);
        boolean succ = (1 == ecmTaskDetailDAO.save(ecmTaskDetailPo));
        return Result.build(succ, ecmTaskDetailPo.getId());
    }

    @Override
    public Result<Integer> updateByRoleAndOrderTaskId(Long workOrdertaskId, String role, Long taskId) {
        boolean succ = (1 == ecmTaskDetailDAO.updateTaskIdByRoleAndWorkOrderTaskId(workOrdertaskId, role, taskId));
        return Result.build(succ);
    }

    @Override
    public List<EcmTaskDetail> getEcmTaskDetailInOrder(Long workOrderTaskId) {
        return ConvertUtil.list2List(ecmTaskDetailDAO.listByWorkOrderTaskId(workOrderTaskId), EcmTaskDetail.class);
    }

    @Override
    public List<EcmTaskDetail> getByOrderIdAndRoleAndTaskId(Integer workOrderTaskId, String role, Integer taskId) {
        List<EcmTaskDetailPO> ecmTaskDetailPo = ecmTaskDetailDAO.listByTaskIdAndRoleAndWorkOrderTaskId(workOrderTaskId,
            role, taskId);
        if (CollectionUtils.isEmpty(ecmTaskDetailPo)) {
            return Lists.newArrayList();
        }
        return ConvertUtil.list2List(ecmTaskDetailPo, EcmTaskDetail.class);
    }

    @Override
    public Result<EcmTaskDetailProgress> getEcmTaskDetailInfo(Long workOrderTaskId) {
        EcmTask ecmTask = ecmTaskManager.getEcmTask(workOrderTaskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildFail("the ecm task is empty");
        }
        // 获取初始信息
        EcmTaskDetailProgress detailProgress = buildInitialEcmTaskDetail(workOrderTaskId);
        if (EcmTaskStatusEnum.WAITING.getValue().equals(ecmTask.getStatus())) {
            return Result.buildSucc(detailProgress);
        }

        if (EcmTaskStatusEnum.SUCCESS.getValue().equals(ecmTask.getStatus())) {
            List<EcmTaskDetail> ecmTaskDetails = getEcmTaskDetailInOrder(workOrderTaskId);
            detailProgress.setSum((long) ecmTaskDetails.size());
        }

        //任务正在执行，刷新列表
        ecmTaskManager.refreshEcmTask(ecmTask);
        ecmTask = ecmTaskManager.getEcmTask(workOrderTaskId);

        // 获取信息
        List<EcmTaskDetail> ecmTaskDetails = getEcmTaskDetailInOrder(workOrderTaskId);
        detailProgress.setWaiting(detailProgress.getSum() - ecmTaskDetails.size());

        // 状态统计
        ecmTaskDetails.stream().filter(Objects::nonNull).forEachOrdered(detail -> {
            // 角色详情
            List<EcmTaskDetail> ecmTaskDetailList = detailProgress.getRoleNameTaskDetailMap()
                .getOrDefault(detail.getRole(), Lists.newArrayList());
            ecmTaskDetailList.add(detail);
            detailProgress.getRoleNameTaskDetailMap().put(detail.getRole(), ecmTaskDetailList);
            // 状态统计
            if (EcmTaskStatusEnum.SUCCESS.getValue().equals(detail.getStatus())) {
                detailProgress.setSuccess(detailProgress.getSuccess() + 1);
            } else if (EcmTaskStatusEnum.FAILED.getValue().equals(detail.getStatus())) {
                detailProgress.setFailed(detailProgress.getFailed() + 1);
            } else if (EcmTaskStatusEnum.RUNNING.getValue().equals(detail.getStatus())) {
                detailProgress.setCreating(detailProgress.getCreating() + 1);
            } else if (EcmTaskStatusEnum.WAITING.getValue().equals(detail.getStatus())) {
                detailProgress.setWaiting(detailProgress.getWaiting() + 1);
            } else if (EcmTaskStatusEnum.CANCEL.getValue().equals(detail.getStatus())) {
                detailProgress.setCancel(detailProgress.getCancel() + 1);
            }
        });

        detailProgress.setStatus(ecmTask.getStatus());
        detailProgress.setOrderType(ecmTask.getOrderType());
        detailProgress.updatePercent();
        return Result.buildSucc(detailProgress);
    }

    @Override
    public Result<EcmSubTaskLog> getTaskDetailLog(Long detailId, String operator) {
        EcmTaskDetailPO ecmTaskDetailPO = ecmTaskDetailDAO.getById(detailId);
        if (AriusObjUtils.isNull(ecmTaskDetailPO)) {
            return Result.buildFail("工单子任务不存在");
        }

        EcmTask ecmTask = ecmTaskManager.getEcmTask(ecmTaskDetailPO.getWorkOrderTaskId());
        if (AriusObjUtils.isNull(ecmTask)) {
            return Result.buildFail("任务不存在");
        }

        List<EcmParamBase> ecmParamBaseList = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            if (!ecmTaskDetailPO.getTaskId().equals(ecmParamBase.getTaskId().longValue())) {
                continue;
            }
            return ecmHandleService.getSubTaskLog(ecmParamBase, ecmTaskDetailPO.getHostname(), operator);
        }
        return Result.buildFail();
    }

    @Override
    public EcmTaskDetail getByWorkOderIdAndHostName(Long workOrderId, String hostname) {
        return ConvertUtil.obj2Obj(ecmTaskDetailDAO.getByWorkOderIdAndHostName(workOrderId, hostname),
            EcmTaskDetail.class);
    }

    @Override
    public Result<Void> deleteEcmTaskDetailsByTaskOrder(Long workOrderTaskId) {
        try {
            ecmTaskDetailDAO.deleteEcmTaskDetailsByTaskOrder(workOrderTaskId);
        } catch (Exception e) {
            LOGGER.error("class=EcmTaskDetailManagerImpl||method=deleteEcmTaskDetailsByTaskOrder||errMsg={}",e);
            return Result.buildFail("根据工单任务id删除对应任务详情信息失败");
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Long> editEcmTaskDetail(EcmTaskDetail buildEcmTaskDetail) {
        boolean succ = 1 == ecmTaskDetailDAO.update(ConvertUtil.obj2Obj(buildEcmTaskDetail, EcmTaskDetailPO.class));
        return Result.build(succ, buildEcmTaskDetail.getId());
    }

    @Override
    public EcmTaskDetailProgress buildInitialEcmTaskDetail(Long workOrderTaskId) {
        EcmTask ecmTask = ecmTaskManager.getEcmTask(workOrderTaskId);
        if (AriusObjUtils.isNull(ecmTask)) {
            LOGGER.error("class=EcmTaskDetailManagerImpl||method=buildInitialEcmTaskDetail||orderTaskId={}||"
                         + "msg=the ecm task is empty",
                workOrderTaskId);
        }

        EcmTaskDetailProgress ecmTaskDetailProgress = EcmTaskDetailProgress.newFieldInitializedInstance();
        List<EcmParamBase> ecmParamBases = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        if (CollectionUtils.isEmpty(ecmParamBases)) {
            LOGGER.error("class=EcmTaskDetailManagerImpl||method=buildInitialEcmTaskDetail||orderTaskId={}||"
                         + "msg=the convert ecm param is empty",
                workOrderTaskId);
        }

        Map<String, List<String>> role2HostNamesMap = getClusterHostNamesFromDbMap(
            ecmTask.getPhysicClusterId().intValue());
        //1.获取工单总量
        Integer sum = ecmParamBases.stream().filter(r -> !AriusObjUtils.isNull(r) && r.getNodeNumber() != 0)
            .mapToInt(EcmParamBase::getNodeNumber).sum();
        ecmTaskDetailProgress.setSum(sum.longValue());

        //2.状态计算
        if (EcmTaskTypeEnum.NEW.getCode() == ecmTask.getOrderType()
            || EcmTaskTypeEnum.UPGRADE.getCode() == ecmTask.getOrderType()
            || EcmTaskTypeEnum.RESTART.getCode() == ecmTask.getOrderType()) {
            ecmParamBases.stream().filter(Objects::nonNull).forEachOrdered(ecmParam -> ecmTaskDetailProgress
                .getRoleNameTaskDetailMap().put(ecmParam.getRoleName(), Lists.newArrayList()));
        }

        //扩缩容获取变化的机器
        if (EcmTaskTypeEnum.EXPAND.getCode() == ecmTask.getOrderType()
            || EcmTaskTypeEnum.SHRINK.getCode() == ecmTask.getOrderType()) {

            for (Map.Entry<String, List<String>> e : role2HostNamesMap.entrySet()) {
                ecmParamBases.stream().filter(r -> !AriusObjUtils.isNull(r) && r.getRoleName().equals(e.getKey()))
                    .forEachOrdered(ecmParam -> {
                        ecmTaskDetailProgress.getRoleNameTaskDetailMap().put(ecmParam.getRoleName(),
                            Lists.newArrayList());
                    });
            }
        }

        ecmTaskDetailProgress.setWaiting(ecmTaskDetailProgress.getSum());
        ecmTaskDetailProgress.setStatus(ecmTask.getStatus());
        ecmTaskDetailProgress.setOrderType(ecmTask.getOrderType());
        ecmTaskDetailProgress.updatePercent();
        return ecmTaskDetailProgress;
    }

    /*****************************************************private*********************************************************/
    private Map</*角色属性*/String, /*角色对应的主机ip列表*/List<String>> getClusterHostNamesFromDbMap(int clusterId) {
        List<ClusterRoleInfo> roles = clusterRoleService.getAllRoleClusterByClusterId(clusterId);
        if (CollectionUtils.isEmpty(roles)) {
            return Maps.newHashMap();
        }

        Map<String, List<String>> role2HostNamesMap = Maps.newHashMap();
        roles.stream().filter(Objects::nonNull).forEachOrdered(role -> {
            List<ClusterRoleHost> clusterHosts = clusterRoleHostService.getByRoleClusterId(role.getId());
            if (CollectionUtils.isEmpty(clusterHosts)) {
                LOGGER.warn("class=||method=getEcmTaskDetailInfo||msg=the cluster hosts is empty");
                return;
            }

            List<String> clusterHostNames = clusterHosts.stream()
                .filter(r -> !AriusObjUtils.isNull(r) && !AriusObjUtils.isBlack(r.getIp()))
                .map(ClusterRoleHost::getHostname).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(clusterHostNames)) {
                LOGGER.warn("class=||method=getEcmTaskDetailInfo||msg=the cluster hosts name is empty");
                return;
            }

            role2HostNamesMap.put(role.getRole(), clusterHostNames);
        });

        return role2HostNamesMap;
    }

    private  void initEcmTaskDetailParam(EcmTaskDetailPO ecmTaskDetailPo) {
        if(ecmTaskDetailPo.getGrp() == null) {
            ecmTaskDetailPo.setGrp(0);
        }
        if(ecmTaskDetailPo.getIdx() == null) {
            ecmTaskDetailPo.setIdx(0);
        }
    }
}
