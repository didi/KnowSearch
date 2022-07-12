package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 * @date 2022-07-12 5:00 下午
 */
public enum OperationEnum {
    INSTALL(0);

    public int type;

    OperationEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
