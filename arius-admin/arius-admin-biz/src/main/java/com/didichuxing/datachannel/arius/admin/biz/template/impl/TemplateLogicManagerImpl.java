package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.page.TemplateLogicPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.TemplateColdManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.*;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template.TemplateOperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.ESTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.*;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateAddEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AmsRemoteException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.component.ResponsibleConvertTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateSattisService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum.OWN;
import static com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum.isTemplateAuthExitByCode;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.TEMPLATE_LOGIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_MAPPING;
import static com.didichuxing.datachannel.arius.admin.core.service.template.physic.impl.IndexTemplatePhyServiceImpl.NOT_CHECK;

@Component
public class TemplateLogicManagerImpl implements TemplateLogicManager {

    private static final ILog           LOGGER = LogFactory.getLog(TemplateLogicManager.class);

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private TemplateQuotaManager        templateQuotaManager;

    @Autowired
    private TemplateSattisService       templateSattisService;

    @Autowired
    private TemplateLabelService        templateLabelService;

    @Autowired
    private TemplateColdManager         templateColdManager;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private ClusterPhyService           clusterPhyService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private AppService                  appService;

    @Autowired
    private ResponsibleConvertTool      responsibleConvertTool;

    @Autowired
    private TemplatePhyManager          templatePhyManager;

    @Autowired
    private HandleFactory               handleFactory;

    @Autowired
    private TemplateDCDRManager templateDcdrManager;

    /**
     * 校验所有逻辑模板元数据信息
     *
     * @return
     */
    @Override
    public boolean checkAllLogicTemplatesMeta() {
        Map<Integer, App> appId2AppMap = ConvertUtil.list2Map(appService.listApps(), App::getId);
        List<IndexTemplate> logicTemplates = indexTemplateService.getAllLogicTemplates();
        for (IndexTemplate templateLogic : logicTemplates) {
            try {
                Result<Void> result = checkLogicTemplateMeta(templateLogic, appId2AppMap);
                if (result.success()) {
                    LOGGER.info("class=TemplateLogicManagerImpl||method=metaCheck||msg=succeed||logicId={}", templateLogic.getId());
                } else {
                    LOGGER.warn("class=TemplateLogicManagerImpl||method=metaCheck||msg=fail||logicId={}||failMsg={}", templateLogic.getId(),
                            result.getMessage());
                }
            } catch (Exception e) {
                LOGGER.error("class=TemplateLogicServiceImpl||method=metaCheck||errMsg={}||logicId={}||",
                        e.getMessage(), templateLogic.getId(), e);
            }
        }

        return true;
    }

    /**
     * 获取模板信息
     * @param excludeLabelIds 排除的Label ID列表
     * @param includeLabelIds 包含的Label ID列表
     * @return list
     */
    @Override
    public List<IndexTemplateWithLabels> getByLabelIds(String includeLabelIds, String excludeLabelIds) {

        List<IndexTemplateWithLabels> indexTemplateLogicWithLabels = Lists.newArrayList();

        Map<Integer, IndexTemplate> logicTemplatesMappings = indexTemplateService.getAllLogicTemplatesMap();

        List<TemplateLabel> templateLabels = fetchLabels(includeLabelIds, excludeLabelIds);
        templateLabels.stream().forEach(templateLabel -> {
            Integer templateId = templateLabel.getIndexTemplateId();

            IndexTemplate indexTemplate = logicTemplatesMappings.get(templateId);
            IndexTemplateWithLabels logicWithLabel = responsibleConvertTool.obj2Obj(indexTemplate,
                    IndexTemplateWithLabels.class);
            if (logicWithLabel != null) {
                logicWithLabel.setLabels(templateLabel.getLabels());
                indexTemplateLogicWithLabels.add(logicWithLabel);
            }

        });

        return indexTemplateLogicWithLabels;
    }

    /**
     * 获取最近访问该模板的APP
     *
     * @param logicId logicId
     * @return result
     */
    @Override
    public List<App> getLogicTemplateAppAccess(Integer logicId) {
        Result<Map<Integer, Long>> result = templateSattisService.getTemplateAccessAppIds(logicId, 7);
        if (result.failed()) {
            throw new AmsRemoteException("获取访问模板的APP列表失败");
        }

        if (null == result.getData() || 0 == result.getData().size()) {
            return Lists.newArrayList();
        }

        List<App> apps = appService.listApps();
        Map<Integer, App> id2AppMap = ConvertUtil.list2Map(apps, App::getId);

        return result.getData().keySet().stream().map(id2AppMap::get).collect( Collectors.toList());
    }

    /**
     * 获取模板的标签信息
     * @param logicId 模板id
     * @return label
     */
    @Override
    public IndexTemplateWithLabels getLabelByLogicId(Integer logicId) {
        IndexTemplateWithLabels indexTemplateLogicWithLabels = ConvertUtil
                .obj2Obj(indexTemplateService.getLogicTemplateById(logicId), IndexTemplateWithLabels.class);

        if (indexTemplateLogicWithLabels != null) {
            indexTemplateLogicWithLabels.setLabels(templateLabelService.listTemplateLabel(logicId));
        }

        return indexTemplateLogicWithLabels;
    }

    /**
     * 新建逻辑模板 无参数校验
     *
     * @param param    模板信息
     * @param operator 操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> addTemplateWithoutCheck(IndexTemplateDTO param,
                                                   String operator) throws AdminOperateException {
        initLogicParam(param);

        // 初始化pipelineID
        param.setIngestPipeline(param.getName());

        // 保存数据库
        Result<Void> result = indexTemplateService.addTemplateWithoutCheck(param);
        if (result.success()) {
            Result<Void> addPhysicalResult = templatePhyManager.addTemplatesWithoutCheck(param.getId(),
                    param.getPhysicalInfos());

            if (addPhysicalResult.failed()) {
                throw new AdminOperateException("新建物理模板失败");
            }

            IndexTemplateConfig defaultTemplateConfig = getDefaultTemplateConfig(param.getId());
            if (param.getDisableSourceFlags() != null) {
                defaultTemplateConfig.setDisableSourceFlags(param.getDisableSourceFlags());
            }

            if(param.getDisableIndexRollover() != null) {
                defaultTemplateConfig.setDisableIndexRollover(param.getDisableIndexRollover());
            }

            if (param.getPreCreateFlags() != null) {
                defaultTemplateConfig.setPreCreateFlags(param.getPreCreateFlags());
            }

            if (param.getShardNum() != null) {
                defaultTemplateConfig.setShardNum(param.getShardNum());
            }

            // 生成配置记录
            insertTemplateConfig(defaultTemplateConfig);

            // 记录操作记录
            operateRecordService.save(TEMPLATE, ADD, param.getId(), JSON.toJSONString(new TemplateOperateRecord(TemplateOperateRecordEnum.NEW.getCode(), "新增模板")), operator);

            SpringTool.publish(new LogicTemplateAddEvent(this, indexTemplateService.getLogicTemplateById(param.getId())));
        }

        return Result.build(result.success(), param.getId());
    }

    /**
     * 新建逻辑模板
     * @param param 模板信息
     * @param operator 操作人
     * @return result
     * @throws AdminOperateException 操作es失败 或者保存物理模板信息失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Result<Integer> createLogicTemplate(IndexTemplateDTO param,
                                               String operator) throws AdminOperateException {
        Result<Void> checkResult = indexTemplateService.validateTemplate(param, ADD);
        if (checkResult.failed()) {
            LOGGER.warn("class=TemplateLogicServiceImpl||method=addTemplate||msg={}", checkResult.getMessage());
            return Result.buildFrom(checkResult);
        }

        LOGGER.info("class=TemplateLogicServiceImpl||method=addTemplate||id={}||hotTime={}||name={}||physicalInfos={}",
                param.getId(), param.getHotTime(), param.getName(), JSON.toJSONString(param.getPhysicalInfos()));

        if (param.getPhysicalInfos() != null) {
            setIndexTemplateLogicHotTime(param);
        }

        checkResult = templatePhyManager.validateTemplates(param.getPhysicalInfos(), ADD);
        if (checkResult.failed()) {
            LOGGER.warn("class=TemplateLogicServiceImpl||method=addTemplate||msg={}", checkResult.getMessage());
            return Result.buildFrom(checkResult);
        }

        // 校验资源与模板的数据中心是否匹配
        List<String> clusters = param.getPhysicalInfos().stream().map(IndexTemplatePhyDTO::getCluster)
                .collect(Collectors.toList());
        for (String cluster : clusters) {
            ClusterPhy clusterPhy = clusterPhyService.getClusterByName(cluster);
            if (!clusterPhy.getDataCenter().equals(param.getDataCenter())) {
                return Result.buildParamIllegal("物理集群数据中心不符");
            }
        }

        return addTemplateWithoutCheck(param, operator);
    }

    private void setIndexTemplateLogicHotTime(IndexTemplateDTO param) {
        for (IndexTemplatePhyDTO physicalDTO : param.getPhysicalInfos()) {
            physicalDTO.setLogicId(NOT_CHECK);
            physicalDTO.setName(param.getName());
            physicalDTO.setExpression(param.getExpression());
            physicalDTO.setWriteRateLimit(param.getWriteRateLimit());

            if (param.getHotTime() == null || param.getHotTime() <= 0) {
                int clusterSettingHotDay = templateColdManager.fetchClusterDefaultHotDay(physicalDTO.getCluster());
                if (clusterSettingHotDay > 0) {
                    param.setHotTime(clusterSettingHotDay);
                }
            }
        }
    }

    /**
     * 获取所有逻辑模板聚合
     *
     * @param appId 当前App Id
     * @return
     */
    @Override
    public List<IndexTemplateLogicAggregate> getAllTemplatesAggregate(Integer appId) {
        List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = new ArrayList<>();
        List<IndexTemplateWithCluster> logicTemplates = indexTemplateService
                .getAllLogicTemplateWithClusters();

        if (CollectionUtils.isNotEmpty(logicTemplates)) {
            indexTemplateLogicAggregates = fetchLogicTemplatesAggregates(logicTemplates, appId);
        }

        return indexTemplateLogicAggregates;
    }

    /**
     * 获取逻辑集群所有逻辑模板聚合
     *
     * @param logicClusterId 逻辑集群ID
     * @param appId 操作的App Id
     * @return
     */
    @Override
    public List<IndexTemplateLogicAggregate> getLogicClusterTemplatesAggregate(Long logicClusterId, Integer appId) {

        if (logicClusterId == null) {
            return new ArrayList<>();
        }

        List<IndexTemplateWithCluster> logicTemplates = indexTemplateService
                .getLogicTemplateWithClustersByClusterId(logicClusterId);

        if (CollectionUtils.isEmpty(logicTemplates)) {
            return new ArrayList<>();
        }

        return fetchLogicTemplatesAggregates(logicTemplates, appId);
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
            Map<Integer, String> appId2AppNameMap = Maps.newHashMap();

            for (IndexTemplateLogicAggregate aggregate : aggregates) {
                ConsoleTemplateVO consoleTemplateVO = fetchConsoleTemplate(aggregate);

                //获取项目名称
                Integer appId = consoleTemplateVO.getAppId();
                if (appId2AppNameMap.containsKey(appId)) {
                    consoleTemplateVO.setAppName(appId2AppNameMap.get(appId));
                } else {
                    String appName = appService.getAppName(appId);
                    if (!AriusObjUtils.isNull(appName)) {
                        consoleTemplateVO.setAppName(appName);
                        appId2AppNameMap.put(appId, appName);
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
                templateLogic.setAuthType(AppTemplateAuthEnum.NO_PERMISSION.getCode());
                if (aggregate.getAppTemplateAuth() != null) {
                    templateLogic.setAuthType(aggregate.getAppTemplateAuth().getType());
                }

                templateLogic.setValue(DEFAULT_TEMPLATE_VALUE);
                if (aggregate.getIndexTemplateValue() != null) {

                    templateLogic.setValue(aggregate.getIndexTemplateValue().getValue());
                }

                if (aggregate.getEsTemplateQuotaUsage() != null) {
                    templateLogic.setQuotaUsage(ConvertUtil.obj2Obj(
                            aggregate.getEsTemplateQuotaUsage(), QuotaUsage.class));
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
    public List<ConsoleTemplateVO> getConsoleTemplateVOSForClusterLogic(Long clusterLogicId, Integer appId) {
        if (AriusObjUtils.isNull(clusterLogicId)) {
            return Lists.newArrayList();
        }

        List<IndexTemplate> logicClusterTemplates = indexTemplateService.getLogicClusterTemplates(clusterLogicId);

        Set<Integer> templateLogicIds = logicClusterTemplates.stream().map(IndexTemplate::getId)
                .collect(Collectors.toSet());

        return getConsoleTemplatesVOS(appId)
                .stream()
                .filter(r -> templateLogicIds.contains(r.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsoleTemplateVO> getConsoleTemplatesVOS(Integer appId) {
        return fetchConsoleTemplates(getAllTemplatesAggregate(appId));
    }

    @Override
    public List<IndexTemplate> getTemplatesByAppIdAndAuthType(Integer appId, Integer authType) {
        if (!appService.isAppExists(appId)) {
            return Lists.newArrayList();
        }

        //超级用户对所有模板都是管理权限
        if (appService.isSuperApp(appId) && !OWN.getCode().equals(authType)) {
            return Lists.newArrayList();
        }

        if (!isTemplateAuthExitByCode(authType)) {
            return Lists.newArrayList();
        }

        switch (AppTemplateAuthEnum.valueOf(authType)) {
            case OWN:
                if (appService.isSuperApp(appId)) {
                    return indexTemplateService.getAllLogicTemplates();
                }else {
                    return indexTemplateService.getAppLogicTemplatesByAppId(appId);
                }

            case RW:
                List<AppTemplateAuth> appActiveTemplateRWAuths = appLogicTemplateAuthService
                    .getAppActiveTemplateRWAuths(appId);
                return appActiveTemplateRWAuths
                        .stream()
                        .map(r -> indexTemplateService.getLogicTemplateById(r.getTemplateId()))
                        .collect(Collectors.toList());

            case R:
                List<AppTemplateAuth> appActiveTemplateRAuths = appLogicTemplateAuthService
                    .getAppActiveTemplateRAuths(appId);
                return appActiveTemplateRAuths
                        .stream()
                        .map(r -> indexTemplateService.getLogicTemplateById(r.getTemplateId()))
                        .collect(Collectors.toList());

            case NO_PERMISSION:
                List<IndexTemplate> allLogicTemplates = indexTemplateService.getAllLogicTemplates();
                List<Integer> appRAndRwAuthTemplateIdList = appLogicTemplateAuthService
                        .getAppTemplateRWAndRAuthsWithoutCodecResponsible(appId)
                        .stream()
                        .map(AppTemplateAuth::getTemplateId)
                        .collect(Collectors.toList());

                List<IndexTemplate> notAuthIndexTemplateList = allLogicTemplates
                        .stream()
                        .filter(r -> !appId.equals(r.getAppId()) && !appRAndRwAuthTemplateIdList.contains(r.getId()))
                        .collect(Collectors.toList());
                return notAuthIndexTemplateList;

            default:
                return Lists.newArrayList();

        }
    }

    @Override
    public List<String> getTemplateLogicNames(Integer appId) {
        List<IndexTemplate> templateLogics = indexTemplateService.getAppLogicTemplatesByAppId(appId);

        return templateLogics.stream().map(IndexTemplate::getName).collect(Collectors.toList());
    }

    @Override
    public Result<Void> editTemplate(IndexTemplateDTO param, String operator)
            throws AdminOperateException {
        return indexTemplateService.editTemplate(param, operator);
    }

    @Override
    public Result<Void> delTemplate(Integer logicTemplateId, String operator)
            throws AdminOperateException {
        return indexTemplateService.delTemplate(logicTemplateId, operator);
    }

    @Override
    public PaginationResult<ConsoleTemplateVO> pageGetConsoleTemplateVOS(TemplateConditionDTO condition, Integer appId) {
        BaseHandle baseHandle     = handleFactory.getByHandlerNamePer(TEMPLATE_LOGIC.getPageSearchType());
        if (baseHandle instanceof TemplateLogicPageSearchHandle) {
            TemplateLogicPageSearchHandle handle = (TemplateLogicPageSearchHandle) baseHandle;
            return handle.doPageHandle(condition, condition.getAuthType(), appId);
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
    public Result<Void> checkAppAuthOnLogicTemplate(Integer logicId, Integer appId) {
        if (AriusObjUtils.isNull(logicId)) {
            return Result.buildParamIllegal("索引id为空");
        }

        if (AriusObjUtils.isNull(appId)) {
            return Result.buildParamIllegal("应用Id为空");
        }

        IndexTemplate templateLogic = indexTemplateService.getLogicTemplateById(logicId);
        if (templateLogic == null) {
            return Result.buildNotExist("索引不存在");
        }

        if (templateLabelService.isImportantIndex(logicId)) {
            return Result.buildOpForBidden("禁止操作重要索引，请联系Arius服务号处理");
        }

        if (appService.isSuperApp(appId)) {
            return Result.buildSucc();
        }

        if (!templateLogic.getAppId().equals(appId)) {
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
    public Result<List<ConsoleTemplateVO>> getTemplateVOByPhyCluster(String phyCluster, Integer appId) {
        // 根据物理集群名称获取全量逻辑模板列表
        List<IndexTemplatePhyWithLogic> templateByPhyCluster = indexTemplatePhyService.getTemplateByPhyCluster(phyCluster);

        // 转化为视图列表展示
        List<ConsoleTemplateVO> consoleTemplateVOLists = new ArrayList<>();
        templateByPhyCluster.forEach(indexTemplatePhyWithLogic -> consoleTemplateVOLists.add(buildTemplateVO(indexTemplatePhyWithLogic, appId)));

        return Result.buildSucc(consoleTemplateVOLists);
    }

    /**************************************** private method ***************************************************/
    /**
     * 校验逻辑模板Master ROLE物理模板是否存在
     * @param templateLogic 逻辑模板
     * @param appId2AppMap APP映射
     * @return
     */
    private Result<Void> checkLogicTemplateMeta(IndexTemplate templateLogic, Map<Integer, App> appId2AppMap) {
        List<String> errMsg = Lists.newArrayList();

        if (!appId2AppMap.containsKey(templateLogic.getAppId())) {
            errMsg.add("所属APP ID不存在：" + templateLogic.getAppId());
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
    private ConsoleTemplateVO buildTemplateVO(IndexTemplatePhyWithLogic param, Integer appId) {
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
     * @param appTemplateAuths                 App模板权限
     * @param templateQuotaUsages              逻辑模板Quota使用率
     * @param logicTemplateValues              逻辑模板健康分
     */
    private IndexTemplateLogicAggregate fetchTemplateAggregate(IndexTemplateWithCluster indexTemplateLogicWithCluster,
                                                               Map<Integer, AppTemplateAuth> appTemplateAuths,
                                                               Map<Integer, ESTemplateQuotaUsage> templateQuotaUsages,
                                                               Map<Integer, IndexTemplateValue> logicTemplateValues,
                                                               List<Integer> hasDCDRLogicIds) {

        IndexTemplateLogicAggregate indexTemplateLogicAggregate = new IndexTemplateLogicAggregate();

        indexTemplateLogicAggregate.setIndexTemplateLogicWithCluster(indexTemplateLogicWithCluster);
        indexTemplateLogicAggregate.setAppTemplateAuth(appTemplateAuths.get(indexTemplateLogicWithCluster.getId()));
        indexTemplateLogicAggregate.setEsTemplateQuotaUsage(templateQuotaUsages.get(indexTemplateLogicWithCluster.getId()));
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
     * @param appId App Id
     * @return
     */
    private List<IndexTemplateLogicAggregate> fetchLogicTemplatesAggregates(List<IndexTemplateWithCluster> logicTemplates,
                                                                            Integer appId) {
        List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(logicTemplates)) {

            // 模板权限
            Map<Integer, AppTemplateAuth> appTemplateAuths = ConvertUtil
                    .list2Map(appLogicTemplateAuthService.getTemplateAuthsByAppId(appId), AppTemplateAuth::getTemplateId);

            // quota
            Map<Integer, ESTemplateQuotaUsage> templateQuotaUsages = ConvertUtil
                    .list2Map( templateQuotaManager.listAllTemplateQuotaUsageWithCache(), ESTemplateQuotaUsage::getLogicId);

            // 模板
            Map<Integer, IndexTemplateValue> logicTemplateValues = ConvertUtil.list2Map(fetchTemplateValues(),
                    IndexTemplateValue::getLogicTemplateId);

            // 具备DCDR的模版id
            List<Integer> hasDCDRLogicIds = getHaveDCDRLogicIds();

            for (IndexTemplateWithCluster combineLogicCluster : logicTemplates) {
                try {
                    indexTemplateLogicAggregates.add(fetchTemplateAggregate(combineLogicCluster, appTemplateAuths,
                            templateQuotaUsages, logicTemplateValues, hasDCDRLogicIds));
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

    private void initLogicParam(IndexTemplateDTO param) {
        if (param.getDateFormat() == null) {
            param.setDateFormat("");
        } else {
            param.setDateFormat(param.getDateFormat().replace("Y", "y"));
        }

        if (param.getDateField() == null) {
            param.setDateField("");
        }

        if (param.getDateFieldFormat() == null) {
            param.setDateFieldFormat("");
        }

        if (param.getIdField() == null) {
            param.setIdField("");
        }

        if (param.getRoutingField() == null) {
            param.setRoutingField("");
        }

        if (param.getDesc() == null) {
            param.setDesc("");
        }

        if (param.getLibraDepartment() == null) {
            param.setLibraDepartment("");
        }

        if (param.getLibraDepartmentId() == null) {
            param.setLibraDepartmentId("");
        }

        if (param.getHotTime() == null) {
            param.setHotTime(-1);
        }

        if (param.getDisableSourceFlags() == null) {
            param.setDisableSourceFlags(false);
        }

        if (param.getPreCreateFlags() == null) {
            param.setPreCreateFlags(true);
        }

        if (param.getShardNum() == null) {
            param.setShardNum(-1);
        }
    }

    private IndexTemplateConfig getDefaultTemplateConfig(Integer logicId) {
        IndexTemplateConfig indexTemplateConfig = new IndexTemplateConfig();
        indexTemplateConfig.setLogicId(logicId);
        indexTemplateConfig.setAdjustRackTpsFactor(1.0);
        indexTemplateConfig.setAdjustRackShardFactor(1.0);
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
     *
     * @param indexTemplateConfig 索引模板配置
     */
    private boolean insertTemplateConfig(IndexTemplateConfig indexTemplateConfig) {
        return indexTemplateService.insertTemplateConfig(indexTemplateConfig).success();
    }

    /**
     * 校验模板名称是否是其他逻辑模板名称的前缀
     * @param templateName 逻辑模板名称
     * @return 校验的结果
     */
    private Result<Void> checkTemplateNamePrefix(String templateName) {
        if (StringUtils.isEmpty(templateName)) {
            return Result.buildFail("模板名称为空");
        }

        // 获取全部的正在使用的逻辑模板
        List<IndexTemplate> allLogicTemplates = indexTemplateService.getAllLogicTemplates();

        if (!CollectionUtils.isEmpty(allLogicTemplates)) {
            for (IndexTemplate indexTemplate : allLogicTemplates) {
                String logicTemplateName = indexTemplate.getName();

                // 为了隔离索引创建匹配的模板，新建模板的名称和其他模板名称不能互相存在前缀匹配
                if (logicTemplateName.startsWith(templateName)
                        || templateName.startsWith(logicTemplateName)) {
                    return Result.buildFail(String.format("目前和%s存在前缀匹配的情况", logicTemplateName));
                }
            }
        }

        return Result.buildSucc();
    }
}
