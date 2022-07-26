package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.INDEX_SHARD_SMALL_THRESHOLD;

/**
 * dashboard默认的配置
 */
public enum DashBoardMetricThresholdValueNameEnum {
    
    SHARD_SIZE(INDEX_SHARD_SMALL_THRESHOLD,"shardSize", "索引下的shard大小字段","kb",">",1D);

    /**
     * 名称
     */
    private String name;
    /**
     * 指标项名称
     */
    private String metrics;
    /**
     * 描述
     */
    private String desc;
    /**
     * 单位
     */
    private String unit;
    /**
     * 比较符号
     */
    private String compare;
    /**
     * 阈值
     */
    private Double value;

    DashBoardMetricThresholdValueNameEnum(String name,String metrics, String desc, String unit, String compare, Double value) {
        this.name = name;
        this.metrics = metrics;
        this.desc = desc;
        this.unit = unit;
        this.compare = compare;
        this.value = value;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCompare() {
        return compare;
    }

    public void setCompare(String compare) {
        this.compare = compare;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<DashBoardMetricThresholdValueNameEnum> getAllDefaultThresholdValue(){
        List<DashBoardMetricThresholdValueNameEnum> list = Lists.newArrayList();
        for (DashBoardMetricThresholdValueNameEnum typeEnum : DashBoardMetricThresholdValueNameEnum.values()) {
            list.add(typeEnum);
        }
        return list;
    }
}