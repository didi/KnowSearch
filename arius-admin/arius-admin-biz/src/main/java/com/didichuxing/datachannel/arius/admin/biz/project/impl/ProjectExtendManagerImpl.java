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
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicTemplateIndexDetailDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectBriefExtendVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectExtendVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.VerifyCodeFactory;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.common.PagingData;
import com.didiglobal.logi.security.common.PagingResult;
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
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
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
    @Autowired
    private RoleTool          roleTool;
    @Autowired
    private               ESIndexCatService esIndexCatService;
    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("ProjectExtendManagerImpl", 10, 20, 100);
    
    /**
     * “检查一个项目的资源是否可用。”
     * <p>
     * 函数定义如下：
     * <p>
     * * 该函数返回一个 Result<Void> 对象。 * 该函数接受一个参数，一个名为 projectId 的整数对象
     *
     * @param projectId 项目的 ID。
     * @return 一个 Result 对象，里面有一个 Void 对象。
     */
    @Override
    public Result<Void> checkResourcesByProjectId(Integer projectId) {
        List<ClusterLogic> clusterLogics = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
        
        List<ClusterLogicTemplateIndexDetailDTO> clusterLogicTemplateIndexDetailDTOS = clusterLogics.stream()
                .map(clusterLogic -> getTemplateIndexVO(clusterLogic, projectId))
                
                .collect(Collectors.toList());
        long templateSize = clusterLogicTemplateIndexDetailDTOS.stream()
                .map(ClusterLogicTemplateIndexDetailDTO::getTemplates).filter(CollectionUtils::isNotEmpty)
                .mapToLong(Collection::size).sum();
        long indexSize = clusterLogicTemplateIndexDetailDTOS.stream()
                .map(ClusterLogicTemplateIndexDetailDTO::getCatIndexResults).filter(CollectionUtils::isNotEmpty)
                .mapToLong(Collection::size).sum();
        if (CollectionUtils.isNotEmpty(clusterLogics) || templateSize != 0 || indexSize != 0) {
            return Result.buildFail(String.format(
                    "无法删除 %s！如需删除，请下线掉应用关联的全部集群、模板、索引资源。",
                    projectService.getProjectDetailByProjectId(projectId).getProjectName()));
        }
        
        return Result.buildSucc();
    }
    
    @Override
    public Result<ProjectExtendVO> createProject(ProjectExtendSaveDTO saveDTO, String operator, Integer operatorId) {
        try {
            // 1. 创建项目
            ProjectSaveDTO project = saveDTO.getProject();
            // 2. 创建项目配置
            ProjectConfigDTO config = saveDTO.getConfig();
            // 将项目中的所有者、用户提取出来后，使用biz层中的逻辑进行添加
            List<Integer> ownerIdList = Optional.ofNullable(project.getOwnerIdList()).orElse(Lists.newArrayList());
            List<Integer> userIdList = Optional.ofNullable(project.getUserIdList()).orElse(Lists.newArrayList());
            project.setOwnerIdList(Collections.emptyList());
            project.setUserIdList(Collections.emptyList());
            //当所有者没有传入进来的时候
            if (CollectionUtils.isEmpty(ownerIdList)) {
                if (operatorId.equals(-1)) {
                    return Result.buildFail("当前操作人id为空");
                }
                ownerIdList.add(operatorId);

            }
            //谁创建、谁包含
            if (!ownerIdList.contains(operatorId)) {
                ownerIdList.add(operatorId);
            }

            // 3. 创建项目
            ProjectVO projectVO = projectService.createProject(project, operator);
            // 4. 转换
            ProjectExtendVO projectExtendVO = ConvertUtil.obj2Obj(projectVO, ProjectExtendVO.class);
            // 5. 添加拥有者和成员
            addOwnerAndUsers(operator, ownerIdList, userIdList, projectVO, projectExtendVO);

            //5. 设置项目id
            config.setProjectId(projectVO.getId());
            // 6. 创建项目配置
            TupleTwo<Result<Void>, ProjectConfigPO> resultProjectConfigTuple = projectConfigService
                .updateOrInitProjectConfig(config, operator);
        
            // 全量获取具有管理员角色的用户
            final List<UserBriefVO> userBriefListWithAdminRole = userService.getUserBriefListByRoleId(AuthConstant.ADMIN_ROLE_ID);
            buildProjectExtendVO(projectExtendVO,userBriefListWithAdminRole);
            // 设置项目配置
            if (resultProjectConfigTuple.v1().success()) {
                projectExtendVO.setConfig(ConvertUtil.obj2Obj(resultProjectConfigTuple.v2(), ProjectConfigVO.class));

            }
            //7. 写入操作日志
            operateRecordService.save(new OperateRecord(project.getProjectName(), OperateTypeEnum.APPLICATION_CREATE,
                TriggerWayEnum.MANUAL_TRIGGER, project.getProjectName(), operator));
            //创建es user
            createESUserDefault(projectVO, operator);
            if (Objects.isNull(project.getId())) {
                projectExtendVO.setId(projectExtendVO.getConfig().getProjectId());
            }
            return Result.<ProjectExtendVO> buildSucc(projectExtendVO);
        } catch (LogiSecurityException e) {
            return Result.buildFail(e.getMessage());
        }
    }

    private void addOwnerAndUsers(String operator, List<Integer> ownerIdList, List<Integer> userIdList,
                                  ProjectVO projectVO, ProjectExtendVO projectExtendVO) {
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
            
            // 全量获取具有管理员角色的用户
            final List<UserBriefVO> userBriefListWithAdminRole =userService.getUserBriefListByRoleId(AuthConstant.ADMIN_ROLE_ID);
            buildProjectExtendVO(projectExtendVO, userBriefListWithAdminRole);
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
    private void buildProjectExtendVO(ProjectExtendVO projectExtendVO, List<UserBriefVO> userBriefListWithAdminRole) {
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectExtendVO.getId())) {
            projectExtendVO.setUserList(userBriefListWithAdminRole);
            projectExtendVO.setOwnerList(userBriefListWithAdminRole);
            projectExtendVO.setIsAdmin(true);
        } else {
            List<UserBriefVO> ownerList = Optional.ofNullable(projectExtendVO.getOwnerList())
                    .orElse(Lists.newArrayList());
            List<UserBriefVO> useList = Optional.ofNullable(projectExtendVO.getUserList()).orElse(Lists.newArrayList());
            ownerList.addAll(userBriefListWithAdminRole);
            useList.addAll(userBriefListWithAdminRole);
        
            projectExtendVO.setOwnerList(ownerList.stream().distinct().collect(Collectors.toList()));
            projectExtendVO.setUserList(useList.stream().distinct().collect(Collectors.toList()));
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
        // 校验项目绑定逻辑集群
        Result<Void> result = checkResourcesByProjectId(projectId);
        if (result.failed()) {
            return result;
        }
        ProjectBriefVO projectBriefVO = projectService.getProjectBriefByProjectId(projectId);
        projectService.deleteProjectByProjectId(projectId, operator);
        //获取全部项目配置
        List<ESUser> esUsers = esUserService.listESUsers(Collections.singletonList(projectId));

        //删除es user
        esUserService.deleteByESUsers(projectId);
        for (ESUser esUser : esUsers) {
            operateRecordService.save(new OperateRecord(projectBriefVO.getProjectName(),
                OperateTypeEnum.APPLICATION_ACCESS_MODE, TriggerWayEnum.MANUAL_TRIGGER,
                String.format("删除访问模式:%s", ProjectSearchTypeEnum.valueOf(esUser.getSearchType()).getDesc()), operator));
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
     * @param request
     * @return 项目分页信息
     */
    @Override
    public PagingResult<ProjectExtendVO> getProjectPage(ProjectQueryExtendDTO queryDTO, HttpServletRequest request) {
        final List<Integer> projectIds = Lists.newArrayList();
        final List<Integer> esUserByProjectIds = Lists.newArrayList();
        //当查询模式不为空
        if (Objects.nonNull(queryDTO.getSearchType())) {
            final List<Integer> projectIdBySearchType = esUserService
                .getProjectIdBySearchType(queryDTO.getSearchType());
            if (CollectionUtils.isEmpty(projectIdBySearchType)) {
                return PagingResult
                    .success(new PagingData<>(Collections.emptyList(), PagingData.Pagination.builder().build()));
            } else {
                esUserByProjectIds.addAll(projectIdBySearchType);

            }
        }
        String operator = HttpRequestUtil.getOperator(request);
        Integer operatorId = HttpRequestUtil.getOperatorId(request);
        //如果当前操作人不是管理员的角色，则只应该获取属于自己的项目，不能获取全部的项目
        if (StringUtils.isNotBlank(operator) && !roleTool.isAdmin(operator)) {

            final List<Integer> operatorIdByProjectIds = userProjectService
                .getProjectIdListByUserIdList(Collections.singletonList(operatorId));
            //esUserByProjectIds、operatorIdByProjectIds取交集
            if (CollectionUtils.isNotEmpty(esUserByProjectIds)) {
                projectIds.addAll(
                    Sets.intersection(Sets.newHashSet(esUserByProjectIds), Sets.newHashSet(operatorIdByProjectIds)));
            } else {
                projectIds.addAll(operatorIdByProjectIds);
            }

        } else {
            projectIds.addAll(esUserByProjectIds);
        }
        /**
         * 1.管理员角色的用户
         * 	应用列表:全部展示()
         * 	编辑能力:应该具备
         * 	删除能力:应该具备
         * 2.非管理员角色的用户:
         * 	应用列表:
         * 		非检索状态下:展示自己拥有的应用列表
         * 		编辑能力:应该具备
         * 		删除能力:应该具备
         * 		检索状态下 :  假如A应用不属于当前的用户,是否可以被检索出来(不能)
         * 		编辑能力   :  假如A应用不属于当前的用户,是否可以被编辑(不能)
         * 		删除能力  :   假如A应用不属于当前的用户,是否可以被删除 (不能)
         */
        final ProjectQueryDTO projectQueryDTO = ConvertUtil.obj2Obj(queryDTO, ProjectQueryDTO.class);

        final PagingData<ProjectVO> projectPage = projectService.getProjectPage(projectQueryDTO, projectIds);
        final List<ProjectExtendVO> projectExtendVOList = ConvertUtil.list2List(projectPage.getBizData(),
            ProjectExtendVO.class);
        //全量获取具有管理员角色的用户
        final List<UserBriefVO> userBriefListWithAdminRole =
                userService.getUserBriefListByRoleId(AuthConstant.ADMIN_ROLE_ID);
        for (ProjectExtendVO projectExtendVO : projectExtendVOList) {
            FUTURE_UTIL.runnableTask(() -> {
                
                buildProjectExtendVO(projectExtendVO,userBriefListWithAdminRole);
                final ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectExtendVO.getId());
                projectExtendVO.setConfig(ConvertUtil.obj2Obj(projectConfig, ProjectConfigVO.class));
            });
        }
        FUTURE_UTIL.waitExecute();
        return PagingResult.success(new PagingData<>(projectExtendVOList, projectPage.getPagination()));
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
            //超级项目侧校验添加的用户收否存在管理员角色
            final Result<Void> ownerResult = checkProject(project.getId(), ownerIdList, OperationEnum.EDIT);
            if (ownerResult.failed()) {
                return ownerResult;
            }
            //超级项目侧校验添加的用户收否存在管理员角色
            final Result<Void> userResult = checkProject(project.getId(), userIdList, OperationEnum.EDIT);
            if (userResult.failed()) {
                return userResult;
            }
            project.setOwnerIdList(Collections.emptyList());
            project.setUserIdList(Collections.emptyList());
            //操作前的项目信息
            final ProjectVO beforeProjectVo = projectService.getProjectDetailByProjectId(project.getId());
            projectService.updateProject(project, operator);
            //更新项目与用户拥有者的关联关系
            final Result<Void> operationProjectOwnerResult = operationProjectMemberOrOwner(operator,
                beforeProjectVo, ownerIdList,
                (projectId, userList) -> userProjectService.updateOwnerProject(projectId, userList),
                ProjectVO::getOwnerList, OperateTypeEnum.APPLICATION_OWNER_CHANGE, OperationEnum.EDIT

            );
            if (operationProjectOwnerResult.failed()) {
                return operationProjectOwnerResult;
            }
            //更新项目与用户成员的关联关系
            final Result<Void> operationProjectUserResult = operationProjectMemberOrOwner(operator,
                beforeProjectVo, userIdList,
                (projectId, userList) -> userProjectService.updateUserProject(projectId, userList),
                ProjectVO::getUserList, OperateTypeEnum.APPLICATION_USER_CHANGE, OperationEnum.EDIT

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
     * @param beforeProjectVo         操作前的项目id
     * @param userOrOwnerList 添加/更新用户id列表
     * @param operationConsumerFunc    更新/添加操作
     * @param  operationFuncMapper      函数映射器 需要获取的mapper userList/ownerList
     * @param operateTypeEnum 操作类型枚举
     * @param operation       是否是删除操作
     * @return {@code Result<Void>}
     */
    private Result<Void> operationProjectMemberOrOwner(String operator, ProjectVO beforeProjectVo,
                                                       List<Integer> userOrOwnerList,
                                                       BiConsumer<Integer, List<Integer>> operationConsumerFunc,
                                                       Function<ProjectVO, List<UserBriefVO>> operationFuncMapper,
                                                       OperateTypeEnum operateTypeEnum, OperationEnum operation) {
        //超级项目侧校验添加的用户收否存在管理员角色
        final Result<Void> result = checkProject(beforeProjectVo.getId(), userOrOwnerList, operation);
        if (result.failed()) {
            return result;
        }
    
        final String projectName = beforeProjectVo.getProjectName();
        List<UserBriefVO> beforeProjectUserList = Lists.newArrayList();
        //操作前的
        Optional.ofNullable(beforeProjectVo).map(operationFuncMapper).ifPresent(beforeProjectUserList::addAll);
        //更新/add
        operationConsumerFunc.accept(beforeProjectVo.getId(), userOrOwnerList);
        //操作后
        List<UserBriefVO> afterProjectUserList = Lists.newArrayList();
        Optional.ofNullable(projectService.getProjectDetailByProjectId(beforeProjectVo.getId()))
                .map(operationFuncMapper).ifPresent(afterProjectUserList::addAll);
        TupleTwo</*beforeUserStr*/String, /*afterUserStr*/String> tuple2 = projectOwnerOrMemberChangeStr(
                beforeProjectUserList, afterProjectUserList);
        recordProjectOwnerOrUserChange(tuple2, projectName, operator, operateTypeEnum);
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
        } catch (LogiSecurityException e) {
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
            final ProjectVO beforeProjectVo = projectService.getProjectDetailByProjectId(projectId);
            final Result<Void> operationProjectUserResult = operationProjectMemberOrOwner(operator, beforeProjectVo,
                userIdList, (id, userList) -> userProjectService.saveUserProject(id, userList), ProjectVO::getUserList,
                OperateTypeEnum.APPLICATION_USER_CHANGE, OperationEnum.ADD);
            if (operationProjectUserResult.failed()) {
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
            final ProjectVO beforeProjectVo = projectService.getProjectDetailByProjectId(projectId);
            Consumer<TupleTwo</*projectId*/Integer, /*userId*/Integer>> delProjectUserConsumer = tupleTwo -> projectService
                .delProjectUser(tupleTwo.v1, tupleTwo.v2, operator);
            final Result<Void> operationProjectOwnerResult = operationProjectMemberOrOwner(operator, beforeProjectVo,
                Collections.singletonList(userId),
                (id, userList) -> userList.stream().map(user -> Tuples.of(id, user)).forEach(delProjectUserConsumer),
                ProjectVO::getOwnerList, OperateTypeEnum.APPLICATION_USER_CHANGE, OperationEnum.DELETE

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
            final ProjectVO beforeProjectVo = projectService.getProjectDetailByProjectId(projectId);
            final Result<Void> operationProjectOwnerResult = operationProjectMemberOrOwner(operator, beforeProjectVo,
                ownerIdList, (id, ownerList) -> userProjectService.saveOwnerProject(id, ownerList),
                ProjectVO::getOwnerList, OperateTypeEnum.APPLICATION_OWNER_CHANGE, OperationEnum.ADD

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
            final ProjectVO beforeProjectVo = projectService.getProjectDetailByProjectId(projectId);
            Consumer<TupleTwo</*projectId*/Integer, /*ownerId*/Integer>> delProjectUserConsumer = tupleTwo -> projectService
                .delProjectOwner(tupleTwo.v1, tupleTwo.v2, operator);
            final Result<Void> operationProjectOwnerResult = operationProjectMemberOrOwner(operator, beforeProjectVo,
                Collections.singletonList(ownerId),
                (id, userList) -> userList.stream().map(user -> Tuples.of(id, user)).forEach(delProjectUserConsumer),
                ProjectVO::getOwnerList, OperateTypeEnum.APPLICATION_USER_CHANGE, OperationEnum.DELETE

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
        final com.didiglobal.logi.security.common.Result<List<UserBriefVO>> listResult = projectService
            .unassignedByProjectId(projectId);
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
        final com.didiglobal.logi.security.common.Result<List<ProjectBriefVO>> listResult = projectService
            .getProjectBriefByUserId(userId);
        if (listResult.successed()) {
            final List<ProjectBriefExtendVO> dtoList = ConvertUtil.list2List(listResult.getData(),
                ProjectBriefExtendVO.class).stream()
                    .filter(distinctByKey(ProjectBriefVO::getId)).collect(Collectors.toList());
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

    private Result<Void> checkProject(Integer projectId, List<Integer> userIdList, OperationEnum operation) {
        if (operation.equals(OperationEnum.DELETE)) {
            return Result.buildSucc();
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
        if (operation.equals(OperationEnum.EDIT)) {
            if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                Predicate<List<RoleBriefVO>> checkContainsAdminRoleFunc = roleBriefList -> roleBriefList.stream()
                    .anyMatch(roleBriefVO -> AuthConstant.ADMIN_ROLE_ID.equals(roleBriefVO.getId()));
                /*当前用户列表中存在管理员*/
                if (CollectionUtils.isNotEmpty(userIdList)&&userIdList.stream().map(roleService::getRoleBriefListByUserId)
                    .noneMatch(checkContainsAdminRoleFunc)) {
                    return Result.buildFail("超级项目只被允许添加拥有管理员角色的用户");

                }
            }

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
    private TupleTwo</*beforeOwnerUserName*/String, /*afterOwnerUserName*/String> projectOwnerOrMemberChangeStr(List<UserBriefVO> beforeProjectAddOwnerOrUserList,
                                                                                                                List<UserBriefVO> afterProjectAddOwnerUserList) {
        String beforeOwnerUserName = beforeProjectAddOwnerOrUserList.stream().map(UserBriefVO::getUserName).sorted()
            .collect(Collectors.joining(","));
        String afterOwnerUserName = afterProjectAddOwnerUserList.stream().map(UserBriefVO::getUserName).sorted()
            .collect(Collectors.joining(","));
        return Tuples.of(beforeOwnerUserName, afterOwnerUserName);

    }

    /**
     * 记录项目的所有者或者成员的变更
     *
     * @param tuple2      tuple2
     * @param projectName 项目名称
     * @param operator    操作人或角色
     */
    private void recordProjectOwnerOrUserChange(TupleTwo</*beforeOwnerOrUser*/String, /*afterOwnerOrUser*/String> tuple2,
                                                String projectName, String operator, OperateTypeEnum operateTypeEnum) {
        if (!StringUtils.equals(tuple2.v1, tuple2.v2)) {
            String content=StringUtils.isBlank(tuple2.v1)?String.format("新增:【%s】",tuple2.v2):
                    StringUtils.isBlank(tuple2.v2)?String.format("删除:【%s】",tuple2.v1):String.format("【%s】-->【%s】",
                            tuple2.v1,tuple2.v2 );
            
            operateRecordService.save(new OperateRecord.Builder()

                .projectName(projectName).operationTypeEnum(operateTypeEnum)
                .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).content(content)
                .userOperation(operator).build());
        }
    }
    
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
    
    private ClusterLogicTemplateIndexDetailDTO getTemplateIndexVO(ClusterLogic clusterLogic, Integer projectId) {
        IndexTemplateDTO param = new IndexTemplateDTO();
        param.setResourceId(clusterLogic.getId());
        param.setProjectId(projectId);
        List<IndexTemplate> indexTemplates = indexTemplateService.listLogicTemplates(param);
        // 通过逻辑集群获取 index
        List<IndexCatCellDTO> catIndexResults = esIndexCatService.syncGetIndexByCluster(clusterLogic.getName(),
                projectId);
        ClusterLogicTemplateIndexDetailDTO templateIndexVO = new ClusterLogicTemplateIndexDetailDTO();
        templateIndexVO.setCatIndexResults(catIndexResults);
        templateIndexVO.setTemplates(indexTemplates);
        return templateIndexVO;
    }
}