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
public class MetricsContent implements Serializable {
    /**
     * 集群名称，仅在节点维度指标、索引维度指标、模板维度指标设置该值，用于前端dashboard跳转至指标看板
     */
    private String                   cluster;

    /**
     * 节点名称、模板名称、索引名称、网关相关信息
     */
    private String                   name;

    /**
     * 多个时间片指标数据
     */
    private List<MetricsContentCell> metricsContentCells;

    /**
     * 时间段内的最大值或平均值
     */
    private Double                   valueInTimePeriod;

}
