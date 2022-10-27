package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 * @date 2022-07-19 2:31 下午
 */
public enum HostStatusEnum {
    ON_LINE(0),
    OFF_LINE(1),
    UN_KNOW(-1);

    private int status;

    HostStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static HostStatusEnum find(int status) {
        for (HostStatusEnum value : HostStatusEnum.values()) {
            if (value.getStatus() == status) {
                return value;
            }
        }
        return UN_KNOW;
    }
}
