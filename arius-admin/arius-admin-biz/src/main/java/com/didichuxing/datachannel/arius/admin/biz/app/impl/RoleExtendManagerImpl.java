package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.RoleExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.RoleExtendVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.security.common.PagingData;
import com.didiglobal.logi.security.common.PagingData.Pagination;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.role.RoleAssignDTO;
import com.didiglobal.logi.security.common.dto.role.RoleQueryDTO;
import com.didiglobal.logi.security.common.dto.role.RoleSaveDTO;
import com.didiglobal.logi.security.common.vo.role.AssignInfoVO;
import com.didiglobal.logi.security.common.vo.role.RoleBriefVO;
import com.didiglobal.logi.security.common.vo.role.RoleDeleteCheckVO;
import com.didiglobal.logi.security.common.vo.role.RoleVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.RoleService;
import java.util.List;
import java.util.Objects;
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
	
	@Override
	public Result<RoleExtendVO> getRoleDetailByRoleId(Integer roleId) {
		final RoleVO roleVO = roleService.getRoleDetailByRoleId(roleId);
		final RoleExtendVO roleExtendVO = ConvertUtil.obj2Obj(roleVO, RoleExtendVO.class);
		if (Objects.equals(roleExtendVO.getId(), AuthConstant.RESOURCE_OWN_ROLE_ID) || Objects.equals(
				roleExtendVO.getId(), AuthConstant.ADMIN_ROLE_ID)) {
			roleExtendVO.setIsDefaultRole(true);
		}
		return Result.buildSucc(roleExtendVO);
	}
	
	@Override
	public PagingResult<RoleExtendVO> getRolePage(RoleQueryDTO queryDTO) {
		final PagingData<RoleVO> rolePage = roleService.getRolePage(queryDTO);
		final List<RoleVO> bizData = rolePage.getBizData();
		final List<RoleExtendVO> roleExtendVOList = ConvertUtil.list2List(bizData, RoleExtendVO.class);
		for (RoleExtendVO roleExtendVO : roleExtendVOList) {
			if (Objects.equals(roleExtendVO.getId(), AuthConstant.RESOURCE_OWN_ROLE_ID) || Objects.equals(
					roleExtendVO.getId(), AuthConstant.ADMIN_ROLE_ID)) {
				roleExtendVO.setIsDefaultRole(true);
			}
		}
		final Pagination pagination = rolePage.getPagination();
		return PagingResult.success(new PagingData<>(roleExtendVOList, pagination));
	}
	
	@Override
	public Result<Void> createRole(RoleSaveDTO saveDTO, HttpServletRequest request) {
		try {
			roleService.createRole(saveDTO, request);
			return Result.buildSucc();
		} catch (LogiSecurityException e) {
			return Result.buildFail(e.getMessage());
		}
	}
	
	@Override
	public Result<Void> deleteUserFromRole(Integer roleId, Integer userId, HttpServletRequest request) {
		try {
			roleService.deleteUserFromRole(roleId, userId, request);
			return Result.buildSucc();
		} catch (LogiSecurityException e) {
			return Result.buildFail(e.getMessage());
		}
	}
	
	@Override
	public Result<Void> updateRole(RoleSaveDTO saveDTO, HttpServletRequest request) {
		try {
			roleService.updateRole(saveDTO, request);
			return Result.buildSucc();
		} catch (LogiSecurityException e) {
			return Result.buildFail(e.getMessage());
		}
	}
	
	@Override
	public Result<Void> assignRoles(RoleAssignDTO assignDTO, HttpServletRequest request) {
		try {
			roleService.assignRoles(assignDTO, request);
			return Result.buildSucc();
			
		} catch (LogiSecurityException e) {
			return Result.buildFail(e.getMessage());
		}
	}
	
	@Override
	public Result<List<AssignInfoVO>> getAssignInfoByRoleId(Integer roleId) {
		return Result.buildSucc(roleService.getAssignInfoByRoleId(roleId));
	}
	
	@Override
	public Result<List<RoleBriefVO>> getRoleBriefListByRoleName(String roleName) {
		return Result.buildSucc(roleService.getRoleBriefListByRoleName(roleName));
	}
	
	@Override
	public Result<RoleDeleteCheckVO> checkBeforeDelete(Integer roleId) {
		return Result.buildSucc(roleService.checkBeforeDelete(roleId));
	}
	

}