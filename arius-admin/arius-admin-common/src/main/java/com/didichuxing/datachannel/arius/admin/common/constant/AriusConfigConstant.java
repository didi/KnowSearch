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
     * arius的common配置组
     */
    public static final String  ARIUS_TEMPLATE_GROUP                                      = "arius.template.group";

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
     * 模板业务类型
     */
    public static final String LOGIC_TEMPLATE_BUSINESS_TYPE_LIST                       = "logic.template.business_type";

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
    public static final String REQUEST_INTERCEPTOR_SWITCH_OPEN                         = "request.interceptor.is_switch_open";

    /**
     * meta监控配置组
     */
    public static final String ARIUS_META_MONITOR_GROUP                                = "arius.meta.monitor.group";

    /**
     * 节点是否并行获取
     */
    public static final String NODE_STAT_COLLECT_CONCURRENT                            = "node.stat.is_collect_concurrent";

    /**
     * 索引是否并行获取
     */
    public static final String INDEX_STAT_COLLECT_CONCURRENT                           = "index.stat.is_collect_concurrent";


    /**
     * dashboard阈值配置组
     */
    public static final String ARIUS_DASHBOARD_THRESHOLD_GROUP                         = "arius.dashboard.threshold.group";


    /**
     * 小Shard列表阈值定义(小Shard索引列表)
     */
    public static final String INDEX_SHARD_SMALL_THRESHOLD                             = "index.shard.small_threshold";

    /**
     * 节点分片个数(节点分片个数大于>500)
     */
    public static final String NODE_SHARD_NUM_THRESHOLD = "node.shard.num_threshold";

    /**
     * 索引模板Segment内存大小列表阈值定义
     */
    public static final String INDEX_TEMPLATE_SEGMENT_MEMORY_SIZE_THRESHOLD            = "index.template.segment_memory_size_threshold";

    /**
     * 索引Segment内存大小列表阈值定义
     */
    public static final String INDEX_SEGMENT_MEMORY_SIZE_THRESHOLD                     = "index.segment.memory_size_threshold";

    /**
     * 索引模板Segment个数阈值定义(索引模板Segments个数超过1000)
     */
    public static final String INDEX_TEMPLATE_SEGMENT_COUNT_THRESHOLD                  = "index.template.segment_num_threshold";

    /**
     * 索引Segment个数阈值定义(索引Segments个数超过阀值100)
     */
    public static final String INDEX_SEGMENT_NUM_THRESHOLD                             = "index.segment.num_threshold";

    /**
     * 索引Mapping个数阈值定义
     */
    public static final String INDEX_MAPPING_NUM_THRESHOLD                             = "index.mapping.num_threshold";

    /**
     * 采集延时阈值定义
     */
    public static final String DASHBOARD_CLUSTER_METRIC_COLLECTOR_DELAYED_THRESHOLD = "cluster.metric.collector.delayed_threshold ";
    /**
     * 磁盘利用率超红线阈值
     */
    public static final String NODE_DISK_USED_PERCENT_THRESHOLD = "node.disk.used_percent_threshold";
    /**
     * 堆内存利用率超红线阈值
     */
    public static final String NODE_LARGE_HEAD_USAGE_PERCENT_THRESHOLD                 = "node.jvm.heap.used_percent_threshold";
    /**
     * 堆内存利用率持续时间红线
     */
    public static final String NODE_LARGE_HEAD_USED_PERCENT_TIME_USAGE_THRESHOLD      = "node.jvm.heap.used_percent_time_duration_threshold";
    /**
     * CPU利用率超红线
     */
    public static final String NODE_LARGE_CPU_USAGE_PERCENT_THRESHOLD                  = "node.cpu.used_percent_threshold";
    /**
     * cpu利用率持续时间超红线阈值
     */
    public static final String NODE_CPU_USED_PERCENT_THRESHOLD_TIME_DURATION_THRESHOLD = "node.cpu.used_percent_threshold_time_duration_threshold";
    /**
     * 大Shard列表阈值定义(大Shard索引列表)
     */
    public static final String INDEX_SHARD_BIG_THRESHOLD                              = "index.shard.big_threshold";


    /**
     * rollover阈值，当索引大小超过该值自动rollover
     */
    public static final String INDEX_ROLLOVER_THRESHOLD                                = "index.rollover.threshold";

    /**
     * 大shard阈值，用于集群指标看板，超过该值被认为是大shard
     */
    public static final String BIG_SHARD_THRESHOLD                                     = "cluster.shard.big_threshold";

    /**
     * 集群shard数阈值
     */
    public static final String CLUSTER_SHARD_NUM_THRESHOLD                             = "cluster.shard.num_threshold";

    /**
     * 模板冷热配置组
     */
    public static final String ARIUS_TEMPLATE_COLD_GROUP                               = "arius.template.cold.group";

    /**
     * 索引模板默认冷存天数
     */
    public static final String INDEX_TEMPLATE_COLD_DAY_DEFAULT                         = "index.template.default_cold_day";

    /**
     * default value
     */
    /**
     * 请求是否拦截鉴权
     */
    public static final Boolean REQUEST_INTERCEPTOR_SWITCH_OPEN_DEFAULT_VALUE         = Boolean.TRUE;

    /**
     * 索引模板默认冷存天数默认值
     */
    public static final String INDEX_TEMPLATE_COLD_DAY_DEFAULT_VALUE                   = "-1";
    /**
     * 历史索引模板shard分配是否自动调整
     */
    public static final boolean HISTORY_TEMPLATE_PHYSIC_INDICES_ALLOCATION_IS_EFFECTIVE_DEFAULT_VALUE = true;
    public static final String CLUSTER_PACKAGE_VERSION_LIST_DEFAULT_VALUE              = "7.6.2,7.6.0,7.6.0.1400,6.6.1.900";

    public static final String CLUSTER_DATA_CENTER_LIST_DEFAULT_VALUE                  = "cn,en";

    public static final String CLUSTER_RESOURCE_TYPE_LIST_DEFAULT_VALUE                = "信创,ACS,VMWARE";

    public static final String CLUSTER_NODE_COUNT_LIST_DEFAULT_VALUE                   = "2,4,6,8,10,20";

    public static final String LOGIC_TEMPLATE_TIME_FORMAT_LIST_DEFAULT_VALUE           = "yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss.SSS,yyyy-MM-dd HH:mm:ss.SSS Z," +
            "                                                                               yyyy-MM-dd'T'HH:mm:ss,yyyy-MM-dd'T'HH:mm:ss.SSS,yyyy-MM-dd'T'HH:mm:ssZ,yyyy-MM-dd'T'HH:mm:ss.SSSZ,yyyy/MM/dd HH:mm:ss,epoch_second,epoch_millis,yyyy-MM-dd";

    public static final String LOGIC_TEMPLATE_BUSINESS_TYPE_LIST_DEFAULT_VALUE         = "系统数据,日志数据,业务上报数据,RDS数据,离线导入数据";

    public static final String CLUSTER_NODE_SPECIFICATION_LIST_DEFAULT_VALUE           = "16c-64g-3072g,16c-48g-3072g";

    /**
     * 节点状态并发采集
     */
    public static final boolean NODE_STAT_COLLECT_CONCURRENT_DEFAULT_VALUE              = false;

    /**
     * 索引状态并发采集
     */
    public static final boolean INDEX_STAT_COLLECT_CONCURRENT_DEFAULT_VALUE             = false;

    /**
     * 采集延时阈值定义
     */
    public static final String DASHBOARD_CLUSTER_METRIC_COLLECTOR_DELAYED_DEFAULT_VALUE                         = "{\"name\":\"cluster.metric.collector.delayed_threshold\",\"metrics\":\"clusterMetricCollectorTimeDelayMin\",\"unit\":\"m\",\"compare\":\">\",\"value\":5}";

    /**
     * dashboard节点CPU利用率超阈值的[持续时间]默认值
     */
    public static final String DASHBOARD_NODE_CPU_USED_PERCENT_THRESHOLD_TIME_DURATION_THRESHOLD_DEFAULT_VALUE = "{\"name\":\"node.cpu.used_percent_threshold_time_duration\":\"cpuUsedPercentThresholdTimeDuration\",\"unit\":\"m\",\"compare\":\">\",\"value\":5}";

    /**
     * dashboardCPU利用率红线默认值
     */
    public static final String DASHBOARD_CPU_PERCENT_THRESHOLD_DEFAULT_VALUE                                   = "{\"name\":\"node.cpu.used_percent_threshold\",\"metrics\":\"cpuUsedPercentThreshold\",\"unit\":\"%\",\"compare\":\">\",\"value\":60}";

    /**
     * dashboard内存利用率持续时间红线默认值
     */
    public static final String DASHBOARD_LARGE_HEAD_USED_PERCENT_TIME_DEFAULT_VALUE                            = "{\"name\":\"node.jvm.heap.used_percent_threshold_time_duration_threshold\",\"metrics\":\"jvmHeapUsedPercentThresholdTimeDuration\",\"unit\":\"m\",\"compare\":\">\",\"value\":10}";
    /**
     * dashboard堆内存利用率超红线阈值默认值
     */
    public static final String DASHBOARD_HEAD_USED_PERCENT_THRESHOLD_DEFAULT_VALUE                             = "{\"name\":\"node.jvm.heap.used_percent_threshold\",\"metrics\":\"jvmHeapUsedPercentThreshold\",\"unit\":\"%\",\"compare\":\">\",\"value\":75}";

    /**
     * 磁盘利用率超红线阈值默认值
     */
    public static final String DASHBOARD_NODE_DISK_USED_PERCENT_THRESHOLD_DEFAULT_VALUE                        = "{\"name\":\"node.disk.used_percent_threshold\",\"metrics\":\"diskUsedPercentThreshold\",\"unit\":\"%\",\"compare\":\">\",\"value\":80}";

    /**
     * 索引Segment个数阈值默认值
     */
    public static final String DASHBOARD_INDEX_SEGMENT_NUM_THRESHOLD_DEFAULT_VALUE                             = "{\"name\":\"index.segment.num_threshold\",\"metrics\":\"segmentNum\",\"unit\":\"个\",\"compare\":\">\",\"value\":100}";
    /**
     * 索引模板Segment个数阈值定义默认值
     */
    public static final String DASHBOARD_INDEX_TEMPLATE_SEGMENT_COUNT_THRESHOLD_DEFAULT_VALUE                  = "{\"name\":\"index.template.segment_num_threshold\",\"metrics\":\"segmentNum\",\"unit\":\"个\",\"compare\":\">\",\"value\":700}";
    /**
     * 索引Segment内存大小列表阈值定义默认值
     */
    public static final String DASHBOARD_INDEX_SEGMENT_MEMORY_SIZE_THRESHOLD_DEFAULT_VALUE                     = "{\"name\":\"index.segment.memory_size_threshold\",\"metrics\":\"segmentMemSize\",\"unit\":\"MB\",\"compare\":\">\",\"value\":500}";
    /**
     * 索引模板Segment内存大小列表阈值定义默认值
     */
    public static final String DASHBOARD_INDEX_TEMPLATE_SEGMENT_MEMORY_SIZE_THRESHOLD_DEFAULT_VALUE            = "{\"name\":\"index.template.segment.memory_size_threshold\",\"metrics\":\"segmentMemSize\",\"unit\":\"MB\",\"compare\":\">\",\"value\":3000}";
    /**
     * 大Shard列表阈值定义(大Shard索引列表)默认值
     */
    public static final String DASHBOARD_INDEX_SHARD_BIG_THRESHOLD_DEFAULT_VALUE                               = "{\"name\":\"index.shard.big_threshold\",\"metrics\":\"shardSize\",\"unit\":\"GB\",\"compare\":\">\",\"value\":20}";
    /**
     * 小Shard列表阈值定义(小Shard索引列表)默认值
     */
    public static final String DASHBOARD_INDEX_SHARD_SMALL_THRESHOLD_DEFAULT_VALUE                             = "{\"name\":\"index.shard.small_threshold\",\"metrics\":\"shardSize\",\"unit\":\"MB\",\"compare\":\"<\",\"value\":1000}";
    /**
     * 索引Mapping个数阈值定义默认值
     */
    public static final String DASHBOARD_INDEX_MAPPING_NUM_THRESHOLD_DEFAULT_VALUE                             = "{\"name\":\"index.mapping.num_threshold\",\"metrics\":\"mappingNum\",\"unit\":\"个\",\"compare\":\">\",\"value\":100}";
    /**
     * 集群shard数阈值默认值
     */
    public static final String DASHBOARD_CLUSTER_SHARD_NUM_THRESHOLD_DEFAULT_VALUE                            = "{\"name\":\"cluster.shard.num_threshold\",\"metrics\":\"shardNum\",\"unit\":\"个\",\"compare\":\">\",\"value\":2000}";
    /**
     * 节点分片个数(节点分片个数大于>500)
     */
    public static final String DASHBOARD_NODE_SHARD_NUM_THRESHOLD_DEFAULT_VALUE                               = "{\"name\":\"node.shard.num_threshold\",\"metrics\":\"shardNum\",\"unit\":\"个\",\"compare\":\">\",\"value\":1000}";
    /**
     * 集群大shard阈值定义
     */
    public static final double BIG_SHARD                                                                      = 10d;
}