package com.didichuxing.datachannel.arius.admin.persistence.mysql.arius;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.arius.AriusUserInfoPO;

/**
 * @author d06679
 * @date 2018/8/27
 */
@Repository
public interface AriusUserInfoDAO {

    int insert(AriusUserInfoPO param);

    AriusUserInfoPO getByDomainAccount(String domainAccount);

    List<AriusUserInfoPO> listByCondition(AriusUserInfoPO param);

    AriusUserInfoPO getById(Long userId);
    
    AriusUserInfoPO getByName(String name);

    int update(AriusUserInfoPO param);

    List<AriusUserInfoPO> listAllEnable();

    List<AriusUserInfoPO> listByRole(Integer role);

    List<AriusUserInfoPO> listByRoles(List<Integer> roles);
}
