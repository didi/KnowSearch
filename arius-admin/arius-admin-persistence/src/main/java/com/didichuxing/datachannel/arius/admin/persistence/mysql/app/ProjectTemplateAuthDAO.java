package com.didichuxing.datachannel.arius.admin.persistence.mysql.app;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectTemplateAuthPO;

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

    ProjectTemplateAuthPO getByAppIdAndTemplateIdAndType(@Param("projectId") Integer projectId,
                                                         @Param("templateId") String templateId,
                                                         @Param("type") Integer type);

    List<ProjectTemplateAuthPO> listByLogicTemplateId(String logicTemplateId);

    List<ProjectTemplateAuthPO> listWithRwAuths();

    List<ProjectTemplateAuthPO> listWithOwnerAuths();


}