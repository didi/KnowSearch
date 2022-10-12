package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.dictionary;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 详细介绍类情况.
 *
 * @ClassName MetricsDictionaryVO
 * @Author gyp
 * @Date 2022/9/28
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("指标字典信息")
public class MetricsDictionaryVO {
    /**
     * id自增
     */
    private Integer id;

    /**
     * 指标分类
     */
    private String type;
    /**
     * metric
     */
    private String metricType;

    /**
     * 指标名称
     */
    private String  name;

    /**
     * 指标价值
     */
    private String  price;

    /**
     * 计算间隔
     */
    private String interval;

    /**
     * 当前计算逻辑
     */
    private String currentCalLogic;
    /**
     * 是否黄金指标
     */
    private Integer isGold;
    /**
     * 单位
     */
    private String unit;
    /**
     * 交互形式
     */
    private String interactiveForm;
    /**
     * 告警指标
     */
    private Integer isWarning;
    /**
     * 指标来源
     */
    private String source;
    /**
     * 指标标签
     */
    private String tags;
    /**
     * 模块
     */
    private String model;
    /**
     * 阈值配置
     */
    private Integer isThreshold;
    /**
     * 阈值详情
     */
    private String threshold;
}