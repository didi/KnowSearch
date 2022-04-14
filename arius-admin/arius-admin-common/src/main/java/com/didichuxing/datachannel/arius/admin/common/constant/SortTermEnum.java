package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * Created by linyunan on 2/16/22
 *
 * 支持排序的字段枚举
 */
public enum SortTermEnum {
    /*********************************物理集群相关*****************************************/
    DISK_FREE_PERCENT("disk_usage_percent"),

    /*********************************模板相关*****************************************/
    CHECK_POINT_DIFF("check_point_diff"),
    HAS_DCDR("has_dcdr"),
    LEVEL("level"),
    ACTIVE_SHARD_NUM("active_shard_num");

    SortTermEnum(String type) { this.type = type;}

    private String type;

    public String getType() { return type;}

    public static Boolean isExit(String type) {
        for (SortTermEnum sortTermEnum : SortTermEnum.values()) {
            if (sortTermEnum.getType().equals(type)) { return true; }
        }

        return false;
    }
}
