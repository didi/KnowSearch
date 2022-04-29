package com.didichuxing.datachannel.arius.admin.biz.template.srv.security;

import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

/**
 * @author didi
 */
public interface SecurityRoleService {
    /**
     * 创建角色
     * @param cluster 集群
     * @param roleName  角色名字
     * @param expression  索引名称
     * @param privilegeSet 权限集合
     * @param retryCount  重试次数
     * @return result
     * @throws ESOperateException
     */
    Result<Boolean> createRoleIfAbsent(String cluster, String roleName, String expression, Set<String> privilegeSet,
                              int retryCount) throws ESOperateException;

    /**
     * 确认角色存在
     * @param cluster 集群
     * @param roleName 角色
     * @param expression  索引名称
     * @param privilegeSet 权限集合
     * @return result
     */
    Result<Boolean> ensureRoleExist(String cluster, String roleName, String expression, Set<String> privilegeSet);
}
