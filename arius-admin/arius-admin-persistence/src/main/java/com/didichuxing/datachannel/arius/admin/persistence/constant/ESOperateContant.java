package com.didichuxing.datachannel.arius.admin.persistence.constant;

public class ESOperateContant {

    private ESOperateContant() {
    }

    /**
     * rebalance配置名字
     */
    public static final String  REBALANCE                           = "cluster.routing.rebalance.enable";

    /**
     * remote-cluster
     */
    public static final String  REMOTE_CLUSTER_FORMAT               = "cluster.remote.%s.seeds";

    /**
     * 操作es超时时间 单位s
     */
    public static final Integer ES_OPERATE_TIMEOUT                  = 30;

    /**
     * rack配置名字
     */
    public static final String  TEMPLATE_INDEX_INCLUDE_RACK         = "index.routing.allocation.include.rack";
    public static final String  INDEX_INCLUDE_RACK                  = "routing.allocation.include.rack";

    /**
     * read-only
     */
    public static final String  INDEX_BLOCKS_WRITE                  = "blocks.write";

    /**
     * write-only
     */
    public static final String  INDEX_BLOCKS_READ                   = "blocks.read";

    /**
     * shard配置名字
     */
    public static final String  INDEX_SHARD_NUM                     = "index.number_of_shards";
    public static final String  INDEX_SHARD_ROUTING_NUM             = "index.number_of_routing_size";

    /**
     * 模板默认order
     */
    public static final Long    TEMPLATE_DEFAULT_ORDER              = 10L;

    /**
     * 节点rack配置名称
     */
    public static final String  NODE_STATS_RACK                     = "node.rack";
    public static final String  RACK                                = "rack";

    /**
     * 高版本role——data\client\master\ingest
     */
    public static final String  ES_ROLE_DATA                        = "data";
    public static final String  ES_ROLE_COORDINATING_ONLY           = "coordinating_only";
    public static final String  ES_ROLE_CLIENT                      = "client";
    public static final String  ES_ROLE_MASTER                      = "master";
    public static final String  ES_ROLE_INGEST                      = "ingest";
    public static final String  ES_ROLE_ML                          = "ml";

    public static final String  CLUSTER_ROUTING_ALLOCATION_OUTGOING = "cluster.routing.allocation.node_concurrent_outgoing_recoveries";
    public static final String  CLUSTER_ROUTING_ALLOCATION_INGOING  = "cluster.routing.allocation.node_concurrent_incoming_recoveries";
    public static final String  COLD_MAX_BYTES_PER_SEC_KEY          = "indices.recovery.ceph_max_bytes_per_sec";

    /**
     * 单type配置项
     */
    public static final String  SINGLE_TYPE                         = "index.mapping.single_type";

    /**
     * 指标看板相关
     */
    public static final String  STATUS                              = "status";
    public static final String  INDICES                             = "indices";
    public static final String  COUNT                               = "count";
    public static final String  SHARDS                              = "shards";
    public static final String  TOTAL                               = "total";
    public static final String  DOCS                                = "docs";
    public static final String  NODES                               = "nodes";
    public static final String  OS                                  = "os";
    public static final String  MEM                                 = "mem";
    public static final String  TOTAL_IN_BYTES                      = "total_in_bytes";
    public static final String  FREE_IN_BYTES                       = "free_in_bytes";
    public static final String  USED_IN_BYTES                       = "used_in_bytes";
    public static final String  FREE_PERCENT                        = "free_percent";
    public static final String  USED_PERCENT                        = "used_percent";
    public static final String  FS                                  = "fs";

    /**
     * 索引管理
     */
    public static final String  READ                                = "index.blocks.read";
    public static final String  WRITE                               = "index.blocks.write";
    public static final String  BLOCKS                              = "blocks";
    public static final String  INDEX                               = "index";
    public static final String  DEFAULTS                            = "defaults";
    public static final String  PRIMARY                             = "p";
}
