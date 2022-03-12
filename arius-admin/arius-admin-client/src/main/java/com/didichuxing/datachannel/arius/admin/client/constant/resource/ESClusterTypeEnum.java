package com.didichuxing.datachannel.arius.admin.client.constant.resource;

/**
 * 集群类型枚举
 * @author  d06679
 * @date 2017/7/14.
 */
public enum ESClusterTypeEnum {
                               /**
                                * 弹性云集群
                                */
                               ES_DOCKER(3, "docker集群"),

                               /**
                                * 物理机集群
                                */
                               ES_HOST(4, "host集群"),

                               /**
                                * 未知
                                */
                               UNKNOWN(-1, "unknown");

    ESClusterTypeEnum(int code, String desc) {
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

    public static ESClusterTypeEnum valueOf(Integer code) {
        if (code == null) {
            return ESClusterTypeEnum.UNKNOWN;
        }

        for (ESClusterTypeEnum typeEnum : ESClusterTypeEnum.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum;
            }
        }
        return ESClusterTypeEnum.UNKNOWN;
    }
}
