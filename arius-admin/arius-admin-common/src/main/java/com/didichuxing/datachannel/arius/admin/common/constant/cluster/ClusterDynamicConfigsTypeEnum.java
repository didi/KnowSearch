package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ClusterDynamicConfigsTypeEnum {

    /**
     * 未知项
     */
    UNKNOWN(-1,"unknown"),

    /**
     * 物理集群下的breaker配置项
     */
    BREAKER(1,"breaker"),

    /**
     * 物理集群下的routing配置项
     */
    ROUTING(2,"routing"),

    /**
     * 物理集群下的zen配置项
     */
    ZEN(3,"zen");

    ClusterDynamicConfigsTypeEnum(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public static List<ClusterDynamicConfigsTypeEnum> valuesWithoutUnknown() {
        return Arrays.stream(values()).filter(clusterDynamicConfigsTypeEnum ->
                clusterDynamicConfigsTypeEnum != ClusterDynamicConfigsTypeEnum.UNKNOWN).collect(Collectors.toList());
    }

    private final int code;
    private final String type;

    public String getType() {
        return type;
    }

    public int getCode() {
        return code;
    }
}
