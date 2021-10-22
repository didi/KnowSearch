package com.didichuxing.datachannel.arius.admin.client.constant.task;

public enum WorkTaskStatusEnum {
                                /**执行成功*/
                                SUCCESS("success", "执行成功"),

                                FAILED("failed", "执行失败"),

                                RUNNING("running", "执行中"),

                                WAITING("waiting", "等待"),

                                PAUSE("pause", "暂停"),

                                CANCEL("cancel", "取消"),

                                UNKNOWN("unknown", "unknown");

    WorkTaskStatusEnum(String status, String value) {
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

    public static WorkTaskStatusEnum valueOfStatus(String status) {
        if (status == null) {
            return WorkTaskStatusEnum.UNKNOWN;
        }
        for (WorkTaskStatusEnum statusEnum : WorkTaskStatusEnum.values()) {
            if (status.equals(statusEnum.getStatus())) {
                return statusEnum;
            }
        }

        return WorkTaskStatusEnum.UNKNOWN;
    }

}
