package com.didichuxing.datachannel.arius.admin.remote.department.content;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

/**
 * 企业部门枚举
 * 使用只需要加入相应的枚举属性, 对应代码包加入到 com.didichuxing.datachannel.arius.admin.remote.department 路径下
 *
 * @author linyunan
 * @date 2021-04-26
 */
public enum DepartmentTypeEnum {
                                /**
                                 * 本地部门信息, 固定为一个部门即可
                                 */
                                DEFAULT(1, "defaultDep"),

                                UNKNOWN(-1, "unknown");

    private Integer code;

    private String  type;

    DepartmentTypeEnum(Integer code, String type) {
        this.code = code;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Integer getCode() {
        return code;
    }

    public static DepartmentTypeEnum valueOfType(String type) {
        if (AriusObjUtils.isNull(type)) {
            return DepartmentTypeEnum.UNKNOWN;
        }
        for (DepartmentTypeEnum codeEnum : DepartmentTypeEnum.values()) {
            if (type.equals(codeEnum.getType())) {
                return codeEnum;
            }
        }

        return DepartmentTypeEnum.UNKNOWN;
    }
}
