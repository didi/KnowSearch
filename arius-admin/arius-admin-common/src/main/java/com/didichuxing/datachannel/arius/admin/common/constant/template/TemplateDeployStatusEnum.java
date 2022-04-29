package com.didichuxing.datachannel.arius.admin.common.constant.template;

/**
 * @author d06679
 * @date 2019/3/29
 */
public enum TemplateDeployStatusEnum {
                                      /**主从都在线*/
                                      MASTER_SLAVE_ONLINE(1, "主从都在线"),

                                      MASTER_SLAVE_OFFLINE(2, "主从都不在线"),

                                      ONLY_MASTER_ONLINE(3, "只有主在线"),

                                      NO_MASTER_ONLINE(4, "主不在线从在线"),

                                      UNKNOWN(-2, "被删除");

    TemplateDeployStatusEnum(int code, String desc) {
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

    public static TemplateDeployStatusEnum valueOf(Integer code) {
        if (code == null) {
            return TemplateDeployStatusEnum.UNKNOWN;
        }
        for (TemplateDeployStatusEnum state : TemplateDeployStatusEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return TemplateDeployStatusEnum.UNKNOWN;
    }

}
