package com.didichuxing.datachannel.arius.admin.common.bean.po.metrics;

import com.baomidou.mybatisplus.annotation.TableName;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 指标字典配置po
 *
 * @author shizeying
 * @date 2022/09/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("metric_dictionary_info")
public class MetricsDictionaryPO extends BasePO {
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