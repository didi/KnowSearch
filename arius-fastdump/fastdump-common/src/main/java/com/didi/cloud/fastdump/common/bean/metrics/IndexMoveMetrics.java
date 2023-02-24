package com.didi.cloud.fastdump.common.bean.metrics;

import com.didi.cloud.fastdump.common.bean.BaseEntity;

import lombok.Builder;
import lombok.Data;

/**
 * Created by linyunan on 2022/11/22
 */
@Builder
@Data
public class IndexMoveMetrics extends BaseEntity {
    private String       taskId;
    private Long         timestamp;
    /**
     * 指标等级
     * @see com.didi.cloud.fastdump.common.enums.MetricsLevelEnum
     */
    private String       level;
    private String       ip;
    private String       sourceIndex;
    private String       sourceClusterName;
    private String       targetIndex;
    private String       targetClusterName;
    private String       failedLuceneDataPath;
    private String       message;
}
