package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.RoleExtendVO;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.role.RoleAssignDTO;
import com.didiglobal.logi.security.common.dto.role.RoleQueryDTO;
import com.didiglobal.logi.security.common.dto.role.RoleSaveDTO;
import com.didiglobal.logi.security.common.vo.role.AssignInfoVO;
import com.didiglobal.logi.security.common.vo.role.RoleBriefVO;
import com.didiglobal.logi.security.common.vo.role.RoleDeleteCheckVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * 角色扩展管理器
 *
 * @author shizeying
 * @date 2022/06/16
 */
public interface RoleExtendManager {
    /**
     * 删除角色通过角色id
     *
     * @param id      id
     * @param request 请求
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> deleteRoleByRoleId(Integer id, HttpServletRequest request);
        /**
     * 获取角色详情（主要是获取角色所拥有的权限信息）
     * @param roleId 角色id
     * @return RoleVo 角色信息
     */
    Result<RoleExtendVO> getRoleDetailByRoleId(Integer roleId);

    /**
     * 分页获取角色列表
     *
     * @param queryDTO 查询角色列表条件
     * @return 角色列表
     */
    PagingResult<RoleExtendVO> getRolePage(RoleQueryDTO queryDTO);

    /**
     * 保存角色
     * @param saveDTO 角色信息
     * @param request 请求信息
     * @throws LogiSecurityException 参数检查错误信息
     */
     Result<Void> createRole(RoleSaveDTO saveDTO, HttpServletRequest request);

   

    /**
     * 删除角色
     * @param roleId 角色id
     * @param userId 用户id
     * @param request 请求信息
     * @throws LogiSecurityException 该角色已分配给用户，不能删除
     */
     Result<Void> deleteUserFromRole(Integer roleId, Integer userId, HttpServletRequest request) ;

    /**
     * 更新角色信息
     * @param saveDTO 角色信息
     * @param request 请求信息
     * @throws LogiSecurityException 参数检查错误信息
     */
     Result<Void> updateRole(RoleSaveDTO saveDTO, HttpServletRequest request) ;

    /**
     * 分配角色给用户
     * @param assignDTO 分配信息
     * @param request 请求信息
     * @throws LogiSecurityException 角色分配flag不可为空
     */
     Result<Void> assignRoles(RoleAssignDTO assignDTO, HttpServletRequest request);

    /**
     * 根据角色id，获取分配信息
     * @param roleId 角色id
     * @return 分配信息List
     */
    Result<List<AssignInfoVO>> getAssignInfoByRoleId(Integer roleId);

    /**
     * 根据角色名模糊查询
     * @param roleName 角色名
     * @return 角色简要信息list
     */
    Result<List<RoleBriefVO>> getRoleBriefListByRoleName(String roleName);

    /**
     * 判断该角色是否已经分配给用户，如有分配给用户，则返回用户名list
     * @param roleId 角色id
     * @return 检查结果
     */
    Result<RoleDeleteCheckVO> checkBeforeDelete(Integer roleId);
}