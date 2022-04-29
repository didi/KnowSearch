package com.didichuxing.datachannel.arius.admin.common.constant.template;

/**
 * @author d06679
 * @date 2019/3/29
 */
public enum TemplateDeployRoleEnum {
                                    /**主*/
                                    MASTER(1, "主"),

                                    SLAVE(2, "从"),

                                    UNKNOWN(-1, "未知");

    TemplateDeployRoleEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private Integer code;

    private String  desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TemplateDeployRoleEnum valueOf(Integer code) {
        if (code == null) {
            return TemplateDeployRoleEnum.UNKNOWN;
        }
        for (TemplateDeployRoleEnum state : TemplateDeployRoleEnum.values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }

        return TemplateDeployRoleEnum.UNKNOWN;
    }

}
