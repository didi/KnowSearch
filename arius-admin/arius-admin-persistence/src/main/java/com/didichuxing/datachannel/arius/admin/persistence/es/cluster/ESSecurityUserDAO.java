package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.ES_OPERATE_TIMEOUT;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.security.SecurityUser;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.security.ESGetSecurityUserResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.security.ESPutSecurityUserResponse;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @author didi
 */
@Repository
public class ESSecurityUserDAO extends BaseESDAO {

    private static final ILog LOGGER = LogFactory.getLog(ESSecurityRoleDAO.class);

    /**
     * 获取用户
     * @param cluster 集群
     * @param userName 用户
     * @return result
     */
    public SecurityUser getByName(String cluster, String userName) {
        ESClient client = esOpClient.getESClient(cluster);
        ESGetSecurityUserResponse response = client.admin().indices().prepareGetSecurityUser().setUserName(userName)
            .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        Map<String, SecurityUser> userMap = response.getUserMap();
        return userMap == null ? null : userMap.get(userName);
    }

    /**
     * put用户
     * @param cluster 集群
     * @param userName 用户
     * @param securityUser 信息
     * @return result
     */
    public boolean putUser(String cluster, String userName, SecurityUser securityUser) {
        ESClient client = esOpClient.getESClient(cluster);
        ESPutSecurityUserResponse response = client.admin().indices().preparePutSecurityUser().setName(userName)
            .setUser(securityUser).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        LOGGER.info("method=putUser||cluster={}||roleName={}||response={}", cluster, userName, response.getCreated());
        return true;
    }
}
