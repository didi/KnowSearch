package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

public enum DashBoardMetricThresholdValueNameEnum {
    
    SHARD_SIZE("shardSize", "索引下的shard大小字段"),
    SEGMENT_NUM("segmentNum", "索引Segements个数d的字段");
    
    private String value;
    private String desc;
    
    DashBoardMetricThresholdValueNameEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
}