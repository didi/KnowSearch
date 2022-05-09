package com.didichuxing.datachannel.arius.admin.common.constant.resource;

/**
 * 节点接入规则枚举
 * @author chengxiang
 */
public enum ESClusterImportRuleEnum {
                                   AUTO_IMPORT(0, "自动接入"),

                                   FULL_IMPORT(1, "全量接入"),

                                   UNKNOWN(-1, "unknown");

    ESClusterImportRuleEnum(int code, String desc) {
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

    public static ESClusterImportRuleEnum valueOf(Integer code) {
        if (code == null) {
            return ESClusterImportRuleEnum.UNKNOWN;
        }
        for (ESClusterImportRuleEnum importRuleEnum : ESClusterImportRuleEnum.values()) {
            if (importRuleEnum.getCode() == code) {
                return importRuleEnum;
            }
        }
        return ESClusterImportRuleEnum.UNKNOWN;
    }

    public static boolean validCode(Integer code) {
        if (code == null) {
            return false;
        }
        for(ESClusterImportRuleEnum importRuleEnum : ESClusterImportRuleEnum.values()) {
            if (importRuleEnum.getCode() == code) {
                return true;
            }
        }
        return false;
    }

}
