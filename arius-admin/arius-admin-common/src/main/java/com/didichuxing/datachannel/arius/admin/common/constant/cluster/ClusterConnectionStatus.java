package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

/**
 * Cluster 连通状态标示
 */
public enum ClusterConnectionStatus {
                                     /**
                                      * 正常
                                      */
                                     NORMAL(0, "正常连通"),
                                     /**
                                      * 未认证
                                      */
                                     UNAUTHORIZED(1, "密码错误无法连通"),
                                     /**
                                      * 未连接
                                      */
                                     DISCONNECTED(2, "集群离线未连接"),
                                     /**
                                      * 未知
                                      */
                                     UNKNOWN(3, "未知");

    ClusterConnectionStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private Integer code;
    private String  desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
