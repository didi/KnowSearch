package com.didi.cloud.fastdump.core.service.metrics;

import com.didi.cloud.fastdump.common.bean.metrics.IndexMoveMetrics;

/**
 * Created by linyunan on 2022/11/22
 */
public interface ESMetricsService {
    void save(IndexMoveMetrics indexMoveMetrics);
}
