package com.didichuxing.datachannel.arius.admin.core.service.project;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import java.util.List;

/**
 * project逻辑集群权限管理服务
 * @author wangshu
 * @date 2020/09/17
 */
public interface ProjectClusterLogicAuthService {

    /**
     * 设置 项目对某逻辑集群的权限.
     * 封装了新增、更新、删除操作，调用接口时只需描述期望的权限状态
     * @param projectId           项目
     * @param logicClusterId 逻辑集群ID
     * @param auth           要设置的权限
     * @param responsible    责任人，逗号分隔的用户名列表
     * @return 设置结果
     */
    Result<Void> ensureSetLogicClusterAuth(Integer projectId, Long logicClusterId, ProjectClusterLogicAuthEnum auth,
                                           String responsible, String operator);

    /**
     * 新增 项目逻辑集群权限
     * @param logicClusterAuth APP逻辑集群权限
     * @param operator 操作者
     * @return
     */
    Result<Void> addLogicClusterAuth(ProjectLogicClusterAuthDTO logicClusterAuth, String operator);

    /**
     * 新增 项目逻辑集群权限
     * @param logicClusterAuth APP逻辑集群权限
     * @param operator 操作者
     * @return
     */
    Result<Void> updateLogicClusterAuth(ProjectLogicClusterAuthDTO logicClusterAuth, String operator);

    /**
     * 删除 项目逻辑集群权限
     * @param authId 权限点ID
     * @param operator 操作者
     * @return
     */
    Result<Void> deleteLogicClusterAuthById(Long authId, String operator);

    Result<Boolean> deleteLogicClusterAuthByLogicClusterId(Long logicClusterId);

    /**
     * 根据权限记录ID获取逻辑集群权限点
     * @param authId 权限点ID
     * @return
     */
    ProjectClusterLogicAuth getLogicClusterAuthById(Long authId);

    /**
     * 获取指定 项目对指定逻辑集群的权限.
     * @param projectId           项目
     * @param logicClusterId 逻辑集群ID
     */
    ProjectClusterLogicAuthEnum getLogicClusterAuthEnum(Integer projectId, Long logicClusterId);

    /**
     * 获取指定 项目对指定逻辑集群的权限，若没有权限则返回null.
     * 有权限时，返回结果中id不为null则为来自于权限表的数据，否则为来自于创建表的数据
     * @param projectId           项目
     * @param logicClusterId 逻辑集群ID
     */
    ProjectClusterLogicAuth getLogicClusterAuth(Integer projectId, Long logicClusterId);

    /**
     * 获取指定 项目所有权限点
     * @param projectId  项目
     * @return
     */
    List<ProjectClusterLogicAuth> getAllLogicClusterAuths(Integer projectId);

    /**
     * 访问权限
     * @param projectId  项目
     * @return
     */
    List<ProjectClusterLogicAuth> getLogicClusterAccessAuths(Integer projectId);

    /**
     * 获取指定逻辑集群指定类型的权限点
     * @param logicClusterId 逻辑集群ID
     * @param clusterAuthType 逻辑集群权限类型，为null则不筛选权限类型，返回改逻辑集群的全部权限点
     * @return
     */
    List<ProjectClusterLogicAuth> getLogicClusterAuths(Long logicClusterId,
                                                       ProjectClusterLogicAuthEnum clusterAuthType);

    /**
     * 判断 项目是否有在指定逻辑集群下创建索引的权限
     * @param projectId  项目
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    boolean canCreateLogicTemplate(Integer projectId, Long logicClusterId);

    /**
     * 增加权限  不做参数校验
     * @param authDTO  权限信息
     * @param operator 操作人
     * @return result
     */
    Result<Void> addLogicClusterAuthWithoutCheck(ProjectLogicClusterAuthDTO authDTO, String operator);

    /**
     * 构建项目对物理集群的权限信息
     * @param projectId                   项目
     * @param clusterLogicId          逻辑集群Id
     * @param projectClusterLogicAuthEnum 权限点
     * @return
     */
    ProjectClusterLogicAuth buildClusterLogicAuth(Integer projectId, Long clusterLogicId,
                                                  ProjectClusterLogicAuthEnum projectClusterLogicAuthEnum);

    /**
     * 获取全量权限信息
     * @return
     */
    List<ProjectClusterLogicAuth> list();
}