package com.didichuxing.datachannel.arius.admin.biz.project.impl;

import com.didichuxing.datachannel.arius.admin.biz.project.RoleExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.RoleExtendVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
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
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.service.RoleService;
import com.didiglobal.logi.security.service.UserProjectService;
import com.didiglobal.logi.security.service.UserService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
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
	@Autowired
	private OperateRecordService operateRecordService;
	@Autowired
	private UserProjectService userProjectService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserService userService;
	
	/**
	 * @param id
	 * @param request
	 * @return
	 */
	@Override
	public Result deleteRoleByRoleId(Integer id, HttpServletRequest request) {
		if (AuthConstant.RESOURCE_OWN_ROLE_ID.equals(id) || AuthConstant.ADMIN_ROLE_ID.equals(id)) {
			return Result.buildFail(String.format("属于内置角色:[%s]，不可以被删除", id));
		}
		try {
			final RoleBriefVO roleBriefByRoleId = roleService.getRoleBriefByRoleId(id);
			final RoleDeleteCheckVO roleDeleteCheckVO = roleService.checkBeforeDelete(id);
			if (CollectionUtils.isNotEmpty(roleDeleteCheckVO.getUserNameList())){
				final RoleVO roleVO = roleService.getRoleDetailByRoleId(id);
				return Result.buildFailWithMsg(roleDeleteCheckVO,String.format("角色:[%s]已经分配给用了,不允许删除,请先解除分配的用户再试！",
						roleVO.getRoleName()));
			}
			
			roleService.deleteRoleByRoleId(id, request);
			operateRecordService.save(new OperateRecord.Builder()
							.userOperation(HttpRequestUtil.getOperator(request))
							.operationTypeEnum(OperateTypeEnum.ROLE_MANAGER_DELETE)
							.content(String.format("删除角色:[%s]",roleBriefByRoleId.getRoleName()))
						.project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
							.triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
					.build()
			
			);
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
			operateRecordService.save(new OperateRecord.Builder().userOperation(HttpRequestUtil.getOperator(request))
					.operationTypeEnum(OperateTypeEnum.ROLE_MANAGER_CREATE).content(String.format("新增角色:[%s]",
							saveDTO.getRoleName()))
						.project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
					.triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).build()
			
			);
			return Result.buildSucc();
		} catch (LogiSecurityException e) {
			return Result.buildFail(e.getMessage());
		}
	}
	
	@Override
	public Result<Void> deleteUserFromRole(Integer roleId, Integer userId, HttpServletRequest request) {
		try {
			roleService.deleteUserFromRole(roleId, userId, request);
			//如果改角色为超级管理员、那么需要一并删除超级项目的管理能力
			if (AuthConstant.ADMIN_ROLE_ID.equals(roleId)) {
				userProjectService.delOwnerProject(AuthConstant.SUPER_PROJECT_ID, Collections.singletonList(userId));
				userProjectService.delUserProject(AuthConstant.SUPER_PROJECT_ID, Collections.singletonList(userId));
			}
			final RoleBriefVO roleBriefByRoleId = roleService.getRoleBriefByRoleId(roleId);
			final UserBriefVO userBriefVO = userService.getUserBriefListByUserIdList(Collections.singletonList(userId))
					.get(0);
			operateRecordService.save(new OperateRecord.Builder().userOperation(HttpRequestUtil.getOperator(request))
					.operationTypeEnum(OperateTypeEnum.ROLE_MANAGER_UNBIND_USER)
					.content(String.format("角色:[%s]解绑的用户:[%s]", roleBriefByRoleId.getRoleName(),
							userBriefVO.getUserName()))
							.project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
					.triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).build()
			
			);
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
			
			List<Integer> userIds=Lists.newArrayList();
			List<Integer> roleIds=Lists.newArrayList();
			
			if (Boolean.TRUE.equals(assignDTO.getFlag())) {
				//true：N个角色分配给1个用户
				userIds.add(assignDTO.getId());
				roleIds.addAll(assignDTO.getIdList());
				
			} else {
				//false：1个角色分配给N个用户
				userIds.addAll(assignDTO.getIdList());
				roleIds.add(assignDTO.getId());
				
			}
			String roleNames =
					roleIds.stream().map(roleService::getRoleBriefByRoleId).map(RoleBriefVO::getRoleName).sorted().distinct()
							.collect(Collectors.joining(","));
			String userNames =
					userService.getUserBriefListByUserIdList(userIds).stream().map(UserBriefVO::getUserName).sorted().distinct().collect(
							Collectors.joining(","));
			
			operateRecordService.save(
						new OperateRecord.Builder().userOperation(HttpRequestUtil.getOperator(request))
								.operationTypeEnum(OperateTypeEnum.ROLE_MANAGER_BIND_USER)
								.project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
								.content(String.format("角色列表:[%s]解绑的用户列表:[%s]", roleNames, userNames))
								.triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).build());
		
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
	

	

}