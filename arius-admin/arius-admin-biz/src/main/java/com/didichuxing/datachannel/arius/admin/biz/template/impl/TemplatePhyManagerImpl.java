package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.MILLIS_PER_DAY;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum.MASTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum.SLAVE;
import static com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory.genIndexNameClear;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.INDEX_SHARD_NUM;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.TEMPLATE_INDEX_INCLUDE_NODE_NAME;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalCopyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalUpgradeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplatePhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.impl.IndexTemplatePhyServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didiglobal.knowframework.elasticsearch.client.utils.JsonUtils;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@Component
public class TemplatePhyManagerImpl implements TemplatePhyManager {

    private static final ILog               LOGGER                       = LogFactory
        .getLog(IndexTemplatePhyServiceImpl.class);

    public static final Integer             NOT_CHECK                    = -100;
    private static final Integer            INDEX_OP_OK                  = 0;
    private static final Integer            TOMORROW_INDEX_NOT_CREATE    = 1;
    private static final Integer            EXPIRE_INDEX_NOT_DELETE      = 2;
    private static final Integer            INDEX_ALL_ERR                = TOMORROW_INDEX_NOT_CREATE
                                                                           + EXPIRE_INDEX_NOT_DELETE;

    private static final String             TEMPLATE_PHYSICAL_ID_IS_NULL = "物理模板id为空";

    private static final String             TEMPLATE_PHYSICAL_NOT_EXISTS = "物理模板不存在";

    private static final String             CHECK_FAIL_MSG               = "check fail||msg={}";

    public static final int                 MIN_SHARD_NUM                = 1;
    public static final int                 MAX_VERSION                  = 9;

    @Autowired
    private OperateRecordService            operateRecordService;

    @Autowired
    private ClusterPhyService               clusterPhyService;

    

    @Autowired
    private ESTemplateService               esTemplateService;

    @Autowired
    private PreCreateManager                preCreateManager;

    @Autowired
    private ClusterRoleHostService          clusterRoleHostService;

    @Autowired
    private IndexTemplateService            indexTemplateService;

    @Autowired
    private IndexTemplatePhyService         indexTemplatePhyService;
    @Autowired
    private IndexTemplatePhyService         physicalService;

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Autowired
    private ProjectService                  projectService;
    @Autowired
    private ESTemplateService templateService;

    @Autowired
    private ClusterRegionService            clusterRegionService;

    @Override
    public boolean checkMeta() {
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.listTemplate();

        List<IndexTemplate> templateLogics = indexTemplateService.listAllLogicTemplates();
        Map<Integer, IndexTemplate> logicId2IndexTemplateLogicMap = ConvertUtil.list2Map(templateLogics,
            IndexTemplate::getId);

        Multimap<String, IndexTemplatePhy> cluster2IndexTemplatePhysicalMultiMap = ConvertUtil
            .list2MulMap(templatePhysicals, IndexTemplatePhy::getCluster);

        Set<String> esClusters = clusterPhyService.listAllClusters().stream().map(ClusterPhy::getCluster)
            .collect(Collectors.toSet());

        for (String cluster : cluster2IndexTemplatePhysicalMultiMap.keySet()) {
            int tomorrowIndexNotCreateCount = 0;
            int expireIndexNotDeleteCount = 0;

            Collection<IndexTemplatePhy> clusterTemplates = cluster2IndexTemplatePhysicalMultiMap.get(cluster);

            for (IndexTemplatePhy templatePhysical : clusterTemplates) {
                try {
                    Result<Void> result = checkMetaInner(templatePhysical, logicId2IndexTemplateLogicMap, esClusters);
                    if (result.success()) {
                        LOGGER.info("class=TemplatePhyManagerImpl||method=metaCheck||msg=succ||physicalId={}",
                            templatePhysical.getId());
                    } else {
                        LOGGER.warn(
                            "class=TemplatePhyManagerImpl||method=metaCheck||msg=fail||physicalId={}||failMsg={}",
                            templatePhysical.getId(), result.getMessage());
                    }
                    int indexOpResult = checkIndexCreateAndExpire(templatePhysical, logicId2IndexTemplateLogicMap);
                    if (indexOpResult == TOMORROW_INDEX_NOT_CREATE || indexOpResult == INDEX_ALL_ERR) {
                        tomorrowIndexNotCreateCount++;
                    }
                    if (indexOpResult == EXPIRE_INDEX_NOT_DELETE || indexOpResult == INDEX_ALL_ERR) {
                        expireIndexNotDeleteCount++;
                    }

                } catch (Exception e) {
                    LOGGER.error("class=TemplatePhyServiceImpl||method=metaCheck||errMsg={}||physicalId={}||",
                        e.getMessage(), templatePhysical.getId(), e);
                }
            }
        }

        return true;
    }

    @Override
    public void syncMeta(Long physicalId, int retryCount, Map<String, TemplateConfig> templateConfigMap) throws ESOperateException {

        // 从数据库获取物理模板
        IndexTemplatePhy indexTemplatePhy = indexTemplatePhyService.getTemplateById(physicalId);
        if (indexTemplatePhy == null) {
            return;
        }
        // 获取到ES中的模版配置信息
        TemplateConfig templateConfig = templateConfigMap.get(indexTemplatePhy.getName());

        if (templateConfig == null) {
            // es集群中还没有模板，创建
            esTemplateService.syncCreate(indexTemplatePhy.getCluster(), indexTemplatePhy.getName(),
                indexTemplatePhy.getExpression(), indexTemplatePhy.getShard(), indexTemplatePhy.getShardRouting(),
                retryCount);

        } else {
            // 校验表达式
            if (!indexTemplatePhy.getExpression().equals(templateConfig.getTemplate())
                && esTemplateService.syncUpdateExpression(indexTemplatePhy.getCluster(), indexTemplatePhy.getName(),
                    indexTemplatePhy.getExpression(), retryCount)) {
                // 表达式不同（表达式发生变化），同步到ES集群
                LOGGER.info(
                    "class=TemplatePhyManagerImpl||method=syncMeta||msg=syncUpdateExpression succ||template={}||srcExp={}||tgtExp={}",
                    indexTemplatePhy.getName(), templateConfig.getTemplate(), indexTemplatePhy.getExpression());
            }

            // 标志shard是否需要修改
            Map<String, String> settings = templateConfig.getSetttings();
            String shardNum = settings.get(INDEX_SHARD_NUM);

            // 校验shard个数:如果相等就跳过 无需进行一次es操作
            if (String.valueOf(indexTemplatePhy.getShard()).equals(shardNum)) {
                return;
            }
            shardNum = String.valueOf(indexTemplatePhy.getShard());
            if (esTemplateService.syncUpdateShard(indexTemplatePhy.getCluster(), indexTemplatePhy.getName(),
                Integer.valueOf(shardNum), indexTemplatePhy.getShardRouting(), retryCount)) {
                // 同步变化到ES集群
                LOGGER.info(
                    "class=TemplatePhyManagerImpl||method=syncMeta||msg=syncUpdateShard succ||template={}||srcShard={}",
                    indexTemplatePhy.getName(), settings.get(INDEX_SHARD_NUM), shardNum);
            }
        }
    }

    @Override
    public Result<Void> delTemplate(Long physicalId, String operator) throws ESOperateException {
        return indexTemplatePhyService.delTemplate(physicalId, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delTemplateByLogicId(Integer logicId, String operator) throws ESOperateException {
        List<IndexTemplatePhy> indexTemplatePhies = indexTemplatePhyService.getTemplateByLogicId(logicId);

        boolean succ = true;
        if (CollectionUtils.isEmpty(indexTemplatePhies)) {
            LOGGER.info(
                "class=TemplatePhyManagerImpl||method=delTemplateByLogicId||logicId={}||msg=template no physical info!",
                logicId);
        } else {
            LOGGER.info(
                "class=TemplatePhyManagerImpl||method=delTemplateByLogicId||logicId={}||physicalSize={}||msg=template has physical info!",
                logicId, indexTemplatePhies.size());
            for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhies) {
                if (delTemplate(indexTemplatePhy.getId(), operator).failed()) {
                    succ = false;
                }

            }
        }

        return Result.build(succ);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> upgradeTemplate(TemplatePhysicalUpgradeDTO param, String operator,
                                        Integer projectId) throws ESOperateException {
        Result<Void> checkResult = checkUpgradeParam(param);
        if (checkResult.failed()) {
            LOGGER.warn("class=TemplatePhyManagerImpl||method=upgradeTemplate||msg={}",
                CHECK_FAIL_MSG + checkResult.getMessage());
            return checkResult;
        } else {
            IndexTemplatePhy oldIndexTemplatePhy = indexTemplatePhyService.getTemplateById(param.getPhysicalId());
            Integer projectIdByTemplateLogicId = indexTemplateService.getProjectIdByTemplateLogicId(oldIndexTemplatePhy.getLogicId());
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("模版 [%s] 升级版本：%s->%s", oldIndexTemplatePhy.getName(),
                            oldIndexTemplatePhy.getVersion(), param.getVersion()), operator, projectId,
                    param.getLogicId(), OperateTypeEnum.TEMPLATE_SERVICE_UPGRADED_VERSION, projectIdByTemplateLogicId);

        }

        return upgradeTemplateWithCheck(param, operator, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> rolloverUpgradeTemplate(TemplatePhysicalUpgradeDTO param,
                                                String operator) throws ESOperateException {
        //rollover 生版本号不需要对参数进行校验
        return upgradeTemplateWithCheck(param, operator, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> upgradeMultipleTemplate(List<TemplatePhysicalUpgradeDTO> params, String operator,
                                                   Integer projectId) throws ESOperateException {
        if (CollectionUtils.isEmpty(params)) {
            Result.buildFail("参数为空");
        }

        for (TemplatePhysicalUpgradeDTO param : params) {
            Result<Void> ret = upgradeTemplate(param, operator, projectId);
            if (ret.failed()) {
                throw new ESOperateException(ret.getMessage());
            }
        }
        return Result.buildSucc(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> copyTemplate(TemplatePhysicalCopyDTO param, String operator) throws AdminOperateException {
        Result<Void> checkResult = checkCopyParam(param);
        if (checkResult.failed()) {
            LOGGER.warn("class=TemplatePhyManagerImpl||method=copyTemplate||msg={}",
                CHECK_FAIL_MSG + checkResult.getMessage());
            return checkResult;
        }

        IndexTemplatePhy indexTemplatePhy = indexTemplatePhyService.getTemplateById(param.getPhysicalId());
        IndexTemplatePhyDTO tgtTemplateParam = ConvertUtil.obj2Obj(indexTemplatePhy, IndexTemplatePhyDTO.class);
        tgtTemplateParam.setCluster(param.getCluster());
        tgtTemplateParam.setRole(SLAVE.getCode());
        tgtTemplateParam.setShard(param.getShard());
        tgtTemplateParam.setVersion(indexTemplatePhy.getVersion());
        tgtTemplateParam.setRegionId(param.getRegionId());
        // 获取 master 中的集群然后进行 setting 和 mapping 的复制流程
        Integer logicId = indexTemplatePhy.getLogicId();
        Optional<IndexTemplatePhy> masterIndexTemplatePhyOption = indexTemplatePhyService.getTemplateByLogicId(logicId)
                .stream().filter(ip -> ip.getRole().equals(MASTER.getCode())).findAny();
        if (masterIndexTemplatePhyOption.isPresent()) {
            IndexTemplatePhy masterIndexTemplatePhy = masterIndexTemplatePhyOption.get();
            TemplateConfig templateConfig = templateService.syncGetTemplateConfig(masterIndexTemplatePhy.getCluster(),
                    masterIndexTemplatePhy.getName());
            if (Objects.isNull(templateConfig)){
                return Result.buildFail("无法获取源集群模版信息");
            }
            tgtTemplateParam.setMappings(templateConfig.getMappings().toJson().toJSONString());
            tgtTemplateParam.setSettings(JsonUtils.reFlat(templateConfig.getSetttings()).toJSONString());
        }

        Result<Long> addResult = addTemplateWithoutCheck(tgtTemplateParam);
        if (addResult.failed()) {
            return Result.buildFrom(addResult);
        }

        // 记录操作记录
        Integer projectIdByTemplateLogicId = indexTemplateService.getProjectIdByTemplateLogicId(logicId);
        operateRecordService.saveOperateRecordWithManualTrigger(
                String.format("复制【%s】物理模板至【%s】", indexTemplatePhy.getCluster(), param.getCluster()), operator,
                AuthConstant.SUPER_PROJECT_ID, indexTemplatePhy.getLogicId(), OperateTypeEnum.TEMPLATE_SERVICE, projectIdByTemplateLogicId);

        if (esTemplateService.syncCopyMappingAndAlias(indexTemplatePhy.getCluster(), indexTemplatePhy.getName(),
            tgtTemplateParam.getCluster(), tgtTemplateParam.getName(), 0)) {
            LOGGER.info(
                "class=TemplatePhyManagerImpl||methood=copyTemplate||TemplatePhysicalCopyDTO={}||msg=syncCopyMappingAndAlias succ",
                param);
        } else {
            LOGGER.warn(
                "class=TemplatePhyManagerImpl||methood=copyTemplate||TemplatePhysicalCopyDTO={}||msg=syncCopyMappingAndAlias fail",
                param);
        }

        return Result.buildSucWithTips("模板部署集群变更!");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editTemplate(IndexTemplatePhyDTO param, String operator) throws ESOperateException {
        Result<Void> checkResult = indexTemplatePhyService.validateTemplate(param, EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=TemplatePhyManagerImpl||method=editTemplate||msg={}",
                CHECK_FAIL_MSG + checkResult.getMessage());
            return checkResult;
        }

        IndexTemplatePhy oldIndexTemplatePhy = indexTemplatePhyService.getTemplateById(param.getId());
        Result<Void> result = editTemplateWithoutCheck(param, operator, 0);
        if (result.success()) {
            String editContent = AriusObjUtils.findChangedWithClear(oldIndexTemplatePhy, param);
            if (StringUtils.isNotBlank(editContent)) {
                Integer projectIdByTemplateLogicId = indexTemplateService.getProjectIdByTemplateLogicId(oldIndexTemplatePhy.getLogicId());
                operateRecordService.saveOperateRecordWithManualTrigger(
                        String.format("%s 变更:【%s】", TemplateOperateRecordEnum.CONFIG.getDesc(), editContent),
                        operator, AuthConstant.SUPER_PROJECT_ID, oldIndexTemplatePhy.getLogicId(),
                        OperateTypeEnum.TEMPLATE_SERVICE, projectIdByTemplateLogicId);

            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> editMultipleTemplate(List<IndexTemplatePhyDTO> params,
                                                String operator) throws ESOperateException {
        if (CollectionUtils.isEmpty(params)) {
            Result.buildFail("参数为空");
        }

        for (IndexTemplatePhyDTO param : params) {
            Result<Void> ret = editTemplate(param, operator);
            if (ret.failed()) {
                throw new ESOperateException(String.format("编辑模板:%s失败", param.getName()));
            }
        }

        return Result.buildSucc(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addTemplatesWithoutCheck(Integer logicId,
                                                 List<IndexTemplatePhyDTO> physicalInfos) throws AdminOperateException {
        for (IndexTemplatePhyDTO param : physicalInfos) {
            param.setLogicId(logicId);
            Result<Long> result = addTemplateWithoutCheck(param);
            if (result.failed()) {
                result.setMessage(result.getMessage() + "; 集群:" + param.getCluster() + ",模板:" + param.getName());
                return Result.buildFrom(result);
            }
        }
        return Result.buildSucc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> addTemplateWithoutCheck(IndexTemplatePhyDTO param) throws AdminOperateException {
        if (null != indexTemplatePhyService.getTemplateByClusterAndName(param.getCluster(), param.getName())) {
            return Result.buildParamIllegal("索引已经存在");
        }

        initParamWhenAdd(param);

        Result<Long> result = indexTemplatePhyService.insert(param);
        Long physicalId = result.getData();
        if (result.success()) {
            //删除数据库中历史的脏数据
            indexTemplatePhyService.deleteDirtyByClusterAndName(param.getCluster(), param.getName());

            //创建索引模板
            syncCreateIndexTemplateWithEs(param);

            SpringTool.publish(new PhysicalTemplateAddEvent(this, indexTemplatePhyService.getTemplateById(physicalId),
                buildIndexTemplateLogicWithPhysicalForNew(param)));
        }

        return Result.buildSucc(physicalId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> switchMasterSlave(Integer logicId, Long expectMasterPhysicalId, String operator) {
        List<IndexTemplatePhy> indexTemplatePhies = indexTemplatePhyService.getTemplateByLogicId(logicId);
        if (CollectionUtils.isEmpty(indexTemplatePhies)) {
            return Result.buildNotExist("模板不存在");
        }

        IndexTemplatePhy oldMaster = null;
        IndexTemplatePhy newMaster = null;

        for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhies) {
            if (indexTemplatePhy.getRole().equals(MASTER.getCode())) {
                if (oldMaster != null) {
                    LOGGER.error("class=TemplatePhyServiceImpl||method=switchMasterSlave||errMsg=no master||logicId={}",
                        logicId);
                }
                oldMaster = indexTemplatePhy;
            } else {
                if (expectMasterPhysicalId == null && newMaster == null) {
                    newMaster = indexTemplatePhy;
                }

                if (indexTemplatePhy.getId().equals(expectMasterPhysicalId)) {
                    newMaster = indexTemplatePhy;
                }
            }
        }

        if (newMaster == null) {
            return Result.buildNotExist("无法确定新的主");
        }

        boolean succ = true;

        if (oldMaster == null) {
            LOGGER.error("class=TemplatePhyServiceImpl||method=switchMasterSlave||errMsg=no master||logicId={}",
                logicId);
        } else {
            succ = indexTemplatePhyService.updateTemplateRole(oldMaster, SLAVE, operator).success();
        }

        succ = succ && (indexTemplatePhyService.updateTemplateRole(newMaster, MASTER, operator).success());

        return Result.build(succ);
    }

    @Override
    public Result<Void> editTemplateWithoutCheck(IndexTemplatePhyDTO param, String operator,
                                                 int retryCount) throws ESOperateException {
        IndexTemplatePhy oldIndexTemplatePhy = indexTemplatePhyService.getTemplateById(param.getId());
        //不需要shard比较
        

        boolean succ = indexTemplatePhyService.update(param).success();
        String tips = "";
        if (succ) {
            SpringTool.publish(
                new PhysicalTemplateModifyEvent(this, ConvertUtil.obj2Obj(oldIndexTemplatePhy, IndexTemplatePhy.class),
                    indexTemplatePhyService.getTemplateById(oldIndexTemplatePhy.getId()),
                    indexTemplateService.getLogicTemplateWithPhysicalsById(oldIndexTemplatePhy.getLogicId())));
        }

        return Result.buildWithTips(succ, tips);
    }

    @Override
    public Tuple</*存放冷存索引列表*/Set<String>, /*存放热存索引列表*/Set<String>> getHotAndColdIndexByBeforeDay(IndexTemplatePhyWithLogic physicalWithLogic,
                                                                                                 int days) {
        try {
            IndexTemplate logicTemplate = physicalWithLogic.getLogicTemplate();

            if (!physicalWithLogic.getExpression().endsWith("*")) {
                return new Tuple<>();
            }

            if (!TemplateUtils.isSaveByDay(logicTemplate.getDateFormat())
                && !TemplateUtils.isSaveByMonth(logicTemplate.getDateFormat())) {
                return new Tuple<>();
            }

            List<String> indices = indexTemplatePhyService.getMatchIndexNames(physicalWithLogic.getId());
            if (CollectionUtils.isEmpty(indices)) {
                LOGGER.info(
                    "class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||template={}||msg=no match indices",
                    logicTemplate.getName());
                return new Tuple<>();
            }

            return getHotAndColdIndexSet( days, logicTemplate, indices);
        } catch (Exception e) {
            LOGGER.warn("class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||templateName={}||errMsg={}",
                physicalWithLogic.getName(), e.getMessage(), e);
        }

        return new Tuple<>();
    }

    @Override
    public Set<String> getIndexByBeforeDay(IndexTemplatePhyWithLogic physicalWithLogic, int days) {
        try {
            IndexTemplate logicTemplate = physicalWithLogic.getLogicTemplate();

            if (!physicalWithLogic.getExpression().endsWith("*")) {
                return Sets.newHashSet();
            }

            if (!TemplateUtils.isSaveByDay(logicTemplate.getDateFormat())
                && !TemplateUtils.isSaveByMonth(logicTemplate.getDateFormat())) {
                return Sets.newHashSet();
            }

            List<String> indices = indexTemplatePhyService.getMatchIndexNames(physicalWithLogic.getId());
            if (CollectionUtils.isEmpty(indices)) {
                LOGGER.info(
                    "class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||template={}||msg=no match indices",
                    logicTemplate.getName());
                return Sets.newHashSet();
            }

            return getFinalIndexSet( days, logicTemplate, indices);
        } catch (Exception e) {
            LOGGER.warn("class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||templateName={}||errMsg={}",
                physicalWithLogic.getName(), e.getMessage(), e);
        }

        return Sets.newHashSet();
    }

    @Override
    public List<ConsoleTemplatePhyVO> getConsoleTemplatePhyVOS(IndexTemplatePhyDTO param, Integer projectId) {
        List<ConsoleTemplatePhyVO> consoleTemplatePhyVOS = ConvertUtil
            .list2List(indexTemplatePhyService.getByCondt(param), ConsoleTemplatePhyVO.class);

        buildConsoleTemplatePhyVO(consoleTemplatePhyVOS, projectId);

        return consoleTemplatePhyVOS;
    }

    @Override
    public List<String> getTemplatePhyNames(Integer projectId) {
        return getConsoleTemplatePhyVOS(null, projectId).parallelStream().map(ConsoleTemplatePhyVO::getName)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getCanCopyTemplatePhyClusterPhyNames(Long templatePhyId) {
        List<String> canCopyClusterPhyNames = Lists.newArrayList();
        IndexTemplatePhy templatePhy = indexTemplatePhyService.getTemplateById(templatePhyId);
        if (null != templatePhy && null != templatePhy.getCluster()) {
            clusterPhyService.listAllClusters().stream()
                .filter(clusterPhy -> !templatePhy.getCluster().equals(clusterPhy.getCluster()))
                .forEach(clusterPhy -> canCopyClusterPhyNames.add(clusterPhy.getCluster()));
        }

        return canCopyClusterPhyNames;
    }

    @Override
    public Result<List<IndexTemplatePhysicalVO>> getTemplatePhies(Integer logicId) {
        if (!indexTemplateService.exist(logicId)) {
            return Result.buildFail("模板Id不存在");
        }
        return Result.buildSucc(ConvertUtil.list2List(indexTemplatePhyService.getTemplateByLogicId(logicId),
            IndexTemplatePhysicalVO.class));
    }

    /**
     * @param regionId
     * @return
     */
    @Override
    public Result<List<IndexTemplatePhysicalVO>> listByRegionId(Integer regionId) {
        Result<List<IndexTemplatePhy>> ret = physicalService.listByRegionId(regionId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }
        return Result.buildSucc(ConvertUtil.list2List(ret.getData(), IndexTemplatePhysicalVO.class));
    }

    /**************************************** private method ****************************************************/
    private void initParamWhenAdd(IndexTemplatePhyDTO param) {
        IndexTemplate logic = indexTemplateService.getLogicTemplateById(param.getLogicId());

        if (param.getName() == null) {
            param.setName(logic.getName());
        }
        if (param.getExpression() == null) {
            param.setExpression(logic.getExpression());
        }
        if (param.getStatus() == null) {
            param.setStatus(TemplatePhysicalStatusEnum.NORMAL.getCode());
        }

        if (param.getRack() == null) {
            param.setRack("");
        }

        if (param.getVersion() == null) {
            param.setVersion(0);
        }

        if (param.getConfig() == null) {
            param.setConfig("");
        }
        if (param.getShardRouting() == null) {
            param.setShardRouting(1);
        }

        IndexTemplatePhysicalConfig indexTemplatePhysicalConfig = new IndexTemplatePhysicalConfig();
        if (StringUtils.isNotBlank(param.getConfig())) {
            indexTemplatePhysicalConfig = JSON.parseObject(param.getConfig(), IndexTemplatePhysicalConfig.class);
        }

        indexTemplatePhysicalConfig.setGroupId(param.getGroupId());
        indexTemplatePhysicalConfig.setDefaultWriterFlags(param.getDefaultWriterFlags());

        param.setConfig(JSON.toJSONString(indexTemplatePhysicalConfig));
    }

    private Result<Void> checkUpgradeParam(TemplatePhysicalUpgradeDTO param) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("模板升版本信息为空");
        }
        if (AriusObjUtils.isNull(param.getPhysicalId())) {
            return Result.buildParamIllegal(TEMPLATE_PHYSICAL_ID_IS_NULL);
        }
        if (AriusObjUtils.isNull(param.getVersion())) {
            return Result.buildParamIllegal("物理模板版本为空");
        }

        IndexTemplatePhy oldIndexTemplatePhy = indexTemplatePhyService.getTemplateById(param.getPhysicalId());
        if (oldIndexTemplatePhy == null) {
            return Result.buildNotExist(TEMPLATE_PHYSICAL_NOT_EXISTS);
        }
        if (Objects.equals(param.getVersion(), oldIndexTemplatePhy.getVersion())
            || (param.getVersion() > 0 && param.getVersion() < oldIndexTemplatePhy.getVersion())) {
            return Result.buildParamIllegal("物理模板版本非法");
        }
        if (param.getShard() != null && param.getShard() < MIN_SHARD_NUM) {
            return Result.buildParamIllegal("shard个数非法");
        }

        return Result.buildSucc();
    }

    private Result<Void> upgradeTemplateWithCheck(TemplatePhysicalUpgradeDTO param, String operator,
                                                  int retryCount) throws ESOperateException {
        IndexTemplatePhy indexTemplatePhy = indexTemplatePhyService.getTemplateById(param.getPhysicalId());
       

        IndexTemplate logic = indexTemplateService.getLogicTemplateById(indexTemplatePhy.getLogicId());
        LOGGER.info("class=TemplatePhyManagerImpl||method=upgradeTemplateWithCheck||name={}||shard={}||version={}",
            logic.getName(), param.getShard(), param.getVersion());

        IndexTemplatePhyDTO updateParam = new IndexTemplatePhyDTO();
        updateParam.setId(indexTemplatePhy.getId());
        updateParam.setVersion(param.getVersion());
        /*
        这里提前创建当天索引
          1.避免因为getTemplateConfig失败，导致升版本后不分区索引mapping异常
          2.避免由于事务原因，导致当天最新版本的分区索引未被创建
        */
        if (!preCreateManager.syncCreateTodayIndexByPhysicalId(updateParam.getId(), updateParam.getVersion())) {
            return Result.buildFail("创建当前最新版本索引失败，请稍后重试！");
        }

        Result<Void> editResult = editTemplateWithoutCheck(updateParam, operator, retryCount);

        if (editResult.failed()) {
            return editResult;
        }

        preCreateManager.asyncCreateTodayAndTomorrowIndexByPhysicalId(indexTemplatePhy.getId());

        return Result.buildSucc();
    }

    private Result<Void> checkCopyParam(TemplatePhysicalCopyDTO param) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("复制参数为空");
        }
        if (AriusObjUtils.isNull(param.getPhysicalId())) {
            return Result.buildParamIllegal(TEMPLATE_PHYSICAL_ID_IS_NULL);
        }
        if (AriusObjUtils.isNull(param.getCluster())) {
            return Result.buildParamIllegal("目标集群为空");
        }
        if (AriusObjUtils.isNull(param.getShard())) {
            return Result.buildParamIllegal("shard为空");
        }

        IndexTemplatePhy oldIndexTemplatePhy = indexTemplatePhyService.getTemplateById(param.getPhysicalId());
        if (oldIndexTemplatePhy == null) {
            return Result.buildNotExist(TEMPLATE_PHYSICAL_NOT_EXISTS);
        }

        if (!clusterPhyService.isClusterExists(param.getCluster())) {
            return Result.buildNotExist("目标集群不存在");
        }

        if (oldIndexTemplatePhy.getCluster().equals(param.getCluster())) {
            return Result.buildParamIllegal("目标集群不能与源集群相同");
        }

        if (param.getShard() < 1) {
            return Result.buildParamIllegal("shard非法");
        }

        return Result.buildSucc();
    }

    private Result<Void> checkMetaInner(IndexTemplatePhy templatePhysical,
                                        Map<Integer, IndexTemplate> logicId2IndexTemplateLogicMap,
                                        Set<String> esClusters) {
        List<String> errMsgs = Lists.newArrayList();

        if (!esClusters.contains(templatePhysical.getCluster())) {
            errMsgs.add("物理集群不存在：" + templatePhysical.getName() + "(" + templatePhysical.getId() + ")");
        }

        if (!logicId2IndexTemplateLogicMap.containsKey(templatePhysical.getLogicId())) {
            errMsgs.add("逻辑模板不存在：" + templatePhysical.getName() + "(" + templatePhysical.getId() + ")");
        }
    
        TemplateConfig templateConfig = null;
        try {
            templateConfig = esTemplateService.syncGetTemplateConfig(templatePhysical.getCluster(),
                    templatePhysical.getName());
        } catch (ESOperateException e) {
            errMsgs.add(String.format("获取 templateConfig 失败:%s", e.getMessage()));
        }
    
        if (templateConfig == null) {
            errMsgs.add("es模板不存在：" + templatePhysical.getName() + "(" + templatePhysical.getId() + ")");
        }

        if (CollectionUtils.isEmpty(errMsgs)) {
            return Result.buildSucc();
        }

        return Result.build(ResultType.ADMIN_META_ERROR.getCode(), String.join(",", errMsgs));

    }

    private int checkIndexCreateAndExpire(IndexTemplatePhy templatePhysical,
                                          Map<Integer, IndexTemplate> logicId2IndexTemplateLogicMap) {
        int result = INDEX_OP_OK;
        if (templatePhysical.getCreateTime().before(AriusDateUtils.getZeroDate())) {
            Set<String> indices = Sets
                .newHashSet(indexTemplatePhyService.getMatchNoVersionIndexNames(templatePhysical.getId()));

            IndexTemplate templateLogic = logicId2IndexTemplateLogicMap.get(templatePhysical.getLogicId());
            String tomorrowIndexName = IndexNameFactory.getNoVersion(templateLogic.getExpression(),
                templateLogic.getDateFormat(), 1);
            String expireIndexName = IndexNameFactory.getNoVersion(templateLogic.getExpression(),
                templateLogic.getDateFormat(), -1 * templateLogic.getExpireTime());

            if (!indices.contains(tomorrowIndexName)) {
                LOGGER.warn(
                    "class=TemplatePhyManagerImpl||method=checkIndexCreateAndExpire||cluster={}||template={}||msg=TOMORROW_INDEX_NOT_CREATE",
                    templatePhysical.getCluster(), templatePhysical.getName());
                result = result + TOMORROW_INDEX_NOT_CREATE;
            }

            if (TemplateUtils.isSaveByDay(templateLogic.getDateFormat()) && indices.contains(expireIndexName)) {
                LOGGER.warn(
                    "class=TemplatePhyManagerImpl||method=checkIndexCreateAndExpire||cluster={}||template={}||msg=EXPIRE_INDEX_NOT_DELETE",
                    templatePhysical.getCluster(), templatePhysical.getName());
                result = result + EXPIRE_INDEX_NOT_DELETE;
            }
        }
        return result;
    }

    private IndexTemplateWithPhyTemplates buildIndexTemplateLogicWithPhysicalForNew(IndexTemplatePhyDTO param) {
        IndexTemplateWithPhyTemplates logicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(param.getLogicId());
        if (CollectionUtils.isNotEmpty(param.getPhysicalInfos())) {
            List<IndexTemplatePhy> physicals = ConvertUtil.list2List(param.getPhysicalInfos(), IndexTemplatePhy.class);
            logicWithPhysical.setPhysicals(physicals);
        }
        return logicWithPhysical;
    }

    /**
     * 判定是否是合法的shard number.
     *
     * @param shardNum
     * @return
     */
    private boolean isValidShardNum(Integer shardNum) {
        return (shardNum != null && shardNum > 0);
    }

    private void buildConsoleTemplatePhyVO(List<ConsoleTemplatePhyVO> params, Integer currentProjectId) {

        Map<Integer, String> projectId2ProjectNameMap = Maps.newHashMap();

        for (ConsoleTemplatePhyVO consoleTemplatePhyVO : params) {

            IndexTemplate logicTemplate = indexTemplateService.getLogicTemplateById(consoleTemplatePhyVO.getLogicId());
            if (AriusObjUtils.isNull(logicTemplate)) {
                LOGGER.error(
                    "class=TemplatePhyServiceImpl||method=buildConsoleTemplatePhyVO||errMsg=IndexTemplateLogic is empty||logicId={}",
                    consoleTemplatePhyVO.getLogicId());
                continue;
            }

            handleIndexTemplateLogic(currentProjectId, projectId2ProjectNameMap, consoleTemplatePhyVO, logicTemplate);

        }
    }

    private void handleIndexTemplateLogic(Integer currentProjectId, Map<Integer, String> projectId2ProjectNameMap,
                                          ConsoleTemplatePhyVO consoleTemplatePhyVO, IndexTemplate logicTemplate) {
        //设置归属项目信息
        Integer projectIdFromLogicTemplate = logicTemplate.getProjectId();
        if (!AriusObjUtils.isNull(projectIdFromLogicTemplate)) {
            consoleTemplatePhyVO.setProjectId(projectIdFromLogicTemplate);

            if (projectId2ProjectNameMap.containsKey(projectIdFromLogicTemplate)) {
                consoleTemplatePhyVO.setProjectName(projectId2ProjectNameMap.get(logicTemplate.getProjectId()));
            } else {
                String projectName = Optional
                    .ofNullable(projectService.getProjectBriefByProjectId(logicTemplate.getProjectId()))
                    .map(ProjectBriefVO::getProjectName).orElse(null);

                if (!AriusObjUtils.isNull(projectName)) {
                    consoleTemplatePhyVO.setProjectName(projectName);
                    projectId2ProjectNameMap.put(projectIdFromLogicTemplate, projectName);
                }
            }
        }

        //设置逻辑模板名称
        consoleTemplatePhyVO.setLogicName(logicTemplate.getName());

        //设置描述信息, 是否要加一列描述信息
        consoleTemplatePhyVO.setMemo(logicTemplate.getDesc());

        //设置权限
        if (AriusObjUtils.isNull(currentProjectId)) {
            consoleTemplatePhyVO.setAuthType(ProjectTemplateAuthEnum.NO_PERMISSION.getCode());
            return;
        }
        if (currentProjectId.equals(projectIdFromLogicTemplate)) {
            consoleTemplatePhyVO.setAuthType(ProjectTemplateAuthEnum.OWN.getCode());
        } else {
            ProjectTemplateAuthEnum authEnum = projectLogicTemplateAuthService
                .getAuthEnumByProjectIdAndLogicId(currentProjectId, projectIdFromLogicTemplate);
            consoleTemplatePhyVO.setAuthType(authEnum.getCode());
        }
    }

    private Set<String> getFinalIndexSet( int days,
                                         IndexTemplate logicTemplate, List<String> indices) {
        Set<String> finalIndexSet = Sets.newHashSet();
        for (String indexName : indices) {
            if (StringUtils.isBlank(indexName)) {
                continue;
            }

            Date indexTime = IndexNameFactory.genIndexTimeByIndexName(
                genIndexNameClear(indexName, logicTemplate.getExpression()), logicTemplate.getExpression(),
                logicTemplate.getDateFormat());

            if (indexTime == null) {
                LOGGER.warn(
                    "class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||template={}||indexName={}||msg=template parse index time fail",
                    logicTemplate.getName(), indexName);
                continue;
            }

            if (TemplateUtils.isSaveByMonth(logicTemplate.getDateFormat())) {
                // 需要将索引时间定为当月的最后一天 确保最后一天的数据能被保留到保存时长
                indexTime = AriusDateUtils.getLastDayOfTheMonth(indexTime);
            }

            long timeIntervalDay = (System.currentTimeMillis() - indexTime.getTime()) / MILLIS_PER_DAY;
            if (timeIntervalDay < days) {
                LOGGER.info(
                    "class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||template={}||indexName={}||timeIntervalDay={}||msg=index not match",
                    logicTemplate.getName(), indexName, timeIntervalDay);
                continue;
            }

            LOGGER.info(
                "class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||indexName={}||indexTime={}||timeIntervalDay={}",
                indexName, indexTime, timeIntervalDay);

            finalIndexSet.add(indexName);
        }
        return finalIndexSet;
    }

    private void syncCreateIndexTemplateWithEs(IndexTemplatePhyDTO param) throws AdminOperateException {
        IndexTemplate logic = indexTemplateService.getLogicTemplateById(param.getLogicId());
        MappingConfig mappings = null;
        if (StringUtils.isNotBlank(param.getMappings())) {
            Result result = AriusIndexMappingConfigUtils.parseMappingConfig(param.getMappings());
            if (result.success()) {
                mappings = (MappingConfig) result.getData();
            }
        }
        Map<String, String> settingsMap = getSettingsMap(param.getShard(), param.getRegionId(), param.getSettings());
        boolean ret;
        if (null != mappings || MapUtils.isNotEmpty(settingsMap)) {
            ret = esTemplateService.syncCreate(settingsMap, param.getCluster(), param.getName(), logic.getExpression(),
                mappings, 0);
        } else {
            ret = esTemplateService.syncCreate(param.getCluster(), param.getName(), logic.getExpression(),
                param.getShard(), param.getShardRouting(), 0);
        }
        if (!ret) {
            throw new ESOperateException("failed to create template!");
        }
    }

    private Map<String, String> getSettingsMap(Integer shard, Integer regionId,
                                               String settings) throws AdminOperateException {
        Map<String, String> settingsMap = new HashMap<>();
        if (null != shard && shard > 0) {
            settingsMap.put(INDEX_SHARD_NUM, String.valueOf(shard));
        }
        if (null != settings) {
            settingsMap.putAll(AriusIndexTemplateSetting.flat(JSON.parseObject(settings)));
        }
        if (null != regionId) {
            Result<List<ClusterRoleHost>> roleHostResult = clusterRoleHostService.listByRegionId(regionId);
            if (roleHostResult.failed()) {
                throw new AdminOperateException(String.format("获取region[%d]节点列表异常", regionId));
            }
            List<ClusterRoleHost> data = roleHostResult.getData();
            if (CollectionUtils.isEmpty(data)) {
                throw new AdminOperateException(String.format("获取region[%d]节点列表为空, 请检查region中是否存在数据节点", regionId));
            }
            // 判断region的划分方式，根据划分方式配置settings
            ClusterRegion region = clusterRegionService.getRegionById(regionId.longValue());
            if(StringUtils.isBlank(region.getDivideAttributeKey())) {
                buildSettingMapByDefault(settingsMap, data);
            }else {
                buildSettingMapByAttribute(settingsMap, data, region.getDivideAttributeKey());
            }
        }
        return settingsMap;
    }

    private void buildSettingMapByDefault(Map<String, String> settingsMap, List<ClusterRoleHost> data) {
        List<String> nodeNames = data.stream().map(ClusterRoleHost::getNodeSet)
                .filter(nodeName -> !AriusObjUtils.isBlank(nodeName)).distinct().collect(Collectors.toList());
        settingsMap.put(TEMPLATE_INDEX_INCLUDE_NODE_NAME, String.join(",", nodeNames));
    }

    private void buildSettingMapByAttribute(Map<String, String> settingsMap, List<ClusterRoleHost> data, String divideAttributeKey) {
        // 构建attribute属性信息（根据划分方式attribute的属性构建）
        Set<String> attributeValueSet = Sets.newHashSet();
        List<String> attributesList = data.stream().map(ClusterRoleHost::getAttributes)
                .filter(attributes -> !AriusObjUtils.isBlank(attributes)).distinct().collect(Collectors.toList());
        for (String attributes : attributesList) {
            Map<String, String> attributeMap = ConvertUtil.str2Map(attributes);
            attributeValueSet.add(attributeMap.get(divideAttributeKey));
        }
        settingsMap.put(ESOperateConstant.TEMPLATE_INDEX_INCLUDE_ATTRIBUTE_PREFIX + divideAttributeKey,
                String.join(",", attributeValueSet));
    }

    private Tuple</*存放冷存索引列表*/Set<String>, /*存放热存索引列表*/Set<String>> getHotAndColdIndexSet(
                                                                                          int days,
                                                                                          IndexTemplate logicTemplate,
                                                                                          List<String> indices) {
        Set<String> finalColdIndexSet = Sets.newHashSet();
        Set<String> finalHotIndexSet = Sets.newHashSet();
        for (String indexName : indices) {
            if (StringUtils.isBlank(indexName)) {
                continue;
            }

            Date indexTime = IndexNameFactory.genIndexTimeByIndexName(
                genIndexNameClear(indexName, logicTemplate.getExpression()), logicTemplate.getExpression(),
                logicTemplate.getDateFormat());

            if (indexTime == null) {
                LOGGER.warn(
                    "class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||template={}||indexName={}||msg=template parse index time fail",
                    logicTemplate.getName(), indexName);
                continue;
            }

            if (TemplateUtils.isSaveByMonth(logicTemplate.getDateFormat())) {
                // 需要将索引时间定为当月的最后一天 确保最后一天的数据能被保留到保存时长
                indexTime = AriusDateUtils.getLastDayOfTheMonth(indexTime);
            }

            long timeIntervalDay = (System.currentTimeMillis() - indexTime.getTime()) / MILLIS_PER_DAY;
            if (timeIntervalDay < days) {
                LOGGER.info(
                    "class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||template={}||indexName={}||timeIntervalDay={}||msg=index not match",
                    logicTemplate.getName(), indexName, timeIntervalDay);
                finalHotIndexSet.add(indexName);
                continue;
            }

            LOGGER.info(
                "class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||indexName={}||indexTime={}||timeIntervalDay={}",
                indexName, indexTime, timeIntervalDay);

            finalColdIndexSet.add(indexName);
        }
        return new Tuple<>(finalColdIndexSet, finalHotIndexSet);
    }
}