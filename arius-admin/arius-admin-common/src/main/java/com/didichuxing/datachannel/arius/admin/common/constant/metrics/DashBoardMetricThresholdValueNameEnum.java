package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum.*;

/**
 * dashboard默认的配置
 */
public enum DashBoardMetricThresholdValueNameEnum {
    /**
     * {"name":"索引Segments个数","metrics":"segmentNum","unit":"个","compare":">","value":1}
     * {"name":"模板Segments个数","metrics":"segmentNum""unit":"个","compare":">","value":1}
     * {"name":"索引Segments内存大小","metrics":"segmentMemSize""unit":"kb","compare":">","value":1}
     * {"name":"模板Segments内存大小","metrics":"segmentMemSize","unit":"kb","compare":">","value":50}
     * {"name":"节点分片个数","metrics":"shardNum""unit":"个","compare":">","value":500}
     * {"name":"小shard索引列表","metrics":"shardSize","unit":"个","compare":">","value":100}
     * {"name":"索引Mapping个数","metrics":"mappingNum","unit":"个","compare":">","value":100}
     * {"name":"集群shard个数","metrics":"shardNum","unit":"个","compare":">","value":10000}
     */

    INDEX_SEGMENTS_NUM_THRESHOLD(INDEX_SEGMENT_NUM_THRESHOLD,INDEX_SEGMENT_NUM,"索引Segments个数","segmentNum", "个",">",1D),
    TEMPLATE_SEGMENTS_NUM_THRESHOLD(INDEX_TEMPLATE_SEGMENT_COUNT_THRESHOLD,TEMPLATE_SEGMENT_NUM,"模板Segments个数","segmentNum","个",">",1D),
    INDEX_SEGMENTS_MEN_SIZE_THRESHOLD(INDEX_SEGMENT_MEMORY_SIZE_THRESHOLD,INDEX_SEGMENT_MEM_SIZE,"索引Segments内存大小","segmentMemSize","kb",">",1D),
    TEMPLATE_SEGMENTS_MEN_SIZE_THRESHOLD(INDEX_TEMPLATE_SEGMENT_MEMORY_SIZE_THRESHOLD,TEMPLATE_SEGMENT_MEM_NUM,"模板Segments内存大小","segmentMemSize","kb",">",50D),
    NODE_SHARD_SIZE_THRESHOLD(NODE_SHARD_BIG_THRESHOLD,NODE_SHARD_NUM,"节点分片个数","shardNum","个",">",500D),
    MAPPING_NUM_THRESHOLD(INDEX_MAPPING_NUM_THRESHOLD,INDEX_MAPPING_NUM,"索引Mapping个数","mappingNum","个",">",1D),
    INDEX_SMALL_SHARD_THRESHOLD(INDEX_SHARD_SMALL_THRESHOLD,INDEX_SMALL_SHARD,"小shard索引列表","shardSize","个",">",100D),
    SHARD_NUM_THRESHOLD(CLUSTER_SHARD_NUM_THRESHOLD,CLUSTER_SHARD_NUM, "集群shard个数", "shardNum", "个", ">", 10000D);
    /**
     * 配置名称
     */
    private String configName;

    /**
     *
     */
    private DashBoardMetricListTypeEnum typeEnum;
    /**
     * 名称
     */
    private String name;
    /**
     * 指标项名称
     */
    private String metrics;
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

    DashBoardMetricThresholdValueNameEnum(String configName, DashBoardMetricListTypeEnum typeEnum, String name, String metrics, String unit, String compare, Double value) {

        this.configName = configName;
        this.typeEnum = typeEnum;
        this.name = name;
        this.metrics = metrics;
        this.unit = unit;
        this.compare = compare;
        this.value = value;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public DashBoardMetricListTypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(DashBoardMetricListTypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
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

    public static List<DashBoardMetricThresholdValueNameEnum> getAllDefaultThresholdValue(){
        List<DashBoardMetricThresholdValueNameEnum> list = Lists.newArrayList();
        for (DashBoardMetricThresholdValueNameEnum typeEnum : DashBoardMetricThresholdValueNameEnum.values()) {
            list.add(typeEnum);
        }
        return list;
    }
}