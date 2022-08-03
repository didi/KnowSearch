package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Created by linyunan on 3/11/22
 */
public enum DashBoardMetricListTypeEnum {
    UNKNOWN(OneLevelTypeEnum.UNKNOWN, "", "未知"),
    CLUSTER_ELAPSED_TIME_GTE_5MIN(OneLevelTypeEnum.CLUSTER, "clusterElapsedTimeGte5Min", "采集超过5分钟"),
    NODE_DEAD(OneLevelTypeEnum.NODE, "dead", "节点是否死亡"),
    NODE_LARGE_DISK_USAGE(OneLevelTypeEnum.NODE, "largeDiskUsage", "磁盘利用率是否超红线"),
    NODE_LARGE_HEAD(OneLevelTypeEnum.NODE, "largeHead", "堆内存利用率是否超红线"),
    NODE_LARGE_CPU_USAGE(OneLevelTypeEnum.NODE, "largeCpuUsage", "CPU利用率是否超红线"),
    NODE_WRITE_REJECTED_NUM(OneLevelTypeEnum.NODE, "writeRejectedNum", "WriteRejected节点"),
    NODE_SEARCH_REJECTED_NUM(OneLevelTypeEnum.NODE, "searchRejectedNum", "SearchRejected节点"),
    NODE_SHARD_NUM(OneLevelTypeEnum.NODE, "shardNum", "shardNum节点"),
    CLUSTER_SHARD_NUM(OneLevelTypeEnum.CLUSTER, "shardNum", "shardNum节点"),
    
    TEMPLATE_SEGMENT_MEM_NUM(OneLevelTypeEnum.TEMPLATE, "segmentMemSize", "模板Segments内存大小（MB）"),
    TEMPLATE_SEGMENT_NUM(OneLevelTypeEnum.TEMPLATE, "segmentNum", "模板Segments个数"),
    
    INDEX_RED(OneLevelTypeEnum.INDEX, "red", "是否RED索引"),
    INDEX_SINGLE_REP(OneLevelTypeEnum.INDEX, "singReplicate", "是否单副本索引"),
    INDEX_UNASSIGNED_SHARD(OneLevelTypeEnum.INDEX, "unassignedShard", "是否是未分配shard索引"),
    INDEX_BIG_SHARD(OneLevelTypeEnum.INDEX, "bigShard", "大shard索引列表"),
    INDEX_SMALL_SHARD(OneLevelTypeEnum.INDEX, "smallShard", "小shard索引列表"),
    INDEX_MAPPING_NUM(OneLevelTypeEnum.INDEX, "mappingNum", "索引Mapping字段个数"),
    INDEX_SEGMENT_NUM(OneLevelTypeEnum.INDEX, "segmentNum", "索引Segments个数"),
    INDEX_SEGMENT_MEM_SIZE(OneLevelTypeEnum.INDEX, "segmentMemSize", "索引Segments内存大小（MB）");
    
    DashBoardMetricListTypeEnum(OneLevelTypeEnum oneLevelTypeEnum, String type, String desc) {
        this.oneLevelTypeEnum = oneLevelTypeEnum;
        this.type = type;
        this.desc = desc;
    }
    
    private OneLevelTypeEnum oneLevelTypeEnum;
    private String           type;
    private String           desc;
    
    public String getType() {
        return type;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public OneLevelTypeEnum getOneLevelTypeEnum() {
        return oneLevelTypeEnum;
    }
    
    public static boolean hasExist(String oneLevelType, String metricsType) {
        if (null == metricsType) {
            return false;
        }
        for (DashBoardMetricListTypeEnum typeEnum : DashBoardMetricListTypeEnum.values()) {
            OneLevelTypeEnum oneLevelTypeEnum = typeEnum.getOneLevelTypeEnum();
            if (oneLevelTypeEnum.getType().equals(oneLevelType) && metricsType.equals(typeEnum.getType())) {
                return true;
            }
        }
        
        return false;
    }
    
    public static DashBoardMetricListTypeEnum valueOfType(String type) {
        if (null == type) {
            return DashBoardMetricListTypeEnum.UNKNOWN;
        }
        
        for (DashBoardMetricListTypeEnum typeEnum : DashBoardMetricListTypeEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }
        
        return DashBoardMetricListTypeEnum.UNKNOWN;
    }

    public static DashBoardMetricListTypeEnum valueOfTypeAndOneLevelType(String type,String oneLevelType) {
        if (null == type) {
            return DashBoardMetricListTypeEnum.UNKNOWN;
        }

        for (DashBoardMetricListTypeEnum typeEnum : DashBoardMetricListTypeEnum.values()) {
            if (type.equals(typeEnum.getType())&&oneLevelType.equals(typeEnum.getOneLevelTypeEnum().getType())) {
                return typeEnum;
            }
        }

        return DashBoardMetricListTypeEnum.UNKNOWN;
    }
    
    public static List<DashBoardMetricListTypeEnum> valueOfTypes(List<String> types) {
        List<DashBoardMetricListTypeEnum> resList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(types)) {
            return resList;
        }
        
        for (String s : types) {
            if (null == s) {
                continue;
            }
            
            for (DashBoardMetricListTypeEnum typeEnum : DashBoardMetricListTypeEnum.values()) {
                if (s.equals(typeEnum.getType())) {
                    resList.add(typeEnum);
                }
            }
        }
        
        return resList;
    }
    
    public static List<String> getFaultTypeList() {
        return Lists.newArrayList(NODE_DEAD.getType(), INDEX_RED.getType(), INDEX_SINGLE_REP.getType(),
                INDEX_BIG_SHARD.getType(), INDEX_SMALL_SHARD.getType(), CLUSTER_ELAPSED_TIME_GTE_5MIN.getType(),
                INDEX_UNASSIGNED_SHARD.getType());
    }
    
    public static List<String> getValueTypeList() {
        return Lists.newArrayList(NODE_WRITE_REJECTED_NUM.getType(), NODE_SEARCH_REJECTED_NUM.getType(),
                NODE_SHARD_NUM.getType(),CLUSTER_SHARD_NUM.getType(), NODE_LARGE_DISK_USAGE.getType(), NODE_LARGE_HEAD.getType(),
                NODE_LARGE_CPU_USAGE.getType(), TEMPLATE_SEGMENT_MEM_NUM.getType(), TEMPLATE_SEGMENT_NUM.getType(),
                INDEX_MAPPING_NUM.getType(), INDEX_SEGMENT_MEM_SIZE.getType(), INDEX_SEGMENT_NUM.getType());
    }
    
}