package com.didichuxing.datachannel.arius.admin.biz.template.srv.security.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_SECURITY;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityRoleService;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityService;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityUserService;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.SecurityRoleAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
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

    private static final String APP_ID_NOT_EXISTS_TIPS = "appId[%d]不存在";

    @Autowired
    private SecurityUserService securityUserService;

    @Autowired
    private SecurityRoleService securityRoleService;

    @Autowired
    private AppService                  appService;

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_SECURITY;
    }

    /**
     * 为逻辑模板创建APP的指定权限
     *
     * @param appId           APPID
     * @param logicTemplateId 逻辑模板
     * @param authType        权限
     * @param retryCount      重试次数
     * @return result
     */
    @Override
    public Result<Void> saveAppLogicTemplateAuth(Integer appId, Integer logicTemplateId, Integer authType, int retryCount) {
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);

        if (CollectionUtils.isEmpty(templatePhysicals)) {
            LOGGER.warn("class=SecurityServiceImpl||method=newAppLogicTemplateAuth||logicTemplateId={}||msg=no physical template",
                logicTemplateId);
            return Result.buildNotExist("逻辑模板没有部署物理模板:" + logicTemplateId);
        }

        if (!appService.isAppExists(appId)) {
            LOGGER.warn("class=SecurityServiceImpl||method=newAppLogicTemplateAuth||appId={}||msg=appId not exist", appId);
            return Result.buildNotExist(String.format(APP_ID_NOT_EXISTS_TIPS, appId));
        }

        AppTemplateAuthEnum authEnum = AppTemplateAuthEnum.valueOf(authType);
        if (AppTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            LOGGER.warn("class=SecurityServiceImpl||method=newAppLogicTemplateAuth||authType={}||msg=authType not exist", appId);
            return Result.buildNotExist(String.format(AUTH_TYPE_NOT_EXISTS_TIPS, authType));
        }

        List<String> failMsgs = Lists.newArrayList();
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                Result<Void> result = doSaveAppPhysicalTemplateAuth(templatePhysical, appId, authType, retryCount);
                if (result.failed()) {
                    failMsgs.add("[" + templatePhysical.getId() + "]" + result.getMessage());
                }
            } catch (Exception e) {
                failMsgs.add(e.getMessage());
                LOGGER.error("class=SecurityServiceImpl||method=newAppLogicTemplateAuth||cluster={}||appId={}||authType={}||errMsg={}",
                    templatePhysical.getCluster(), appId, authType, e.getMessage(), e);
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
     * @param appId           APPID
     * @param logicTemplateId 逻辑模板
     * @param authType        权限
     * @param retryCount      重试次数
     * @return result
     */
    @Override
    public Result<Void> deleteAppLogicTemplateAuth(Integer appId, Integer logicTemplateId, Integer authType, int retryCount) {
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);

        if (CollectionUtils.isEmpty(templatePhysicals)) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteAppLogicTemplateAuth||logicTemplateId={}||msg=no physical template",
                logicTemplateId);
            return Result.buildNotExist("逻辑模板没有部署物理模板:" + logicTemplateId);
        }

        if (!appService.isAppExists(appId)) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteAppLogicTemplateAuth||appId={}||msg=appId not exist", appId);
            return Result.buildNotExist(String.format(APP_ID_NOT_EXISTS_TIPS, appId));
        }

        AppTemplateAuthEnum authEnum = AppTemplateAuthEnum.valueOf(authType);
        if (AppTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteAppLogicTemplateAuth||authType={}||msg=authType not exist", appId);
            return Result.buildNotExist(String.format(AUTH_TYPE_NOT_EXISTS_TIPS, authType));
        }

        List<String> failMsgs = Lists.newArrayList();
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                Result<Void> result = doDeleteAppPhysicalTemplateAuth(templatePhysical, appId, authType, retryCount);
                if (result.failed()) {
                    failMsgs.add("[" + templatePhysical.getId() + "]" + result.getMessage());
                }
            } catch (Exception e) {
                failMsgs.add(e.getMessage());
                LOGGER.error("class=SecurityServiceImpl||method=deleteAppLogicTemplateAuth||cluster={}||appId={}||authType={}||errMsg={}",
                    templatePhysical.getCluster(), appId, authType, e.getMessage(), e);
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
     * @param srcAppId        源APP
     * @param tgtAppId        现APP
     * @param retryCount      重试次数
     * @return result
     */
    @Override
    public Result<Void> editLogicTemplateOwnApp(Integer logicTemplateId, Integer srcAppId, Integer tgtAppId, int retryCount) {
        Result<Void> deleteResult = deleteAppLogicTemplateAuth(srcAppId, logicTemplateId, AppTemplateAuthEnum.OWN.getCode(),
            retryCount);
        LOGGER.info("class=SecurityServiceImpl||method=editLogicTemplateOwnApp||logicTemplateId={}||srcAppid={}||tgtAppid={}||msg={}",
            logicTemplateId, srcAppId, tgtAppId, deleteResult.getMessage());

        Result<Void> saveResult = saveAppLogicTemplateAuth(tgtAppId, logicTemplateId, AppTemplateAuthEnum.OWN.getCode(),
            retryCount);
        LOGGER.info("class=SecurityServiceImpl||method=editLogicTemplateOwnApp||logicTemplateId={}||srcAppid={}||tgtAppid={}||msg={}",
            logicTemplateId, srcAppId, tgtAppId, saveResult.getMessage());

        return saveResult;
    }

    /**
     * 为物理模板创建APP的管理权限
     *
     * @param templatePhysical 模板信息
     * @param appId            APPID
     * @param authType         权限
     * @param retryCount       重试次数
     * @return result
     */
    @Override
    public Result<Void> saveAppPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer appId, Integer authType,
                                                    int retryCount) throws ESOperateException {
        if (!appService.isAppExists(appId)) {
            LOGGER.warn("class=SecurityServiceImpl||method=saveAppPhysicalTemplateAuth||appId={}||msg=appId not exist", appId);
            return Result.buildNotExist(String.format(APP_ID_NOT_EXISTS_TIPS, appId));
        }

        AppTemplateAuthEnum authEnum = AppTemplateAuthEnum.valueOf(authType);
        if (AppTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            LOGGER.warn("class=SecurityServiceImpl||method=saveAppPhysicalTemplateAuth||authType={}||msg=authType not exist", appId);
            return Result.buildNotExist(String.format(AUTH_TYPE_NOT_EXISTS_TIPS, authType));
        }

        if (templatePhysical == null) {
            LOGGER.warn("class=SecurityServiceImpl||method=saveAppPhysicalTemplateAuth||msg=templatePhysical is null", appId);
            return Result.buildNotExist("templatePhysical为空");
        }

        return doSaveAppPhysicalTemplateAuth(templatePhysical, appId, retryCount, retryCount);
    }

    /**
     * 删除物理模板的APP管理权限
     *
     * @param templatePhysical 模板信息
     * @param appId            APPID
     * @param authType         权限
     * @param retryCount       重试次数
     * @return result
     */
    @Override
    public Result<Void> deleteAppPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer appId, Integer authType,
                                                      int retryCount) throws ESOperateException {

        if (!appService.isAppExists(appId)) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteAppPhysicalTemplateAuth||appId={}||msg=appId not exist", appId);
            return Result.buildNotExist(String.format(APP_ID_NOT_EXISTS_TIPS, appId));
        }

        AppTemplateAuthEnum authEnum = AppTemplateAuthEnum.valueOf(authType);
        if (AppTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteAppPhysicalTemplateAuth||authType={}||msg=authType not exist", appId);
            return Result.buildNotExist(String.format(AUTH_TYPE_NOT_EXISTS_TIPS, authType));
        }

        if (templatePhysical == null) {
            LOGGER.warn("class=SecurityServiceImpl||method=deleteAppPhysicalTemplateAuth||msg=templatePhysical is null", appId);
            return Result.buildNotExist("templatePhysical为空");
        }

        return doDeleteAppPhysicalTemplateAuth(templatePhysical, appId, retryCount, retryCount);
    }

    /**
     * APP密码修改
     *
     * @param appId      APPID
     * @param verifyCode 校验码
     * @param retryCount 重试次数
     * @return result
     */
    @Override
    public Result<Void> editAppVerifyCode(Integer appId, String verifyCode, int retryCount) {
        List<ClusterPhy> clusters = clusterPhyService.listAllClusters();

        List<String> failMsgs = Lists.newArrayList();
        for (ClusterPhy cluster : clusters) {
            if (!isTemplateSrvOpen(cluster.getCluster())) {
                continue;
            }

            try {
                Result<Boolean> result = securityUserService.changePasswordIfExist(cluster.getCluster(), getUserName(appId),
                    verifyCode, retryCount);
                if (result.failed()) {
                    failMsgs.add(result.getMessage());
                }
            } catch (Exception e) {
                failMsgs.add(e.getMessage());
                LOGGER.error("class=SecurityServiceImpl||method=editAppVerifyCode||cluster={}||appId={}||errMsg={}", cluster, appId,
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
        checkTemplateOwnApp(templatePhysical, templateLogic.getAppId());

        List<AppTemplateAuth> templateAuths = appLogicTemplateAuthService
            .getTemplateAuthsByLogicTemplateId(templateLogic.getId());
        checkTemplateRWAuth(templatePhysical, templateAuths);
    }

    private void checkTemplateRWAuth(IndexTemplatePhy templatePhysical, List<AppTemplateAuth> templateAuths) {
        if (CollectionUtils.isEmpty(templateAuths)) {
            return;
        }

        for (AppTemplateAuth templateAuth : templateAuths) {
            String roleName = getRoleName(templatePhysical, templateAuth.getType());
            securityRoleService.ensureRoleExist(templatePhysical.getCluster(), roleName,
                templatePhysical.getExpression(), getRolePrivilegeSet(templateAuth.getType()));
            securityUserService.ensureUserHasAuth(templatePhysical.getCluster(), getUserName(templateAuth.getAppId()),
                roleName, templateAuth.getAppId());
        }
    }

    private void checkTemplateOwnApp(IndexTemplatePhy templatePhysical, Integer appId) {
        String roleName = getRoleName(templatePhysical, AppTemplateAuthEnum.OWN.getCode());
        securityRoleService.ensureRoleExist(templatePhysical.getCluster(), roleName, templatePhysical.getExpression(),
            getRolePrivilegeSet(AppTemplateAuthEnum.OWN.getCode()));
        securityUserService.ensureUserHasAuth(templatePhysical.getCluster(), getUserName(appId), roleName, appId);
    }

    private Result<Void> doSaveAppPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer appId, Integer authType,
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
        LOGGER.info("class=SecurityServiceImpl||method=doSaveAppPhysicalTemplateAuth||cluster={}||roleName={}||msg={}",
            templatePhysical.getCluster(), roleName, createRoleResult.getMessage());
        if (createRoleResult.failed()) {
            return Result.buildFrom(createRoleResult);
        }

        Result<Boolean> saveUserResult = securityUserService.appendUserRoles(templatePhysical.getCluster(), getUserName(appId),
            roleName, appId, retryCount);
        LOGGER.info("class=SecurityServiceImpl||method=doSaveAppPhysicalTemplateAuth||cluster={}||roleName={}||appid={}||msg={}",
            templatePhysical.getCluster(), roleName, appId, saveUserResult.getMessage());

        return Result.buildFrom(saveUserResult);

    }

    private Result<Void> doDeleteAppPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer appId, Integer authType,
                                                         int retryCount) throws ESOperateException {
        if (!isTemplateSrvOpen(templatePhysical.getCluster())) {
            return Result.buildFail("[" + templatePhysical.getCluster() + "]不支持安全特性");
        }

        String roleName = getRoleName(templatePhysical, authType);
        if (StringUtils.isBlank(roleName)) {
            return Result.buildFail("权限类型非法");
        }

        Result<Boolean> saveUserResult = securityUserService.deleteUserRoles(templatePhysical.getCluster(), getUserName(appId),
            roleName, retryCount);

        LOGGER.info("class=SecurityServiceImpl||method=doDeleteAppPhysicalTemplateAuth||cluster={}||roleName={}||appid={}||msg={}",
            templatePhysical.getCluster(), roleName, appId, saveUserResult.getMessage());

        return Result.buildFrom(saveUserResult);
    }

    private String getUserName(Integer appId) {
        return "user_" + appId;
    }

    private String getRoleName(IndexTemplatePhy template, Integer authType) {
        SecurityRoleAuthEnum securityRoleAuthEnum = SecurityRoleAuthEnum
            .valueByAuth(AppTemplateAuthEnum.valueOf(authType));
        if (securityRoleAuthEnum == null) {
            return null;
        }
        return template.getId() + "_" + template.getName() + "_" + securityRoleAuthEnum.getAuthName();
    }

    private Set<String> getRolePrivilegeSet(Integer authType) {
        SecurityRoleAuthEnum securityRoleAuthEnum = SecurityRoleAuthEnum
            .valueByAuth(AppTemplateAuthEnum.valueOf(authType));
        if (securityRoleAuthEnum == null) {
            return Sets.newHashSet();
        }
        return securityRoleAuthEnum.getPrivilegeSet();
    }
}