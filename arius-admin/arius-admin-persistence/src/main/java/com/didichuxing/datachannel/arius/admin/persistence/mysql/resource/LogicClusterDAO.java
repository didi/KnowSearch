package com.didichuxing.datachannel.arius.admin.persistence.mysql.resource;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.LogicClusterPO;

/**
 * 逻辑集群DAO
 * @author d06679
 * @date 2019/3/22
 */
@Repository
public interface LogicClusterDAO {

    List<LogicClusterPO> listByCondition(LogicClusterPO param);

    int insert(LogicClusterPO param);

    int update(LogicClusterPO param);

    int delete(Long id);

    LogicClusterPO getById(Long id);

    List<LogicClusterPO> listByIds(@Param("ids") Set<Long> ids);

    LogicClusterPO getByName(String name);

    LogicClusterPO getLastCommon();

    List<LogicClusterPO> listByAppId(Integer appId);

    List<LogicClusterPO> listAll();

    List<LogicClusterPO> listByResponsible(String responsible);
}
