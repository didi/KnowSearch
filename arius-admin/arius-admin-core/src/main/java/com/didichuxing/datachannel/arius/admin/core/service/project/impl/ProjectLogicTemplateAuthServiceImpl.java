package com.didichuxing.datachannel.arius.admin.core.service.project.impl;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectTemplateAuthPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.event.auth.ProjectTemplateAuthAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.ProjectTemplateAuthDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.ProjectTemplateAuthEditEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.project.ProjectTemplateAuthDAO;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * @author d06679
 * @date 2019/4/16
 */
@Service
public class ProjectLogicTemplateAuthServiceImpl implements ProjectLogicTemplateAuthService {

    private static final ILog              LOGGER = LogFactory.getLog(ProjectLogicTemplateAuthServiceImpl.class);

    @Autowired
    private ProjectTemplateAuthDAO         templateAuthDAO;

    @Autowired
    private ProjectService                 projectService;

    @Autowired
    private IndexTemplateService           indexTemplateService;

    @Autowired
    private ProjectClusterLogicAuthService logicClusterAuthService;

    @Autowired
    private OperateRecordService           operateRecordService;
    private final Cache<String, Object> PROJECT_LOGIC_TEMPLATE_AUTH_CACHE = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();

    @Override
    public boolean deleteRedundancyTemplateAuths(boolean shouldDeleteFlags) {
        Map<Integer, IndexTemplate> logicTemplateId2LogicTemplateMappings = ConvertUtil
            .list2Map(indexTemplateService.listAllLogicTemplates(), IndexTemplate::getId);

        Multimap<Integer, ProjectTemplateAuthPO> projectId2TemplateAuthsMappings = ConvertUtil
            .list2MulMap(templateAuthDAO.listWithRwAuths(), ProjectTemplateAuthPO::getProjectId);

        Map<Long, ProjectTemplateAuthPO> needDeleteTemplateAuths = Maps.newHashMap();

        for (Integer projectId : projectId2TemplateAuthsMappings.keySet()) {
            List<ProjectTemplateAuthPO> appTemplateAuths = Lists
                .newArrayList(projectId2TemplateAuthsMappings.get(projectId));

            Multimap<Integer, ProjectTemplateAuthPO> currentProjectLogicId2TemplateAuthsMappings = ConvertUtil
                .list2MulMap(appTemplateAuths, ProjectTemplateAuthPO::getTemplateId);

            for (Integer logicTemplateId : currentProjectLogicId2TemplateAuthsMappings.keySet()) {
                List<ProjectTemplateAuthPO> currentLogicTemplateAuths = Lists
                    .newArrayList(currentProjectLogicId2TemplateAuthsMappings.get(logicTemplateId));

                if (!logicTemplateId2LogicTemplateMappings.containsKey(logicTemplateId)) {
                    needDeleteTemplateAuths
                        .putAll(ConvertUtil.list2Map(currentLogicTemplateAuths, ProjectTemplateAuthPO::getId));

                    LOGGER.info(
                        "class=AppLogicTemplateAuthServiceImpl||method=checkMeta||msg=templateDeleted||projectId=={}||logicId={}",
                        projectId, logicTemplateId);

                } else if (projectId
                    .equals(logicTemplateId2LogicTemplateMappings.get(logicTemplateId).getProjectId())) {
                    needDeleteTemplateAuths
                        .putAll(ConvertUtil.list2Map(currentLogicTemplateAuths, ProjectTemplateAuthPO::getId));

                    LOGGER.info(
                        "class=AppLogicTemplateAuthServiceImpl||method=checkMeta||msg=appOwnTemplate||projectId=={}||logicId={}",
                        projectId, logicTemplateId);
                } else {

                    if (currentLogicTemplateAuths.size() == 1) {
                        continue;
                    }

                    currentLogicTemplateAuths.sort(Comparator.comparing(ProjectTemplateAuthPO::getType));

                    needDeleteTemplateAuths.putAll(
                        ConvertUtil.list2Map(currentLogicTemplateAuths.subList(1, currentLogicTemplateAuths.size()),
                            ProjectTemplateAuthPO::getId));

                    LOGGER.info(
                        "class=AppLogicTemplateAuthServiceImpl||method=checkMeta||msg=appHasMultiTemplateAuth||projectId=={}||logicId={}",
                        projectId, logicTemplateId);
                }
            }
        }

        doDeleteOperationForNeed(needDeleteTemplateAuths.values(), shouldDeleteFlags);

        return true;
    }

    @Override
    public Result<Void> ensureSetLogicTemplateAuth(Integer projectId, Integer logicTemplateId,
                                                   ProjectTemplateAuthEnum auth, String operator) {
        // 参数检查
        if (projectId == null) {
            return Result.buildParamIllegal("未指定projectId");
        }

        if (logicTemplateId == null) {
            return Result.buildParamIllegal("未指定逻辑模板ID");
        }

        if (StringUtils.isBlank(operator)) {
            return Result.buildParamIllegal("未指定操作人");
        }

        // 获取权限表中已经存在的权限¬记录
        ProjectTemplateAuthPO oldAuthPO = templateAuthDAO.getByProjectIdAndTemplateId(projectId,
            String.valueOf(logicTemplateId));

        if (oldAuthPO == null) {
            // 之前无权限
            // NO_PERMISSIONS不需添加
            if (auth == null || auth == ProjectTemplateAuthEnum.NO_PERMISSION) {
                return Result.buildSucc();
            }

            // 新增
            return addTemplateAuth(
                new ProjectTemplateAuthDTO(null, projectId, logicTemplateId, auth.getCode()));
        } else {
            // 有权限记录
            // 期望删除权限
            if (auth == ProjectTemplateAuthEnum.NO_PERMISSION) {
                return deleteTemplateAuth(oldAuthPO.getId());
            }

            // 期望更新权限信息
            ProjectTemplateAuthDTO newAuthDTO = new ProjectTemplateAuthDTO(oldAuthPO.getId(), null, null,
                auth == null ? null : auth.getCode());
            return updateTemplateAuth(newAuthDTO);
        }
    }

    /**
     * 获取APP有权限的逻辑模板权限点（包括模板所属APP的OWN权限以及添加的R/RW权限）
     * @param projectId APP ID
     * @return 模板权限
     */
    @Override
    public List<ProjectTemplateAuth> getTemplateAuthsByProjectId(Integer projectId) {
        if (!projectService.checkProjectExist(projectId)) {
            return Lists.newArrayList();
        }

        //超级项目拥有所有模板own权限
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            List<IndexTemplate> allLogicTemplates = indexTemplateService.listAllLogicTemplates();
            return allLogicTemplates.stream().map(r -> buildTemplateAuth(r, ProjectTemplateAuthEnum.OWN))
                .collect(Collectors.toList());
        }

        // 从权限表获取的权限
        List<ProjectTemplateAuth> projectTemplateRWAndRAuths = getProjectActiveTemplateRWAndRAuths(projectId);

        // 从逻辑模板表创建信息获取的own权限
        List<ProjectTemplateAuth> projectTemplateOwnerAuths = getAppTemplateOwnerAuths(projectId);
        return mergeAppTemplateAuths(projectTemplateRWAndRAuths, projectTemplateOwnerAuths);
    }

    @Override
    public ProjectTemplateAuth getTemplateRWAuthByLogicTemplateIdAndProjectId(Integer logicTemplateId,
                                                                              Integer projectId) {
        return ConvertUtil.obj2Obj(
            templateAuthDAO.getByProjectIdAndTemplateId(projectId, String.valueOf(logicTemplateId)),
            ProjectTemplateAuth.class);
    }

    /**
     * 获取逻辑模板权限列表
     * @param logicTemplateId 逻辑模板ID
     * @return 模板权限
     */
    @Override
    public List<ProjectTemplateAuth> getTemplateAuthsByLogicTemplateId(Integer logicTemplateId) {
        return ConvertUtil.list2List(templateAuthDAO.listByLogicTemplateId(String.valueOf(logicTemplateId)),
            ProjectTemplateAuth.class);
    }

    /**
     * 增加权限
     *
     * @param authDTO 权限信息
     * @return result
     */
    @Override
    public Result<Void> addTemplateAuth(ProjectTemplateAuthDTO authDTO) {

        Result<Void> checkResult = validateTemplateAuth(authDTO, OperationEnum.ADD);
        if (checkResult.failed()) {
            LOGGER.warn("class=AppAuthServiceImpl||method=addTemplateAuth||msg={}||msg=check fail!",
                checkResult.getMessage());
            return checkResult;
        }

        return addTemplateAuthWithoutCheck(authDTO);
    }

    /**
     * 修改权限 可以修改权限类型和责任人
     *
     * @param authDTO 参数
     * @return result
     */
    @Override
    public Result<Void> updateTemplateAuth(ProjectTemplateAuthDTO authDTO) {
        Result<Void> checkResult = validateTemplateAuth(authDTO, OperationEnum.EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=AppAuthServiceImpl||method=updateTemplateAuth||msg={}||msg=check fail!",
                checkResult.getMessage());
            return checkResult;
        }
        return updateTemplateAuthWithoutCheck(authDTO);
    }

    /**
     * 删除模板权限
     *
     * @param authId 主键
     * @return result
     */
    @Override
    public Result<Void> deleteTemplateAuth(Long authId) {

        ProjectTemplateAuthPO oldAuthPO = templateAuthDAO.getById(authId);
        if (oldAuthPO == null) {
            return Result.buildNotExist("权限不存在");
        }

        boolean succeed = 1 == templateAuthDAO.delete(authId);

        if (succeed) {
            SpringTool.publish(
                new ProjectTemplateAuthDeleteEvent(this, ConvertUtil.obj2Obj(oldAuthPO, ProjectTemplateAuth.class)));

        }

        return Result.build(succeed);
    }

    @Override
    public Result<Void> deleteTemplateAuthByTemplateId(Integer templateId) {
        boolean succeed = false;
        try {
            List<ProjectTemplateAuthPO> oldAppTemplateAuthPO = templateAuthDAO.getByTemplateId(templateId);
            if (CollectionUtils.isEmpty(oldAppTemplateAuthPO)) {
                return Result.buildSucc();
            }

            List<Integer> oldTemplateIds = oldAppTemplateAuthPO.stream().map(ProjectTemplateAuthPO::getTemplateId)
                .collect(Collectors.toList());
            succeed = oldTemplateIds.size() == templateAuthDAO.batchDeleteByTemplateIds(oldTemplateIds);
            if (Boolean.FALSE.equals(succeed)) {
                LOGGER.error(
                    "class=AppLogicTemplateAuthServiceImpl||method=deleteTemplateAuthByTemplateId||delete infos failed");
            }
        } catch (Exception e) {
            LOGGER.error("class=AppLogicTemplateAuthServiceImpl||method=deleteTemplateAuthByTemplateId||errMsg={}",
                e.getMessage(), e);
        }

        return Result.build(succeed);
    }

    /**
     * @param authId
     * @return
     */
    @Override
    public Integer getProjectIdById(Long authId) {
        return templateAuthDAO.getProjectIdById(authId);
    }

    /**
     * 获取所有APP的权限
     * @return map, key为projectId，value为app拥有的权限点集合
     */
    @Override
    public Map<Integer, Collection<ProjectTemplateAuth>> getAllProjectTemplateAuths() {

        List<ProjectTemplateAuth> authTemplates = getAllAppsActiveTemplateRWAuths();
        authTemplates.addAll(getAllAppsActiveTemplateOwnerAuths());

        return ConvertUtil.list2MulMap(authTemplates, ProjectTemplateAuth::getProjectId).asMap();
    }
    
    @Override
    public Map<Integer, Collection<ProjectTemplateAuth>> getAllProjectTemplateAuthsWithCache() {
        try {
            return (Map<Integer, Collection<ProjectTemplateAuth>>) PROJECT_LOGIC_TEMPLATE_AUTH_CACHE.get("getAllProjectTemplateAuthsWithCache", this::getAllProjectTemplateAuths);
        } catch (ExecutionException e) {
            return getAllProjectTemplateAuths();
        }
    }
    
    @Override
    public ProjectTemplateAuthEnum getAuthEnumByProjectIdAndLogicId(Integer projectId, Integer logicId) {
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return ProjectTemplateAuthEnum.OWN;
        }

        for (ProjectTemplateAuth appTemplateAuth : getTemplateAuthsByLogicTemplateId(logicId)) {
            if (projectId.equals(appTemplateAuth.getProjectId())) {
                return ProjectTemplateAuthEnum.valueOf(appTemplateAuth.getType());
            }
        }

        return ProjectTemplateAuthEnum.NO_PERMISSION;
    }

    @Override
    public ProjectTemplateAuth buildTemplateAuth(IndexTemplate logicTemplate,
                                                 ProjectTemplateAuthEnum projectTemplateAuthEnum) {
        ProjectTemplateAuth auth = new ProjectTemplateAuth();
        auth.setProjectId(logicTemplate.getProjectId());
        auth.setTemplateId(logicTemplate.getId());
        auth.setType(projectTemplateAuthEnum.getCode());
        return auth;
    }

    /**************************************** private method ****************************************************/
    /**
     * 修改权限 可以修改权限类型和责任人 不校验参数
     * @param authDTO  参数
     * @param operator 操作人
     * @return result
     */
    private Result<Void> updateTemplateAuthWithoutCheck(ProjectTemplateAuthDTO authDTO) {

        ProjectTemplateAuthPO oldAuthPO = templateAuthDAO.getById(authDTO.getId());
        ProjectTemplateAuthPO newAuthPO = ConvertUtil.obj2Obj(authDTO, ProjectTemplateAuthPO.class);

        boolean succeed = 1 == templateAuthDAO.update(newAuthPO);

        if (succeed) {
            SpringTool.publish(
                new ProjectTemplateAuthEditEvent(this, ConvertUtil.obj2Obj(oldAuthPO, ProjectTemplateAuth.class),
                    ConvertUtil.obj2Obj(templateAuthDAO.getById(authDTO.getId()), ProjectTemplateAuth.class)));

        }

        return Result.build(succeed);
    }

    /**
     * 增加权限  不做参数校验
     * @param authDTO  权限信息
     * @return result
     */
    private Result<Void> addTemplateAuthWithoutCheck(ProjectTemplateAuthDTO authDTO) {
        ProjectTemplateAuthPO authPO = ConvertUtil.obj2Obj(authDTO, ProjectTemplateAuthPO.class);
        boolean succeed = 1 == templateAuthDAO.insert(authPO);
        if (succeed) {
            // 发送消息
            SpringTool
                .publish(new ProjectTemplateAuthAddEvent(this, ConvertUtil.obj2Obj(authPO, ProjectTemplateAuth.class)));
        }

        return Result.build(succeed);
    }

    /**
     * 验证权限参数
     * @param authDTO   参数信息
     * @param operation 操作
     * @return result
     */
    private Result<Void> validateTemplateAuth(ProjectTemplateAuthDTO authDTO, OperationEnum operation) {

        LOGGER.debug("class=AppAuthServiceImpl||method=validateTemplateAuth||authDTO={}",JSON.toJSONString(authDTO));


        if (authDTO == null) {
            return Result.buildParamIllegal("权限信息为空");
        }

        Integer projectId = authDTO.getProjectId();
        Integer logicTemplateId = authDTO.getTemplateId();
        ProjectTemplateAuthEnum authEnum = ProjectTemplateAuthEnum.valueOf(authDTO.getType());

        if (OperationEnum.ADD.equals(operation)) {
            Result<Void> result = handleAdd(authDTO, projectId, logicTemplateId, authEnum);
            if (result.failed()) {
                return result;
            }
        } else if (OperationEnum.EDIT.equals(operation)) {
            Result<Void> result = handleEdit(authDTO);
            if (result.failed()) {
                return result;
            }
        }

        // 不能添加管理权限
        if (ProjectTemplateAuthEnum.OWN == authEnum) {
            return Result.buildParamIllegal("不支持添加管理权限");
        }

        return Result.buildSucc();
    }

    private Result<Void> handleEdit(ProjectTemplateAuthDTO authDTO) {
        // 更新权限检查
        if (AriusObjUtils.isNull(authDTO.getId())) {
            return Result.buildParamIllegal("权限ID为空");
        }

        if (null == templateAuthDAO.getById(authDTO.getId())) {
            return Result.buildNotExist("权限不存在");
        }
        return Result.buildSucc();
    }

    private Result<Void> handleAdd(ProjectTemplateAuthDTO authDTO, Integer projectId, Integer logicTemplateId,
                                   ProjectTemplateAuthEnum authEnum) {
        // 新增权限检查
        if (AriusObjUtils.isNull(projectId)) {
            return Result.buildParamIllegal("projectId为空");
        }

        if (projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("project[%d]不存在", projectId));
        }

        if (AriusObjUtils.isNull(logicTemplateId)) {
            return Result.buildParamIllegal("模板ID为空");
        }

        IndexTemplate logicTemplate = indexTemplateService.getLogicTemplateById(logicTemplateId);
        if (AriusObjUtils.isNull(logicTemplate)) {
            return Result.buildParamIllegal(String.format("逻辑模板[%d]不存在", logicTemplateId));
        }

        if (AriusObjUtils.isNull(authDTO.getType())) {
            return Result.buildParamIllegal("权限类型为空");
        }

        // 重复添加不做幂等，抛出错误
        if (null != templateAuthDAO.getByProjectIdAndTemplateId(projectId, String.valueOf(logicTemplateId))) {
            return Result.buildNotExist("权限已存在");
        }

        // APP是逻辑模板的owner，无需添加
        if (logicTemplate.getProjectId().equals(projectId) && authEnum == ProjectTemplateAuthEnum.OWN) {
            return Result.buildDuplicate(String.format("APP[%d]已有管理权限", projectId));
        }

        // 有集群权限才能新增索引权限
        ClusterLogic clusterLogic = indexTemplateService.getLogicTemplateWithClusterAndMasterTemplate(logicTemplateId)
            .getLogicCluster();
        if (AriusObjUtils.isNull(clusterLogic) || logicClusterAuthService.getLogicClusterAuthEnum(projectId,
            clusterLogic.getId()) == ProjectClusterLogicAuthEnum.NO_PERMISSIONS) {
            return Result.buildOpForBidden("没有索引所在集群的权限");
        }
        return Result.buildSucc();
    }

    /**
     * 获取所有APP具备OWNER权限模板权限点列表
     * @return
     */
    private List<ProjectTemplateAuth> getAllAppsActiveTemplateOwnerAuths() {
        List<IndexTemplate> logicTemplates = indexTemplateService.listAllLogicTemplates();
        List<Integer> projectIds = projectService.getProjectBriefList().stream().map(ProjectBriefVO::getId)
            .collect(Collectors.toList());

        return logicTemplates.stream()
            .filter(indexTemplateLogic -> projectIds.contains(indexTemplateLogic.getProjectId()))
            .map(r -> buildTemplateAuth(r, ProjectTemplateAuthEnum.OWN)).collect(Collectors.toList());
    }

    /**
     * 获取所有应用具备RW权限点的模板权限列表
     * @return
     */
    private List<ProjectTemplateAuth> getAllAppsActiveTemplateRWAuths() {
        List<ProjectTemplateAuth> rwTemplateAuths = ConvertUtil.list2List(templateAuthDAO.listWithRwAuths(),
            ProjectTemplateAuth.class);

        // 过滤出active的逻辑模板的权限点
        Set<Integer> logicTemplateIds = indexTemplateService.listAllLogicTemplates().stream().map(IndexTemplate::getId)
            .collect(Collectors.toSet());
        return rwTemplateAuths.stream().filter(authTemplate -> logicTemplateIds.contains(authTemplate.getTemplateId()))
            .collect(Collectors.toList());
    }

    private List<ProjectTemplateAuth> getAppTemplateOwnerAuths(Integer projectId) {
        List<IndexTemplate> ownAuthTemplates = indexTemplateService.listProjectLogicTemplatesByProjectId(projectId);
        return ownAuthTemplates.stream().map(r -> buildTemplateAuth(r, ProjectTemplateAuthEnum.OWN))
            .collect(Collectors.toList());
    }

    @Override
    public List<ProjectTemplateAuth> getProjectActiveTemplateRWAndRAuths(Integer projectId) {
        return ConvertUtil.list2List(templateAuthDAO.listWithRwAuthsByProjectId(projectId), ProjectTemplateAuth.class);
    }

    @Override
    public List<ProjectTemplateAuth> getProjectTemplateRWAndRAuthsWithoutCodec(Integer projectId) {
        return ConvertUtil.list2List(templateAuthDAO.listWithRwAuthsByProjectId(projectId), ProjectTemplateAuth.class);
    }

    @Override
    public List<ProjectTemplateAuth> getProjectActiveTemplateRWAuths(Integer projectId) {
        ProjectTemplateAuthPO projectTemplateAuthPO = new ProjectTemplateAuthPO();
        projectTemplateAuthPO.setProjectId(projectId);
        projectTemplateAuthPO.setType(ProjectTemplateAuthEnum.RW.getCode());
        return ConvertUtil.list2List(templateAuthDAO.listByCondition(projectTemplateAuthPO), ProjectTemplateAuth.class);
    }

    @Override
    public List<ProjectTemplateAuth> getProjectActiveTemplateRAuths(Integer projectId) {
        ProjectTemplateAuthPO projectTemplateAuthPO = new ProjectTemplateAuthPO();
        projectTemplateAuthPO.setProjectId(projectId);
        projectTemplateAuthPO.setType(ProjectTemplateAuthEnum.R.getCode());
        return ConvertUtil.list2List(templateAuthDAO.listByCondition(projectTemplateAuthPO), ProjectTemplateAuth.class);
    }

    /**
     * 模板权限列表
     * @param templateAuths 模板权限列表
     * @param deleteFlags   删除标示
     */
    private void doDeleteOperationForNeed(Collection<ProjectTemplateAuthPO> templateAuths, boolean deleteFlags) {
        if (CollectionUtils.isNotEmpty(templateAuths)) {
            for (ProjectTemplateAuthPO templateAuth : templateAuths) {
                if (deleteFlags) {
                    if (1 == templateAuthDAO.delete(templateAuth.getId())) {
                        LOGGER.info(
                            "class=AppLogicTemplateAuthServiceImpl||method=checkMeta||msg=deleteTemplateAuthSucceed||authId={}",
                            templateAuth.getId());
                    }
                } else {
                    LOGGER.info("class=AppLogicTemplateAuthServiceImpl||method=checkMeta||msg=deleteCheck||authId={}",
                        templateAuth.getId());
                }
            }
        }
    }

    /**
     * 合并读写、读、管理权限
     * @param projectTemplateRWAuths       项目对模板有读写、读的权限信息列表
     * @param projectTemplateOwnerAuths    项目对模板有管理的权限信息列表
     * @return
     */
    private List<ProjectTemplateAuth> mergeAppTemplateAuths(List<ProjectTemplateAuth> projectTemplateRWAuths,
                                                            List<ProjectTemplateAuth> projectTemplateOwnerAuths) {
        List<ProjectTemplateAuth> mergeAppTemplateAuthList = Lists.newArrayList();
        List<Integer> appOwnTemplateId = projectTemplateOwnerAuths.stream().map(ProjectTemplateAuth::getTemplateId)
            .collect(Collectors.toList());

        //合并读写、读、管理权限
        for (ProjectTemplateAuth appTemplateRWAuth : projectTemplateRWAuths) {
            if (appOwnTemplateId.contains(appTemplateRWAuth.getTemplateId())) {
                continue;
            }
            mergeAppTemplateAuthList.add(appTemplateRWAuth);
        }

        mergeAppTemplateAuthList.addAll(projectTemplateOwnerAuths);
        return mergeAppTemplateAuthList;
    }
}