package com.didichuxing.datachannel.arius.admin.core.service.task.fastindex;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.FastIndexTaskInfo;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.task.FastIndexTaskInfoDAO;

/**
 * 数据迁移子任务
 *
 * @author didi
 * @date 2022/10/31
 */
@Service
public class FastIndexTaskServiceImpl implements FastIndexTaskService {

    @Autowired
    private FastIndexTaskInfoDAO fastIndexTaskInfoDAO;

    /**
     * 批量新增索引数据迁移子任务
     *
     * @param recordList 记录
     * @return int
     */
    @Override
    public boolean saveTasks(List<FastIndexTaskInfo> recordList) {
        return fastIndexTaskInfoDAO.insertBatch(recordList) > 0;
    }

    /**
     * 刷新任务
     * 负责更新内核任务ID、任务状态、任务统计信息、任务开始与结束时间
     *
     * @param taskInfo 记录
     */
    @Override
    public void refreshTask(FastIndexTaskInfo taskInfo) {
        fastIndexTaskInfoDAO.refreshTask(taskInfo);
    }

    @Override
    public List<FastIndexTaskInfo> listByTaskId(Integer taskId) {
        return fastIndexTaskInfoDAO.listByTaskId(taskId);
    }

    @Override
    public List<Integer> listTemplateIdByTaskId(Integer taskId) {
        return fastIndexTaskInfoDAO.listTemplateIdByTaskId(taskId);
    }

    @Override
    public List<FastIndexTaskInfo> listByTaskIdAndStatus(Integer taskId, List<Integer> taskStatusList) {
        return fastIndexTaskInfoDAO.listByTaskIdAndStatus(taskId, taskStatusList);
    }
}
