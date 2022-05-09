package com.didichuxing.datachannel.arius.admin.common.constant.app;

import java.util.Arrays;
import java.util.Objects;

public enum AppClusterLogicAuthEnum {
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

    AppClusterLogicAuthEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static AppClusterLogicAuthEnum valueOf(Integer code) {
        if (code != null) {
            for (AppClusterLogicAuthEnum state : AppClusterLogicAuthEnum.values()) {
                if (state.getCode().equals(code)) {
                    return state;
                }
            }
        }

        return AppClusterLogicAuthEnum.NO_PERMISSIONS;
    }

    public static AppClusterLogicAuthEnum highestAuth(AppClusterLogicAuthEnum... authEnums) {
        if (authEnums == null) {
            return NO_PERMISSIONS;
        }

        // ordinal越小权限越大
        return Arrays.stream(authEnums).filter(Objects::nonNull).min(AppClusterLogicAuthEnum::compareTo)
            .orElse(NO_PERMISSIONS);

    }

    public boolean higherOrEqual(AppClusterLogicAuthEnum anotherEnums) {
        if (anotherEnums == null) {
            return true;
        }

        return this.ordinal() <= anotherEnums.ordinal();
    }

    public boolean higher(AppClusterLogicAuthEnum anotherEnums) {
        if (anotherEnums == null) {
            return true;
        }

        return this.ordinal() < anotherEnums.ordinal();
    }

    public static boolean isExitByCode(Integer code) {
        if (code == null) {
            return false;
        }

        for (AppClusterLogicAuthEnum state : AppClusterLogicAuthEnum.values()) {
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
