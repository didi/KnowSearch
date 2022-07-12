package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 */
public enum PackageTypeEnum {
    CONFIG_DEPENDENT(0),

    CONFIG_INDEPENDENT(1);

    public int type;

    PackageTypeEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
