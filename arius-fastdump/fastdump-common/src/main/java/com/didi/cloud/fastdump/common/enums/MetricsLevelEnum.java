package com.didi.cloud.fastdump.common.enums;

public enum MetricsLevelEnum {
    INFO("info"),
    WARN("warn"),
    ERROR("error");

    private final String level;

    MetricsLevelEnum(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }
}
