package com.didichuxing.datachannel.arius.admin.core.component;

import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didiglobal.knowframework.security.common.vo.role.RoleVO;
import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;
import com.didiglobal.knowframework.security.common.vo.user.UserVO;
import com.didiglobal.knowframework.security.service.RoleService;
import com.didiglobal.knowframework.security.service.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 角色工具
 *
 * @author shizeying
 * @date 2022/06/01
 * @see com.didiglobal.logi.security.service.RoleService 实现admin册指定角色任务的判断
 */
@Component
public class RoleTool {
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserService userService;

    /**
     * 校验用户是否为管理员
     *
     * @param userName 用户名
     * @return boolean
     */
    public boolean isAdmin(String userName) {
        final RoleVO roleVO = roleService.getRoleDetailByRoleId(AuthConstant.ADMIN_ROLE_ID);
        return Optional.ofNullable(roleVO).map(RoleVO::getAuthedUsers).orElse(Collections.emptyList()).stream()
            .anyMatch(user -> StringUtils.equals(user, userName));
    }

    /**
     * 管理员用户列表
     *
     * @return {@code List<UserBriefVO>}
     */
    public List<UserBriefVO> getAdminList() {
        final RoleVO roleVO = roleService.getRoleDetailByRoleId(AuthConstant.ADMIN_ROLE_ID);
        return Optional.ofNullable(roleVO).map(RoleVO::getAuthedUsers)

            .orElse(Collections.emptyList()).stream().map(userService::getUserBriefByUserName)
            .collect(Collectors.toList());
    }

    public boolean isAdmin(Integer userId) {
        final RoleVO roleVO = roleService.getRoleDetailByRoleId(AuthConstant.ADMIN_ROLE_ID);
        final UserVO userVO = userService.getUserDetailByUserId(userId);
        return Optional.ofNullable(roleVO).map(RoleVO::getAuthedUsers).orElse(Collections.emptyList()).stream()
            .anyMatch(user -> StringUtils.equals(user, userVO.getUserName()));
    }
}