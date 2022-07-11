package com.didichuxing.datachannel.arius.admin.core.service.project;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * APP逻辑模板权限控制服务
 * @author d06679
 * @date 2019/3/13
 */
public interface ProjectLogicTemplateAuthService {

    /**
     * 元数据校验
     * @param delete 是否执行删除操作
     * @return
     */
    boolean deleteRedundancyTemplateAuths(boolean delete);

    /**
     * 在逻辑模板权限表中设置项目对某逻辑模板的权限
     * 封装了新增、更新、删除操作，调用接口时只需描述期望的权限状态
     * @param projectId           project的ID
     * @param logicTemplateId 逻辑模板ID
     * @param auth            要设置的权限
     * @param responsible     责任人，逗号分隔的用户名列表
     * @return 设置结果
     */
    Result<Void> ensureSetLogicTemplateAuth(Integer projectId, Integer logicTemplateId, ProjectTemplateAuthEnum auth,
                                      String responsible, String operator);

    /**
     * 获取project有权限的逻辑模板权限点
     * @param projectId APP ID
     * @return 模板权限
     */
    List<ProjectTemplateAuth> getTemplateAuthsByProjectId(Integer projectId);

    /**
     * 从权限表获取prject对active逻辑模板的读写权限点
     * @param projectId project ID
     * @return
     */
    List<ProjectTemplateAuth> getProjectActiveTemplateRWAndRAuths(Integer projectId);
    List<ProjectTemplateAuth> getProjectTemplateRWAndRAuthsWithoutCodecResponsible(Integer projectId);
    List<ProjectTemplateAuth> getProjectActiveTemplateRWAuths(Integer projectId);
    List<ProjectTemplateAuth> getProjectActiveTemplateRAuths(Integer projectId);

    /**
     * 获取指定逻辑模板的模板权限点列表
     * @param logicTemplateId 逻辑模板id
     * @return 模板权限 WR R
     */
    List<ProjectTemplateAuth> getTemplateAuthsByLogicTemplateId(Integer logicTemplateId);

    /**
     * 获取指定逻辑模板的模板权读、读写限点列表
     */
    ProjectTemplateAuth getTemplateRWAuthByLogicTemplateIdAndProjectId(Integer logicTemplateId, Integer projectId);

    /**
     * 增加逻辑模板权限
     *
     * @param authDTO 权限信息
     * @return result
     */
    Result<Void> addTemplateAuth(ProjectTemplateAuthDTO authDTO);

    /**
     * 修改逻辑模板权限，仅可以修改权限类型和责任人
     * @param authDTO  参数
     * @param operator 操作人
     * @return result
     */
    Result<Void> updateTemplateAuth(ProjectTemplateAuthDTO authDTO, String operator);

    /**
     * 删除逻辑模板权限
     *
     * @param authId 主键
     * @return result
     */
    Result<Void> deleteTemplateAuth(Long authId);

    /**
     * 根据逻辑模板id删除权限信息
     *
     * @param templateId 模板Id
     * @return
     */
    Result<Void> deleteTemplateAuthByTemplateId(Integer templateId);

    /**
     * 获取所有project 的权限
     * @return map, key为APP ID, value为权限点信息
     */
    Map<Integer/*projectId*/, Collection<ProjectTemplateAuth>> getAllProjectTemplateAuths();

    /**
     * 获取当前projectId对逻辑索引的权限
     * projectId为超级项目, 有所有资源的管理权限
     */
    ProjectTemplateAuthEnum getAuthEnumByProjectIdAndLogicId(Integer projectId, Integer logicId);

    /**
     * 构建具备O指定权限点的模板
     * @param logicTemplate 逻辑模板
     * @return
     */
    ProjectTemplateAuth buildTemplateAuth(IndexTemplate logicTemplate, ProjectTemplateAuthEnum projectTemplateAuthEnum);
    
    Integer getProjectIdById(Long authId);
}