package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

/**
 * 集群与模板连接状态
 *
 * @author shizeying
 * @date 2022/08/12
 */
public enum ClusterConnectionStatusWithTemplateEnum {
    /**
     * 正常
     */
    NORMAL(0, "正常连通"),
    /**
     * 断开连接
     */
    DISCONNECTED(1, "集群故障，请检查集群状态后重试");
    
    ClusterConnectionStatusWithTemplateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    private final Integer code;
    private final String  desc;
    
    public Integer getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
}