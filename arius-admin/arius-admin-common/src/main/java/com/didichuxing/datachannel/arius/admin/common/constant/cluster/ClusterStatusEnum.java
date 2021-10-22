package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

/**
 * @author wangshu
 * @date 2020/09/22
 */
public enum ClusterStatusEnum {

    /**
     * green
     */
    GREEN(1, "green"),

    /**
     * yellow
     */
    YELLOW(2, "yellow"),

    /**
     * red
     */
    RED(3, "red"),

    /**
     * 未知
     */
    UNKNOWN(-1,"unknown");

    ClusterStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ClusterStatusEnum valueOf(Integer code) {
        if (YELLOW.getCode().equals(code)) {
            return YELLOW;
        } else if (RED.getCode().equals(code)) {
            return RED;
        }

        return GREEN;
    }

    public static ClusterStatusEnum valuesOf(String desc){
        if (YELLOW.getDesc().equals(desc)) {
            return YELLOW;
        } else if (RED.getDesc().equals(desc)) {
            return RED;
        }

        return GREEN;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    private Integer code;
    private String desc;
}
