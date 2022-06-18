package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import static com.didichuxing.datachannel.arius.admin.core.service.app.impl.ESUserServiceImpl.VERIFY_CODE_LENGTH;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.didichuxing.datachannel.arius.admin.biz.app.ProjectExtendManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
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
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.VerifyCodeFactory;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
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
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.service.UserProjectService;
import com.didiglobal.logi.security.service.UserService;
import com.google.common.collect.Lists;
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
    private OperateRecordService operateRecordService;
    @Autowired
    private ClusterLogicService  clusterLogicService;
    @Autowired
    private IndexTemplateService indexTemplateService;
    @Autowired
    private UserService        userService;
    @Autowired
    private UserProjectService userProjectService;
    
    @Override
    public Result<ProjectExtendVO> createProject(ProjectExtendSaveDTO saveDTO, String operator) {
        try {
            // 1. 创建项目
            ProjectSaveDTO project = saveDTO.getProject();
            // 2. 创建项目配置
            ProjectConfigDTO config = saveDTO.getConfig();
            List<Integer> ownerIdList = project.getOwnerIdList();
            List<Integer> userIdList = project.getUserIdList();
            project.setOwnerIdList(Collections.emptyList());
            project.setUserIdList(Collections.emptyList());
    
            // 3. 创建项目
            ProjectVO projectVO = projectService.createProject(project, operator);
            // 4. 转换
            ProjectExtendVO projectExtendVO = ConvertUtil.obj2Obj(project, ProjectExtendVO.class);
            if (CollectionUtils.isNotEmpty(ownerIdList)) {
                addProjectOwner(projectVO.getId(), ownerIdList, operator);
                Optional.ofNullable(userService.getUserBriefListByUserIdList(ownerIdList))
                        .ifPresent(projectExtendVO::setOwnerList);
            }
            if (CollectionUtils.isNotEmpty(userIdList)) {
                addProjectUser(projectVO.getId(), userIdList, operator);
                Optional.ofNullable(userService.getUserBriefListByUserIdList(userIdList))
                        .ifPresent(projectExtendVO::setUserList);
            }
           
            //5. 设置项目id
            config.setProjectId(projectVO.getId());
            // 6. 创建项目配置
            Tuple<Result<Void>, ProjectConfigPO> resultProjectConfigTuple = projectConfigService.updateOrInitProjectConfig(
                    config, operator);
            
            if (resultProjectConfigTuple.getV1().success()) {
                if (AuthConstant.SUPER_PROJECT_ID.equals(projectExtendVO.getId())) {
                    projectExtendVO.setIsAdmin(true);
                }
                projectExtendVO.setConfig(
                        ConvertUtil.obj2Obj(resultProjectConfigTuple.getV2(), ProjectConfigVO.class));
                
            }
            //7. 写入操作日志
            operateRecordService.save(new OperateRecord(project.getProjectName(), OperateTypeEnum.APPLICATION_CREATE
                    , TriggerWayEnum.MANUAL_TRIGGER,project.getProjectName(),operator));
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
            }
            ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectId);
            projectExtendVO.setConfig(ConvertUtil.obj2Obj(projectConfig, ProjectConfigVO.class));
            
            return Result.buildSucc(projectExtendVO);
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
       
        //校验项目绑定逻辑集群
        List<ClusterLogic> clusterLogics = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
        if (CollectionUtils.isNotEmpty(clusterLogics)) {
            return Result.buildFail("项目已绑定逻辑集群，不能删除");
        }
        
        //校验项目绑定模板服务
        List<IndexTemplate> indexTemplates = indexTemplateService.listProjectLogicTemplatesByProjectId(projectId);
        if (CollectionUtils.isNotEmpty(indexTemplates)) {
            return Result.buildFail("项目已绑定模板服务，不能删除");
        }
        ProjectBriefVO projectBriefVO = projectService.getProjectBriefByProjectId(projectId);
        projectService.deleteProjectByProjectId(projectId, operator);
        //获取全部项目配置
        List<ESUser> esUsers = esUserService.listESUsers(Collections.singletonList(projectId));
        
        //删除es user
        esUserService.deleteByESUsers(projectId);
        for (ESUser esUser : esUsers) {
            operateRecordService.save(
                    new OperateRecord(projectBriefVO.getProjectName(), OperateTypeEnum.APPLICATION_ACCESS_MODE,
                            TriggerWayEnum.MANUAL_TRIGGER,
                            String.format("删除访问模式:%s", AppSearchTypeEnum.valueOf(esUser.getSearchType()).getDesc()),
                            operator));
        }
        operateRecordService.save(
                new OperateRecord(projectBriefVO.getProjectName(), OperateTypeEnum.APPLICATION_DELETE,
                        TriggerWayEnum.MANUAL_TRIGGER, projectBriefVO.getProjectName(), operator));
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
    public PagingResult<ProjectExtendVO> getProjectPage(ProjectQueryExtendDTO queryDTO) {
        final ProjectQueryDTO projectQueryDTO = ConvertUtil.obj2Obj(queryDTO, ProjectQueryDTO.class);
    
        if (Objects.isNull(queryDTO.getSearchType())) {
            final PagingData<ProjectVO> projectPage = projectService.getProjectPage(projectQueryDTO);
            final List<ProjectVO> bizData = projectPage.getBizData();
            final List<ProjectExtendVO> projectExtendVOList = ConvertUtil.list2List(bizData, ProjectExtendVO.class);
            for (ProjectExtendVO projectExtendVO : projectExtendVOList) {
                if (AuthConstant.SUPER_PROJECT_ID.equals(projectExtendVO.getId())) {
                    projectExtendVO.setIsAdmin(true);
                }
            }
    
            return PagingResult.success(new PagingData<>(projectExtendVOList, projectPage.getPagination()));
        } else {
            final List<Integer> projectIds = esUserService.getProjectIdBySearchType(queryDTO.getSearchType());
            final PagingData<ProjectVO> projectPage = projectService.getProjectPage(projectQueryDTO, projectIds);
            final List<ProjectExtendVO> projectExtendVOList = ConvertUtil.list2List(projectPage.getBizData(),
                    ProjectExtendVO.class);
            for (ProjectExtendVO projectExtendVO : projectExtendVOList) {
                if (AuthConstant.SUPER_PROJECT_ID.equals(projectExtendVO.getId())) {
                    projectExtendVO.setIsAdmin(true);
                }
                final ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectExtendVO.getId());
                projectExtendVO.setConfig(ConvertUtil.obj2Obj(projectConfig,ProjectConfigVO.class));
            }
            return PagingResult.success(new PagingData<>(projectExtendVOList, projectPage.getPagination()));
        
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
                projectConfigService.updateOrInitProjectConfig(config, operator);
                
            }
            final ProjectSaveDTO project = saveDTO.getProject();
            List<Integer> ownerIdList = project.getOwnerIdList();
            List<Integer> userIdList = project.getUserIdList();
            project.setOwnerIdList(Collections.emptyList());
            project.setUserIdList(Collections.emptyList());
            ProjectVO oldProject = projectService.getProjectDetailByProjectId(project.getId());
            projectService.updateProject(project, operator);
            //更新项目与用户拥有者的关联关系
            if (CollectionUtils.isNotEmpty(ownerIdList)) {
                List<UserBriefVO> oldUserBriefList = Lists.newArrayList();
                //操作前的
                Optional.ofNullable(oldProject.getOwnerList()).ifPresent(oldUserBriefList::addAll);
                //更新
                userProjectService.updateOwnerProject(project.getId(), ownerIdList);
                //操作后
                List<UserBriefVO> newUserBriefList = Lists.newArrayList();
                Optional.ofNullable(projectService.getProjectDetailByProjectId(project.getId()))
                        .map(ProjectVO::getOwnerList).ifPresent(newUserBriefList::addAll);
                //旧的用户列表
                String oldUser = oldUserBriefList.stream().map(UserBriefVO::getUserName).distinct()
                        .collect(Collectors.joining(","));
                //新的用户列表
                String newUser = newUserBriefList.stream().map(UserBriefVO::getUserName).distinct()
                        .collect(Collectors.joining(","));
                operateRecordService.save(
                        new OperateRecord(oldProject.getProjectName(), OperateTypeEnum.APPLICATION_OWNER_CHANGE,
                                TriggerWayEnum.MANUAL_TRIGGER, oldUser + "-->" + newUser, operator));
            }
            //更新项目与用户成员的关联关系
            if (CollectionUtils.isNotEmpty(userIdList)) {
                List<UserBriefVO> oldUserBriefList = Lists.newArrayList();
                //操作前的
                Optional.ofNullable(oldProject.getUserList()).ifPresent(oldUserBriefList::addAll);
                //更新
                userProjectService.updateUserProject(project.getId(), userIdList);
                //操作后
                List<UserBriefVO> newUserBriefList = Lists.newArrayList();
                Optional.ofNullable(projectService.getProjectDetailByProjectId(project.getId()))
                        .map(ProjectVO::getUserList).ifPresent(newUserBriefList::addAll);
                //旧的用户列表
                String oldUser = oldUserBriefList.stream().map(UserBriefVO::getUserName).distinct()
                        .collect(Collectors.joining(","));
                //新的用户列表
                String newUser = newUserBriefList.stream().map(UserBriefVO::getUserName).distinct()
                        .collect(Collectors.joining(","));
                operateRecordService.save(
                        new OperateRecord(oldProject.getProjectName(), OperateTypeEnum.APPLICATION_USER_CHANGE,
                                TriggerWayEnum.MANUAL_TRIGGER, oldUser + "-->" + newUser, operator));
        
            }
          
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
    
        try {
            //更新前
       
            //获取旧的项目的owner和user
            List<UserBriefVO> oldUserBriefList = Lists.newArrayList();
            Optional.ofNullable(projectService.getProjectDetailByProjectId(projectId)).map(ProjectVO::getUserList)
                    .ifPresent(oldUserBriefList::addAll);
            userProjectService.saveUserProject(projectId, userIdList);

            //更新后
            List<UserBriefVO> newUserBriefList = Lists.newArrayList();
            ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);
            Optional.ofNullable(projectVO).map(ProjectVO::getUserList)
                    .ifPresent(newUserBriefList::addAll);
            String newUserName = newUserBriefList.stream().map(UserBriefVO::getUserName)
                    .collect(Collectors.joining(","));
            String oldUser = oldUserBriefList.stream().map(UserBriefVO::getUserName).collect(Collectors.joining(","));
            operateRecordService.save(
                    new OperateRecord(projectVO.getProjectName(), OperateTypeEnum.APPLICATION_OWNER_CHANGE,
                            TriggerWayEnum.MANUAL_TRIGGER, oldUser + "-->" + newUserName, operator));
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
            //更新前
            ProjectVO oldProject = projectService.getProjectDetailByProjectId(projectId);
            List<UserBriefVO> oldUserBriefList = Lists.newArrayList();
            Optional.ofNullable(oldProject.getUserList()).ifPresent(oldUserBriefList::addAll);
            //更新
            projectService.delProjectUser(projectId, userId, operator);
            //更新后
            ProjectVO newProject = projectService.getProjectDetailByProjectId(projectId);
            List<UserBriefVO> newUserBriefList = Lists.newArrayList();
            Optional.ofNullable(newProject.getUserList()).ifPresent(newUserBriefList::addAll);
            String oldUser = oldUserBriefList.stream().map(UserBriefVO::getUserName).collect(Collectors.joining(","));
            String newUser = newUserBriefList.stream().map(UserBriefVO::getUserName).collect(Collectors.joining(","));
            operateRecordService.save(
                    new OperateRecord(oldProject.getProjectName(), OperateTypeEnum.APPLICATION_USER_CHANGE,
                            TriggerWayEnum.MANUAL_TRIGGER, oldUser + "-->" + newUser, operator));
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
        
        try {
            ProjectVO oldProject = projectService.getProjectDetailByProjectId(projectId);
            //获取旧的项目的owner和user
            List<UserBriefVO> oldUserBriefList = Lists.newArrayList();
            Optional.ofNullable(oldProject.getOwnerList()).ifPresent(oldUserBriefList::addAll);
            // 更新
            userProjectService.saveOwnerProject(projectId, ownerIdList);
            List<UserBriefVO> newUserBriefList = Lists.newArrayList();
            //更新后的列表
            Optional.ofNullable(projectService.getProjectDetailByProjectId(projectId)).map(ProjectVO::getOwnerList)
                    .ifPresent(newUserBriefList::addAll);
            String newUserName = newUserBriefList.stream().map(UserBriefVO::getUserName)
                    .collect(Collectors.joining(","));
            String oldUser = oldUserBriefList.stream().map(UserBriefVO::getUserName).collect(Collectors.joining(","));
           
            operateRecordService.save(
                    new OperateRecord(oldProject.getProjectName(), OperateTypeEnum.APPLICATION_OWNER_CHANGE,
                            TriggerWayEnum.MANUAL_TRIGGER, oldUser + "-->" + newUserName, operator));
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
            ProjectVO oldProject = projectService.getProjectDetailByProjectId(projectId);
            List<UserBriefVO> oldUserBriefList = Lists.newArrayList();
            Optional.ofNullable(oldProject.getUserList()).ifPresent(oldUserBriefList::addAll);
            projectService.delProjectOwner(projectId, ownerId, operator);
            ProjectVO newProject = projectService.getProjectDetailByProjectId(projectId);
            List<UserBriefVO> newUserBriefList = Lists.newArrayList();
            Optional.ofNullable(newProject.getUserList()).ifPresent(newUserBriefList::addAll);
            String oldUser = oldUserBriefList.stream().map(UserBriefVO::getUserName).collect(Collectors.joining(","));
            String newUser = newUserBriefList.stream().map(UserBriefVO::getUserName).collect(Collectors.joining(","));
            operateRecordService.save(
                    new OperateRecord(oldProject.getProjectName(), OperateTypeEnum.APPLICATION_OWNER_CHANGE,
                            TriggerWayEnum.MANUAL_TRIGGER, oldUser + "-->" + newUser, operator));
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
            operateRecordService.save(
                    new OperateRecord(data.getProjectName(), OperateTypeEnum.APPLICATION_ACCESS_MODE,
                            TriggerWayEnum.MANUAL_TRIGGER,
                            String.format("新增访问模式:%s", AppSearchTypeEnum.TEMPLATE.getDesc()), operator,result.getV2()
                            .getId()));

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