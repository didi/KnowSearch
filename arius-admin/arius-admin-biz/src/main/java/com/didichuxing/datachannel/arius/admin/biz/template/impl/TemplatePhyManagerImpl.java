package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.capacityplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.TemplatePreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhysicalInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalCopyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalUpgradeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplatePhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template.TemplateOperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfoWithLogic;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.impl.TemplatePhyServiceImpl;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.COPY;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum.MASTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum.SLAVE;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.MILLIS_PER_DAY;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory.genIndexNameClear;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.*;

@Component
public class TemplatePhyManagerImpl implements TemplatePhyManager {

    private static final ILog           LOGGER                    = LogFactory.getLog(TemplatePhyServiceImpl.class);

    public static final Integer         NOT_CHECK                 = -100;
    private static final Integer        INDEX_OP_OK               = 0;
    private static final Integer        TOMORROW_INDEX_NOT_CREATE = 1;
    private static final Integer        EXPIRE_INDEX_NOT_DELETE   = 2;
    private static final Integer        INDEX_ALL_ERR             = TOMORROW_INDEX_NOT_CREATE + EXPIRE_INDEX_NOT_DELETE;

    private static final String TEMPLATE_PHYSICAL_ID_IS_NULL = "物理模板id为空";

    private static final String TEMPLATE_PHYSICAL_NOT_EXISTS = "物理模板不存在";

    private static final String CHECK_FAIL_MSG = "check fail||msg={}";

    public static final int MIN_SHARD_NUM = 1;

    @Autowired
    private OperateRecordService        operateRecordService;

    @Autowired
    private ClusterPhyService           clusterPhyService;

    @Autowired
    private TemplateLabelService        templateLabelService;

    @Autowired
    private ESTemplateService           esTemplateService;

    @Autowired
    private TemplatePreCreateManager    templatePreCreateManager;

    @Autowired
    private IndexPlanManager indexPlanManager;

    @Autowired
    private RegionRackService           regionRackService;

    @Autowired
    private IndexTemplateInfoService indexTemplateInfoService;

    @Autowired
    private TemplatePhyService          templatePhyService;

    @Autowired
    private AriusConfigInfoService      ariusConfigInfoService;

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private AppService                  appService;

    @Override
    public boolean checkMeta() {
        List<IndexTemplatePhyInfo> templatePhysicals = templatePhyService.listTemplate();

        List<IndexTemplateInfo> templateLogics = indexTemplateInfoService.getAllLogicTemplates();
        Map<Integer, IndexTemplateInfo> logicId2IndexTemplateLogicMap = ConvertUtil.list2Map(templateLogics,
                IndexTemplateInfo::getId);

        Multimap<String, IndexTemplatePhyInfo> cluster2IndexTemplatePhysicalMultiMap = ConvertUtil
                .list2MulMap(templatePhysicals, IndexTemplatePhyInfo::getCluster);

        Set<String> esClusters = clusterPhyService.listAllClusters().stream().map( ClusterPhy::getCluster)
                .collect( Collectors.toSet());

        for (String cluster : cluster2IndexTemplatePhysicalMultiMap.keySet()) {
            int tomorrowIndexNotCreateCount = 0;
            int expireIndexNotDeleteCount = 0;

            Collection<IndexTemplatePhyInfo> clusterTemplates = cluster2IndexTemplatePhysicalMultiMap.get(cluster);

            for (IndexTemplatePhyInfo templatePhysical : clusterTemplates) {
                try {
                    Result<Void> result = checkMetaInner(templatePhysical, logicId2IndexTemplateLogicMap, esClusters);
                    if (result.success()) {
                        LOGGER.info("class=TemplatePhyManagerImpl||method=metaCheck||msg=succ||physicalId={}", templatePhysical.getId());
                    } else {
                        LOGGER.warn("class=TemplatePhyManagerImpl||method=metaCheck||msg=fail||physicalId={}||failMsg={}", templatePhysical.getId(),
                                result.getMessage());
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
    public void syncMeta(Long physicalId, int retryCount) throws ESOperateException {

        // 从数据库获取物理模板
        IndexTemplatePhyInfo indexTemplatePhyInfo = templatePhyService.getTemplateById(physicalId);
        if (indexTemplatePhyInfo == null) {
            return;
        }

        // 从ES集群获取模板配置
        TemplateConfig templateConfig = esTemplateService.syncGetTemplateConfig(indexTemplatePhyInfo.getCluster(),
                indexTemplatePhyInfo.getName());

        if (templateConfig == null) {
            // es集群中还没有模板，创建
            esTemplateService.syncCreate(indexTemplatePhyInfo.getCluster(), indexTemplatePhyInfo.getName(), indexTemplatePhyInfo.getExpression(),
                    indexTemplatePhyInfo.getRack(), indexTemplatePhyInfo.getShard(), indexTemplatePhyInfo.getShardRouting(), retryCount);

        } else {
            // 校验表达式
            if (
                    !indexTemplatePhyInfo.getExpression().equals(templateConfig.getTemplate()) &&
                            esTemplateService.syncUpdateExpression(indexTemplatePhyInfo.getCluster(), indexTemplatePhyInfo.getName(),
                                    indexTemplatePhyInfo.getExpression(), retryCount)
            ) {
                // 表达式不同（表达式发生变化），同步到ES集群
                LOGGER.info("class=TemplatePhyManagerImpl||method=syncMeta||msg=syncUpdateExpression succ||template={}||srcExp={}||tgtExp={}",
                        indexTemplatePhyInfo.getName(), templateConfig.getTemplate(), indexTemplatePhyInfo.getExpression());
            }

            // 标志shard或rack是否需要修改
            boolean editShardOrRack = false;
            Map<String, String> settings = templateConfig.getSetttings();
            String rack = settings.get(TEMPLATE_INDEX_INCLUDE_RACK);
            String shardNum = settings.get(INDEX_SHARD_NUM);

            // 校验shard个数
            if (!String.valueOf(indexTemplatePhyInfo.getShard()).equals(shardNum)) {
                editShardOrRack = true;
                shardNum = String.valueOf(indexTemplatePhyInfo.getShard());
            }

            // 校验rack
            if (
                    StringUtils.isNotBlank(indexTemplatePhyInfo.getRack()) &&
                            (!settings.containsKey(TEMPLATE_INDEX_INCLUDE_RACK)
                                    || !indexTemplatePhyInfo.getRack().equals(settings.get(TEMPLATE_INDEX_INCLUDE_RACK)))
            ) {
                editShardOrRack = true;
                rack = indexTemplatePhyInfo.getRack();
            }

            if (editShardOrRack && esTemplateService.syncUpdateRackAndShard(indexTemplatePhyInfo.getCluster(), indexTemplatePhyInfo.getName(), rack,
                    Integer.valueOf(shardNum), indexTemplatePhyInfo.getShardRouting(), retryCount)) {
                // 同步变化到ES集群
                    LOGGER.info(
                            "class=TemplatePhyManagerImpl||method=syncMeta||msg=syncUpdateRackAndShard succ||template={}||srcRack={}||srcShard={}||tgtRack={}||tgtShard={}",
                            indexTemplatePhyInfo.getName(), settings.get(TEMPLATE_INDEX_INCLUDE_RACK), settings.get(INDEX_SHARD_NUM),
                            rack, shardNum);
            }
        }
    }

    @Override
    public Result<Void> delTemplate(Long physicalId, String operator) throws ESOperateException {
        return templatePhyService.delTemplate(physicalId, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delTemplateByLogicId(Integer logicId, String operator) throws ESOperateException {
        List<IndexTemplatePhyInfo> indexTemplatePhyInfos = templatePhyService.getTemplateByLogicId(logicId);

        boolean succ = true;
        if (CollectionUtils.isEmpty(indexTemplatePhyInfos)) {
            LOGGER.info("class=TemplatePhyManagerImpl||method=delTemplateByLogicId||logicId={}||msg=template no physical info!", logicId);
        } else {
            LOGGER.info("class=TemplatePhyManagerImpl||method=delTemplateByLogicId||logicId={}||physicalSize={}||msg=template has physical info!",
                    logicId, indexTemplatePhyInfos.size());
            for (IndexTemplatePhyInfo indexTemplatePhyInfo : indexTemplatePhyInfos) {
                if (delTemplate(indexTemplatePhyInfo.getId(), operator).failed()) {
                    succ = false;
                }

            }
        }

        return Result.build(succ);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> upgradeTemplate(TemplatePhysicalUpgradeDTO param, String operator) throws ESOperateException {
        Result<Void> checkResult = checkUpgradeParam(param);
        if (checkResult.failed()) {
            LOGGER.warn("class=TemplatePhyManagerImpl||method=upgradeTemplate||msg={}", CHECK_FAIL_MSG + checkResult.getMessage());
            return checkResult;
        } else {
            operateRecordService.save(TEMPLATE, EDIT, param.getLogicId(), JSON.toJSONString(new TemplateOperateRecord(TemplateOperateRecordEnum.UPGRADE.getCode(),
                    "模板版本升级为：" + param.getVersion())), operator);
        }

        return upgradeTemplateWithCheck(param, operator, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> rolloverUpgradeTemplate(TemplatePhysicalUpgradeDTO param, String operator) throws ESOperateException {
        //rollover 生版本号不需要对参数进行校验
        return upgradeTemplateWithCheck(param, operator, 0);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> upgradeMultipleTemplate(List<TemplatePhysicalUpgradeDTO> params,
                                                   String operator) throws ESOperateException {
        if (CollectionUtils.isEmpty(params)) {
            Result.buildFail("参数为空");
        }

        for (TemplatePhysicalUpgradeDTO param : params) {
            Result<Void> ret = upgradeTemplate(param, operator);
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
            LOGGER.warn("class=TemplatePhyManagerImpl||method=copyTemplate||msg={}", CHECK_FAIL_MSG + checkResult.getMessage());
            return checkResult;
        }

        IndexTemplatePhyInfo indexTemplatePhyInfo = templatePhyService.getTemplateById(param.getPhysicalId());
        IndexTemplatePhysicalInfoDTO tgtTemplateParam = ConvertUtil.obj2Obj(indexTemplatePhyInfo, IndexTemplatePhysicalInfoDTO.class);
        tgtTemplateParam.setCluster(param.getCluster());
        tgtTemplateParam.setRack(param.getRack());
        tgtTemplateParam.setRole(SLAVE.getCode());
        tgtTemplateParam.setShard(param.getShard());
        tgtTemplateParam.setVersion(indexTemplatePhyInfo.getVersion());

        Result<Long> addResult = addTemplateWithoutCheck(tgtTemplateParam);
        if (addResult.failed()) {
            return Result.buildFrom(addResult);
        }

        // 记录操作记录
        operateRecordService.save(TEMPLATE, COPY, indexTemplatePhyInfo.getLogicId(),
                String.format("复制【%s】物理模板至【%s】", indexTemplatePhyInfo.getCluster(), param.getCluster()), operator);

        if (esTemplateService.syncCopyMappingAndAlias(indexTemplatePhyInfo.getCluster(), indexTemplatePhyInfo.getName(),
                tgtTemplateParam.getCluster(), tgtTemplateParam.getName(), 0)) {
            LOGGER.info("class=TemplatePhyManagerImpl||methood=copyTemplate||TemplatePhysicalCopyDTO={}||msg=syncCopyMappingAndAlias succ", param);
        } else {
            LOGGER.warn("class=TemplatePhyManagerImpl||methood=copyTemplate||TemplatePhysicalCopyDTO={}||msg=syncCopyMappingAndAlias fail", param);
        }

        return Result.buildSucWithTips("模板部署集群变更!请注意模板APP是否可以使用修改后的集群rack\n模板复制后请确认逻辑模板quota是否充足");
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editTemplate(IndexTemplatePhysicalInfoDTO param, String operator) throws ESOperateException {
        Result<Void> checkResult = validateTemplate(param, EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=TemplatePhyManagerImpl||method=editTemplate||msg={}", CHECK_FAIL_MSG + checkResult.getMessage());
            return checkResult;
        }

        IndexTemplatePhyInfo oldIndexTemplatePhyInfo = templatePhyService.getTemplateById(param.getId());
        Result<Void> result = editTemplateWithoutCheck(param, operator, 0);
        if (result.success()) {
            String editContent = AriusObjUtils.findChangedWithClear(oldIndexTemplatePhyInfo, param);
            if (StringUtils.isNotBlank(editContent)) {
                operateRecordService.save(TEMPLATE, EDIT, param.getLogicId(),
                        JSON.toJSONString(new TemplateOperateRecord(TemplateOperateRecordEnum.CONFIG.getCode(), editContent)), operator);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> editMultipleTemplate(List<IndexTemplatePhysicalInfoDTO> params,
                                                String operator) throws ESOperateException {
        if (CollectionUtils.isEmpty(params)) {
            Result.buildFail("参数为空");
        }

        for (IndexTemplatePhysicalInfoDTO param : params) {
            Result<Void> ret = editTemplate(param, operator);
            if (ret.failed()) {
                throw new ESOperateException(String.format("编辑模板:%s失败", param.getName()));
            }
        }

        return Result.buildSucc(true);
    }

    @Override
    public Result<Void> validateTemplate(IndexTemplatePhysicalInfoDTO param, OperationEnum operation) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("物理模板参数为空");
        }
        if (operation == OperationEnum.ADD) {
            Result<Void> result = handleValidateTemplateAdd(param);
            if (result.failed()) {return result;}
        } else if (operation == EDIT) {
            Result<Void> result = handleValidateTemplateEdit(param);
            if (result.failed()) {return result;}
        }

        Result<Void> result = handleValidateTemplate(param);
        if (result.failed()) {return result;}

        return Result.buildSucc();
    }

    @Override
    public Result<Void> validateTemplates(List<IndexTemplatePhysicalInfoDTO> params, OperationEnum operation) {
        if (AriusObjUtils.isNull(params)) {
            return Result.buildParamIllegal("物理模板信息为空");
        }

        Set<String> deployClusterSet = Sets.newTreeSet();
        for (IndexTemplatePhysicalInfoDTO param : params) {
            Result<Void> checkResult = validateTemplate(param, operation);
            if (checkResult.failed()) {
                LOGGER.warn("class=TemplatePhyManagerImpl||method=validateTemplates||msg={}", CHECK_FAIL_MSG + checkResult.getMessage());
                checkResult
                        .setMessage(checkResult.getMessage() + "; 集群:" + param.getCluster() + ",模板:" + param.getName());
                return checkResult;
            }

            if (deployClusterSet.contains(param.getCluster())) {
                return Result.buildParamIllegal("部署集群重复");
            } else {
                deployClusterSet.add(param.getCluster());
            }

        }

        return Result.buildSucc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addTemplatesWithoutCheck(Integer logicId,
                                           List<IndexTemplatePhysicalInfoDTO> physicalInfos) throws AdminOperateException {
        for (IndexTemplatePhysicalInfoDTO param : physicalInfos) {
            param.setLogicId(logicId);
            param.setPhysicalInfos(physicalInfos);
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
    public Result<Long> addTemplateWithoutCheck(IndexTemplatePhysicalInfoDTO param) throws AdminOperateException {
        if (null != templatePhyService.getTemplateByClusterAndName(param.getCluster(), param.getName())) {
            return Result.buildParamIllegal("索引已经存在");
        }

        initParamWhenAdd(param);

        // 为了解决写入的长尾问题，引擎增加了逻辑shard的概念，这里需要计算逻辑shard的值，并调整源shard个数
        indexPlanManager.initShardRoutingAndAdjustShard(param);
        Result<Long> result = templatePhyService.insert(param);
        Long physicalId = result.getData();
        if (result.success()) {
            //删除数据库中历史的脏数据
            templatePhyService.deleteDirtyByClusterAndName(param.getCluster(), param.getName());

            //创建索引模板
            syncCreateIndexTemplateWithEs(param);

            SpringTool.publish(new PhysicalTemplateAddEvent(this, templatePhyService.getTemplateById(physicalId),
                    buildIndexTemplateLogicWithPhysicalForNew(param)));
        }

        return Result.buildSucc(physicalId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editTemplateFromLogic(IndexTemplateInfoDTO param, String operator) throws ESOperateException {
        List<IndexTemplatePhyInfo> indexTemplatePhyInfos = templatePhyService.getTemplateByLogicId(param.getId());
        if (CollectionUtils.isEmpty(indexTemplatePhyInfos)) {
            return Result.buildSucc();
        }

        for (IndexTemplatePhyInfo indexTemplatePhyInfo : indexTemplatePhyInfos) {
            if (AriusObjUtils.isChanged(param.getExpression(), indexTemplatePhyInfo.getExpression())) {
                Result<Void> result = templatePhyService.updateTemplateExpression(indexTemplatePhyInfo, param.getExpression(), operator);
                if (result.failed()) {
                    return result;
                }
            }

            if (isValidShardNum(param.getShardNum())
                    && AriusObjUtils.isChanged(param.getShardNum(), indexTemplatePhyInfo.getShard())) {
                Result<Void> result = templatePhyService.updateTemplateShardNum(indexTemplatePhyInfo, param.getShardNum(), operator);
                if (result.failed()) {
                    return result;
                }
            }
        }

        return Result.buildSucc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> switchMasterSlave(Integer logicId, Long expectMasterPhysicalId, String operator) {
        List<IndexTemplatePhyInfo> indexTemplatePhyInfos = templatePhyService.getTemplateByLogicId(logicId);
        if (CollectionUtils.isEmpty(indexTemplatePhyInfos)) {
            return Result.buildNotExist("模板不存在");
        }

        IndexTemplatePhyInfo oldMaster = null;
        IndexTemplatePhyInfo newMaster = null;

        for (IndexTemplatePhyInfo indexTemplatePhyInfo : indexTemplatePhyInfos) {
            if (indexTemplatePhyInfo.getRole().equals(MASTER.getCode())) {
                if (oldMaster != null) {
                    LOGGER.error("class=TemplatePhyServiceImpl||method=switchMasterSlave||errMsg=no master||logicId={}", logicId);
                }
                oldMaster = indexTemplatePhyInfo;
            } else {
                if (expectMasterPhysicalId == null && newMaster == null) {
                    newMaster = indexTemplatePhyInfo;
                }

                if (indexTemplatePhyInfo.getId().equals(expectMasterPhysicalId)) {
                    newMaster = indexTemplatePhyInfo;
                }
            }
        }

        if (newMaster == null) {
            return Result.buildNotExist("无法确定新的主");
        }

        boolean succ = true;

        if (oldMaster == null) {
            LOGGER.error("class=TemplatePhyServiceImpl||method=switchMasterSlave||errMsg=no master||logicId={}", logicId);
        } else {
            succ = templatePhyService.updateTemplateRole(oldMaster,SLAVE,operator).success();
        }

        succ = succ && (templatePhyService.updateTemplateRole(newMaster,MASTER,operator).success());


        return Result.build(succ);
    }

    @Override
    public Result<Void> editTemplateRackWithoutCheck(Long physicalId, String tgtRack, String operator,
                                               int retryCount) throws ESOperateException {
        IndexTemplatePhysicalInfoDTO updateParam = new IndexTemplatePhysicalInfoDTO();
        updateParam.setId(physicalId);
        updateParam.setRack(tgtRack);
        return editTemplateWithoutCheck(updateParam, operator, retryCount);
    }

    @Override
    public Result<Void> upgradeTemplateVersion(Long physicalId, String operator, int retryCount) throws ESOperateException {
        IndexTemplatePhyInfo indexTemplatePhyInfo = templatePhyService.getTemplateById(physicalId);
        if (indexTemplatePhyInfo == null) {
            return Result.buildNotExist("模板不存在");
        }

        int version = indexTemplatePhyInfo.getVersion() + 1;
        if (version > 9) {
            version = 0;
        }

        IndexTemplatePhysicalInfoDTO updateParam = new IndexTemplatePhysicalInfoDTO();
        updateParam.setId(indexTemplatePhyInfo.getId());
        updateParam.setVersion(version);
        return editTemplateWithoutCheck(updateParam, operator, retryCount);
    }

    @Override
    public Result<Void> editTemplateWithoutCheck(IndexTemplatePhysicalInfoDTO param, String operator,
                                                 int retryCount) throws ESOperateException {
        IndexTemplatePhyInfo oldIndexTemplatePhyInfo = templatePhyService.getTemplateById(param.getId());

        if (param.getShard() != null && !oldIndexTemplatePhyInfo.getShard().equals(param.getShard())) {
            indexPlanManager.initShardRoutingAndAdjustShard(param);
        }

        boolean succ = templatePhyService.update(param).success();
        String tips = "";
        if (succ) {
            if (AriusObjUtils.isChanged(param.getRack(), oldIndexTemplatePhyInfo.getRack())
                    || AriusObjUtils.isChanged(param.getShard(), oldIndexTemplatePhyInfo.getShard())) {
                esTemplateService.syncUpdateRackAndShard(oldIndexTemplatePhyInfo.getCluster(), oldIndexTemplatePhyInfo.getName(), param.getRack(),
                        param.getShard(), param.getShardRouting(), retryCount);
                if (AriusObjUtils.isChanged(param.getRack(), oldIndexTemplatePhyInfo.getRack())) {
                    tips = "模板部署rack变更!请注意模板APP是否可以使用修改后的rack";
                }
            }

            SpringTool.publish(new PhysicalTemplateModifyEvent(this, ConvertUtil.obj2Obj(oldIndexTemplatePhyInfo, IndexTemplatePhyInfo.class),
                    templatePhyService.getTemplateById(oldIndexTemplatePhyInfo.getId()),
                    indexTemplateInfoService.getLogicTemplateWithPhysicalsById(oldIndexTemplatePhyInfo.getLogicId())));
        }

        return Result.buildWithTips(succ, tips);
    }

    @Override
    public Tuple</*存放冷存索引列表*/Set<String>,/*存放热存索引列表*/Set<String>> getHotAndColdIndexByBeforeDay(IndexTemplatePhyInfoWithLogic physicalWithLogic, int days) {
        try {
            IndexTemplateInfo logicTemplate = physicalWithLogic.getLogicTemplate();

            if (!physicalWithLogic.getExpression().endsWith("*")) {
                return new Tuple<>();
            }

            if (!TemplateUtils.isSaveByDay(logicTemplate.getDateFormat())
                    && !TemplateUtils.isSaveByMonth(logicTemplate.getDateFormat())) {
                return new Tuple<>();
            }

            List<String> indices = templatePhyService.getMatchIndexNames(physicalWithLogic.getId());
            if (CollectionUtils.isEmpty(indices)) {
                LOGGER.info("class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||template={}||msg=no match indices", logicTemplate.getName());
                return new Tuple<>();
            }

            return getHotAndColdIndexSet(physicalWithLogic, days, logicTemplate, indices);
        } catch (Exception e) {
            LOGGER.warn("class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||templateName={}||errMsg={}", physicalWithLogic.getName(),
                    e.getMessage(), e);
        }

        return new Tuple<>();
    }

    @Override
    public Set<String> getIndexByBeforeDay(IndexTemplatePhyInfoWithLogic physicalWithLogic, int days) {
        try {
            IndexTemplateInfo logicTemplate = physicalWithLogic.getLogicTemplate();

            if (!physicalWithLogic.getExpression().endsWith("*")) {
                return Sets.newHashSet();
            }

            if (!TemplateUtils.isSaveByDay(logicTemplate.getDateFormat())
                    && !TemplateUtils.isSaveByMonth(logicTemplate.getDateFormat())) {
                return Sets.newHashSet();
            }

            List<String> indices = templatePhyService.getMatchIndexNames(physicalWithLogic.getId());
            if (CollectionUtils.isEmpty(indices)) {
                LOGGER.info("class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||template={}||msg=no match indices", logicTemplate.getName());
                return Sets.newHashSet();
            }

            return getFinalIndexSet(physicalWithLogic, days, logicTemplate, indices);
        } catch (Exception e) {
            LOGGER.warn("class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||templateName={}||errMsg={}", physicalWithLogic.getName(),
                    e.getMessage(), e);
        }

        return Sets.newHashSet();
    }

    @Override
    public List<ConsoleTemplatePhyVO> getConsoleTemplatePhyVOS(IndexTemplatePhysicalInfoDTO param, Integer appId) {
        List<ConsoleTemplatePhyVO> consoleTemplatePhyVOS = ConvertUtil.list2List(templatePhyService.getByCondt(param),
            ConsoleTemplatePhyVO.class);

        buildConsoleTemplatePhyVO(consoleTemplatePhyVOS, appId);

        return consoleTemplatePhyVOS;
    }

    @Override
    public List<String> getTemplatePhyNames(Integer appId) {
        return getConsoleTemplatePhyVOS(null, appId).parallelStream().map(ConsoleTemplatePhyVO::getName)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getCanCopyTemplatePhyClusterPhyNames(Long templatePhyId) {
        List<String> canCopyClusterPhyNames = Lists.newArrayList();
        IndexTemplatePhyInfo templatePhy = templatePhyService.getTemplateById(templatePhyId);
        if (null != templatePhy && null != templatePhy.getCluster()) {
            clusterPhyService.listAllClusters()
                    .stream()
                    .filter(clusterPhy -> !templatePhy.getCluster().equals(clusterPhy.getCluster()))
                    .forEach(clusterPhy -> canCopyClusterPhyNames.add(clusterPhy.getCluster()));
        }

        return canCopyClusterPhyNames;
    }

    @Override
    public Result<List<IndexTemplatePhysicalVO>> getTemplatePhies(Integer logicId) {
        if (!indexTemplateInfoService.exist(logicId)) {
            return Result.buildFail("模板Id不存在");
        }
        return Result.buildSucc(
            ConvertUtil.list2List(templatePhyService.getTemplateByLogicId(logicId), IndexTemplatePhysicalVO.class));
    }

    /**************************************** private method ****************************************************/
    private void initParamWhenAdd(IndexTemplatePhysicalInfoDTO param) {
        IndexTemplateInfo logic = indexTemplateInfoService.getLogicTemplateById(param.getLogicId());

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

        IndexTemplatePhyInfo oldIndexTemplatePhyInfo = templatePhyService.getTemplateById(param.getPhysicalId());
        if (oldIndexTemplatePhyInfo == null) {
            return Result.buildNotExist(TEMPLATE_PHYSICAL_NOT_EXISTS);
        }
        if (Objects.equals(param.getVersion(), oldIndexTemplatePhyInfo.getVersion())
                || (param.getVersion() > 0 && param.getVersion() < oldIndexTemplatePhyInfo.getVersion())) {
            return Result.buildParamIllegal("物理模板版本非法");
        }
        if (param.getRack() != null && !clusterPhyService.isRacksExists(oldIndexTemplatePhyInfo.getCluster(), param.getRack())) {
            return Result.buildParamIllegal("物理模板rack非法");
        }
        if (param.getShard() != null && param.getShard() < MIN_SHARD_NUM) {
            return Result.buildParamIllegal("shard个数非法");
        }

        IndexTemplateInfo logic = indexTemplateInfoService.getLogicTemplateById(oldIndexTemplatePhyInfo.getLogicId());
        if (TemplateUtils.isOnly1Index(logic.getExpression())) {
            return Result.buildParamIllegal("不是分区创建的索引，不能升版本");
        }

        return Result.buildSucc();
    }

    private Result<Void> upgradeTemplateWithCheck(TemplatePhysicalUpgradeDTO param, String operator,
                                            int retryCount) throws ESOperateException {
        IndexTemplatePhyInfo indexTemplatePhyInfo = templatePhyService.getTemplateById(param.getPhysicalId());
        if (templateLabelService.hasDeleteDoc(indexTemplatePhyInfo.getLogicId())) {
            return Result.buildParamIllegal("模板有删除操作,禁止升版本");
        }

        IndexTemplateInfo logic = indexTemplateInfoService.getLogicTemplateById(indexTemplatePhyInfo.getLogicId());
        LOGGER.info("class=TemplatePhyManagerImpl||method=upgradeTemplateWithCheck||name={}||rack={}||shard={}||version={}", logic.getName(), param.getRack(),
                param.getShard(), param.getVersion());

        IndexTemplatePhysicalInfoDTO updateParam = new IndexTemplatePhysicalInfoDTO();
        updateParam.setId(indexTemplatePhyInfo.getId());
        updateParam.setRack(param.getRack());
        updateParam.setShard(param.getShard());
        updateParam.setVersion(param.getVersion());
        Result<Void> editResult = editTemplateWithoutCheck(updateParam, operator, retryCount);

        if (editResult.failed()) {
            return editResult;
        }

        templatePreCreateManager.asyncCreateTodayAndTomorrowIndexByPhysicalId(indexTemplatePhyInfo.getId(), 3);

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

        IndexTemplatePhyInfo oldIndexTemplatePhyInfo = templatePhyService.getTemplateById(param.getPhysicalId());
        if (oldIndexTemplatePhyInfo == null) {
            return Result.buildNotExist(TEMPLATE_PHYSICAL_NOT_EXISTS);
        }

        if (!clusterPhyService.isClusterExists(param.getCluster())) {
            return Result.buildNotExist("目标集群不存在");
        }

        if (oldIndexTemplatePhyInfo.getCluster().equals(param.getCluster())) {
            return Result.buildParamIllegal("目标集群不能与源集群相同");
        }

        if (StringUtils.isNotEmpty(param.getRack())
                && !clusterPhyService.isRacksExists(param.getCluster(), param.getRack())) {
            return Result.buildNotExist("rack不存在");
        }

        if (param.getShard() < 1) {
            return Result.buildParamIllegal("shard非法");
        }

        return Result.buildSucc();
    }

    private boolean needOperateAhead(IndexTemplatePhyInfoWithLogic physicalWithLogic) {
        Set<String> clusterSet = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
                CLUSTERS_INDEX_EXPIRE_DELETE_AHEAD, "", ",");
        return clusterSet.contains(physicalWithLogic.getCluster());
    }

    private Result<Void> checkMetaInner(IndexTemplatePhyInfo templatePhysical,
                                        Map<Integer, IndexTemplateInfo> logicId2IndexTemplateLogicMap,
                                        Set<String> esClusters) {
        List<String> errMsgs = Lists.newArrayList();

        if (!esClusters.contains(templatePhysical.getCluster())) {
            errMsgs.add("物理集群不存在：" + templatePhysical.getName() + "(" + templatePhysical.getId() + ")");
        }

        if (!logicId2IndexTemplateLogicMap.containsKey(templatePhysical.getLogicId())) {
            errMsgs.add("逻辑模板不存在：" + templatePhysical.getName() + "(" + templatePhysical.getId() + ")");
        }

        TemplateConfig templateConfig = esTemplateService.syncGetTemplateConfig(templatePhysical.getCluster(),
                templatePhysical.getName());

        if (templateConfig == null) {
            errMsgs.add("es模板不存在：" + templatePhysical.getName() + "(" + templatePhysical.getId() + ")");
        }

        if (CollectionUtils.isEmpty(errMsgs)) {
            return Result.buildSucc();
        }

        return Result.build( ResultType.ADMIN_META_ERROR.getCode(), String.join(",", errMsgs));

    }

    private int checkIndexCreateAndExpire(IndexTemplatePhyInfo templatePhysical,
                                          Map<Integer, IndexTemplateInfo> logicId2IndexTemplateLogicMap) {
        int result = INDEX_OP_OK;
        if (templatePhysical.getCreateTime().before(AriusDateUtils.getZeroDate())) {
            Set<String> indices = Sets.newHashSet( templatePhyService.getMatchNoVersionIndexNames(templatePhysical.getId()));

            IndexTemplateInfo templateLogic = logicId2IndexTemplateLogicMap.get(templatePhysical.getLogicId());
            String tomorrowIndexName = IndexNameFactory.getNoVersion(templateLogic.getExpression(),
                    templateLogic.getDateFormat(), 1);
            String expireIndexName = IndexNameFactory.getNoVersion(templateLogic.getExpression(),
                    templateLogic.getDateFormat(), -1 * templateLogic.getExpireTime());

            if (!indices.contains(tomorrowIndexName)) {
                LOGGER.warn("class=TemplatePhyManagerImpl||method=checkIndexCreateAndExpire||cluster={}||template={}||msg=TOMORROW_INDEX_NOT_CREATE",
                        templatePhysical.getCluster(), templatePhysical.getName());
                result = result + TOMORROW_INDEX_NOT_CREATE;
            }

            if (TemplateUtils.isSaveByDay(templateLogic.getDateFormat()) && indices.contains(expireIndexName)) {
                LOGGER.warn("class=TemplatePhyManagerImpl||method=checkIndexCreateAndExpire||cluster={}||template={}||msg=EXPIRE_INDEX_NOT_DELETE",
                        templatePhysical.getCluster(), templatePhysical.getName());
                result = result + EXPIRE_INDEX_NOT_DELETE;
            }
        }
        return result;
    }

    private IndexTemplateInfoWithPhyTemplates buildIndexTemplateLogicWithPhysicalForNew(IndexTemplatePhysicalInfoDTO param) {
        IndexTemplateInfoWithPhyTemplates logicWithPhysical = indexTemplateInfoService
                .getLogicTemplateWithPhysicalsById(param.getLogicId());
        if (CollectionUtils.isNotEmpty(param.getPhysicalInfos())) {
            List<IndexTemplatePhyInfo> physicals = ConvertUtil.list2List(param.getPhysicalInfos(), IndexTemplatePhyInfo.class);
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
        return  (shardNum != null && shardNum > 0);
    }

    private void buildConsoleTemplatePhyVO(List<ConsoleTemplatePhyVO> params, Integer currentAppId) {
        
        Map<Integer, String> appId2AppNameMap = Maps.newHashMap();

        for (ConsoleTemplatePhyVO consoleTemplatePhyVO : params) {

            IndexTemplateInfo logicTemplate = indexTemplateInfoService.getLogicTemplateById(consoleTemplatePhyVO.getLogicId());
            if (AriusObjUtils.isNull(logicTemplate)) {
                LOGGER.error(
                        "class=TemplatePhyServiceImpl||method=buildConsoleTemplatePhyVO||errMsg=IndexTemplateLogic is empty||logicId={}",
                        consoleTemplatePhyVO.getLogicId());
                continue;
            }

            handleIndexTemplateLogic(currentAppId, appId2AppNameMap, consoleTemplatePhyVO, logicTemplate);

        }
    }

    private void handleIndexTemplateLogic(Integer currentAppId, Map<Integer, String> appId2AppNameMap, ConsoleTemplatePhyVO consoleTemplatePhyVO, IndexTemplateInfo logicTemplate) {
        //设置归属项目信息
        Integer appIdFromLogicTemplate = logicTemplate.getAppId();
        if (!AriusObjUtils.isNull(appIdFromLogicTemplate)) {
            consoleTemplatePhyVO.setAppId(appIdFromLogicTemplate);

            if (appId2AppNameMap.containsKey(appIdFromLogicTemplate)) {
                consoleTemplatePhyVO.setAppName(appId2AppNameMap.get(logicTemplate.getAppId()));
            } else {
                String appName = appService.getAppName(logicTemplate.getAppId());
                if (!AriusObjUtils.isNull(appName)) {
                    consoleTemplatePhyVO.setAppName(appName);
                    appId2AppNameMap.put(appIdFromLogicTemplate, appName);
                }
            }
        }

        //设置逻辑模板名称
        consoleTemplatePhyVO.setLogicName(logicTemplate.getName());

        //设置描述信息, 是否要加一列描述信息
        consoleTemplatePhyVO.setMemo(logicTemplate.getDesc());

        //设置权限
        if (AriusObjUtils.isNull(currentAppId)) {
            consoleTemplatePhyVO.setAuthType(AppTemplateAuthEnum.NO_PERMISSION.getCode());
            return;
        }
        if (currentAppId.equals(appIdFromLogicTemplate)) {
            consoleTemplatePhyVO.setAuthType(AppTemplateAuthEnum.OWN.getCode());
        } else {
            AppTemplateAuthEnum authEnum = appLogicTemplateAuthService.getAuthEnumByAppIdAndLogicId(currentAppId,
                    appIdFromLogicTemplate);
            consoleTemplatePhyVO.setAuthType(authEnum.getCode());
        }
    }

    private Result<Void> handleValidateTemplate(IndexTemplatePhysicalInfoDTO param) {
        if (param.getCluster() != null && !clusterPhyService.isClusterExists(param.getCluster())) {
            return Result.buildParamIllegal("集群不存在");
        }
        if (StringUtils.isNotEmpty(param.getRack())) {
            if (!clusterPhyService.isRacksExists(param.getCluster(), param.getRack())) {
                return Result.buildParamIllegal("集群rack不存在");
            }
           /* // 校验rack匹配且只匹配到一个region
            if (regionRackService.countRackMatchedRegion(param.getCluster(), param.getRack()) != 1) {
                return Result.buildParamIllegal("集群rack不符合逻辑集群规划");
            }*/
        }
        if (param.getShard() != null && param.getShard() < 1) {
            return Result.buildParamIllegal("shard个数非法");
        }
        if (param.getRole() != null
                && TemplateDeployRoleEnum.UNKNOWN.equals(TemplateDeployRoleEnum.valueOf(param.getRole()))) {
            return Result.buildParamIllegal("模板角色非法");
        }
        if (param.getLogicId() != null && !Objects.equals(param.getLogicId(), NOT_CHECK)) {
            IndexTemplateInfo logic = indexTemplateInfoService.getLogicTemplateById(param.getLogicId());
            if (logic == null) {
                return Result.buildNotExist("逻辑模板不存在");
            }
        }
        return Result.buildSucc();
    }

    private Result<Void> handleValidateTemplateEdit(IndexTemplatePhysicalInfoDTO param) {
        if (AriusObjUtils.isNull(param.getId())) {
            return Result.buildParamIllegal(TEMPLATE_PHYSICAL_ID_IS_NULL);
        }
        IndexTemplatePhyInfo indexTemplatePhyInfo = templatePhyService.getTemplateById(param.getId());
        if (indexTemplatePhyInfo == null) {
            return Result.buildNotExist(TEMPLATE_PHYSICAL_NOT_EXISTS);
        }
        return Result.buildSucc();
    }

    private Result<Void> handleValidateTemplateAdd(IndexTemplatePhysicalInfoDTO param) {
        if (AriusObjUtils.isNull(param.getLogicId())) {
            return Result.buildParamIllegal("逻辑模板id为空");
        }
        if (AriusObjUtils.isNull(param.getCluster())) {
            return Result.buildParamIllegal("集群为空");
        }

        if (AriusObjUtils.isNull(param.getShard())) {
            return Result.buildParamIllegal("shard为空");
        }
        if (AriusObjUtils.isNull(param.getRole())) {
            return Result.buildParamIllegal("模板角色为空");
        }

        IndexTemplatePhyInfo indexTemplatePhyInfo = templatePhyService.getTemplateByClusterAndName(param.getCluster(), param.getName());
        if (indexTemplatePhyInfo != null) {
            return Result.buildDuplicate("物理模板已经存在");
        }
        return Result.buildSucc();
    }

    private Set<String> getFinalIndexSet(IndexTemplatePhyInfoWithLogic physicalWithLogic, int days, IndexTemplateInfo logicTemplate, List<String> indices) {
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

            if (needOperateAhead(physicalWithLogic)) {
                int aheadSeconds = ariusConfigInfoService.intSetting(ARIUS_COMMON_GROUP,
                        INDEX_OPERATE_AHEAD_SECONDS, 2 * 60 * 60);
                indexTime = AriusDateUtils.getBeforeSeconds(indexTime, aheadSeconds);
            }

            long timeIntervalDay = (System.currentTimeMillis() - indexTime.getTime()) / MILLIS_PER_DAY;
            if (timeIntervalDay < days) {
                LOGGER.info(
                        "class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||template={}||indexName={}||timeIntervalDay={}||msg=index not match",
                        logicTemplate.getName(), indexName, timeIntervalDay);
                continue;
            }

            LOGGER.info("class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||indexName={}||indexTime={}||timeIntervalDay={}", indexName,
                    indexTime, timeIntervalDay);

            finalIndexSet.add(indexName);
        }
        return finalIndexSet;
    }

    private void syncCreateIndexTemplateWithEs(IndexTemplatePhysicalInfoDTO param) throws ESOperateException {
        IndexTemplateInfo logic = indexTemplateInfoService.getLogicTemplateById(param.getLogicId());
        MappingConfig mappings = null;
        Result result = AriusIndexMappingConfigUtils.parseMappingConfig(param.getMappings());
        if (result.success()) {
            mappings = (MappingConfig) result.getData();
        }
        Map<String, String> settingsMap = getSettingsMap(param.getCluster(), param.getRack(), param.getShard(), param.getShardRouting(), param.getSettings());
        boolean ret;
        if (null != mappings || null != param.getSettings()) {
            ret = esTemplateService.syncCreate(settingsMap, param.getCluster(), param.getName(), logic.getExpression(), mappings, 0);
        } else {
            ret = esTemplateService.syncCreate(param.getCluster(), param.getName(), logic.getExpression(), param.getRack(), param.getShard(), param.getShardRouting(), 0);
        }
        if (!ret) {
            throw new ESOperateException("failed to create template!");
        }
    }

    private Map<String, String> getSettingsMap(String cluster, String rack, Integer shard, Integer shardRouting, AriusIndexTemplateSetting settings) {
        Map<String, String> settingsMap = new HashMap<>();
        if (StringUtils.isNotBlank(rack)) {
            settingsMap.put(TEMPLATE_INDEX_INCLUDE_RACK, rack);
        }
        if (shard != null && shard > 0) {
            settingsMap.put(INDEX_SHARD_NUM, String.valueOf(shard));
        }
        /*if (shardRouting != null && shardRoutingEnableClusters.contains(cluster)) {
            settingsMap.put(INDEX_SHARD_ROUTING_NUM, String.valueOf(shardRouting));
        }*/
        settingsMap.put(SINGLE_TYPE, "true");

        //这里设置自定义分词器、副本数量、translog是否异步
        if (null != settings) {
            settingsMap.putAll(settings.toJSON());
        }
        return settingsMap;
    }

    private Tuple</*存放冷存索引列表*/Set<String>,/*存放热存索引列表*/Set<String>> getHotAndColdIndexSet(IndexTemplatePhyInfoWithLogic physicalWithLogic,
                                                                                         int days, IndexTemplateInfo logicTemplate, List<String> indices) {
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

            LOGGER.info("class=TemplatePhyManagerImpl||method=getIndexByBeforeDay||indexName={}||indexTime={}||timeIntervalDay={}", indexName,
                    indexTime, timeIntervalDay);

            finalColdIndexSet.add(indexName);
        }
        return new Tuple<>(finalColdIndexSet, finalHotIndexSet);
    }
}
