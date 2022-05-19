package com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.yesOrNo;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template.TemplateOperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.*;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateConfigPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.LevelEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.component.ResponsibleConvertTool;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateConfigDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateTypeDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

@Service
public class IndexTemplateServiceImpl implements IndexTemplateService {

    private static final ILog           LOGGER = LogFactory.getLog(IndexTemplateServiceImpl.class);

    @Autowired
    private IndexTemplateDAO indexTemplateDAO;

    @Autowired
    private IndexTemplateConfigDAO      indexTemplateConfigDAO;

    @Autowired
    private IndexTemplateTypeDAO        indexTemplateTypeDAO;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private AppService                  appService;

    @Autowired
    private AriusUserInfoService        ariusUserInfoService;

    @Autowired
    private ResponsibleConvertTool      responsibleConvertTool;

    @Autowired
    private ESIndexService              esIndexService;

    @Autowired
    private AppLogicTemplateAuthService logicTemplateAuthService;

    @Autowired
    private AppClusterLogicAuthService  logicClusterAuthService;

    @Autowired
    private ClusterLogicService         clusterLogicService;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Autowired
    private RegionRackService           regionRackService;

    private Cache<String, List<IndexTemplate>> templateListCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(10).build();

    /**
     * 条件查询
     *
     * @param param 条件
     * @return 逻辑模板列表
     */
    @Override
    public List<IndexTemplate> getLogicTemplates(IndexTemplateDTO param) {
        return responsibleConvertTool.list2List(
            indexTemplateDAO.listByCondition(responsibleConvertTool.obj2Obj(param, IndexTemplatePO.class)),
            IndexTemplate.class);
    }

    /**
     * 根据逻辑模板名称模糊查询
     *
     * @param param 模糊查询条件
     * @return
     */
    @Override
    public List<IndexTemplate> fuzzyLogicTemplatesByCondition(IndexTemplateDTO param) {
        return responsibleConvertTool.list2List(
            indexTemplateDAO.likeByCondition(responsibleConvertTool.obj2Obj(param, IndexTemplatePO.class)),
            IndexTemplate.class);
    }

    @Override
    public List<IndexTemplate> pagingGetLogicTemplatesByCondition(TemplateConditionDTO param) {
        String sortTerm = null == param.getSortTerm() ? SortConstant.ID : param.getSortTerm();
        String sortType = param.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        List<IndexTemplatePO> indexTemplatePOS = Lists.newArrayList();
        try {
            indexTemplatePOS = indexTemplateDAO.pagingByCondition(param.getName(),
                    param.getDataType(), param.getHasDCDR(), (param.getPage() - 1) * param.getSize(), param.getSize(), sortTerm, sortType);
        } catch (Exception e) {
            LOGGER.error("class=TemplateLogicServiceImpl||method=pagingGetLogicTemplatesByCondition||err={}",
                e.getMessage(), e);
        }

        return responsibleConvertTool.list2List(indexTemplatePOS, IndexTemplate.class);
    }

    @Override
    public List<IndexTemplate> pagingGetTemplateSrvByCondition(TemplateSrvQueryDTO param) {
        List<IndexTemplatePO> indexTemplatePOS = Lists.newArrayList();
        try {
            indexTemplatePOS = indexTemplateDAO.pagingByCondition(param.getName(), null, null,
                    (param.getPage() - 1) * param.getSize(), param.getSize(), SortConstant.ID, SortConstant.DESC);
        } catch (Exception e) {
            LOGGER.error("class=IndexTemplateServiceImpl||method=pagingGetTemplateSrvByCondition||err={}", e.getMessage(), e);
        }
        return responsibleConvertTool.list2List(indexTemplatePOS, IndexTemplate.class);
    }

    @Override
    public Long fuzzyLogicTemplatesHitByCondition(IndexTemplateDTO param) {
        return indexTemplateDAO
            .getTotalHitByCondition(responsibleConvertTool.obj2Obj(param, IndexTemplatePO.class));
    }

    /**
     * 根据名字查询
     *
     * @param templateName 模板名字
     * @return list
     */
    @Override
    public List<IndexTemplate> getLogicTemplateByName(String templateName) {
        return responsibleConvertTool.list2List(indexTemplateDAO.listByName(templateName),
            IndexTemplate.class);
    }

    /**
     * 查询指定的逻辑模板
     *
     * @param logicTemplateId 模板id
     * @return 模板信息  不存在返回null
     */
    @Override
    public IndexTemplate getLogicTemplateById(Integer logicTemplateId) {
        return responsibleConvertTool.obj2Obj(indexTemplateDAO.getById(logicTemplateId), IndexTemplate.class);
    }

    /**
     * 删除逻辑模板
     *
     * @param logicTemplateId  模板id
     * @param operator 操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delTemplate(Integer logicTemplateId, String operator) throws AdminOperateException {
        IndexTemplatePO oldPO = indexTemplateDAO.getById(logicTemplateId);
        if (oldPO == null) {
            return Result.buildNotExist(TEMPLATE_NOT_EXIST);
        }

        boolean succeed = (1 == indexTemplateDAO.delete(logicTemplateId));

        if (succeed) {
            Result<Void> deleteTemplateAuthResult = logicTemplateAuthService.deleteTemplateAuthByTemplateId(oldPO.getId(), AriusUser.SYSTEM.getDesc());
            if (deleteTemplateAuthResult.failed()) {
                throw new AdminOperateException("删除模板失败");
            } else {
                LOGGER.info("class=TemplateLogicServiceImpl||method=delTemplate||logicId={}||msg=deleteTemplateAuthByTemplateId succ", logicTemplateId);
            }

            Result<Void> result = indexTemplatePhyService.delTemplateByLogicId(logicTemplateId, operator);
            if (result.failed()) {
                throw new AdminOperateException("删除模板失败");
            } else {
                operateRecordService.save(TEMPLATE, DELETE, logicTemplateId, String.format("模板%d下线", logicTemplateId), operator);
                LOGGER.info("class=TemplateLogicServiceImpl||method=delTemplate||logicId={}||msg=delTemplateByLogicId succ", logicTemplateId);
            }

        } else {
            throw new AdminOperateException("删除模板失败");
        }
        return Result.buildSucc();
    }

    /**
     * 校验模板参数是否合法
     *
     * @param param     参数
     * @param operation 操作
     * @return result
     */
    @Override
    public Result<Void> validateTemplate(IndexTemplateDTO param, OperationEnum operation) {
        if (param == null) {
            return Result.buildParamIllegal("模板信息为空");
        }

        String dateFormatFinal = null;
        String expressionFinal = null;
        String nameFinal = null;
        String dateFieldFinal = null;

        if (ADD.equals(operation)) {
            Result<Void> result = validateAdd(param);
            if (result.failed()) {return result;}

            dateFormatFinal = StringUtils.isBlank(param.getDateFormat()) ? null : param.getDateFormat();
            expressionFinal = param.getExpression();
            nameFinal = param.getName();
            dateFieldFinal = param.getDateField();
        } else if (EDIT.equals(operation)) {
            if (AriusObjUtils.isNull(param.getId())) {
                return Result.buildParamIllegal("模板id为空");
            }

            IndexTemplatePO oldPO = indexTemplateDAO.getById(param.getId());
            if (oldPO == null) {
                return Result.buildNotExist(TEMPLATE_NOT_EXIST);
            }
            dateFormatFinal = getDateFormat(param, oldPO);
            expressionFinal = getExpression(param, oldPO);
            dateFieldFinal = getDateField(param, oldPO);
            nameFinal = oldPO.getName();
        }


        List<IndexTemplate> indexTemplateList = getLogicTemplateByName(param.getName());
        Result<Void> result = validateIndexTemplateLogicStep1(param, indexTemplateList);
        if (result.failed()) {return result;}

        result = validateIndexTemplateLogicStep2(param, dateFormatFinal, expressionFinal, nameFinal, dateFieldFinal);
        if (result.failed()) {return result;}

        return Result.buildSucc();
    }

    /**
     * 编辑逻辑模板
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editTemplate(IndexTemplateDTO param, String operator) throws AdminOperateException {
        Result<Void> checkResult = validateTemplate(param, EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=TemplateLogicServiceImpl||method=editTemplate||msg={}", checkResult.getMessage());
            return checkResult;
        }

        return editTemplateWithoutCheck(param, operator);
    }

    @Override
    public Result<Void> addTemplateWithoutCheck(IndexTemplateDTO param) throws AdminOperateException {
        IndexTemplatePO templatePO = responsibleConvertTool.obj2Obj(param, IndexTemplatePO.class);
        boolean succ;
        try {
            succ = (1 == indexTemplateDAO.insert(templatePO));
        } catch (DuplicateKeyException e) {
            LOGGER.warn("class=TemplateLogicServiceImpl||method=addTemplateWithoutCheck||errMsg={}", e.getMessage());
            throw new AdminOperateException(String.format("保存逻辑模板【%s】失败：模板名称已存在！", templatePO.getName()));
        }

        param.setId(templatePO.getId());
        return Result.build(succ);
    }

    /**
     * 获取模板配置信息
     *
     * @param logicTemplateId 模板id
     * @return 配置信息  不存在返回null
     */
    @Override
    public IndexTemplateConfig getTemplateConfig(Integer logicTemplateId) {
        return responsibleConvertTool.obj2Obj(indexTemplateConfigDAO.getByLogicId(logicTemplateId),
            IndexTemplateConfig.class);
    }

    /**
     * 更新模板配置
     *
     * @param configDTO 配置参数
     * @param operator  操作人
     * @return result
     */
    @Override
    public Result<Void> updateTemplateConfig(IndexTemplateConfigDTO configDTO, String operator) {
        Result<Void> checkResult = checkConfigParam(configDTO);
        if (checkResult.failed()) {
            LOGGER.warn("class=TemplateLogicServiceImpl||method=updateTemplateConfig||msg={}",
                checkResult.getMessage());
            return checkResult;
        }

        IndexTemplatePO oldPO = indexTemplateDAO.getById(configDTO.getLogicId());
        if (oldPO == null) {
            return Result.buildNotExist(TEMPLATE_NOT_EXIST);
        }

        boolean succ = 1 == indexTemplateConfigDAO
            .update(responsibleConvertTool.obj2Obj(configDTO, TemplateConfigPO.class));

        return Result.build(succ);
    }

    @Override
    public Result<Void> insertTemplateConfig(IndexTemplateConfig indexTemplateConfig) {
        return Result.build(1 == indexTemplateConfigDAO.insert(ConvertUtil.obj2Obj(indexTemplateConfig,TemplateConfigPO.class)));
    }

    /**
     * 更新模板配置
     *
     * @param logicTemplateId  logicId
     * @param factor   factor
     * @param operator 操作人
     */
    @Override
    public void upsertTemplateShardFactor(Integer logicTemplateId, Double factor, String operator) {
        IndexTemplateConfig templateConfig = getTemplateConfig(logicTemplateId);
        if (templateConfig == null) {
            TemplateConfigPO configPO = getDefaultTemplateConfig(logicTemplateId);
            configPO.setAdjustRackShardFactor(factor);
            Result.build(1 == indexTemplateConfigDAO.insert(configPO));
        } else {
            IndexTemplateConfigDTO param = new IndexTemplateConfigDTO();
            param.setLogicId(logicTemplateId);
            param.setAdjustRackShardFactor(factor);
            updateTemplateConfig(param, operator);
        }
    }

    /**
     * 更新模板配置
     *
     * @param logicTemplateId  logicId
     * @param factor   factor
     * @param operator 操作人
     */
    @Override
    public void updateTemplateShardFactorIfGreater(Integer logicTemplateId, Double factor, String operator) {
        IndexTemplateConfig templateConfig = getTemplateConfig(logicTemplateId);
        if (templateConfig == null) {
            TemplateConfigPO configPO = getDefaultTemplateConfig(logicTemplateId);
            configPO.setAdjustRackShardFactor(factor);
            Result.build(1 == indexTemplateConfigDAO.insert(configPO));
            return;
        } else if (templateConfig.getAdjustRackShardFactor() < factor) {
            IndexTemplateConfigDTO param = new IndexTemplateConfigDTO();
            param.setLogicId(logicTemplateId);
            param.setAdjustRackShardFactor(factor);
            updateTemplateConfig(param, operator);
            return;
        }
        Result.buildSucc();
    }

    /**
     * 判断模板是否存在
     *
     * @param logicTemplateId 模板id
     * @return true/false
     */
    @Override
    public boolean exist(Integer logicTemplateId) {
        return indexTemplateDAO.getById(logicTemplateId) != null;
    }

    /**
     * 获取平台所有的模板
     *
     * @return map
     */
    @Override
    public Map<Integer, IndexTemplate> getAllLogicTemplatesMap() {
        return getAllLogicTemplates().stream()
            .collect(Collectors.toMap(IndexTemplate::getId, indexTemplateLogic -> indexTemplateLogic));
    }

    @Override
    public List<IndexTemplate> getLogicTemplatesByIds(List<Integer> logicTemplateIds) {
        if (CollectionUtils.isEmpty(logicTemplateIds)) {
            return new ArrayList<>();
        }

        return responsibleConvertTool.list2List(indexTemplateDAO.listByIds(logicTemplateIds),
            IndexTemplate.class);
    }

    @Override
    public Map<Integer, IndexTemplate> getLogicTemplatesMapByIds(List<Integer> logicTemplateIds) {
        return getLogicTemplatesByIds(logicTemplateIds).stream()
            .collect(Collectors.toMap(IndexTemplate::getId, indexTemplateLogic -> indexTemplateLogic));
    }

    /**
     * 根据APP ID查询模板
     *
     * @param appId APP ID
     * @return list
     */
    @Override
    public List<IndexTemplate> getAppLogicTemplatesByAppId(Integer appId) {
        return responsibleConvertTool.list2List(indexTemplateDAO.listByAppId(appId), IndexTemplate.class);
    }

    /**
     * 根据逻辑集群获取所有的逻辑模板
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    @Override
    public List<IndexTemplate> getLogicClusterTemplates(Long logicClusterId) {
        List<IndexTemplate> logicTemplates = Lists.newArrayList();

        if (logicClusterId != null) {
            List<IndexTemplateWithCluster> indexTemplateLogicWithClusters = getLogicTemplateWithClustersByClusterId(
                logicClusterId);
            logicTemplates = ConvertUtil.list2List(indexTemplateLogicWithClusters, IndexTemplate.class);
        }

        return logicTemplates;
    }

    /**
     * 获取模板具体的物理索引
     *
     * @param appId appId
     */
    @Override
    public Result<List<Tuple<String, String>>> getLogicTemplatesByAppId(Integer appId) {
        List<AppTemplateAuth> appTemplateAuths = logicTemplateAuthService.getTemplateAuthsByAppId(appId);
        if (CollectionUtils.isEmpty(appTemplateAuths)) {
            return Result.buildSucc();
        }

        List<Tuple<String, String>> indicesClusterTupleList = new ArrayList<>();

        appTemplateAuths.parallelStream().forEach(appTemplateAuth -> {
            IndexTemplateWithPhyTemplates logicWithPhysical = getLogicTemplateWithPhysicalsById(
                appTemplateAuth.getTemplateId());

            if (null != logicWithPhysical && logicWithPhysical.hasPhysicals()) {
                IndexTemplatePhy indexTemplatePhysicalInfo = logicWithPhysical.getPhysicals().get(0);

                String cluster = indexTemplatePhysicalInfo.getCluster();
                Set<String> indices = esIndexService.syncGetIndexNameByExpression(cluster,
                    indexTemplatePhysicalInfo.getExpression());
                if (CollectionUtils.isNotEmpty(indices) && StringUtils.isNotBlank(cluster)) {
                    indices.forEach(i -> indicesClusterTupleList.add(new Tuple<>(i, cluster)));
                }
            }
        });

        LOGGER.info("class=TemplateLogicServiceImpl||method=getAllTemplateIndicesByAppid||appId={}||indicesList={}",
            appId, JSON.toJSONString(indicesClusterTupleList));

        return Result.buildSucc(indicesClusterTupleList);
    }

    /**
     * 模板移交
     *
     * @param logicId        模板id
     * @param tgtAppId       appid
     * @param tgtResponsible 责任人
     * @param operator       操作人
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> turnOverLogicTemplate(Integer logicId, Integer tgtAppId, String tgtResponsible,
                                        String operator) throws AdminOperateException {

        IndexTemplate templateLogic = getLogicTemplateById(logicId);
        if (templateLogic == null) {
            return Result.buildParamIllegal(TEMPLATE_NOT_EXIST);
        }

        IndexTemplateDTO logicDTO = new IndexTemplateDTO();
        logicDTO.setId(logicId);
        logicDTO.setAppId(tgtAppId);
        logicDTO.setResponsible(tgtResponsible);

        return editTemplate(logicDTO, operator);

    }

    /**
     * 获取所有逻辑模板物理模板数量
     *
     * @return 逻辑模板和对应物理模板数量的映射
     */
    @Override
    public Map<Integer, Integer> getAllLogicTemplatesPhysicalCount() {
        return indexTemplatePhyService.getAllLogicTemplatesPhysicalCount();
    }

    /**
     * 查询全部的模板
     *
     * @return
     */
    @Override
    public List<IndexTemplate> getAllLogicTemplates() {
        return responsibleConvertTool.list2List(indexTemplateDAO.listAll(), IndexTemplate.class);
    }

    @Override
    public List<IndexTemplate> getAllLogicTemplatesWithCache() {
        try {
            return templateListCache.get("getAllLogicTemplates", this::getAllLogicTemplates);
        } catch (Exception e) {
            return getAllLogicTemplates();
        }
    }

    /**
     * 获取type
     *
     * @param logicId 模板id
     * @return list
     */
    @Override
    public List<IndexTemplateType> getLogicTemplateTypes(Integer logicId) {
        return ConvertUtil.list2List(indexTemplateTypeDAO.listByIndexTemplateId(logicId), IndexTemplateType.class);
    }

    /**
     * 根据责任人查询
     *
     * @param responsibleId 责任人id
     * @return list
     */
    @Override
    public List<IndexTemplate> getTemplateByResponsibleId(Long responsibleId) {
        return responsibleConvertTool.list2List(indexTemplateDAO.likeByResponsible(String.valueOf(responsibleId)),
            IndexTemplate.class);
    }

    /**
     * 需要修改逻辑表和物理表的name
     * 需要修改集群中的name
     *
     * @param param    参数
     * @param operator 操作人
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editTemplateName(IndexTemplateDTO param, String operator) throws AdminOperateException {
        if (AriusObjUtils.isNull(param.getId())) {
            return Result.buildParamIllegal("索引ID必填");
        }

        if (AriusObjUtils.isNull(param.getName())) {
            return Result.buildParamIllegal("索引名称必填");
        }

        IndexTemplatePO logicParam = new IndexTemplatePO();
        logicParam.setId(param.getId());
        logicParam.setName(param.getName());

        boolean succ = 1 == indexTemplateDAO.update(logicParam);
        if (!succ) {
            return Result.buildFail("修改逻辑模板名称失败");
        }

        List<IndexTemplatePhy> physicals = indexTemplatePhyService.getTemplateByLogicId(param.getId());
        if (CollectionUtils.isNotEmpty(physicals)) {
            for (IndexTemplatePhy physical : physicals) {
                physical.setName(param.getName());
                Result<Void> result = indexTemplatePhyService.updateTemplateName(physical, operator);
                if (result.failed()) {
                    throw new AdminOperateException("修改物理模板[" + physical.getId() + "]失败：" + result.getMessage());
                }
            }
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Void> editTemplateInfoTODB(IndexTemplateDTO param) {
        boolean succ = false;
        try {
            succ = 1 == indexTemplateDAO.update(ConvertUtil.obj2Obj(param, IndexTemplatePO.class));
        } catch (Exception e) {
            LOGGER.error("class=TemplateLogicServiceImpl||method=editTemplateInfoTODB||||msg={}", e.getMessage(), e);
        }
        return succ ? Result.buildSucc() : Result.buildFail();
    }

    @Override
    public List<IndexTemplate> getTemplatesByHasAuthCluster(Integer appId) {
        if (appId == null) {
            return new ArrayList<>();
        }

        // 获取有权限的集群id
        Set<Long> hasAuthLogicClusterIds = logicClusterAuthService.getAllLogicClusterAuths(appId).stream()
            .map(AppClusterLogicAuth::getLogicClusterId).collect(Collectors.toSet());

        // 获取集群下的模板
        return getLogicTemplateWithClusterAndMasterTemplateByClusters(hasAuthLogicClusterIds).stream()
                .map(IndexTemplate.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<IndexTemplate> getHasAuthTemplatesInLogicCluster(Integer appId, Long logicClusterId) {
        if (appId == null || logicClusterId == null) {
            return new ArrayList<>();
        }

        // 获取逻辑集群下的逻辑模板列表
        List<IndexTemplateLogicWithClusterAndMasterTemplate> templatesInLogicCluster = getLogicTemplateWithClusterAndMasterTemplateByCluster(
            logicClusterId);

        // 获取app的模板权限记录
        List<AppTemplateAuth> appTemplateAuths = logicTemplateAuthService.getTemplateAuthsByAppId(appId);
        Set<Integer> hasAuthTemplateIds = appTemplateAuths.stream().map(AppTemplateAuth::getTemplateId)
            .collect(Collectors.toSet());

        // 筛选app有权限的逻辑模板
        return templatesInLogicCluster.stream()
            .filter(templateInLogicCluster -> hasAuthTemplateIds.contains(templateInLogicCluster.getId()))
            .map(IndexTemplate.class::cast )
            .collect(Collectors.toList());
    }

    @Override
    public List<IndexTemplateLogicWithClusterAndMasterTemplate> getLogicTemplatesWithClusterAndMasterTemplate() {

        List<IndexTemplateWithCluster> logicClusters = getAllLogicTemplateWithClusters();
        if (CollectionUtils.isEmpty(logicClusters)) {
            return new ArrayList<>();
        }

        return logicClusters.parallelStream().filter(Objects::nonNull).map(this::convert).collect(Collectors.toList());
    }

    @Override
    public IndexTemplateLogicWithClusterAndMasterTemplate getLogicTemplateWithClusterAndMasterTemplate(Integer logicTemplateId) {
        return convert(getLogicTemplateWithCluster(logicTemplateId));
    }

    @Override
    public List<IndexTemplateLogicWithClusterAndMasterTemplate> getLogicTemplatesWithClusterAndMasterTemplate(Set<Integer> logicTemplateIds) {

        List<IndexTemplateWithCluster> logicClusters = getLogicTemplateWithClusters(logicTemplateIds);
        if (CollectionUtils.isEmpty(logicClusters)) {
            return new ArrayList<>();
        }

        return logicClusters.stream().filter(Objects::nonNull).map(this::convert).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, IndexTemplateLogicWithClusterAndMasterTemplate> getLogicTemplatesWithClusterAndMasterTemplateMap(Set<Integer> logicTemplateIds) {
        return getLogicTemplatesWithClusterAndMasterTemplate(logicTemplateIds).stream()
            .collect(Collectors.toMap(IndexTemplateLogicWithClusterAndMasterTemplate::getId, template -> template));
    }

    @Override
    public List<IndexTemplateLogicWithClusterAndMasterTemplate> getLogicTemplateWithClusterAndMasterTemplateByClusters(Set<Long> logicClusterIds) {

        if (CollectionUtils.isEmpty(logicClusterIds)) {
            return new ArrayList<>();
        }

        // 过滤出指定集群的数据
        return getLogicTemplatesWithClusterAndMasterTemplate().parallelStream()
            .filter(logicTemplateWithLogicCluster -> logicTemplateWithLogicCluster != null
                                                     && logicTemplateWithLogicCluster.getLogicCluster() != null
                                                     && logicClusterIds.contains(
                                                         logicTemplateWithLogicCluster.getLogicCluster().getId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<IndexTemplateLogicWithClusterAndMasterTemplate> getLogicTemplateWithClusterAndMasterTemplateByCluster(Long logicClusterId) {
        if (logicClusterId == null) {
            return new ArrayList<>();
        }

        // 过滤出指定集群的数据
        return getLogicTemplatesWithClusterAndMasterTemplate().parallelStream()
            .filter(logicTemplateWithLogicCluster -> logicTemplateWithLogicCluster != null
                                                     && logicTemplateWithLogicCluster.getLogicCluster() != null
                                                     && logicClusterId.equals(
                                                         logicTemplateWithLogicCluster.getLogicCluster().getId()))
            .collect(Collectors.toList());
    }

    /**
     * 获取单个逻辑模板逻辑集群相关信息
     * @param logicTemplateId 逻辑模板ID
     * @return
     */
    @Override
    public IndexTemplateWithCluster getLogicTemplateWithCluster(Integer logicTemplateId) {
        IndexTemplateWithPhyTemplates physicalTemplates = getLogicTemplateWithPhysicalsById(logicTemplateId);

        if (physicalTemplates == null) {
            return null;
        }
        return convert2WithCluster(Arrays.asList(physicalTemplates)).stream().filter(Objects::nonNull).findFirst()
            .orElse(null);
    }

    @Override
    public List<IndexTemplateWithCluster> getLogicTemplateWithClusters(Set<Integer> logicTemplateIds) {
        List<IndexTemplateWithPhyTemplates> physicalTemplates = getLogicTemplateWithPhysicalsByIds(
            logicTemplateIds);

        return convert2WithCluster(physicalTemplates).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 查询模板资源信息
     *
     * @return 带有逻辑集群的所有逻辑模板列表
     */
    @Override
    public List<IndexTemplateWithCluster> getAllLogicTemplateWithClusters() {
        List<IndexTemplateWithPhyTemplates> logicTemplatesCombinePhysicals = getAllLogicTemplateWithPhysicals();

        return convert2WithCluster(logicTemplatesCombinePhysicals).stream().filter(Objects::nonNull)
            .collect(Collectors.toList());

    }

    /**
     * 查询模板资源信息
     *
     * @param logicClusterId 逻辑集群ID
     * @return List<IndexTemplateLogicClusterMeta> 逻辑模板列表
     */
    @Override
    public List<IndexTemplateWithCluster> getLogicTemplateWithClustersByClusterId(Long logicClusterId) {
        List<IndexTemplateWithCluster> allClusterMetas = getAllLogicTemplateWithClusters();

        List<IndexTemplateWithCluster> currentClusterMetas = new ArrayList<>();
        for (IndexTemplateWithCluster clusterMeta : allClusterMetas) {
            if (isLogicClusterIdWithin(clusterMeta.getLogicClusters(), logicClusterId)) {
                currentClusterMetas.add(clusterMeta);
            }
        }

        return currentClusterMetas;
    }

    /**
     * 获取所有带有物理模板详情的逻辑模板列表
     * @return
     */
    @Override
    public List<IndexTemplateWithPhyTemplates> getAllLogicTemplateWithPhysicals() {
        return batchConvertLogicTemplateCombinePhysical(indexTemplateDAO.listAll());
    }

    @Override
    public List<IndexTemplateWithPhyTemplates> getLogicTemplateWithPhysicalsByIds(Set<Integer> logicTemplateIds) {
        if (CollectionUtils.isEmpty(logicTemplateIds)) {
            return new ArrayList<>();
        }
        return batchConvertLogicTemplateCombinePhysical(
            indexTemplateDAO.listByIds(new ArrayList<>(logicTemplateIds)));
    }

    /**
     * 根据当前逻辑模板获取对应带有物理模板列表的逻辑模板详情
     * @param logicTemplateId 逻辑模板ID
     * @return
     */
    @Override
    public IndexTemplateWithPhyTemplates getLogicTemplateWithPhysicalsById(Integer logicTemplateId) {
        IndexTemplatePO templateLogic = indexTemplateDAO.getById(logicTemplateId);
        if (templateLogic == null) {
            return null;
        }

        List<IndexTemplateWithPhyTemplates> physicalTemplates = batchConvertLogicTemplateCombinePhysical(
            Arrays.asList(templateLogic));
        return physicalTemplates.stream().findFirst().orElse(null);
    }

    /**
     * 获取指定数据中的模板信息
     * @param dataCenter 数据中心
     * @return list
     */
    @Override
    public List<IndexTemplateWithPhyTemplates> getTemplateWithPhysicalByDataCenter(String dataCenter) {
        return batchConvertLogicTemplateCombinePhysical(indexTemplateDAO.listByDataCenter(dataCenter));
    }


    /**
     * 更新读状态
     * @param logicId 逻辑模板
     * @param blockRead  是否禁读
     * @param operator  操作人
     * @return
     * @throws AdminOperateException
     */
    @Override
    public Result updateBlockReadState(Integer logicId, Boolean blockRead, String operator) {
        if (null == logicId || null == blockRead) {
            return Result.buildFail("logicId or blockRead is null");
        }
        int row = indexTemplateDAO.updateBlockReadState(logicId, blockRead);
        if (1 != row) {
            return Result.buildFail("修改禁读状态失败");
        }
        operateRecordService.save(TEMPLATE, EDIT, logicId, JSON.toJSONString(new TemplateOperateRecord(TemplateOperateRecordEnum.READ.getCode(),
                "更新读状态为:" + (blockRead ? "禁用读" : "启用读"))), operator);
        return Result.buildSucc(row);
    }

    /**
     * 更新写状态
     * @param logicId 逻辑模板
     * @param blockWrite 是否禁写
     * @param operator 操作热人
     * @return
     */
    @Override
    public Result updateBlockWriteState(Integer logicId, Boolean blockWrite, String operator) {
        if (null == logicId || null == blockWrite) {
            return Result.buildFail("logicId or blockWrite is null");
        }
        int row = indexTemplateDAO.updateBlockWriteState(logicId, blockWrite);
        if (1 != row) {
            return Result.buildFail("修改禁写状态失败");
        }
        operateRecordService.save(TEMPLATE, EDIT, logicId, JSON.toJSONString(new TemplateOperateRecord(TemplateOperateRecordEnum.WRITE.getCode(),
                "更新写状态为:" + (blockWrite ? "禁用写" : "启用写"))), operator);
        return Result.buildSucc(row);
    }

    @Override
    public Result updateTemplateWriteRateLimit(ConsoleTemplateRateLimitDTO dto) throws ESOperateException {
        List<IndexTemplatePhy> phyList = indexTemplatePhyService.getTemplateByLogicId(dto.getLogicId());
        for (IndexTemplatePhy indexTemplatePhy : phyList) {
            ClusterPhy clusterPhy = clusterPhyService.getClusterByName(indexTemplatePhy.getCluster());
            List<String> templateServices = ListUtils.string2StrList(clusterPhy.getTemplateSrvs());
            if (!templateServices.contains(TemplateServiceEnum.TEMPLATE_LIMIT_W.getCode().toString())) {
                return Result.buildFail("指定物理集群没有开启写入限流服务");
            }
        }
        IndexTemplatePO oldPO = indexTemplateDAO.getById(dto.getLogicId());
        IndexTemplatePO editTemplate = responsibleConvertTool.obj2Obj(dto, IndexTemplatePO.class);
        editTemplate.setId(dto.getLogicId());
        editTemplate.setWriteRateLimit(dto.getAdjustRateLimit());
        int update = indexTemplateDAO.update(editTemplate);
        if (update > 0) {
            IndexTemplateDTO param = responsibleConvertTool.obj2Obj(editTemplate, IndexTemplateDTO.class);
            param.setId(dto.getLogicId());
            // 将修改同步到物理模板
            Result editPhyResult = indexTemplatePhyService.editTemplateFromLogic(param, AriusUser.SYSTEM.getDesc());
            if (editPhyResult.failed()) {
                return Result.buildFail("修改限流，修改物理模板失败");
            }
            operateRecordService.save(TEMPLATE, EDIT, dto.getLogicId(), String.format("数据库写入限流值修改%s->%s", dto.getCurRateLimit(), dto.getAdjustRateLimit()), dto.getSubmitor());
            SpringTool.publish(new LogicTemplateModifyEvent(this, responsibleConvertTool.obj2Obj(oldPO, IndexTemplate.class), getLogicTemplateById(oldPO.getId())));
            return Result.buildSucc();
        }
        return Result.buildFail();
    }

    @Override
    public Result<Void> preCheckTemplateName(String name) {
        if (name == null) {
            return Result.buildParamIllegal("模板名称为空");
        }
        List<String> pos = indexTemplateDAO.listAllNames();
        for (String po : pos) {
            if (name.equals(po)) {
                return Result.buildDuplicate("模板名称已经存在");
            }
            if (name.startsWith(po) || po.startsWith(name)) {
                return Result.buildParamIllegal("索引模板" + name + "与【" + po + "】冲突,不能互为前缀,模板表达式匹配时会重叠");
            }
        }
        return Result.buildSuccWithMsg("索引模板可以使用");
    }


    /**************************************** private method ****************************************************/
    /**
     * 转换逻辑模板，获取并组合对应的物理模板信息
     * @param logicTemplates 逻辑模板列表
     * @return
     */
    private List<IndexTemplateWithPhyTemplates> batchConvertLogicTemplateCombinePhysical(List<IndexTemplatePO> logicTemplates) {

        if (CollectionUtils.isEmpty(logicTemplates)) {
            return Lists.newArrayList();
        }

        // 逻辑模板对应1到多个物理模板
        Multimap<Integer, IndexTemplatePhy> logicId2PhysicalTemplatesMapping = ConvertUtil.list2MulMap(
            indexTemplatePhyService.getTemplateByLogicIds(
                logicTemplates.stream().map(IndexTemplatePO::getId).collect(Collectors.toList())),
            IndexTemplatePhy::getLogicId);

        List<IndexTemplateWithPhyTemplates> indexTemplateCombinePhysicalTemplates = Lists.newArrayListWithCapacity(logicTemplates.size());

        for (IndexTemplatePO logicTemplate : logicTemplates) {
            IndexTemplateWithPhyTemplates logicWithPhysical = responsibleConvertTool.obj2Obj(logicTemplate,
                IndexTemplateWithPhyTemplates.class);
            logicWithPhysical
                .setPhysicals(Lists.newArrayList(logicId2PhysicalTemplatesMapping.get(logicTemplate.getId())));

            indexTemplateCombinePhysicalTemplates.add(logicWithPhysical);
        }

        return indexTemplateCombinePhysicalTemplates;
    }

    /**************************************** private method ****************************************************/
    /**
     *
     * @param esLogicClusters 逻辑集群列表
     * @param logicClusterId 逻辑ID
     * @return
     */
    private boolean isLogicClusterIdWithin(List<ClusterLogic> esLogicClusters, Long logicClusterId) {
        if (CollectionUtils.isNotEmpty(esLogicClusters) && logicClusterId != null) {
            for (ClusterLogic logic : esLogicClusters) {
                if (logic.getId().equals(logicClusterId)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 构建整合逻辑集群元数据信息的逻辑模板列表
     *
     * @param logicClusterId2ClusterMeta     逻辑集群ID与逻辑集群元数据映射
     * @param clusterRackMeta2LogicClusterId 集群Rack元数据信息跟逻辑集群ID的映射
     * @param templateLogicWithPhysical      带有物理模板的逻辑模板信息
     * @return
     */
    private IndexTemplateWithCluster buildLogicTemplateWithLogicClusterMeta(Map<Long, ClusterLogic> logicClusterId2ClusterMeta,
                                                                            Multimap<String, Long> clusterRackMeta2LogicClusterId,
                                                                            IndexTemplateWithPhyTemplates templateLogicWithPhysical) {
        List<IndexTemplatePhy> physicals = templateLogicWithPhysical.getPhysicals();

        Map<Long, ClusterLogic> relatedLogicClusters = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(physicals)) {
            for (IndexTemplatePhy physical : physicals) {

                List<ClusterLogic> logicClusters = getPhysicalTemplateLogicCluster(physical, clusterRackMeta2LogicClusterId,
                        logicClusterId2ClusterMeta);

                if (!CollectionUtils.isEmpty(logicClusters)) {
                    logicClusters.forEach(logicCluster -> relatedLogicClusters.put(logicCluster.getId(), logicCluster));
                }
            }
        }

        IndexTemplateWithCluster templateLogicWithCluster = ConvertUtil.obj2Obj(templateLogicWithPhysical,
            IndexTemplateWithCluster.class);

        templateLogicWithCluster.setLogicClusters(Lists.newArrayList(relatedLogicClusters.values()));

        return templateLogicWithCluster;
    }

    /**
     * 获取物理模板对应的逻辑集群信息
     *
     * @param physicalTemplate                        物理模板
     * @param clusterRack2LogicClusterId      集群Rack
     * @param logicClusterId2LogicClusterMeta 逻辑集群ID与逻辑集群的映射关系
     * @return
     */
    private List<ClusterLogic> getPhysicalTemplateLogicCluster(IndexTemplatePhy physicalTemplate,
                                                               Multimap<String, Long> clusterRack2LogicClusterId,
                                                               Map<Long, ClusterLogic> logicClusterId2LogicClusterMeta) {

        if (physicalTemplate != null) {
            Collection<Long> logicClusterIds = clusterRack2LogicClusterId
                    .get(fetchRackKey(physicalTemplate.getCluster(), fetchFirstRack(physicalTemplate.getRack())));

            if (CollectionUtils.isEmpty(logicClusterIds)) {
                logicClusterIds = clusterRack2LogicClusterId
                        .get(fetchRackKey(physicalTemplate.getCluster(), AdminConstant.RACK_COMMA));
            }

            List<ClusterLogic> clusterLogics = Lists.newArrayList();
            logicClusterIds.forEach(logicClusterId -> clusterLogics.add(logicClusterId2LogicClusterMeta.get(logicClusterId)));

            return clusterLogics;
        }

        return null;
    }

    /**
     * 获取第一个Rack
     * @param racks 格式化的Rack列表
     * @return
     */
    private String fetchFirstRack(String racks) {
        if (StringUtils.isNotBlank(racks)) {
            return racks.split(",")[0];
        }

        return StringUtils.EMPTY;
    }

    /**
     * 获取所有集群Rack与逻辑集群ID映射
     *
     * @return
     */
    private Multimap<String, Long> fetchClusterRacks2LogicClusterIdMappings() {
        Multimap<String, Long> logicClusterIdMappings = ArrayListMultimap.create();
        for (ClusterLogicRackInfo param : regionRackService.listAllLogicClusterRacks()) {
            List<Long> logicClusterIds = ListUtils.string2LongList(param.getLogicClusterIds());
            logicClusterIds.forEach(logicClusterId -> logicClusterIdMappings.put(fetchRackKey(param.getPhyClusterName(), param.getRack()), logicClusterId));
        }
        return logicClusterIdMappings;
    }

    /**
     * 生成Map Key
     *
     * @param cluster 物理集群名称
     * @param rack    Rack名称
     * @return
     */
    private String fetchRackKey(String cluster, String rack) {
        return cluster + "&" + rack;
    }

    /**
     * 获取所有逻辑集群
     *
     * @return
     */
    private Map<Long, ClusterLogic> getLogicClusters() {
        return ConvertUtil.list2Map(clusterLogicService.listAllClusterLogics(), ClusterLogic::getId);
    }

    /**
     * 从LogicTemplateCombinePhysicalTemplates转换到LogicTemplateCombineLogicCluster
     * @param logicTemplatesCombinePhysicals
     * @return
     */
    private List<IndexTemplateWithCluster> convert2WithCluster(List<IndexTemplateWithPhyTemplates> logicTemplatesCombinePhysicals) {
        if (CollectionUtils.isEmpty(logicTemplatesCombinePhysicals)) {
            return new ArrayList<>();
        }

        List<IndexTemplateWithCluster> indexTemplateLogicWithClusters = new CopyOnWriteArrayList<>();
        // 所有逻辑集群，key-逻辑集群id，value-逻辑集群
        final Map<Long, ClusterLogic> logicClusterMap = getLogicClusters();
        // 集群rack到逻辑集群id的映射
        final Multimap<String, Long> clusterIdMappingsMap = fetchClusterRacks2LogicClusterIdMappings();

        logicTemplatesCombinePhysicals.forEach(templateLogicWithPhysical -> {
            try {
                indexTemplateLogicWithClusters.add(buildLogicTemplateWithLogicClusterMeta(logicClusterMap,
                    clusterIdMappingsMap, templateLogicWithPhysical));
            } catch (Exception e) {
                LOGGER.error("class=LogicTemplateCombineClusterServiceImpl||method=acquireLogicTemplateCombineClusters"
                             + "||physical={}",
                    templateLogicWithPhysical, e);
            }
        });
        return indexTemplateLogicWithClusters;
    }

    private Result<Void> checkConfigParam(IndexTemplateConfigDTO configDTO) {
        if (configDTO == null) {
            return Result.buildParamIllegal("配置信息为空");
        }
        if (configDTO.getLogicId() == null) {
            return Result.buildParamIllegal("模板ID为空");
        }
        if (configDTO.getIsSourceSeparated() != null && !yesOrNo(configDTO.getIsSourceSeparated())) {
            return Result.buildParamIllegal("索引存储分离开关非法");
        }
        if (configDTO.getDynamicLimitEnable() != null && !yesOrNo(configDTO.getDynamicLimitEnable())) {
            return Result.buildParamIllegal("写入动态限流开关非法");
        }
        if (configDTO.getMappingImproveEnable() != null && !yesOrNo(configDTO.getMappingImproveEnable())) {
            return Result.buildParamIllegal("mapping优化开关非法");
        }

        return Result.buildSucc();
    }

    private TemplateConfigPO getDefaultTemplateConfig(Integer logicId) {
        TemplateConfigPO configPO = new TemplateConfigPO();
        configPO.setLogicId(logicId);
        configPO.setAdjustRackTpsFactor(1.0);
        configPO.setAdjustRackShardFactor(1.0);
        configPO.setDynamicLimitEnable(AdminConstant.YES);
        configPO.setMappingImproveEnable(AdminConstant.NO);
        configPO.setIsSourceSeparated(AdminConstant.NO);
        configPO.setDisableSourceFlags(false);
        configPO.setPreCreateFlags(true);
        configPO.setShardNum(1);
        return configPO;
    }

    /**
     * 编辑逻辑模板   无参数校验
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    private Result<Void> editTemplateWithoutCheck(IndexTemplateDTO param, String operator) throws AdminOperateException {

        if (param.getDateFormat() != null) {
            param.setDateFormat(param.getDateFormat().replace("Y", "y"));
        }

        IndexTemplatePO oldPO = indexTemplateDAO.getById(param.getId());
        IndexTemplatePO editTemplate = responsibleConvertTool.obj2Obj(param, IndexTemplatePO.class);
        if ("".equals(editTemplate.getResponsible())) {
            editTemplate.setResponsible(null);
        }

        boolean succeed = (1 == indexTemplateDAO.update(editTemplate));

        if (succeed) {
            param.setId(editTemplate.getId());
            // 将修改同步到物理模板
            Result<Void> editPhyResult = indexTemplatePhyService.editTemplateFromLogic(param, operator);
            if (editPhyResult.failed()) {
                throw new AdminOperateException("修改物理模板失败");
            }

            // 保存模板修改记录
            operateRecordService.save(ModuleEnum.TEMPLATE, OperationEnum.EDIT, param.getId(), JSON.toJSONString(
                    new TemplateOperateRecord(TemplateOperateRecordEnum.TRANSFER.getCode(), AriusObjUtils.findChangedWithClear(oldPO, editTemplate))), operator);

            SpringTool.publish(new LogicTemplateModifyEvent(this, responsibleConvertTool.obj2Obj(oldPO, IndexTemplate.class)
                    , getLogicTemplateById(oldPO.getId())));
        }

        return Result.build(succeed);
    }

    /**
     * 转换具体格式
     * @param combineLogicCluster 逻辑模板
     * @return
     */
    private IndexTemplateLogicWithClusterAndMasterTemplate convert(IndexTemplateWithCluster combineLogicCluster) {
        if (combineLogicCluster == null) {
            return null;
        }

        IndexTemplateLogicWithClusterAndMasterTemplate combineLogicClusterAndMasterTemplate = ConvertUtil
            .obj2Obj(combineLogicCluster, IndexTemplateLogicWithClusterAndMasterTemplate.class);

        combineLogicClusterAndMasterTemplate.setLogicCluster(fetchOne(combineLogicCluster.getLogicClusters()));

        combineLogicClusterAndMasterTemplate.setMasterTemplate(
            fetchMasterTemplate(indexTemplatePhyService.getValidTemplatesByLogicId(combineLogicCluster.getId())));

        return combineLogicClusterAndMasterTemplate;
    }

    /**
     * 获取Master物理模板
     * @param physicalTemplates 物理模板列表
     * @return
     */
    private IndexTemplatePhy fetchMasterTemplate(List<IndexTemplatePhy> physicalTemplates) {
        if (CollectionUtils.isEmpty(physicalTemplates)) {
            return null;
        }

        for (IndexTemplatePhy physicalTemplate : physicalTemplates) {
            if (TemplateDeployRoleEnum.MASTER.getCode().equals(physicalTemplate.getRole())) {
                return physicalTemplate;
            }
        }

        return null;
    }

    /**
     * 获取第一条记录
     * @param logicClusters 逻辑集群列表
     * @return
     */
    private ClusterLogic fetchOne(List<ClusterLogic> logicClusters) {
        if (CollectionUtils.isNotEmpty(logicClusters)) {
            return logicClusters.get(0);
        }

        return null;
    }

    private Result<Void> validateIndexTemplateLogicStep2(IndexTemplateDTO param, String dateFormatFinal, String expressionFinal, String nameFinal, String dateFieldFinal) {
        List<String> responsibles = ListUtils.string2StrList(param.getResponsible());
        for (String responsible : responsibles) {
            if (AriusObjUtils.isNull(ariusUserInfoService.getByDomainAccount(responsible))) {
                return Result.buildParamIllegal(String.format("责任人%s非法", responsible));
            }
        }
        if (expressionFinal != null && expressionFinal.endsWith("*") && AriusObjUtils.isNull(dateFormatFinal)) {
            return Result.buildParamIllegal("表达式*结尾,后缀格式必填");
        }
        if (dateFormatFinal != null && param.getExpireTime() != null && TemplateUtils.isSaveByDay(dateFormatFinal)
                && param.getExpireTime() > TEMPLATE_SAVE_BY_DAY_EXPIRE_MAX) {
            return Result.buildParamIllegal("按天创建的索引数据保存时长不能超过180天");
        }
        if (dateFormatFinal != null && param.getExpireTime() != null && TemplateUtils.isSaveByMonth(dateFormatFinal)
                && (param.getExpireTime() < TEMPLATE_SAVE_BY_MONTH_EXPIRE_MIN && param.getExpireTime() > 0)) {
            return Result.buildParamIllegal("按月创建的索引数据保存时长不能小于30天");
        }
        if (param.getExpireTime() != null && param.getExpireTime() > 0 &&
                param.getExpireTime() < AdminConstant.PLATFORM_EXPIRE_TIME_MIN) {
            return Result.buildParamIllegal(String.format("分区索引模板数据保存天数不能小于%d天", AdminConstant.PLATFORM_EXPIRE_TIME_MIN));
        }
        if (nameFinal != null) {
            boolean expressionMatch = nameFinal.equals(expressionFinal) || (nameFinal + "*").equals(expressionFinal);
            if (!expressionMatch) {
                return Result.buildParamIllegal("表达式与模板名字不匹配");
            }
        }
        if (StringUtils.isNotBlank(dateFormatFinal) && StringUtils.isBlank(dateFieldFinal)) {
            return Result.buildParamIllegal("索引分区创建，分区字段必填");
        }
        return Result.buildSucc();
    }

    private Result<Void> validateIndexTemplateLogicStep1(IndexTemplateDTO param, List<IndexTemplate> indexTemplateList) {
        // 校验索引名字
        if (param.getName() != null) {
            Result<Void> result = validateIndexName(param, indexTemplateList);
            if (result.failed()){return result;}
        }
        if (param.getExpression() != null) {
            Result<Void> result = validateExpression(param, indexTemplateList);
            if (result.failed()){return result;}
        }
        if (param.getDataCenter() != null
                && !DataCenterEnum.validate(param.getDataCenter())) {
            return Result.buildParamIllegal("数据中心非法");
        }
        if (param.getAppId() != null
                && !appService.isAppExists(param.getAppId())) {
            return Result.buildParamIllegal("所属应用不存在");
        }
        if (param.getDataType() != null
                && DataTypeEnum.UNKNOWN.equals(DataTypeEnum.valueOf(param.getDataType()))) {
            return Result.buildParamIllegal("数据类型非法");
        }
        if (param.getShardNum() != null && param.getShardNum() <= 0) {
            return Result.buildNotExist("shard数量必须大于0");
        }
        return Result.buildSucc();
    }

    private String getDateField(IndexTemplateDTO param, IndexTemplatePO oldPO) {
        String dateFieldFinal;
        if (param.getDateField() != null) {
            dateFieldFinal = param.getDateField();
        } else {
            dateFieldFinal = oldPO.getDateField();
        }
        return dateFieldFinal;
    }

    private String getExpression(IndexTemplateDTO param, IndexTemplatePO oldPO) {
        String expressionFinal;
        if (param.getExpression() != null) {
            expressionFinal = param.getExpression();
        } else {
            expressionFinal = oldPO.getExpression();
        }
        return expressionFinal;
    }

    private String getDateFormat(IndexTemplateDTO param, IndexTemplatePO oldPO) {
        String dateFormatFinal;
        if (param.getDateFormat() != null) {
            dateFormatFinal = param.getDateFormat();
        } else {
            dateFormatFinal = oldPO.getDateFormat();
        }
        return dateFormatFinal;
    }

    private Result<Void> validateAdd(IndexTemplateDTO param) {
        if (AriusObjUtils.isNull(param.getName())) {
            return Result.buildParamIllegal("名字为空");
        }
        if (AriusObjUtils.isNull(param.getAppId())) {
            return Result.buildParamIllegal("所属应用为空");
        }
        if (AriusObjUtils.isNull(param.getDataType())) {
            return Result.buildParamIllegal("数据类型为空");
        }
        if (AriusObjUtils.isNull(param.getExpireTime())) {
            return Result.buildParamIllegal("保存时长为空");
        }
        if (AriusObjUtils.isNull(param.getResponsible())) {
            return Result.buildParamIllegal("责任人为空");
        }
        if (AriusObjUtils.isNull(param.getExpression())) {
            return Result.buildParamIllegal("表达式为空");
        }
        if (AriusObjUtils.isNull(param.getDataCenter())) {
            return Result.buildParamIllegal("数据中心为空");
        }
        if (AriusObjUtils.isNull(param.getQuota())) {
            return Result.buildParamIllegal("Quota为空");
        }
        if (AriusObjUtils.isNull(param.getWriteRateLimit())) {
            param.setWriteRateLimit(-1);
        }
        if(LevelEnum.valueOfCode(param.getLevel()).equals(LevelEnum.UNKNOWN)) {
            return Result.buildParamIllegal("模板设置的服务等级为未知类型");
        }
        if(levelOfTemplateLower(param)) {
            return Result.buildParamIllegal("模板设置的服务等级低于所属逻辑集群的服务等级");
        }

        return Result.buildSucc();
    }

    private boolean levelOfTemplateLower(IndexTemplateDTO param) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(param.getResourceId());
        return !AriusObjUtils.isNull(clusterLogic) && clusterLogic.getLevel() < param.getLevel();
    }

    private Result<Void> validateExpression(IndexTemplateDTO param, List<IndexTemplate> indexTemplateList) {
        String expression = param.getExpression();
        for (IndexTemplate templateLogic : indexTemplateList) {
            if (StringUtils.isBlank(templateLogic.getExpression()) || StringUtils.isBlank(expression)) {
                continue;
            }

            if (templateLogic.getId().equals(param.getId())) {
                continue;
            }

            if (templateLogic.getExpression().equals(expression)) {
                return Result.buildParamIllegal("模板表达式已经存在");
            }

            String otherExpressionPre = templateLogic.getExpression();
            if (otherExpressionPre.endsWith("*")) {
                otherExpressionPre = otherExpressionPre.substring(0, otherExpressionPre.length() - 1);
            }

            String expressionPre = expression;
            if (expressionPre.contains("*")) {
                expressionPre = expressionPre.substring(0, expressionPre.length() - 1);
            }

            if (expressionPre.startsWith(otherExpressionPre) || otherExpressionPre.startsWith(expressionPre)) {
                return Result.buildParamIllegal("表达式与【" + templateLogic.getName() + "】冲突,不能互为前缀,模板表达式匹配时会重叠");
            }
        }
        return Result.buildSucc();
    }

    private Result<Void> validateIndexName(IndexTemplateDTO param, List<IndexTemplate> indexTemplateList) {
        String name = param.getName();
        if (name.length() < TEMPLATE_NAME_SIZE_MIN || name.length() > TEMPLATE_NAME_SIZE_MAX) {
            return Result.buildParamIllegal(String.format("名称长度非法, %s-%s",TEMPLATE_NAME_SIZE_MIN,TEMPLATE_NAME_SIZE_MAX));
        }

        for (Character c : name.toCharArray()) {
            if (!TEMPLATE_NAME_CHAR_SET.contains(c)) {
                return Result.buildParamIllegal("名称包含非法字符, 只能包含小写字母、数字、-、_和.");
            }
        }

        for (IndexTemplate templateLogic : indexTemplateList) {
            if (templateLogic.getName().equals(name) && !templateLogic.getId().equals(param.getId())) {
                return Result.buildDuplicate("模板名称已经存在");
            }
        }
        return Result.buildSucc();
    }
}
