package com.didichuxing.datachannel.arius.admin.biz.template.srv.security;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

/**
 * @author didi
 */
public interface SecurityUserService {
    /**
     * 为用户添加角色
     *
     * @param cluster 集群
     * @param userName 用户
     * @param roleName 角色名字
     * @param projectId projectId
     * @param retryCount  重试次数
     * @return result
     */
    Result<Boolean> appendUserRoles(String cluster, String userName, String roleName, Integer projectId,
                           int retryCount) throws ESOperateException;

    /**
     * 删除用户角色
     *
     * @param cluster 集群
     * @param userName 用户
     * @param roleName 角色名字
     * @param retryCount  重试次数
     * @return result
     */
    Result<Boolean> deleteUserRoles(String cluster, String userName, String roleName, int retryCount) throws ESOperateException;

    /**
     * 确保用户拥有制定权限
     * @param cluster 集群
     * @param userName 用户
     * @param roleName 权限
     * @param projectId projectId
     * @return result
     */
    Result<Boolean> ensureUserHasAuth(String cluster, String userName, String roleName, Integer projectId);

    /**
     * 修改密码
     * @param cluster 集群
     * @param userName 用户名
     * @param verifyCode 密码
     * @param retryCount retryCount
     * @return
     */
    Result<Boolean> changePasswordIfExist(String cluster, String userName, String verifyCode,
                                 int retryCount) throws ESOperateException;
}