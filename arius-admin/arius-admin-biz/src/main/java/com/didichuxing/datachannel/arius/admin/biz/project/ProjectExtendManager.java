package com.didichuxing.datachannel.arius.admin.biz.project;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectExtendSaveDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectQueryExtendDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectBriefExtendVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectExtendVO;
import com.didiglobal.knowframework.security.common.PagingResult;
import com.didiglobal.knowframework.security.common.vo.project.ProjectDeleteCheckVO;
import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * 基于logi的扩展能力
 *
 * @author shizeying
 * @date 2022/06/10
 */
public interface ProjectExtendManager {
    /**
     * 创建项目
     *
     * @param saveDTO    项目信息
     * @param operator   请求信息
     * @param operatorId
     * @return 项目信息
     */
    Result<ProjectExtendVO> createProject(ProjectExtendSaveDTO saveDTO, String operator, Integer operatorId);

    /**
     * 获取项目详情，通过项目id
     *
     * @param projectId 项目id
     * @return ProjectVo 项目信息
     */
    Result<ProjectExtendVO> getProjectDetailByProjectId(Integer projectId);

    /**
     * 获取所有项目简要信息
     *
     * @return 项目简要信息list
     */
    Result<List<ProjectBriefExtendVO>> getProjectBriefList();

    /**
     * 删除项目
     *
     * @param projectId 项目id
     * @param operator  请求信息
     * @return
     */
    Result<Void> deleteProjectByProjectId(Integer projectId, String operator);

    /**
     * 根据项目id获取项目简要信息
     *
     * @param projectId 项目id
     * @return 项目简要信息
     */
    Result<ProjectBriefExtendVO> getProjectBriefByProjectId(Integer projectId);

    /**
     * 条件分页查询项目信息
     *
     * @param queryDTO 条件信息
     * @param request
     * @return 项目分页信息
     */
    PagingResult<ProjectExtendVO> getProjectPage(ProjectQueryExtendDTO queryDTO, HttpServletRequest request);

    /**
     * 更新项目信息
     *
     * @param saveDTO 项目信息
     * @param operator 请求信息
     */
    Result<Void> updateProject(ProjectExtendSaveDTO saveDTO, String operator);

    /**
     * 更改项目运行状态，旧状态取反
     *
     * @param projectId 项目id
     * @param operator 请求信息
     */
    Result<Void> changeProjectStatus(Integer projectId, String operator);

    /**
     * 增加项目成员
     *
     * @param projectId 项目id
     * @param userIdList 项目id
     * @param operator 请求信息
     */
    Result<Void> addProjectUser(Integer projectId, List<Integer> userIdList, String operator);

    /**
     * 删除项目成员
     *
     * @param projectId 项目id
     * @param userId 项目id
     * @param operator 请求信息
     */
    Result<Void> delProjectUser(Integer projectId, Integer userId, String operator);

    /**
     * 增加项目负责人
     *
     * @param projectId   项目id
     * @param ownerIdList 负责人id
     * @param operator    请求信息
     */
    Result<Void> addProjectOwner(Integer projectId, List<Integer> ownerIdList, String operator);

    /**
     * 删除项目负责人
     *
     * @param projectId 项目id
     * @param ownerId 负责人id
     * @param operator 请求信息
     */
    Result<Void> delProjectOwner(Integer projectId, Integer ownerId, String operator);

    /**
     * 项目删除前的检查
     *
     * @param projectId 项目id
     * @return ProjectDeleteCheckVO 检查结果
     */
    Result<ProjectDeleteCheckVO> checkBeforeDelete(Integer projectId);

    /**
     * 校验项目是否存在
     * @param projectId
     * @return true:存在，false：不存在
     */
    Result<Void> checkProjectExist(Integer projectId);

    /**
     * 未分配项目的用户列表
     *
     * @param projectId         projectId
     * @param containsAdminRole 是否包含管理员
     * @return {@code Result}
     */
    Result<List<UserBriefVO>> unassignedByProjectId(Integer projectId, Boolean containsAdminRole);

    /**
     * 获取user下绑定的项目
     *
     * @param userId 用户id
     * @return {@code Result<List<ProjectBriefVO>>}
     */
    Result<List<ProjectBriefExtendVO>> getProjectBriefByUserId(Integer userId);

    /**
     * 用户列表按项目id列表
     *
     * @param projectId 项目id
     * @return {@code Result<List<UserBriefVO>>}
     */
    Result<List<UserBriefVO>> listUserListByProjectId(Integer projectId);
    
    
    /**
     * “检查一个项目的资源是否可用。”
     *
     * 函数定义如下：
     *
     * * 该函数返回一个 Result<Void> 对象。
     * * 该函数接受一个参数，一个名为 projectId 的整数对象
     *
     * @param projectId 项目的 ID。
     * @return 一个 Result 对象，里面有一个 Void 对象。
     */
    Result<Void> checkResourcesByProjectId(Integer projectId);
}