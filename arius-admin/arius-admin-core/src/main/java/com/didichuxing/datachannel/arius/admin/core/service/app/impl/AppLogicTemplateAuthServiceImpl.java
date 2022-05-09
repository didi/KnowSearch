package com.didichuxing.datachannel.arius.admin.core.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppTemplateAuthPO;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppTemplateAuthAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppTemplateAuthDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppTemplateAuthEditEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.component.ResponsibleConvertTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppTemplateAuthDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
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
    private IndexTemplateInfoService indexTemplateInfoService;

    @Autowired
    private AriusUserInfoService       ariusUserInfoService;

    @Autowired
    private ResponsibleConvertTool     responsibleConvertTool;

    @Autowired
    private AppClusterLogicAuthService logicClusterAuthService;

    @Autowired
    private OperateRecordService       operateRecordService;

    @Override
    public boolean deleteRedundancyTemplateAuths(boolean shouldDeleteFlags) {
        Map<Integer, IndexTemplateInfo> logicTemplateId2LogicTemplateMappings = ConvertUtil
            .list2Map(indexTemplateInfoService.getAllLogicTemplates(), IndexTemplateInfo::getId);

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

                    LOGGER.info("class=AppLogicTemplateAuthServiceImpl||method=checkMeta||msg=templateDeleted||appId=={}||logicId={}", appId, logicTemplateId);

                } else if (appId.equals(logicTemplateId2LogicTemplateMappings.get(logicTemplateId).getAppId())) {
                    needDeleteTemplateAuths
                        .putAll(ConvertUtil.list2Map(currentLogicTemplateAuths, AppTemplateAuthPO::getId));

                    LOGGER.info("class=AppLogicTemplateAuthServiceImpl||method=checkMeta||msg=appOwnTemplate||appId=={}||logicId={}", appId, logicTemplateId);
                } else {

                    if(currentLogicTemplateAuths.size() == 1) {
                        continue;
                    }

                    currentLogicTemplateAuths.sort(Comparator.comparing(AppTemplateAuthPO::getType));

                    needDeleteTemplateAuths.putAll(
                        ConvertUtil.list2Map(currentLogicTemplateAuths.subList(1, currentLogicTemplateAuths.size()),
                            AppTemplateAuthPO::getId));

                    LOGGER.info("class=AppLogicTemplateAuthServiceImpl||method=checkMeta||msg=appHasMultiTemplateAuth||appId=={}||logicId={}", appId,
                        logicTemplateId);
                }
            }
        }

        doDeleteOperationForNeed(needDeleteTemplateAuths.values(), shouldDeleteFlags);

        return true;
    }

    @Override
    public Result<Void> ensureSetLogicTemplateAuth(Integer appId, Integer logicTemplateId, AppTemplateAuthEnum auth,
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
            // 之前无权限
            // NO_PERMISSIONS不需添加
            if (auth == null || auth == AppTemplateAuthEnum.NO_PERMISSION) {
                return Result.buildSucc();
            }

            // 新增
            return addTemplateAuth(new AppTemplateAuthDTO(null, appId, logicTemplateId, auth.getCode(), responsible),
                operator);
        } else {
            // 有权限记录
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
        if (!appService.isAppExists(appId)) {
            return Lists.newArrayList();
        }

        //超级项目拥有所有模板own权限
        if (appService.isSuperApp(appId)) {
            List<IndexTemplateInfo> allLogicTemplates = indexTemplateInfoService.getAllLogicTemplates();
            return allLogicTemplates.stream().map(r -> buildTemplateAuth(r, AppTemplateAuthEnum.OWN))
                .collect(Collectors.toList());
        }

        // 从权限表获取的权限
        List<AppTemplateAuth> appTemplateRWAndRAuths = getAppActiveTemplateRWAndRAuths(appId);

        // 从逻辑模板表创建信息获取的own权限
        List<AppTemplateAuth> appTemplateOwnerAuths = getAppTemplateOwnerAuths(appId);
        return mergeAppTemplateAuths(appTemplateRWAndRAuths, appTemplateOwnerAuths);
    }

    @Override
    public AppTemplateAuth getTemplateRWAuthByLogicTemplateIdAndAppId(Integer logicTemplateId, Integer appId) {
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
    public Result<Void> addTemplateAuth(AppTemplateAuthDTO authDTO, String operator) {

        Result<Void> checkResult = validateTemplateAuth(authDTO, OperationEnum.ADD);
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
    public Result<Void> updateTemplateAuth(AppTemplateAuthDTO authDTO, String operator) {
        Result<Void> checkResult = validateTemplateAuth(authDTO, OperationEnum.EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=AppAuthServiceImpl||method=updateTemplateAuth||msg={}||msg=check fail!",
                checkResult.getMessage());
            return checkResult;
        }
        return updateTemplateAuthWithoutCheck(authDTO, operator);
    }

    /**
     * 删除模板权限
     * @param authId   主键
     * @param operator 操作人
     * @return result
     */
    @Override
    public Result<Void> deleteTemplateAuth(Long authId, String operator) {

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

    @Override
    public Result<Void> deleteTemplateAuthByTemplateId(Integer templateId, String operator) {
        boolean succeed = false;
        try {
            List<AppTemplateAuthPO> oldAppTemplateAuthPO = templateAuthDAO.getByTemplateId(templateId);
            if (CollectionUtils.isEmpty(oldAppTemplateAuthPO)) {
                return Result.buildSucc();
            }

            List<Integer> oldTemplateIds = oldAppTemplateAuthPO.stream().map(AppTemplateAuthPO::getTemplateId).collect(Collectors.toList());
            succeed = oldTemplateIds.size() == templateAuthDAO.batchDeleteByTemplateIds(oldTemplateIds);
            if (succeed) {
                operateRecordService.save(ModuleEnum.LOGIC_TEMPLATE_PERMISSIONS, OperationEnum.DELETE, templateId,
                        StringUtils.EMPTY, operator);
            } else {
                LOGGER.error("class=AppLogicTemplateAuthServiceImpl||method=deleteTemplateAuthByTemplateId||delete infos failed");
            }
        } catch (Exception e) {
            LOGGER.error("class=AppLogicTemplateAuthServiceImpl||method=deleteTemplateAuthByTemplateId||errMsg={}",
                    e.getMessage(), e);
        }

        return Result.build(succeed);
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
    public AppTemplateAuth buildTemplateAuth(IndexTemplateInfo logicTemplate, AppTemplateAuthEnum appTemplateAuthEnum) {
        AppTemplateAuth auth = new AppTemplateAuth();
        auth.setAppId(logicTemplate.getAppId());
        auth.setTemplateId(logicTemplate.getId());
        auth.setType(appTemplateAuthEnum.getCode());
        auth.setResponsible(logicTemplate.getResponsible());
        return auth;
    }

    /**************************************** private method ****************************************************/
    /**
     * 修改权限 可以修改权限类型和责任人 不校验参数
     * @param authDTO  参数
     * @param operator 操作人
     * @return result
     */
    private Result<Void> updateTemplateAuthWithoutCheck(AppTemplateAuthDTO authDTO, String operator) {

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
    private Result<Void> addTemplateAuthWithoutCheck(AppTemplateAuthDTO authDTO, String operator) {
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
    private Result<Void> validateTemplateAuth(AppTemplateAuthDTO authDTO, OperationEnum operation) {
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
            Result<Void> result = handleAdd(authDTO, appId, logicTemplateId, authEnum);
            if (result.failed()) {
                return result;
            }
        } else if (OperationEnum.EDIT.equals(operation)) {
            Result<Void> result = handleEdit(authDTO);
            if (result.failed()){
                return result;
            }
        }

        // 不能添加管理权限
        if (AppTemplateAuthEnum.OWN == authEnum) {
            return Result.buildParamIllegal("不支持添加管理权限");
        }

        // 校验责任人是否合法
        if (!AriusObjUtils.isNull(authDTO.getResponsible())
                && AriusObjUtils.isNull(ariusUserInfoService.getByDomainAccount(authDTO.getResponsible()))) {
            return Result.buildParamIllegal("责任人非法");
        }

        return Result.buildSucc();
    }

    private Result<Void> handleEdit(AppTemplateAuthDTO authDTO) {
        // 更新权限检查
        if (AriusObjUtils.isNull(authDTO.getId())) {
            return Result.buildParamIllegal("权限ID为空");
        }

        if (null == templateAuthDAO.getById(authDTO.getId())) {
            return Result.buildNotExist("权限不存在");
        }
        return Result.buildSucc();
    }

    private Result<Void> handleAdd(AppTemplateAuthDTO authDTO, Integer appId, Integer logicTemplateId, AppTemplateAuthEnum authEnum) {
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

        IndexTemplateInfo logicTemplate = indexTemplateInfoService.getLogicTemplateById(logicTemplateId);
        if (AriusObjUtils.isNull(logicTemplate)) {
            return Result.buildParamIllegal(String.format("逻辑模板[%d]不存在", logicTemplateId));
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
        ClusterLogic clusterLogic = indexTemplateInfoService
            .getLogicTemplateWithClusterAndMasterTemplate(logicTemplateId).getLogicCluster();
        if (AriusObjUtils.isNull(clusterLogic) || logicClusterAuthService.getLogicClusterAuthEnum(appId,
                clusterLogic.getId()) == AppClusterLogicAuthEnum.NO_PERMISSIONS) {
            return Result.buildOpForBidden("没有索引所在集群的权限");
        }
        return Result.buildSucc();
    }

    /**
     * 获取所有APP具备OWNER权限模板权限点列表
     * @return
     */
    private List<AppTemplateAuth> getAllAppsActiveTemplateOwnerAuths() {
        List<IndexTemplateInfo> logicTemplates = indexTemplateInfoService.getAllLogicTemplates();
        Map<Integer, App> appsMap = appService.getAppsMap();

        return logicTemplates
                .stream()
                .filter(indexTemplateLogic -> appsMap.containsKey(indexTemplateLogic.getAppId()))
                .map(r -> buildTemplateAuth(r, AppTemplateAuthEnum.OWN))
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
        Set<Integer> logicTemplateIds = indexTemplateInfoService.getAllLogicTemplates().stream().map(IndexTemplateInfo::getId)
                .collect(Collectors.toSet());
        return rwTemplateAuths.stream().filter(authTemplate -> logicTemplateIds.contains(authTemplate.getTemplateId()))
            .collect(Collectors.toList());
    }

    private List<AppTemplateAuth> getAppTemplateOwnerAuths(Integer appId) {
        List<IndexTemplateInfo> ownAuthTemplates = indexTemplateInfoService.getAppLogicTemplatesByAppId(appId);
        return ownAuthTemplates.stream().map(r -> buildTemplateAuth(r, AppTemplateAuthEnum.OWN))
            .collect(Collectors.toList());
    }

    @Override
    public List<AppTemplateAuth> getAppActiveTemplateRWAndRAuths(Integer appId) {
        return responsibleConvertTool
            .list2List(templateAuthDAO.listWithRwAuthsByAppId(appId), AppTemplateAuth.class);
    }

    @Override
    public List<AppTemplateAuth> getAppTemplateRWAndRAuthsWithoutCodecResponsible(Integer appId) {
        return ConvertUtil
                .list2List(templateAuthDAO.listWithRwAuthsByAppId(appId), AppTemplateAuth.class);
    }

    @Override
    public List<AppTemplateAuth> getAppActiveTemplateRWAuths(Integer appId) {
        AppTemplateAuthPO appTemplateAuthPO = new AppTemplateAuthPO();
        appTemplateAuthPO.setAppId(appId);
        appTemplateAuthPO.setType(AppTemplateAuthEnum.RW.getCode());
        return responsibleConvertTool
                .list2List(templateAuthDAO.listByCondition(appTemplateAuthPO), AppTemplateAuth.class);
    }

    @Override
    public List<AppTemplateAuth> getAppActiveTemplateRAuths(Integer appId) {
        AppTemplateAuthPO appTemplateAuthPO = new AppTemplateAuthPO();
        appTemplateAuthPO.setAppId(appId);
        appTemplateAuthPO.setType(AppTemplateAuthEnum.R.getCode());
        return responsibleConvertTool
                .list2List(templateAuthDAO.listByCondition(appTemplateAuthPO), AppTemplateAuth.class);
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
                        LOGGER.info("class=AppLogicTemplateAuthServiceImpl||method=checkMeta||msg=deleteTemplateAuthSucceed||authId={}", templateAuth.getId());
                    }
                } else {
                    LOGGER.info("class=AppLogicTemplateAuthServiceImpl||method=checkMeta||msg=deleteCheck||authId={}", templateAuth.getId());
                }
            }
        }
    }

    /**
     * 合并读写、读、管理权限
     * @param appTemplateRWAuths       项目对模板有读写、读的权限信息列表
     * @param appTemplateOwnerAuths    项目对模板有管理的权限信息列表
     * @return
     */
    private List<AppTemplateAuth> mergeAppTemplateAuths(List<AppTemplateAuth> appTemplateRWAuths,
                                                        List<AppTemplateAuth> appTemplateOwnerAuths) {
        List<AppTemplateAuth> mergeAppTemplateAuthList = Lists.newArrayList();
        List<Integer> appOwnTemplateId = appTemplateOwnerAuths.stream().map(AppTemplateAuth::getTemplateId)
            .collect(Collectors.toList());

        //合并读写、读、管理权限
        for (AppTemplateAuth appTemplateRWAuth : appTemplateRWAuths) {
            if (appOwnTemplateId.contains(appTemplateRWAuth.getTemplateId())) {
                continue;
            }
            mergeAppTemplateAuthList.add(appTemplateRWAuth);
        }

        mergeAppTemplateAuthList.addAll(appTemplateOwnerAuths);
        return mergeAppTemplateAuthList;
    }
}
