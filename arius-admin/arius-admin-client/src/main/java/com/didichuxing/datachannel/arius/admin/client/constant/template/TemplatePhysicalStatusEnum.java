package com.didichuxing.datachannel.arius.admin.client.constant.template;

/**
 * @author d06679
 * @date 2019/3/29
 */
public enum TemplatePhysicalStatusEnum {
                                        /**常规*/
                                        NORMAL(1, "常规"),

                                        INDEX_DELETING(-1, "索引删除中"),

                                        DELETED(-2, "删除"),

                                        UNKNOWN(-3, "unknown");

    TemplatePhysicalStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int    code;

    private String desc;

    private String label;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getLabel() {
        return label;
    }

    public static TemplatePhysicalStatusEnum valueOf(Integer code) {
        if (code == null) {
            return TemplatePhysicalStatusEnum.UNKNOWN;
        }
        for (TemplatePhysicalStatusEnum state : TemplatePhysicalStatusEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return TemplatePhysicalStatusEnum.UNKNOWN;
    }

}
