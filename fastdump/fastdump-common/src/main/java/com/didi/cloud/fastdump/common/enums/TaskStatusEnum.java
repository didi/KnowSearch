package com.didi.cloud.fastdump.common.enums;

public enum TaskStatusEnum {
                            PAUSE("pause", 0),

                            SUCCESS("success", 1),

                            RUNNING("running", 2),

                            FAILED("failed", 3),

                            WAIT("waiting", 4),

                            UNKNOWN("unknown", -1);
    TaskStatusEnum(String value, Integer code) {
        this.value = value;
        this.code = code;
    }

    private String  value;

    private Integer code;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static TaskStatusEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (TaskStatusEnum state : TaskStatusEnum.values()) {
            if (UNKNOWN.getCode().equals(code)) {
                continue;
            }

            if (state.getCode().equals(code)) {
                return state;
            }
        }
        return null;
    }

    public static TaskStatusEnum valueOfType(String value) {
        if (value == null) {
            return null;
        }

        for (TaskStatusEnum state : TaskStatusEnum.values()) {
            if (UNKNOWN.getValue().equals(value)) { continue;}

            if (state.getValue().equals(value)) {
                return state;
            }
        }
        return null;
    }
}