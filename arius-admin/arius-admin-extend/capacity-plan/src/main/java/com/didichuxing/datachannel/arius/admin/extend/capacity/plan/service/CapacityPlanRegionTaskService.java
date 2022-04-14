package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateMetaMetric;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanRegionContext;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionTask;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionTaskItem;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskTypeEnum;

/**
 * @author d06679
 * @date 2019-06-24
 */
public interface CapacityPlanRegionTaskService {

    /**
     * 获取指定的region的任务列表
     * @param regionId regionId
     * @return list
     */
    List<CapacityPlanRegionTask> getTaskByRegionId(Long regionId);

    /**
     * 获取指定的region的最近一次的任务就信息
     * @param regionId regionId
     * @return result
     */
    CapacityPlanRegionTask getDecreasingTaskByRegionId(Long regionId);

    /**
     * 获取指定的region的任务列表
     * @param taskId regionId
     * @return list
     */
    List<CapacityPlanRegionTaskItem> getTaskItemByTaskId(Long taskId);

    /**
     * 检查任务
     * @return result
     */
    Result<Void> checkTasks();

    /**
     * 完成任务
     * @param taskId 任务id
     * @return result
     */
    boolean finishTask(Long taskId);

    /**
     * 保存region初始化信息
     * @param regionId regionId
     * @param racks racks
     * @param templateMetaMetrics 模板列表第
     * @return result
     */
    boolean saveInitTask(Long regionId, String racks, List<TemplateMetaMetric> templateMetaMetrics);

    /**
     * 执行初始化region结果
     * @param taskId taskId
     * @return result
     */
    Result<Void> exeInitTask(Long taskId);

    /**
     * 保存任务
     * @param typeEnum 任务类型
     * @param regionContext 上下文
     * @param deltaRacks 变化的rack
     * @param statusEnum 任务状态
     * @return result
     */
    boolean saveTask(CapacityPlanRegionTaskTypeEnum typeEnum, CapacityPlanRegionContext regionContext,
                     List<String> deltaRacks, CapacityPlanRegionTaskStatusEnum statusEnum);

    /**
     * 获取region最后一个checktask
     * @param regionId regionId
     * @return task
     */
    CapacityPlanRegionTask getLastCheckTask(Long regionId);

    /**
     * 删除region对应的task
     * @param regionId region
     * @return true/false
     */
    int deleteTasksByRegionId(Long regionId);

    /**
     * 获取过去days天内的缩容任务
     * @param regionId regionID
     * @param days 天数
     * @return task
     */
    CapacityPlanRegionTask getLastDecreaseTask(Long regionId, int days);
}
