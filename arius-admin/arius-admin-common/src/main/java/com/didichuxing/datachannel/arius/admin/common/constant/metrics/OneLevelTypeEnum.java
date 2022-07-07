package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import java.util.List;

import com.google.common.collect.Lists;

public enum OneLevelTypeEnum {
                UNKNOWN(""),
                CLUSTER("cluster"),
                NODE("node"),
                TEMPLATE("template"),
                INDEX("index"),
                CLUSTER_THREAD_POOL_QUEUE("clusterThreadPoolQueue");

    OneLevelTypeEnum(String type) {
        this.type = type;
    }

    private final String type;

    public String getType() {
        return type;
    }

    public static OneLevelTypeEnum valueOfType(String type) {
        if (null == type) {
            return OneLevelTypeEnum.UNKNOWN;
        }

        for (OneLevelTypeEnum typeEnum : OneLevelTypeEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }

        return null;
    }

    public static List<String> listNoClusterOneLevelType(){
        return Lists.newArrayList(NODE.getType(), TEMPLATE.getType(), INDEX.getType());
    }
}