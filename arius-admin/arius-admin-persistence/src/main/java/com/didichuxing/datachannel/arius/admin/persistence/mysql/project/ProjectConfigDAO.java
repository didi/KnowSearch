package com.didichuxing.datachannel.arius.admin.persistence.mysql.project;

import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectConfigPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * esconfigdao
 *
 * @author shizeying
 * @date 2022/05/25
 */
@Repository
public interface ProjectConfigDAO {

    ProjectConfigPO getByProjectId(@Param("projectId") int projectId);

    int insert(ProjectConfigPO param);

    int update(ProjectConfigPO param);

    List<ProjectConfigPO> listAll();

    boolean checkProjectConfigByProjectId(@Param("projectId") int projectId);

    int deleteByProjectId(@Param("projectId") int projectId);
}