package com.didichuxing.datachannel.arius.admin.persistence.mysql.app;

import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppClusterLogicAuthPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wangshu
 * @date 2020/09/17
 */
@Repository
public interface AppLogicClusterAuthDAO {

    List<AppClusterLogicAuthPO> listByCondition(AppClusterLogicAuthPO param);

    int insert(AppClusterLogicAuthPO param);

    int update(AppClusterLogicAuthPO param);

    int delete(Long authId);

    int deleteByLogicClusterId(Long logicClusterId);

    List<AppClusterLogicAuthPO> listByAppId(int appId);

    List<AppClusterLogicAuthPO> listWithAccessByAppId(int appId);

    AppClusterLogicAuthPO getById(Long authId);

    AppClusterLogicAuthPO getByAppIdAndLogicCluseterId(@Param("appId") Integer appId,
                                                       @Param("logicClusterId") Long logicClusterId);

    AppClusterLogicAuthPO getByAppIdAndLogicClusterIdAndType(@Param("appId") Integer appId,
                                                             @Param("logicClusterId") Long logicClusterId,
                                                             @Param("type") Integer type);
}
