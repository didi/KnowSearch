package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * Created by linyunan on 2/16/22
 */

public enum SortEnum {
    /*********************************物理集群相关*****************************************/
    DISK_FREE_PERCENT("disk_usage_percent"),

    /*********************************模板相关*****************************************/
    CHECK_POINT_DIFF("check_point_diff"),
    HAS_DCDR("has_dcdr"),
    LEVEL("level"),
    ACTIVE_SHARD_NUM("active_shard_num");

    SortEnum(String type) { this.type = type;}

    private String type;

    public String getType() { return type;}

    public static Boolean isExit(String type) {
        for (SortEnum sortEnum : SortEnum.values()) {
            if (sortEnum.getType().equals(type)) { return true; }
        }

        return false;
    }
}
