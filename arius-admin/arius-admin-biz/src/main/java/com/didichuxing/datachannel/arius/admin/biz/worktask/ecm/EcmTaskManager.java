package com.didichuxing.datachannel.arius.admin.biz.worktask.ecm;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmTaskBasic;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.EcmActionEnum;

import java.util.List;

/**
 * ES工单任务 服务类
 * @author didi
 * @since 2020-09-24
 */
public interface EcmTaskManager {
    /**
     * 校验同一个集群是否存在未完成任务
     * @param  phyClusterId 物理集群ID
     * @return true 存在
     */
    boolean existUnClosedEcmTask(Long phyClusterId);

    /**
     * 创建一个EcmTask
     * @param  ecmTaskDTO 工单任务
     * @return result
     */
    Result<Long> saveEcmTask(EcmTaskDTO ecmTaskDTO);

    /**
     * 查询全部的 EcmTask
     * @param
     * @return result
     */
    List<EcmTask> listEcmTask();

    /**
     * 查询 工单任务基本情况
     * @param  taskId
     * @return result
     */
    Result<EcmTaskBasic> getEcmTaskBasicByTaskId(Long taskId);

    /**
     * 创建集群任务
     * @param  taskId  工单任务ID
     * @param  operator  操作人
     * @return result
     */
    Result createClusterEcmTask(Long taskId, String operator);

    /**
     * 根据taskId执行一个Ecm任务
     * @param  taskId 工单任务ID
     * @param  operator  操作人
     * @return result
     */
    Result actionClusterEcmTask(Long taskId, String operator);

    /**
     * 继续执行单个Ecm任务
     * @param taskId
     * @param ecmActionEnum
     * @param hostname
     * @param operator
     * @return
     */
    Result actionClusterEcmTask(Long taskId, EcmActionEnum ecmActionEnum, String hostname, String operator);

    /**
     * 取消工单部署集群节点
     * @param  taskId 工单任务ID
     * @param  operator  操作人
     * @return result
     */
    Result cancelClusterEcmTask(Long taskId, String operator);

    /**
     * 根据ID获取任务
     * @param  id 任务ID
     * @return result
     */
    EcmTask getEcmTask(Long id);

    /**
     * 刷新同步任务
     * @param ecmTask
     * @return
     */
    Result<EcmTaskStatusEnum> refreshEcmTask(EcmTask ecmTask);

    /**
     * 根据ID修改任务信息
     * @param  ecmTask
     * @return result
     */
    int updateEcmTask(EcmTask ecmTask);

//    /**
//     * 根据物理集群id获取状态是running的任务
//     * @param clusterId 物理集群id
//     * @return 任务
//     */
//    EcmTask getRunningEcmTaskByClusterId(Integer clusterId);
}
