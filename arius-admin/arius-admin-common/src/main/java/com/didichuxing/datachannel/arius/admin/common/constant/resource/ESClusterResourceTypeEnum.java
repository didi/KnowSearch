package com.didichuxing.datachannel.arius.admin.common.constant.resource;

/**
 * 集群所属资源类型枚举
 * @author chengxiang
 */
public enum ESClusterResourceTypeEnum {
                               /**
                                * acs集群
                                */
                               ES_ACS(3, "acs集群"),

                               /**
                                * vmware集群
                                */
                               ES_VMWARE(4, "vmware集群"),

                                /**
                                * tce集群
                                */
                                ES_TCE(5, "tce集群"),

                               /**
                                * 未知
                                */
                               UNKNOWN(-1, "unknown");

    ESClusterResourceTypeEnum(int code, String desc) {
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

    public static ESClusterResourceTypeEnum valueOf(Integer code) {
        if (code == null) {
            return ESClusterResourceTypeEnum.UNKNOWN;
        }

        for (ESClusterResourceTypeEnum typeEnum : ESClusterResourceTypeEnum.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum;
            }
        }
        return ESClusterResourceTypeEnum.UNKNOWN;
    }

    public static boolean validCode(Integer code) {
        if (code == null) {
            return false;
        }
        for (ESClusterResourceTypeEnum typeEnum : ESClusterResourceTypeEnum.values()) {
            if (typeEnum.getCode() == code) {
                return true;
            }
        }
        return false;
    }
}
