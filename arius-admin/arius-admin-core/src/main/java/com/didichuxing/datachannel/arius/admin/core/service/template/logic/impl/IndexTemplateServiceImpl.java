package com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.*;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateTypePO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.LevelEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateConfigDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateTypeDAO;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

@Service
public class IndexTemplateServiceImpl implements IndexTemplateService {

    private static final ILog                  LOGGER            = LogFactory.getLog(IndexTemplateServiceImpl.class);

    @Autowired
    private IndexTemplateDAO                   indexTemplateDAO;

    @Autowired
    private IndexTemplateConfigDAO             indexTemplateConfigDAO;

    @Autowired
    private IndexTemplateTypeDAO               indexTemplateTypeDAO;

    @Autowired
    private IndexTemplatePhyService            indexTemplatePhyService;

    @Autowired
    private ProjectService                     projectService;

    @Autowired
    private ESIndexService                     esIndexService;

    @Autowired
    private ProjectLogicTemplateAuthService    logicTemplateAuthService;

    @Autowired
    private ProjectClusterLogicAuthService     logicClusterAuthService;

    @Autowired
    private ClusterLogicService                clusterLogicService;

  

    @Autowired
    private ClusterRegionService               clusterRegionService;

    private final Cache<String, List<IndexTemplate>> templateListCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(10).build();
    private final Cache<String, Map<Integer, IndexTemplate>> INDEX_TEMPLATE_SERVICE_CACHE = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(10).build();

    /**
     * 条件查询
     *
     * @param param 条件
     * @return 逻辑模板列表
     */
    @Override
    public List<IndexTemplate> listLogicTemplates(IndexTemplateDTO param) {
        return ConvertUtil.list2List(
            indexTemplateDAO.listByCondition(ConvertUtil.obj2Obj(param, IndexTemplatePO.class)), IndexTemplate.class);
    }

    /**
     * 根据逻辑模板名称模糊查询
     *
     * @param param 模糊查询条件
     * @return
     */
    @Override
    public List<IndexTemplate> fuzzyLogicTemplatesByCondition(IndexTemplateDTO param) {
        return ConvertUtil.list2List(
            indexTemplateDAO.likeByCondition(ConvertUtil.obj2Obj(param, IndexTemplatePO.class)), IndexTemplate.class);
    }
    
    /**
     * @param param           查询条件对象，与上一方法中的查询条件对象相同。
     * @param logicClusterIds 逻辑集群 ID 列表。
     * @return 列表 < 索引模板 >
     */
    @Override
    public List<IndexTemplate> pagingGetTemplateSrvByConditionAndLogicClusterIdList(TemplateQueryDTO param,
                                                                                    List<Integer> logicClusterIds) {
        List<IndexTemplatePO> indexTemplatePOS = Lists.newArrayList();
        String sortTerm = null == param.getSortTerm() ? SortConstant.ID : param.getSortTerm();
        
        String sortType = param.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        try {
            indexTemplatePOS = indexTemplateDAO.pagingByConditionAndLogicClusterIdList(
                    ConvertUtil.obj2Obj(param, IndexTemplatePO.class), (param.getPage() - 1) * param.getSize(),
                    param.getSize(), sortTerm, sortType, logicClusterIds);
        } catch (Exception e) {
            LOGGER.error("class=IndexTemplateServiceImpl||method=pagingGetTemplateSrvByCondition||err={}",
                    e.getMessage(), e);
        }
        return ConvertUtil.list2List(indexTemplatePOS, IndexTemplate.class);
    }
    
    @Override
    public List<IndexTemplate> pagingGetLogicTemplatesByCondition(TemplateConditionDTO param) {
        String sortTerm = null == param.getSortTerm() ? SortConstant.ID : param.getSortTerm();
        String sortType = param.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
       
        List<IndexTemplatePO> indexTemplatePOS = Lists.newArrayList();
        try {
            indexTemplatePOS = indexTemplateDAO.pagingByCondition(ConvertUtil.obj2Obj(param, IndexTemplatePO.class),
                (param.getPage() - 1) * param.getSize(), param.getSize(), sortTerm, sortType);
        } catch (Exception e) {
            LOGGER.error("class=TemplateLogicServiceImpl||method=pagingGetLogicTemplatesByCondition||err={}",
                e.getMessage(), e);
        }

        return ConvertUtil.list2List(indexTemplatePOS, IndexTemplate.class);
    }

    @Override
    public List<IndexTemplate> pagingGetTemplateSrvByCondition(TemplateQueryDTO param) {
        List<IndexTemplatePO> indexTemplatePOS = Lists.newArrayList();
        String sortTerm = null == param.getSortTerm() ? SortConstant.ID : param.getSortTerm();
        
        String sortType = param.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        try {
            indexTemplatePOS = indexTemplateDAO.pagingByCondition(ConvertUtil.obj2Obj(param, IndexTemplatePO.class),
                (param.getPage() - 1) * param.getSize(), param.getSize(), sortTerm, sortType);
        } catch (Exception e) {
            LOGGER.error("class=IndexTemplateServiceImpl||method=pagingGetTemplateSrvByCondition||err={}",
                e.getMessage(), e);
        }
        return ConvertUtil.list2List(indexTemplatePOS, IndexTemplate.class);
    }

    @Override
    public Long fuzzyLogicTemplatesHitByCondition(IndexTemplateDTO param) {
      
        return indexTemplateDAO.getTotalHitByCondition(ConvertUtil.obj2Obj(param, IndexTemplatePO.class));
    }
    
    /**
     * 它返回与给定条件和逻辑集群 ID 匹配的模板数量。
     *
     * @param param           将用于构造查询的参数对象。
     * @param logicClusterIds 逻辑集群 ID 列表。
     * @return 长
     */
    @Override
    public Long fuzzyLogicTemplatesHitByConditionAndLogicClusterIdList(IndexTemplateDTO param,
                                                                       List<Integer> logicClusterIds) {
      
        return indexTemplateDAO.getTotalHitByConditionAndLogicClusterIdList(ConvertUtil.obj2Obj(param,
                IndexTemplatePO.class),logicClusterIds);
    }
    
    /**
     * 根据名字查询
     *
     * @param templateName 模板名字
     * @return list
     */
    @Override
    public List<IndexTemplate> listLogicTemplateByName(String templateName) {
        return ConvertUtil.list2List(indexTemplateDAO.listByName(templateName), IndexTemplate.class);
    }

    /**
     * 查询指定的逻辑模板
     *
     * @param logicTemplateId 模板id
     * @return 模板信息  不存在返回null
     */
    @Override
    public IndexTemplate getLogicTemplateById(Integer logicTemplateId) {
        return ConvertUtil.obj2Obj(indexTemplateDAO.getById(logicTemplateId), IndexTemplate.class);
    }

    /**
     * 删除逻辑模板
     *
     * @param logicTemplateId 模板id
     * @param operator        操作人
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
            Result<Void> deleteTemplateAuthResult = logicTemplateAuthService
                .deleteTemplateAuthByTemplateId(oldPO.getId());
            if (deleteTemplateAuthResult.failed()) {
                throw new AdminOperateException("删除模板失败");
            } else {
                LOGGER.info(
                    "class=TemplateLogicServiceImpl||method=delTemplate||logicId={}||msg=deleteTemplateAuthByTemplateId succ",
                    logicTemplateId);
            }
            //一并下线索引模版配置
            indexTemplateConfigDAO.deleteByLogicId(logicTemplateId);

            Result<Void> result = indexTemplatePhyService.delTemplateByLogicId(logicTemplateId, operator);
            if (result.failed()) {
                throw new AdminOperateException("删除模板失败");
            } else {

                LOGGER.info(
                    "class=TemplateLogicServiceImpl||method=delTemplate||logicId={}||msg=delTemplateByLogicId succ",
                    logicTemplateId);
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
    public Result<Void> validateTemplate(IndexTemplateDTO param, OperationEnum operation, Integer projectId) {
        if (param == null) {
            return Result.buildParamIllegal("模板信息为空");
        }

        String dateFormatFinal = null;
        String expressionFinal = null;
        String nameFinal = null;
        String dateFieldFinal = null;

        if (ADD.equals(operation)) {
            Result<Void> result = validateAdd(param);
            if (result.failed()) {
                return result;
            }

            dateFormatFinal = StringUtils.isBlank(param.getDateFormat()) ? null : param.getDateFormat();
            expressionFinal = param.getExpression();
            nameFinal = param.getName();
            dateFieldFinal = param.getDateField();
        } else if (EDIT.equals(operation)) {
            if (AriusObjUtils.isNull(param.getId())) {
                return Result.buildParamIllegal("模板id为空");
            }

            IndexTemplatePO oldPO = indexTemplateDAO.getById(param.getId());
            final Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(IndexTemplatePO::getProjectId,
                oldPO, projectId);
            if (checkProjectCorrectly.failed()) {
                return checkProjectCorrectly;
            }
            if (oldPO == null) {
                return Result.buildNotExist(TEMPLATE_NOT_EXIST);
            }
            dateFormatFinal = getDateFormat(param, oldPO);
            expressionFinal = getExpression(param, oldPO);
            dateFieldFinal = getDateField(param, oldPO);
            nameFinal = oldPO.getName();
            param.setName(nameFinal);
        }

        List<IndexTemplate> indexTemplateList = listAllLogicTemplates();
        Result<Void> result = validateIndexTemplateLogicStep1(param, indexTemplateList);
        if (result.failed()) {
            return result;
        }

        result = validateIndexTemplateLogicStep2(param, dateFormatFinal, expressionFinal, nameFinal, dateFieldFinal);
        if (result.failed()) {
            return result;
        }

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
    public Result<Void> editTemplate(IndexTemplateDTO param, String operator,
                                     Integer projectId) throws AdminOperateException {
        Result<Void> checkResult = validateTemplate(param, EDIT, projectId);
        if (checkResult.failed()) {
            LOGGER.warn("class=TemplateLogicServiceImpl||method=editTemplate||msg={}", checkResult.getMessage());
            return checkResult;
        }

        return editTemplateWithoutCheck(param, operator);
    }

    @Override
    public Result<Void> addTemplateWithoutCheck(IndexTemplateDTO param) throws AdminOperateException {
        IndexTemplatePO templatePO = ConvertUtil.obj2Obj(param, IndexTemplatePO.class);
        boolean succ;
        try {
            succ = (1 == indexTemplateDAO.insert(templatePO));
        } catch (DuplicateKeyException e) {
            LOGGER.warn("class=TemplateLogicServiceImpl||method=addTemplateWithoutCheck||errMsg={}", e.getMessage());
            throw new AdminOperateException(String.format("保存逻辑模板【%s】失败！", templatePO.getName()));
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
        return ConvertUtil.obj2Obj(indexTemplateConfigDAO.getByLogicId(logicTemplateId), IndexTemplateConfig.class);
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

        boolean succ = 1 == indexTemplateConfigDAO.update(ConvertUtil.obj2Obj(configDTO, TemplateConfigPO.class));

        return Result.build(succ);
    }

    @Override
    public Result<Void> insertTemplateConfig(IndexTemplateConfig indexTemplateConfig) {
        return Result.build(
            1 == indexTemplateConfigDAO.insert(ConvertUtil.obj2Obj(indexTemplateConfig, TemplateConfigPO.class)));
    }
    
     /**
     * 获取通过逻辑id
     *
     * @param logicId 逻辑标识
     * @return {@link TemplateConfig}
     */
    @Override
    public TemplateConfigPO getTemplateConfigByLogicId(Integer logicId) {
        return indexTemplateConfigDAO.getByLogicId(logicId);
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
            configPO.setAdjustShardFactor(factor);
            Result.build(1 == indexTemplateConfigDAO.insert(configPO));
        } else {
            IndexTemplateConfigDTO param = new IndexTemplateConfigDTO();
            param.setLogicId(logicTemplateId);
            param.setAdjustShardFactor(factor);
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
            configPO.setAdjustShardFactor(factor);
            Result.build(1 == indexTemplateConfigDAO.insert(configPO));
            return;
        } else if (templateConfig.getAdjustShardFactor() < factor) {
            IndexTemplateConfigDTO param = new IndexTemplateConfigDTO();
            param.setLogicId(logicTemplateId);
            param.setAdjustShardFactor(factor);
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
        return listAllLogicTemplates().stream()
            .collect(Collectors.toMap(IndexTemplate::getId, indexTemplateLogic -> indexTemplateLogic));
    }
    
    @Override
    public Map<Integer, IndexTemplate> getAllLogicTemplatesMapWithCache() {
        try {
            return (Map<Integer, IndexTemplate>) INDEX_TEMPLATE_SERVICE_CACHE.get(
                "getAllLogicTemplatesMapWithCache", this::getAllLogicTemplatesMap);
        } catch (ExecutionException e) {
            return getAllLogicTemplatesMap();
        }
    }
    
    @Override
    public List<IndexTemplate> listLogicTemplatesByIds(List<Integer> logicTemplateIds) {
        if (CollectionUtils.isEmpty(logicTemplateIds)) {
            return new ArrayList<>();
        }

        return ConvertUtil.list2List(indexTemplateDAO.listByIds(logicTemplateIds), IndexTemplate.class);
    }

    @Override
    public Map<Integer, IndexTemplate> getLogicTemplatesMapByIds(List<Integer> logicTemplateIds) {
        return listLogicTemplatesByIds(logicTemplateIds).stream()
            .collect(Collectors.toMap(IndexTemplate::getId, indexTemplateLogic -> indexTemplateLogic));
    }

    /**
     * 根据APP ID查询模板
     *
     * @param projectId APP ID
     * @return list
     */
    @Override
    public List<IndexTemplate> listProjectLogicTemplatesByProjectId(Integer projectId) {
        return ConvertUtil.list2List(indexTemplateDAO.listByProjectId(projectId), IndexTemplate.class);
    }

    /**
     * 根据逻辑集群获取所有的逻辑模板
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    @Override
    public List<IndexTemplate> listLogicClusterTemplates(Long logicClusterId) {
        List<IndexTemplate> logicTemplates = Lists.newArrayList();

        if (logicClusterId != null) {
            List<IndexTemplateWithCluster> indexTemplateLogicWithClusters = listLogicTemplateWithClustersByClusterId(
                logicClusterId);
            logicTemplates = ConvertUtil.list2List(indexTemplateLogicWithClusters, IndexTemplate.class);
        }

        return logicTemplates;
    }

    /**
     * 获取模板具体的物理索引
     *
     * @param projectId projectId
     */
    @Override
    public Result<List<Tuple<String, String>>> listLogicTemplatesByProjectId(Integer projectId) {
        List<ProjectTemplateAuth> projectTemplateAuths = logicTemplateAuthService
            .getTemplateAuthsByProjectId(projectId);
        if (CollectionUtils.isEmpty(projectTemplateAuths)) {
            return Result.buildSucc();
        }

        List<Tuple<String, String>> indicesClusterTupleList = new ArrayList<>();

        projectTemplateAuths.parallelStream().forEach(appTemplateAuth -> {
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

        LOGGER.info(
            "class=TemplateLogicServiceImpl||method=getAllTemplateIndicesByProjectId||projectId={}||indicesList={}",
            projectId, JSON.toJSONString(indicesClusterTupleList));

        return Result.buildSucc(indicesClusterTupleList);
    }

    /**
     * 模板移交
     *
     * @param logicId         模板id
     * @param sourceProjectId
     * @param tgtProjectId    projectId
     * @param operator        操作人
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> turnOverLogicTemplate(Integer logicId, Integer sourceProjectId, Integer tgtProjectId, String operator) throws AdminOperateException {

        IndexTemplate templateLogic = getLogicTemplateById(logicId);
        if (templateLogic == null) {
            return Result.buildParamIllegal(TEMPLATE_NOT_EXIST);
        }

        IndexTemplateDTO logicDTO = new IndexTemplateDTO();
        logicDTO.setId(logicId);
        logicDTO.setProjectId(tgtProjectId);

        return editTemplate(logicDTO, operator, sourceProjectId);

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
    public List<IndexTemplate> listAllLogicTemplates() {
        return ConvertUtil.list2List(indexTemplateDAO.listAll(), IndexTemplate.class);
    }

    @Override
    public List<IndexTemplate> listAllLogicTemplatesWithCache() {
        try {
            return templateListCache.get("listAllLogicTemplates", this::listAllLogicTemplates);
        } catch (Exception e) {
            return listAllLogicTemplates();
        }
    }

    /**
     * 获取type
     *
     * @param logicId 模板id
     * @return list
     */
    @Override
    public List<IndexTemplateType> listLogicTemplateTypes(Integer logicId) {
        return ConvertUtil.list2List(indexTemplateTypeDAO.listByIndexTemplateId(logicId), IndexTemplateType.class);
    }
    
    /**
     * 更新
     *
     * @param param 入参
     * @return boolean
     */
    @Override
    public boolean updateTemplateType(TemplateTypePO param) {
        return indexTemplateTypeDAO.update(param) == 1;
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
        return Result.build(succ);
    }

    @Override
    public List<IndexTemplate> listTemplatesByHasAuthCluster(Integer projectId) {
        if (projectId == null) {
            return new ArrayList<>();
        }

        // 获取有权限的集群id
        Set<Long> hasAuthLogicClusterIds = logicClusterAuthService.getAllLogicClusterAuths(projectId).stream()
            .map(ProjectClusterLogicAuth::getLogicClusterId).collect(Collectors.toSet());

        // 获取集群下的模板
        return listLogicTemplateWithClusterAndMasterTemplateByClusters(hasAuthLogicClusterIds).stream()
            .map(IndexTemplate.class::cast).collect(Collectors.toList());
    }

    @Override
    public List<IndexTemplate> listHasAuthTemplatesInLogicCluster(Integer projectId, Long logicClusterId) {
        if (projectId == null || logicClusterId == null) {
            return new ArrayList<>();
        }

        // 获取逻辑集群下的逻辑模板列表
        List<IndexTemplateLogicWithClusterAndMasterTemplate> templatesInLogicCluster = listLogicTemplateWithClusterAndMasterTemplateByCluster(
            logicClusterId);

        // 获取app的模板权限记录
        List<ProjectTemplateAuth> projectTemplateAuths = logicTemplateAuthService
            .getTemplateAuthsByProjectId(projectId);
        Set<Integer> hasAuthTemplateIds = projectTemplateAuths.stream().map(ProjectTemplateAuth::getTemplateId)
            .collect(Collectors.toSet());

        // 筛选app有权限的逻辑模板
        return templatesInLogicCluster.stream()
            .filter(templateInLogicCluster -> hasAuthTemplateIds.contains(templateInLogicCluster.getId()))
            .map(IndexTemplate.class::cast).collect(Collectors.toList());
    }

    @Override
    public List<IndexTemplateLogicWithClusterAndMasterTemplate> listLogicTemplatesWithClusterAndMasterTemplate() {

        List<IndexTemplateWithCluster> logicClusters = listAllLogicTemplateWithClusters();
        if (CollectionUtils.isEmpty(logicClusters)) {
            return new ArrayList<>();
        }

        return logicClusters.parallelStream().filter(Objects::nonNull).map(this::convert).collect(Collectors.toList());
    }

    @Override
    public IndexTemplateLogicWithClusterAndMasterTemplate getLogicTemplateWithClusterAndMasterTemplate(Integer logicTemplateId) {
        return convert(getLogicTemplateWithCluster(logicTemplateId));
    }
    
    /**
     * @param logicTemplateId
     * @return
     */
    @Override
    public String getMaterClusterPhyByLogicTemplateId(Integer logicTemplateId) {
        IndexTemplatePhy IndexTemplatePhy = indexTemplatePhyService.getTemplateByLogicIdAndRole(logicTemplateId,
                TemplateDeployRoleEnum.MASTER.getCode());
        if (IndexTemplatePhy==null){
            return null;
        }
     
        return IndexTemplatePhy.getCluster();
    }
    
    /**
     * 通过逻辑模板ID获取主模板的物理模板ID
     *
     * @param logicTemplateId 逻辑模板 ID。
     * @return 长
     */
    @Override
    public Long getMasterTemplatePhyIdByLogicTemplateId(Integer logicTemplateId) {
        IndexTemplatePhy IndexTemplatePhy = indexTemplatePhyService.getTemplateByLogicIdAndRole(logicTemplateId,
                TemplateDeployRoleEnum.MASTER.getCode());
        if (IndexTemplatePhy==null){
            return null;
        }
     
        return IndexTemplatePhy.getId();
    }
    
    @Override
    public List<IndexTemplateLogicWithClusterAndMasterTemplate> listLogicTemplatesWithClusterAndMasterTemplate(Set<Integer> logicTemplateIds) {

        List<IndexTemplateWithCluster> logicClusters = listLogicTemplateWithClusters(logicTemplateIds);
        if (CollectionUtils.isEmpty(logicClusters)) {
            return new ArrayList<>();
        }

        return logicClusters.stream().filter(Objects::nonNull).map(this::convert).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, IndexTemplateLogicWithClusterAndMasterTemplate> getLogicTemplatesWithClusterAndMasterTemplateMap(Set<Integer> logicTemplateIds) {
        return listLogicTemplatesWithClusterAndMasterTemplate(logicTemplateIds).stream()
            .collect(Collectors.toMap(IndexTemplateLogicWithClusterAndMasterTemplate::getId, template -> template));
    }

    @Override
    public List<IndexTemplateLogicWithClusterAndMasterTemplate> listLogicTemplateWithClusterAndMasterTemplateByClusters(Set<Long> logicClusterIds) {

        if (CollectionUtils.isEmpty(logicClusterIds)) {
            return new ArrayList<>();
        }

        // 过滤出指定集群的数据
        return listLogicTemplatesWithClusterAndMasterTemplate().parallelStream()
            .filter(logicTemplateWithLogicCluster -> logicTemplateWithLogicCluster != null
                                                     && logicTemplateWithLogicCluster.getLogicCluster() != null
                                                     && logicClusterIds.contains(
                                                         logicTemplateWithLogicCluster.getLogicCluster().getId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<IndexTemplateLogicWithClusterAndMasterTemplate> listLogicTemplateWithClusterAndMasterTemplateByCluster(Long logicClusterId) {
        if (logicClusterId == null) {
            return new ArrayList<>();
        }

        // 过滤出指定集群的数据
        return listLogicTemplatesWithClusterAndMasterTemplate().parallelStream()
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
        return convert2WithCluster(Arrays.asList(physicalTemplates)).stream().distinct().filter(Objects::nonNull).findFirst()
            .orElse(null);
    }

    @Override
    public List<IndexTemplateWithCluster> listLogicTemplateWithClusters(Set<Integer> logicTemplateIds) {
        List<IndexTemplateWithPhyTemplates> physicalTemplates = listLogicTemplateWithPhysicalsByIds(logicTemplateIds);

        return convert2WithCluster(physicalTemplates).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 查询模板资源信息
     *
     * @return 带有逻辑集群的所有逻辑模板列表
     */
    @Override
    public List<IndexTemplateWithCluster> listAllLogicTemplateWithClusters() {
        List<IndexTemplateWithPhyTemplates> logicTemplatesCombinePhysicals = listAllLogicTemplateWithPhysicals();

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
    public List<IndexTemplateWithCluster> listLogicTemplateWithClustersByClusterId(Long logicClusterId) {
        List<IndexTemplateWithCluster> allClusterMetas = listAllLogicTemplateWithClusters();

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
    public List<IndexTemplateWithPhyTemplates> listAllLogicTemplateWithPhysicals() {
        return batchConvertLogicTemplateCombinePhysicalWithCache(indexTemplateDAO.listAll());
    }

    @Override
    public List<IndexTemplateWithPhyTemplates> listLogicTemplateWithPhysicalsByIds(Set<Integer> logicTemplateIds) {
        if (CollectionUtils.isEmpty(logicTemplateIds)) {
            return new ArrayList<>();
        }
        return batchConvertLogicTemplateCombinePhysical(indexTemplateDAO.listByIds(new ArrayList<>(logicTemplateIds)));
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
     *
     * @return list
     */
    @Override
    public List<IndexTemplateWithPhyTemplates> listTemplateWithPhysical() {
        return batchConvertLogicTemplateCombinePhysical(indexTemplateDAO.listAll());
    }

    /**
     * 更新读状态
     *
     * @param logicId   逻辑模板
     * @param blockRead 是否禁读
     * @return
     * @throws AdminOperateException
     */
    @Override
    public Result<Void> updateBlockReadState(Integer logicId, Boolean blockRead) {
        if (null == logicId || null == blockRead) {
            return Result.buildFail("logicId or blockRead is null");
        }

        return Result.build(1 == indexTemplateDAO.updateBlockReadState(logicId, blockRead));
    }

    /**
     * 更新写状态
     *
     * @param logicId    逻辑模板
     * @param blockWrite 是否禁写
     * @return
     */
    @Override
    public Result<Void> updateBlockWriteState(Integer logicId, Boolean blockWrite) {
        if (null == logicId || null == blockWrite) {
            return Result.buildFail("logicId or blockWrite is null");
        }

        return Result.build(indexTemplateDAO.updateBlockWriteState(logicId, blockWrite) == 1);
    }

    @Override
    public Result updateTemplateWriteRateLimit(ConsoleTemplateRateLimitDTO dto) throws ESOperateException {
     
        IndexTemplatePO oldPO = indexTemplateDAO.getById(dto.getLogicId());
        IndexTemplatePO editTemplate = ConvertUtil.obj2Obj(dto, IndexTemplatePO.class);
        editTemplate.setId(dto.getLogicId());
        editTemplate.setWriteRateLimit(dto.getAdjustRateLimit());
        int update = indexTemplateDAO.update(editTemplate);
        if (update > 0) {
            IndexTemplateDTO param = ConvertUtil.obj2Obj(editTemplate, IndexTemplateDTO.class);
            param.setId(dto.getLogicId());
            // 将修改同步到物理模板
            Result editPhyResult = indexTemplatePhyService.editTemplateFromLogic(param, AriusUser.SYSTEM.getDesc());
            if (editPhyResult.failed()) {
                return Result.buildFail("修改限流，修改物理模板失败");
            }

            SpringTool.publish(new LogicTemplateModifyEvent(this, ConvertUtil.obj2Obj(oldPO, IndexTemplate.class),
                getLogicTemplateById(oldPO.getId())));
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

    @Override
    public Result<List<IndexTemplate>> listByRegionId(Integer regionId) {
        Result<List<IndexTemplatePhy>> phyListResult = indexTemplatePhyService.listByRegionId(regionId);
        if (phyListResult.failed() && CollectionUtils.isEmpty(phyListResult.getData())) {
            return Result.buildFail(phyListResult.getMessage());
        }

        List<Integer> logicTemplateIdList = phyListResult.getData().stream().map(IndexTemplatePhy::getLogicId)
            .distinct().collect(Collectors.toList());
        List<IndexTemplate> logicTemplateList = listLogicTemplatesByIds(logicTemplateIdList);
        return Result.buildSucc(logicTemplateList);
    }

    @Override
    public List<IndexTemplateWithCluster> convert2WithCluster(List<IndexTemplateWithPhyTemplates> indexTemplateWithPhyTemplates) {
        if (CollectionUtils.isEmpty(indexTemplateWithPhyTemplates)) {
            return new ArrayList<>();
        }

        List<IndexTemplateWithCluster> res = Lists.newArrayList();
        for (IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplate : indexTemplateWithPhyTemplates) {
            IndexTemplateWithCluster indexTemplateWithCluster = ConvertUtil.obj2Obj(indexTemplateWithPhyTemplate,
                IndexTemplateWithCluster.class);
            IndexTemplatePhy masterPhyTemplate = indexTemplateWithPhyTemplate.getMasterPhyTemplate();
             if (null == masterPhyTemplate) {
                continue;
            }
            res.add(indexTemplateWithCluster);

            ClusterRegion region = clusterRegionService.getRegionById(masterPhyTemplate.getRegionId().longValue());
            if (null == region) {
                continue;
            }

            String logicClusterIds = region.getLogicClusterIds();
            if (REGION_NOT_BOUND_LOGIC_CLUSTER_ID.equals(logicClusterIds)) {
                continue;
            }
    
            List<String> logicClusterIdStrList = ListUtils.string2StrList(logicClusterIds);
            List<Long> logicClusterIdList = logicClusterIdStrList.stream().map(Long::parseLong).distinct()
                    .collect(Collectors.toList());
            //这里存在逻辑集群创建项目被超级项目拿来使用了，且创建了模版，但是集群个数1的情况
            final List<ClusterLogic> clusterLogicListByIds = clusterLogicService.getClusterLogicListByIds(
                    logicClusterIdList);
            if (clusterLogicListByIds.size() == 1) {
                indexTemplateWithCluster.setLogicClusters(
                      clusterLogicListByIds  );
            } else {
                List<ClusterLogic> clusterLogicList = clusterLogicListByIds
                        .stream().filter(clusterLogic -> Objects.equals(clusterLogic.getProjectId(),
                                indexTemplateWithPhyTemplate.getProjectId())).collect(Collectors.toList());
                indexTemplateWithCluster.setLogicClusters(clusterLogicList);
            }
            
                    

        }
        return res;
    }

    @Override
    public List<IndexTemplate> listByResourceIds(List<Long> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return new ArrayList<>();
        }

        return ConvertUtil.list2List(indexTemplateDAO.listByResourceIds(resourceIds), IndexTemplate.class);
    }

    /**
     * @param templateLogicId 模板逻辑id
     * @return
     */
    @Override
    public Integer getProjectIdByTemplateLogicId(Integer templateLogicId) {
        return indexTemplateDAO.getProjectIdByTemplateLogicId(templateLogicId);
    }

    /**
     * @param logicId
     * @return
     */
    @Override
    public IndexTemplatePO getLogicTemplatePOById(Integer logicId) {
        return indexTemplateDAO.getById(logicId);
    }

    /**
     * @param editTemplate
     * @return
     */
    @Override
    public boolean update(IndexTemplatePO editTemplate) {
        return indexTemplateDAO.update(editTemplate) == 1;
    }

    /**
     * @param days
     * @param templateIdList
     * @return
     */
    @Override
    public int batchChangeHotDay(Integer days, List<Integer> templateIdList) {

        return indexTemplateDAO.batchChangeHotDay(days, templateIdList);
    }

    /**
     * @param logicTemplateId
     * @return
     */
    @Override
    public String getNameByTemplateLogicId(Integer logicTemplateId) {
        return indexTemplateDAO.getNameByTemplateLogicId(logicTemplateId);
    }
    
    /**
     * @param projectId
     * @return
     */
    @Override
    public List<Integer> getLogicTemplateIdListByProjectId(Integer projectId) {
        return indexTemplateDAO.getLogicTemplateIdListByProjectId(projectId);
    }
    
    /**************************************** private method ****************************************************/
    private List<IndexTemplateWithPhyTemplates> batchConvertLogicTemplateCombinePhysicalWithFunction(List<IndexTemplatePO> logicTemplates,
                                                                                         Supplier<List<IndexTemplatePhy>> supplier){
        if (CollectionUtils.isEmpty(logicTemplates)) {
            return Lists.newArrayList();
        }

        // 逻辑模板对应1到多个物理模板
        Multimap<Integer, IndexTemplatePhy> logicId2PhysicalTemplatesMapping = ConvertUtil
            .list2MulMap(supplier.get(), IndexTemplatePhy::getLogicId);

        List<IndexTemplateWithPhyTemplates> indexTemplateCombinePhysicalTemplates = Lists
            .newArrayListWithCapacity(logicTemplates.size());

        for (IndexTemplatePO logicTemplate : logicTemplates) {
            IndexTemplateWithPhyTemplates logicWithPhysical = ConvertUtil.obj2Obj(logicTemplate,
                IndexTemplateWithPhyTemplates.class);
            logicWithPhysical
                .setPhysicals(Lists.newArrayList(logicId2PhysicalTemplatesMapping.get(logicTemplate.getId())));

            indexTemplateCombinePhysicalTemplates.add(logicWithPhysical);
        }

        return indexTemplateCombinePhysicalTemplates;
        
    }
    
    private List<IndexTemplateWithPhyTemplates> batchConvertLogicTemplateCombinePhysicalWithCache(
            List<IndexTemplatePO> logicTemplates) {
        return batchConvertLogicTemplateCombinePhysicalWithFunction(logicTemplates,
                () -> indexTemplatePhyService.listTemplateWithCache());
    }
    /**
     * 转换逻辑模板，获取并组合对应的物理模板信息
     * @param logicTemplates 逻辑模板列表
     * @return
     */
    private List<IndexTemplateWithPhyTemplates> batchConvertLogicTemplateCombinePhysical(List<IndexTemplatePO> logicTemplates) {

       

        return batchConvertLogicTemplateCombinePhysicalWithFunction(logicTemplates, ()->indexTemplatePhyService.listTemplate());
    }

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
        configPO.setAdjustTpsFactor(1.0);
        configPO.setAdjustShardFactor(1.0);
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
    private Result<Void> editTemplateWithoutCheck(IndexTemplateDTO param,
                                                  String operator) throws AdminOperateException {

        if (param.getDateFormat() != null) {
            param.setDateFormat(param.getDateFormat().replace("Y", "y"));
        }

        IndexTemplatePO oldPO = indexTemplateDAO.getById(param.getId());
        IndexTemplatePO editTemplate = ConvertUtil.obj2Obj(param, IndexTemplatePO.class);

        boolean succeed = (1 == indexTemplateDAO.update(editTemplate));

        if (succeed) {
            param.setId(editTemplate.getId());
            // 将修改同步到物理模板
            Result<Void> editPhyResult = indexTemplatePhyService.editTemplateFromLogic(param, operator);
            if (editPhyResult.failed()) {
                throw new AdminOperateException("修改物理模板失败");
            }

            SpringTool.publish(new LogicTemplateModifyEvent(this, ConvertUtil.obj2Obj(oldPO, IndexTemplate.class),
                getLogicTemplateById(oldPO.getId())));
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

    private Result<Void> validateIndexTemplateLogicStep2(IndexTemplateDTO param, String dateFormatFinal,
                                                         String expressionFinal, String nameFinal,
                                                         String dateFieldFinal) {
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
        if (param.getExpireTime() != null && param.getExpireTime() > 0
            && param.getExpireTime() < AdminConstant.PLATFORM_EXPIRE_TIME_MIN) {
            return Result
                .buildParamIllegal(String.format("分区索引模板数据保存天数不能小于%d天", AdminConstant.PLATFORM_EXPIRE_TIME_MIN));
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

    private Result<Void> validateIndexTemplateLogicStep1(IndexTemplateDTO param,
                                                         List<IndexTemplate> indexTemplateList) {
        // 校验索引名字
        if (param.getName() != null) {
            Result<Void> result = validateIndexName(param, indexTemplateList);
            if (result.failed()) {
                return result;
            }
        }
        if (param.getExpression() != null) {
            Result<Void> result = validateExpression(param, indexTemplateList);
            if (result.failed()) {
                return result;
            }
        }
        if (param.getDataCenter() != null && !DataCenterEnum.validate(param.getDataCenter())) {
            return Result.buildParamIllegal("数据中心非法");
        }
        if (param.getProjectId() != null && !projectService.checkProjectExist(param.getProjectId())) {
            return Result.buildParamIllegal("所属应用不存在");
        }
        if (param.getDataType() != null && UNKNOW_DATA_TYPE.getCode().equals(param.getDataType())) {
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
        if (AriusObjUtils.isNull(param.getProjectId())) {
            return Result.buildParamIllegal("所属应用为空");
        }
        if (AriusObjUtils.isNull(param.getDataType())) {
            return Result.buildParamIllegal("数据类型为空");
        }
        if (AriusObjUtils.isNull(param.getExpireTime())) {
            return Result.buildParamIllegal("保存时长为空");
        }

        if (AriusObjUtils.isNull(param.getExpression())) {
            return Result.buildParamIllegal("表达式为空");
        }
        if (AriusObjUtils.isNull(param.getDataCenter())) {
            return Result.buildParamIllegal("数据中心为空");
        }
        if (AriusObjUtils.isNull(param.getDiskSize())) {
            return Result.buildParamIllegal("DiskSize为空");
        }
        if (AriusObjUtils.isNull(param.getWriteRateLimit())) {
            param.setWriteRateLimit(-1);
        }
        if (LevelEnum.valueOfCode(param.getLevel()).equals(LevelEnum.UNKNOWN)) {
            return Result.buildParamIllegal("模板设置的服务等级为未知类型");
        }
        if (levelOfTemplateLower(param)) {
            return Result.buildParamIllegal("模板设置的服务等级低于所属逻辑集群的服务等级");
        }
        if (!clusterLogicService.existClusterLogicById(param.getResourceId() )) {
            return Result.buildNotExist("逻辑集群不存在");
        }

        return Result.buildSucc();
    }

    private boolean levelOfTemplateLower(IndexTemplateDTO param) {
        ClusterLogic clusterLogic =
                clusterLogicService.getClusterLogicByIdThatNotContainsProjectId(param.getResourceId());
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
            return Result
                .buildParamIllegal(String.format("名称长度非法, %s-%s", TEMPLATE_NAME_SIZE_MIN, TEMPLATE_NAME_SIZE_MAX));
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