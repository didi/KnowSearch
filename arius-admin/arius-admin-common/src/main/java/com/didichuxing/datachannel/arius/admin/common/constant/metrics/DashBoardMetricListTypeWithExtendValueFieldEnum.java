package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

/**
 * @author gyp
 * @version 1.0
 * @description: 需要获取额外的值进行展示的指标项，因为有的指标项之前没有设计展示值，比如bigshard，
 *               采集的时候是true和false，现在需要展示大小，就需要添加额外的采集字段进行获取
 * @date 2022/6/22 19:44
 */
public enum DashBoardMetricListTypeWithExtendValueFieldEnum {
    SMALL_SHARD(DashBoardMetricListTypeEnum.INDEX_SMALL_SHARD.getType(),"shardSize"),
    BIG_SHARD(DashBoardMetricListTypeEnum.INDEX_BIG_SHARD.getType(),"shardSize"),
    CLUSTER_ELAPSED_TIME_GTE_5_MIN(DashBoardMetricListTypeEnum.CLUSTER_ELAPSED_TIME_GTE_5MIN.getType(),"collectorDelayed");

    /**
     * 统计指标项，eg。smallShard，bigShard
     */
    private String metricType;

    /**
     * 新增额外的展示需要的字段 eg,小shard列表中的shard大小
     */
    private String extendValueField;

    DashBoardMetricListTypeWithExtendValueFieldEnum(String metricType, String valueType) {
        this.metricType = metricType;
        this.extendValueField = valueType;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getExtendValueField() {
        return extendValueField;
    }

    public void setExtendValueField(String extendValueField) {
        this.extendValueField = extendValueField;
    }

    public static DashBoardMetricListTypeWithExtendValueFieldEnum valueOfMetricType(String metricType) {
        if (null == metricType) {
            return null;
        }

        for (DashBoardMetricListTypeWithExtendValueFieldEnum typeEnum : DashBoardMetricListTypeWithExtendValueFieldEnum.values()) {
            if (metricType.equals(typeEnum.getMetricType())) {
                return typeEnum;
            }
        }
        return null;
    }
}