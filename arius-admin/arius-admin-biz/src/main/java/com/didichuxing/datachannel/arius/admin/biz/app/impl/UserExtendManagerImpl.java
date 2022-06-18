package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.UserExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.security.common.PagingData;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.user.UserBriefQueryDTO;
import com.didiglobal.logi.security.common.dto.user.UserDTO;
import com.didiglobal.logi.security.common.dto.user.UserQueryDTO;
import com.didiglobal.logi.security.common.entity.user.User;
import com.didiglobal.logi.security.common.vo.role.AssignInfoVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.common.vo.user.UserVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.UserService;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserExtendManagerImpl implements UserExtendManager {
	@Autowired
	private UserService          userService;
	@Autowired
	private OperateRecordService operateRecordService;
	
	/**
	 * 用户注册信息校验
	 *
	 * @param type
	 * @param value
	 * @return
	 */
	@Override
	public Result<Void> check(Integer type, String value) {
		com.didiglobal.logi.security.common.Result<Void> check = userService.check(type, value);
		if (check.failed()) {
			return Result.build(check.getCode(), check.getMessage());
		}
		return Result.buildSucc();
	}
	
	/**
	 * 分页获取用户信息
	 *
	 * @param queryDTO 条件信息
	 * @return 用户信息list
	 */
	@Override
	public PagingResult<UserVO> getUserPage(UserQueryDTO queryDTO) {
		PagingData<UserVO> userPage = userService.getUserPage(queryDTO);
		return PagingResult.success(userPage);
	}
	
	/**
	 * 分页获取用户简要信息
	 *
	 * @param queryDTO 条件信息
	 * @return 用户简要信息list
	 */
	@Override
	public PagingResult<UserBriefVO> getUserBriefPage(UserBriefQueryDTO queryDTO) {
		PagingData<UserBriefVO> userBriefPage = userService.getUserBriefPage(queryDTO);
		return PagingResult.success(userBriefPage);
	}
	
	/**
	 * 获取用户详情（主要是获取用户所拥有的权限信息）
	 *
	 * @param userId 用户id
	 * @return 用户详情
	 * @throws LogiSecurityException 用户不存在
	 */
	@Override
	public Result<UserVO> getUserDetailByUserId(Integer userId) {
		return Result.buildSucc(userService.getUserDetailByUserId(userId));
	}
	
	/**
	 * 根据用户id删除用户
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public Result<Void> deleteByUserId(Integer userId) {
		com.didiglobal.logi.security.common.Result<Void> deleteByUserId = userService.deleteByUserId(userId);
		if (deleteByUserId.failed()) {
			return Result.build(deleteByUserId.getCode(), deleteByUserId.getMessage());
		}
		return Result.buildSucc();
	}
	
	/**
	 * 获取用户简要信息
	 *
	 * @param userName
	 * @return 用户简要信息
	 */
	@Override
	public Result<UserBriefVO> getUserBriefByUserName(String userName) {
		
		return Result.buildSucc(userService.getUserBriefByUserName(userName));
	}
	
	/**
	 * 获取用户简要信息
	 *
	 * @param userName 用户名称
	 * @return 用户简要信息
	 */
	@Override
	public Result<User> getUserByUserName(String userName) {
		return Result.buildSucc(userService.getUserByUserName(userName));
	}
	
	/**
	 * 获取用户简要信息List
	 *
	 * @param userIdList 用户idList
	 * @return 用户简要信息List
	 */
	@Override
	public Result<List<UserBriefVO>> getUserBriefListByUserIdList(List<Integer> userIdList) {
		return Result.buildSucc(userService.getUserBriefListByUserIdList(userIdList));
	}
	
	/**
	 * 根据部门id获取用户list（获取该部门下所有的用户，包括各种子部门）
	 *
	 * @param deptId 部门id，如果为null，表示无部门用户
	 * @return 用户简要信息list
	 */
	@Override
	public Result<List<UserBriefVO>> getUserBriefListByDeptId(Integer deptId) {
		return Result.buildSucc(userService.getUserBriefListByDeptId(deptId));
	}
	
	/**
	 * 根据用户id和roleName获取角色list
	 *
	 * @param userId 用户id
	 * @return 分配角色或者分配用户/列表信息
	 * @throws LogiSecurityException 用户id不可为null
	 */
	@Override
	public Result<List<AssignInfoVO>> getAssignDataByUserId(Integer userId) {
		try {
			return Result.buildSucc(userService.getAssignDataByUserId(userId));
		} catch (LogiSecurityException e) {
			return Result.buildFail(e.getMessage());
		}
	}
	
	/**
	 * 根据角色id获取用户list
	 *
	 * @param roleId 角色Id
	 * @return 用户简要信息list
	 */
	@Override
	public Result<List<UserBriefVO>> getUserBriefListByRoleId(Integer roleId) {
		return Result.buildSucc(userService.getUserBriefListByRoleId(roleId));
	}
	
	/**
	 * 会分别以账户名和实名去模糊查询，返回两者的并集 创建项目，添加项目负责人的时候用到
	 *
	 * @param name 账户名或实名
	 * @return 用户简要信息list
	 */
	@Override
	public Result<List<UserBriefVO>> getUserBriefListByUsernameOrRealName(String name) {
		return Result.buildSucc(userService.getUserBriefListByUsernameOrRealName(name));
	}
	
	/**
	 * 获取用户简要信息List并根据创建时间排序
	 *
	 * @param isAsc 是否升序
	 * @return 用户简要信息List
	 */
	@Override
	public Result<List<UserBriefVO>> getAllUserBriefListOrderByCreateTime(boolean isAsc) {
		return Result.buildSucc(userService.getAllUserBriefListOrderByCreateTime(isAsc));
	}
	
	/**
	 * 会分别以账户名和实名去模糊查询，返回两者的并集
	 *
	 * @param name 账户名或实名
	 * @return 用户IdList
	 */
	@Override
	public Result<List<Integer>> getUserIdListByUsernameOrRealName(String name) {
		return Result.buildSucc(userService.getUserIdListByUsernameOrRealName(name));
	}
	
	/**
	 * 获取所有用户简要信息
	 *
	 * @return 用户简要信息List
	 */
	@Override
	public Result<List<UserBriefVO>> getAllUserBriefList() {
		return Result.buildSucc(userService.getAllUserBriefList());
	}
	
	/**
	 * 编辑一个用户
	 *
	 * @param userDTO
	 * @param operator
	 * @return
	 */
	@Override
	public Result<Void> editUser(UserDTO userDTO, String operator) {
		UserBriefVO userBriefVO = userService.getUserBriefByUserName(userDTO.getUserName());
		
		com.didiglobal.logi.security.common.Result<Void> voidResult = userService.editUser(userDTO, operator);
		
		if (voidResult.failed()) {
			return Result.build(voidResult.getCode(), voidResult.getMessage());
		}
		if (StringUtils.isNotBlank(userDTO.getEmail())) {
			operateRecordService.save(
					new OperateRecord(OperateTypeEnum.TENANT_INFO_MODIFY, TriggerWayEnum.MANUAL_TRIGGER,
							String.format("修改email:%s-->%s", userBriefVO.getEmail(), userDTO.getEmail()), operator,
							
							userBriefVO.getId()));
		}
		if (StringUtils.isNotBlank(userDTO.getPhone())) {
			operateRecordService.save(
					new OperateRecord(OperateTypeEnum.TENANT_INFO_MODIFY, TriggerWayEnum.MANUAL_TRIGGER,
							String.format("修改手机号:%s-->%s", userBriefVO.getPhone(), userDTO.getPhone()), operator
							
							,
							
							userBriefVO.getId()));
		}
		if (StringUtils.isNotBlank(userDTO.getRealName())) {
			operateRecordService.save(
					new OperateRecord(OperateTypeEnum.TENANT_INFO_MODIFY, TriggerWayEnum.MANUAL_TRIGGER,
							String.format("修改用户实名:%s-->%s", userBriefVO.getRealName(), userDTO.getRealName()), operator
							
							,
							
							userBriefVO.getId()));
		}
		if (StringUtils.isNotBlank(userDTO.getPw())) {
			operateRecordService.save(
					new OperateRecord(OperateTypeEnum.TENANT_INFO_MODIFY, TriggerWayEnum.MANUAL_TRIGGER, "修改用户密码",
							operator
							
							,
							
							userBriefVO.getId()));
		}
		return Result.buildSucc();
	}
	
	/**
	 * @param ids
	 * @return
	 */
	@Override
	public Result<List<UserVO>> getUserDetailByUserIds(List<Integer> ids) {
		return Result.buildSucc(userService.getUserDetailByUserIds(ids).getData());
	}
	
	/**
	 * 添加用户
	 *
	 * @param param    入参
	 * @param operator 操作人或角色
	 * @return {@code Result<Void>}
	 */
	@Override
	public Result<Void> addUser(UserDTO param, String operator) {
		param.setRoleIds(Collections.singletonList(AuthConstant.RESOURCE_OWN_ROLE_ID));
		final com.didiglobal.logi.security.common.Result<Void> result = userService.addUser(param, operator);
		if (result.failed()) {
			return Result.buildFail(result.getMessage());
		}
		operateRecordService.save(
				new OperateRecord(OperateTypeEnum.TENANT_ADD, TriggerWayEnum.MANUAL_TRIGGER, param.getUserName(),
						operator
				
				));
		return Result.buildSucc();
		
	}
}