package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

public class MetricsConstant {
    private MetricsConstant() {
    }

    public static final String OVERVIEW       = "overview";
    public static final String INDEX          = "index";
    public static final String NODE           = "node";
    public static final String CLIENT_NODE    = "clientNode";
    public static final String APP            = "app";
    public static final String DSL            = "dsl";
    public static final String TOTAL_COST_AVG = "total_cost_avg";
    public static final String DOC_COUNT      = "doc_count";
    public static final String CLUSTER        = "cluster";
    public static final String TEMPLATE       = "template";
    public static final String GATEWAY        = "gateway";
    public static final String USER_SHOW      = "user_show";
    public static final String DASHBOARD      = "dashboard";

    /**
     * 作用于dashBoard list类型的指标项，判断是否异常 ，如：Dead节点列表 true是，false为否
     */
    public static final String FAULT_FLAG     = "true";
}
