package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: dashboard限流值
 * @author gyp
 * @date 2022/6/22 19:44
 * @version 1.0
 */
public enum DashBoardThresholdEnum {

    BIG_COPY_INDEX_CLUSTER_BLACKLIST(OneLevelTypeEnum.UNKNOWN,"bigCopyIndexClusterBlacklist", "大副本索引集群黑名单","[]"),
    INDEX_TEMPLATE_SEGMENTS_MEMORY_SIZE(OneLevelTypeEnum.CLUSTER,"indexTemplateSegementsMemorySize", "索引模板Segements内存大小",""),
    INDEX_TEMPLATE_SEGMENTS_NUM(OneLevelTypeEnum.INDEX,"indexTemplateSegmentsNum", "索引模板Segments个数","1000"),
    INDEX_MAPPING_NUM(OneLevelTypeEnum.INDEX,"indexMappingNum", "索引Mapping个数","100"),
    NODE_SHARD_NUM(OneLevelTypeEnum.NODE,"nodeShardnUM", "节点分片个数","500"),
    INDEX_SEGMENT_MEMORY_SIZE(OneLevelTypeEnum.NODE,"indexSegmentMemorySize", "索引Sements内存大小","1"),
    INDEX_SEGMENT_NUM(OneLevelTypeEnum.NODE,"indexSegmentNum", "索引Segements个数","1000"),
    INDEX_SMALL_SHARD_LIST(OneLevelTypeEnum.NODE,"indexSmallShardList", "小Shard索引列表(shard个数大于1)","1");


    DashBoardThresholdEnum(OneLevelTypeEnum oneLevelTypeEnum , String name, String desc,String value) {
        this.oneLevelTypeEnum = oneLevelTypeEnum;
        this.name = name;
        this.desc = desc;
        this.value = value;
    }

    private OneLevelTypeEnum oneLevelTypeEnum;
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
