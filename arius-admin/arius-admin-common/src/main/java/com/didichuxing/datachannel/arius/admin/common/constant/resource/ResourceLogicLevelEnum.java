package com.didichuxing.datachannel.arius.admin.common.constant.resource;

/**
 * 集群业务分组
 * Created by d06679 on 2017/7/14.
 */
public enum ResourceLogicLevelEnum {
                                    /**normal*/
                                    NORMAL(1, "normal"),

                                    IMPORTANT(2, "important"),

                                    VIP(3, "vip");

    ResourceLogicLevelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int    code;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ResourceLogicLevelEnum valueOf(Integer code) {
        if (code == null) {
            return ResourceLogicLevelEnum.NORMAL;
        }
        for (ResourceLogicLevelEnum state : ResourceLogicLevelEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return ResourceLogicLevelEnum.NORMAL;
    }

    public static ResourceLogicLevelEnum valueFromDesc(String desc) {
        if (desc == null) {
            return ResourceLogicLevelEnum.NORMAL;
        }
        for (ResourceLogicLevelEnum state : ResourceLogicLevelEnum.values()) {
            if (state.getDesc().equals(desc)) {
                return state;
            }
        }

        return ResourceLogicLevelEnum.NORMAL;
    }

}
