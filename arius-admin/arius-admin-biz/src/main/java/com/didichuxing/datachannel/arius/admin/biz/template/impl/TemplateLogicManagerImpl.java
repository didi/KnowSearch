package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.DEFAULT_INDEX_MAPPING_TYPE;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.G_PER_SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.TEMPLATE_LOGIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.TEMPLATE_NAME_CHAR_SET;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.TEMPLATE_NAME_SIZE_MAX;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.TEMPLATE_NAME_SIZE_MIN;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum.OWN;
import static com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum.isTemplateAuthExitByCode;
import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.FAIL;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateSettingEnum.INDEX_PRIORITY;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateSettingEnum.INDEX_TRANSLOG_DURABILITY;
import static com.didichuxing.datachannel.arius.admin.core.service.template.physic.impl.IndexTemplatePhyServiceImpl.NOT_CHECK;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.biz.page.TemplateLogicPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.ColdManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplateLogicSettingsManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplateValue;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesIncrementalSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateWithCreateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateIncrementalSettingsDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.*;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellWithTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateClearVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateDeleteVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateRateLimitVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateCyclicalRollInfoVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.ESSettingConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.template.*;
import com.didichuxing.datachannel.arius.admin.common.event.index.IndexDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateCreatePipelineEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AmsRemoteException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateStatsService;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.elasticsearch.client.utils.JsonUtils;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Component
public class TemplateLogicManagerImpl implements TemplateLogicManager {

    private static final ILog               LOGGER                = LogFactory.getLog(TemplateLogicManager.class);
    private static final String             INDEX_NOT_EXISTS_TIPS = "索引不存在";
    private static final String DYNAMIC_TEMPLATES = "dynamic_templates";
    private static final String PROPERTIES        = "properties";
    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Autowired
    private TemplateStatsService            templateStatsService;

    @Autowired
    private ColdManager                     templateColdManager;

    @Autowired
    private IndexTemplateService            indexTemplateService;

    @Autowired
    private IndexTemplatePhyService         indexTemplatePhyService;

    @Autowired
    private ClusterPhyService               clusterPhyService;

    @Autowired
    private ClusterRegionService            clusterRegionService;

    @Autowired
    private OperateRecordService            operateRecordService;

    @Autowired
    private ProjectService                  projectService;

    @Autowired
    private ESIndexService                  esIndexService;

    @Autowired
    private ESTemplateService               esTemplateService;

    @Autowired
    private TemplatePhyManager              templatePhyManager;

    @Autowired
    private HandleFactory                   handleFactory;

    @Autowired
    private TemplateDCDRManager             templateDcdrManager;

    @Autowired
    private PreCreateManager                preCreateManager;

    @Autowired
    private ClusterLogicService             clusterLogicService;

    private final static Integer            RETRY_TIMES           = 3;

    @Autowired
    private IndicesManager                  indicesManager;

    @Autowired
    private PipelineManager                 templatePipelineManager;
    
    @Autowired
    protected ESClusterNodeService esClusterNodeService;

    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private TemplateLogicSettingsManager    templateLogicSettingsManager;

    public static final int                 MAX_PERCENT           = 10000;
    public static final int                 MIN_PERCENT           = -99;

    /**
     * 获取最近访问该模板的project
     *
     * @param logicId logicId
     * @return result
     */
    @Override
    public List<ProjectBriefVO> getLogicTemplateProjectAccess(Integer logicId) throws AmsRemoteException {
        Result<Map<Integer, Long>> result = templateStatsService.getTemplateAccessProjectIds(logicId, 7);
        if (result.failed()) {
            throw new AmsRemoteException("获取访问模板的project列表失败");
        }

        if (null == result.getData() || 0 == result.getData().size()) {
            return Lists.newArrayList();
        }

        return result.getData().keySet().stream().map(projectService::getProjectBriefByProjectId)
            .filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> create(IndexTemplateWithCreateInfoDTO param, String operator, Integer projectId) {
        IndexTemplateDTO indexTemplateDTO = buildTemplateDTO(param, projectId);
        Result<Void> validLogicTemplateResult = indexTemplateService.validateTemplate(indexTemplateDTO, ADD, projectId);
        if (validLogicTemplateResult.failed()) {
            return validLogicTemplateResult;
        }
    
        Result<Void> validPhyTemplateResult = indexTemplatePhyService.validateTemplates(
                indexTemplateDTO.getPhysicalInfos(), ADD);
        if (validPhyTemplateResult.failed()) {
            return validPhyTemplateResult;
        }

        final Map<String, String> setting = JsonUtils.flat(JSONObject.parseObject(param.getSetting()));
        if (setting.containsKey(ESSettingConstant.INDEX_NUMBER_OF_SHARDS) || setting.containsKey(
                ESSettingConstant.INDEX_ROUTING_ALLOCATION_INCLUDE_NAME) || setting.containsKey(
                ESSettingConstant.INDEX_ROUTING_ALLOCATION_INCLUDE_RACK)) {
            return Result.buildFail(
                    "\"index.number_of_shards \"和 \"index.routing.allocation.include._name \"和 \"index.routing.allocation.include.rack \"三个字段系统会自动计算，不支持用户自定义设置。");
        }

        try {
            Result<Void> save2DBResult = indexTemplateService.addTemplateWithoutCheck(indexTemplateDTO);
            if (save2DBResult.failed()) {
                throw new AdminOperateException(String.format("创建模板失败:%s", save2DBResult.getMessage()));
            }

            // build模版settings，创建物理模版
            Result<Void> save2PhyTemplateResult = templatePhyManager.addTemplatesWithoutCheck(indexTemplateDTO.getId(),
                    indexTemplateDTO.getPhysicalInfos());
            if (save2PhyTemplateResult.failed()) {
                throw new AdminOperateException(String.format("创建模板失败:%s", save2PhyTemplateResult.getMessage()));
            }
        
            Result<Void> saveTemplateConfigResult = insertTemplateConfig(indexTemplateDTO);
            if (saveTemplateConfigResult.failed()) {
                throw new AdminOperateException(String.format("创建模板失败:%s", saveTemplateConfigResult.getMessage()));
            }
    
            operateRecordService.saveOperateRecordWithManualTrigger(String.format("模版创建：%s", param.getName()),
                    operator, projectId, indexTemplateDTO.getId(), OperateTypeEnum.TEMPLATE_MANAGEMENT_CREATE);
            //发布创建pipeline的事件
            SpringTool.publish(new LogicTemplateCreatePipelineEvent(this,indexTemplateDTO.getId()));
            return Result.buildSucc();
        } catch (AdminOperateException e) {
            LOGGER.error("class=TemplateLogicManagerImpl||method=create", e);
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("模板创建出现admin操作异常，请重试");
        } catch (Exception e) {
            LOGGER.error("class=TemplateLogicManagerImpl||method=create", e);
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("模版创建失败，请重新尝试");
        }

    }

    /**
     * 获取所有逻辑模板聚合
     *
     * @param projectId 当前project Id
     * @return
     */
    @Override
    public List<IndexTemplateLogicAggregate> getAllTemplatesAggregate(Integer projectId) {
        List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = new ArrayList<>();
        List<IndexTemplateWithCluster> logicTemplates = indexTemplateService.listAllLogicTemplateWithClusters();
        return indexTemplateLogicAggregates;
    }

    /**
     * 拼接集群名称
     * @param logicClusters 逻辑集群详情列表
     * @return
     */
    @Override
    public String jointCluster(List<ClusterLogic> logicClusters) {
        if (CollectionUtils.isNotEmpty(logicClusters)) {
            return String.join(",", logicClusters.stream().map(ClusterLogic::getName).collect(Collectors.toList()));
        }
        return StringUtils.EMPTY;
    }

    /**
     *
     * @param aggregates 聚合列表
     * @return
     */
    @Override
    public List<ConsoleTemplateVO> fetchConsoleTemplates(List<IndexTemplateLogicAggregate> aggregates) {
        List<ConsoleTemplateVO> consoleTemplates = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(aggregates)) {
            Map<Integer, String> projectId2ProjectNameMap = Maps.newHashMap();
            for (IndexTemplateLogicAggregate aggregate : aggregates) {
                ConsoleTemplateVO consoleTemplateVO = fetchConsoleTemplate(aggregate);
                //获取项目名称
                Integer projectId = consoleTemplateVO.getProjectId();
                if (projectId2ProjectNameMap.containsKey(projectId)) {
                    consoleTemplateVO.setProjectName(projectId2ProjectNameMap.get(projectId));
                } else {
                    String projectName = Optional.ofNullable(projectService.getProjectBriefByProjectId(projectId))
                        .map(ProjectBriefVO::getProjectName).orElse(null);
                    if (!AriusObjUtils.isNull(projectName)) {
                        consoleTemplateVO.setProjectName(projectName);
                        projectId2ProjectNameMap.put(projectId, projectName);
                    }
                }
                consoleTemplates.add(consoleTemplateVO);
            }
        }
        Collections.sort(consoleTemplates);
        return consoleTemplates;
    }

    /**
     * 获取模板VO
     * @param aggregate 模板聚合
     * @return
     */
    @Override
    public ConsoleTemplateVO fetchConsoleTemplate(IndexTemplateLogicAggregate aggregate) {
        if (aggregate != null) {
            ConsoleTemplateVO templateLogic = ConvertUtil.obj2Obj(aggregate.getIndexTemplateLogicWithCluster(),
                ConsoleTemplateVO.class);
            try {
                templateLogic.setAuthType(ProjectTemplateAuthEnum.NO_PERMISSION.getCode());
                if (aggregate.getProjectTemplateAuth() != null) {
                    templateLogic.setAuthType(aggregate.getProjectTemplateAuth().getType());
                }

                templateLogic.setValue(DEFAULT_TEMPLATE_VALUE);
                if (aggregate.getIndexTemplateValue() != null) {

                    templateLogic.setValue(aggregate.getIndexTemplateValue().getValue());
                }
                templateLogic.setHasDCDR(templateLogic.getHasDCDR());

                //设置模板关联物理集群
                List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService
                    .getTemplateByLogicId(templateLogic.getId());
                if (CollectionUtils.isNotEmpty(templatePhyList)) {
                    templateLogic.setClusterPhies(
                        templatePhyList.stream().map(IndexTemplatePhy::getCluster).collect(Collectors.toList()));
                }
            } catch (Exception e) {
                LOGGER.warn("class=TemplateLogicManager||method=fetchConsoleTemplate||aggregate={}", aggregate, e);
            }

            return templateLogic;
        }

        return null;
    }

    @Override
    public List<ConsoleTemplateVO> getConsoleTemplatesVOS(Integer projectId) {
        return fetchConsoleTemplates(getAllTemplatesAggregate(projectId));
    }

    @Override
    public List<IndexTemplate> getTemplatesByProjectIdAndAuthType(Integer projectId, Integer authType) {
        if (!projectService.checkProjectExist(projectId)) {
            return Lists.newArrayList();
        }

        //超级项目对所有模板都是管理权限
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId) && !OWN.getCode().equals(authType)) {
            return Lists.newArrayList();
        }

        if (!isTemplateAuthExitByCode(authType)) {
            return Lists.newArrayList();
        }

        switch (ProjectTemplateAuthEnum.valueOf(authType)) {
            case OWN:
                if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                    return indexTemplateService.listAllLogicTemplates();
                } else {
                    return indexTemplateService.listProjectLogicTemplatesByProjectId(projectId);
                }

            case RW:
                List<ProjectTemplateAuth> projectActiveTemplateRWAuths = projectLogicTemplateAuthService
                    .getProjectActiveTemplateRWAuths(projectId);
                return projectActiveTemplateRWAuths.stream()
                    .map(r -> indexTemplateService.getLogicTemplateById(r.getTemplateId()))
                    .collect(Collectors.toList());

            case R:
                List<ProjectTemplateAuth> projectActiveTemplateRAuths = projectLogicTemplateAuthService
                    .getProjectActiveTemplateRAuths(projectId);
                return projectActiveTemplateRAuths.stream()
                    .map(r -> indexTemplateService.getLogicTemplateById(r.getTemplateId()))
                    .collect(Collectors.toList());

            case NO_PERMISSION:
                List<IndexTemplate> allLogicTemplates = indexTemplateService.listAllLogicTemplates();
                List<Integer> projectRAndRwAuthTemplateIdList = projectLogicTemplateAuthService
                    .getProjectTemplateRWAndRAuthsWithoutCodec(projectId).stream()
                    .map(ProjectTemplateAuth::getTemplateId).collect(Collectors.toList());

                List<IndexTemplate> notAuthIndexTemplateList = allLogicTemplates.stream().filter(
                    r -> !projectId.equals(r.getProjectId()) && !projectRAndRwAuthTemplateIdList.contains(r.getId()))
                    .collect(Collectors.toList());
                return notAuthIndexTemplateList;

            default:
                return Lists.newArrayList();

        }
    }

    @Override
    public List<String> getTemplateLogicNames(Integer projectId) {
        List<IndexTemplate> templateLogics = indexTemplateService.listProjectLogicTemplatesByProjectId(projectId);
        return templateLogics.stream().map(IndexTemplate::getName).collect(Collectors.toList());
    }

    @Override
    public Result<Void> editTemplate(IndexTemplateDTO param, String operator, Integer projectId) {
        try {
            final IndexTemplate oldIndexTemplate = indexTemplateService.getLogicTemplateById(param.getId());
            final Result<Void> result = ProjectUtils.checkProjectCorrectly(IndexTemplate::getProjectId,
                oldIndexTemplate, projectId);
            if (result.failed()) {
                return result;
            }
            final Result<Void> voidResult = indexTemplateService.editTemplateInfoTODB(param);
            if (voidResult.success()) {
                String dataTypeBefore= DataTypeEnum.valueOf(oldIndexTemplate.getDataType()).getDesc();
                String dataTypeAfter= DataTypeEnum.valueOf(param.getDataType()).getDesc();
                String descBefore=oldIndexTemplate.getDesc();
                String descAfter=param.getDesc();
    
                operateRecordService.saveOperateRecordWithManualTrigger(
                        String.format("数据类型变更：【%s】->【%s】; 描述变更:【%s】->【%s】", dataTypeBefore, dataTypeAfter,
                                descBefore, descAfter), operator, projectId, param.getId(),
                        OperateTypeEnum.TEMPLATE_MANAGEMENT_INFO_MODIFY);
            }

            return voidResult;
        } catch (AdminOperateException e) {
            LOGGER.error("class=TemplateLogicManagerImpl||method=newEditTemplate||msg=fail to editTemplate");
        }
        return Result.buildFail("编辑模板失败");
    }

    @Override
    public Result<Void> delTemplate(Integer logicTemplateId, String operator,
                                    Integer projectId) throws AdminOperateException {
        Integer belongToProjectId = indexTemplateService.getProjectIdByTemplateLogicId(logicTemplateId);
        final Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(i -> i, belongToProjectId,
            projectId);
        if (checkProjectCorrectly.failed()) {
            return checkProjectCorrectly;
        }
        List<String> phyClusterList=indexTemplatePhyService.getPhyClusterByLogicTemplateId(logicTemplateId);
       
        Result<ConsoleTemplateClearVO> templateClearInfo = getLogicTemplateClearInfo(
                logicTemplateId);
       
        String beforeDeleteName = indexTemplateService.getNameByTemplateLogicId(logicTemplateId);
        Result<Void> result = indexTemplateService.delTemplate(logicTemplateId, operator);
        if (result.success()) {
            operateRecordService.saveOperateRecordWithManualTrigger(String.format("模板【%s】下线", beforeDeleteName),
                    operator, projectId, logicTemplateId, OperateTypeEnum.TEMPLATE_MANAGEMENT_OFFLINE);
            //一并下线模板关联的索引
            /**
             * [{"cluster":"Zh_test3_cluster_7-6-0-1400","index":"zh_test3_template3_2022-07-18"}]
             */
            if (templateClearInfo.success() && CollectionUtils.isNotEmpty(templateClearInfo.getData().getIndices())&&CollectionUtils.isNotEmpty(phyClusterList)) {
    
                BiFunction</*index*/String,/*phyCluster*/String, IndexCatCellDTO> indexPhyClusterFunc = (index, phyCluster) -> {
                    IndexCatCellDTO indexCatCellDTO = new IndexCatCellDTO();
                    indexCatCellDTO.setIndex(index);
                    indexCatCellDTO.setCluster(phyCluster);
                    return indexCatCellDTO;
                };
                Function</*index*/String,List<IndexCatCellDTO>> phyClusterFunc=
                        index->phyClusterList.stream().map(phyCluster->indexPhyClusterFunc.apply(index,phyCluster))
                                       .collect(Collectors.toList());
    
                List<IndexCatCellDTO> catCellList = templateClearInfo.getData().getIndices()
                       
                        .stream()
                        .map(IndexCatCellWithTemplateVO::getIndex)
                        .map(phyClusterFunc).flatMap(Collection::stream)
            
                        .collect(Collectors.toList());
                SpringTool.publish(new IndexDeleteEvent(this, catCellList, projectId, operator));
            }
        }
        return result;
    }

    @Override
    public PaginationResult<ConsoleTemplateVO> pageGetConsoleTemplateVOS(TemplateConditionDTO condition,
                                                                         Integer projectId) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(TEMPLATE_LOGIC.getPageSearchType());
        if (baseHandle instanceof TemplateLogicPageSearchHandle) {
            TemplateLogicPageSearchHandle handle = (TemplateLogicPageSearchHandle) baseHandle;
            return handle.doPage(condition, projectId);
        }

        LOGGER.warn(
            "class=TemplateLogicManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the TemplateLogicPageSearchHandle");

        return PaginationResult.buildFail("获取模板分页信息失败");
    }

    @Override
    public Result<Void> checkTemplateValidForCreate(String templateName) {
        if (AriusObjUtils.isNull(templateName)) {
            return Result.buildParamIllegal("名字为空");
        }

        if (templateName.length() < TEMPLATE_NAME_SIZE_MIN || templateName.length() > TEMPLATE_NAME_SIZE_MAX) {
            return Result
                .buildParamIllegal(String.format("名称长度非法, %s-%s", TEMPLATE_NAME_SIZE_MIN, TEMPLATE_NAME_SIZE_MAX));
        }

        for (Character c : templateName.toCharArray()) {
            if (!TEMPLATE_NAME_CHAR_SET.contains(c)) {
                return Result.buildParamIllegal("名称包含非法字符, 只能包含小写字母、数字、-、_和.");
            }
        }

        return indexTemplateService.preCheckTemplateName(templateName);
    }

    @Override
    public Result<Boolean> checkTemplateEditMapping(Integer templateId) {
        IndexTemplate indexTemplate = indexTemplateService.getLogicTemplateById(templateId);
        if (null == indexTemplate) {
            LOGGER.error(
                "class=TemplateLogicManagerImpl||method=checkTemplateEditMapping||templateId={}||msg=indexTemplateLogic is empty",
                templateId);
            return Result.buildFail("模板不存在");
        }

        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(indexTemplate.getId());
        if (CollectionUtils.isEmpty(templatePhyList)) {
            return Result.buildSucc(false);
        }

        List<String> clusterPhyNameList = templatePhyList.stream().map(IndexTemplatePhy::getCluster).distinct()
            .collect(Collectors.toList());
        for (String clusterPhyName : clusterPhyNameList) {
            ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
            if (null == clusterPhy) {
                return Result.buildFail(String.format("模板归属集群[%s]不存在", clusterPhyName));
            }
        }

        return Result.buildSucc(true);
    }

    @Override
    public Result<Void> switchRolloverStatus(Integer templateLogicId, Integer status, String operator,
                                             Integer projectId) {
        if (templateLogicId == null || status == null) {
            return Result.buildSucc();
        }
        Boolean newDisable = status == 0;
        IndexTemplateConfig templateConfig = indexTemplateService.getTemplateConfig(templateLogicId);
        if (templateConfig == null) {
            return Result.buildFail("模版不存在");
        }
        final Integer projectIdByTemplateLogicId = indexTemplateService.getProjectIdByTemplateLogicId(templateLogicId);

        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectIdByTemplateLogicId, projectId);
        if (result.failed()) {
            return result;
        }
        Boolean oldDisable = templateConfig.getDisableIndexRollover();
        if (!newDisable.equals(oldDisable)) {
            // 如果状态不同则更新状态
            IndexTemplateConfigDTO indexTemplateConfigDTO = ConvertUtil.obj2Obj(templateConfig,
                IndexTemplateConfigDTO.class);
            indexTemplateConfigDTO.setDisableIndexRollover(newDisable);
            Result<Void> updateStatusResult = indexTemplateService.updateTemplateConfig(indexTemplateConfigDTO,
                operator);
            if (updateStatusResult.success()) {
                // rollover状态修改记录(兼容开启或者关闭)
                operateRecordService.saveOperateRecordWithManualTrigger(
                        String.format("%s:rollover 状态修改为:【%s】", TemplateOperateRecordEnum.ROLLOVER.getDesc(),
                                (newDisable ? "关闭" : "开启")), operator, projectId, templateLogicId,
                        OperateTypeEnum.TEMPLATE_SERVICE);
            }
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Boolean> checkTemplateEditService(Integer templateId, Integer templateSrvId) {
        // 根据逻辑模板id获取对应的逻辑物理模板信息
        IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService
            .getLogicTemplateWithPhysicalsById(templateId);

        if (AriusObjUtils.isNull(logicTemplateWithPhysicals)
            || CollectionUtils.isEmpty(logicTemplateWithPhysicals.getPhysicals())) {
            LOGGER.error(
                "class=TemplateLogicManagerImpl||method=checkTemplateEditService||templateId={}||msg=indexTemplateLogic is empty",
                templateId);
            return Result.buildFail("逻辑模板信息为空");
        }

        // 获取逻辑集群对应的物理模板的物理集群名称列表
        final Optional<Result<Boolean>> resultOptional = logicTemplateWithPhysicals.getPhysicals().stream()
                .map(IndexTemplatePhy::getCluster).distinct().map(this::checkExistClusterNamePhy).findFirst();
        return resultOptional.map(booleanResult -> Result.buildFailWithMsg(false, booleanResult.getMessage()))
                .orElseGet(() -> Result.buildSucc(true));
    }

    @Override
    public Result<Void> checkProjectAuthOnLogicTemplate(Integer logicId, Integer projectId) {
        if (AriusObjUtils.isNull(logicId)) {
            return Result.buildParamIllegal("索引id为空");
        }

        if (AriusObjUtils.isNull(projectId)) {
            return Result.buildParamIllegal("应用Id为空");
        }

        IndexTemplate templateLogic = indexTemplateService.getLogicTemplateById(logicId);
        if (templateLogic == null) {
            return Result.buildNotExist("索引不存在");
        }

        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildSucc();
        }

        if (!templateLogic.getProjectId().equals(projectId)) {
            return Result.buildOpForBidden("您无权对该索引进行操作");
        }

        return Result.buildSucc();
    }

    @Override
    public boolean updateDCDRInfo(Integer logicId) {
        if (!indexTemplateService.exist(logicId)) {
            return true;
        }

        // 1. 获取dcdr标识位
        boolean dcdrFlag = false;
        long totalIndexCheckPointDiff = 0;
        try {
            IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService
                .getLogicTemplateWithPhysicalsById(logicId);
            IndexTemplatePhy slavePhyTemplate = logicTemplateWithPhysicals.getSlavePhyTemplate();
            IndexTemplatePhy masterPhyTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
            if (null != masterPhyTemplate && null != slavePhyTemplate) {
                dcdrFlag = templateDcdrManager.syncExistTemplateDCDR(masterPhyTemplate.getId(),
                        slavePhyTemplate.getCluster());
                
            }
        } catch (Exception e) {
            LOGGER.error("class={}||method=updateDCDRInfo||templateName={}",getClass().getSimpleName(), logicId, e);
            return false;
        }

        // 2. 获取位点差dcdr
        if (dcdrFlag) {
            try {
                Tuple<Long, Long> masterAndSlaveTemplateCheckPointTuple = templateDcdrManager
                    .getMasterAndSlaveTemplateCheckPoint(logicId);
                totalIndexCheckPointDiff = Math
                    .abs(masterAndSlaveTemplateCheckPointTuple.getV1() - masterAndSlaveTemplateCheckPointTuple.getV2());
            } catch (Exception e) {
                LOGGER.error("class={}||method=updateDCDRInfo||templateId={}",
                    getClass().getSimpleName(),logicId,  e);
                 return false;
            }
        }

        try {
            IndexTemplateDTO indexTemplateDTO = new IndexTemplateDTO();
            indexTemplateDTO.setId(logicId);
            indexTemplateDTO.setHasDCDR(dcdrFlag);
            //如果还存在dcdr链路，则未totalIndexCheckPointDiff 否则未-1
            indexTemplateDTO.setCheckPointDiff(Boolean.TRUE.equals(dcdrFlag)?totalIndexCheckPointDiff:-1);
            indexTemplateService.editTemplateInfoTODB(indexTemplateDTO);
        } catch (AdminOperateException e) {
            LOGGER.error(
                "class={}||method=updateDCDRInfo||templateId={}",
                getClass().getSimpleName(),logicId, e);
        }

        return true;
    }

    @Override
    public Result<List<ConsoleTemplateVO>> getTemplateVOByPhyCluster(String phyCluster) {
        // 根据物理集群名称获取全量逻辑模板列表
        List<IndexTemplatePhyWithLogic> templateByPhyCluster = indexTemplatePhyService
            .getTemplateByPhyCluster(phyCluster);

        // 转化为视图列表展示
        List<ConsoleTemplateVO> consoleTemplateVOLists = new ArrayList<>();
        templateByPhyCluster.stream()
            .filter(indexTemplatePhyWithLogic -> indexTemplatePhyWithLogic.getLogicTemplate() != null).forEach(
                indexTemplatePhyWithLogic -> consoleTemplateVOLists.add(buildTemplateVO(indexTemplatePhyWithLogic)));
        return Result.buildSucc(consoleTemplateVOLists);
    }

    @Override
    public Result<Void> clearIndices(TemplateClearDTO clearDTO, String operator, Integer projectId) {
        List<String> indices = clearDTO.getDelIndices();
        Integer templateId = clearDTO.getLogicId();
        if (CollectionUtils.isEmpty(indices)) {
            return Result.buildParamIllegal("清理索引不能为空");
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(templateId);
        if (null != templateLogicWithPhysical && CollectionUtils.isEmpty(templateLogicWithPhysical.getPhysicals())) {
            return Result.buildFail(String.format("模板[%d]不存在Arius平台", templateId));
        }
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(IndexTemplateWithPhyTemplates::getProjectId,
            templateLogicWithPhysical, projectId);
        if (result.failed()) {
            return result;
        }

        boolean succ = false;
        List<IndexTemplatePhy> indexTemplatePhyList = Optional.ofNullable(templateLogicWithPhysical)
            .map(IndexTemplateWithPhyTemplates::getPhysicals).orElse(Lists.newArrayList());
        List<String> clusterList = Lists.newArrayList();
        for (IndexTemplatePhy templatePhysical : indexTemplatePhyList) {
            succ = indices.size() == esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(), indices,
                RETRY_TIMES);
            clusterList.add(templatePhysical.getCluster());

        }

        for (String cluster : clusterList) {
            indicesManager.updateIndexFlagInvalid(cluster, indices);

        }
        String name = Optional.ofNullable(templateLogicWithPhysical).map(IndexTemplateWithPhyTemplates::getName)
            .orElse("");
        String clearIndices = String.join(",", indices);
        operateRecordService.saveOperateRecordWithManualTrigger(
                String.format("清理索引模板：%s 下的索引列表：【%s】", name, clearIndices), operator, projectId,
                clearDTO.getLogicId(), OperateTypeEnum.TEMPLATE_SERVICE_CLEAN);

        return Result.build(succ);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> adjustShard(Integer logicTemplateId, Integer shardNum, Integer projectId,
                                    String operator) throws AdminOperateException {
        final Integer projectIdByTemplateLogicId = indexTemplateService.getProjectIdByTemplateLogicId(logicTemplateId);
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectIdByTemplateLogicId, projectId);
        if (result.failed()) {
            return result;
        }
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);
        IndexTemplatePhyDTO updateParam = new IndexTemplatePhyDTO();
        for (IndexTemplatePhy templatePhy : templatePhyList) {
            if (templatePhy.getShard().equals(shardNum)) {
                throw new AdminOperateException("该模板已经是" + shardNum + "分片", FAIL);
            }
    
            updateParam.setId(templatePhy.getId());
            updateParam.setShard(shardNum);
            boolean succ = esTemplateService.syncUpdateShardNum(templatePhy.getCluster(), templatePhy.getName(),
                    shardNum, RETRY_TIMES);
            if (succ) {
                Result<Void> updateDBResult = indexTemplatePhyService.update(updateParam);
                if (updateDBResult.failed()) {
                    throw new AdminOperateException(updateDBResult.getMessage(), FAIL);
                }
                operateRecordService.saveOperateRecordWithManualTrigger(
                        String.format("同步修改 es 集群 [%s] 中模板[%s]shard 数[%d]", templatePhy.getCluster(),
                                templatePhy.getName(), shardNum), operator, projectId, logicTemplateId,
                        OperateTypeEnum.TEMPLATE_SERVICE_CAPACITY);
            }
        }

        return Result.buildSucc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> upgrade(Integer templateId, String operator, Integer projectId) throws AdminOperateException {
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (result.failed()) {
            return result;
        }
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(templateId);
        if (CollectionUtils.isEmpty(templatePhyList)) {
            return Result.buildFail("模板不存在");
        }
        //检测集群连通状态
        final Optional<TupleTwo<String, Boolean>> clusterConnectionStatusOption = templatePhyList.stream()
                .map(indexTemplatePhy -> Tuples.of(indexTemplatePhy.getCluster(),
                        esClusterService.isConnectionStatus(indexTemplatePhy.getCluster())))
                .filter(tuple -> Boolean.FALSE.equals(tuple.v2)).findFirst();
        if (clusterConnectionStatusOption.isPresent()) {
            return Result.buildFail(
                    String.format("集群%s故障，请检查集群状态后重试。", clusterConnectionStatusOption.get().v1()));
        }
        IndexTemplatePhyDTO updateParam = new IndexTemplatePhyDTO();
        //这里为了保证逻辑模版下所有物理模版均创建最新索引后再进行pipeline切换，需要遍历两次
        for (IndexTemplatePhy templatePhy : templatePhyList) {
            int version = null != templatePhy.getVersion() ? templatePhy.getVersion() : 0;
            version = version < 0 ? 1 : version + 1;
            /*
            这里提前创建当天索引
             1.避免因为getTemplateConfig失败，导致升版本后不分区索引mapping异常
             2.避免由于事务原因，导致当天最新版本的分区索引未被创建
             */
            if (!preCreateManager.syncCreateTodayIndexByPhysicalId(templatePhy.getId(), version)) {
                return Result.buildFail("创建当前最新版本索引失败，请稍后重试！");
            }
        }
        for (IndexTemplatePhy templatePhy : templatePhyList) {
            final Integer beforeVersion = templatePhy.getVersion();
            final Integer afterVersion =beforeVersion + 1;
            updateParam.setId(templatePhy.getId());
            updateParam.setVersion(beforeVersion + 1);
            Result<Void> editResult = templatePhyManager.editTemplateWithoutCheck(updateParam, operator, RETRY_TIMES);
            if (editResult.failed()) {
                throw new AdminOperateException(editResult.getMessage(), FAIL);
            }

            preCreateManager.asyncCreateTodayAndTomorrowIndexByPhysicalId(templatePhy.getId());
    
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("模板 [%s] 升版本 %d->%d", templatePhy.getName(), beforeVersion, afterVersion),
                    operator, projectId, templateId, OperateTypeEnum.TEMPLATE_SERVICE_UPGRADED_VERSION);
        }

        return Result.buildSucc();
    }

    @Override
    public Result<List<ConsoleTemplateVO>> listTemplateVOByLogicCluster(String clusterLogicName, Integer projectId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByNameAndProjectId(clusterLogicName, projectId);
        if (clusterLogic == null) {
            return Result.buildFail();
        }
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        if (clusterRegion == null) {
            return Result.buildFail();
        }
        Predicate<IndexTemplate> filterProjectId = indexTemplate -> AuthConstant.SUPER_PROJECT_ID.equals(projectId)
                                                                    || Objects.equals(indexTemplate.getProjectId(),
                projectId);
        Result<List<IndexTemplate>> listResult = indexTemplateService
            .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        List<ConsoleTemplateVO> vos = listResult.getData().stream()
                .filter(filterProjectId)
                
                .map(indexTemplate -> {
            ConsoleTemplateVO vo = new ConsoleTemplateVO();
            BeanUtils.copyProperties(indexTemplate, vo);
            return vo;
        }).collect(Collectors.toList());

        return Result.buildSucc(vos);
    }

    /**
     * @param projectId
     * @return
     */
    @Override
    public Result<List<Tuple<String, String>>> listLogicTemplatesByProjectId(Integer projectId) {
        return indexTemplateService.listLogicTemplatesByProjectId(projectId);
    }

    /**
     * @param logicId
     * @return
     */
    @Override
    public Result<List<TemplateCyclicalRollInfoVO>> getCyclicalRollInfo(Integer logicId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);
        if (templateLogicWithPhysical == null) {
            return Result.buildParamIllegal(INDEX_NOT_EXISTS_TIPS);
        }

        List<CatIndexResult> catIndexResults = Lists.newArrayList();

        List<IndexTemplatePhy> physicalMasters = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy physicalMaster : physicalMasters) {
            try {
                catIndexResults.addAll(indicesManager.listIndexCatInfoByTemplatePhyId(physicalMaster.getId()));
            } catch (Exception e) {
                LOGGER.warn("class=TemplateLogicManagerImpl||method=getCyclicalRollInfo||logicId={}||errMsg={}",
                    logicId, e.getMessage(), e);
            }
        }
        return Result.buildSucc(ConvertUtil.list2List(catIndexResults, TemplateCyclicalRollInfoVO.class));
    }

    /**
     * @param logicId
     * @return
     */
    @Override
    public Result<ConsoleTemplateRateLimitVO> getTemplateRateLimit(Integer logicId) {
        List<IndexTemplatePhy> indexTemplatePhysicalInfo = indexTemplatePhyService.getTemplateByLogicId(logicId);
        ConsoleTemplateRateLimitVO consoleTemplateRateLimitVO = new ConsoleTemplateRateLimitVO();
        IndexTemplatePhy indexTemplatePhysicalMasterInfo = new IndexTemplatePhy();
        for (IndexTemplatePhy item : indexTemplatePhysicalInfo) {
            if (TemplateDeployRoleEnum.MASTER.getCode().equals(item.getRole())) {
                indexTemplatePhysicalMasterInfo = item;
            }
        }
        consoleTemplateRateLimitVO.setRateLimit(templatePipelineManager.getRateLimit(indexTemplatePhysicalMasterInfo));
        return Result.buildSucc(consoleTemplateRateLimitVO);
    }

    /**
     * @param logicId
     * @param projectId
     * @return
     */
    @Override
    public Result<ConsoleTemplateDetailVO> getDetailVoByLogicId(Integer logicId, Integer projectId) {
        if (Objects.isNull(logicId)) {
            return Result.buildSucc();
        }
        IndexTemplateWithCluster indexTemplateLogicWithCluster = indexTemplateService
            .getLogicTemplateWithCluster(logicId);

        if (null == indexTemplateLogicWithCluster) {
            return Result.buildFail("模板对应资源不存在!");
        }

        ConsoleTemplateDetailVO consoleTemplateDetail = ConvertUtil.obj2Obj(indexTemplateLogicWithCluster,
            ConsoleTemplateDetailVO.class);

        consoleTemplateDetail.setCyclicalRoll(indexTemplateLogicWithCluster.getExpression().endsWith("*"));
        consoleTemplateDetail.setRecoveryPriorityLevel(indexTemplateLogicWithCluster.getPriorityLevel());
        //根据模板resourceId project获取逻辑集群
         ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByIdAndProjectId(
                    consoleTemplateDetail.getResourceId(), consoleTemplateDetail.getProjectId());
        // supperApp显示物理集群，其他项目显示逻辑集群
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            List<IndexTemplatePhy> indexTemplatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicId);
            String phyClusters = indexTemplatePhyList.stream().sorted(Comparator.comparing(IndexTemplatePhy::getRole))
                    .map(IndexTemplatePhy::getCluster).collect(Collectors.joining(","));
            consoleTemplateDetail.setCluster(phyClusters);
        } else {
            Optional.ofNullable(clusterLogic).map(ClusterLogic::getName).ifPresent(consoleTemplateDetail::setCluster);
        }
    
        // 逻辑集群设置集群类型与等级
        Optional.ofNullable(clusterLogic).ifPresent(cl -> {
            consoleTemplateDetail.setClusterLevel(cl.getLevel());
            consoleTemplateDetail.setClusterType(cl.getType());
        });
        consoleTemplateDetail.setAppName(
                projectService.getProjectBriefByProjectId(indexTemplateLogicWithCluster.getProjectId())
                        .getProjectName());
        consoleTemplateDetail.setIndices(getLogicTemplateIndices(logicId));
        consoleTemplateDetail.setEditable(true);
        // 获取 indexRollover 功能开启状态
        Optional.ofNullable(indexTemplateService.getTemplateConfig(logicId))
                .map(IndexTemplateConfig::getDisableIndexRollover)
                .ifPresent(consoleTemplateDetail::setDisableIndexRollover);

        return Result.buildSucc(consoleTemplateDetail);
    }

    /**
     * @param logicId
     * @return
     */
    @Override
    public Result<ConsoleTemplateClearVO> getLogicTemplateClearInfo(Integer logicId) throws AmsRemoteException {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildParamIllegal(INDEX_NOT_EXISTS_TIPS);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildParamIllegal("索引没有部署");
        }

        ConsoleTemplateClearVO consoleTemplateClearVO = new ConsoleTemplateClearVO();
        consoleTemplateClearVO.setLogicId(templateLogicWithPhysical.getId());
        consoleTemplateClearVO.setName(templateLogicWithPhysical.getName());
        consoleTemplateClearVO.setIndices(
            indicesManager.listIndexCatCellWithTemplateByTemplatePhyId(templateLogicWithPhysical.getMasterPhyTemplate().getId()));
        consoleTemplateClearVO.setAccessApps(getLogicTemplateProjectAccess(logicId));

        return Result.buildSucc(consoleTemplateClearVO);
    }

    /**
     * @param logicId
     * @return
     */
    @Override
    public Result<ConsoleTemplateDeleteVO> getLogicTemplateDeleteInfo(Integer logicId) throws AmsRemoteException {
        //与上清理索引信息接口实现合并
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);
        if (templateLogicWithPhysical == null) {
            return Result.buildParamIllegal(INDEX_NOT_EXISTS_TIPS);
        }

        ConsoleTemplateDeleteVO consoleTemplateDeleteVO = new ConsoleTemplateDeleteVO();
        consoleTemplateDeleteVO.setLogicId(templateLogicWithPhysical.getId());
        consoleTemplateDeleteVO.setName(templateLogicWithPhysical.getName());

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildParamIllegal("索引没有部署");
        }

        consoleTemplateDeleteVO.setAccessApps(getLogicTemplateProjectAccess(logicId));

        return Result.buildSucc(consoleTemplateDeleteVO);
    }
    
    /**
     *
     * 它通过其逻辑 ID 更新模板的健康状况。
     * {@link  TemplateHealthEnum}
     * @param logicId 模板的 logicId。
     * @return 一个布尔值。
     */
    @Override
    public boolean updateTemplateHealthByLogicId(Integer logicId) {
        if (!indexTemplateService.exist(logicId)) {
            return true;
        }
        IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = indexTemplateService.getLogicTemplateWithPhysicalsById(
                logicId);
        String masterCluster = Optional.ofNullable(indexTemplateWithPhyTemplates)
                .map(IndexTemplateWithPhyTemplates::getMasterPhyTemplate).map(IndexTemplatePhy::getCluster)
                .orElse(null);
        if (Objects.isNull(masterCluster)) {
            LOGGER.warn(
                    "class={}||method=updateTemplateHealthByLogicId||logicId={}||error=don't find index template cluster",
                    getClass().getSimpleName(), logicId);
            return false;
        }
        final IndexTemplatePO templatePO = new IndexTemplatePO();
        templatePO.setId(logicId);
        if (!esClusterService.isConnectionStatus(masterCluster)) {
            LOGGER.warn(
                    "class={}||method=updateTemplateHealthByLogicId||logicId={}||error=don't find index template cluster",
                    getClass().getSimpleName(), logicId);
            /**
             * {@link TemplateHealthEnum}
             */
            templatePO.setHealth(TemplateHealthEnum.UNKNOWN.getCode());
            indexTemplateService.update(templatePO);
            return true;
        
        }
        try {
            String cluster = indexTemplateWithPhyTemplates.getMasterPhyTemplate().getCluster();
            String expression = indexTemplateWithPhyTemplates.getMasterPhyTemplate().getExpression();
            if (esTemplateService.hasMatchHealthIndexByExpressionTemplateHealthEnum(cluster, expression,
                    TemplateHealthEnum.RED)) {
                templatePO.setHealth(TemplateHealthEnum.RED.getCode());
                indexTemplateService.update(templatePO);
                return true;
            }
             if (esTemplateService.hasMatchHealthIndexByExpressionTemplateHealthEnum(cluster, expression,
                    TemplateHealthEnum.YELLOW)) {
                templatePO.setHealth(TemplateHealthEnum.YELLOW.getCode());
                indexTemplateService.update(templatePO);
                return true;
            }
            
            if (esTemplateService.hasMatchHealthIndexByExpressionTemplateHealthEnum(cluster, expression,
                    TemplateHealthEnum.GREEN)) {
                templatePO.setHealth(TemplateHealthEnum.GREEN.getCode());
                indexTemplateService.update(templatePO);
                return true;
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("class=TemplateLogicManagerImpl||method=updateTemplateHealthByLogicId||logicId={}", logicId, e);
            return false;
        }
    }
    
    /**
     * @param consoleTemplateRateLimitDTO
     * @param operator
     * @param projectId
     * @return
     */
    @Override
    public Result<Void> updateTemplateWriteRateLimit(ConsoleTemplateRateLimitDTO consoleTemplateRateLimitDTO,
                                                     String operator, Integer projectId) {
        final Integer projectIdByTemplateLogicId = indexTemplateService
            .getProjectIdByTemplateLogicId(consoleTemplateRateLimitDTO.getLogicId());
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectIdByTemplateLogicId, projectId);
        if (result.failed()) {
            return result;
        }
        // 判断调整比例是否在区间内
        int percent = (int) Math.ceil(
            100.0 * (consoleTemplateRateLimitDTO.getAdjustRateLimit() - consoleTemplateRateLimitDTO.getCurRateLimit())
                                      / consoleTemplateRateLimitDTO.getCurRateLimit());
        if (percent < MIN_PERCENT || percent > MAX_PERCENT) {
            return Result.buildFail("限流调整值变化太大，一次调整比例在100倍以内");
        }
        try {
            Result<Void> updateTemplateWriteRateLimit = indexTemplateService
                .updateTemplateWriteRateLimit(consoleTemplateRateLimitDTO);
            if (updateTemplateWriteRateLimit.success()) {
                operateRecordService.saveOperateRecordWithManualTrigger(
                        String.format("数据库写入限流值修改 %s->%s", consoleTemplateRateLimitDTO.getCurRateLimit(),
                                consoleTemplateRateLimitDTO.getAdjustRateLimit()), operator, projectId,
                        consoleTemplateRateLimitDTO.getLogicId(),
                        OperateTypeEnum.QUERY_TEMPLATE_DSL_CURRENT_LIMIT_ADJUSTMENT);
            }
            return updateTemplateWriteRateLimit;
        } catch (ESOperateException e) {
            LOGGER.info("限流调整失败", e);
            return Result.buildFail("限流调整失败！");
        }
    }
   
    /**
     * 用索引模板的写操作。
     *
     * @param templateId 要操作的模板的id
     * @param status 0：否，1：是
     * @param operator 触发操作的操作员
     * @param projectId 项目编号
     * @return Result<Void>
     */
    @Override
    public Result<Void> blockWrite(Integer templateId, Boolean status, String operator, Integer projectId) {
        IndexTemplatePO logicTemplate = indexTemplateService.getLogicTemplatePOById(templateId);
    
        if (Objects.isNull(logicTemplate)) {
            return Result.buildFail("逻辑模板不存在");
        }
        Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(IndexTemplatePO::getProjectId,
                logicTemplate, projectId);
        if (checkProjectCorrectly.failed()) {
            return checkProjectCorrectly;
        }
    
        Result<Void> result = indexTemplateService.updateBlockWriteState(templateId, status);
        if (result.success()) {
            // 是否禁写，0：否，1：是
            operateRecordService.saveOperateRecordWithManualTrigger(
                    Objects.equals(status, Boolean.TRUE) ? "禁写" : "开启写", operator, projectId, templateId,
                    OperateTypeEnum.TEMPLATE_MANAGEMENT_CREATE);
        }
    
        return result;
    }
    
    
    /**
     * 用于索引模板的读取。
     *
     * @param templateId 要操作的模板的id
     * @param status 0：否，1：是
     * @param operator 触发操作的用户
     * @param projectId 项目编号
     * @return Result<Void>
     */
    @Override
    public Result<Void> blockRead(Integer templateId, Boolean status, String operator, Integer projectId) {
        IndexTemplatePO logicTemplate = indexTemplateService.getLogicTemplatePOById(templateId);
    
        if (Objects.isNull(logicTemplate)) {
            return Result.buildFail("逻辑模板不存在");
        }
        Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(IndexTemplatePO::getProjectId,
                logicTemplate, projectId);
        if (checkProjectCorrectly.failed()) {
            return checkProjectCorrectly;
        }
        Result<Void> result = indexTemplateService.updateBlockReadState(templateId, status);
        if (result.success()) {
            // 是否禁写，0：否，1：是
            operateRecordService.saveOperateRecordWithManualTrigger(
                    Objects.equals(status, Boolean.TRUE) ? "禁读" : "开启读", operator, projectId, templateId,
                    OperateTypeEnum.TEMPLATE_MANAGEMENT_CREATE);
        }
    
        return result;
    }

    /**
     * 更新模版settings和非分区模版索引的settings(可以用来实现部分模版服务，如异步translog、恢复优先级)
     * @param param 模版增量settings
     * @param operator
     * @param projectId
     * @return
     */
    @Override
    public Result<Void> updateTemplateAndIndexSettings(TemplateIncrementalSettingsDTO param, String operator, Integer projectId) throws AdminOperateException {

        Result<Void> checkResult = checkParam(projectId, operator, param);
        if(checkResult.failed()){
            return Result.buildFail(checkResult.getMessage());
        }

        // 用于错误消息拼接
        boolean updateFail = false;
        StringBuilder updateFailTemplates = new StringBuilder();
        // 构造模版的增量settings
        Map<String, String> incrementalSettings = Maps.newHashMap();
        incrementalSettings.putAll(param.getIncrementalSettings());

        // indicesIncrementalSettingList 存储需要更新settings的索引
        List<IndicesIncrementalSettingDTO> indicesIncrementalSettingList = Lists.newArrayList();

        for (Integer logicId : param.getTemplateIdList()) {
            // 增量方式修改每个模版的settings
            Result<Void> result = templateLogicSettingsManager.updateSettingsByMerge(logicId, incrementalSettings, operator, projectId);
            if(result.failed()){
                updateFail = true;
                updateFailTemplates.append(logicId).append(",");
                LOGGER.error("class=TemplateLogicManagerImpl||method=updateTemplateAndIndexSettings,templateId={}, errMsg={}",
                        logicId, "update settings failed");
            } else {
                // 更新状态到DB中，以便page查询数据时获取到服务的状态：对于translog功能，更新srvCode字段；对于恢复优先级功能，要更新字段priority_level
                Result<Void> updateDBResult = updateStatusToDB(logicId, param);
                if(updateDBResult.failed()){
                    LOGGER.error("class=TemplateLogicManagerImpl||method=updateTemplateAndIndexSettings,templateId={}, errMsg={}",
                            logicId, "update db failed");
                }
            }

            // 对于非分区模版，还要修改其对应的那一个索引的settings
            IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(logicId);
            if(!templateLogicWithPhysical.getExpression().endsWith("*")){
                List<IndexTemplatePhy> physicalMasters = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
                for (IndexTemplatePhy physicalMaster : physicalMasters) {
                    CatIndexResult catIndexResult = indicesManager.listIndexCatInfoByTemplatePhyId(physicalMaster.getId())
                            .stream().findFirst().orElse(null);
                    if(AriusObjUtils.isNull(catIndexResult)){
                        continue;
                    }

                    IndicesIncrementalSettingDTO indicesIncrementalSettingDTO = new IndicesIncrementalSettingDTO();
                    indicesIncrementalSettingDTO.setCluster(physicalMaster.getCluster());
                    indicesIncrementalSettingDTO.setIndex(catIndexResult.getIndex());
                    indicesIncrementalSettingDTO.setIncrementalSettings(param.getIncrementalSettings());

                    indicesIncrementalSettingList.add(indicesIncrementalSettingDTO);
                }
            }
        }

        // 批量更新索引settings
        if(!indicesIncrementalSettingList.isEmpty()){
            Result<Void> result = indicesManager.updateIndexSettingsByMerge(indicesIncrementalSettingList, projectId, operator);
            if(result.failed()){
                return Result.buildFail(result.getMessage());
            }
        }

        if(updateFail){
            return Result.buildFail(updateFailTemplates.deleteCharAt(updateFailTemplates.length()-1) + "模版更新settings失败");
        }

        return Result.buildSucc();
    }

    /**************************************** private method ***************************************************/

    private Result<Void> checkParam(Integer projectId, String operator, TemplateIncrementalSettingsDTO param){
        final Result<Void> projectCheck = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (projectCheck.failed()) {
            return Result.buildFail(projectCheck.getMessage());
        }
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }
        if(param.getIncrementalSettings() == null || param.getIncrementalSettings().isEmpty()){
            return Result.buildParamIllegal("参数为空");
        }

        for(Map.Entry<String, String> entry : param.getIncrementalSettings().entrySet()){
            String key = entry.getKey();
            String value = param.getIncrementalSettings().get(key);

            if (TemplateSettingEnum.stream().noneMatch(settingEnum -> settingEnum.getSetting().equals(key))) {
                return Result.buildParamIllegal("模版settings的key取值有误");
            }

            Result<Void> result;
            switch (TemplateSettingEnum.getBySetting(key)){
                case INDEX_PRIORITY:
                    result = checkPriorityValid(value, INDEX_PRIORITY);
                    break;
                case INDEX_TRANSLOG_DURABILITY:
                    result = checkTranslogValid(value, INDEX_TRANSLOG_DURABILITY);
                    break;
                default:
                    result = Result.buildFail("模版settings的key取值有误");
            }
            if (result.failed()){
                return Result.buildFrom(result);
            }
        }

        return Result.buildSucc();
    }

    private Result<Void> checkPriorityValid(String value, TemplateSettingEnum templateSettingEnum) {
        if (!StringUtils.isNumeric(value)){
            return Result.buildParamIllegal("setting [index.priority] must be numeric");
        }
        Integer priorityLevel = Integer.valueOf(value);
        if (priorityLevel < 0){
            return Result.buildParamIllegal("setting [index.priority] must be >= 0");
        }

        if(templateSettingEnum.getValueList().stream().noneMatch(needValue -> needValue.equals(value))) {
            return Result.buildParamIllegal("setting [index.priority] must be " + templateSettingEnum.getValues());
        }
        return Result.buildSucc();
    }

    private Result<Void> checkTranslogValid(String value, TemplateSettingEnum templateSettingEnum) {
        if (templateSettingEnum.getValueList().stream().noneMatch(needValue -> needValue.equals(value))) {
            return Result.buildParamIllegal("setting [index.translog.durability] must be " + templateSettingEnum.getValues());
        }
        return Result.buildSucc();
    }

    /**
     * 更新状态到数据库中
     * 对于异步translog功能，更新srvCode字段；对于恢复优先级功能，要更新字段priority_level
     * @param logicId
     * @param param
     * @return
     */
    private Result<Void> updateStatusToDB(Integer logicId, TemplateIncrementalSettingsDTO param){
        IndexTemplate indexTemplate = indexTemplateService.getLogicTemplateById(logicId);
        for (Map.Entry<String, String> entry : param.getIncrementalSettings().entrySet()) {
            String key = entry.getKey();
            String value = param.getIncrementalSettings().get(key);

            if(ESSettingConstant.INDEX_TRANSLOG_DURABILITY.equals(key)){
                String updateSrvCode = TemplateServiceEnum.TEMPLATE_TRANSLOG_ASYNC.getCode().toString();
                if(ESSettingConstant.ASYNC.equals(value)){
                    buildTemplateOpenSrv(indexTemplate, updateSrvCode, Boolean.TRUE);
                }else {
                    buildTemplateOpenSrv(indexTemplate, updateSrvCode, Boolean.FALSE);
                }
            }else if (ESSettingConstant.INDEX_PRIORITY.equals(key)){
                indexTemplate.setPriorityLevel(Integer.valueOf(value));
            }
        }
        IndexTemplatePO indexTemplatePO = ConvertUtil.obj2Obj(indexTemplate, IndexTemplatePO.class);
        boolean update = indexTemplateService.update(indexTemplatePO);
        if(!update){
            return Result.buildFail(logicId + "模版更新db失败");
        }

        return Result.buildSucc();
    }

    private void buildTemplateOpenSrv(IndexTemplate indexTemplate, String updateSrvCode, Boolean status) {
        String srvCodeStr = indexTemplate.getOpenSrv();
        List<String> srvCodeList = ListUtils.string2StrList(srvCodeStr);
        if(Boolean.TRUE.equals(status)) {
            if (srvCodeList.isEmpty()) {
                indexTemplate.setOpenSrv(updateSrvCode);
            }else if (!srvCodeList.contains(updateSrvCode)) {
                indexTemplate.setOpenSrv(srvCodeStr + "," + updateSrvCode);
            }
        }else if (srvCodeList.contains(updateSrvCode)) {
            srvCodeList.remove(updateSrvCode);
            indexTemplate.setOpenSrv(ListUtils.strList2String(srvCodeList));
        }
    }

    /**
    * 获取逻辑模板索引列表
    *
    * @param logicId 逻辑ID
    * @return
    */
    private List<String> getLogicTemplateIndices(Integer logicId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (null != templateLogicWithPhysical && null != templateLogicWithPhysical.getMasterPhyTemplate()) {
            return indexTemplatePhyService
                .getMatchNoVersionIndexNames(templateLogicWithPhysical.getMasterPhyTemplate().getId());
        }

        return new ArrayList<>();
    }

    /**
     * 校验逻辑模板Master ROLE物理模板是否存在
     * @param templateLogic 逻辑模板
     * @param projectIds 项目ids
     * @return
     */
    private Result<Void> checkLogicTemplateMeta(IndexTemplate templateLogic, List<Integer> projectIds) {
        List<String> errMsg = Lists.newArrayList();

        if (!projectIds.contains(templateLogic.getProjectId())) {
            errMsg.add("所属PROJECT ID不存在：" + templateLogic.getProjectId());
        }

        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getTemplateByLogicId(templateLogic.getId());

        if (CollectionUtils.isNotEmpty(templatePhysicals)) {
            List<IndexTemplatePhy> templatePhysicalsMaster = templatePhysicals.stream()
                .filter(templatePhysical -> templatePhysical.getRole().equals(TemplateDeployRoleEnum.MASTER.getCode()))
                .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(templatePhysicalsMaster)) {
                errMsg.add("没有部署master：" + templateLogic.getName() + "(" + templateLogic.getId() + ")");
            }
        }

        if (CollectionUtils.isEmpty(errMsg)) {
            return Result.buildSucc();
        }

        return Result.build(ResultType.ADMIN_META_ERROR.getCode(), String.join(",", errMsg));
    }

    /**
     * 构建逻辑模板视图
     */
    private ConsoleTemplateVO buildTemplateVO(IndexTemplatePhyWithLogic param) {
        ConsoleTemplateVO consoleTemplateVO = new ConsoleTemplateVO();
        if (param != null) {
            consoleTemplateVO = ConvertUtil.obj2Obj(param.getLogicTemplate(), ConsoleTemplateVO.class);
            consoleTemplateVO.setClusterPhies(Collections.singletonList(param.getCluster()));
        }
        return consoleTemplateVO;
    }



    /**
     * 获取逻辑模板详情
     *
     * @param indexTemplateLogicWithCluster 逻辑集群
     * @param projectTemplateAuths                 project模板权限
     * @param logicTemplateValues              逻辑模板健康分
     */
    private IndexTemplateLogicAggregate fetchTemplateAggregate(IndexTemplateWithCluster indexTemplateLogicWithCluster,
                                                               Map<Integer, ProjectTemplateAuth> projectTemplateAuths,
                                                               Map<Integer, IndexTemplateValue> logicTemplateValues,
                                                               List<Integer> hasDCDRLogicIds) {

        IndexTemplateLogicAggregate indexTemplateLogicAggregate = new IndexTemplateLogicAggregate();

        indexTemplateLogicAggregate.setIndexTemplateLogicWithCluster(indexTemplateLogicWithCluster);
        indexTemplateLogicAggregate
            .setProjectTemplateAuth(projectTemplateAuths.get(indexTemplateLogicWithCluster.getId()));
        indexTemplateLogicAggregate
            .setIndexTemplateValue(logicTemplateValues.get(indexTemplateLogicWithCluster.getId()));
        indexTemplateLogicAggregate.setHasDCDR(hasDCDRLogicIds.contains(indexTemplateLogicWithCluster.getId()));

        return indexTemplateLogicAggregate;
    }

    /**
     * 校验物理集群的合法性
     * @param clusterPhyName 物理集群
     * @return 校验结果
     */
    private Result<Boolean> checkExistClusterNamePhy(String clusterPhyName) {
        
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail(String.format("模板归属集群[%s]不存在", clusterPhyName));
        }
        return Result.buildSucc();
    }

    private IndexTemplateConfig getDefaultTemplateConfig(Integer logicId) {
        IndexTemplateConfig indexTemplateConfig = new IndexTemplateConfig();
        indexTemplateConfig.setLogicId(logicId);
        indexTemplateConfig.setAdjustTpsFactor(1.0);
        indexTemplateConfig.setAdjustShardFactor(1.0);
        indexTemplateConfig.setDynamicLimitEnable(AdminConstant.YES);
        indexTemplateConfig.setMappingImproveEnable(AdminConstant.NO);
        indexTemplateConfig.setIsSourceSeparated(AdminConstant.NO);
        indexTemplateConfig.setDisableSourceFlags(false);
        indexTemplateConfig.setPreCreateFlags(true);
        indexTemplateConfig.setShardNum(1);
        indexTemplateConfig.setDisableIndexRollover(false);
        return indexTemplateConfig;
    }

    /**
     * 记录模板配置
     * @param param 模板配置参数
     */
    private Result<Void> insertTemplateConfig(IndexTemplateDTO param) {
        IndexTemplateConfig defaultTemplateConfig = getDefaultTemplateConfig(param.getId());
        defaultTemplateConfig.setDisableSourceFlags(false);
        if (param.getDisableIndexRollover() != null) {
            defaultTemplateConfig.setDisableIndexRollover(param.getDisableIndexRollover());
        }

        if (param.getPreCreateFlags() != null) {
            defaultTemplateConfig.setPreCreateFlags(param.getPreCreateFlags());
        }

        if (param.getShardNum() != null) {
            defaultTemplateConfig.setShardNum(param.getShardNum());
        }
        return indexTemplateService.insertTemplateConfig(defaultTemplateConfig);
    }

    private IndexTemplateDTO buildTemplateDTO(IndexTemplateWithCreateInfoDTO param, Integer projectId) {
        IndexTemplateDTO indexTemplateDTO = ConvertUtil.obj2Obj(param, IndexTemplateDTO.class);

        indexTemplateDTO.setProjectId(projectId);

        // 新建模版默认disableIndexRollover字段为true
        indexTemplateDTO.setDisableIndexRollover(true);

        buildExtraField(indexTemplateDTO);
        buildCyclicalRoll(indexTemplateDTO, param);
        buildShardNum(indexTemplateDTO, param);
        buildPhysicalInfo(indexTemplateDTO, param);
        //如果是分区模版
        final boolean isExpression =
                Optional.ofNullable(indexTemplateDTO.getExpression()).map(expression->expression.endsWith(
                "*")).orElse(false);
        List<Integer> openSrvList=Lists.newArrayList();
        if (Boolean.TRUE.equals(isExpression)) {
            openSrvList.add(TemplateServiceEnum.TEMPLATE_PRE_CREATE.getCode());
            openSrvList.add(TemplateServiceEnum.TEMPLATE_DEL_EXPIRE.getCode());
        }
        final ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(
                indexTemplateDTO.getResourceId());
        final TupleTwo</*dcdrExist*/Boolean,/*pipelineExist*/ Boolean> existDCDRAndPipelineModule = esClusterNodeService.existDCDRAndPipelineModule(
                clusterRegion.getPhyClusterName());
        if (Boolean.TRUE.equals(existDCDRAndPipelineModule.v2)) {
            openSrvList.add(TemplateServiceEnum.TEMPLATE_PIPELINE.getCode());
        }
        // 如果存在 dcdr 插件，则开启 dcdr 服务
        if (Boolean.TRUE.equals(existDCDRAndPipelineModule.v1)) {
            openSrvList.add(TemplateServiceEnum.TEMPLATE_DCDR.getCode());
        }
        if (CollectionUtils.isNotEmpty(openSrvList)) {
            //如果集群支持pipeline
            indexTemplateDTO.setOpenSrv(ConvertUtil.list2String(openSrvList, ","));
        }
      

        return indexTemplateDTO;
    }

    private void buildExtraField(IndexTemplateDTO indexTemplateDTO) {
        indexTemplateDTO.setIngestPipeline(indexTemplateDTO.getName());
        indexTemplateDTO.setDiskSize(indexTemplateDTO.getDiskSize());
        indexTemplateDTO.setQuota(indexTemplateDTO.getDiskSize());

        if (null == indexTemplateDTO.getDesc()) {
            indexTemplateDTO.setDesc("");
        }
    }

    private void buildCyclicalRoll(IndexTemplateDTO indexTemplateDTO, IndexTemplateWithCreateInfoDTO param) {
        if (!param.getCyclicalRoll()) {
            indexTemplateDTO.setExpression(param.getName());
            indexTemplateDTO.setExpireTime(-1);
        } else {
            indexTemplateDTO.setExpression(param.getName() + "*");
            // 数据不会过期，必须按月滚动
            if (param.getExpireTime() < 0) {
                indexTemplateDTO.setDateFormat(AdminConstant.YY_MM_DATE_FORMAT);
            } else {
                //每天的数据增量大于200G或者保存时长小于30天 按天存储
                double incrementPerDay = param.getDiskSize() / param.getExpireTime();
                if (incrementPerDay >= 200.0 || param.getExpireTime() <= 30) {
                    if (StringUtils.isNotBlank(param.getDateField())
                        && !AdminConstant.MM_DD_DATE_FORMAT.equals(param.getDateField())) {
                        indexTemplateDTO.setDateFormat(AdminConstant.YY_MM_DD_DATE_FORMAT);
                    }
                } else {
                    indexTemplateDTO.setDateFormat(AdminConstant.YY_MM_DATE_FORMAT);
                }
            }
        }

        if (null == indexTemplateDTO.getDateFormat()) {
            indexTemplateDTO.setDateFormat("");
        }

        if (null == indexTemplateDTO.getDateField()) {
            indexTemplateDTO.setDateField("");
        }

        if (null == indexTemplateDTO.getDateFieldFormat()) {
            indexTemplateDTO.setDateFieldFormat("");
        }
    }

    private void buildPhysicalInfo(IndexTemplateDTO indexTemplateDTO, IndexTemplateWithCreateInfoDTO param) {
        IndexTemplatePhyDTO indexTemplatePhyDTO = ConvertUtil.obj2Obj(indexTemplateDTO, IndexTemplatePhyDTO.class);

        indexTemplatePhyDTO.setLogicId(NOT_CHECK);
        indexTemplatePhyDTO.setGroupId(UUID.randomUUID().toString());
        indexTemplatePhyDTO.setRole(TemplateDeployRoleEnum.MASTER.getCode());
        indexTemplatePhyDTO.setShard(indexTemplateDTO.getShardNum());
        indexTemplatePhyDTO.setDefaultWriterFlags(true);

        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(param.getResourceId());
        indexTemplatePhyDTO.setCluster(clusterRegion.getPhyClusterName());
        indexTemplatePhyDTO.setRegionId(clusterRegion.getId().intValue());

        Integer clusterSettingHotDay = templateColdManager.fetchClusterDefaultHotDay(clusterRegion.getPhyClusterName());
        if (clusterSettingHotDay > 0) {
            indexTemplateDTO.setHotTime(clusterSettingHotDay);
        } else {
            indexTemplateDTO.setHotTime(-1);
        }

        if (StringUtils.isNotBlank(param.getSetting())) {
            indexTemplatePhyDTO.setSettings(param.getSetting());
        } else {
            indexTemplatePhyDTO.setSettings("{}");
        }

        if (StringUtils.isNotBlank(param.getMapping())) {
            AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
           
            ariusTypeProperty.setTypeName(DEFAULT_INDEX_MAPPING_TYPE);
            final JSONObject mappings = JSON.parseObject(param.getMapping());
            if (mappings.containsKey(DYNAMIC_TEMPLATES)) {
                ariusTypeProperty.setDynamicTemplates(mappings.getJSONArray(DYNAMIC_TEMPLATES));
            }
            if (mappings.containsKey(PROPERTIES)){
                ariusTypeProperty.setProperties(mappings.getJSONObject(PROPERTIES));
            }
            
            
            indexTemplatePhyDTO.setMappings(
                ariusTypeProperty.toMappingJSON().getJSONObject(DEFAULT_INDEX_MAPPING_TYPE).toJSONString());
        } else {
            indexTemplatePhyDTO.setMappings("{}");
        }

        indexTemplateDTO.setPhysicalInfos(Lists.newArrayList(indexTemplatePhyDTO));
    }

    private void buildShardNum(IndexTemplateDTO indexTemplateDTO, IndexTemplateWithCreateInfoDTO param) {
        if (param.getCyclicalRoll()) {
            int expireTime = param.getExpireTime();
            if (expireTime < 0) {
                // 如果数据永不过期，平台会按着180天来计算每日数据增量，最终用于生成模板shard
                expireTime = 180;
            }

            if (TemplateUtils.isSaveByDay(indexTemplateDTO.getDateFormat())) {
                // 按天滚动
                indexTemplateDTO.setShardNum(genShardNumBySize(param.getDiskSize() / expireTime));
            } else {
                // 按月滚动
                indexTemplateDTO.setShardNum(genShardNumBySize((param.getDiskSize() / expireTime) * 30));
            }
        } else {
            indexTemplateDTO.setShardNum(genShardNumBySize(param.getDiskSize()));
        }
    }

    private Integer genShardNumBySize(Double size) {
        double shardNumCeil = Math.ceil(size / G_PER_SHARD);
        return (int) shardNumCeil;
    }
}