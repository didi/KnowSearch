package com.didi.cloud.fastdump.core.service.metadata;

import java.util.List;

import com.didi.cloud.fastdump.common.bean.stats.BaseMoveTaskStats;

/**
 * Created by linyunan on 2022/9/5
 */
public interface TaskStatsService<MoveTaskStats extends BaseMoveTaskStats> {
    /**
     * @param moveTaskStats
     * @return    任务id
     */
    boolean putTaskStats(String taskId, MoveTaskStats moveTaskStats);

    MoveTaskStats getMoveTaskStats(String taskId);

    List<MoveTaskStats> listAllMoveTaskStats();

    List<String> listAllTaskIds();

    boolean removeTaskStats(String taskId);

    boolean isTaskSucc(String taskId);
}
