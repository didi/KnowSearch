package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 */
public enum TaskActionEnum {
    START("start"),

    PAUSE("pause"),

    KILL("kill"),

    CANCEL("cancel");

    private String action;

    TaskActionEnum(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
