package com.didichuxing.datachannel.arius.admin.persistence.mysql.resource;

import java.util.List;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicPO;

/**
 * 逻辑集群DAO
 * @author d06679
 * @date 2019/3/22
 */
@Repository
public interface LogicClusterDAO {

    List<ClusterLogicPO> listByCondition(ClusterLogicPO param);

    int insert(ClusterLogicPO param);

    int update(ClusterLogicPO param);

    int delete(Long id);

    ClusterLogicPO getById(Long id);

    List<ClusterLogicPO> listByIds(@Param("ids") Set<Long> ids);

    ClusterLogicPO getByName(String name);

    ClusterLogicPO getLastCommon();

    List<ClusterLogicPO> listByProjectId(@Param("projectId") Integer projectId);

    List<ClusterLogicPO> listAll();

    List<ClusterLogicPO> listByResponsible(String responsible);

//    List<ClusterLogicPO> pagingByCondition(@Param("name") String name,  @Param("appId") Integer appId,
//                                           @Param("type") Integer type, @Param("health") Integer health,
//                                           @Param("from") Long from,    @Param("size") Long size,
//                                           @Param("sortTerm") String sortTerm, @Param("sortType") String sortType);

    List<ClusterLogicPO> pagingByCondition(ClusterLogicConditionDTO param);

    Long getTotalHitByCondition(ClusterLogicPO param);
}