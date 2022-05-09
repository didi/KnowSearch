package com.didichuxing.datachannel.arius.admin.remote.employee;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

/**
 * @author linyunan
 * @date 2021-05-14
 */
public enum EmployeeTypeEnum {
                              /**
                               * default
                               */
                              DEFAULT(1, "defaultEmployee"),

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

    public static EmployeeTypeEnum valueOfType(String type) {
        if (AriusObjUtils.isNull(type)) {
            return EmployeeTypeEnum.UNKNOWN;
        }
        for (EmployeeTypeEnum codeEnum : EmployeeTypeEnum.values()) {
            if (type.equals(codeEnum.getType())) {
                return codeEnum;
            }
        }

        return EmployeeTypeEnum.UNKNOWN;
    }
}
