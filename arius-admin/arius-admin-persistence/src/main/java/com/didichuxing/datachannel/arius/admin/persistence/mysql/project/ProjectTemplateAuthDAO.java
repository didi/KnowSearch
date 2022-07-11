package com.didichuxing.datachannel.arius.admin.persistence.mysql.project;

import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectTemplateAuthPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author d06679
 * @date 2019/4/16
 */
@Repository
public interface ProjectTemplateAuthDAO {

    List<ProjectTemplateAuthPO> listByCondition(ProjectTemplateAuthPO param);

    int insert(ProjectTemplateAuthPO param);

    int update(ProjectTemplateAuthPO param);

    int delete(Long authId);

    int deleteByTemplate(Integer templateId);

    int batchDeleteByTemplateIds(List<Integer> templateIds);

    List<ProjectTemplateAuthPO> listWithRwAuthsByProjectId(@Param("projectId")int projectId);

    List<ProjectTemplateAuthPO> getByTemplateId(Integer templateId);

    ProjectTemplateAuthPO getById(Long authId);

    ProjectTemplateAuthPO getByProjectIdAndTemplateId(@Param("projectId") Integer projectId,
                                                      @Param("templateId") String templateId);

    ProjectTemplateAuthPO getByProjectIdAndTemplateIdAndType(@Param("projectId") Integer projectId,
                                                             @Param("templateId") String templateId,
                                                             @Param("type") Integer type);

    List<ProjectTemplateAuthPO> listByLogicTemplateId(String logicTemplateId);

    List<ProjectTemplateAuthPO> listWithRwAuths();

    List<ProjectTemplateAuthPO> listWithOwnerAuths();
    
    Integer getProjectIdById(Long authId);
}