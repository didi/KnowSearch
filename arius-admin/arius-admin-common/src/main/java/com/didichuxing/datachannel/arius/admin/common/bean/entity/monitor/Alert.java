package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.Data;

import java.util.List;
import java.util.Properties;

@Data
public class Alert {
    /**
     * 告警ID
     */
    private Long id;
    /**
     * 监控ID
     */
    private Long monitorId;
    /**
     * 监控策略ID
     */
    private Long strategyId;
    /**
     * 监控策略名称
     */
    private String strategyName;
    /**
     * 告警类型
     */
    private String type;
    /**
     * 告警优先级
     */
    private Integer priority;
    /**
     * 告警的指标
     */
    private String metric;
    /**
     * 触发告警的曲线tags
     */
    private Properties tags;
    /**
     * 告警开始时间
     */
    private Long startTime;
    /**
     * 告警结束时间
     */
    private Long endTime;
    /**
     * 现场值
     */
    private Double value;
    /**
     * 现场值
     */
    private List<MetricPoint> points;
    /**
     * 告警组
     */
    private List<String> groups;
    /**
     * 表达式
     */
    private String info;
}