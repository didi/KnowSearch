package com.didichuxing.datachannel.arius.admin.core.service.project.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectClusterLogicAuthPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.event.auth.ProjectLogicClusterAuthAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.ProjectLogicClusterAuthDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.ProjectLogicClusterAuthEditEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.project.ProjectLogicClusterAuthDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * APP 逻辑集群权限服务
 * @author wangshu
 * @date 2020/09/19
 */
@Service
public class ProjectClusterLogicAuthServiceImpl implements ProjectClusterLogicAuthService {

    private static final ILog          LOGGER = LogFactory.getLog(ProjectLogicTemplateAuthServiceImpl.class);

    @Autowired
    private ProjectLogicClusterAuthDAO logicClusterAuthDAO;

    @Autowired
    private ClusterLogicService        clusterLogicService;

    @Autowired
    private OperateRecordService       operateRecordService;
    @Autowired
    private ProjectService             projectService;
    @Autowired
    private RoleTool                   roleTool;

    /**
     * 设置APP对某逻辑集群的权限. 封装了新增、更新、删除操作，调用接口时只需描述期望的权限状态
     *
     * @param projectId      APP的ID
     * @param logicClusterId 逻辑集群ID
     * @param auth           要设置的权限
     * @param operator       操作人
     * @return 设置结果
     */
    @Override
    public Result<Void> ensureSetLogicClusterAuth(Integer projectId, Long logicClusterId,
                                                  ProjectClusterLogicAuthEnum auth, String operator) {
        // 参数检查
        if (projectId == null) {
            return Result.buildParamIllegal("未指定projectId");
        }

        if (logicClusterId == null) {
            return Result.buildParamIllegal("未指定逻辑集群ID");
        }

        if (StringUtils.isBlank(operator)) {
            return Result.buildParamIllegal("未指定操作人");
        }

        // 获取已经存在的权限，可能来自于权限表（id不为null）和创建信息表（id为null）
        ProjectClusterLogicAuth oldAuth = getLogicClusterAuth(projectId, logicClusterId);

        if (oldAuth == null || oldAuth.getType().equals(ProjectClusterLogicAuthEnum.NO_PERMISSIONS.getCode())) {
            // 之前无权限
            return handleNoAuth(projectId, logicClusterId, auth, operator);
        } else {
            // 之前有权限
            if (oldAuth.getId() != null) {
                // 期望删除权限
                return deleteAuth(auth,  operator, oldAuth);
            } else {
                //权限来自于创建信息表（权限肯定为OWN）,对于集群owner的app权限信息不能修改，只能增加大于OWN的权限
                return addAuth(projectId, logicClusterId, auth,  operator);
            }
        }
    }

    private Result<Void> addAuth(Integer projectId, Long logicClusterId, ProjectClusterLogicAuthEnum auth,
                                 String operator) {
        if (auth != null
            && ProjectClusterLogicAuthEnum.valueOf(auth.getCode()).higher(ProjectClusterLogicAuthEnum.OWN)) {
            return addLogicClusterAuth(
                new ProjectLogicClusterAuthDTO(null, projectId, logicClusterId, auth.getCode()), operator);
        } else {
            return Result.buildFail("不支持对集群owner的权限进行修改");
        }
    }

    private Result<Void> deleteAuth(ProjectClusterLogicAuthEnum auth,String operator,
                                    ProjectClusterLogicAuth oldAuth) {
        if (auth == ProjectClusterLogicAuthEnum.NO_PERMISSIONS) {
            return deleteLogicClusterAuthById(oldAuth.getId(), operator);
        }

        // 期望更新权限信息
        ProjectLogicClusterAuthDTO newAuthDTO = new ProjectLogicClusterAuthDTO(oldAuth.getId(), null, null,
            auth == null ? null : auth.getCode() );
        return updateLogicClusterAuth(newAuthDTO, operator);
    }

    private Result<Void> handleNoAuth(Integer projectId, Long logicClusterId, ProjectClusterLogicAuthEnum auth,
                                       String operator) {
        // NO_PERMISSIONS不需添加
        if (auth == null || auth == ProjectClusterLogicAuthEnum.NO_PERMISSIONS) {
            return Result.buildSucc();
        }

        // 新增
        return addLogicClusterAuth(
            new ProjectLogicClusterAuthDTO(null, projectId, logicClusterId, auth.getCode()), operator);
    }

    /**
     * 插入逻辑集群权限点
     * @param logicClusterAuth 逻辑集群权限点
     * @return
     */
    @Override
    public Result<Void> addLogicClusterAuth(ProjectLogicClusterAuthDTO logicClusterAuth, String operator) {

        Result<Void> checkResult = validateLogicClusterAuth(logicClusterAuth, OperationEnum.ADD);
        if (checkResult.failed()) {
            LOGGER.warn(
                "class=ProjectClusterLogicAuthServiceImpl||method=createLogicClusterAuth||msg={}||msg=check fail!",
                checkResult.getMessage());
            return checkResult;
        }

        return addLogicClusterAuthWithoutCheck(logicClusterAuth, operator);
    }

    /**
     * 更新逻辑集群权限点
     * @param logicClusterAuth 逻辑集群权限点
     * @return
     */
    @Override
    public Result<Void> updateLogicClusterAuth(ProjectLogicClusterAuthDTO logicClusterAuth, String operator) {
        // 只支持修改权限类型和责任人
        logicClusterAuth.setProjectId(null);
        logicClusterAuth.setLogicClusterId(null);

        Result<Void> checkResult = validateLogicClusterAuth(logicClusterAuth, OperationEnum.EDIT);
        if (checkResult.failed()) {
            LOGGER.warn(
                "class=ProjectClusterLogicAuthServiceImpl||method=createLogicClusterAuth||msg={}||msg=check fail!",
                checkResult.getMessage());
            return checkResult;
        }

        return updateLogicClusterAuthWithoutCheck(logicClusterAuth, operator);
    }

    /**
     * 删除权限点
     * @param authId 权限点ID
     * @return
     */
    @Override
    public Result<Void> deleteLogicClusterAuthById(Long authId, String operator) {

        ProjectClusterLogicAuthPO oldAuthPO = logicClusterAuthDAO.getById(authId);
        if (oldAuthPO == null) {
            return Result.buildNotExist("权限不存在");
        }

        boolean succeed = 1 == logicClusterAuthDAO.delete(authId);
        if (succeed) {
            SpringTool.publish(new ProjectLogicClusterAuthDeleteEvent(this,
                ConvertUtil.obj2Obj(oldAuthPO, ProjectClusterLogicAuth.class)));
            operateRecordService.save(new OperateRecord.Builder().bizId(oldAuthPO.getId())
                .operationTypeEnum(OperateTypeEnum.MY_CLUSTER_OFFLINE).userOperation(operator).build());
        }

        return Result.build(succeed);
    }

    @Override
    public Result<Boolean> deleteLogicClusterAuthByLogicClusterId(Long logicClusterId) {
        boolean succ = logicClusterAuthDAO.deleteByLogicClusterId(logicClusterId) >= 0;
        return Result.buildBoolen(succ);
    }

    /**
     * 获取APP所有权限点
     * @param projectId 逻辑ID
     * @return
     */
    @Override
    public List<ProjectClusterLogicAuth> getAllLogicClusterAuths(Integer projectId) {

        if (projectId == null) {
            return new ArrayList<>();
        }

        // 权限表
        List<ProjectClusterLogicAuthPO> authPOs = logicClusterAuthDAO.listByProjectId(projectId);
        List<ProjectClusterLogicAuth> authDTOs = ConvertUtil.list2List(authPOs, ProjectClusterLogicAuth.class);

        // 从逻辑集群表获取APP作为owner的集群
        List<ClusterLogic> clusterLogicList = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
        authDTOs.addAll(clusterLogicList.stream()
            .map(clusterLogic -> buildLogicClusterAuth(clusterLogic, ProjectClusterLogicAuthEnum.OWN))
            .collect(Collectors.toList()));

        return authDTOs;
    }

    @Override
    public List<ProjectClusterLogicAuth> getLogicClusterAccessAuths(Integer projectId) {
        return ConvertUtil.list2List(logicClusterAuthDAO.listWithAccessByProjectId(projectId),
            ProjectClusterLogicAuth.class);
    }

    /**
     * 根据ID获取逻辑集群权限点
     * @param authId 权限点ID
     * @return
     */
    @Override
    public ProjectClusterLogicAuth getLogicClusterAuthById(Long authId) {
        return ConvertUtil.obj2Obj(logicClusterAuthDAO.getById(authId), ProjectClusterLogicAuth.class);
    }

    /**
     * 获取指定app对指定逻辑集群的权限.
     * @param projectId          APP ID
     * @param logicClusterId 逻辑集群ID
     */
    @Override
    public ProjectClusterLogicAuthEnum getLogicClusterAuthEnum(Integer projectId, Long logicClusterId) {
        if (projectId == null || logicClusterId == null) {
            return ProjectClusterLogicAuthEnum.NO_PERMISSIONS;
        }

        ProjectClusterLogicAuth auth = getLogicClusterAuth(projectId, logicClusterId);
        return auth == null ? ProjectClusterLogicAuthEnum.NO_PERMISSIONS
            : ProjectClusterLogicAuthEnum.valueOf(auth.getType());
    }

    /**
     * 获取指定app对指定逻辑集群的权限，若没有权限则返回null.
     * 有权限时，返回结果中id不为null则为来自于权限表的数据，否则为来自于创建表的数据
     * @param projectId          APP ID
     * @param logicClusterId 逻辑集群ID
     */
    @Override
    public ProjectClusterLogicAuth getLogicClusterAuth(Integer projectId, Long logicClusterId) {
        if (projectId == null || logicClusterId == null) {
            return null;
        }

        // 从逻辑集群表获取创建信息
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByIdAndProjectId(logicClusterId, projectId);
        ProjectClusterLogicAuthEnum authFromCreateRecord = (clusterLogic != null
                                                            && projectId.equals(clusterLogic.getProjectId()))
                                                                ? ProjectClusterLogicAuthEnum.OWN
                                                                : ProjectClusterLogicAuthEnum.NO_PERMISSIONS;

        // 从权限表获取权限信息
        ProjectClusterLogicAuthPO authPO = logicClusterAuthDAO.getByProjectIdAndLogicClusterId(projectId,
            logicClusterId);
        ProjectClusterLogicAuthEnum authFromAuthRecord = authPO != null
            ? ProjectClusterLogicAuthEnum.valueOf(authPO.getType())
            : ProjectClusterLogicAuthEnum.NO_PERMISSIONS;

        // 都没有权限
        if (authFromCreateRecord == ProjectClusterLogicAuthEnum.NO_PERMISSIONS
            && authFromAuthRecord == ProjectClusterLogicAuthEnum.NO_PERMISSIONS) {
            return buildLogicClusterAuth(clusterLogic, ProjectClusterLogicAuthEnum.NO_PERMISSIONS);
        }

        // 选择权限高的构建AppLogicClusterAuthDTO，优先取权限表中的记录
        return authFromAuthRecord.higherOrEqual(authFromCreateRecord)
            ? ConvertUtil.obj2Obj(authPO, ProjectClusterLogicAuth.class)
            : buildLogicClusterAuth(clusterLogic, ProjectClusterLogicAuthEnum.OWN);

    }

    /**
     * 获取逻辑集群权限点列表
     * @param logicClusterId  逻辑集群ID
     * @param clusterAuthType 集群权限类型
     * @return
     */
    @Override
    public List<ProjectClusterLogicAuth> getLogicClusterAuths(Long logicClusterId,
                                                              ProjectClusterLogicAuthEnum clusterAuthType) {

        ProjectClusterLogicAuthPO queryParams = new ProjectClusterLogicAuthPO();
        if (logicClusterId != null) {
            queryParams.setLogicClusterId(logicClusterId);
        }

        if (clusterAuthType != null) {
            queryParams.setType(clusterAuthType.getCode());
        }

        // 权限表
        List<ProjectClusterLogicAuthPO> authPOs = logicClusterAuthDAO.listByCondition(queryParams);
        List<ProjectClusterLogicAuth> authDTOS = ConvertUtil.list2List(authPOs, ProjectClusterLogicAuth.class);

        // 从逻辑集群表获取APP作为owner的集群
        if (logicClusterId != null && clusterAuthType == ProjectClusterLogicAuthEnum.OWN) {
    
            clusterLogicService.listClusterLogicByIdThatProjectIdStrConvertProjectIdList(logicClusterId).stream()
                    .filter(Objects::nonNull)
                    //需要绑定项目
                    .map(clusterLogic -> buildLogicClusterAuth(clusterLogic, ProjectClusterLogicAuthEnum.OWN))
                    .filter(Objects::nonNull).forEach(authDTOS::add);
        }

        return authDTOS;
    }

    @Override
    public boolean canCreateLogicTemplate(Integer projectId, Long logicClusterId) {
        if (projectId == null || logicClusterId == null) {
            return false;
        }

        ProjectClusterLogicAuthEnum authEnum = getLogicClusterAuthEnum(projectId, logicClusterId);
        return authEnum.higherOrEqual(ProjectClusterLogicAuthEnum.ACCESS);
    }

    /**
     * 增加权限  不做参数校验
     * @param authDTO  权限信息
     * @param operator 操作人
     * @return result
     */
    @Override
    public Result<Void> addLogicClusterAuthWithoutCheck(ProjectLogicClusterAuthDTO authDTO, String operator) {
        ProjectClusterLogicAuthPO authPO = ConvertUtil.obj2Obj(authDTO, ProjectClusterLogicAuthPO.class);

        boolean succeed = 1 == logicClusterAuthDAO.insert(authPO);
        if (succeed) {
            // 发送消息
            SpringTool.publish(
                new ProjectLogicClusterAuthAddEvent(this, ConvertUtil.obj2Obj(authPO, ProjectClusterLogicAuth.class)));

            // 记录操作
            operateRecordService.save(new OperateRecord.Builder().content(JSON.toJSONString(authPO))
                .userOperation(operator).operationTypeEnum(OperateTypeEnum.MY_CLUSTER_INFO_MODIFY)
                .project(projectService.getProjectBriefByProjectId(authDTO.getProjectId())).bizId(authPO.getId())
                .build());
        }

        return Result.build(succeed);
    }

    @Override
    public ProjectClusterLogicAuth buildClusterLogicAuth(Integer projectId, Long clusterLogicId,
                                                         ProjectClusterLogicAuthEnum projectClusterLogicAuthEnum) {
        if (null == projectClusterLogicAuthEnum || null == projectId || null == clusterLogicId) {
            return null;
        }

        if (!ProjectClusterLogicAuthEnum.isExitByCode(projectClusterLogicAuthEnum.getCode())) {
            return null;
        }

        ProjectClusterLogicAuth projectClusterLogicAuth = new ProjectClusterLogicAuth();
        projectClusterLogicAuth.setProjectId(projectId);
        projectClusterLogicAuth.setLogicClusterId(clusterLogicId);
        projectClusterLogicAuth.setType(projectClusterLogicAuthEnum.getCode());
        return projectClusterLogicAuth;
    }

    @Override
    public List<ProjectClusterLogicAuth> list() {
        return ConvertUtil.list2List(logicClusterAuthDAO.listByCondition(null), ProjectClusterLogicAuth.class);
    }

    /**************************************** private method ****************************************************/
    /**
     * 验证权限参数
     * @param authDTO   参数信息
     * @param operation 操作
     * @return result
     */
    private Result<Void> validateLogicClusterAuth(ProjectLogicClusterAuthDTO authDTO, OperationEnum operation) {
        if (!EnvUtil.isOnline()) {
            LOGGER.info("class=AppAuthServiceImpl||method=validateTemplateAuth||authDTO={}||operator={}",
                JSON.toJSONString(authDTO), operation);
        }

        if (authDTO == null) {
            return Result.buildParamIllegal("权限信息为空");
        }

        Integer projectId = authDTO.getProjectId();
        Long logicClusterId = authDTO.getLogicClusterId();
        ProjectClusterLogicAuthEnum authEnum = ProjectClusterLogicAuthEnum.valueOf(authDTO.getType());

        if (OperationEnum.ADD.equals(operation)) {
            Result<Void> result = handleAdd(authDTO, projectId, logicClusterId, authEnum);
            if (result.failed()) {
                return result;
            }

        } else if (OperationEnum.EDIT.equals(operation)) {
            Result<Void> result = handleEdit(authDTO);
            if (result.failed()) {
                return result;
            }
        }

        Result<Void> isIllegalResult = isIllegal(authDTO, authEnum);
        if (isIllegalResult.failed()) {
            return isIllegalResult;
        }

        return Result.buildSucc();
    }

    private Result<Void> handleEdit(ProjectLogicClusterAuthDTO authDTO) {
        // 更新权限检查
        if (AriusObjUtils.isNull(authDTO.getId())) {
            return Result.buildParamIllegal("权限ID为空");
        }

        if (null == logicClusterAuthDAO.getById(authDTO.getId())) {
            return Result.buildNotExist("权限不存在");
        }
        return Result.buildSucc();
    }

    private Result<Void> handleAdd(ProjectLogicClusterAuthDTO authDTO, Integer projectId, Long logicClusterId,
                                   ProjectClusterLogicAuthEnum authEnum) {
        // 新增权限检查
        Result<Void> judgeResult = validateProjectIdIsNull(projectId, logicClusterId);
        if (judgeResult.failed()) {
            return judgeResult;
        }

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByIdAndProjectId(logicClusterId, projectId);
        if (AriusObjUtils.isNull(clusterLogic)) {
            return Result.buildParamIllegal(String.format("逻辑集群[%d]不存在", logicClusterId));
        }

        if (AriusObjUtils.isNull(authDTO.getType())) {
            return Result.buildParamIllegal("权限类型为空");
        }

        // 重复添加不做幂等，抛出错误
        if (null != logicClusterAuthDAO.getByProjectIdAndLogicClusterId(projectId, logicClusterId)) {
            return Result.buildDuplicate("权限已存在");
        }

        // APP是逻辑集群的owner，无需添加
        if (clusterLogic.getProjectId().equals(projectId) && authEnum == ProjectClusterLogicAuthEnum.OWN) {
            return Result.buildDuplicate(String.format("APP[%d]已有管理权限", projectId));
        }
        return Result.buildSucc();
    }

    private Result<Void> isIllegal(ProjectLogicClusterAuthDTO authDTO, ProjectClusterLogicAuthEnum authEnum) {
        if (ProjectClusterLogicAuthEnum.NO_PERMISSIONS == authEnum) {
            // 不应该走到这一步，防御编码
            return Result.buildParamIllegal("无权限无需添加");
        }

        // 不能添加管理权限
        if (ProjectClusterLogicAuthEnum.ALL == authEnum) {
            return Result.buildParamIllegal("不支持添加超管权限");
        }
        return Result.buildSucc();
    }

    private Result<Void> validateProjectIdIsNull(Integer projectId, Long logicClusterId) {
        if (AriusObjUtils.isNull(projectId)) {
            return Result.buildParamIllegal("projectId为空");
        }

        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("project [%d]不存在", projectId));
        }

        if (AriusObjUtils.isNull(logicClusterId)) {
            return Result.buildParamIllegal("逻辑集群ID为空");
        }
        return Result.buildSucc();
    }

    /**
     * 由逻辑集群记录构建owner APP的权限数据
     * @param clusterLogic 逻辑集群记录
     */
    private ProjectClusterLogicAuth buildLogicClusterAuth(ClusterLogic clusterLogic,
                                                          ProjectClusterLogicAuthEnum projectClusterLogicAuthEnum) {
        if (clusterLogic == null) {
            return null;
        }
        ProjectClusterLogicAuth appLogicClusterAuth = new ProjectClusterLogicAuth();
        appLogicClusterAuth.setId(null);
        appLogicClusterAuth.setProjectId(clusterLogic.getProjectId());
        appLogicClusterAuth.setLogicClusterId(clusterLogic.getId());
        appLogicClusterAuth.setType(projectClusterLogicAuthEnum.getCode());
        return appLogicClusterAuth;
    }

    /**
     * 修改权限 可以修改权限类型和责任人 不校验参数
     * @param authDTO  参数
     * @param operator 操作人
     * @return result
     */
    private Result<Void> updateLogicClusterAuthWithoutCheck(ProjectLogicClusterAuthDTO authDTO, String operator) {

        ProjectClusterLogicAuthPO oldAuthPO = logicClusterAuthDAO.getById(authDTO.getId());
        ProjectClusterLogicAuthPO newAuthPO = ConvertUtil.obj2Obj(authDTO, ProjectClusterLogicAuthPO.class);
        boolean succeed = 1 == logicClusterAuthDAO.update(newAuthPO);
        if (succeed) {
            SpringTool.publish(new ProjectLogicClusterAuthEditEvent(this,
                ConvertUtil.obj2Obj(oldAuthPO, ProjectClusterLogicAuth.class),
                ConvertUtil.obj2Obj(logicClusterAuthDAO.getById(authDTO.getId()), ProjectClusterLogicAuth.class)));
            operateRecordService.saveOperateRecordWithManualTrigger(JSON.toJSONString(newAuthPO), operator,
                    authDTO.getProjectId(), oldAuthPO.getId(), OperateTypeEnum.MY_CLUSTER_INFO_MODIFY);
        }

        return Result.build(succeed);
    }
}