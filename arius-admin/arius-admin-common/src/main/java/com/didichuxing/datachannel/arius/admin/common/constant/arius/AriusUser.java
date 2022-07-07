package com.didichuxing.datachannel.arius.admin.common.constant.arius;

/**
 *
 * Created by d06679 on 2017/7/14.
 */
public enum AriusUser {
    /**
     * Arius服务号
     */
   ARIUS("Arius服务号"),

   AUTO_EXER("zhaoqingrong"),

   SYSTEM("系统"),

   CAPACITY_PLAN("容量规划"),

   UNKNOWN("unknown");

    AriusUser(String desc) {
        this.desc = desc;
    }

    private final String desc;

    public String getDesc() {
        return desc;
    }

}
