package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.MetricPoint;
import lombok.Data;

import java.util.List;
import java.util.Properties;

@Data
public class OdinAlert {
    /**
     * 告警ID
     */
    private Long              id;

    /**
     * 监控策略ID
     */
    private Long              sid;

    /**
     * 监控策略名称
     */
    private String            sname;

    /**
     * 告警类型
     */
    private String            type;

    /**
     * odin节点
     */
    private String            ns;

    /**
     * 告警优先级
     */
    private Integer           priority;

    /**
     * 告警的指标
     */
    private String            metric;

    /**
     * 触发告警的曲线tags
     */
    private Properties        tags;

    /**
     * 告警开始时间
     */
    private Long              stime;

    /**
     * 告警结束时间
     */
    private Long              etime;

    private Double            value;

    /**
     * 现场值
     */
    private List<MetricPoint> points;

    /**
     * 告警组
     */
    private List<String>      groups;

    /**
     * ignore
     */
    private List<String>      status;

    /**
     * ignore
     */
    private String            siid;

    /**
     * 表达式
     */
    private String            info;

    /**
     * ignore
     */
    private String            created;

    /**
     * ignore
     */
    private Long              hashid;
}