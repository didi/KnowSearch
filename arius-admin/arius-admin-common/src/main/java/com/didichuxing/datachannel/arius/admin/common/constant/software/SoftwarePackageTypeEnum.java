package com.didichuxing.datachannel.arius.admin.common.constant.software;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

public enum SoftwarePackageTypeEnum {
    ES_INSTALL_PACKAGE(1,"es-install-package"),
    GATEWAY_INSTALL_PACKAGE(2,"gateway-install-package"),
    ES_ENGINE_PLUGIN(3,"es-engine-plugin"),
    GATEWAY_ENGINE_PLUGIN(4,"gateway-engine-plugin"),
    ES_PLATFROM_PLUGIN(5,"es-platform-plugin"),
    GATEWAY_PLATFORM_PLUGIN(6,"gateway-platform-plugin"),
    UNKNOWN(99,"unknown");
    private Integer packageType;
    private String packTypeDesc;

    SoftwarePackageTypeEnum(Integer packageType, String packTypeDesc) {
        this.packageType = packageType;
        this.packTypeDesc = packTypeDesc;
    }

    public Integer getPackageType() {
        return packageType;
    }

    public String getPackTypeDesc() {
        return packTypeDesc;
    }

    public static Integer getPackageTypeByDesc(String packTypeDesc){
        if (AriusObjUtils.isNull(packTypeDesc)) {
            return SoftwarePackageTypeEnum.UNKNOWN.getPackageType();
        }
        for (SoftwarePackageTypeEnum softwarePackageTypeEnum : SoftwarePackageTypeEnum.values()) {
            if (packTypeDesc.equals(softwarePackageTypeEnum.getPackTypeDesc())) {
                return softwarePackageTypeEnum.getPackageType();
            }
        }
        return SoftwarePackageTypeEnum.UNKNOWN.getPackageType();
    }

    public static String getPackageTypeDesc(Integer packageType) {
        if (AriusObjUtils.isNull(packageType)) {
            return SoftwarePackageTypeEnum.UNKNOWN.getPackTypeDesc();
        }
        for (SoftwarePackageTypeEnum softwarePackageTypeEnum : SoftwarePackageTypeEnum.values()) {
            if (softwarePackageTypeEnum.getPackageType().compareTo(packageType) == 0) {
                return softwarePackageTypeEnum.getPackTypeDesc();
            }
        }
        return SoftwarePackageTypeEnum.UNKNOWN.getPackTypeDesc();
    }
}
