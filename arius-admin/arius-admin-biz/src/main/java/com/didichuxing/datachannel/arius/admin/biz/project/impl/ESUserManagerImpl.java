package com.didichuxing.datachannel.arius.admin.biz.project.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil.obj2Obj;
import static com.didichuxing.datachannel.arius.admin.core.service.project.impl.ESUserServiceImpl.VERIFY_CODE_LENGTH;

import com.didichuxing.datachannel.arius.admin.biz.project.ESUserManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ConsoleESUserWithVerifyCodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ESUserVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.common.util.VerifyCodeFactory;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ESUserService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.po.ProjectPO;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * es user
 * <blockquote>
 *     <pre>
 *         由于es user 的创建统一归 super admin 进行管理分配，那么系统所有创建es user的逻辑只需要校验用户是否是超级用户即可
 *     </pre>
 * </blockquote>
 * @see 0.2 将appservice 中的能力迁移到es user manager
 * @author shizeying
 * @date 2022/05/25
 */
@Component
public class ESUserManagerImpl implements ESUserManager {
    private static final ILog    LOGGER = LogFactory.getLog(ESUserManagerImpl.class);
    private static final String ES_USER_ERROR_MSG="应用下未匹配到逻辑集群，es user 不可以被设置为 %s 和 %s";
    @Autowired
    private ProjectService       projectService;
    @Autowired
    private ESUserService        esUserService;
    @Autowired
    private OperateRecordService operateRecordService;
    @Autowired
    private RoleTool            roleTool;
    @Autowired
    private ClusterLogicService  clusterLogicService;
    @Autowired
    private ClusterPhyService    clusterPhyService;
    @Autowired
    private ClusterRegionService clusterRegionService;

    /**
     * @param projectIdStr
     * @param request
     * @return
     */
    @Override
    public Result<List<ESUserVO>> listESUsersByProjectId(String projectIdStr, HttpServletRequest request) {

        Integer projectId = null;
        if (StringUtils.isNumeric(projectIdStr)) {
            projectId = Integer.parseInt(projectIdStr);
        } else {
            projectId = HttpRequestUtil.getProjectId(request);
        }

        final String operator = HttpRequestUtil.getOperator(request);
        if (Objects.isNull(projectId)) {
            return Result.buildNotExist("未匹配到项目下的es user");
        }
        ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);

        //确定当前操作者属于该项目成员或者是管理员
        if (!(ProjectUtils.isUserNameBelongProjectMember(operator, projectVO)
              || ProjectUtils.isUserNameBelongProjectResponsible(operator, projectVO) || roleTool.isAdmin(operator))) {
            return Result.buildParamIllegal(String.format("项目:[%s]不存在成员:[%s]", projectIdStr, request));
        }
        final List<ESUser> users = esUserService.listESUsers(Collections.singletonList(projectId));
        for (ESUser user : users) {
            user.setName(projectVO.getProjectName());

        }
        return Result.buildSucc(ConvertUtil.list2List(users, ESUserVO.class));
    }

    /**
     * 新建APP
     *
     * @param appDTO    dto
     * @param projectId
     * @param operator  操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    @Override
    public Result<Integer> registerESUser(ESUserDTO appDTO, Integer projectId, String operator) {
        if (Objects.isNull(appDTO)){
            return Result.buildFail("es user为空");
        }
        initESUser(appDTO, projectId);

        //检查esuser选择的集群并设置集群
        Result<Void> checkClusterResult = checkClusterAuthAndSetCluster(appDTO);
        if (checkClusterResult.failed()){
            LOGGER.warn("class=ESUserManagerImpl||method=registerESUser||fail msg={}", checkClusterResult.getMessage());
            return Result.buildFail(checkClusterResult.getMessage());
        }

        final TupleTwo</*创建的es user*/Result, /*创建的es user po*/ ESUserPO> resultESUserPOTuple = esUserService
            .registerESUser(appDTO, operator);

        if (resultESUserPOTuple.v1().success()) {
            // 操作记录
            saveOperateRecord(
                    String.format("新增访问模式:[%s]", ProjectSearchTypeEnum.valueOf(appDTO.getSearchType()).getDesc()),
                    projectId, operator, OperateTypeEnum.APPLICATION_ACCESS_MODE);

        }

        return resultESUserPOTuple.v1();
    }
    
   
    
    /**
     * @param esUserName   esUser
     * @param projectId
     * @param operator  操作人
     * @return {@code Result<Void>}
     */
    @Override
    public Result<Void> setDefaultDisplay(int esUserName, int projectId, String operator) {
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildFail("应用不存在");
        }
        if (esUserService.getEsUserById(esUserName) == null) {
            return Result.buildFail("es User不存在");
        }
        //获取当前应用下所有的esuser，并将之前应用默认的es user进行解绑
        List<ESUser> esUsers = esUserService.listESUsers(Collections.singletonList(projectId));
        for (ESUser esUser : esUsers) {
            if (esUser.getDefaultDisplay() == true && esUser.getId()!=esUserName) {
                Result<Void> result = unBindDefaultDisplay(esUser.getId(), projectId, operator);
                if (result.failed()) {
                    return Result.buildFail(String.format("解绑应用默认es User:%s失败，不能设置%s为应用默认es User", esUser.getId(), esUserName));
                }
            }
        }
        ESUser esUser = esUserService.getEsUserById(esUserName);
        //将需要设置为默认应用的es user进行存储
        esUser.setDefaultDisplay(true);
        TupleTwo<Result<Void>/*更新的状态*/, ESUserPO/*更新之后的的ESUserPO*/> resultESUserTuple = esUserService
                .editUser(obj2Obj(esUser, ESUserDTO.class));
        if (resultESUserTuple.v1().success()) {
            //将操作记录进行保存
            saveOperateRecord(String.format("将项目[%s]中es user:[%s]设置为项目默认的es user", projectId, esUser),
                    projectId, operator, OperateTypeEnum.APPLICATION_ACCESS_CHANGE);
        }
        return resultESUserTuple.v1();
    }

    /**
     * @param esUserDTO   es user dto
     * @param operator 操作人或角色
     * @return {@code Result<Void>}
     */
    @Override
    public Result<Void> editESUser(ESUserDTO esUserDTO, String operator) {
        if (!projectService.checkProjectExist(esUserDTO.getProjectId())) {
            return Result.buildFail("应用不存在");
        }
        Result<Void> checkResult = esUserService.validateESUser(esUserDTO, EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESUserManagerImpl||method=editESUser||fail msg={}", checkResult.getMessage());
            return checkResult;
        }
        Result<Void> checkClusterResult = checkClusterAuthAndSetCluster(esUserDTO);
        if (checkClusterResult.failed()) {
            LOGGER.warn("class=ESUserManagerImpl||method=editESUser||fail msg={}", checkClusterResult.getMessage());
            return checkClusterResult;
        }

        //获取更新之前的po
        final ESUser oldESUser = esUserService.getEsUserById(esUserDTO.getId());
        //校验当前esUserDTO中的projectId是否存在于esUser
        //更新之后的结果获取
        final TupleTwo<Result<Void>/*更新的状态*/, ESUserPO/*更新之后的的ESUserPO*/> resultESUserTuple = esUserService.editUser(esUserDTO);

        if (resultESUserTuple.v1().success()) {
            // 操作记录
            saveOperateRecord(String.format("修改访问模式:%s-->%s,修改访问集群:%s-->%s",
                            ProjectSearchTypeEnum.valueOf(oldESUser.getSearchType()).getDesc(),
                            ProjectSearchTypeEnum.valueOf(esUserDTO.getSearchType()).getDesc(),oldESUser.getCluster(), esUserDTO.getCluster()), oldESUser.getProjectId(),
                    operator, OperateTypeEnum.APPLICATION_ACCESS_CHANGE);
        }
        return resultESUserTuple.v1();
    }

    /**
     * 删除项目下指定的es user
     *
     * @param esUser    esUser
     * @param projectId
     * @param operator  操作人
     * @return 成功 true  失败 false
     */
    @Override
    public Result<Void> deleteESUserByProject(int esUser, int projectId, String operator) {
        if (!roleTool.isAdmin(operator)) {
            return Result.buildFail("当前操作者权限不足,需要管理员权限");
        }
        //校验当前项目下所有的es user
        final List<ESUser> esUsers = esUserService.listESUsers(Collections.singletonList(projectId));

        //校验当前项目中存在该es user
        if (esUsers.stream().map(ESUser::getId).noneMatch(esUserName -> Objects.equals(esUserName, esUser))) {
            return Result.buildParamIllegal(String.format("当前项目[%s]不存在es user:[%s]", projectId,
                esUsers.stream().map(ESUser::getId).collect(Collectors.toList())));
        }
        //判断删除之后的es user是否为项目使用的es user,如果是项目使用的默认es user，则需要解绑项目默认的es user 后才能进行es user的删除
        if (esUsers.stream().anyMatch(oldESUser -> Objects.equals(oldESUser.getId(), esUser)
                                                   && Boolean.TRUE.equals(oldESUser.getDefaultDisplay()))) {

            return Result.buildFail(String.format("项目[%s]中es user:[%s],属于项目默认的es user,请先进行解绑", projectId, esUser));
        }
        //进行es user的删除
        final TupleTwo<Result<Void>, ESUserPO> resultESUserPOTuple = esUserService.deleteESUserById(esUser);
        if (resultESUserPOTuple.v1().success()) {
            // 操作记录
            saveOperateRecord(String.format("删除访问模式:%s",
                            ProjectSearchTypeEnum.valueOf(resultESUserPOTuple.v2().getSearchType()).getDesc()), projectId,
                    operator, OperateTypeEnum.APPLICATION_ACCESS_MODE);
        }
        return resultESUserPOTuple.v1();
    }

    /**
     * 删除项目下所有的es user
     *
     * @param projectId 项目id
     * @param operator  操作人或角色
     * @return {@code Result<Void>}
     */
    @Override
    public Result<Void> deleteAllESUserByProject(int projectId, String operator) {
        if (!roleTool.isAdmin(operator)) {
            return Result.buildFail("当前操作者权限不足,需要管理员权限");
        }

        final TupleTwo<Result<Void>, List<ESUserPO>> resultListTuple = esUserService.deleteByESUsers(projectId);
        if (resultListTuple.v1().success()) {
            // 操作记录
            saveOperateRecord("删除访问模式", projectId, operator, OperateTypeEnum.APPLICATION_ACCESS_MODE);
        }
        return resultListTuple.v1();
    }

    /**
     * 校验验证码
     *
     * @param esUserName app
     * @param verifyCode 验证码
     * @return result
     */
    @Override
    public Result<Void> verifyAppCode(Integer esUserName, String verifyCode) {
        return esUserService.verifyAppCode(esUserName, verifyCode);
    }

    @Override
    public Result<ConsoleESUserVO> get(Integer esUser) {
        return Result.buildSucc(ConvertUtil.obj2Obj(esUserService.getEsUserById(esUser), ConsoleESUserVO.class));
    }

    /**
     * @param projectId
     * @param operator
     * @return
     */
    @Override
    public Result<List<ConsoleESUserWithVerifyCodeVO>> getNoCodeESUser(Integer projectId, String operator) {

        final ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);
        if (!(ProjectUtils.isUserNameBelongProjectMember(operator, projectVO)
              || ProjectUtils.isUserNameBelongProjectResponsible(operator, projectVO) || roleTool.isAdmin(operator))) {
            return Result.buildFail("权限不足");
        }
        List<ESUser> users = esUserService.listESUsers(Collections.singletonList(projectId));
        for (ESUser user : users) {
            user.setName(projectVO.getProjectName());

        }
        return Result.buildSucc(ConvertUtil.list2List(users, ConsoleESUserWithVerifyCodeVO.class));

    }
     private void saveOperateRecord(String content, Integer projectId, String operator,
                                   OperateTypeEnum operateTypeEnum) {
        operateRecordService.saveOperateRecordWithManualTrigger(content,operator,projectId,projectId,operateTypeEnum);
    }

    /**
     * 获取原生模式下项目的访问集群列表
     * @param projectId
     * @return Result<List<String>>
     */
    @Override
    public Result<List<String>> listClusterByAppInPrimitiveType(Integer projectId) {
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            //超级projectId返回对应的独立的物理集群
            List<String> clusterPhyList = clusterPhyService.listAllClusters().stream()
                    .filter(clusterPhy -> ClusterResourceTypeEnum.PRIVATE.getCode() == clusterPhy.getResourceType())
                    .map(ClusterPhy::getCluster)
                    .distinct()
                    .collect(Collectors.toList());
            if (clusterPhyList.isEmpty()) {
                return Result.buildFail("超级项目下无独立类型集群，无法新增原生模式的ES_User");
            } else {
                return Result.buildSucc(clusterPhyList);
            }
        } else {
            //普通项目返回对应的独立的逻辑集群
            List<String> clusterLogicList = clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId).stream()
                    .filter(clusterLogic -> ClusterResourceTypeEnum.PRIVATE.getCode() == clusterLogic.getType())
                    .map(ClusterLogic::getName)
                    .distinct().collect(Collectors.toList());
            if (clusterLogicList.isEmpty()) {
                return Result.buildFail("该项目下无独立类型集群，无法新增原生模式的ES_User");
            } else {
                return Result.buildSucc(clusterLogicList);
            }
        }
    }

    /**
     * 获取集群模式下项目的访问集群列表
     * @param projectId
     * @return Result<List<String>>
     */
    @Override
    public Result<List<String>> listClusterByAppInClusterType(Integer projectId) {
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            //超级projectId返回对应的物理集群
            List<String> clusterPhyList = clusterPhyService.listAllClusters().stream()
                    .map(ClusterPhy::getCluster)
                    .distinct()
                    .collect(Collectors.toList());
            if (clusterPhyList.isEmpty()) {
                return Result.buildFail("超级项目下无可用集群，无法新增集群模式的ES_User");
            } else {
                return Result.buildSucc(clusterPhyList);
            }
        } else {
            //普通项目返回对应的逻辑集群
            List<String> clusterLogicList = clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId).stream()
                    .map(ClusterLogic::getName)
                    .distinct().collect(Collectors.toList());
            if (clusterLogicList.isEmpty()) {
                return Result.buildFail("该项目下无可用集群，无法新增原生模式的ES_User");
            } else {
                return Result.buildSucc(clusterLogicList);
            }
        }
    }

    /**
     * 对被设置为应用默认的es user进行解绑
     *
     * @param esUserName ES用户
     * @param projectId {@link ProjectPO#getId()}
     * @param operator 操作人 {@link   com.didichuxing.datachannel.arius.admin.core.component.RoleTool#isAdmin}
     * @return {@code Result<Void>}
     */
    private Result<Void> unBindDefaultDisplay(int esUserName, int projectId, String operator) {
        if (!roleTool.isAdmin(operator)) {
            return Result.buildFail("当前操作者权限不足,需要管理员权限");
        }
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildFail("应用不存在");
        }
        //获取需要进行解绑的默认es user并进行解绑
        ESUser esUser = esUserService.getEsUserById(esUserName);
        esUser.setDefaultDisplay(false);
        TupleTwo<Result<Void>/*更新的状态*/, ESUserPO/*更新之后的的ESUserPO*/> resultESUserTuple = esUserService
                .editUser(obj2Obj(esUser, ESUserDTO.class));
        if (resultESUserTuple.v1().success()) {
            //将操作记录进行保存
            saveOperateRecord(String.format("项目[%s]中es user:[%s],属于项目默认的es user,对其进行解绑", projectId, esUser),
                    projectId, operator, OperateTypeEnum.APPLICATION_ACCESS_CHANGE);
        }
        return resultESUserTuple.v1();
    }

    /**
     *  检查集群并设置集群
     *
     * @param esUserDTO 要创建的用户信息
     */
    private Result<Void> checkClusterAuthAndSetCluster(ESUserDTO esUserDTO) {
        // 如果这里是集群模式或者原生模式，那么有且仅当项目下的物理集群唯一时，才可以被设置成功
        Integer projectId = esUserDTO.getProjectId();
       
        ProjectSearchTypeEnum searchTypeEnum = ProjectSearchTypeEnum.valueOf(esUserDTO.getSearchType());
       
        if (!searchTypeEnum.equals(ProjectSearchTypeEnum.TEMPLATE)) {
            //默认必须传集群
            if (StringUtils.isBlank(esUserDTO.getCluster())) {
                return Result.buildFail(String.format(ES_USER_ERROR_MSG,
                        ProjectSearchTypeEnum.PRIMITIVE.getDesc(), ProjectSearchTypeEnum.CLUSTER.getDesc()));
            }
            // 如果是超级项目
            if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                return Result.buildSucc();
            }
            // 获取项目下的物理集群
            ESLogicClusterDTO esLogicClusterDTO = new ESLogicClusterDTO();
            esLogicClusterDTO.setProjectId(projectId);
            List<ClusterLogic> clusterLogics = clusterLogicService.listClusterLogics(esLogicClusterDTO);
            if (CollectionUtils.isEmpty(clusterLogics)) {
                return Result.buildFail(String.format(ES_USER_ERROR_MSG,
                        ProjectSearchTypeEnum.PRIMITIVE.getDesc(), ProjectSearchTypeEnum.CLUSTER.getDesc()));
            }
            List<Long> logicClusterIds = clusterLogics.stream().map(ClusterLogic::getId).distinct()
                    .collect(Collectors.toList());
            // 找到 region
            List<ClusterRegion> regions = clusterRegionService.getClusterRegionsByLogicIds(logicClusterIds);
            if (CollectionUtils.isEmpty(regions)) {
                return Result.buildFail(String.format(ES_USER_ERROR_MSG,
                        ProjectSearchTypeEnum.PRIMITIVE.getDesc(), ProjectSearchTypeEnum.CLUSTER.getDesc()));
            }
            // 找到物理集群
            List<String> clusterPhyList = regions.stream().map(ClusterRegion::getPhyClusterName).distinct()
                    .collect(Collectors.toList());
          
            // 如果存在 cluster 且没有匹配到具有权限的物理集群
            if (StringUtils.isNotBlank(esUserDTO.getCluster()) && clusterPhyList.stream()
                    .noneMatch(cp -> StringUtils.equals(cp, esUserDTO.getCluster()))) {
                return Result.buildFail(String.format("应用没有 %s 集群的权限", esUserDTO.getCluster()));
            }
        }
        // 索引模式默认集群设置未 null
        if (searchTypeEnum.equals(ProjectSearchTypeEnum.TEMPLATE)) {
            esUserDTO.setCluster("");
        }
        return Result.buildSucc();
    }
    
    private static void initESUser(ESUserDTO appDTO, Integer projectId) {
        appDTO.setProjectId(projectId);
        appDTO.setVerifyCode(VerifyCodeFactory.get(VERIFY_CODE_LENGTH));
        appDTO.setIsActive(1);
        if (Objects.isNull(appDTO.getQueryThreshold())) {
            appDTO.setQueryThreshold(1000);
        }
        if (Objects.isNull(appDTO.getIsRoot())) {
            appDTO.setIsRoot(0);
        }
        appDTO.setMemo("新增");
    }
}