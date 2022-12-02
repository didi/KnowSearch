package com.didi.cloud.fastdump.core.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.event.es.TemplateMoveTaskSuccEvent;
import com.didi.cloud.fastdump.core.service.metadata.TemplateMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/6
 */
@Component
public class TemplateMoveTaskSuccListener implements ApplicationListener<TemplateMoveTaskSuccEvent> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(TemplateMoveTaskSuccListener.class);
    @Autowired
    private TemplateMoveTaskMetadata templateMoveTaskMetadata;

    @Override
    public synchronized void onApplicationEvent(TemplateMoveTaskSuccEvent event) {
        TemplateMoveTaskStats templateMoveTaskStats = event.getTemplateMoveTaskStats();
        try {
            templateMoveTaskMetadata.refreshSuccStats(templateMoveTaskStats);
        } catch (Exception e) {
            LOGGER.error("class=IndexMoveTaskSuccListener||method=onApplicationEvent||errMsg= failed to refreshSuccStats" +
                    " detail:{}", e.getMessage(), e);
        }
    }
}
