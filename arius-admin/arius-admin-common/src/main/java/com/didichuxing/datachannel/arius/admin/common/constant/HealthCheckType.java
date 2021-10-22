package com.didichuxing.datachannel.arius.admin.common.constant;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public enum HealthCheckType {
    INDEX_SHARD_NUM_LARGE(
            1,
            "索引shard数过大",
            "检查每个索引的shardNum是否太大, 每个索引的shard大小在保证均匀分配的基础上,不能小于20G",
            1,
            Lists.newArrayList("template", "index", "shard", "node", "rack", "value", "extendInfo")),

    SHARD_COUNT_LARGE(
            2,
            "索引shard条目过多", "检查每个索引的shard文档数是否大多, 每个shard的文档数不能大于5000万条",
            1,
            Lists.newArrayList("template", "index", "shard", "node", "rack", "value", "extendInfo")),

    INDEX_SHARD_NUM_SMALL(
            3,
            "索引shard数过小", "检查每个索引的shardNum是否太小, 每个索引的shard大小不能大于60G",
            1,
            Lists.newArrayList("template", "shard", "rack", "value", "extendInfo")),

    CLUSTER_STATUS(
            4,
            "集群状态", "集群状态检查, 绿色是正常的",
            1,
            Lists.newArrayList("value", "extendInfo")),

    CLUSTER_UNASSIGNED_SHARD(
            5,
            "未初始化shard", "检查集群未初始化的shard个数, 不能存在未初始化的shard",
            1,
            Lists.newArrayList("shard", "value", "extendInfo")),

    CLUSTER_PENDING_TASK(
            6,
            "PendingTask",
            "检查集群Pending Task个数, 不能存在Pending Task",
            1,
            Lists.newArrayList("value", "extendInfo")),

    SHARD_PRI_REP_DOCS_COUNT_SAME(
            7,
            "主从shard条数不一致", "检查集群所以shard的主从副本条数是否一致, 不能存在不一致的shard",
            1,
            Lists.newArrayList("template", "index", "shard", "rack", "value", "extendInfo")),

    INDEX_PER_DOCS_SIZE(
            8,
            "索引文档大小", "检查索引平均每条记录的大小（总容量除以总条数），超过2KB则为不健康",
            1,
            Lists.newArrayList("template", "index", "shard", "rack", "value", "extendInfo")),

    INDEX_TYPE_MAPPING_SIZE(
            9,
            "MappingSize过大", "检查每个索引的mapping，field超过1000个则为不健康",
            1,
            Lists.newArrayList("template", "index", "type", "shard", "rack", "value", "extendInfo")),

    INDEX_SIZE_CHANGE(
            10,
            "索引大小变化", "检查每个索引模板，流量的增量，前一天索引比历史索引容量大一倍，或者小于1/2，则为不健康",
            1,
            Lists.newArrayList("template", "index", "shard", "rack", "value", "extendInfo")),

    INDEX_EXPIRE_NOT_DELETE(
            11,
            "过期索引未删除", "检查每个索引模板，对于有过期时间的模板，是否有过期的索引未删除",
            1,
            Lists.newArrayList("template", "index", "shard", "rack", "extendInfo")),

    INDEX_SHARD_IS_AVERAGE(
            12,
            "Shard是否分配均匀", "检查每个索引的shard是否分配均匀",
            1,
            Lists.newArrayList("template", "index", "shard", "rack", "extendInfo")),

    NODE_SHARD_TOO_MACH(
            13,
            "节点SHARD分配过多", "检查集群节点SHARD是否分配过多",
            1,
            Lists.newArrayList("node", "value", "extendInfo")),

    INVALID_INDICES_IN_ES(
            14,
            "ES中非法索引", "检查ES中没有模板的索引",
            1,
            Lists.newArrayList("index", "extendInfo")),

    EMPTY_INDICES(
            15,
            "30天无数据索引", "检查ES中接入超过30天,且近30天无数据",
            1,
            Lists.newArrayList("template", "shard", "rack", "extendInfo")),

    INDEX_REQUIRED_RELEASED(
            16,
            "不健康索引", "60天总访问量低,健康分低的索引,需要通知用户下线",
            1,
            Lists.newArrayList("template", "extendInfo")),

    UNKNOWN(-1, "未知项", "", 0, Lists.newArrayList(""));

    private int code;

    private String name;

    private int interval;

    private String desc;

    private List<String> errInfoTitles;

    HealthCheckType(int code, String name, String desc, int interval, List<String> errInfoTitles) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.interval = interval;
        this.errInfoTitles = errInfoTitles;
    }

    public List<String> getErrInfoTitles() {
        return errInfoTitles;
    }

    public String getName() {
        return name;
    }

    public int getInterval() {
        return interval;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static HealthCheckType valueOf(Integer code) {
        if (code == null) {
            return HealthCheckType.UNKNOWN;
        }
        for (HealthCheckType state : HealthCheckType.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return HealthCheckType.UNKNOWN;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("name", getName());
        map.put("code", getCode());
        return map;
    }

    public static List<Map<String, Object>> getMaps() {
        List<Map<String, Object>> result = Lists.newArrayList();
        for (HealthCheckType state : HealthCheckType.values()) {
            result.add(state.toMap());
        }
        return result;
    }
}
