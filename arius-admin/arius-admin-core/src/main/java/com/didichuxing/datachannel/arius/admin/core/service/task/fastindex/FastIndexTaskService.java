package com.didichuxing.datachannel.arius.admin.core.service.task.fastindex;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.FastIndexTaskInfo;

/**
 * 数据迁移子任务
 *
 * @author didi
 * @date 2022/10/31
 */
public interface FastIndexTaskService {

    /**
     * 批量新增索引数据迁移子任务
     *
     * @param recordList 记录
     * @return int
     */
    boolean saveTasks(List<FastIndexTaskInfo> recordList);

    /**
     * 刷新任务
     * 负责更新内核任务ID、任务状态、任务统计信息、任务开始与结束时间
     *
     * @param taskInfo 记录
     */
    void refreshTask(FastIndexTaskInfo taskInfo);

    List<FastIndexTaskInfo> listByTaskId(Integer taskId);

    List<Integer> listTemplateIdByTaskId(Integer taskId);

    List<FastIndexTaskInfo> listByTaskIdAndStatus(Integer taskId, List<Integer> taskStatusList);
}
