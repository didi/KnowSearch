package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 * @date 2022-08-10 10:05 上午
 */
public enum HostActionEnum {
    IGNORE("ignore", TaskStatusEnum.IGNORED.getStatus()),

    REDO("redo", TaskStatusEnum.RUNNING.getStatus()),

    KILL("kill", TaskStatusEnum.KILLED.getStatus()),

    UN_KNOW("un_know", TaskStatusEnum.UN_KNOW.getStatus());

    private String action;

    private Integer status;

    HostActionEnum(String action, Integer status) {
        this.action = action;
        this.status = status;
    }

    public String getAction() {
        return action;
    }

    public Integer getStatus() {
        return status;
    }

    public static HostActionEnum find(String action) {
        for (HostActionEnum value : HostActionEnum.values()) {
            if (value.getAction().equals(action)) {
                return value;
            }
        }
        return UN_KNOW;
    }
}
