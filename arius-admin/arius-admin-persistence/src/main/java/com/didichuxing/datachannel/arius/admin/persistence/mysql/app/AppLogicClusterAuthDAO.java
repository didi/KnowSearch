package com.didichuxing.datachannel.arius.admin.persistence.mysql.app;

import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppLogicClusterAuthPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wangshu
 * @date 2020/09/17
 */
@Repository
public interface AppLogicClusterAuthDAO {

    List<AppLogicClusterAuthPO> listByCondition(AppLogicClusterAuthPO param);

    int insert(AppLogicClusterAuthPO param);

    int update(AppLogicClusterAuthPO param);

    int delete(Long authId);

    List<AppLogicClusterAuthPO> listByAppId(int appId);

    List<AppLogicClusterAuthPO> listWithRauthByAppId(int appId);

    AppLogicClusterAuthPO getById(Long authId);

    AppLogicClusterAuthPO getByAppIdAndLogicCluseterId(@Param("appId") Integer appId,
                                                       @Param("logicClusterId") Long logicClusterId);

    AppLogicClusterAuthPO getByAppIdAndLogicClusterIdAndType(@Param("appId") Integer appId,
                                                             @Param("logicClusterId") Long logicClusterId,
                                                             @Param("type") Integer type);
}
