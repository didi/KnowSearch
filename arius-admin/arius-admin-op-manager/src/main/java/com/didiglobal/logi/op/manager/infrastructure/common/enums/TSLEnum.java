package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 * @date 2022-10-13 3:26 下午
 */
public enum TSLEnum {
    /**
     * 关闭
     */
    CLOSE(0),

    /**
     * 打开
     */
    OPEN(1);

    public int type;

    TSLEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
