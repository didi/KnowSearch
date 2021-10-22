package com.didi.arius.gateway.common.enums;

public enum ClusterType {

    INDEX(0),
    SOURCE(1);

    int type;

    private ClusterType(int type) {
        this.type = type;
    }

    public static ClusterType IntegerToType(int code) {
        for (ClusterType type : ClusterType.values()) {
            if (type.type == code) {
                return type;
            }
        }
        return INDEX;
    }
}
