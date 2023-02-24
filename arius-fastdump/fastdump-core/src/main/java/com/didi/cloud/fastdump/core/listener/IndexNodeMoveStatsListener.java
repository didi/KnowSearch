package com.didi.cloud.fastdump.core.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.sinker.es.ESIndexDataSinker;
import com.didi.cloud.fastdump.common.bean.source.es.ESIndexSource;
import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.taskcontext.es.ESIndexMoveTaskActionContext;
import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;
import com.didi.cloud.fastdump.common.event.es.IndexNodeMoveStatsEvent;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/6
 */
@Component
public class IndexNodeMoveStatsListener implements ApplicationListener<IndexNodeMoveStatsEvent> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(IndexNodeMoveStatsListener.class);
    private final IndexMoveTaskMetadata IndexMoveTaskMetadata;

    public IndexNodeMoveStatsListener(IndexMoveTaskMetadata IndexMoveTaskMetadata) {
        this.IndexMoveTaskMetadata = IndexMoveTaskMetadata;
    }

    @Override
    public synchronized void onApplicationEvent(IndexNodeMoveStatsEvent event) {
        ESIndexMoveTaskActionContext moveTaskContext = event.getEsIndexMoveTaskContext();
        String taskId = moveTaskContext.getTaskId();

        ESIndexSource     source = moveTaskContext.getSource();
        ESIndexDataSinker sinker = moveTaskContext.getSinker();

        IndexNodeMoveTaskStats indexNodeMoveTaskStats = new IndexNodeMoveTaskStats();

        indexNodeMoveTaskStats.setTaskId(taskId);
        indexNodeMoveTaskStats.setSourceIndex(source.getSourceIndex());
        indexNodeMoveTaskStats.setSourceCluster(source.getSourceCluster());

        indexNodeMoveTaskStats.setTargetIndex(sinker.getTargetIndex());
        indexNodeMoveTaskStats.setTargetCluster(sinker.getTargetCluster());

        indexNodeMoveTaskStats.setStatus(moveTaskContext.getStatus());
        indexNodeMoveTaskStats.setStatusCode(TaskStatusEnum.valueOfType(moveTaskContext.getStatus()).getCode());

        indexNodeMoveTaskStats.setCostTime(0L);

        IndexMoveTaskMetadata.putTaskStats(taskId, indexNodeMoveTaskStats);
    }
}
