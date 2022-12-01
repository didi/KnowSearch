package com.didi.cloud.fastdump.core.action.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/8
 */
@Component
public class GetIndexNodeMoveStatsAction implements Action<String, IndexNodeMoveTaskStats> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(GetIndexNodeMoveStatsAction.class);
    private final IndexMoveTaskMetadata indexMoveTaskMetadata;

    public GetIndexNodeMoveStatsAction(IndexMoveTaskMetadata indexMoveTaskMetadata) {
        this.indexMoveTaskMetadata = indexMoveTaskMetadata;
    }

    @Override
    public IndexNodeMoveTaskStats doAction(String taskId) throws Exception {
        return indexMoveTaskMetadata.getMoveTaskStats(taskId);
    }
}
