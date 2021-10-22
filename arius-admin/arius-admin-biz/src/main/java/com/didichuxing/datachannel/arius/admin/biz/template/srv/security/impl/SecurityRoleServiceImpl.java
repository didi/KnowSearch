package com.didichuxing.datachannel.arius.admin.biz.template.srv.security.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityRoleService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESSecurityRoleDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.security.SecurityRole;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @author didi
 */
@Service
public class SecurityRoleServiceImpl implements SecurityRoleService {
    private static final ILog LOGGER = LogFactory.getLog(SecurityRoleServiceImpl.class);

    @Autowired
    private ESSecurityRoleDAO esSecurityRoleDAO;

    /**
     * 创建角色
     *
     * @param cluster      集群
     * @param roleName     角色名字
     * @param expression   索引名称
     * @param privilegeSet 权限集合
     * @param retryCount   重试次数
     * @return result
     */
    @Override
    public Result createRoleIfAbsent(String cluster, String roleName, String expression, Set<String> privilegeSet,
                                     int retryCount) throws ESOperateException {
        SecurityRole role = esSecurityRoleDAO.getNyName(cluster, roleName);
        if (role != null) {
            return Result.buildSucc();
        }

        LOGGER.info("method=createRoleIfAbsent||cluster={}||roleName={}||expression={}||privilegeSet={}", cluster,
            roleName, expression, privilegeSet);

        return Result.build(ESOpTimeoutRetry.esRetryExecute("createRoleIfAbsent", retryCount,
            () -> esSecurityRoleDAO.putRole(cluster, roleName, expression, privilegeSet)));
    }

    /**
     * 确认角色存在
     *
     * @param cluster      集群
     * @param roleName     角色
     * @param expression   索引名称
     * @param privilegeSet 权限集合
     * @return result
     */
    @Override
    public Result ensureRoleExist(String cluster, String roleName, String expression, Set<String> privilegeSet) {
        try {
            Result result = createRoleIfAbsent(cluster, roleName, expression, privilegeSet, 3);
            LOGGER.info("method=ensureRoleExist||cluster={}||roleName={}||result={}", cluster, roleName,
                result.getMessage());
            return result;
        } catch (Exception e) {
            LOGGER.error("method=ensureRoleExist||cluster={}||roleName={}||errMsg={}", cluster, roleName,
                e.getMessage(), e);
            return Result.buildFail(e.getMessage());
        }
    }
}
