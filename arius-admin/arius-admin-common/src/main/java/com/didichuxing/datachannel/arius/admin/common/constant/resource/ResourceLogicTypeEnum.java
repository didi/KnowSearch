package com.didichuxing.datachannel.arius.admin.common.constant.resource;

/**
 * 集群业务分组
 * 无论是共享，独立还是独享，一个逻辑集群都只能对应一个物理集群，只是物理集群中region(平台最小的资源划分单位)的使用逻辑不同做了区分
 * @author d06679
 * @date 2017/7/14
 *
 */
public enum ResourceLogicTypeEnum {
    /**
     * 已绑定的region可以被其他的绑定该物理集群的共享类型的逻辑集群重复绑定
     */
    PUBLIC(1, "共享资源"),

    /**
     * 已绑定的物理集群上的所有的region都不能被其他逻辑集群绑定
     */
    PRIVATE(2, "独立资源"),

    /**
     * 已绑定的region不能被其他的绑定该物理集群的其他逻辑集群重复绑定
     */
    EXCLUSIVE(3, "独享资源"),

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
