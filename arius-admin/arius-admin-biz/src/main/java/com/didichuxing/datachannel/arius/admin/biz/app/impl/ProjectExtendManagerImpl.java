package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import static com.didichuxing.datachannel.arius.admin.core.service.app.impl.ESUserServiceImpl.VERIFY_CODE_LENGTH;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.didichuxing.datachannel.arius.admin.biz.app.ProjectExtendManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectExtendSaveDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectQueryExtendDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectBriefExtendVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectExtendVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.VerifyCodeFactory;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.common.PagingData;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.project.ProjectBriefQueryDTO;
import com.didiglobal.logi.security.common.dto.project.ProjectQueryDTO;
import com.didiglobal.logi.security.common.dto.project.ProjectSaveDTO;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.common.vo.project.ProjectDeleteCheckVO;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.common.vo.user.UserVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.OplogService;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.service.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * logi 项目的扩展能力 将logi中整体能力全部移动到这里
 *
 * @author shizeying
 * @date 2022/06/10
 */
@Component
public class ProjectExtendManagerImpl implements ProjectExtendManager {
    
    @Autowired
    private ProjectConfigService projectConfigService;
    @Autowired
    private ProjectService       projectService;
    @Autowired
    private ESUserService        esUserService;
    @Autowired
    private OplogService         oplogService;
    @Autowired
    private ClusterLogicService  clusterLogicService;
    @Autowired
    private IndexTemplateService indexTemplateService;
    @Autowired
    private UserService          userService;
    
    @Override
    public Result<ProjectExtendVO> createProject(ProjectExtendSaveDTO saveDTO, String operator) {
        try {
            // 1. 创建项目
            ProjectSaveDTO project = saveDTO.getProject();
            // 2. 创建项目配置
            ProjectConfigDTO config = saveDTO.getConfig();
            // 3. 创建项目
            ProjectVO projectVO = projectService.createProject(project, operator);
            // 4. 转换
            ProjectExtendVO projectExtendVO = ConvertUtil.obj2Obj(project, ProjectExtendVO.class);
            //5. 设置项目id
            config.setProjectId(projectVO.getId());
            // 6. 创建项目配置
            Tuple<Result<Void>, ProjectConfigPO> resultProjectConfigPOTuple = projectConfigService.updateOrInitProjectConfig(
                    config, operator);
            //todo 需要设置创建成功的日志
            if (resultProjectConfigPOTuple.getV1().success()) {
                if (AuthConstant.SUPER_PROJECT_ID.equals(projectExtendVO.getId())) {
                    projectExtendVO.setIsAdmin(true);
                } else {
                    projectExtendVO.setIsAdmin(false);
                }
                projectExtendVO.setConfig(
                        ConvertUtil.obj2Obj(resultProjectConfigPOTuple.getV2(), ProjectConfigVO.class));
                
            }
            
            //创建es user
            createESUserDefault(projectVO, operator);
            return Result.<ProjectExtendVO>buildSucc(projectExtendVO);
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
    
    @Override
    public Result<ProjectExtendVO> getProjectDetailByProjectId(Integer projectId) {
        try {
            ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);
            ProjectExtendVO projectExtendVO = ConvertUtil.obj2Obj(projectVO, ProjectExtendVO.class);
            if (AuthConstant.SUPER_PROJECT_ID.equals(projectExtendVO.getId())) {
                projectExtendVO.setIsAdmin(true);
            } else {
                projectExtendVO.setIsAdmin(false);
            }
            ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectId);
            projectExtendVO.setConfig(ConvertUtil.obj2Obj(projectConfig, ProjectConfigVO.class));
            
            return Result.<ProjectExtendVO>buildSucc(projectExtendVO);
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
    
    @Override
    public Result<List<ProjectBriefExtendVO>> getProjectBriefList() {
        List<ProjectBriefVO> projectBriefList = projectService.getProjectBriefList();
        List<ProjectBriefExtendVO> projectBriefExtendList = ConvertUtil.list2List(projectBriefList,
                ProjectBriefExtendVO.class);
        return getListResult(projectBriefExtendList);
    }
    
    @Override
    public Result<Void> deleteProjectByProjectId(Integer projectId, String operator) {
        //项目删除前的检查
        //校验项目是否绑定es user
        List<ESUser> esUsers = esUserService.listESUsers(Collections.singletonList(projectId));
        if (CollectionUtils.isNotEmpty(esUsers)) {
            return Result.buildFail("项目已绑定es user，不能删除");
        }
        //校验项目绑定逻辑集群
        List<ClusterLogic> clusterLogics = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
        if (CollectionUtils.isNotEmpty(clusterLogics)) {
            return Result.buildFail("项目已绑定逻辑集群，不能删除");
        }
        
        //校验项目绑定模板服务
        List<IndexTemplate> indexTemplates = indexTemplateService.getProjectLogicTemplatesByProjectId(projectId);
        if (CollectionUtils.isNotEmpty(indexTemplates)) {
            return Result.buildFail("项目已绑定模板服务，不能删除");
        }
        
        projectService.deleteProjectByProjectId(projectId, operator);
        return Result.buildSucc();
    }
    
    /**
     * 根据项目id获取项目简要信息
     *
     * @param projectId 项目id
     * @return 项目简要信息
     */
    @Override
    public Result<ProjectBriefExtendVO> getProjectBriefByProjectId(Integer projectId) {
        final ProjectBriefVO projectBriefVO = projectService.getProjectBriefByProjectId(projectId);
        final ProjectBriefExtendVO projectBriefExtendVO = ConvertUtil.obj2Obj(projectBriefVO,
                ProjectBriefExtendVO.class);
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectBriefExtendVO.getId())) {
            projectBriefExtendVO.setIsAdmin(true);
        } else {
            projectBriefExtendVO.setIsAdmin(false);
        }
        ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectBriefExtendVO.getId());
        projectBriefExtendVO.setConfig(ConvertUtil.obj2Obj(projectConfig, ProjectConfigVO.class));
        return Result.buildSucc(projectBriefExtendVO);
    }
    
    /**
     * 条件分页查询项目信息
     *
     * @param queryDTO 条件信息
     * @return 项目分页信息
     */
    @Override
    public PagingResult<ProjectVO> getProjectPage(ProjectQueryExtendDTO queryDTO) {
        final ProjectQueryDTO projectQueryDTO = ConvertUtil.obj2Obj(queryDTO, ProjectQueryDTO.class);
    
        if (Objects.isNull(queryDTO.getSearchType())) {
            final PagingData<ProjectVO> projectPage = projectService.getProjectPage(projectQueryDTO);
            return PagingResult.success(projectPage);
        } else {
            final List<Integer> projectIds = esUserService.getProjectIdBySearchType(queryDTO.getSearchType());
            final PagingData<ProjectVO> projectPage = projectService.getProjectPage(projectQueryDTO, projectIds);
            return PagingResult.success(projectPage);
        
        }
    }
    
    /**
     * 更新项目信息
     *
     * @param saveDTO  项目信息
     * @param operator 请求信息
     * @throws LogiSecurityException 项目相关的错误信息
     */
    @Override
    public Result<Void> updateProject(ProjectExtendSaveDTO saveDTO, String operator) {
        try {
            final ProjectConfigDTO config = saveDTO.getConfig();
            if (Objects.nonNull(config) && ObjectUtils.isNotEmpty(config)) {
                final Integer id = saveDTO.getProject().getId();
                config.setProjectId(id);
                //只有success时候会存在tuple._2不为null
                final Tuple<Result<Void>, ProjectConfigPO> tuple = projectConfigService.updateOrInitProjectConfig(
                        config, operator);
                //todo 需要设置创建成功的日志
                if (tuple.getV1().success()) {
            
                }
            }
            final ProjectSaveDTO project = saveDTO.getProject();
            projectService.updateProject(project, operator);
            return Result.buildSucc();
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
    
    /**
     * 更改项目运行状态，旧状态取反
     *
     * @param projectId 项目id
     * @param operator  请求信息
     */
    @Override
    public Result<Void> changeProjectStatus(Integer projectId, String operator) {
        try {
            projectService.changeProjectStatus(projectId, operator);
        return Result.buildSucc();
        }catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
    
    /**
     * 增加项目成员
     *
     * @param projectId 项目id
     * @param userIdList    项目id
     * @param operator  请求信息
     */
    @Override
    public Result<Void> addProjectUser(Integer projectId, List<Integer> userIdList, String operator) {
       
       
       
       final Result<Void> result = checkProject(projectId, userIdList);
        if (result.failed()) {
            return result;
        }
        //校验成员
        final ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);
        //校验当前userList是否已经拥有该项目
        final List<Integer> haveUserIdList = Optional.ofNullable(projectVO.getUserList())
                .orElse(Collections.emptyList()).stream().map(UserBriefVO::getId)
                //过滤出已经拥有该项目的user
                .filter(userIdList::contains).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(haveUserIdList)) {
            final String havaUseridListStr = haveUserIdList.stream().map(String::valueOf)
                    .collect(Collectors.joining("，"));
            return Result.buildParamIllegal(String.format("以下用户已经是该项目的成员:%s", havaUseridListStr));
        }
        try {
            for (Integer id : userIdList) {
                projectService.addProjectUser(projectId, id, operator);
            }
            return Result.buildSucc();
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
       
    }
    
    /**
     * 删除项目成员
     *
     * @param projectId 项目id
     * @param userId    项目id
     * @param operator  请求信息
     */
    @Override
    public Result<Void> delProjectUser(Integer projectId, Integer userId, String operator) {
        try {
            projectService.delProjectUser(projectId, userId, operator);
            return Result.buildSucc();
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
    
    /**
     * 增加项目负责人
     *
     * @param projectId   项目id
     * @param ownerIdList 负责人id
     * @param operator    请求信息
     */
    @Override
    public Result<Void> addProjectOwner(Integer projectId, List<Integer> ownerIdList, String operator) {
        final Result<Void> result = checkProject(projectId, ownerIdList);
        if (result.failed()) {
            return result;
        }
         //校验成员
        final ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);
        //校验当前userList是否已经拥有该项目
        final List<Integer> haveUserIdList = Optional.ofNullable(projectVO.getOwnerList())
                .orElse(Collections.emptyList()).stream().map(UserBriefVO::getId)
                //过滤出已经拥有该项目的user
                .filter(ownerIdList::contains).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(haveUserIdList)) {
            final String havaUseridListStr = haveUserIdList.stream().map(String::valueOf)
                    .collect(Collectors.joining("，"));
            return Result.buildParamIllegal(String.format("以下用户已经是该项目的拥有者:%s", havaUseridListStr));
        }
        try {
            for (Integer ownerId : ownerIdList) {
                projectService.addProjectOwner(projectId, ownerId, operator);
        
            }
            return Result.buildSucc();
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
    
    /**
     * 删除项目负责人
     *
     * @param projectId 项目id
     * @param ownerId   负责人id
     * @param operator  请求信息
     */
    @Override
    public Result<Void> delProjectOwner(Integer projectId, Integer ownerId, String operator) {
        try {
            projectService.delProjectOwner(projectId, ownerId, operator);
            return Result.buildSucc();
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
    
    /**
     * 项目删除前的检查
     *
     * @param projectId 项目id
     * @return ProjectDeleteCheckVO 检查结果
     */
    @Override
    public Result<ProjectDeleteCheckVO> checkBeforeDelete(Integer projectId) {
        return Result.buildSucc(projectService.checkBeforeDelete(projectId));
    }
    
    /**
     * 分页查询项目简要信息
     *
     * @param queryDTO 查询条件
     * @return 简要信息List
     */
    @Override
    public PagingData<ProjectBriefVO> getProjectBriefPage(ProjectBriefQueryDTO queryDTO) {
        return projectService.getProjectBriefPage(queryDTO);
    }
    
    /**
     * 校验项目是否存在
     *
     * @param projectId
     * @return true:存在，false：不存在
     */
    @Override
    public Result<Void> checkProjectExist(Integer projectId) {
        if (projectService.checkProjectExist(projectId)) {
            return Result.buildSucc();
        } else {
            return Result.buildFail("项目不存在");
        }
    }
    
    /**
     * 未分配项目的用户列表
     *
     * @param projectId projectId
     * @return {@code Result}
     */
    @Override
    public Result<List<UserBriefVO>> unassignedByProjectId(Integer projectId) {
        final com.didiglobal.logi.security.common.Result<List<UserBriefVO>> listResult = projectService.unassignedByProjectId(
                projectId);
        if (listResult.successed()) {
            return Result.buildSucc(listResult.getData());
        } else {
            return Result.buildFail(listResult.getMessage());
        }
    
    }
    
    /**
     * 获取user下绑定的项目
     *
     * @param userId 用户id
     * @return {@code Result<List<ProjectBriefVO>>}
     */
    @Override
    public Result<List<ProjectBriefExtendVO>> getProjectBriefByUserId(Integer userId) {
        final com.didiglobal.logi.security.common.Result<List<ProjectBriefVO>> listResult = projectService.getProjectBriefByUserId(
                userId);
        if (listResult.successed()) {
            final List<ProjectBriefExtendVO> dtoList = ConvertUtil.list2List(listResult.getData(),
                    ProjectBriefExtendVO.class);
            return getListResult(dtoList);
        } else {
            return Result.buildFail(listResult.getMessage());
        }
    }
    
    @NotNull
    private Result<List<ProjectBriefExtendVO>> getListResult(List<ProjectBriefExtendVO> projectBriefExtendList) {
        for (ProjectBriefExtendVO projectBriefExtendVO : projectBriefExtendList) {
            if (AuthConstant.SUPER_PROJECT_ID.equals(projectBriefExtendVO.getId())) {
                projectBriefExtendVO.setIsAdmin(true);
            } else {
                projectBriefExtendVO.setIsAdmin(false);
            }
            ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectBriefExtendVO.getId());
            projectBriefExtendVO.setConfig(ConvertUtil.obj2Obj(projectConfig, ProjectConfigVO.class));
        }
        return Result.buildSucc(projectBriefExtendList);
    }
    
    /**
     * 新增默认的es user
     *
     * @param data 数据
     */
    private void createESUserDefault(ProjectVO data, String operator) {
        ESUserDTO esUserDTO = new ESUserDTO();
        esUserDTO.setIsRoot(0);
        esUserDTO.setSearchType(AppSearchTypeEnum.TEMPLATE.getCode());
        esUserDTO.setVerifyCode(VerifyCodeFactory.get(VERIFY_CODE_LENGTH));
        esUserDTO.setMemo((data).getProjectName() + "项目默认的es user");
        esUserDTO.setProjectId((data).getId());
        esUserDTO.setMemo("创建项目es user");
        final Tuple<Result, ESUserPO> result = esUserService.registerESUser(esUserDTO, operator);
        if (result.getV1().success()){
            //oplogService.saveOplog(new OplogDTO(operator, NewModuleEnum.APPLICATION.getOperationType(),
            //        NewModuleEnum.getOperatingContent(NewModuleEnum.APPLICATION, AppSearchTypeEnum.TEMPLATE.getDesc(),
            //                result.getV2().getId() + ""),
            //        NewModuleEnum.APPLICATION.getModule(), result.getV2().getId().toString(),
            //        OperationMethodEnum.SYSTEM_TRIGGER.getOperationMethod()));
        }
    }
    
    private Result<Void> checkProject(Integer projectId, List<Integer> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Result.buildParamIllegal("用户id不存在");
        }
        if (Objects.isNull(projectId)) {
            return Result.buildParamIllegal("项目id不存在");
        }
        if (!projectService.checkProjectExist(projectId)){
            return Result.buildParamIllegal(String.format("项目%s不存在", projectId));
        }
        //校验当前用户id是否存在
        final List<UserVO> userList = userIdList.stream()
                .map(userService::getUserDetailByUserId)
                .collect(Collectors.toList());
        if (userList.size() != userIdList.size()) {
            final List<Integer> idList = userList.stream().map(UserVO::getId).collect(Collectors.toList());
            //不存在用户id集合
            String notExitsIds = userIdList.stream().filter(id -> !idList.contains(id)).distinct().map(String::valueOf)
                    .collect(Collectors.joining("，"));
            return Result.buildParamIllegal(String.format("传入用户:%s不存在", notExitsIds));
        }
        
        
        return Result.buildSucc();
    }
}