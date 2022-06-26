package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.DEFAULT_INDEX_MAPPING_TYPE;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.G_PER_SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.TEMPLATE_LOGIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.TEMPLATE_NAME_CHAR_SET;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.TEMPLATE_NAME_SIZE_MAX;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.TEMPLATE_NAME_SIZE_MIN;
import static com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum.OWN;
import static com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum.isTemplateAuthExitByCode;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.FAIL;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_MAPPING;
import static com.didichuxing.datachannel.arius.admin.core.service.template.physic.impl.IndexTemplatePhyServiceImpl.NOT_CHECK;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.page.TemplateLogicPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.TemplateColdManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplateValue;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateLabel;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateWithCreateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template.TemplateOperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicAggregate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AmsRemoteException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateSattisService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class TemplateLogicManagerImpl implements TemplateLogicManager {

    private static final ILog           LOGGER      = LogFactory.getLog(TemplateLogicManager.class);

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Autowired
    private TemplateSattisService       templateSattisService;

    @Autowired
    private TemplateLabelService        templateLabelService;

    @Autowired
    private TemplateColdManager         templateColdManager;

    @Autowired
    private IndexTemplateService        indexTemplateService;

    @Autowired
    private IndexTemplatePhyService     indexTemplatePhyService;

    @Autowired
    private ClusterPhyService           clusterPhyService;

    @Autowired
    private ClusterRegionService        clusterRegionService;

    @Autowired
    private OperateRecordService        operateRecordService;

    @Autowired
    private ProjectService projectService;
 

    @Autowired
    private ESIndexService              esIndexService;

    @Autowired
    private ESTemplateService           esTemplateService;

    



    @Autowired
    private TemplatePhyManager          templatePhyManager;

    @Autowired
    private HandleFactory               handleFactory;

    @Autowired
    private TemplateDCDRManager         templateDcdrManager;

    @Autowired
    private PreCreateManager            preCreateManager;

    @Autowired
    private ClusterLogicService         clusterLogicService;

    private final static Integer RETRY_TIMES = 3;





    /**
     * 获取最近访问该模板的project
     *
     * @param logicId logicId
     * @return result
     */
    @Override
    public List<ProjectBriefVO> getLogicTemplateProjectAccess(Integer logicId) {
        Result<Map<Integer, Long>> result = templateSattisService.getTemplateAccessProjectIds(logicId, 7);
        if (result.failed()) {
            throw new AmsRemoteException("获取访问模板的project列表失败");
        }

        if (null == result.getData() || 0 == result.getData().size()) {
            return Lists.newArrayList();
        }
       

        return result.getData().keySet().stream().map(projectService::getProjectBriefByProjectId)
                .filter(Objects::nonNull).collect( Collectors.toList());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> create(IndexTemplateWithCreateInfoDTO param, String operator, Integer projectId) throws AdminOperateException {
        IndexTemplateDTO indexTemplateDTO = buildTemplateDTO(param, projectId);
        Result<Void> validLogicTemplateResult = indexTemplateService.validateTemplate(indexTemplateDTO, ADD);
        if (validLogicTemplateResult.failed()) { return validLogicTemplateResult;}

        Result<Void> validPhyTemplateResult = indexTemplatePhyService.validateTemplates(indexTemplateDTO.getPhysicalInfos(), ADD);
        if (validPhyTemplateResult.failed()) { return validPhyTemplateResult;}

        Result<Void> save2DBResult = indexTemplateService.addTemplateWithoutCheck(indexTemplateDTO);
        if (save2DBResult.failed()) {
            throw new AdminOperateException(String.format("创建模板失败:%s", save2DBResult.getMessage()));
        }

        Result<Void> save2PhyTemplateResult = templatePhyManager.addTemplatesWithoutCheck(indexTemplateDTO.getId(), indexTemplateDTO.getPhysicalInfos());
        if (save2PhyTemplateResult.failed()) {
            throw new AdminOperateException(String.format("创建模板失败:%s", save2PhyTemplateResult.getMessage()));
        }

        Result<Void> saveTemplateConfigResult = insertTemplateConfig(indexTemplateDTO);
        if (saveTemplateConfigResult.failed()) {
            throw new AdminOperateException(String.format("创建模板失败:%s", saveTemplateConfigResult.getMessage()));
        }

        operateRecordService.save(TEMPLATE, ADD, param.getId(),
            JSON.toJSONString(new TemplateOperateRecord(TemplateOperateRecordEnum.NEW.getCode(), "新增模板")), operator);

        return Result.buildSucc();
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
        List<IndexTemplateWithCluster> logicTemplates = indexTemplateService
                .listAllLogicTemplateWithClusters();

        if (CollectionUtils.isNotEmpty(logicTemplates)) {
            indexTemplateLogicAggregates = fetchLogicTemplatesAggregates(logicTemplates, projectId);
        }

        return indexTemplateLogicAggregates;
    }

    /**
     * 获取逻辑集群所有逻辑模板聚合
     *
     * @param logicClusterId 逻辑集群ID
     * @param projectId 操作的project Id
     * @return
     */
    @Override
    public List<IndexTemplateLogicAggregate> getLogicClusterTemplatesAggregate(Long logicClusterId, Integer projectId) {

        if (logicClusterId == null) {
            return new ArrayList<>();
        }

        List<IndexTemplateWithCluster> logicTemplates = indexTemplateService
                .listLogicTemplateWithClustersByClusterId(logicClusterId);

        if (CollectionUtils.isEmpty(logicTemplates)) {
            return new ArrayList<>();
        }

        return fetchLogicTemplatesAggregates(logicTemplates, projectId);
    }

    /**
     * 拼接集群名称
     * @param logicClusters 逻辑集群详情列表
     * @return
     */
    @Override
    public String jointCluster(List<ClusterLogic> logicClusters) {
        if (CollectionUtils.isNotEmpty(logicClusters)) {
            return String.join(",", logicClusters.stream().
                    map(ClusterLogic::getName).collect(
                    Collectors.toList()));
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
                    String projectName =  Optional.ofNullable(projectService.getProjectBriefByProjectId(projectId))
                                .map(ProjectBriefVO::getProjectName)
                                .orElse(null);
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
            ConsoleTemplateVO templateLogic = ConvertUtil.obj2Obj(
                    aggregate.getIndexTemplateLogicWithCluster(),
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
                List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(templateLogic.getId());
                if (CollectionUtils.isNotEmpty(templatePhyList)) {
                    templateLogic.setClusterPhies(
                            templatePhyList.stream().map(IndexTemplatePhy::getCluster).collect(Collectors.toList()));
                }
            } catch (Exception e) {
                LOGGER.warn("class=TemplateLogicManager||method=fetchConsoleTemplate||aggregate={}",
                        aggregate, e);
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
                }else {
                    return indexTemplateService.listProjectLogicTemplatesByProjectId(projectId);
                }

            case RW:
                List<ProjectTemplateAuth> projectActiveTemplateRWAuths = projectLogicTemplateAuthService
                    .getProjectActiveTemplateRWAuths(projectId);
                return projectActiveTemplateRWAuths
                        .stream()
                        .map(r -> indexTemplateService.getLogicTemplateById(r.getTemplateId()))
                        .collect(Collectors.toList());

            case R:
                List<ProjectTemplateAuth> projectActiveTemplateRAuths = projectLogicTemplateAuthService
                    .getProjectActiveTemplateRAuths(projectId);
                return projectActiveTemplateRAuths
                        .stream()
                        .map(r -> indexTemplateService.getLogicTemplateById(r.getTemplateId()))
                        .collect(Collectors.toList());

            case NO_PERMISSION:
                List<IndexTemplate> allLogicTemplates = indexTemplateService.listAllLogicTemplates();
                List<Integer> projectRAndRwAuthTemplateIdList = projectLogicTemplateAuthService
                        .getProjectTemplateRWAndRAuthsWithoutCodecResponsible(projectId)
                        .stream()
                        .map(ProjectTemplateAuth::getTemplateId)
                        .collect(Collectors.toList());

                List<IndexTemplate> notAuthIndexTemplateList = allLogicTemplates
                        .stream()
                        .filter(r -> !projectId.equals(r.getProjectId()) && !projectRAndRwAuthTemplateIdList.contains(r.getId()))
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
    public Result<Void> editTemplate(IndexTemplateDTO param, String operator)
            throws AdminOperateException {
        return indexTemplateService.editTemplate(param, operator);
    }

    @Override
    public Result<Void> newEditTemplate(IndexTemplateDTO param, String operator) {
        try {
            return indexTemplateService.editTemplateInfoTODB(param);
        } catch (AdminOperateException e) {
            LOGGER.error("class=TemplateLogicManagerImpl||method=newEditTemplate||msg=fail to editTemplate");
        }
        return Result.buildFail("编辑模板失败");
    }

    @Override
    public Result<Void> delTemplate(Integer logicTemplateId, String operator)
            throws AdminOperateException {
        return indexTemplateService.delTemplate(logicTemplateId, operator);
    }

    @Override
    public PaginationResult<ConsoleTemplateVO> pageGetConsoleTemplateVOS(TemplateConditionDTO condition, Integer projectId) {
        BaseHandle baseHandle     = handleFactory.getByHandlerNamePer(TEMPLATE_LOGIC.getPageSearchType());
        if (baseHandle instanceof TemplateLogicPageSearchHandle) {
            TemplateLogicPageSearchHandle handle = (TemplateLogicPageSearchHandle) baseHandle;
            return handle.doPage(condition, projectId);
        }

        LOGGER.warn("class=TemplateLogicManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the TemplateLogicPageSearchHandle");

        return PaginationResult.buildFail("获取模板分页信息失败");
    }

    @Override
    public Result<Void> checkTemplateValidForCreate(String templateName) {
        if (AriusObjUtils.isNull(templateName)) {
            return Result.buildParamIllegal("名字为空");
        }

        if (templateName.length() < TEMPLATE_NAME_SIZE_MIN || templateName.length() > TEMPLATE_NAME_SIZE_MAX) {
            return Result.buildParamIllegal(String.format("名称长度非法, %s-%s",TEMPLATE_NAME_SIZE_MIN,TEMPLATE_NAME_SIZE_MAX));
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

        List<String> clusterPhyNameList = templatePhyList.stream().map(IndexTemplatePhy::getCluster).distinct().collect(Collectors.toList());
        for (String clusterPhyName : clusterPhyNameList) {
            ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
            if (null == clusterPhy) {
                return Result.buildFail(String.format("模板归属集群[%s]不存在", clusterPhyName));
            }
            
            List<String> templateSrvList = ListUtils.string2StrList(clusterPhy.getTemplateSrvs());
            if (!templateSrvList.contains(TEMPLATE_MAPPING.getCode().toString())){
                return Result.buildFail("该模板归属集群未开启mapping修改服务");
            }
        }

        return Result.buildSucc(true);
    }

    @Override
    public Result<Void> switchRolloverStatus(Integer templateLogicId, Integer status, String operator) {
        if(templateLogicId == null || status == null) {
            return Result.buildSucc();
        }
        Boolean newDisable = status == 0;
        IndexTemplateConfig templateConfig = indexTemplateService.getTemplateConfig(templateLogicId);
        if(templateConfig == null) {
            return Result.buildFail("模版不存在");
        }
        Boolean oldDisable = templateConfig.getDisableIndexRollover();
        if(!newDisable.equals(oldDisable)) {
            // 如果状态不同则更新状态
            IndexTemplateConfigDTO indexTemplateConfigDTO = ConvertUtil.obj2Obj(templateConfig, IndexTemplateConfigDTO.class);
            indexTemplateConfigDTO.setDisableIndexRollover(newDisable);
            Result<Void> updateStatusResult = indexTemplateService.updateTemplateConfig(indexTemplateConfigDTO, operator);
            if (updateStatusResult.success()) {
                // rollover状态修改记录(兼容开启或者关闭)
                operateRecordService.save(TEMPLATE, OperationEnum.EDIT, templateLogicId, JSON.toJSONString(
                        new TemplateOperateRecord(TemplateOperateRecordEnum.ROLLOVER.getCode(), "rollover状态修改为:" + (newDisable ? "关闭" : "开启"))), operator);
            }
        }
        return Result.buildSucc();
    }

    @Override
    public List<Integer> getHaveDCDRLogicIds() {
        Result<List<TemplateLabel>> result = templateLabelService.listHaveDcdrTemplates();
        if (result.failed() || CollectionUtils.isEmpty(result.getData())) {
            return Lists.newArrayList();
        }

        return result.getData().stream().map(TemplateLabel::getIndexTemplateId).collect(Collectors.toList());
    }

    @Override
    public Result<Boolean> checkTemplateEditService(Integer templateId, Integer templateSrvId) {
        // 根据逻辑模板id获取对应的逻辑物理模板信息
        IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService.getLogicTemplateWithPhysicalsById(templateId);

        if (AriusObjUtils.isNull(logicTemplateWithPhysicals)
                || CollectionUtils.isEmpty(logicTemplateWithPhysicals.getPhysicals())) {
            LOGGER.error(
                    "class=TemplateLogicManagerImpl||method=checkTemplateEditService||templateId={}||msg=indexTemplateLogic is empty",
                    templateId);
            return Result.buildFail("逻辑模板信息为空");
        }

        // 获取逻辑集群对应的物理模板的物理集群名称列表
        List<String> clusterPhyNameList = logicTemplateWithPhysicals.getPhysicals()
                .stream()
                .map(IndexTemplatePhy::getCluster)
                .distinct()
                .collect(Collectors.toList());

        // 查看对应的物理集群是否开启了指定的索引模板服务
        for (String clusterPhyName : clusterPhyNameList) {
            Result<Boolean> checkResult = checkTemplateSrvByClusterName(clusterPhyName, templateSrvId);
            if (checkResult.failed()) {
                return Result.buildFailWithMsg(false, checkResult.getMessage());
            }
        }

        return Result.buildSucc(true);
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

        if (templateLabelService.isImportantIndex(logicId)) {
            return Result.buildOpForBidden("禁止操作重要索引，请联系Arius服务号处理");
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
        if (!indexTemplateService.exist(logicId)) { return true; }

        // 1. 获取dcdr标识位
        boolean dcdrFlag = false;
        long totalIndexCheckPointDiff = 0;
        try {
            IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService.getLogicTemplateWithPhysicalsById(logicId);
            IndexTemplatePhy slavePhyTemplate  = logicTemplateWithPhysicals.getSlavePhyTemplate();
            IndexTemplatePhy masterPhyTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
            if (null != masterPhyTemplate && null != slavePhyTemplate) {
                dcdrFlag = templateDcdrManager.syncExistTemplateDCDR(masterPhyTemplate.getId(), slavePhyTemplate.getCluster());
            }
        } catch (Exception e) {
            LOGGER.error("class=TemplateLogicManagerImpl||method=updateDCDRInfo||templateName={}||errorMsg={}",
                    logicId, e.getMessage(), e);
        }

        // 2. 获取位点差dcdr
        if (dcdrFlag) {
            try {
                Tuple<Long, Long> masterAndSlaveTemplateCheckPointTuple = templateDcdrManager.getMasterAndSlaveTemplateCheckPoint(logicId);
                totalIndexCheckPointDiff = Math.abs(masterAndSlaveTemplateCheckPointTuple.getV1() - masterAndSlaveTemplateCheckPointTuple.getV2());
            } catch (Exception e) {
                LOGGER.error("class=TemplateLogicManagerImpl||method=updateDCDRInfo||templateId={}||errorMsg={}",
                        logicId, e.getMessage(), e);
            }
        }

        try {
            IndexTemplateDTO indexTemplateDTO = new IndexTemplateDTO();
            indexTemplateDTO.setId(logicId);
            indexTemplateDTO.setHasDCDR(dcdrFlag);
            indexTemplateDTO.setCheckPointDiff(totalIndexCheckPointDiff);
            indexTemplateService.editTemplateInfoTODB(indexTemplateDTO);
        } catch (AdminOperateException e) {
            LOGGER.error(
                "class=TemplateLogicManagerImpl||method=updateDCDRInfo||templateId={}||errorMsg=failed to edit template",
                logicId, e.getMessage(), e);
        }

        return true;
    }

    @Override
    public Result<List<ConsoleTemplateVO>> getTemplateVOByPhyCluster(String phyCluster) {
        // 根据物理集群名称获取全量逻辑模板列表
        List<IndexTemplatePhyWithLogic> templateByPhyCluster = indexTemplatePhyService.getTemplateByPhyCluster(phyCluster);

        // 转化为视图列表展示
        List<ConsoleTemplateVO> consoleTemplateVOLists = new ArrayList<>();
        templateByPhyCluster.forEach(indexTemplatePhyWithLogic -> consoleTemplateVOLists.add(buildTemplateVO(indexTemplatePhyWithLogic)));

        return Result.buildSucc(consoleTemplateVOLists);
    }

    @Override
    public Result<Void> clearIndices(Integer templateId, List<String> indices, Integer projectId) {
        // TODO: zeyin 添加project权限校验 , 操作记录
        if (CollectionUtils.isEmpty(indices)) { return Result.buildParamIllegal("清理索引不能为空");}

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(templateId);
        if (null != templateLogicWithPhysical && CollectionUtils.isEmpty(templateLogicWithPhysical.getPhysicals())) {
            return Result.buildFail(String.format("模板[%d]不存在Arius平台", templateId));
        }

        boolean succ = false;
        List<IndexTemplatePhy> indexTemplatePhyList = Optional.ofNullable(templateLogicWithPhysical.getPhysicals())
                .orElse(Lists.newArrayList());
        for (IndexTemplatePhy templatePhysical : indexTemplatePhyList) {
             succ = indices.size() == esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(), indices, RETRY_TIMES);
        }
        return Result.build(succ);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> adjustShard(Integer logicTemplateId,
                                    Integer shardNum,
                                    Integer projectId) throws AdminOperateException {
        // TODO: zeyin 添加project权限校验 , 操作记录
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);
        IndexTemplatePhyDTO updateParam = new IndexTemplatePhyDTO();
        for (IndexTemplatePhy templatePhy : templatePhyList) {
            if (templatePhy.getShard().equals(shardNum)) { throw new AdminOperateException("该模板已经是" + shardNum + "分片", FAIL);}

            updateParam.setId(templatePhy.getId());
            updateParam.setShard(shardNum);
            Result<Void> updateDBResult = indexTemplatePhyService.update(updateParam);
            if (updateDBResult.failed()) { throw new AdminOperateException(updateDBResult.getMessage(), FAIL);}

            boolean succ = esTemplateService.syncUpdateShardNum(templatePhy.getCluster(), templatePhy.getName(), shardNum, RETRY_TIMES);
            if (!succ) { throw new AdminOperateException(String.format("同步修改es集群[%s]中模板[%]shard数[%d]失败, 请确认集群是否正常",
                            templatePhy.getCluster(), templatePhy.getName(), shardNum), FAIL);}
        }

        return Result.buildSucc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> upgrade(Integer templateId, String operator) throws AdminOperateException {
        // TODO: zeyin 添加project权限校验 , 操作记录
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(templateId);
        if (CollectionUtils.isEmpty(templatePhyList)) { return Result.buildFail("模板不存在");}

        IndexTemplatePhyDTO updateParam = new IndexTemplatePhyDTO();
        for (IndexTemplatePhy templatePhy : templatePhyList) {
            updateParam.setId(templatePhy.getId());
            updateParam.setShard(updateParam.getShard());
            updateParam.setRack("");
            updateParam.setVersion(templatePhy.getVersion() + 1);

            Result<Void> editResult = templatePhyManager.editTemplateWithoutCheck(updateParam, operator, RETRY_TIMES);
            if (editResult.failed()) { throw new AdminOperateException(editResult.getMessage(), FAIL);}

            preCreateManager.asyncCreateTodayAndTomorrowIndexByPhysicalId(templatePhy.getId());
        }

        return Result.buildSucc();
    }

    @Override
    public Result<List<ConsoleTemplateVO>> listTemplateVOByLogicCluster(String clusterLogicName, Integer projectId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByName(clusterLogicName);
        if (clusterLogic == null) {
            return Result.buildFail();
        }
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        if (clusterRegion == null) {
            return Result.buildFail();
        }
        Result<List<IndexTemplate>> listResult = indexTemplateService.listByRegionId(Math.toIntExact(clusterRegion.getId()));
        List<ConsoleTemplateVO> vos = listResult.getData().stream().map(indexTemplate -> {
            ConsoleTemplateVO vo = new ConsoleTemplateVO();
            BeanUtils.copyProperties(indexTemplate,vo);
            return vo;
        }).collect(Collectors.toList());

        return Result.buildSucc(vos);
    }

    /**************************************** private method ***************************************************/
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
                    .filter(templatePhysical -> templatePhysical.getRole().equals( TemplateDeployRoleEnum.MASTER.getCode()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(templatePhysicalsMaster)) {
                errMsg.add("没有部署master：" + templateLogic.getName() + "(" + templateLogic.getId() + ")");
            }
        }

        if (CollectionUtils.isEmpty(errMsg)) {
            return Result.buildSucc();
        }

        return Result.build( ResultType.ADMIN_META_ERROR.getCode(), String.join(",", errMsg));
    }

    /**
     * 构建逻辑模板视图
     */
    private ConsoleTemplateVO buildTemplateVO(IndexTemplatePhyWithLogic param) {
        if (param == null) {
            return null;
        }

        ConsoleTemplateVO consoleTemplateVO = ConvertUtil.obj2Obj(param.getLogicTemplate(), ConsoleTemplateVO.class);
        consoleTemplateVO.setClusterPhies(Collections.singletonList(param.getCluster()));
        return consoleTemplateVO;
    }


    /**
     * 获取逻辑模板标签列表
     * @param includeLabelIds 包含的标签ID列表
     * @param excludeLabelIds 排除的标签ID列表
     * @return
     */
    private List<TemplateLabel> fetchLabels(String includeLabelIds, String excludeLabelIds) {
        Result<List<TemplateLabel>> result = templateLabelService.listByLabelIds(includeLabelIds,
                excludeLabelIds);
        if (result.failed()) {
            throw new AmsRemoteException("获取模板标签失败");
        }

        return result.getData();
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
        indexTemplateLogicAggregate.setProjectTemplateAuth(projectTemplateAuths.get(indexTemplateLogicWithCluster.getId()));
        indexTemplateLogicAggregate.setIndexTemplateValue(logicTemplateValues.get(indexTemplateLogicWithCluster.getId()));
        indexTemplateLogicAggregate.setHasDCDR(hasDCDRLogicIds.contains(indexTemplateLogicWithCluster.getId()));

        return indexTemplateLogicAggregate;
    }

    /**
     * 获取模板价值分
     *
     * @return
     */
    private List<IndexTemplateValue> fetchTemplateValues() {
        List<IndexTemplateValue> templateValues = Lists.newArrayList();
        Result<List<IndexTemplateValue>> listTemplateValueResult = templateSattisService.listTemplateValue();
        if (listTemplateValueResult.success()) {
            templateValues.addAll(listTemplateValueResult.getData());
        }

        return templateValues;
    }

    /**
     * 获取逻辑模板聚合信息
     * @param logicTemplates 逻辑模板列表
     * @param projectId project Id
     * @return
     */
    private List<IndexTemplateLogicAggregate> fetchLogicTemplatesAggregates(List<IndexTemplateWithCluster> logicTemplates,
                                                                            Integer projectId) {
        List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(logicTemplates)) {

            // 模板权限
            Map<Integer, ProjectTemplateAuth> projectTemplateAuths = ConvertUtil
                    .list2Map(projectLogicTemplateAuthService.getTemplateAuthsByProjectId(projectId), ProjectTemplateAuth::getTemplateId);
            
            // 模板
            Map<Integer, IndexTemplateValue> logicTemplateValues = ConvertUtil.list2Map(fetchTemplateValues(),
                    IndexTemplateValue::getLogicTemplateId);

            // 具备DCDR的模版id
            List<Integer> hasDCDRLogicIds = getHaveDCDRLogicIds();

            for (IndexTemplateWithCluster combineLogicCluster : logicTemplates) {
                try {
                    indexTemplateLogicAggregates.add(fetchTemplateAggregate(combineLogicCluster, projectTemplateAuths,
                             logicTemplateValues, hasDCDRLogicIds));
                } catch (Exception e) {
                    LOGGER.warn(
                            "class=LogicTemplateManager||method=fetchLogicTemplatesAggregates||" + "combineLogicCluster={}",
                            combineLogicCluster, e);
                }
            }
        }

        return indexTemplateLogicAggregates;
    }

    /**
     * 校验物理集群是否开启了指定的索引模板服务
     * @param clusterPhyName 物理集群
     * @param templateSrvId  索引服务id
     * @return 校验结果
     */
    private Result<Boolean> checkTemplateSrvByClusterName(String clusterPhyName, Integer templateSrvId) {
        TemplateServiceEnum templateServiceEnum = TemplateServiceEnum.getById(templateSrvId);
        if (AriusObjUtils.isNull(templateServiceEnum)) {
            return Result.buildFail("指定校验的索引服务id不存在");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail(String.format("模板归属集群[%s]不存在", clusterPhyName));
        }

        // 校验物理集群是否已经开启了指定的索引模板服务
        List<String> templateSrvList = ListUtils.string2StrList(clusterPhy.getTemplateSrvs());
        if (!templateSrvList.contains(templateSrvId.toString())) {
            return Result.buildFail(String.format("该模板归属集群未开启%s服务",templateServiceEnum.getServiceName()));
        }

        return Result.buildSucc();
    }

    private IndexTemplateConfig getDefaultTemplateConfig(Integer logicId) {
        IndexTemplateConfig indexTemplateConfig = new IndexTemplateConfig();
        indexTemplateConfig.setLogicId(logicId);
        indexTemplateConfig.setAdjustTpsFactor(1.0);
        indexTemplateConfig.setAdjustShardFactor(1.0);
        indexTemplateConfig.setDynamicLimitEnable( AdminConstant.YES);
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
        if(param.getDisableIndexRollover() != null) {
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

        buildExtraField(indexTemplateDTO);
        buildCyclicalRoll(indexTemplateDTO, param);
        buildShardNum(indexTemplateDTO, param);
        buildPhysicalInfo(indexTemplateDTO, param);

        return indexTemplateDTO;
    }

    private void buildExtraField(IndexTemplateDTO indexTemplateDTO) {
        indexTemplateDTO.setIngestPipeline(indexTemplateDTO.getName());
        indexTemplateDTO.setDiskSize(indexTemplateDTO.getDiskSize());
        indexTemplateDTO.setQuota(indexTemplateDTO.getDiskSize());
        //todo: 0.3干掉
        indexTemplateDTO.setIdField("");
        indexTemplateDTO.setRoutingField("");

        if (null == indexTemplateDTO.getDesc()) { indexTemplateDTO.setDesc("");}
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
                    if (StringUtils.isNotBlank(param.getDateField()) && !AdminConstant.MM_DD_DATE_FORMAT.equals(param.getDateField())) {
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
        if (clusterSettingHotDay > 0) { indexTemplateDTO.setHotTime(clusterSettingHotDay);}
        else { indexTemplateDTO.setHotTime(-1);}

        if (StringUtils.isNotBlank(param.getSetting())) { indexTemplatePhyDTO.setSettings(param.getSetting());}
        else { indexTemplatePhyDTO.setSettings("{}");}

        if (StringUtils.isNotBlank(param.getMapping())) {
            AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
            ariusTypeProperty.setTypeName(DEFAULT_INDEX_MAPPING_TYPE);
            ariusTypeProperty.setProperties(JSON.parseObject(param.getMapping()));
            indexTemplatePhyDTO.setMappings(ariusTypeProperty.toMappingJSON().getJSONObject(DEFAULT_INDEX_MAPPING_TYPE).toJSONString());
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