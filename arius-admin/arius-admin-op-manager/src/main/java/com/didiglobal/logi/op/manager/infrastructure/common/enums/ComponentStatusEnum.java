package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 * @date 2022-07-05 10:20 上午
 */
public enum ComponentStatusEnum {

    GREEN(0),
    YELLOW(1),
    RED(2),
    UN_KNOW(-1);

    public int status;

    ComponentStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
