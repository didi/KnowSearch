package com.didi.cloud.fastdump.core.action.movetask;

import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/8
 */
@Component
public class PauseIndexNodeMoveAction implements Action<String, Boolean> {
    private final IndexMoveTaskMetadata indexTaskStatsService;

    public PauseIndexNodeMoveAction(IndexMoveTaskMetadata indexTaskStatsService) {
        this.indexTaskStatsService = indexTaskStatsService;
    }

    @Override
    public Boolean doAction(String taskId) throws Exception {
        IndexNodeMoveTaskStats moveTaskStats = indexTaskStatsService.getMoveTaskStats(taskId);
        moveTaskStats.setInterruptMark(true);
        return true;
    }
}
