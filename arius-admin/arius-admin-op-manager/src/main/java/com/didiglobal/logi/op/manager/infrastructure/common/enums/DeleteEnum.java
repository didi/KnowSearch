package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 * @date 2022-07-12 3:26 下午
 */
public enum DeleteEnum {

    NORMAL(0),

    uninstall(1);

    public int type;

    DeleteEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
