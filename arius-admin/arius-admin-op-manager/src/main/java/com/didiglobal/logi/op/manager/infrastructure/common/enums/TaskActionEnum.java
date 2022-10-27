package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 */
public enum TaskActionEnum {
    START("start", TaskStatusEnum.RUNNING.getStatus()),

    PAUSE("pause", TaskStatusEnum.PAUSE.getStatus()),

    KILL("kill", TaskStatusEnum.KILLED.getStatus()),

    CANCEL("cancel", TaskStatusEnum.CANCELLED.getStatus()),

    UN_KNOW("un_know", TaskStatusEnum.UN_KNOW.getStatus());

    private String action;

    private Integer status;

    TaskActionEnum(String action, Integer status) {
        this.action = action;
        this.status = status;
    }

    public String getAction() {
        return action;
    }

    public Integer getStatus() {
        return status;
    }

    public static TaskActionEnum find(String action) {
        for (TaskActionEnum value : TaskActionEnum.values()) {
            if (value.getAction().equals(action)) {
                return value;
            }
        }
        return UN_KNOW;
    }
}
