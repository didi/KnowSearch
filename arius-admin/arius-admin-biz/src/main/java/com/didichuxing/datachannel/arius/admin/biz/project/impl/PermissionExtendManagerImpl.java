package com.didichuxing.datachannel.arius.admin.biz.project.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.project.PermissionExtendManager;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didiglobal.knowframework.security.common.Result;
import com.didiglobal.knowframework.security.common.vo.permission.PermissionTreeVO;
import com.didiglobal.knowframework.security.service.PermissionService;

/**
 * 扩展管理器impl许可
 *
 * @author shizeying
 * @date 2022/06/14
 */
@Component
public class PermissionExtendManagerImpl implements PermissionExtendManager {
    @Autowired
    private PermissionService permissionService;

    /**
     * 建立资源owner角色权限树
     *
     * @return {@code Result<PermissionTreeVO>}
     */
    @Override
    public Result<PermissionTreeVO> buildPermissionTreeByResourceOwn() {
        final PermissionTreeVO permissionTreeVO = permissionService
            .buildPermissionTreeByRoleId(AuthConstant.RESOURCE_OWN_ROLE_ID);
        return Result.buildSucc(permissionTreeVO);
    }

}