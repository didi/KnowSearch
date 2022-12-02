package com.didi.cloud.fastdump.common.event.es.metrics;

import com.didi.cloud.fastdump.common.bean.metrics.IndexMoveMetrics;

/**
 * Created by linyunan on 2022/11/22
 */
public class ESIndexMoveMetricsEvent extends BaseMetricsEvent{
    private IndexMoveMetrics indexMoveMetrics;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ESIndexMoveMetricsEvent(Object source, IndexMoveMetrics indexMoveMetrics) {
        super(source);
        this.indexMoveMetrics = indexMoveMetrics;
    }

    public IndexMoveMetrics getIndexMoveMetrics() {
        return indexMoveMetrics;
    }

    public void setIndexMoveMetrics(IndexMoveMetrics indexMoveMetrics) {
        this.indexMoveMetrics = indexMoveMetrics;
    }
}
