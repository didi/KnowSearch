package com.didichuxing.datachannel.arius.admin.biz.project;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.UserExtendDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.UserQueryExtendDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.UserExtendVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.UserWithPwVO;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.user.UserBriefQueryDTO;
import com.didiglobal.logi.security.common.dto.user.UserDTO;
import com.didiglobal.logi.security.common.entity.user.User;
import com.didiglobal.logi.security.common.vo.role.AssignInfoVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.common.vo.user.UserVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import java.util.List;

/**
 * 用户扩展管理器
 *
 * @author shizeying
 * @date 2022/06/16
 */
public interface UserExtendManager {
    /**
     * 添加用户
     *
     * @param param    入参
     * @param operator 操作人或角色
     * @return {@code Result<Void>}
     */
    Result<Void> addUser(UserDTO param, String operator);

    /**
     * 用户注册信息校验
     * @param type
     * @param value
     * @return
     */
    Result<Void> check(Integer type, String value);

    /**
     * 分页获取用户信息
     *
     * @param queryDTO 条件信息
     * @return 用户信息list
     */
    PagingResult<UserExtendVO> getUserPage(UserQueryExtendDTO queryDTO);

    /**
     * 分页获取用户简要信息
     * @param queryDTO 条件信息
     * @return 用户简要信息list
     */
    PagingResult<UserBriefVO> getUserBriefPage(UserBriefQueryDTO queryDTO);

    /**
     * 获取用户详情（主要是获取用户所拥有的权限信息）
     *
     * @param userId    用户id
     * @param projectId
     * @return 用户详情
     * @throws LogiSecurityException 用户不存在
     */
    Result<UserWithPwVO> getUserDetailByUserId(Integer userId, Integer projectId) throws Exception;

    /**
     * 根据用户id删除用户
     * @param userId
     * @return
     */
    Result<Void> deleteByUserId(Integer userId);

    /**
     * 获取用户简要信息
     * @param userName
     * @return 用户简要信息
     */
    Result<UserBriefVO> getUserBriefByUserName(String userName);

    /**
     * 获取用户简要信息
     * @param userName 用户名称
     * @return 用户简要信息
     */
    Result<User> getUserByUserName(String userName);

    /**
     * 获取用户简要信息List
     * @param userIdList 用户idList
     * @return 用户简要信息List
     */
    Result<List<UserBriefVO>> getUserBriefListByUserIdList(List<Integer> userIdList);

    /**
     * 根据部门id获取用户list（获取该部门下所有的用户，包括各种子部门）
     * @param deptId 部门id，如果为null，表示无部门用户
     * @return 用户简要信息list
     */
    Result<List<UserBriefVO>> getUserBriefListByDeptId(Integer deptId);

    /**
     * 根据用户id和roleName获取角色list
     * @param userId 用户id
     * @return 分配角色或者分配用户/列表信息
     */
    Result<List<AssignInfoVO>> getAssignDataByUserId(Integer userId);

    /**
     * 根据角色id获取用户list
     * @param roleId 角色Id
     * @return 用户简要信息list
     */
    Result<List<UserBriefVO>> getUserBriefListByRoleId(Integer roleId);

    /**
     * 会分别以账户名和实名去模糊查询，返回两者的并集
     * 创建项目，添加项目负责人的时候用到
     * @param name 账户名或实名
     * @return 用户简要信息list
     */
    Result<List<UserBriefVO>> getUserBriefListByUsernameOrRealName(String name);

    /**
     * 获取用户简要信息List并根据创建时间排序
     * @param isAsc 是否升序
     * @return 用户简要信息List
     */
    Result<List<UserBriefVO>> getAllUserBriefListOrderByCreateTime(boolean isAsc);

    /**
     * 会分别以账户名和实名去模糊查询，返回两者的并集
     * @param name 账户名或实名
     * @return 用户IdList
     */
    Result<List<Integer>> getUserIdListByUsernameOrRealName(String name);

    /**
     * 获取所有用户简要信息
     * @return 用户简要信息List
     */
    Result<List<UserBriefVO>> getAllUserBriefList();

    /**
     * 编辑一个用户
     * @param userDTO
     * @param operator
     * @return
     */
    Result<Void> editUser(UserExtendDTO userDTO, String operator);

    Result<List<UserVO>> getUserDetailByUserIds(List<Integer> ids);
}