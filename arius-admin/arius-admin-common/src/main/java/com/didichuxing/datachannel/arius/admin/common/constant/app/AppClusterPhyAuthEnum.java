package com.didichuxing.datachannel.arius.admin.common.constant.app;

public enum AppClusterPhyAuthEnum {
                                     /**
                                      * 超级权限
                                      */
                                     ALL(0, "manager", "超管"),

                                     /**
                                      * Owner权限
                                      */
                                     OWN(1, "own", "配置管理"),

                                     /**
                                      * 访问权限
                                      */
                                     ACCESS(2, "r", "访问"),

                                     /**
                                      * 没有权限
                                      */
                                     NO_PERMISSIONS(-1, "", "无权限");

    private final Integer code;
    private final String  name;
    private final String  desc;

    AppClusterPhyAuthEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static AppClusterPhyAuthEnum valueOf(Integer code) {
        if (code != null) {
            for (AppClusterPhyAuthEnum state : AppClusterPhyAuthEnum.values()) {
                if (state.getCode().equals(code)) {
                    return state;
                }
            }
        }

        return AppClusterPhyAuthEnum.NO_PERMISSIONS;
    }

    public static boolean isExitByCode(Integer code) {
        if (code == null) {
            return false;
        }

        for (AppClusterPhyAuthEnum state : AppClusterPhyAuthEnum.values()) {
            if (state.getCode().equals(code)) {
                return true;
            }
        }

        return false;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

}
