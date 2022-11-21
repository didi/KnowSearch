package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

/**
 * @author wangshu
 * @date 2020/09/22
 */
public enum GatewayHealthEnum {

                               /**
                                * green
                                */
                               GREEN(0, "green"),

                               /**
                                * yellow
                                */
                               YELLOW(1, "yellow"),

                               /**
                                * red
                                */
                               RED(2, "red"),

                               /**
                                * 未知
                                */
                               UNKNOWN(-1, "unknown");

    GatewayHealthEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static GatewayHealthEnum valueOf(Integer code) {
        if (YELLOW.getCode().equals(code)) {
            return YELLOW;
        } else if (RED.getCode().equals(code)) {
            return RED;
        } else if (GREEN.getCode().equals(code)) {
            return GREEN;
        }
        return UNKNOWN;
    }

    public static GatewayHealthEnum valuesOf(String desc) {
        if (AriusObjUtils.isBlack(desc) || UNKNOWN.getDesc().equals(desc)) {
            return UNKNOWN;
        }

        if (YELLOW.getDesc().equals(desc)) {
            return YELLOW;
        } else if (RED.getDesc().equals(desc)) {
            return RED;
        } else if (GREEN.getDesc().equals(desc)) {
            return GREEN;
        }
        return UNKNOWN;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    private Integer code;
    private String  desc;

    public static boolean isExitByCode(Integer code) {
        if (null == code) {
            return false;
        }

        for (GatewayHealthEnum value : GatewayHealthEnum.values()) {
            if (code.equals(value.getCode())) {
                return true;
            }
        }

        return false;
    }
}