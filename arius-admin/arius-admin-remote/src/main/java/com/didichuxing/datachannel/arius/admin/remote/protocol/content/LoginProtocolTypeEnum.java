package com.didichuxing.datachannel.arius.admin.remote.protocol.content;

/**
 * 登录校验方式
 *
 * @author linyunan
 * @date 2021-04-26
 */
public enum LoginProtocolTypeEnum {
                              /**
                               * 默认校验方式, 与DB校验
                               */
                              DEFAULT(1, "defaultLoginProtocol"),

                              LDAP(2, "ldapLoginProtocol"),

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

    public static LoginProtocolTypeEnum valueOfCode(Integer code) {
        if (code == null) {
            return LoginProtocolTypeEnum.UNKNOWN;
        }
        for (LoginProtocolTypeEnum codeEnum : LoginProtocolTypeEnum.values()) {
            if (code.equals(codeEnum.getCode())) {
                return codeEnum;
            }
        }

        return LoginProtocolTypeEnum.UNKNOWN;
    }
}
