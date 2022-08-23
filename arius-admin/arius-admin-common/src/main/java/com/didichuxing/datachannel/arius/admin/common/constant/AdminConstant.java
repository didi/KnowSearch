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

//    public static final String       DEFAULT_TYPE                            = "type";

    public static final String       DEFAULT_INDEX_MAPPING_TYPE              = "_doc";
    public static final String INDEX_NUMBER_OF_SHARDS = "index.number_of_shards";
    public static final String       DEFAULT_DYNAMIC_TEMPLATES_KEY           = "dynamic_templates";
//    public static final String       SINGLE_TYPE_KEY                         = "index.mapping.single_type";
//    public static final String       DEFAULT_SINGLE_TYPE                     = "true";

    public static final String       HOST_NAME                               = HostUtil.getHostName();

    public static final Integer      YES                                     = 1;

    public static final Integer      NO                                      = 0;

    public static final Integer      NOT_EXPIRE                              = -1;

    public static final Double       BYTE_TO_G                               = 1.0 / (1024 * 1024 * 1024);

    public static final Double       BYTE_TO_MB                              = 1.0 / (1024 * 1024);

    public static final Double       TO_W                                    = 1.0 / 10000;

    public static final Double       G_PER_SHARD                             = 50.0;

    public static final String       COMMA                                   = ",";

    /**
     * 默认的插件版本
     */
    public static final String       DEFAULT_PLUGIN_VERSION                  = "0.0.0.1";

    public static final Long         MILLIS_PER_DAY                          = 24 * 60 * 60 * 1000L;

    public static final String       MM_DD_DATE_FORMAT                       = "MMdd";
    public static final String       YY_MM_DD_DATE_FORMAT                    = "_yyyy-MM-dd";
    public static final String       YY_MM_DATE_FORMAT                       = "_yyyy-MM";

    public static final Integer      PIPELINE_RATE_LIMIT_MAX_VALUE           = 1000000;
    public static final Integer      PIPELINE_RATE_LIMIT_MIN_VALUE           = 1000;

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
    public static final String  REGION_NOT_BOUND_LOGIC_CLUSTER_ID = "-1";

    public static final String  JOB_SUCCESS                       = "success";
    public static final String  JOB_FAILED                        = "failed";

    /**
     * 端口号绑定的上限和下限数值
     */
    public static final Integer MAX_BIND_PORT_VALUE               = 65535;
    public static final Integer MIN_BIND_PORT_VALUE               = 1;

}