package com.didichuxing.datachannel.arius.admin.common.constant.dcdr;

/**
 * Created by linyunan on 12/14/21
 */
public enum DCDRStatusEnum {
                            CANCELLED("cancelled", 0),

                            SUCCESS("success", 1),

                            RUNNING("running", 2),

                            FAILED("failed", 3),

                            WAIT("wait", 4),

                            UNKNOWN("unknown", -1);

    DCDRStatusEnum(String value, Integer code) {
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

    public static DCDRStatusEnum valueFromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (DCDRStatusEnum state : DCDRStatusEnum.values()) {
            if (UNKNOWN.getCode().equals(code)) {
                continue;
            }

            if (state.getCode().equals(code)) {
                return state;
            }
        }
        return null;
    }
}