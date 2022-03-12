package com.didichuxing.datachannel.arius.admin.client.constant.resource;

/**
 * 集群业务分组
 *
 * @author d06679
 * @date 2017/7/14
 *
 */
public enum ResourceLogicTypeEnum {
                                   /**共享资源*/
                                   PUBLIC(1, "共享资源"),

                                   PRIVATE(2, "独立资源"),

                                   EXCLUSIVE(3, "独占资源"),

                                   UNKNOWN(-1, "未知");

    ResourceLogicTypeEnum(int code, String desc) {
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

    public static ResourceLogicTypeEnum valueOf(Integer code) {
        if (code == null) {
            return ResourceLogicTypeEnum.UNKNOWN;
        }
        for (ResourceLogicTypeEnum state : ResourceLogicTypeEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return ResourceLogicTypeEnum.UNKNOWN;
    }

    public static boolean isExist(Integer code) {
        return UNKNOWN.getCode() != ResourceLogicTypeEnum.valueOf(code).getCode();
    }
}
