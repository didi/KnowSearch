package com.didichuxing.datachannel.arius.admin.biz.project.impl;

import static com.didichuxing.datachannel.arius.admin.core.service.project.impl.ESUserServiceImpl.VERIFY_CODE_LENGTH;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.didichuxing.datachannel.arius.admin.biz.project.ProjectExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectExtendSaveDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectQueryExtendDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectBriefExtendVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectExtendVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.VerifyCodeFactory;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.common.PagingData;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.project.ProjectBriefQueryDTO;
import com.didiglobal.logi.security.common.dto.project.ProjectQueryDTO;
import com.didiglobal.logi.security.common.dto.project.ProjectSaveDTO;
import com.didiglobal.logi.security.common.enums.project.ProjectUserCode;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.common.vo.project.ProjectDeleteCheckVO;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.common.vo.role.RoleBriefVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.common.vo.user.UserVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.service.RoleService;
import com.didiglobal.logi.security.service.UserProjectService;
import com.didiglobal.logi.security.service.UserService;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
    private UserService          userService;
    @Autowired
    private UserProjectService   userProjectService;
    @Autowired
    private RoleService          roleService;
    
    @Override
    public Result<ProjectExtendVO> createProject(ProjectExtendSaveDTO saveDTO, String operator) {
        try {
            // 1. 创建项目
            ProjectSaveDTO project = saveDTO.getProject();
            // 2. 创建项目配置
            ProjectConfigDTO config = saveDTO.getConfig();
            // 将项目中的所有者、用户提取出来后，使用biz层中的逻辑进行添加
            List<Integer> ownerIdList = project.getOwnerIdList();
            List<Integer> userIdList = project.getUserIdList();
            project.setOwnerIdList(Collections.emptyList());
            project.setUserIdList(Collections.emptyList());
            
            // 3. 创建项目
            ProjectVO projectVO = projectService.createProject(project, operator);
            // 4. 转换
            ProjectExtendVO projectExtendVO = ConvertUtil.obj2Obj(projectVO, ProjectExtendVO.class);
            // 5. 添加拥有者和成员
            addOwnerAndUsers(operator, ownerIdList, userIdList, projectVO, projectExtendVO);
            
            //5. 设置项目id
            config.setProjectId(projectVO.getId());
            // 6. 创建项目配置
            TupleTwo<Result<Void>, ProjectConfigPO> resultProjectConfigTuple = projectConfigService.updateOrInitProjectConfig(
                    config, operator);
            setAdminProjectExtendVO(projectExtendVO);
            // 设置项目配置
            if (resultProjectConfigTuple.v1().success()) {
                projectExtendVO.setConfig(ConvertUtil.obj2Obj(resultProjectConfigTuple.v2(), ProjectConfigVO.class));
                
            }
            //7. 写入操作日志
            operateRecordService.save(new OperateRecord(project.getProjectName(), OperateTypeEnum.APPLICATION_CREATE,
                    TriggerWayEnum.MANUAL_TRIGGER, project.getProjectName(), operator));
            //创建es user
            createESUserDefault(projectVO, operator);
            if (Objects.isNull(project.getId())){
                projectExtendVO.setId(projectExtendVO.getConfig().getProjectId());
            }
            return Result.<ProjectExtendVO>buildSucc(projectExtendVO);
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
    
    private void addOwnerAndUsers(String operator, List<Integer> ownerIdList, List<Integer> userIdList,
                               ProjectVO projectVO,
                           ProjectExtendVO projectExtendVO) {
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
    }
    
    @Override
    public Result<ProjectExtendVO> getProjectDetailByProjectId(Integer projectId) {
        try {
            ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);
            ProjectExtendVO projectExtendVO = ConvertUtil.obj2Obj(projectVO, ProjectExtendVO.class);
            setAdminProjectExtendVO(projectExtendVO);
            ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectId);
            projectExtendVO.setConfig(ConvertUtil.obj2Obj(projectConfig, ProjectConfigVO.class));
            
            return Result.buildSucc(projectExtendVO);
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
    
    /**
     * 设置超级项目
     *
     * @param projectExtendVO 项目延长签证官
     */
    private void setAdminProjectExtendVO(ProjectExtendVO projectExtendVO) {
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectExtendVO.getId())) {
            projectExtendVO.setIsAdmin(true);
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
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildFail("系统内置项目，不能删除");
        }
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
                            String.format("删除访问模式:%s", ProjectSearchTypeEnum.valueOf(esUser.getSearchType()).getDesc()),
                            operator));
        }
        operateRecordService.save(new OperateRecord(projectBriefVO.getProjectName(), OperateTypeEnum.APPLICATION_DELETE,
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
            projectExtendVOList.forEach(this::setAdminProjectExtendVO);
            
            return PagingResult.success(new PagingData<>(projectExtendVOList, projectPage.getPagination()));
        } else {
            final List<Integer> projectIds = esUserService.getProjectIdBySearchType(queryDTO.getSearchType());
            final PagingData<ProjectVO> projectPage = projectService.getProjectPage(projectQueryDTO, projectIds);
            final List<ProjectExtendVO> projectExtendVOList = ConvertUtil.list2List(projectPage.getBizData(),
                    ProjectExtendVO.class);
            for (ProjectExtendVO projectExtendVO : projectExtendVOList) {
                setAdminProjectExtendVO(projectExtendVO);
                final ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectExtendVO.getId());
                projectExtendVO.setConfig(ConvertUtil.obj2Obj(projectConfig, ProjectConfigVO.class));
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
            
            projectService.updateProject(project, operator);
            //更新项目与用户拥有者的关联关系
            final Result<Void> operationProjectOwnerResult = operationProjectMemberOrOwner(operator, saveDTO.getProject().getId(),
                    ownerIdList,
                    (projectId, userList) -> userProjectService.updateOwnerProject(projectId, userList),
                    ProjectVO::getOwnerList, OperateTypeEnum.APPLICATION_OWNER_CHANGE,
                    Boolean.FALSE
    
            );
            if (operationProjectOwnerResult.failed()){
                return operationProjectOwnerResult;
            }
            //更新项目与用户成员的关联关系
            final Result<Void> operationProjectUserResult = operationProjectMemberOrOwner(operator,
                    saveDTO.getProject().getId(),
                    userIdList, (projectId, userList) -> userProjectService.updateUserProject(projectId, userList),
                    ProjectVO::getUserList, OperateTypeEnum.APPLICATION_USER_CHANGE, Boolean.FALSE
    
            );
            if (operationProjectUserResult.failed()) {
                return operationProjectUserResult;
            }
            return Result.buildSucc();
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }
    
    /**
     * 操作项目成员/项目拥有者
     *
     * @param operator        操作人或角色
     * @param projectId         操作前的项目id
     * @param userOrOwnerList 添加/更新用户id列表
     * @param operationConsumerFunc    更新/添加操作
     * @param  operationFuncMapper      函数映射器 需要获取的mapper userList/ownerList
     * @param operateTypeEnum 操作类型枚举
     * @param isDeleteOp       是否是删除操作
     * @return {@code Result<Void>}
     */
    private Result<Void> operationProjectMemberOrOwner(String operator, Integer projectId,
                                                       List<Integer> userOrOwnerList,
                                                       BiConsumer<Integer, List<Integer>> operationConsumerFunc,
                                                       Function<ProjectVO, List<UserBriefVO>>  operationFuncMapper,
                                                       OperateTypeEnum operateTypeEnum, Boolean isDeleteOp ) {
        if (CollectionUtils.isNotEmpty(userOrOwnerList)) {
            if (Boolean.FALSE.equals(isDeleteOp)) {
                //超级项目侧校验添加的用户收否存在管理员角色
                final Result<Void> result = checkProject(projectId, userOrOwnerList);
                if (result.failed()) {
                    return result;
                }
            }
            //操作前的项目信息
            final ProjectVO beforeProjectVo = projectService.getProjectDetailByProjectId(projectId);
            final String projectName = beforeProjectVo.getProjectName();
            List<UserBriefVO> beforeProjectUserList = Lists.newArrayList();
            //操作前的
            Optional.ofNullable(beforeProjectVo).map(operationFuncMapper).ifPresent(beforeProjectUserList::addAll);
            //更新/add
            operationConsumerFunc.accept(projectId, userOrOwnerList);
            //操作后
            List<UserBriefVO> afterProjectUserList = Lists.newArrayList();
            Optional.ofNullable(projectService.getProjectDetailByProjectId(projectId)).map(operationFuncMapper)
                    .ifPresent(afterProjectUserList::addAll);
            TupleTwo</*beforeUserStr*/String,/*afterUserStr*/String> tuple2 = projectOwnerOrMemberChangeStr(
                    beforeProjectUserList, afterProjectUserList);
            recordProjectOwnerOrUserChange(tuple2, projectName, operator, operateTypeEnum);
       }
        return Result.buildSucc();
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
     * @param projectId  项目id
     * @param userIdList 项目id
     * @param operator   请求信息
     */
    @Override
    public Result<Void> addProjectUser(Integer projectId, List<Integer> userIdList, String operator) {
        try {
            final Result<Void> operationProjectUserResult = operationProjectMemberOrOwner(operator,
                    projectId, userIdList,
                    (id, userList) -> userProjectService.saveUserProject(id, userList), ProjectVO::getUserList,
                    OperateTypeEnum.APPLICATION_USER_CHANGE,Boolean.FALSE
            );
            if (operationProjectUserResult.failed()){
                return operationProjectUserResult;
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
            //操作前
            Consumer<TupleTwo</*projectId*/Integer,/*userId*/Integer>> delProjectUserConsumer = tupleTwo -> projectService.delProjectUser(
                    tupleTwo.v1, tupleTwo.v2, operator);
            final Result<Void> operationProjectOwnerResult = operationProjectMemberOrOwner(operator, projectId,
                    Collections.singletonList(userId),
                    (id, userList) -> userList.stream().map(user -> Tuples.of(id, user))
                            .forEach(delProjectUserConsumer),
                    ProjectVO::getOwnerList, OperateTypeEnum.APPLICATION_USER_CHANGE, Boolean.TRUE
    
            );
            if (operationProjectOwnerResult.failed()) {
                return operationProjectOwnerResult;
            }
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
        try {
            final Result<Void> operationProjectOwnerResult = operationProjectMemberOrOwner(operator,
                    projectId, ownerIdList,
                    (id, ownerList) -> userProjectService.saveOwnerProject(id, ownerList), ProjectVO::getOwnerList,
                    OperateTypeEnum.APPLICATION_OWNER_CHANGE,Boolean.FALSE
    
            );
            if (operationProjectOwnerResult.failed()) {
                return operationProjectOwnerResult;
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
            //操作前
            Consumer<TupleTwo</*projectId*/Integer,/*ownerId*/Integer>> delProjectUserConsumer = tupleTwo -> projectService.delProjectOwner(
                    tupleTwo.v1, tupleTwo.v2, operator);
            final Result<Void> operationProjectOwnerResult = operationProjectMemberOrOwner(operator, projectId,
                    Collections.singletonList(ownerId),
                    (id, userList) -> userList.stream().map(user -> Tuples.of(id, user))
                            .forEach(delProjectUserConsumer), ProjectVO::getOwnerList,
                    OperateTypeEnum.APPLICATION_USER_CHANGE, Boolean.TRUE
    
            );
            if (operationProjectOwnerResult.failed()) {
                return operationProjectOwnerResult;
            }
            
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
     * @param projectId 项目id
     * @return
     */
    @Override
    public Result<List<UserBriefVO>> listUserListByProjectId(Integer projectId) {
        final List<Integer> userIdLists = userProjectService.getUserIdListByProjectId(projectId,
                ProjectUserCode.NORMAL);
        userIdLists.addAll(userProjectService.getUserIdListByProjectId(projectId, ProjectUserCode.OWNER));
        return Result.buildSucc(userService.getUserBriefListByUserIdList(userIdLists));
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
        esUserDTO.setSearchType(ProjectSearchTypeEnum.TEMPLATE.getCode());
        esUserDTO.setVerifyCode(VerifyCodeFactory.get(VERIFY_CODE_LENGTH));
        esUserDTO.setMemo((data).getProjectName() + "项目默认的es user");
        esUserDTO.setProjectId((data).getId());
        esUserDTO.setMemo("创建项目es user");
        final TupleTwo<Result, ESUserPO> result = esUserService.registerESUser(esUserDTO, operator);
        if (result.v1().success()) {
            operateRecordService.save(new OperateRecord(data.getProjectName(), OperateTypeEnum.APPLICATION_ACCESS_MODE,
                    TriggerWayEnum.MANUAL_TRIGGER, String.format("新增访问模式:%s", ProjectSearchTypeEnum.TEMPLATE.getDesc()),
                    operator, result.v2().getId()));
            
        }
    }
    
    private Result<Void> checkProject(Integer projectId, List<Integer> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Result.buildParamIllegal("用户id不存在");
        }
        if (Objects.isNull(projectId)) {
            return Result.buildParamIllegal("项目id不存在");
        }
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("项目%s不存在", projectId));
        }
        //校验当前用户id是否存在
        final List<UserVO> userList = userIdList.stream().map(userService::getUserDetailByUserId)
                .collect(Collectors.toList());
        if (userList.size() != userIdList.size()) {
            final List<Integer> idList = userList.stream().map(UserVO::getId).collect(Collectors.toList());
            //不存在用户id集合
            String notExitsIds = userIdList.stream().filter(id -> !idList.contains(id)).distinct().map(String::valueOf)
                    .collect(Collectors.joining("，"));
            return Result.buildParamIllegal(String.format("传入用户:%s不存在", notExitsIds));
        }
        //超级项目侧校验添加的用户收否存在管理员角色
        if (!checkAddProjectUserOrOwnerIsAdminRole(projectId, userIdList)) {
            return Result.buildFail("超级项目只被允许添加拥有管理员角色的用户");
        }
        
        return Result.buildSucc();
    }
    
    /**
     * 项目所有者或成员改变str
     *
     * @param beforeProjectAddOwnerOrUserList 之前项目添加所有者列表
     * @param afterProjectAddOwnerUserList    在项目添加所有者列表
     * @return {@code Tuple2<String, String>}
     */
    private TupleTwo</*beforeOwnerUserName*/String,/*afterOwnerUserName*/String> projectOwnerOrMemberChangeStr(
            List<UserBriefVO> beforeProjectAddOwnerOrUserList, List<UserBriefVO> afterProjectAddOwnerUserList){
           String beforeOwnerUserName = afterProjectAddOwnerUserList.stream().map(UserBriefVO::getUserName)
                   .sorted()
                    .collect(Collectors.joining(","));
            String afterOwnerUserName = beforeProjectAddOwnerOrUserList.stream().map(UserBriefVO::getUserName)
                    .sorted()
                    .collect(Collectors.joining(","));
            return Tuples.of(beforeOwnerUserName,afterOwnerUserName);
        
    }
    
    /**
     * 记录项目的所有者或者成员的变更
     *
     * @param tuple2      tuple2
     * @param projectName 项目名称
     * @param operator    操作人或角色
     */
    private void recordProjectOwnerOrUserChange(TupleTwo</*beforeOwnerOrUser*/String,/*afterOwnerOrUser*/String> tuple2,
                                                String projectName, String operator, OperateTypeEnum operateTypeEnum) {
        if (!StringUtils.equals(tuple2.v1, tuple2.v2)) {
            operateRecordService.save(new OperateRecord.Builder()
                    
                    .projectName(projectName).operationTypeEnum(operateTypeEnum)
                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).content(tuple2.v1 + "-->" + tuple2.v2)
                    .userOperation(operator).build());
        }
    }
    
    /**
     * 检查添加超级项目用户的合法性
     *
     * @param projectId  项目id
     * @param userIdList 用户id列表
     * @return boolean true:包含管理员角色可以添加
     */
    private boolean checkAddProjectUserOrOwnerIsAdminRole(Integer projectId, List<Integer> userIdList) {
        Predicate<List<RoleBriefVO>> checkContainsAdminRoleFunc = roleBriefList -> roleBriefList.stream()
                .anyMatch(roleBriefVO -> AuthConstant.ADMIN_ROLE_ID.equals(roleBriefVO.getId()));
        return AuthConstant.SUPER_PROJECT_ID.equals(projectId) && userIdList.stream()
                .map(roleService::getRoleBriefListByUserId).allMatch(checkContainsAdminRoleFunc);
        
    }
   
}