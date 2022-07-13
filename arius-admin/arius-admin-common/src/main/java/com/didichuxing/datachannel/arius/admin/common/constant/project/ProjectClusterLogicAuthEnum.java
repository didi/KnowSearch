package com.didichuxing.datachannel.arius.admin.common.constant.project;

import java.util.Arrays;
import java.util.Objects;

public enum ProjectClusterLogicAuthEnum {
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

    ProjectClusterLogicAuthEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static ProjectClusterLogicAuthEnum valueOf(Integer code) {
        if (code != null) {
            for (ProjectClusterLogicAuthEnum state : ProjectClusterLogicAuthEnum.values()) {
                if (state.getCode().equals(code)) {
                    return state;
                }
            }
        }

        return ProjectClusterLogicAuthEnum.NO_PERMISSIONS;
    }

    public static ProjectClusterLogicAuthEnum highestAuth(ProjectClusterLogicAuthEnum... authEnums) {
        if (authEnums == null) {
            return NO_PERMISSIONS;
        }

        // ordinal越小权限越大
        return Arrays.stream(authEnums).filter(Objects::nonNull).min(ProjectClusterLogicAuthEnum::compareTo)
            .orElse(NO_PERMISSIONS);

    }

    public boolean higherOrEqual(ProjectClusterLogicAuthEnum anotherEnums) {
        if (anotherEnums == null) {
            return true;
        }

        return this.ordinal() <= anotherEnums.ordinal();
    }

    public boolean higher(ProjectClusterLogicAuthEnum anotherEnums) {
        if (anotherEnums == null) {
            return true;
        }

        return this.ordinal() < anotherEnums.ordinal();
    }

    public static boolean isExitByCode(Integer code) {
        if (code == null) {
            return false;
        }

        for (ProjectClusterLogicAuthEnum state : ProjectClusterLogicAuthEnum.values()) {
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