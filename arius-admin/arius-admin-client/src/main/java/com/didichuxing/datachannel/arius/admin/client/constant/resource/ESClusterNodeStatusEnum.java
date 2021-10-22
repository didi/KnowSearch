package com.didichuxing.datachannel.arius.admin.client.constant.resource;

/**
 * 节点角色类型枚举
 *
 * Created by d06679 on 2017/7/14.
 */
public enum ESClusterNodeStatusEnum {
                                     /**在线*/
                                     ONLINE(1, "在线"),

                                     OFFLINE(2, "离线"),

                                     FAULT(3, "故障"),

                                     UNKNOWN(-1, "unknown");

    ESClusterNodeStatusEnum(int code, String desc) {
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

    public static ESClusterNodeStatusEnum valueOf(Integer code) {
        if (code == null) {
            return ESClusterNodeStatusEnum.UNKNOWN;
        }
        for (ESClusterNodeStatusEnum typeEnum : ESClusterNodeStatusEnum.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum;
            }
        }

        return ESClusterNodeStatusEnum.UNKNOWN;
    }

}
