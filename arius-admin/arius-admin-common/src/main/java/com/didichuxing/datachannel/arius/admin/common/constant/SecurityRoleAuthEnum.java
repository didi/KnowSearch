package com.didichuxing.datachannel.arius.admin.common.constant;

import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import com.google.common.collect.Sets;

/**
 * @author didi
 */
public enum SecurityRoleAuthEnum {

                                  /**
                                   * 管理权限
                                   */
                                  OWN(ProjectTemplateAuthEnum.OWN, "own_rw", Sets.newHashSet("all")),

                                  /**
                                   * 读写权限
                                   */
                                  RW(ProjectTemplateAuthEnum.RW, "rw", Sets.newHashSet("read", "write")),

                                  /**
                                   * 读权限
                                   */
                                  R(ProjectTemplateAuthEnum.R, "r", Sets.newHashSet("read"));

    private final ProjectTemplateAuthEnum projectTemplateAuthEnum;

    private final String              authName;

    private final Set<String>         privilegeSet;

    SecurityRoleAuthEnum(ProjectTemplateAuthEnum projectTemplateAuthEnum, String authName, Set<String> privilegeSet) {
        this.projectTemplateAuthEnum = projectTemplateAuthEnum;
        this.authName = authName;
        this.privilegeSet = privilegeSet;
    }

    public ProjectTemplateAuthEnum getAppTemplateAuthEnum() {
        return projectTemplateAuthEnum;
    }

    public String getAuthName() {
        return authName;
    }

    public Set<String> getPrivilegeSet() {
        return privilegeSet;
    }

    public static SecurityRoleAuthEnum valueByAuth(ProjectTemplateAuthEnum projectTemplateAuthEnum) {
        for (SecurityRoleAuthEnum securityRoleAuthEnum : SecurityRoleAuthEnum.values()) {
            if (securityRoleAuthEnum.getAppTemplateAuthEnum().equals(projectTemplateAuthEnum)) {
                return securityRoleAuthEnum;
            }
        }
        return null;
    }
}