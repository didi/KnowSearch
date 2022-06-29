package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: dashboard限流值
 * @author gyp
 * @date 2022/6/22 19:44
 * @version 1.0
 */
public enum DashBoardThresholdEnum {

    BIG_COPY_INDEX_CLUSTER_BLACKLIST("bigCopyIndexClusterBlacklist", "大副本索引集群黑名单","[]"),
    INDEX_TEMPLATE_SEGMENTS_MEMORY_SIZE("template_segmentMemSize", "索引模板Segements内存大小","1"),
    INDEX_TEMPLATE_SEGMENTS_NUM("template_segmentNum", "索引模板Segments个数","1000"),
    INDEX_MAPPING_NUM("index_mappingNum", "索引Mapping个数","100"),
    NODE_SHARD_NUM("node_shardNum", "节点分片个数","500"),
    INDEX_SEGMENT_MEMORY_SIZE("index_segmentMemSize", "索引Sements内存大小","1"),
    INDEX_SEGMENT_NUM("index_segmentNum", "索引Segements个数","1000"),
    INDEX_SMALL_SHARD_LIST("index_smallShard", "小Shard索引列表(shard个数大于1)","1");


    DashBoardThresholdEnum(String name, String desc,String value) {
        this.name = name;
        this.desc = desc;
        this.value = value;
    }

    private String name;
    private String desc;

    private String value;

    public String getName() {
        return name;
    }
    public String getValue() { return value; }

    public static Map getDashBoardThresholdValue() {
        Map<String, String> map = new HashMap();
        map.put(BIG_COPY_INDEX_CLUSTER_BLACKLIST.getName(), BIG_COPY_INDEX_CLUSTER_BLACKLIST.getValue());
        map.put(INDEX_TEMPLATE_SEGMENTS_MEMORY_SIZE.getName(), INDEX_TEMPLATE_SEGMENTS_MEMORY_SIZE.getValue());
        map.put(INDEX_TEMPLATE_SEGMENTS_NUM.getName(), INDEX_TEMPLATE_SEGMENTS_NUM.getValue());
        map.put(INDEX_MAPPING_NUM.getName(), INDEX_MAPPING_NUM.getValue());
        map.put(NODE_SHARD_NUM.getName(), NODE_SHARD_NUM.getValue());
        map.put(INDEX_SEGMENT_MEMORY_SIZE.getName(), INDEX_SEGMENT_MEMORY_SIZE.getValue());
        map.put(INDEX_SMALL_SHARD_LIST.getName(), INDEX_SMALL_SHARD_LIST.getValue());
        map.put(INDEX_SEGMENT_NUM.getName(), INDEX_SEGMENT_NUM.getValue());
        return map;
    }
}