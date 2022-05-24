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
     * arius的common配置
     */
    public static final String ARIUS_COMMON_GROUP                              = "arius.common.group";

    public static final String ARIUS_TEMPLATE_COLD                             = "arius.template.cold";

    public static final String ARIUS_QUOTA_CONFIG_GROUP                        = "arius.quota.config.group";

    public static final String ARIUS_QUOTA_CONFIG_TPS_PER_CPU_WITH_REPLICA     = "arius.quota.config.tps.per.cpu.with.replica";

    public static final String ARIUS_QUOTA_CONFIG_TPS_PER_CPU_NO_REPLICA       = "arius.quota.config.tps.per.cpu.NO.replica";

    public static final String ARIUS_QUOTA_CONFIG_COST_PER_G_PER_MONTH         = "arius.quota.config.cost.per.g.per.month";

    public static final String QUOTA_DYNAMIC_LIMIT_BLACK_LOGIC_ID              = "quota.dynamic.limit.black.logic_id";

    public static final String QUOTA_DYNAMIC_LIMIT_BLACK_CLUSTER               = "quota.dynamic.limit.black.cluster";

    public static final String QUOTA_DYNAMIC_LIMIT_BLACK_APP_IDS               = "quota.dynamic.limit.black.app_ids";

    public static final String ARIUS_WO_AUTO_PROCESS_CREATE_TEMPLATE_DISK_MAXG = "arius.wo.auto.process.create.template.disk.maxG";

    /**
     * 节点是否并行获取
     */
    public static final String NODE_STAT_COLLECT_CONCURRENT                    = "node_stat.collect.concurrent";

    /**
     * 索引是否并行获取
     */
    public static final String INDEX_STAT_COLLECT_CONCURRENT                   = "index_stat.collect.concurrent";

    public static final String INDEX_TEMPLATE_COLD_DAY_DEFAULT                 = "index_template.cold.day.default";

    public static final String REQUEST_INTERCEPTOR_SWITCH_OPEN                 = "request.interceptor.switch.open";

    public static final String INDEX_OPERATE_AHEAD_SECONDS                     = "index.operate.ahead.seconds";

    public static final String CLUSTERS_INDEX_EXPIRE_DELETE_AHEAD              = "clusters.index.expire.delete.ahead";

    public static final String APP_DEFAULT_READ_AUTH_INDICES                   = "app.default.read.auth.indices";

    /**
     * 集群安装包版本列表
     */
    public static final String CLUSTER_PACKAGE_VERSION_LIST                    = "cluster.package.version_list";

    public static final String CLUSTER_DATA_CENTER_LIST                        = "cluster.data.center_list";

    public static final String CLUSTER_RESOURCE_TYPE_LIST                      = "cluster.resource.type_list";

    public static final String CLUSTER_NODE_COUNT_LIST                         = "cluster.node.count_list";

    public static final String LOGIC_TEMPLATE_TIME_FORMAT_LIST                 = "logic.template.time_format_list";

    public static final String CLUSTER_NODE_SPECIFICATION_LIST                 = "cluster.node.specification_list";

    public static final String TEMPLATE_PHYSIC_INDICES_ALLOCATION_IS_EFFECTIVE = "template.physic.indices.allocation.is_effective";

    /**
     * default value
     */
    public static final String CLUSTER_PACKAGE_VERSION_LIST_DEFAULT_VALUE      = "7.6.2,7.6.0,7.6.0.1400,6.6.1.900";

    public static final String CLUSTER_DATA_CENTER_LIST_DEFAULT_VALUE          = "cn,en";

    public static final String CLUSTER_RESOURCE_TYPE_LIST_DEFAULT_VALUE        = "信创,acs,vmware";

    public static final String CLUSTER_NODE_COUNT_LIST_DEFAULT_VALUE           = "2,4,6,8,10,20";

    public static final String LOGIC_TEMPLATE_TIME_FORMAT_LIST_DEFAULT_VALUE   = "yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss.SSS,yyyy-MM-dd HH:mm:ss.SSS Z,"
                                                                                 + "yyyy-MM-dd'T'HH:mm:ss,yyyy-MM-dd'T'HH:mm:ss.SSS,yyyy-MM-dd'T'HH:mm:ssZ,yyyy-MM-dd'T'HH:mm:ss.SSSZ,yyyy/MM/dd HH:mm:ss,epoch_second,epoch_millis,yyyy-MM-dd";
    public static final String CLUSTER_NODE_SPECIFICATION_LIST_DEFAULT_VALUE   = "16c-64g-3072g,16c48g3072g";

}
