package com.didichuxing.datachannel.arius.admin.common.constant;

import com.didiglobal.logi.log.util.HostUtil;

import java.util.Arrays;
import java.util.List;

/**
 *
 * 工程常量
 *
 * 如果配置的量过多,需要拆解
 *
 */
public class AdminConstant {

    private AdminConstant() {
    }

    public static final String       DEFAULT_TYPE                            = "type";

    public static final String       DEFAULT_INDEX_MAPPING_TYPE              = "_doc";

    public static final String       SINGLE_TYPE_KEY                         = "index.mapping.single_type";
    public static final String       DEFAULT_SINGLE_TYPE                     = "true";

    public static final String       HOST_NAME                               = HostUtil.getHostName();

    public static final Integer      YES                                     = 1;

    public static final Integer      NO                                      = 0;

    public static final Integer      NOT_EXPIRE                              = -1;

    public static final Double       BYTE_TO_G                               = 1.0 / (1024 * 1024 * 1024);

    public static final Double       TO_W                                    = 1.0 / 10000;

    public static final Double       G_PER_SHARD                             = 50.0;

    /**
     * Rack相关常量
     */
    public static final String       RACK_COMMA                              = ",";

    public static final String       COMMA                                   = ",";

    /**
     * 默认的插件版本
     */
    public static final String       DEFAULT_PLUGIN_VERSION                  = "0.0.0.1";

    /**
     * 集群和Rack分割符
     */
    public static final String       CLUSTER_RACK_COMMA                      = "@";

    public static final String       DCDR_TASK_BIZ_COMMA                     = "@";

    public static final String       INCLUDE_CLUSTER_ALL_RACK                = "all";

    public static final String       COLD_RACK_PREFER                        = "c";

    public static final String       DEFAULT_COLD_RACK                       = "cold";

    public static final String       DEFAULT_HOT_RACK                        = "*";

    public static final Long         MILLIS_PER_DAY                          = 24 * 60 * 60 * 1000L;

    public static final Long         SECONDS_PER_DAY                         = 24 * 60 * 60L;

    public static final String       MM_DD_DATE_FORMAT                       = "MMdd";
    public static final String       YY_MM_DD_DATE_FORMAT                    = "_yyyy-MM-dd";
    public static final String       YY_MM_DATE_FORMAT                       = "_yyyy-MM";

    public static final Integer      PIPELINE_RATE_LIMIT_MAX_VALUE           = 1000000;
    public static final Integer      PIPELINE_RATE_LIMIT_MIN_VALUE           = 1000;

    /**
     * 容量规划空闲Rack针对新需求的比例
     */
    public static final Double       FREE_RACK_FOR_NEW_DEMAND_RATE           = 0.4;

    /**
     * 最小可申请资源Quota
     */
    public static final Double       MIN_QUOTA                               = 50.0 / (7 * 1024);

    /**
     * 默认写标识
     */
    public static final Boolean      DEFAULT_WRITER_FLAGS                    = true;

    /**
     * 默认组ID
     */
    public static final String       DEFAULT_GROUP_ID                        = "DEFAULT_GROUP_ID";

    /**
     * 校验模板mappings or settings信息前缀
     */
    public static final String       ES_CHECK_TEMPLATE_INDEX_PREFIX          = "check_template_";

    /**
     * 版本为2.3.3版本的ES物理集群列表
     */
    public static final List<String> LOW_VERSION_ES_CLUSTER                  = Arrays.asList("bigdata-arius-olap",
        "bigdata-arius-arcs", "mtn-to-es", "kefu-es", "DSearch_cluster_py");

    /**
     * 默认创建隔日索引标识
     */
    public static final Boolean      DEFAULT_PRE_CREATE_FLAGS                = true;

    /**
     * 是否禁用索引资源标识
     */
    public static final Boolean      DISABLE_SOURCE_FLAGS                    = false;

    public static final Integer      DEFAULT_SHARD_NUM                       = 1;

    /**
     * 默认APP ID
     */
    public static final Integer      DEFAULT_APP_ID                          = 1;

    /**
     * 数据最短保存时长
     */
    public static final int          PLATFORM_EXPIRE_TIME_MIN                = 2;
    public static final int          PLATFORM_HOT_TIME_MIN                   = 1;
    public static final int          PLATFORM_DELETED_TEMPLATE_EXPIRED_TIME  = 7;

    /**
     * 默认物理Client集群Http服务端口号
     */
    public static final Integer      DEFAULT_PHYSIC_CLUSTER_HTTP_SERVER_PORT = 8060;

    public static final String       CLIENT_CLUSTER_HEALTH_CHECK_SUFFIX      = DEFAULT_PHYSIC_CLUSTER_HTTP_SERVER_PORT
                                                                               + "/_cluster/health?pretty";

    public static boolean yesOrNo(int config) {
        return config == YES || config == NO;
    }

    /**
     * region没有被绑定到逻辑集群时的逻辑集群ID字段值
     */
    public static final String   REGION_NOT_BOUND_LOGIC_CLUSTER_ID           = "-1";

    public static final String JOB_SUCCESS                                   = "success";
    public static final String JOB_FAILED                                    = "failed";

    /**
     * 端口号绑定的上限和下限数值
     */
    public static final Integer MAX_BIND_PORT_VALUE                          =   65535;
    public static final Integer MIN_BIND_PORT_VALUE                          =   1;

}
