package com.didichuxing.datachannel.arius.admin.common.constant.dcdr;

/**
 * Created by linyunan on 12/14/21
 */
public enum DCDRSwithTypeEnum {
                               SMOOTH("smooth", 1),

                               FORCE("force", 2),

                               UNKNOWN("unknown", -1);

    DCDRSwithTypeEnum(String value, Integer code) {
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

    public static DCDRSwithTypeEnum valueFromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DCDRSwithTypeEnum state : DCDRSwithTypeEnum.values()) {
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
