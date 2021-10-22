package com.didichuxing.datachannel.arius.admin.persistence.constant;

/**
 * @author d06679
 * @date 2019/3/20
 */
public class ESOperateContant {

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

    /**
     * 高版本role——data
     */
    public static final String  ES_ROLE_DATA                        = "data";

    /**
     * 高版本role——master
     */
    public static final String  ES_ROLE_MASTER                      = "master";

    public static final String  CLUSTER_ROUTING_ALLOCATION_OUTGOING = "cluster.routing.allocation.node_concurrent_outgoing_recoveries";
    public static final String  CLUSTER_ROUTING_ALLOCATION_INGOING  = "cluster.routing.allocation.node_concurrent_incoming_recoveries";
    public static final String  COLD_MAX_BYTES_PER_SEC_KEY          = "indices.recovery.ceph_max_bytes_per_sec";

    /**
     * 单type配置项
     */
    public static final String SINGLE_TYPE                          = "index.mapping.single_type";
}
