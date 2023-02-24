package com.didi.cloud.fastdump.core.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.stats.IndexMoveTaskStats;
import com.didi.cloud.fastdump.common.event.es.IndexMoveTaskSuccEvent;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/6
 */
@Component
public class IndexMoveTaskSuccListener implements ApplicationListener<IndexMoveTaskSuccEvent> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(IndexMoveTaskSuccListener.class);
    @Autowired
    private  IndexMoveTaskMetadata IndexMoveTaskMetadata;

    @Override
    public synchronized void onApplicationEvent(IndexMoveTaskSuccEvent event) {
        IndexMoveTaskStats indexMoveTaskStats = event.getIndexMoveTaskStats();
        try {
            IndexMoveTaskMetadata.refreshSuccStats(indexMoveTaskStats);
        } catch (Exception e) {
            LOGGER.error("class=IndexMoveTaskSuccListener||method=onApplicationEvent||errMsg= failed to refreshSuccStats" +
                    " detail:{}", e.getMessage(), e);
        }
    }
}
