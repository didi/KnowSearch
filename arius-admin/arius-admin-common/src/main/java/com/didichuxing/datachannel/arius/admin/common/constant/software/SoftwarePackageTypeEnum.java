package com.didichuxing.datachannel.arius.admin.common.constant.software;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

public enum SoftwarePackageTypeEnum {
    ES_INSTALL_PACKAGE(1,"ESInstallPackage"),
    GATEWAY_INSTALL_PACKAGE(2,"gatewayInstallPackage"),
    ES_ENGINE_PLUGIN(3,"ESEnginePlugin"),
    GATEWAY_ENGINE_PLUGIN(4,"gatewayEnginePlugin"),
    ES_PLATFROM_PLUGIN(5,"ESPlatformPlugin"),
    GATEWAY_PLATFORM_PLUGIN(6,"gatewayPlatformPlugin"),
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
}
