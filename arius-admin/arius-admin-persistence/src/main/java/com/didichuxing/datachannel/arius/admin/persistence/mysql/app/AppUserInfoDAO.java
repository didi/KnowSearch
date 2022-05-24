package com.didichuxing.datachannel.arius.admin.persistence.mysql.app;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppUserInfoPO;

/**
 * @author d06679
 * @date 2019/5/20
 * @deprecated 后续下线移除
 */
@Deprecated
@Repository
public interface AppUserInfoDAO {

    int insert(AppUserInfoPO param);

    List<AppUserInfoPO> listByUser(String userName);

    AppUserInfoPO getByAppIdAndUser(@Param("appId") Integer appId,
                                    @Param("userName") String userName);

    int update(AppUserInfoPO param);

    AppUserInfoPO getLastLoginRecordByAppId(Integer appId);
}