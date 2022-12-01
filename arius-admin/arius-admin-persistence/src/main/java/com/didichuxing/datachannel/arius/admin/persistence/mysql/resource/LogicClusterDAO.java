package com.didichuxing.datachannel.arius.admin.persistence.mysql.resource;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicPO;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 逻辑集群DAO
 * @author d06679
 * @date 2019/3/22
 */
@Repository
public interface LogicClusterDAO {

    List<ClusterLogicPO> listByCondition(ClusterLogicPO param);

    List<ClusterLogicPO> listByNameAndProjectId(@Param("name") String name, @Param("projectId") Integer projectId);

    int insert(ClusterLogicPO param);

    int update(ClusterLogicPO param);

    int delete(Long id);

    ClusterLogicPO getById(Long id);

    List<ClusterLogicPO> listByIds(@Param("ids") Set<Long> ids);

    ClusterLogicPO getByName(String name);

    ClusterLogicPO getLastCommon();

    List<ClusterLogicPO> listByProjectId(@Param("projectId") Integer projectId);

    List<ClusterLogicPO> listAll();


    List<ClusterLogicPO> pagingByCondition(ClusterLogicConditionDTO param);

    Long getTotalHitByCondition(ClusterLogicConditionDTO param);
    
    List<ClusterLogicPO> listByLevel(Integer level);
    
    /**
     * 获取 projectId 等于给定 projectId 的实体的所有 ID。
     *
     * @param projectId 您要为其获取 ID 的项目的 ID。
     * @return 项目中具有给定 ID 的所有任务 ID 的列表。
     */
    List<Long> getAllIdsByProjectId(Integer projectId);
}