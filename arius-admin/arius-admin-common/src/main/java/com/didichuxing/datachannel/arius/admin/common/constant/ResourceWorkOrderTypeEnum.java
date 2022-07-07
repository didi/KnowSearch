package com.didichuxing.datachannel.arius.admin.common.constant;

@Deprecated
public enum ResourceWorkOrderTypeEnum {
    CREATE(1,   "新建"),
    INCREASE(2, "扩缩容"),
    UPDATE(3,   "升级"),
    OFFLINE(4,  "下线"),
    UNKNOWN(-1, "未知");

    ResourceWorkOrderTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;

    private final String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}