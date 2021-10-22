package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * @author zengqiao
 * @date 20/10/26
 */
public class ESClusterConstant {

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
    public static final String  DEFAULT_CLUSTER_TEMPLATE_SRVS   = "1";

    /**
     * 集群默认机房
     */
    public static final String  DEFAULT_CLUSTER_IDC             = "ys02";

    /**
     * 集群默认实例数
     */
    public static final Integer DEFAULT_CLUSTER_PAID_COUNT      = 1;

    /**
     * 集群默认规格
     */
    public static final String  DEFAULT_CLUSTER_NODE_SPEC       = "";

    /**
     * 集群master角色节点最小数量
     */
    public static final Integer MASTER_NODE_MIN_NUMBER          = 3;
}