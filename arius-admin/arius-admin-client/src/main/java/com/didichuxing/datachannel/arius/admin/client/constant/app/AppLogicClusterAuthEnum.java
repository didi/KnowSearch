package com.didichuxing.datachannel.arius.admin.client.constant.app;

import java.util.Arrays;
import java.util.Objects;

/**
 * APP 逻辑集群权限点
 * @author wangshu
 * @date 2020/09/17
 */
public enum AppLogicClusterAuthEnum {
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

    AppLogicClusterAuthEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static AppLogicClusterAuthEnum valueOf(Integer code) {
        if (code != null) {
            for (AppLogicClusterAuthEnum state : AppLogicClusterAuthEnum.values()) {
                if (state.getCode().equals(code)) {
                    return state;
                }
            }
        }

        return AppLogicClusterAuthEnum.NO_PERMISSIONS;
    }

    public static AppLogicClusterAuthEnum highestAuth(AppLogicClusterAuthEnum... authEnums) {
        if (authEnums == null) {
            return NO_PERMISSIONS;
        }

        // ordinal越小权限越大
        return Arrays.stream(authEnums).filter(Objects::nonNull).min(AppLogicClusterAuthEnum::compareTo)
            .orElse(NO_PERMISSIONS);

    }

    public boolean higherOrEqual(AppLogicClusterAuthEnum anotherEnums) {
        if (anotherEnums == null) {
            return true;
        }

        return this.ordinal() <= anotherEnums.ordinal();
    }

    public boolean higher(AppLogicClusterAuthEnum anotherEnums) {
        if (anotherEnums == null) {
            return true;
        }

        return this.ordinal() < anotherEnums.ordinal();
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
