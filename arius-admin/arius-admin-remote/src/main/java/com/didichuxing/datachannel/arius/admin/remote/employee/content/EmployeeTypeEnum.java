package com.didichuxing.datachannel.arius.admin.remote.employee.content;

/**
 * @author linyunan
 * @date 2021-05-14
 */
public enum EmployeeTypeEnum {
                              /**
                               * default
                               */
                              DEFAULT(1, "defaultEmployee"),

                              DIDI(2, "didiEmployee"),

                              UNKNOWN(-1, "unknown");

    private Integer code;

    private String  type;

    EmployeeTypeEnum(Integer code, String type) {
        this.code = code;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Integer getCode() {
        return code;
    }

    public static EmployeeTypeEnum valueOfCode(Integer code) {
        if (code == null) {
            return EmployeeTypeEnum.UNKNOWN;
        }
        for (EmployeeTypeEnum codeEnum : EmployeeTypeEnum.values()) {
            if (code.equals(codeEnum.getCode())) {
                return codeEnum;
            }
        }

        return EmployeeTypeEnum.UNKNOWN;
    }
}
