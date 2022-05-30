package com.didichuxing.datachannel.arius.admin.common.constant.arius;

/**
 * 用户角色
 * @author zengqiao_cn@163.com
 * @date 19/4/15
 */
@Deprecated
public enum AriusUserRoleEnum {
    /**
     * 未知角色
     */
    UNKNOWN(-1, "unknown", "未知角色"),

    /**
     * 普通用户角色
     */
    NORMAL(0, "normal", "普通用户"),

    /**
     * 平台开发角色
     */
    RD(1, "rd", "开发"),

    /**
     * 平台运维角色
     */
    OP(2, "op", "运维");

    private Integer role;

    private String message;

    private String desc;

    AriusUserRoleEnum(Integer role, String message, String desc ) {
        this.role       = role;
        this.message    = message;
        this.desc       = desc;
    }

    public Integer getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }

    public String getDesc() {return desc;}

    @Override
    public String toString() {
        return "AriusUserRoleEnum{" +
                "role=" + role +
                ", message='" + message + '\'' +
                '}';
    }

    public static AriusUserRoleEnum getUserRoleEnum(Integer role) {
        for (AriusUserRoleEnum elem: AriusUserRoleEnum.values()) {
            if (elem.role.equals(role)) {
                return elem;
            }
        }
        return AriusUserRoleEnum.UNKNOWN;
    }
}