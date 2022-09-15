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

    INDEX_SEGMENTS_NUM_THRESHOLD(INDEX_SEGMENT_NUM_THRESHOLD,INDEX_SEGMENT_NUM,DASHBOARD_INDEX_SEGMENT_NUM_THRESHOLD_DEFAULT_VALUE),
    TEMPLATE_SEGMENTS_NUM_THRESHOLD(INDEX_TEMPLATE_SEGMENT_COUNT_THRESHOLD,TEMPLATE_SEGMENT_NUM,DASHBOARD_INDEX_TEMPLATE_SEGMENT_COUNT_THRESHOLD_DEFAULT_VALUE),
    INDEX_SEGMENTS_MEN_SIZE_THRESHOLD(INDEX_SEGMENT_MEMORY_SIZE_THRESHOLD,INDEX_SEGMENT_MEM_SIZE,DASHBOARD_INDEX_SEGMENT_MEMORY_SIZE_THRESHOLD_DEFAULT_VALUE),
    TEMPLATE_SEGMENTS_MEN_SIZE_THRESHOLD(INDEX_TEMPLATE_SEGMENT_MEMORY_SIZE_THRESHOLD,TEMPLATE_SEGMENT_MEM_NUM,DASHBOARD_INDEX_TEMPLATE_SEGMENT_MEMORY_SIZE_THRESHOLD_DEFAULT_VALUE),
    NODE_SHARD_SIZE_THRESHOLD(NODE_SHARD_NUM_THRESHOLD,NODE_SHARD_NUM,DASHBOARD_NODE_SHARD_NUM_THRESHOLD_DEFAULT_VALUE),
    MAPPING_NUM_THRESHOLD(INDEX_MAPPING_NUM_THRESHOLD,INDEX_MAPPING_NUM,DASHBOARD_INDEX_MAPPING_NUM_THRESHOLD_DEFAULT_VALUE),
    INDEX_SMALL_SHARD_THRESHOLD(INDEX_SHARD_SMALL_THRESHOLD,INDEX_SMALL_SHARD,DASHBOARD_INDEX_SHARD_SMALL_THRESHOLD_DEFAULT_VALUE),
    COLLECTOR_DELAYED_THRESHOLD(DASHBOARD_CLUSTER_METRIC_COLLECTOR_DELAYED_THRESHOLD,CLUSTER_ELAPSED_TIME_GTE_5MIN,DASHBOARD_CLUSTER_METRIC_COLLECTOR_DELAYED_DEFAULT_VALUE),
    SHARD_NUM_THRESHOLD(CLUSTER_SHARD_NUM_THRESHOLD,CLUSTER_SHARD_NUM, DASHBOARD_CLUSTER_SHARD_NUM_THRESHOLD_DEFAULT_VALUE);
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
    private String defaultValue;

    DashBoardMetricThresholdValueNameEnum(String configName, DashBoardMetricListTypeEnum typeEnum, String defaultValue) {
        this.configName = configName;
        this.typeEnum = typeEnum;
        this.defaultValue = defaultValue;
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

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public static List<DashBoardMetricThresholdValueNameEnum> getAllDefaultThresholdValue(){
        List<DashBoardMetricThresholdValueNameEnum> list = Lists.newArrayList();
        for (DashBoardMetricThresholdValueNameEnum typeEnum : DashBoardMetricThresholdValueNameEnum.values()) {
            list.add(typeEnum);
        }
        return list;
    }
}