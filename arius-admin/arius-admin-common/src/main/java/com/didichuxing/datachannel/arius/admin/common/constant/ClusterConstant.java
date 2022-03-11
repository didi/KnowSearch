package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * @author zengqiao
 * @date 20/10/26
 */
public class ClusterConstant {

    private ClusterConstant() {
    }

    /**
     * 初始化id
     */
    public static final Long    INVALID_VALUE                   = -1L;

    /**
     * 集群默认HttpAddress
     */
    public static final String  DEFAULT_HTTP_ADDRESS            = "127.0.0.1:9200";

    /**
     * 集群完成状态
     */
    public static final String  CLOUD_DONE_STATUS               = "done";

    /**
     * 集群失败状态
     */
    public static final String  CLOUD_FAILED_STATUS             = "failed";

    /**
     * odin集群状态同步最大重试次数
     */
    public static final Integer SYN_ODIN_STATUS_MAX_RETRY_TIMES = 1 << 7;

    /**
     * 集群默认服务
     */
    public static final String  DEFAULT_CLUSTER_TEMPLATE_SRVS   = "";

    /**
     * 集群默认机房
     */
    public static final String  DEFAULT_CLUSTER_IDC             = "ys02";

    public static final Integer DEFAULT_CLUSTER_HEALTH          = 3;

    /**
     * 集群默认实例数
     */
    public static final Integer DEFAULT_CLUSTER_PAID_COUNT      = 1;

    /**
     * 集群默认规格
     */
    public static final String  DEFAULT_CLUSTER_NODE_SPEC       = "";

    /**
     * 接入集群master角色节点最小数量
     */
    public static final Integer JOIN_MASTER_NODE_MIN_NUMBER     = 1;

    /**
     * 创建集群master角色节点最小数量
     */
    public static final Integer CREATE_MASTER_NODE_MIN_NUMBER   = 1;

    /**
     * 物理集群标识
     */
    public static final Integer PHY_CLUSTER                     = 1;

    /**
     * 逻辑集群标识
     */
    public static final Integer LOGIC_CLUSTER                   = 0;

    /**
     * 所有集群Str
     */
    public static final String  ALL_CLUSTER                     = "allCluster";

    /**
     * client运行模式（0：读写共享 1：读写分离）
     */
    public static final Integer DEFAULT_RUN_MODEL               = 0;

    /**
     * 默认可用端口号
     */
    public static final String  DEFAULT_PORT                    = "8060";

    /**
     * 间隔时间, 一小时内
     */
    public static final long    DEFAULT_TIME_INTERVAL           = 24 * 60 * 60 * 1000L;

    public static final long    MAX_TIME_INTERVAL               = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 集群读写地址连接重试次数和重试间隔睡眠时间
     */
    public static final int     DEFAULT_RETRY_TIME_INTERVAL     = 5000;

    public static Integer defaultRetryTime(int retryTimes) {
        return DEFAULT_RETRY_TIME_INTERVAL;
    }

    public static final int DEFAULT_RETRY_TIMES = 24;
}