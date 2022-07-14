package com.didichuxing.datachannel.arius.admin.common.constant.resource;

/**
 * 集群创建来源枚举 接入 or 新建集群
 * @author chengxiang
 */
public enum ESClusterCreateSourceEnum {
                                       /**
                                        *
                                        */
                                       ES_IMPORT(0, "接入集群"),

                                       /**
                                        * vmware集群
                                        */
                                       ES_NEW(1, "新建集群"),

                                       /**
                                        * 未知
                                        */
                                       UNKNOWN(-1, "unknown");

    ESClusterCreateSourceEnum(int code, String desc) {
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

    public static ESClusterCreateSourceEnum valueOf(Integer code) {
        if (code == null) {
            return ESClusterCreateSourceEnum.UNKNOWN;
        }

        for (ESClusterCreateSourceEnum typeEnum : ESClusterCreateSourceEnum.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum;
            }
        }
        return ESClusterCreateSourceEnum.UNKNOWN;
    }

    public static boolean validCode(Integer code) {
        if (code == null) {
            return false;
        }
        for (ESClusterCreateSourceEnum typeEnum : ESClusterCreateSourceEnum.values()) {
            if (typeEnum.getCode() == code) {
                return true;
            }
        }
        return false;
    }
}
