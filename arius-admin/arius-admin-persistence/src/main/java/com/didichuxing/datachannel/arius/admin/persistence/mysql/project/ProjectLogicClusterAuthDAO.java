package com.didichuxing.datachannel.arius.admin.persistence.mysql.project;

import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectClusterLogicAuthPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wangshu
 * @date 2020/09/17
 */
@Repository
public interface ProjectLogicClusterAuthDAO {

    List<ProjectClusterLogicAuthPO> listByCondition(ProjectClusterLogicAuthPO param);

    int insert(ProjectClusterLogicAuthPO param);

    int update(ProjectClusterLogicAuthPO param);

    int delete(Long authId);

    int deleteByLogicClusterId(Long logicClusterId);

    List<ProjectClusterLogicAuthPO> listByProjectId(@Param("projectId")int projectId);

    List<ProjectClusterLogicAuthPO> listWithAccessByProjectId(@Param("projectId")int projectId);

    ProjectClusterLogicAuthPO getById(Long authId);

    ProjectClusterLogicAuthPO getByProjectIdAndLogicClusterId(@Param("projectId") Integer projectId,
                                                              @Param("logicClusterId") Long logicClusterId);

    ProjectClusterLogicAuthPO getByProjectIdAndLogicClusterIdAndType(@Param("projectId") Integer projectId,
                                                                     @Param("logicClusterId") Long logicClusterId,
                                                                     @Param("type") Integer type);
}