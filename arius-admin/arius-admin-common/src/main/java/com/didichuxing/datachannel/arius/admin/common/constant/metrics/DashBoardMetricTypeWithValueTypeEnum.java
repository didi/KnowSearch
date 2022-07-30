package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author gyp
 * @version 1.0
 * @description: 需要通过其他指标获取值进行展示的，
 * @date 2022/6/22 19:44
 */
public enum DashBoardMetricTypeWithValueTypeEnum {
    BIG_SHARD("bigShard","shardSize");

    /**
     * 统计指标
     */
    private String metricType;

    /**
     * 获取值的指标
     */
    private String valueType;

    DashBoardMetricTypeWithValueTypeEnum(String metricType, String valueType) {
        this.metricType = metricType;
        this.valueType = valueType;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public static Map<String,String> getAllDashBoardMetricTypeWithValueTypes(){
        Map<String,String> map = Maps.newHashMap();
        DashBoardMetricTypeWithValueTypeEnum[] metricTypeWithValueTypeEnums = DashBoardMetricTypeWithValueTypeEnum.values();
        for (int i = 0; i < metricTypeWithValueTypeEnums.length; i++) {
            map.put(metricTypeWithValueTypeEnums[i].getMetricType(),metricTypeWithValueTypeEnums[i].getValueType());
        }
        return map;
    }
}