package com.didi.cloud.fastdump.core.action.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/8
 */
@Component
public class DeleteIndexNodeMoveStatsAction implements Action<String, Void> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DeleteIndexNodeMoveStatsAction.class);
    private final IndexMoveTaskMetadata indexMoveTaskMetadata;

    public DeleteIndexNodeMoveStatsAction(IndexMoveTaskMetadata indexMoveTaskMetadata) {
        this.indexMoveTaskMetadata = indexMoveTaskMetadata;
    }

    @Override
    public Void doAction(String taskId) throws Exception {
        indexMoveTaskMetadata.removeTaskStats(taskId);
        indexMoveTaskMetadata.removeTaskIpList(taskId);
        return null;
    }
}
