package com.didichuxing.datachannel.arius.admin.common.constant.workorder;

/**
 * 操作类型
 * @author fengqiongfeng
 * @date 2020/08/24
 */
public enum OperationTypeEnum {
                               CREATE(0,
                                      "创建"), UPDATE(1,
                                                    "更新"), DELETE(2,
                                                                  "删除"), INSTALL(3,
                                                                                 "安装"), UNINSTALL(4,
                                                                                                  "卸载"), UNKNOWN(-1,
                                                                                                                 "UNKNOWN");

    private Integer code;

    private String  message;

    OperationTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static OperationTypeEnum valueOfCode(Integer code) {
        if (null == code) {
            return OperationTypeEnum.UNKNOWN;
        }
        for (OperationTypeEnum typeEnum : OperationTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }

        return OperationTypeEnum.UNKNOWN;
    }
}