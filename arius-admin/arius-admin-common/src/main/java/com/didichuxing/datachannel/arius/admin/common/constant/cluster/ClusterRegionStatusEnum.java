package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

/**
 * Region是否在容量规划标示
 * @author wangshu
 * @date 2020/09/14
 */
@Deprecated
public enum ClusterRegionStatusEnum {
    /**
     * Region未加入容量规划
     */
    NOT_IN_CAPACITY_PLAN(0, "未加入容量规划"),

    /**
     * Region已经在容量规划中
     */
    IN_CAPACITY_PLAN(1, "容量规划中");

    ClusterRegionStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    private Integer code;
    private String message;
}