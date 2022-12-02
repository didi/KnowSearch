package com.didi.cloud.fastdump.core.listener.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.event.es.metrics.ESIndexMoveMetricsEvent;
import com.didi.cloud.fastdump.core.service.metrics.ESMetricsService;

/**
 * Created by linyunan on 2022/9/6
 */
@Component
public class ESIndexMoveMetricsListener implements ApplicationListener<ESIndexMoveMetricsEvent> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ESIndexMoveMetricsListener.class);
    @Autowired
    private ESMetricsService esMetricsService;

    @Override
    public synchronized void onApplicationEvent(ESIndexMoveMetricsEvent event) {
        esMetricsService.save(event.getIndexMoveMetrics());
    }
}
