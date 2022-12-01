package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.knowframework.elasticsearch.client.ESClient;
import com.didiglobal.knowframework.elasticsearch.client.request.security.SecurityRole;
import com.didiglobal.knowframework.elasticsearch.client.request.security.SecurityRoleIndex;
import com.didiglobal.knowframework.elasticsearch.client.response.security.ESGetSecurityRoleResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.security.ESPutSecurityRoleResponse;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_TIMEOUT;

/**
 * @author didi
 */
@Repository
public class ESSecurityRoleDAO extends BaseESDAO {

    /**
     * 获取角色
     * @param cluster 集群
     * @param roleName 角色名字
     * @return result
     */
    public SecurityRole getNyName(String cluster, String roleName) {
        ESClient client = esOpClient.getESClient(cluster);
        ESGetSecurityRoleResponse response = client.admin().indices().prepareGetSecurityRole().setRoleName(roleName)
            .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        Map<String, SecurityRole> roleMap = response.getRoleMap();
        return roleMap == null ? null : roleMap.get(roleName);
    }

    /**
     * put角色
     * @param cluster 集群
     * @param roleName 角色名字
     * @param expression 索引
     * @param privilegeSet 权限点
     * @return result
     */
    public boolean putRole(String cluster, String roleName, String expression, Set<String> privilegeSet) {
        ESClient client = esOpClient.getESClient(cluster);
        ESPutSecurityRoleResponse response = client.admin().indices().preparePutSecurityRole().setName(roleName)
            .setRole(buildRole(expression, privilegeSet)).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        LOGGER.info("class=ESSecurityRoleDAO||method=putRole||cluster={}||roleName={}||response={}", cluster, roleName,
            response.getRole().toJSONString());
        return true;
    }

    /**************************************** private method ****************************************************/

    private SecurityRole buildRole(String expression, Set<String> privilegeSet) {
        SecurityRoleIndex securityRoleIndex = new SecurityRoleIndex();
        securityRoleIndex.setNames(Lists.newArrayList(expression));
        securityRoleIndex.setPrivileges(Lists.newArrayList(privilegeSet));
        SecurityRole securityRole = new SecurityRole();
        securityRole.setIndices(Lists.newArrayList(securityRoleIndex));
        return securityRole;
    }
}