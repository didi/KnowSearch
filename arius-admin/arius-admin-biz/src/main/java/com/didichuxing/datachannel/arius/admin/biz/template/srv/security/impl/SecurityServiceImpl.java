package com.didichuxing.datachannel.arius.admin.biz.template.srv.security.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_SECURITY;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityRoleService;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityService;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityUserService;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.SecurityRoleAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 对于模板删除时权限数据没有清理，这块主要基于以下几个点的思考：
 *  1、基于大集群规划，这个功能不是强需求
 *  2、权限元数据不断累积，可以形成对引擎的压测，发生更多问题
 *  3、后续如果需要清理，建议通过定时任务清理，基于事件监听的处理逻辑只处理核心的必要的逻辑
 * @author didi
 */
@Service
public class SecurityServiceImpl extends BaseTemplateSrv implements SecurityService {

    private static final ILog           LOGGER = LogFactory.getLog(SecurityServiceImpl.class);

    private static final String AUTH_TYPE_NOT_EXISTS_TIPS = "authType[%d]不存在";

    private static final String PROJECT_ID_NOT_EXISTS_TIPS = "projectId[%d]不存在";

    @Autowired
    private SecurityUserService securityUserService;

    @Autowired
    private SecurityRoleService securityRoleService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Autowired
    private ClusterPhyService clusterPhyService;
    @Autowired
    private ESUserService esUserService;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_SECURITY;
    }

    /**
     * 为逻辑模板创建APP的指定权限
     *
     * @param projectId           APPID
     * @param logicTemplateId 逻辑模板
     * @param authType        权限
     * @param retryCount      重试次数
     * @return result
     */
    @Override
    public Result<Void> saveAppLogicTemplateAuth(Integer projectId, Integer logicTemplateId, Integer authType, int retryCount) {
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);

        if (CollectionUtils.isEmpty(templatePhysicals)) {
            LOGGER.warn("class=SecurityServiceImpl||method=newAppLogicTemplateAuth||logicTemplateId={}||msg=no physical template",
                logicTemplateId);
            return Result.buildNotExist("逻辑模板没有部署物理模板:" + logicTemplateId);
        }

        if (!projectService.checkProjectExist(projectId)) {
            LOGGER.warn("class=SecurityServiceImpl||method=newAppLogicTemplateAuth||appId={}||msg=appId not exist",
                    projectId);
            return Result.buildNotExist(String.format(PROJECT_ID_NOT_EXISTS_TIPS, projectId));
        }

        ProjectTemplateAuthEnum authEnum = ProjectTemplateAuthEnum.valueOf(authType);
        if (ProjectTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            LOGGER.warn("class=SecurityServiceImpl||method=newAppLogicTemplateAuth||authType={}||msg=authType not exist",
                    projectId);
            return Result.buildNotExist(String.format(AUTH_TYPE_NOT_EXISTS_TIPS, authType));
        }

        List<String> failMsgs = Lists.newArrayList();
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                Result<Void> result = doSaveProjectPhysicalTemplateAuth(templatePhysical, projectId, authType, retryCount);
                if (result.failed()) {
                    failMsgs.add("[" + templatePhysical.getId() + "]" + result.getMessage());
                }
            } catch (Exception e) {
                failMsgs.add(e.getMessage());
                LOGGER.error("class=SecurityServiceImpl||method=newAppLogicTemplateAuth||cluster={}||appId={}||authType={}||errMsg={}",
                    templatePhysical.getCluster(), projectId, authType, e.getMessage(), e);
            }
        }

        if (CollectionUtils.isEmpty(failMsgs)) {
            return Result.buildSucc();
        }

        return Result.buildFail(String.join(",", failMsgs));
    }

    /**
     * 为逻辑模板删除APP的指定权限
     *
     * @param projectId           APPID
     * @param logicTemplateId 逻辑模板
     * @param authType        权限
     * @param retryCount      重试次数
     * @return result
     */
    @Override
    public Result<Void> deleteProjectLogicTemplateAuth(Integer projectId, Integer logicTemplateId, Integer authType, int retryCount) {
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);

        if (CollectionUtils.isEmpty(templatePhysicals)) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteProjectLogicTemplateAuth||logicTemplateId={}||msg=no physical template",
                logicTemplateId);
            return Result.buildNotExist("逻辑模板没有部署物理模板:" + logicTemplateId);
        }

        if (!projectService.checkProjectExist(projectId)) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteProjectLogicTemplateAuth||appId={}||msg=appId not exist",
                    projectId);
            return Result.buildNotExist(String.format(PROJECT_ID_NOT_EXISTS_TIPS, projectId));
        }

        ProjectTemplateAuthEnum authEnum = ProjectTemplateAuthEnum.valueOf(authType);
        if (ProjectTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteProjectLogicTemplateAuth||authType={}||msg=authType not exist",
                    projectId);
            return Result.buildNotExist(String.format(AUTH_TYPE_NOT_EXISTS_TIPS, authType));
        }

        List<String> failMsgs = Lists.newArrayList();
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                Result<Void> result = doDeleteProjectPhysicalTemplateAuth(templatePhysical, projectId, authType, retryCount);
                if (result.failed()) {
                    failMsgs.add("[" + templatePhysical.getId() + "]" + result.getMessage());
                }
            } catch (Exception e) {
                failMsgs.add(e.getMessage());
                LOGGER.error("class=SecurityServiceImpl||method=deleteProjectLogicTemplateAuth||cluster={}||appId={}||authType={}||errMsg={}",
                    templatePhysical.getCluster(), projectId, authType, e.getMessage(), e);
            }
        }

        if (CollectionUtils.isEmpty(failMsgs)) {
            return Result.buildSucc();
        }

        return Result.buildFail(String.join(",", failMsgs));
    }

    /**
     * 修改逻辑模板的APPID
     *
     * @param logicTemplateId 逻辑模板
     * @param srcProjectId        源APP
     * @param tgtProjectId        现APP
     * @param retryCount      重试次数
     * @return result
     */
    @Override
    public Result<Void> editLogicTemplateOwnProject(Integer logicTemplateId, Integer srcProjectId, Integer tgtProjectId, int retryCount) {
        Result<Void> deleteResult = deleteProjectLogicTemplateAuth(srcProjectId, logicTemplateId, ProjectTemplateAuthEnum.OWN.getCode(),
            retryCount);
        LOGGER.info("class=SecurityServiceImpl||method=editLogicTemplateOwnProject||logicTemplateId={}||srcAppid={}||tgtAppid={}||msg={}",
            logicTemplateId, srcProjectId, tgtProjectId, deleteResult.getMessage());

        Result<Void> saveResult = saveAppLogicTemplateAuth(tgtProjectId, logicTemplateId, ProjectTemplateAuthEnum.OWN.getCode(),
            retryCount);
        LOGGER.info("class=SecurityServiceImpl||method=editLogicTemplateOwnProject||logicTemplateId={}||srcProjectId={}||tgtProjectId={}||msg={}",
            logicTemplateId, srcProjectId, tgtProjectId, saveResult.getMessage());

        return saveResult;
    }

    /**
     * 为物理模板创建APP的管理权限
     *
     * @param templatePhysical 模板信息
     * @param projectId            APPID
     * @param authType         权限
     * @param retryCount       重试次数
     * @return result
     */
    @Override
    public Result<Void> saveProjectPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer projectId, Integer authType,
                                                        int retryCount) throws ESOperateException {
        if (!projectService.checkProjectExist(projectId)) {
            LOGGER.warn("class=SecurityServiceImpl||method=saveProjectPhysicalTemplateAuth||projectId={}||msg=projectId not exist",
                    projectId);
            return Result.buildNotExist(String.format(PROJECT_ID_NOT_EXISTS_TIPS, projectId));
        }

        ProjectTemplateAuthEnum authEnum = ProjectTemplateAuthEnum.valueOf(authType);
        if (ProjectTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            LOGGER.warn("class=SecurityServiceImpl||method=saveProjectPhysicalTemplateAuth||authType={}||msg=authType not exist",
                    projectId);
            return Result.buildNotExist(String.format(AUTH_TYPE_NOT_EXISTS_TIPS, authType));
        }

        if (templatePhysical == null) {
            LOGGER.warn("class=SecurityServiceImpl||method=saveProjectPhysicalTemplateAuth||msg=templatePhysical is null",
                    projectId);
            return Result.buildNotExist("templatePhysical为空");
        }

        return doSaveProjectPhysicalTemplateAuth(templatePhysical, projectId, retryCount, retryCount);
    }

    /**
     * 删除物理模板的APP管理权限
     *
     * @param templatePhysical 模板信息
     * @param projectId            APPID
     * @param authType         权限
     * @param retryCount       重试次数
     * @return result
     */
    @Override
    public Result<Void> deleteProjectPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer projectId, Integer authType,
                                                          int retryCount) throws ESOperateException {

        if (!projectService.checkProjectExist(projectId)) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteProjectPhysicalTemplateAuth||projectId={}||msg=projectId not exist",
                    projectId);
            return Result.buildNotExist(String.format(PROJECT_ID_NOT_EXISTS_TIPS, projectId));
        }

        ProjectTemplateAuthEnum authEnum = ProjectTemplateAuthEnum.valueOf(authType);
        if (ProjectTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteProjectPhysicalTemplateAuth||authType={}||msg"
                        + "=authType "
                        + "not exist",
                    projectId);
            return Result.buildNotExist(String.format(AUTH_TYPE_NOT_EXISTS_TIPS, authType));
        }

        if (templatePhysical == null) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteProjectPhysicalTemplateAuth||msg=templatePhysical is "
                        + "null",
                    projectId);
            return Result.buildNotExist("templatePhysical为空");
        }

        return doDeleteProjectPhysicalTemplateAuth(templatePhysical, projectId, retryCount, retryCount);
    }

    /**
     * APP密码修改
     *
     * @param projectId      APPID
     * @param verifyCode 校验码
     * @param retryCount 重试次数
     * @return result
     */
    @Override
    public Result<Void> editProjectVerifyCode(Integer projectId, String verifyCode, int retryCount) {
        List<ClusterPhy> clusters = clusterPhyService.listAllClusters();

        List<String> failMsgs = Lists.newArrayList();
        for (ClusterPhy cluster : clusters) {
            if (!isTemplateSrvOpen(cluster.getCluster())) {
                continue;
            }

            try {
                Result<Boolean> result = securityUserService.changePasswordIfExist(cluster.getCluster(), getUserName(
                                projectId),
                    verifyCode, retryCount);
                if (result.failed()) {
                    failMsgs.add(result.getMessage());
                }
            } catch (Exception e) {
                failMsgs.add(e.getMessage());
                LOGGER.error("class=SecurityServiceImpl||method=editProjectVerifyCode||cluster={}||projectId={}||errMsg={}",
                        cluster,
                        projectId,
                    e.getMessage(), e);
            }
        }

        if (CollectionUtils.isEmpty(failMsgs)) {
            return Result.buildSucc();
        }

        return Result.buildFail(String.join(",", failMsgs));
    }

    /**
     * 元数据一致性保证
     *
     * @param cluster 集群
     * @return result
     */
    @Override
    public void checkMeta(String cluster) {
        if (!isTemplateSrvOpen(cluster)) {
            return;
        }

        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getNormalTemplateByCluster(cluster);
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return;
        }

        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                doCheckMeta(templatePhysical);
            } catch (Exception e) {
                LOGGER.error("class=SecurityServiceImpl||method=checkMeta||cluster={}||template={}||errMsg={}", cluster,
                    templatePhysical.getName(), e.getMessage(), e);
            }
        }
    }

    /**************************************** private method ****************************************************/
    private void doCheckMeta(IndexTemplatePhy templatePhysical) {
        IndexTemplate templateLogic = indexTemplateService
            .getLogicTemplateWithPhysicalsById(templatePhysical.getLogicId());
        checkTemplateOwnProject(templatePhysical, templateLogic.getProjectId());

        List<ProjectTemplateAuth> templateAuths = projectLogicTemplateAuthService
            .getTemplateAuthsByLogicTemplateId(templateLogic.getId());
        checkTemplateRWAuth(templatePhysical, templateAuths);
    }

    private void checkTemplateRWAuth(IndexTemplatePhy templatePhysical, List<ProjectTemplateAuth> templateAuths) {
        if (CollectionUtils.isEmpty(templateAuths)) {
            return;
        }

        for (ProjectTemplateAuth templateAuth : templateAuths) {
            String roleName = getRoleName(templatePhysical, templateAuth.getType());
            securityRoleService.ensureRoleExist(templatePhysical.getCluster(), roleName,
                templatePhysical.getExpression(), getRolePrivilegeSet(templateAuth.getType()));
            securityUserService.ensureUserHasAuth(templatePhysical.getCluster(), getUserName(templateAuth.getProjectId()),
                roleName, templateAuth.getProjectId());
        }
    }

    private void checkTemplateOwnProject(IndexTemplatePhy templatePhysical, Integer projectId) {
        String roleName = getRoleName(templatePhysical, ProjectTemplateAuthEnum.OWN.getCode());
        securityRoleService.ensureRoleExist(templatePhysical.getCluster(), roleName, templatePhysical.getExpression(),
            getRolePrivilegeSet(ProjectTemplateAuthEnum.OWN.getCode()));
        securityUserService.ensureUserHasAuth(templatePhysical.getCluster(), getUserName(projectId), roleName, projectId);
    }

    private Result<Void> doSaveProjectPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer projectId, Integer authType,
                                                           int retryCount) throws ESOperateException {

        if (!isTemplateSrvOpen(templatePhysical.getCluster())) {
            return Result.buildFail("[" + templatePhysical.getCluster() + "]不支持安全特性");
        }

        String roleName = getRoleName(templatePhysical, authType);

        if (StringUtils.isBlank(roleName)) {
            return Result.buildFail("权限类型非法");
        }
        Result<Boolean> createRoleResult = securityRoleService.createRoleIfAbsent(templatePhysical.getCluster(), roleName,
            templatePhysical.getExpression(), getRolePrivilegeSet(authType), retryCount);
        LOGGER.info("class=SecurityServiceImpl||method=doSaveProjectPhysicalTemplateAuth||cluster={}||roleName={}||msg"
                    + "={}",
            templatePhysical.getCluster(), roleName, createRoleResult.getMessage());
        if (createRoleResult.failed()) {
            return Result.buildFrom(createRoleResult);
        }

        Result<Boolean> saveUserResult = securityUserService.appendUserRoles(templatePhysical.getCluster(), getUserName(projectId),
            roleName, projectId, retryCount);
        LOGGER.info("class=SecurityServiceImpl||method=doSaveProjectPhysicalTemplateAuth||cluster={}||roleName"
                    + "={}||projectId={}||msg={}",
            templatePhysical.getCluster(), roleName, projectId, saveUserResult.getMessage());

        return Result.buildFrom(saveUserResult);

    }

    private Result<Void> doDeleteProjectPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer projectId, Integer authType,
                                                             int retryCount) throws ESOperateException {
        if (!isTemplateSrvOpen(templatePhysical.getCluster())) {
            return Result.buildFail("[" + templatePhysical.getCluster() + "]不支持安全特性");
        }

        String roleName = getRoleName(templatePhysical, authType);
        if (StringUtils.isBlank(roleName)) {
            return Result.buildFail("权限类型非法");
        }

        Result<Boolean> saveUserResult = securityUserService.deleteUserRoles(templatePhysical.getCluster(), getUserName(projectId),
            roleName, retryCount);

        LOGGER.info("class=SecurityServiceImpl||method=doDeleteProjectPhysicalTemplateAuth||cluster={}||roleName"
                    + "={}||projectId={}||msg={}",
            templatePhysical.getCluster(), roleName, projectId, saveUserResult.getMessage());

        return Result.buildFrom(saveUserResult);
    }

    private String getUserName(Integer projectId) {
        return "user_" + esUserService.getDefaultESUserByProject(projectId).getId();
    }

    private String getRoleName(IndexTemplatePhy template, Integer authType) {
        SecurityRoleAuthEnum securityRoleAuthEnum = SecurityRoleAuthEnum
            .valueByAuth(ProjectTemplateAuthEnum.valueOf(authType));
        if (securityRoleAuthEnum == null) {
            return null;
        }
        return template.getId() + "_" + template.getName() + "_" + securityRoleAuthEnum.getAuthName();
    }

    private Set<String> getRolePrivilegeSet(Integer authType) {
        SecurityRoleAuthEnum securityRoleAuthEnum = SecurityRoleAuthEnum
            .valueByAuth(ProjectTemplateAuthEnum.valueOf(authType));
        if (securityRoleAuthEnum == null) {
            return Sets.newHashSet();
        }
        return securityRoleAuthEnum.getPrivilegeSet();
    }
}