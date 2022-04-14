package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariousLineChartMetrics implements Serializable {

    /**
     * 指标类型
     */
    private String               type;

    /**
     * 集群节点指标类型
     */
    private List<MetricsContent> metricsContents;
}
