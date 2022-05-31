package com.didichuxing.datachannel.arius.admin.remote.protocol.content;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

/**
 * 登录校验方式
 *
 * @author linyunan
 * @date 2021-04-26
 */
@Deprecated
public enum LoginProtocolTypeEnum {
                              /**
                               * 默认校验方式, 与DB校验
                               */
                              DEFAULT(1, "defaultLogin"),

                              LDAP(2, "ldap"),

                              UNKNOWN(-1, "unknown");

    private Integer code;

    private String  type;

    LoginProtocolTypeEnum(Integer code, String type) {
        this.code = code;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Integer getCode() {
        return code;
    }

    public static LoginProtocolTypeEnum valueOfType(String type) {
        if (AriusObjUtils.isNull(type)) {
            return LoginProtocolTypeEnum.UNKNOWN;
        }
        for (LoginProtocolTypeEnum codeEnum : LoginProtocolTypeEnum.values()) {
            if (type.equals(codeEnum.getType())) {
                return codeEnum;
            }
        }

        return LoginProtocolTypeEnum.UNKNOWN;
    }
}