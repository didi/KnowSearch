package com.didichuxing.datachannel.arius.admin.common.constant.task;

import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;

public enum OpTaskStatusEnum {
                              /**执行成功*/
                              SUCCESS("success", "执行成功"),

                              FAILED("failed", "执行失败"),

                              RUNNING("running", "执行中"),

                              WAITING("waiting", "等待"),

                              PAUSE("pause", "暂停"),

                              CANCEL("cancel", "取消"),
                              TIMEOUT("timeout", "超时"),
                              IGNORED("Ignored", "超时"),
                              KILLED("Killed", "杀死"),

                              UNKNOWN("unknown", "unknown");

    OpTaskStatusEnum(String status, String value) {
        this.status = status;
        this.value = value;
    }

    private String status;

    private String value;

    public String getStatus() {
        return status;
    }

    public String getValue() {
        return value;
    }

    public static OpTaskStatusEnum valueOfStatus(String status) {
        if (status == null) {
            return OpTaskStatusEnum.UNKNOWN;
        }
        for (OpTaskStatusEnum statusEnum : OpTaskStatusEnum.values()) {
            if (status.equals(statusEnum.getStatus())) {
                return statusEnum;
            }
        }

        return OpTaskStatusEnum.UNKNOWN;
    }
    /**
     * > 它返回传递给它的 TaskStatusEnum 值的 OpTaskStatusEnum 值
     *
     * @param taskStatusEnum 任务管理器中的任务状态。
     * @return 正在返回 OpTaskStatusEnum。
     */
    public static OpTaskStatusEnum valueOfStatusByOpManagerEnum(TaskStatusEnum taskStatusEnum) {
        switch (taskStatusEnum) {
            case PAUSE:
                return PAUSE;
            case FAILED:
                return FAILED;
            case KILLED:
                return KILLED;
            case IGNORED:
                return IGNORED;
            case RUNNING:
                return RUNNING;
            case SUCCESS:
                return SUCCESS;
            case TIMEOUT:
                return TIMEOUT;
            case WAITING:
                return WAITING;
            case CANCELLED:
                return CANCEL;
            default:
                return UNKNOWN;
        }
    }
}