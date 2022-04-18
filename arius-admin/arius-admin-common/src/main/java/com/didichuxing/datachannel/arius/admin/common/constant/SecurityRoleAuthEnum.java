package com.didichuxing.datachannel.arius.admin.common.constant;

import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum;
import com.google.common.collect.Sets;

/**
 * @author didi
 */
public enum SecurityRoleAuthEnum {

                                  /**
                                   * 管理权限
                                   */
                                  OWN(AppTemplateAuthEnum.OWN, "own_rw", Sets.newHashSet("all")),

                                  /**
                                   * 读写权限
                                   */
                                  RW(AppTemplateAuthEnum.RW, "rw", Sets.newHashSet("read", "write")),

                                  /**
                                   * 读权限
                                   */
                                  R(AppTemplateAuthEnum.R, "r", Sets.newHashSet("read"));

    private final AppTemplateAuthEnum appTemplateAuthEnum;

    private final String              authName;

    private final Set<String>         privilegeSet;

    SecurityRoleAuthEnum(AppTemplateAuthEnum appTemplateAuthEnum, String authName, Set<String> privilegeSet) {
        this.appTemplateAuthEnum = appTemplateAuthEnum;
        this.authName = authName;
        this.privilegeSet = privilegeSet;
    }

    public AppTemplateAuthEnum getAppTemplateAuthEnum() {
        return appTemplateAuthEnum;
    }

    public String getAuthName() {
        return authName;
    }

    public Set<String> getPrivilegeSet() {
        return privilegeSet;
    }

    public static SecurityRoleAuthEnum valueByAuth(AppTemplateAuthEnum appTemplateAuthEnum) {
        for (SecurityRoleAuthEnum securityRoleAuthEnum : SecurityRoleAuthEnum.values()) {
            if (securityRoleAuthEnum.getAppTemplateAuthEnum().equals(appTemplateAuthEnum)) {
                return securityRoleAuthEnum;
            }
        }
        return null;
    }
}
