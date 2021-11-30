package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

/**
 * @author wangpengkai
 */

public enum MetricsTypeEnum {

    /**
     * code表示编号
     * firstMetricsType表示一级目录下指标配置信息，如集群看板和网关看板
     * secondMetricsType表示二级目录下指标配置信息，如集群看板下的总览指标类型
     */
    /**
     * 未知的分类
     */
    UNKNOWN(-1, "unknown", "unknown"),
    /**
     * 集群看板下的总览指标
     */
    CLUSTER_OVERVIEW(11, MetricsConstant.CLUSTER,"overview"),
    /**
     * 集群看板下的节点指标
     */
    CLUSTER_NODE(12, MetricsConstant.CLUSTER,"node"),
    /**
     * 集群看板下的索引指标
     */
    CLUSTER_INDEX(13, MetricsConstant.CLUSTER,"index"),
    /**
     * 网关看板下的总览指标
     */
    GATEWAY_OVERVIEW(21, MetricsConstant.GATEWAY,"overview"),
    /**
     * 网关看板下的节点指标
     */
    GATEWAY_NODE(22, MetricsConstant.GATEWAY,"node"),
    /**
     * 网关看板下的索引指标
     */
    GATEWAY_INDEX (23, MetricsConstant.GATEWAY,"index"),
    /**
     * 网关看板下的项目指标
     */
    GATEWAY_APP (24, MetricsConstant.GATEWAY,"app"),
    /**
     * 网关看板下的DSL指标
     */
    GATEWAY_DSL (25, MetricsConstant.GATEWAY,"dsl"),
    /**
     * 字段页展示中的DSL模板配置
     */
    USER_CONFIG_SHOW_DSL_TEMPLATE(31,MetricsConstant.USER_SHOW,"dslTemplate"),
    /**
     * 字段页展示中的索引查询配置
     */
    USER_CONFIG_SHOW_INDEX_SEARCH(32,MetricsConstant.USER_SHOW,"indexSearch");


    MetricsTypeEnum(int code, String firstMetricsType, String secondMetricsType) {
        this.code = code;
        this.firstMetricsType = firstMetricsType;
        this.secondMetricsType = secondMetricsType;
    }

    private int code;
    private String firstMetricsType;
    private String secondMetricsType;

    public int getCode() {
        return code;
    }

    public String getFirstMetricsType() {
        return firstMetricsType;
    }

    public String getSecondMetricsType() {
        return secondMetricsType;
    }

    public static MetricsTypeEnum valueOfCode(Integer code) {
        if(null == code) {
            return MetricsTypeEnum.UNKNOWN;
        }
        for(MetricsTypeEnum typeEnum:MetricsTypeEnum.values()) {
            if(code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return MetricsTypeEnum.UNKNOWN;
    }
}
