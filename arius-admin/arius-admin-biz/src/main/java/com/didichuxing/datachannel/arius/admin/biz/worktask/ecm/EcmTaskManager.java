package com.didichuxing.datachannel.arius.admin.biz.worktask.ecm;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmTaskBasic;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.ecm.EcmTaskPO;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.EcmActionEnum;

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
     * 查询处于running状态的ecm集群任务
     * @return List<EcmTask>
     */
    List<EcmTask> listRunningEcmTask();

    /**
     * 查询 工单任务基本情况
     * @param  taskId
     * @return result
     */
    Result<EcmTaskBasic> getEcmTaskBasicByTaskId(Long taskId);

    /**
     * 创建ES集群任务:1. 先初始化集群信息, 再灰度创建master角色的节点, master节点两个一组启动
     * @param  taskId  工单任务ID
     * @param  operator  操作人
     * @return result
     */
    Result<EcmOperateAppBase> savaAndActionEcmTask(Long taskId, String operator);

    /**
     * 重试集群任务：将任务状态改为waiting 同时将zeus任务的id置空
     * @param  taskId  工单任务ID
     * @param  operator  操作人
     * @return result
     */
    Result<Void> retryClusterEcmTask(Long taskId, String operator);

    /**
     * 根据taskId执行一个Ecm任务
     * @param  taskId 工单任务ID
     * @param  operator  操作人
     * @return result
     */
    Result<EcmOperateAppBase> actionClusterEcmTask(Long taskId, String operator);

    /**
     * 继续执行单个Ecm任务
     * @param taskId
     * @param ecmActionEnum
     * @param hostname
     * @param operator
     * @return
     */
    Result<EcmOperateAppBase> actionClusterEcmTask(Long taskId, EcmActionEnum ecmActionEnum, String hostname, String operator);

    /**
     * 取消工单部署集群节点
     * @param  taskId 工单任务ID
     * @param  operator  操作人
     * @return result
     */
    Result<Void> cancelClusterEcmTask(Long taskId, String operator);

    /**
     * 暂停集群任务
     * @param  taskId 工单任务ID
     * @param  operator  操作人
     * @return result
     */
    Result<Void> pauseClusterEcmTask(Long taskId, String operator);

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
    EcmTaskStatusEnum refreshEcmTask(EcmTask ecmTask);

    /**
     * 根据ID修改任务信息
     * @param  ecmTask
     * @return result
     */
    boolean updateEcmTask(EcmTask ecmTask);

    /**
     * 根据物理集群id获取正在执行或者等待执行的工单信息
     * @param physicClusterId 物理集群id
     * @return EcmTaskPO
     */
    EcmTaskPO getRunningEcmTaskByClusterId(Integer physicClusterId);

    /**
     * 对于单个集群任务节点进行操作
     * @param taskId ecm任务执行id主键
     * @param ecmActionEnum 集群任务的操作
     * @param hostname 主机名称或者ip
     * @param operator 操作人
     * @return
     */
    Result<Void> actionClusterHostEcmTask(Long taskId, EcmActionEnum ecmActionEnum, String hostname, String operator);

    /**
     * 根据物理集群名称获取到正在执行或者等待执行的ecm任务
     * @param clusterName 物理集群名称
     * @return ecm任务
     */
    Result<EcmTask> getUsefulEcmTaskByClusterName(String clusterName);

    /**
     * 根据集群名称，获取当前正在运行的ecm任务的工单id(一个集群只能存在一个可执行或者待执行的ecm任务)
     * @param cluster 物理集群名称
     * @return ecm任务工单的contentObj内容
     */
    Result<String> getEcmTaskOrderDetailInfo(String cluster);

    /**
     * 根据集群名称，ip和port获取对应的rack信息的设置
     * @param clusterName 物理集群名称
     * @param ip ip地址
     * @return 判断指定data节点的rack类型，如果是冷节点则返回cold，否则返回*
     */
    String judgeColdRackFromEcmTaskOfClusterNewOrder(String clusterName, String ip);
}
