package com.didi.cloud.fastdump.core.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.event.es.IndexMoveTaskDistributedEvent;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/6
 */
@Component
public class IndexMoveTaskDistributedListener implements ApplicationListener<IndexMoveTaskDistributedEvent> {
    private final IndexMoveTaskMetadata indexMoveTaskMetadata;

    public IndexMoveTaskDistributedListener(IndexMoveTaskMetadata indexMoveTaskMetadata) {
        this.indexMoveTaskMetadata = indexMoveTaskMetadata;
    }

    @Override
    public synchronized void onApplicationEvent(IndexMoveTaskDistributedEvent event) {
        indexMoveTaskMetadata.putTaskIpList(event.getTaskId(), event.getIpList());
    }
}
