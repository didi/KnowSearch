package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * 工程常量
 * <p>
 * 如果配置的量过多,需要拆解
 *
 * @author d06679
 * @date 2018/5/24
 */
public class AriusConfigConstant {

    private AriusConfigConstant() {
    }

    /**
     * arius的common配置组
     */
    public static final String ARIUS_COMMON_GROUP                                      = "arius.common.group";

    /**
     * 集群版本列表
     */
    public static final String CLUSTER_PACKAGE_VERSION_LIST                            = "cluster.package.version_list";

    /**
     * 数据中心列表
     */
    public static final String CLUSTER_DATA_CENTER_LIST                                = "cluster.data.center_list";

    /**
     * 集群资源列表
     */
    public static final String CLUSTER_RESOURCE_TYPE_LIST                              = "cluster.resource.type_list";

    /**
     * 节点数量列表
     */
    public static final String CLUSTER_NODE_COUNT_LIST                                 = "cluster.node.count_list";

    /**
     * 模板时间格式列表
     */
    public static final String LOGIC_TEMPLATE_TIME_FORMAT_LIST                         = "logic.template.time_format_list";

    /**
     * 节点规格列表
     */
    public static final String CLUSTER_NODE_SPECIFICATION_LIST                         = "cluster.node.specification_list";

    /**
     * 历史索引模板shard分配是否自动调整
     */
    public static final String HISTORY_TEMPLATE_PHYSIC_INDICES_ALLOCATION_IS_EFFECTIVE = "history.template.physic.indices.allocation.is_effective";

    /**
     * 请求是否拦截鉴权
     */
    public static final String REQUEST_INTERCEPTOR_SWITCH_OPEN                         = "request.interceptor.switch.open";

    /**
     * meta监控配置组
     */
    public static final String ARIUS_META_MONITOR_GROUP                                = "arius.meta.monitor.group";

    /**
     * 节点是否并行获取
     */
    public static final String NODE_STAT_COLLECT_CONCURRENT                            = "node_stat.collect.concurrent";

    /**
     * 索引是否并行获取
     */
    public static final String INDEX_STAT_COLLECT_CONCURRENT                           = "index_stat.collect.concurrent";

    /**
     * dashboard阈值配置组
     */
    public static final String ARIUS_DASHBOARD_THRESHOLD_GROUP                         = "arius.dashboard.threshold.group";


    /**
     * 小Shard列表阈值定义
     */
    public static final String INDEX_SHARD_SMALL_THRESHOLD                             = "index.shard.small_threshold";

    /**
     * 大Shard列表阈值定义
     */
    public static final String NODE_SHARD_BIG_THRESHOLD                                = "node.shard.big_threshold";

    /**
     * 索引模板Segment内存大小列表阈值定义
     */
    public static final String INDEX_TEMPLATE_SEGMENT_MEMORY_SIZE_THRESHOLD            = "index.template.segment_memory_size_threshold";

    /**
     * 索引Segment内存大小列表阈值定义
     */
    public static final String INDEX_SEGMENT_MEMORY_SIZE_THRESHOLD                     = "index.segment.memory_size_threshold";

    /**
     * 索引模板Segment个数阈值定义
     */
    public static final String INDEX_TEMPLATE_SEGMENT_COUNT_THRESHOLD                  = "index.template.segment_count_threshold";

    /**
     * 索引Segment个数阈值定义
     */
    public static final String INDEX_SEGMENT_NUM_THRESHOLD                             = "index.segment.num_threshold";

    /**
     * 索引Mapping个数阈值定义
     */
    public static final String INDEX_MAPPING_NUM_THRESHOLD                             = "index.mapping.num_threshold";





    /**
     * 模板冷热配置组
     */
    public static final String ARIUS_TEMPLATE_COLD_GROUP                               = "arius.template.cold.group";

    /**
     * 索引模板默认冷存天数
     */
    public static final String INDEX_TEMPLATE_COLD_DAY_DEFAULT                         = "index_template.cold.day.default";

    /**
     * default value
     */
    public static final String CLUSTER_PACKAGE_VERSION_LIST_DEFAULT_VALUE              = "7.6.2,7.6.0,7.6.0.1400,6.6.1.900";

    public static final String CLUSTER_DATA_CENTER_LIST_DEFAULT_VALUE                  = "cn,en";

    public static final String CLUSTER_RESOURCE_TYPE_LIST_DEFAULT_VALUE                = "信创,acs,vmware";

    public static final String CLUSTER_NODE_COUNT_LIST_DEFAULT_VALUE                   = "2,4,6,8,10,20";

    public static final String LOGIC_TEMPLATE_TIME_FORMAT_LIST_DEFAULT_VALUE           = "yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss.SSS,yyyy-MM-dd HH:mm:ss.SSS Z,"
                                                                                         + "yyyy-MM-dd'T'HH:mm:ss,yyyy-MM-dd'T'HH:mm:ss.SSS,yyyy-MM-dd'T'HH:mm:ssZ,yyyy-MM-dd'T'HH:mm:ss.SSSZ,yyyy/MM/dd HH:mm:ss,epoch_second,epoch_millis,yyyy-MM-dd";
    public static final String CLUSTER_NODE_SPECIFICATION_LIST_DEFAULT_VALUE           = "16c-64g-3072g,16c-48g-3072g";

}