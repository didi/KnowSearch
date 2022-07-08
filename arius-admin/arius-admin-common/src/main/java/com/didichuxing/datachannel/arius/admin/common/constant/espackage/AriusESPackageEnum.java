package com.didichuxing.datachannel.arius.admin.common.constant.espackage;

public enum AriusESPackageEnum {
                                /**
                                 * 滴滴内部的es程序包，对于es内核进行了一定的改造,版本为4位
                                 */
                                DI_DI_INTERNAL(1, 4, "滴滴内部"),
                                /**
                                 * 外部开源的es程序包，版本为3位
                                 */
                                OPEN_SOURCE(2, 3, "外部通用"),
                                /**
                                 * 未知
                                 */
                                UNKNOWN(-1, 0, "未知");

    AriusESPackageEnum(Integer code, Integer versionLength, String desc) {
        this.code = code;
        this.versionLength = versionLength;
        this.desc = desc;
    }

    private int     code;
    private int     versionLength;
    private String  desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public int getVersionLength() { return versionLength; }

    public static AriusESPackageEnum valueOfCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }

        for (AriusESPackageEnum param : AriusESPackageEnum.values()) {
            if (param.getCode() == code) {
                return param;
            }
        }

        return UNKNOWN;
    }

    public static AriusESPackageEnum valueOfLength(Integer versionLength) {
        if(versionLength == null) {
            return UNKNOWN;
        }

        for(AriusESPackageEnum param : AriusESPackageEnum.values()) {
            if(param.getVersionLength() == versionLength) {
                return param;
            }
        }

        return UNKNOWN;
    }
}
