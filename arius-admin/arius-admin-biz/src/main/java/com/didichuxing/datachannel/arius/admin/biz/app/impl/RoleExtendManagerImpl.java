package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.RoleExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.RoleService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 扩展管理器角色impl
 *
 * @author shizeying
 * @date 2022/06/16
 */
@Component
public class RoleExtendManagerImpl implements RoleExtendManager {
    @Autowired
    private RoleService roleService;
    
    /**
     * @param id
     * @param request
     * @return
     */
    @Override
    public Result<Void> deleteRoleByRoleId(Integer id, HttpServletRequest request) {
        if (AuthConstant.RESOURCE_OWN_ROLE_ID.equals(id) || AuthConstant.ADMIN_ROLE_ID.equals(id)) {
            return Result.buildFail(String.format("属于内置角色:[%s]，不可以被删除", id));
        }
        try {
            roleService.deleteRoleByRoleId(id, request);
            return Result.buildSucc();
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
}