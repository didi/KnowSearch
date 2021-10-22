package com.didichuxing.datachannel.arius.admin.core.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppTemplateAuthPO;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppTemplateAuthAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppTemplateAuthDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppTemplateAuthEditEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.component.ResponsibleConvertTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicClusterAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppTemplateAuthDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author d06679
 * @date 2019/4/16
 */
@Service
public class AppLogicTemplateAuthServiceImpl implements AppLogicTemplateAuthService {

    private static final ILog          LOGGER = LogFactory.getLog(AppLogicTemplateAuthServiceImpl.class);

    @Autowired
    private AppTemplateAuthDAO         templateAuthDAO;

    @Autowired
    private AppService                 appService;

    @Autowired
    private TemplateLogicService       templateLogicService;

    @Autowired
    private AriusUserInfoService       ariusUserInfoService;

    @Autowired
    private ResponsibleConvertTool     responsibleConvertTool;

    @Autowired
    private AppLogicClusterAuthService logicClusterAuthService;

    @Autowired
    private OperateRecordService       operateRecordService;

    /**
     * Check是否删除多余的模板权限记录。
     * @param shouldDeleteFlags 是否删除
     * @return
     */
    @Override
    public boolean deleteExcessTemplateAuthsIfNeed(boolean shouldDeleteFlags) {
        Map<Integer, IndexTemplateLogic> logicTemplateId2LogicTemplateMappings = ConvertUtil
            .list2Map(templateLogicService.getLogicTemplatesWithCache(), IndexTemplateLogic::getId);

        Multimap<Integer, AppTemplateAuthPO> appId2TemplateAuthsMappings = ConvertUtil
            .list2MulMap(templateAuthDAO.listWithRwAuths(), AppTemplateAuthPO::getAppId);

        Map<Long, AppTemplateAuthPO> needDeleteTemplateAuths = Maps.newHashMap();

        for (Integer appId : appId2TemplateAuthsMappings.keySet()) {
            List<AppTemplateAuthPO> appTemplateAuths = Lists.newArrayList(appId2TemplateAuthsMappings.get(appId));

            Multimap<Integer, AppTemplateAuthPO> currentAppLogicId2TemplateAuthsMappings = ConvertUtil
                .list2MulMap(appTemplateAuths, AppTemplateAuthPO::getTemplateId);

            for (Integer logicTemplateId : currentAppLogicId2TemplateAuthsMappings.keySet()) {
                List<AppTemplateAuthPO> currentLogicTemplateAuths = Lists
                    .newArrayList(currentAppLogicId2TemplateAuthsMappings.get(logicTemplateId));

                if (!logicTemplateId2LogicTemplateMappings.containsKey(logicTemplateId)) {
                    needDeleteTemplateAuths
                        .putAll(ConvertUtil.list2Map(currentLogicTemplateAuths, AppTemplateAuthPO::getId));

                    LOGGER.info("method=checkMeta||msg=templateDeleted||appId=={}||logicId={}", appId, logicTemplateId);

                } else if (appId.equals(logicTemplateId2LogicTemplateMappings.get(logicTemplateId).getAppId())) {
                    needDeleteTemplateAuths
                        .putAll(ConvertUtil.list2Map(currentLogicTemplateAuths, AppTemplateAuthPO::getId));

                    LOGGER.info("method=checkMeta||msg=appOwnTemplate||appId=={}||logicId={}", appId, logicTemplateId);
                } else {

                    if (currentLogicTemplateAuths.size() == 1) {
                        continue;
                    }

                    currentLogicTemplateAuths.sort(Comparator.comparing(AppTemplateAuthPO::getType));

                    needDeleteTemplateAuths.putAll(
                        ConvertUtil.list2Map(currentLogicTemplateAuths.subList(1, currentLogicTemplateAuths.size()),
                            AppTemplateAuthPO::getId));

                    LOGGER.info("method=checkMeta||msg=appHasMultiTemplateAuth||appId=={}||logicId={}", appId,
                        logicTemplateId);
                }
            }
        }

        doDeleteOperationForNeed(needDeleteTemplateAuths.values(), shouldDeleteFlags);

        return true;
    }

    @Override
    public Result ensureSetLogicTemplateAuth(Integer appId, Integer logicTemplateId, AppTemplateAuthEnum auth,
                                             String responsible, String operator) {
        // 参数检查
        if (appId == null) {
            return Result.buildParamIllegal("未指定appId");
        }

        if (logicTemplateId == null) {
            return Result.buildParamIllegal("未指定逻辑模板ID");
        }

        if (StringUtils.isBlank(operator)) {
            return Result.buildParamIllegal("未指定操作人");
        }

        // 获取权限表中已经存在的权限¬记录
        AppTemplateAuthPO oldAuthPO = templateAuthDAO.getByAppIdAndTemplateId(appId, String.valueOf(logicTemplateId));

        if (oldAuthPO == null) {
            /*---------------之前无权限---------------*/
            // NO_PERMISSIONS不需添加
            if (auth == null || auth == AppTemplateAuthEnum.NO_PERMISSION) {
                return Result.buildSucc();
            }

            // 新增
            return addTemplateAuth(new AppTemplateAuthDTO(null, appId, logicTemplateId, auth.getCode(), responsible),
                operator);
        } else {
            /*---------------有权限记录---------------*/
            // 期望删除权限
            if (auth == AppTemplateAuthEnum.NO_PERMISSION) {
                return deleteTemplateAuth(oldAuthPO.getId(), operator);
            }

            // 期望更新权限信息
            AppTemplateAuthDTO newAuthDTO = new AppTemplateAuthDTO(oldAuthPO.getId(), null, null,
                auth == null ? null : auth.getCode(), StringUtils.isBlank(responsible) ? null : responsible);
            return updateTemplateAuth(newAuthDTO, operator);
        }
    }

    /**
     * 获取APP有权限的逻辑模板权限点（包括模板所属APP的OWN权限以及添加的R/RW权限）
     * @param appId APP ID
     * @return 模板权限
     */
    @Override
    public List<AppTemplateAuth> getTemplateAuthsByAppId(Integer appId) {
        // app不存在返回空list
        if (appId == null) {
            return new ArrayList<>();
        }

        App app = appService.getAppById(appId);
        if (app == null) {
            return new ArrayList<>();
        }

        // 从权限表获取的权限
        List<AppTemplateAuth> appTemplateRWAuths = getAppActiveTemplateRWAuths(appId);

        // 从逻辑模板表创建信息获取的own权限
        List<AppTemplateAuth> appTemplateOwnerAuths = getAppTemplateOwnerAuths(app);

        return mergeAppTemplateAuths(appTemplateRWAuths, appTemplateOwnerAuths);
    }

    /**
     * 获取所有APP的权限
     * @return map, key为appId，value为app拥有的权限点集合
     */
    @Override
    public Map<Integer, Collection<AppTemplateAuth>> getAllAppTemplateAuths() {

        List<AppTemplateAuth> authTemplates = getAllAppsActiveTemplateRWAuths();
        authTemplates.addAll(getAllAppsActiveTemplateOwnerAuths());

        return ConvertUtil.list2MulMap(authTemplates, AppTemplateAuth::getAppId).asMap();
    }

    @Override
    public AppTemplateAuthEnum getAuthEnumByAppIdAndLogicId(Integer appId, Integer logicId) {
        if (appService.isSuperApp(appId)) {
            return AppTemplateAuthEnum.OWN;
        }

        for (AppTemplateAuth appTemplateAuth : getTemplateAuthsByLogicTemplateId(logicId)) {
            if (appId.equals(appTemplateAuth.getAppId())) {
                return AppTemplateAuthEnum.valueOf(appTemplateAuth.getType());
            }
        }

        return AppTemplateAuthEnum.NO_PERMISSION;
    }

    @Override
    public AppTemplateAuth getTemplateAuthByLogicTemplateIdAndAppId(Integer logicTemplateId, Integer appId) {
        return responsibleConvertTool.obj2Obj(
            templateAuthDAO.getByAppIdAndTemplateId(appId, String.valueOf(logicTemplateId)), AppTemplateAuth.class);
    }

    /**
     * 获取逻辑模板权限列表
     * @param logicTemplateId 逻辑模板ID
     * @return 模板权限
     */
    @Override
    public List<AppTemplateAuth> getTemplateAuthsByLogicTemplateId(Integer logicTemplateId) {
        return responsibleConvertTool.list2List(templateAuthDAO.listByLogicTemplateId(String.valueOf(logicTemplateId)),
            AppTemplateAuth.class);
    }

    /**
     * 增加权限
     * @param authDTO  权限信息
     * @param operator 操作人
     * @return result
     */
    @Override
    public Result addTemplateAuth(AppTemplateAuthDTO authDTO, String operator) {

        Result checkResult = validateTemplateAuth(authDTO, OperationEnum.ADD);
        if (checkResult.failed()) {
            LOGGER.warn("class=AppAuthServiceImpl||method=addTemplateAuth||msg={}||msg=check fail!",
                checkResult.getMessage());
            return checkResult;
        }

        return addTemplateAuthWithoutCheck(authDTO, operator);
    }

    /**
     * 修改权限 可以修改权限类型和责任人
     * @param authDTO  参数
     * @param operator 操作人
     * @return result
     */
    @Override
    public Result updateTemplateAuth(AppTemplateAuthDTO authDTO, String operator) {
        Result checkResult = validateTemplateAuth(authDTO, OperationEnum.EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=AppAuthServiceImpl||method=updateTemplateAuth||msg={}||msg=check fail!",
                checkResult.getMessage());
            return checkResult;
        }
        return updateTemplateAuthWithoutCheck(authDTO, operator);
    }

    /**
     * 删除模板权限
     * @param authId   APPID
     * @param operator 操作人
     * @return result
     */
    @Override
    public Result deleteTemplateAuth(Long authId, String operator) {

        AppTemplateAuthPO oldAuthPO = templateAuthDAO.getById(authId);
        if (oldAuthPO == null) {
            return Result.buildNotExist("权限不存在");
        }

        boolean succeed = 1 == templateAuthDAO.delete(authId);

        if (succeed) {
            SpringTool.publish(
                new AppTemplateAuthDeleteEvent(this, responsibleConvertTool.obj2Obj(oldAuthPO, AppTemplateAuth.class)));

            operateRecordService.save(ModuleEnum.LOGIC_TEMPLATE_PERMISSIONS, OperationEnum.DELETE, oldAuthPO.getId(),
                StringUtils.EMPTY, operator);
        }

        return Result.build(succeed);
    }

    /**************************************** private method ****************************************************/
    /**
     * 构建具备OWNER权限点的模板
     * @param logicTemplate 逻辑模板
     * @return
     */
    private AppTemplateAuth buildTemplateOwnerAuth(IndexTemplateLogic logicTemplate) {
        AppTemplateAuth auth = new AppTemplateAuth();
        auth.setAppId(logicTemplate.getAppId());
        auth.setTemplateId(logicTemplate.getId());
        auth.setType(AppTemplateAuthEnum.OWN.getCode());
        auth.setResponsible(logicTemplate.getResponsible());
        return auth;
    }

    /**
     * 修改权限 可以修改权限类型和责任人 不校验参数
     * @param authDTO  参数
     * @param operator 操作人
     * @return result
     */
    private Result updateTemplateAuthWithoutCheck(AppTemplateAuthDTO authDTO, String operator) {

        AppTemplateAuthPO oldAuthPO = templateAuthDAO.getById(authDTO.getId());
        AppTemplateAuthPO newAuthPO = responsibleConvertTool.obj2Obj(authDTO, AppTemplateAuthPO.class);

        boolean succeed = 1 == templateAuthDAO.update(newAuthPO);

        if (succeed) {
            SpringTool.publish(
                new AppTemplateAuthEditEvent(this, responsibleConvertTool.obj2Obj(oldAuthPO, AppTemplateAuth.class),
                    responsibleConvertTool.obj2Obj(templateAuthDAO.getById(authDTO.getId()), AppTemplateAuth.class)));

            operateRecordService.save(ModuleEnum.LOGIC_TEMPLATE_PERMISSIONS, OperationEnum.EDIT, oldAuthPO.getId(),
                JSON.toJSONString(newAuthPO), operator);
        }

        return Result.build(succeed);
    }

    /**
     * 增加权限  不做参数校验
     * @param authDTO  权限信息
     * @param operator 操作人
     * @return result
     */
    private Result addTemplateAuthWithoutCheck(AppTemplateAuthDTO authDTO, String operator) {
        AppTemplateAuthPO authPO = responsibleConvertTool.obj2Obj(authDTO, AppTemplateAuthPO.class);

        boolean succeed = 1 == templateAuthDAO.insert(authPO);
        if (succeed) {
            // 发送消息
            SpringTool.publish(
                new AppTemplateAuthAddEvent(this, responsibleConvertTool.obj2Obj(authPO, AppTemplateAuth.class)));

            // 记录操作
            operateRecordService.save(ModuleEnum.LOGIC_TEMPLATE_PERMISSIONS, OperationEnum.ADD, authPO.getId(),
                JSON.toJSONString(authPO), operator);
        }

        return Result.build(succeed);
    }

    /**
     * 验证权限参数
     * @param authDTO   参数信息
     * @param operation 操作
     * @return result
     */
    private Result validateTemplateAuth(AppTemplateAuthDTO authDTO, OperationEnum operation) {
        if (!EnvUtil.isOnline()) {
            LOGGER.info("class=AppAuthServiceImpl||method=validateTemplateAuth||authDTO={}",
                JSON.toJSONString(authDTO));
        }

        if (authDTO == null) {
            return Result.buildParamIllegal("权限信息为空");
        }

        Integer appId = authDTO.getAppId();
        Integer logicTemplateId = authDTO.getTemplateId();
        AppTemplateAuthEnum authEnum = AppTemplateAuthEnum.valueOf(authDTO.getType());

        if (OperationEnum.ADD.equals(operation)) {
            // 新增权限检查
            if (AriusObjUtils.isNull(appId)) {
                return Result.buildParamIllegal("appId为空");
            }

            if (AriusObjUtils.isNull(appService.getAppById(appId))) {
                return Result.buildParamIllegal(String.format("app[%d]不存在", appId));
            }

            if (AriusObjUtils.isNull(logicTemplateId)) {
                return Result.buildParamIllegal("模板ID为空");
            }

            IndexTemplateLogic logicTemplate = templateLogicService.getLogicTemplateById(logicTemplateId);
            if (AriusObjUtils.isNull(logicTemplate)) {
                return Result.buildParamIllegal(String.format("逻辑模板[%d]不存在", logicTemplate));
            }

            if (AriusObjUtils.isNull(authDTO.getType())) {
                return Result.buildParamIllegal("权限类型为空");
            }

            if (AriusObjUtils.isNull(authDTO.getResponsible())) {
                return Result.buildParamIllegal("责任人为空");
            }

            // 重复添加不做幂等，抛出错误
            if (null != templateAuthDAO.getByAppIdAndTemplateId(appId, String.valueOf(logicTemplateId))) {
                return Result.buildNotExist("权限已存在");
            }

            // APP是逻辑模板的owner，无需添加
            if (logicTemplate.getAppId().equals(appId) && authEnum == AppTemplateAuthEnum.OWN) {
                return Result.buildDuplicate(String.format("APP[%d]已有管理权限", appId));
            }

            // 有集群权限才能新增索引权限
            ESClusterLogic esClusterLogic = templateLogicService
                .getLogicTemplateWithClusterAndMasterTemplate(logicTemplateId).getLogicCluster();
            if (logicClusterAuthService.getLogicClusterAuthEnum(appId,
                esClusterLogic.getId()) == AppLogicClusterAuthEnum.NO_PERMISSIONS) {
                return Result.buildOpForBidden("没有索引所在集群的权限");
            }

        } else if (OperationEnum.EDIT.equals(operation)) {
            // 更新权限检查
            if (AriusObjUtils.isNull(authDTO.getId())) {
                return Result.buildParamIllegal("权限ID为空");
            }

            if (null == templateAuthDAO.getById(authDTO.getId())) {
                return Result.buildNotExist("权限不存在");
            }
        }

        // 不能添加管理权限
        if (AppTemplateAuthEnum.OWN == authEnum) {
            return Result.buildParamIllegal("不支持添加管理权限");
        }

        // 校验责任人是否合法
        if (!AriusObjUtils.isNull(authDTO.getResponsible())) {
            if (AriusObjUtils.isNull(ariusUserInfoService.getByDomainAccount(authDTO.getResponsible()))) {
                return Result.buildParamIllegal("责任人非法");
            }
        }

        return Result.buildSucc();
    }

    /**
     * 获取所有APP具备OWNER权限模板权限点列表
     * @return
     */
    private List<AppTemplateAuth> getAllAppsActiveTemplateOwnerAuths() {
        List<IndexTemplateLogic> logicTemplates = templateLogicService.getLogicTemplatesWithCache();
        Map<Integer, App> appsMap = appService.getAppsMap();

        return logicTemplates
                .stream()
                .filter(indexTemplateLogic -> appsMap.containsKey(indexTemplateLogic.getAppId()))
                .map(this::buildTemplateOwnerAuth)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有应用具备RW权限点的模板权限列表
     * @return
     */
    private List<AppTemplateAuth> getAllAppsActiveTemplateRWAuths() {
        List<AppTemplateAuth> rwTemplateAuths = responsibleConvertTool.list2List(templateAuthDAO.listWithRwAuths(),
            AppTemplateAuth.class);

        // 过滤出active的逻辑模板的权限点
        Set<Integer> logicTemplateIds = getActiveTemplateIds();
        return rwTemplateAuths.stream().filter(authTemplate -> logicTemplateIds.contains(authTemplate.getTemplateId()))
            .collect(Collectors.toList());
    }

    /**
     * 获取APP具备OWN权限的模板权限列表
     * @param app APP
     * @return
     */
    private List<AppTemplateAuth> getAppTemplateOwnerAuths(App app) {
        if (app == null) {
            return new ArrayList<>();
        }

        // 获取APP作为创建者的模板
        List<IndexTemplateLogic> ownAuthTemplates = templateLogicService.getAppLogicTemplatesByAppId(app.getId());

        // 超级项目可以默认全部模板为own权限
        if (appService.isSuperApp(app.getId())) {
            ownAuthTemplates = templateLogicService.getAllLogicTemplates();
        }

        return ownAuthTemplates.stream().map(this::buildTemplateOwnerAuth).collect(Collectors.toList());
    }

    /**
     * 从权限表获取APP对active逻辑模板的读写权限点
     * @param appId APP ID
     * @return
     */
    private List<AppTemplateAuth> getAppActiveTemplateRWAuths(Integer appId) {
        // 从权限表获取读写权限
        List<AppTemplateAuth> authTemplates = responsibleConvertTool
            .list2List(templateAuthDAO.listWithRwAuthsByAppId(appId), AppTemplateAuth.class);

        // 获取有权限的且仍存在的逻辑模板，以逻辑模板id为key
        // todo : authService应该作为基础服务，不应该耦合上层逻辑，需要把排除操作上升到业务service
        // 现在业务service中逻辑比较多且复杂，后续理清后修改
        Set<Integer> logicTemplateIds = getActiveTemplateIds();

        // 过滤出active的逻辑模板的权限点
        return authTemplates.stream().filter(authTemplate -> logicTemplateIds.contains(authTemplate.getTemplateId()))
            .collect(Collectors.toList());
    }

    private Set<Integer> getActiveTemplateIds() {
        return templateLogicService.getLogicTemplatesWithCache().stream().map(IndexTemplateLogic::getId)
            .collect(Collectors.toSet());
    }

    /**
     * 模板权限列表
     * @param templateAuths 模板权限列表
     * @param deleteFlags   删除标示
     */
    private void doDeleteOperationForNeed(Collection<AppTemplateAuthPO> templateAuths, boolean deleteFlags) {
        if (CollectionUtils.isNotEmpty(templateAuths)) {
            for (AppTemplateAuthPO templateAuth : templateAuths) {
                if (deleteFlags) {
                    if (1 == templateAuthDAO.delete(templateAuth.getId())) {
                        LOGGER.info("method=checkMeta||msg=deleteTemplateAuthSucceed||authId={}", templateAuth.getId());
                    }
                } else {
                    LOGGER.info("method=checkMeta||msg=deleteCheck||authId={}", templateAuth.getId());
                }
            }
        }
    }

    private List<AppTemplateAuth> mergeAppTemplateAuths(List<AppTemplateAuth> appTemplateRWAuths,
                                                        List<AppTemplateAuth> appTemplateOwnerAuths) {
        List<Integer> templateId = appTemplateRWAuths.stream().map(AppTemplateAuth::getTemplateId)
            .collect(Collectors.toList());

        List<AppTemplateAuth> appTemplateAuths = appTemplateOwnerAuths.parallelStream()
            .filter(r -> !templateId.contains(r.getTemplateId())).collect(Collectors.toList());

        appTemplateRWAuths.forEach(r -> r.setType(AppTemplateAuthEnum.OWN.getCode()));

        appTemplateAuths.addAll(appTemplateRWAuths);

        return appTemplateAuths;
    }
}
