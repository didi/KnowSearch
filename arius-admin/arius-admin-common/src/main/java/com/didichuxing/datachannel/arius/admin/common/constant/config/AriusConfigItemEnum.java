package com.didichuxing.datachannel.arius.admin.common.constant.config;

public enum AriusConfigItemEnum {
                                 /**正常*/
                                 DASHBOARD_THRESHOLD("dashboard.threshold", "dashboard限制阈值");

    AriusConfigItemEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    private String name;

    private String desc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
