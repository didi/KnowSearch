package com.didichuxing.datachannel.arius.admin.common.constant;

import org.omg.CORBA.PUBLIC_MEMBER;

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

}
