package com.didi.cloud.fastdump.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 集群/索引状态
 */
public enum HealthEnum {

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

    HealthEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static HealthEnum valueOf(Integer code) {
        if (YELLOW.getCode().equals(code)) {
            return YELLOW;
        } else if (RED.getCode().equals(code)) {
            return RED;
        } else if (GREEN.getCode().equals(code)) {
            return GREEN;
        }
        return UNKNOWN;
    }

    public static HealthEnum valuesOf(String desc) {
        if (StringUtils.isBlank(desc) || UNKNOWN.getDesc().equals(desc)) {
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

    private final Integer code;
    private final String  desc;

    public static boolean isExitByCode(Integer code) {
        if (null == code) {
            return false;
        }

        for (HealthEnum value : HealthEnum.values()) {
            if (code.equals(value.getCode())) {
                return true;
            }
        }

        return false;
    }
}
