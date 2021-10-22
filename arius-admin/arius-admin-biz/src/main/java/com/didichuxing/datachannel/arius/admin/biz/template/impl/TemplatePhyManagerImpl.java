package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.COPY;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.DELETE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.SWITCH_MASTER_SLAVE;
import static com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum.MASTER;
import static com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum.SLAVE;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.MILLIS_PER_DAY;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;
import static com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory.genIndexNameClear;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.INDEX_SHARD_NUM;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.TEMPLATE_INDEX_INCLUDE_RACK;

import com.didichuxing.datachannel.arius.admin.core.component.CacheSwitch;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.TemplatePreCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.shard.TemplateShardManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplatePhysicalCopyDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplatePhysicalUpgradeDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplatePhyVO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplatePhysicalPO;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.notify.info.cluster.ClusterTemplatePhysicalMetaErrorNotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.info.template.TemplatePhysicalMetaErrorNotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.service.NotifyService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.impl.TemplatePhyServiceImpl;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhysicalDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@Component
public class TemplatePhyManagerImpl implements TemplatePhyManager {

    private static final ILog           LOGGER                    = LogFactory.getLog(TemplatePhyServiceImpl.class);

    public static final Integer         NOT_CHECK                 = -100;
    private static final Integer        INDEX_OP_OK               = 0;
    private static final Integer        TOMORROW_INDEX_NOT_CREATE = 1;
    private static final Integer        EXPIRE_INDEX_NOT_DELETE   = 2;
    private static final Integer        INDEX_ALL_ERR             = TOMORROW_INDEX_NOT_CREATE + EXPIRE_INDEX_NOT_DELETE;

    @Autowired
    private IndexTemplatePhysicalDAO    indexTemplatePhysicalDAO;

    @Autowired
    private OperateRecordService        operateRecordService;

    @Autowired
    private ESClusterPhyService         esClusterPhyService;

    @Autowired
    private TemplateLabelService        templateLabelService;

    @Autowired
    private ESTemplateService           esTemplateService;

    @Autowired
    private TemplatePreCreateManager    templatePreCreateManager;

    @Autowired
    private TemplateShardManager        templateShardManager;

    @Autowired
    private ESRegionRackService         esRegionRackService;

    @Autowired
    private TemplateLogicService        templateLogicService;

    @Autowired
    private TemplatePhyService          templatePhyService;

    @Autowired
    private NotifyService               notifyService;

    @Autowired
    private AriusConfigInfoService      ariusConfigInfoService;

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private CacheSwitch                 cacheSwitch;

    @Autowired
    private AppService                  appService;

    private FutureUtil                  futureUtil                = FutureUtil.init("TemplatePhyManagerImpl");

    private final Cache<Integer, List<ConsoleTemplatePhyVO>> consoleTemplatePhyVOSCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();

    /**
     * 元数据校验
     *
     * @return
     */
    @Override
    public boolean checkMeta() {
        List<IndexTemplatePhy> templatePhysicals = templatePhyService.listTemplate();

        List<IndexTemplateLogic> templateLogics = templateLogicService.getLogicTemplatesWithCache();
        Map<Integer, IndexTemplateLogic> logicId2IndexTemplateLogicMap = ConvertUtil.list2Map(templateLogics,
                IndexTemplateLogic::getId);

        Multimap<String, IndexTemplatePhy> cluster2IndexTemplatePhysicalMultiMap = ConvertUtil
                .list2MulMap(templatePhysicals, IndexTemplatePhy::getCluster);

        Set<String> esClusters = esClusterPhyService.listAllClusters().stream().map( ESClusterPhy::getCluster)
                .collect( Collectors.toSet());

        for (String cluster : cluster2IndexTemplatePhysicalMultiMap.keySet()) {

            int tomorrowIndexNotCreateCount = 0;
            int expireIndexNotDeleteCount = 0;

            Collection<IndexTemplatePhy> clusterTemplates = cluster2IndexTemplatePhysicalMultiMap.get(cluster);

            for (IndexTemplatePhy templatePhysical : clusterTemplates) {
                try {
                    Result result = checkMetaInner(templatePhysical, logicId2IndexTemplateLogicMap, esClusters);
                    if (result.success()) {
                        LOGGER.info("method=metaCheck||msg=succ||physicalId={}", templatePhysical.getId());
                    } else {
                        LOGGER.warn("method=metaCheck||msg=fail||physicalId={}||failMsg={}", templatePhysical.getId(),
                                result.getMessage());
                        notifyService.send( NotifyTaskTypeEnum.TEMPLATE_PHYSICAL_META_ERROR,
                                new TemplatePhysicalMetaErrorNotifyInfo(templatePhysical, result.getMessage()),
                                Arrays.asList());
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

            List<String> errMsgs = Lists.newArrayList();
            if (tomorrowIndexNotCreateCount * 1.0 / clusterTemplates.size() > 0.7) {
                errMsgs.add("有" + tomorrowIndexNotCreateCount + "个索引模板创建明天索引失败");
            }
            if (expireIndexNotDeleteCount * 1.0 / clusterTemplates.size() > 0.7) {
                errMsgs.add("有" + expireIndexNotDeleteCount + "个索引模板删除过期索引失败");
            }

            if (CollectionUtils.isNotEmpty(errMsgs)) {
                notifyService.send(NotifyTaskTypeEnum.CLUSTER_TEMPLATE_PHYSICAL_META_ERROR,
                        new ClusterTemplatePhysicalMetaErrorNotifyInfo(cluster, String.join(",", errMsgs)),
                        Arrays.asList());
            }

        }

        return true;
    }

    /**
     * 元数据同步
     *
     * @param physicalId
     * @return
     */
    @Override
    public void syncMeta(Long physicalId, int retryCount) throws ESOperateException {

        // 从数据库获取物理模板
        TemplatePhysicalPO physicalPO = indexTemplatePhysicalDAO.getById(physicalId);
        if (physicalPO == null) {
            return;
        }

        // 从ES集群获取模板配置
        TemplateConfig templateConfig = esTemplateService.syncGetTemplateConfig(physicalPO.getCluster(),
                physicalPO.getName());

        if (templateConfig == null) {
            // es集群中还没有模板，创建
            esTemplateService.syncCreate(physicalPO.getCluster(), physicalPO.getName(), physicalPO.getExpression(),
                    physicalPO.getRack(), physicalPO.getShard(), physicalPO.getShardRouting(), retryCount);

        } else {
            // 校验表达式
            if (!physicalPO.getExpression().equals(templateConfig.getTemplate())) {
                // 表达式不同（表达式发生变化），同步到ES集群
                if (esTemplateService.syncUpdateExpression(physicalPO.getCluster(), physicalPO.getName(),
                        physicalPO.getExpression(), retryCount)) {
                    LOGGER.info("method=syncMeta||msg=syncUpdateExpression succ||template={}||srcExp={}||tgtExp={}",
                            physicalPO.getName(), templateConfig.getTemplate(), physicalPO.getExpression());
                }
            }

            // 标志shard或rack是否需要修改
            boolean editShardOrRack = false;
            Map<String, String> settings = templateConfig.getSetttings();
            String rack = settings.get(TEMPLATE_INDEX_INCLUDE_RACK);
            String shardNum = settings.get(INDEX_SHARD_NUM);

            // 校验shard个数
            if (!String.valueOf(physicalPO.getShard()).equals(shardNum)) {
                editShardOrRack = true;
                shardNum = String.valueOf(physicalPO.getShard());
            }

            // 校验rack
            if (StringUtils.isNotBlank(physicalPO.getRack())) {
                if (!settings.containsKey(TEMPLATE_INDEX_INCLUDE_RACK)
                        || !physicalPO.getRack().equals(settings.get(TEMPLATE_INDEX_INCLUDE_RACK))) {
                    editShardOrRack = true;
                    rack = physicalPO.getRack();
                }
            }

            if (editShardOrRack) {
                // 同步变化到ES集群
                if (esTemplateService.syncUpdateRackAndShard(physicalPO.getCluster(), physicalPO.getName(), rack,
                        Integer.valueOf(shardNum), physicalPO.getShardRouting(), retryCount)) {
                    LOGGER.info(
                            "method=syncMeta||msg=syncUpdateRackAndShard succ||template={}||srcRack={}||srcShard={}||tgtRack={}||tgtShard={}",
                            physicalPO.getName(), settings.get(TEMPLATE_INDEX_INCLUDE_RACK), settings.get(INDEX_SHARD_NUM),
                            rack, shardNum);
                }
            }
        }
    }

    /**
     * 删除
     *
     * @param physicalId 物理模板id
     * @param operator   操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delTemplate(Long physicalId, String operator) throws ESOperateException {
        TemplatePhysicalPO oldPO = indexTemplatePhysicalDAO.getById(physicalId);
        if (oldPO == null) {
            return Result.buildNotExist("template not exist");
        }

        boolean succ = 1 == indexTemplatePhysicalDAO.updateStatus(physicalId,
                TemplatePhysicalStatusEnum.INDEX_DELETING.getCode());
        if (succ) {
            // 删除集群中的模板
            esTemplateService.syncDelete(oldPO.getCluster(), oldPO.getName(), 0);

            operateRecordService.save(TEMPLATE, DELETE, oldPO.getLogicId(), "删除" + oldPO.getCluster() + "物理模板",
                    operator);

            SpringTool.publish(new PhysicalTemplateDeleteEvent(this, ConvertUtil.obj2Obj(oldPO, IndexTemplatePhy.class),
                    templateLogicService.getLogicTemplateWithPhysicalsById(oldPO.getLogicId())));
        }

        return Result.build(succ);
    }

    /**
     * 删除
     *
     * @param logicId  id
     * @param operator 操作人
     * @return result
     * @throws ESOperateException e
     */
    @Override
    public Result delTemplateByLogicId(Integer logicId, String operator) throws ESOperateException {
        List<TemplatePhysicalPO> physicalPOs = indexTemplatePhysicalDAO.listByLogicId(logicId);

        boolean succ = true;
        if (CollectionUtils.isEmpty(physicalPOs)) {
            LOGGER.info("method=delTemplateByLogicId||logicId={}||msg=template no physical info!", logicId);
        } else {
            LOGGER.info("method=delTemplateByLogicId||logicId={}||physicalSize={}||msg=template has physical info!",
                    logicId, physicalPOs.size());
            for (TemplatePhysicalPO physicalPO : physicalPOs) {
                if (delTemplate(physicalPO.getId(), operator).failed()) {
                    succ = false;
                }

            }
        }

        return Result.build(succ);
    }

    /**
     * 升版本
     * <p>
     * 1、修改数据库中的版本号
     * 2、删除原版本明天的索引,如果指定了rack就按着rack创建,否则在源rack上创建
     * 3、创建新版本明天的索引,按着模板rack创建
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result upgradeTemplate(TemplatePhysicalUpgradeDTO param, String operator) throws ESOperateException {
        Result checkResult = checkUpgradeParam(param);
        if (checkResult.failed()) {
            LOGGER.warn("check fail||msg={}", checkResult.getMessage());
            return checkResult;
        }

        return upgradeTemplateWithCheck(param, operator, 0);
    }

    /**
     * 复制 只在目标集群建立模板即可,模板管理的资源都是与逻辑模板id管理,与物理模板没有关系
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result copyTemplate(TemplatePhysicalCopyDTO param, String operator) throws AdminOperateException {
        Result checkResult = checkCopyParam(param);
        if (checkResult.failed()) {
            LOGGER.warn("check fail||msg={}", checkResult.getMessage());
            return checkResult;
        }

        // 默认升级
        if (param.getUpgrade() == null) {
            param.setUpgrade(true);
        }

        TemplatePhysicalPO srcPhysicalPO = indexTemplatePhysicalDAO.getById(param.getPhysicalId());
        IndexTemplatePhysicalDTO tgtTemplateParam = ConvertUtil.obj2Obj(srcPhysicalPO, IndexTemplatePhysicalDTO.class);
        tgtTemplateParam.setCluster(param.getCluster());
        tgtTemplateParam.setRack(param.getRack());
        tgtTemplateParam.setRole(SLAVE.getCode());
        tgtTemplateParam.setVersion(srcPhysicalPO.getVersion());

        Result<Long> addResult = addTemplateWithoutCheck(tgtTemplateParam);
        if (addResult.failed()) {
            return addResult;
        }

        // 记录操作记录
        operateRecordService.save(TEMPLATE, COPY, srcPhysicalPO.getLogicId(),
                "复制" + srcPhysicalPO.getCluster() + "物理模板至" + param.getCluster(), operator);

        if (Boolean.TRUE.equals(param.getUpgrade())) {
            TemplatePhysicalUpgradeDTO upgradeParam = new TemplatePhysicalUpgradeDTO();
            upgradeParam.setPhysicalId(addResult.getData());
            upgradeParam.setVersion(srcPhysicalPO.getVersion());
            Result upgradeResult = upgradeTemplateWithCheck(upgradeParam, operator, 0);
            if (upgradeResult.failed()) {
                throw new AdminOperateException("目标集群升级版本异常:" + upgradeResult.getMessage());
            }
        }

        if (esTemplateService.syncCopyMappingAndAlias(srcPhysicalPO.getCluster(), srcPhysicalPO.getName(),
                tgtTemplateParam.getCluster(), tgtTemplateParam.getName(), 0)) {
            LOGGER.info("methood=copyTemplate||TemplatePhysicalCopyDTO={}||msg=syncCopyMappingAndAlias succ", param);
        } else {
            LOGGER.warn("methood=copyTemplate||TemplatePhysicalCopyDTO={}||msg=syncCopyMappingAndAlias fail", param);
        }

        return Result.buildSucWithTips("模板部署集群变更!请注意模板APP是否可以使用修改后的集群rack\n模板复制后请确认逻辑模板quota是否充足");
    }

    /**
     * 删除
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result editTemplate(IndexTemplatePhysicalDTO param, String operator) throws ESOperateException {
        Result checkResult = validateTemplate(param, EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("check fail||msg={}", checkResult.getMessage());
            return checkResult;
        }

        return editTemplateWithoutCheck(param, operator, 0);
    }

    /**
     * 校验物理模板信息
     *
     * @param param     参数
     * @param operation 操作
     * @return result
     */
    @Override
    public Result validateTemplate(IndexTemplatePhysicalDTO param, OperationEnum operation) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("物理模板参数为空");
        }
        if (operation == OperationEnum.ADD) {
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

            TemplatePhysicalPO physicalPO = indexTemplatePhysicalDAO.getByClusterAndName(param.getCluster(),
                    param.getName());
            if (physicalPO != null) {
                return Result.buildDuplicate("物理模板已经存在");
            }
        } else if (operation == EDIT) {
            if (AriusObjUtils.isNull(param.getId())) {
                return Result.buildParamIllegal("物理模板id为空");
            }
            TemplatePhysicalPO physicalPO = indexTemplatePhysicalDAO.getById(param.getId());
            if (physicalPO == null) {
                return Result.buildNotExist("物理模板不存在");
            }
        }

        if (param.getCluster() != null) {
            if (!esClusterPhyService.isClusterExists(param.getCluster())) {
                return Result.buildParamIllegal("集群不存在");
            }
        }
        if (StringUtils.isNotEmpty(param.getRack())) {
            if (!esClusterPhyService.isRacksExists(param.getCluster(), param.getRack())) {
                return Result.buildParamIllegal("集群rack不存在");
            }
            // 校验rack匹配且只匹配到一个region
            if (esRegionRackService.countRackMatchedRegion(param.getCluster(), param.getRack()) != 1) {
                return Result.buildParamIllegal("集群rack不符合逻辑集群规划");
            }
        }
        if (param.getShard() != null) {
            if (param.getShard() < 1) {
                return Result.buildParamIllegal("shard个数非法");
            }
        }
        if (param.getRole() != null) {
            if (TemplateDeployRoleEnum.UNKNOWN.equals(TemplateDeployRoleEnum.valueOf(param.getRole()))) {
                return Result.buildParamIllegal("模板角色非法");
            }
        }
        if (param.getLogicId() != null && !Objects.equals(param.getLogicId(), NOT_CHECK)) {
            IndexTemplateLogic logic = templateLogicService.getLogicTemplateById(param.getLogicId());
            if (logic == null) {
                return Result.buildNotExist("逻辑模板不存在");
            }
        }

        return Result.buildSucc();
    }

    /**
     * 批量校验物理模板信息
     *
     * @param params    参数
     * @param operation 操作
     * @return result
     */
    @Override
    public Result validateTemplates(List<IndexTemplatePhysicalDTO> params, OperationEnum operation) {
        if (AriusObjUtils.isNull(params)) {
            return Result.buildParamIllegal("物理模板信息为空");
        }

        Set<String> deployClusterSet = Sets.newTreeSet();
        for (IndexTemplatePhysicalDTO param : params) {
            Result checkResult = validateTemplate(param, operation);
            if (checkResult.failed()) {
                LOGGER.warn("check fail||msg={}", checkResult.getMessage());
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

    /**
     * 批量新增物理模板
     *
     * @param logicId       逻辑模板id
     * @param physicalInfos 物理模板信息
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addTemplatesWithoutCheck(Integer logicId,
                                           List<IndexTemplatePhysicalDTO> physicalInfos) throws ESOperateException {
        for (IndexTemplatePhysicalDTO param : physicalInfos) {
            param.setLogicId(logicId);
            param.setPhysicalInfos(physicalInfos);
            Result<Long> result = addTemplateWithoutCheck(param);
            if (result.failed()) {
                result.setMessage(result.getMessage() + "; 集群:" + param.getCluster() + ",模板:" + param.getName());
                return result;
            }
        }
        return Result.buildSucc();
    }

    /**
     * 新建
     *
     * @param param 模板参数
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> addTemplateWithoutCheck(IndexTemplatePhysicalDTO param) throws ESOperateException {
        if(null != indexTemplatePhysicalDAO.getByClusterAndName(param.getCluster(), param.getName())){
            return Result.buildParamIllegal("索引已经存在");
        }

        initParamWhenAdd(param);

        // 为了解决写入的长尾问题，引擎增加了逻辑shard的概念，这里需要计算逻辑shard的值，并调整源shard个数
        templateShardManager.initShardRoutingAndAdjustShard(param);

        TemplatePhysicalPO newTemplate = ConvertUtil.obj2Obj(param, TemplatePhysicalPO.class);
        boolean succ = (1 == indexTemplatePhysicalDAO.insert(newTemplate));
        if (succ) {
            //删除数据库中历史的脏数据
            indexTemplatePhysicalDAO.deleteDirtyByClusterAndName(newTemplate.getCluster(), newTemplate.getName());

            IndexTemplateLogic logic = templateLogicService.getLogicTemplateById(param.getLogicId());

            esTemplateService.syncCreate(param.getCluster(), param.getName(), logic.getExpression(), param.getRack(),
                    param.getShard(), param.getShardRouting(), 0);

            SpringTool.publish(new PhysicalTemplateAddEvent(this,
                    ConvertUtil.obj2Obj(indexTemplatePhysicalDAO.getById(newTemplate.getId()), IndexTemplatePhy.class),
                    buildIndexTemplateLogicWithPhysicalForNew(param)));
        }

        return Result.buildSucc(newTemplate.getId());
    }

    /**
     * 修改由于逻辑模板修改而物理模板需要同步修改的属性
     * <p>
     * 目前有:
     * expression
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result editTemplateFromLogic(IndexTemplateLogicDTO param, String operator) throws ESOperateException {
        List<TemplatePhysicalPO> physicalPOs = indexTemplatePhysicalDAO.listByLogicId(param.getId());
        if (CollectionUtils.isEmpty(physicalPOs)) {
            return Result.buildSucc();
        }

        for (TemplatePhysicalPO physicalPO : physicalPOs) {
            if (AriusObjUtils.isChanged(param.getExpression(), physicalPO.getExpression())) {
                TemplatePhysicalPO updateParam = new TemplatePhysicalPO();
                updateParam.setId(physicalPO.getId());
                updateParam.setExpression(param.getExpression());
                boolean succeed = (1 == indexTemplatePhysicalDAO.update(updateParam));
                if (succeed) {
                    esTemplateService.syncUpdateExpression(physicalPO.getCluster(), physicalPO.getName(),
                            param.getExpression(), 0);
                } else {
                    LOGGER.warn("editTemplateFromLogic fail||physicalId={}||expression={}", physicalPO.getId(),
                            param.getExpression());
                    return Result.build(false);
                }
            }

            if (isValidShardNum(param.getShardNum())
                    && AriusObjUtils.isChanged(param.getShardNum(), physicalPO.getShard())) {
                TemplatePhysicalPO updateParam = new TemplatePhysicalPO();
                updateParam.setId(physicalPO.getId());
                updateParam.setShard(param.getShardNum());
                boolean succeed = 1 == indexTemplatePhysicalDAO.update(updateParam);
                if (succeed) {
                    LOGGER.info("editTemplateFromLogic succeed||physicalId={}||preShardNum={}||currentShardNum={}",
                            physicalPO.getId(), physicalPO.getShard(), param.getShardNum());

                    esTemplateService.syncUpdateRackAndShard(physicalPO.getCluster(), physicalPO.getName(),
                            physicalPO.getRack(), param.getShardNum(), physicalPO.getShardRouting(), 0);
                } else {
                    LOGGER.warn("editTemplateFromLogic fail||physicalId={}||expression={}", physicalPO.getId(),
                            param.getExpression());
                    return Result.build(false);
                }
            }
        }

        return Result.buildSucc();
    }

    /**
     * 主从切换
     *
     * @param logicId                逻辑模板id
     * @param expectMasterPhysicalId 期望的主
     * @param operator               操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result switchMasterSlave(Integer logicId, Long expectMasterPhysicalId, String operator) {
        List<TemplatePhysicalPO> physicalPOS = indexTemplatePhysicalDAO.listByLogicId(logicId);
        if (CollectionUtils.isEmpty(physicalPOS)) {
            return Result.buildNotExist("模板不存在");
        }

        TemplatePhysicalPO oldMaster = null;
        TemplatePhysicalPO newMaster = null;

        for (TemplatePhysicalPO physicalPO : physicalPOS) {
            if (physicalPO.getRole().equals(MASTER.getCode())) {
                if (oldMaster != null) {
                    LOGGER.error(
                            "class=TemplatePhyServiceImpl||method=switchMasterSlave||errMsg=no master||logicId={}",
                            logicId);
                }
                oldMaster = physicalPO;
            } else {
                if (expectMasterPhysicalId == null && newMaster == null) {
                    newMaster = physicalPO;
                }

                if (physicalPO.getId().equals(expectMasterPhysicalId)) {
                    newMaster = physicalPO;
                }
            }
        }

        if (newMaster == null) {
            return Result.buildNotExist("无法确定新的主");
        }

        TemplatePhysicalPO param = new TemplatePhysicalPO();
        boolean succ = true;

        if (oldMaster == null) {
            LOGGER.error("class=TemplatePhyServiceImpl||method=switchMasterSlave||errMsg=no master||logicId={}",
                    logicId);
        } else {
            param.setId(oldMaster.getId());
            param.setRole(SLAVE.getCode());
            succ = succ && 1 == indexTemplatePhysicalDAO.update(param);
        }

        param.setId(newMaster.getId());
        param.setRole(MASTER.getCode());
        succ = succ && (1 == indexTemplatePhysicalDAO.update(param));

        if (succ) {
            operateRecordService.save(TEMPLATE, SWITCH_MASTER_SLAVE, logicId,
                    "src_master:" + (oldMaster != null ? oldMaster.getId() : "") + ", tgt_master:" + newMaster.getId(),
                    operator);
        }

        return Result.build(succ);
    }

    /**
     * 更新模板的rack和shard
     *
     * @param physicalId 物理模板的id
     * @param tgtRack    rack
     * @return result
     * @throws ESOperateException
     */
    @Override
    public Result editTemplateRackWithoutCheck(Long physicalId, String tgtRack, String operator,
                                               int retryCount) throws ESOperateException {
        IndexTemplatePhysicalDTO updateParam = new IndexTemplatePhysicalDTO();
        updateParam.setId(physicalId);
        updateParam.setRack(tgtRack);
        return editTemplateWithoutCheck(updateParam, operator, retryCount);
    }

    /**
     * 升级模板
     *
     * @param physicalId physicalId
     * @return reuslt
     */
    @Override
    public Result upgradeTemplateVersion(Long physicalId, String operator, int retryCount) throws ESOperateException {
        TemplatePhysicalPO physicalPO = indexTemplatePhysicalDAO.getById(physicalId);
        if (physicalPO == null) {
            return Result.buildNotExist("模板不存在");
        }

        int version = physicalPO.getVersion() + 1;
        if (version > 9) {
            version = 0;
        }

        IndexTemplatePhysicalDTO updateParam = new IndexTemplatePhysicalDTO();
        updateParam.setId(physicalPO.getId());
        updateParam.setVersion(version);
        return editTemplateWithoutCheck(updateParam, operator, retryCount);
    }

    @Override
    public Result editTemplateWithoutCheck(IndexTemplatePhysicalDTO param, String operator,
                                           int retryCount) throws ESOperateException {
        TemplatePhysicalPO oldPO = indexTemplatePhysicalDAO.getById(param.getId());

        if (param.getShard() != null && !oldPO.getShard().equals(param.getShard())) {
            templateShardManager.initShardRoutingAndAdjustShard(param);
        }

        TemplatePhysicalPO updateParam = ConvertUtil.obj2Obj(param, TemplatePhysicalPO.class);

        boolean succ = 1 == indexTemplatePhysicalDAO.update(updateParam);
        String tips = "";
        if (succ) {
            if (AriusObjUtils.isChanged(param.getRack(), oldPO.getRack())
                    || AriusObjUtils.isChanged(param.getShard(), oldPO.getShard())) {
                esTemplateService.syncUpdateRackAndShard(oldPO.getCluster(), oldPO.getName(), param.getRack(),
                        param.getShard(), param.getShardRouting(), retryCount);
                if (AriusObjUtils.isChanged(param.getRack(), oldPO.getRack())) {
                    tips = "模板部署rack变更!请注意模板APP是否可以使用修改后的rack";
                }
            }
            // 记录操作记录
            String editContent = AriusObjUtils.findChanged(oldPO, updateParam);
            if (StringUtils.isNotBlank(editContent)) {
                operateRecordService.save(TEMPLATE, EDIT, oldPO.getLogicId(),
                        "修改" + oldPO.getCluster() + "物理模板:" + editContent, operator);
            }

            SpringTool.publish(new PhysicalTemplateModifyEvent(this, ConvertUtil.obj2Obj(oldPO, IndexTemplatePhy.class),
                    templatePhyService.getTemplateById(oldPO.getId()),
                    templateLogicService.getLogicTemplateWithPhysicalsById(oldPO.getLogicId())));
        }

        return Result.buildWithTips(succ, tips);
    }

    @Override
    public Set<String> getIndexByBeforeDay(IndexTemplatePhyWithLogic physicalWithLogic, int days) {
        try {
            IndexTemplateLogic logicTemplate = physicalWithLogic.getLogicTemplate();

            if (!physicalWithLogic.getExpression().endsWith("*")) {
                return Sets.newHashSet();
            }

            if (!TemplateUtils.isSaveByDay(logicTemplate.getDateFormat())
                    && !TemplateUtils.isSaveByMonth(logicTemplate.getDateFormat())) {
                return Sets.newHashSet();
            }

            List<String> indices = templatePhyService.getMatchIndexNames(physicalWithLogic.getId());
            if (CollectionUtils.isEmpty(indices)) {
                LOGGER.info("method=getIndexByBeforeDay||template={}||msg=no match indices", logicTemplate.getName());
                return Sets.newHashSet();
            }

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
                            "method=getIndexByBeforeDay||template={}||indexName={}||msg=template parse index time fail",
                            logicTemplate.getName(), indexName);
                    continue;
                }

                if (TemplateUtils.isSaveByMonth(logicTemplate.getDateFormat())) {
                    // 需要将索引时间定为当月的最后一天 确保最后一天的数据能被保留到保存时长
                    indexTime = AriusDateUtils.getLastDayOfTheMonth(indexTime);
                }

                if (needOperateAhead(physicalWithLogic)) {
                    int aheadSeconds = ariusConfigInfoService.intSetting(ARIUS_COMMON_GROUP,
                            "operate.index.ahead.seconds", 2 * 60 * 60);
                    indexTime = AriusDateUtils.getBeforeSeconds(indexTime, aheadSeconds);
                }

                long timeIntervalDay = (System.currentTimeMillis() - indexTime.getTime()) / MILLIS_PER_DAY;
                if (timeIntervalDay < days) {
                    LOGGER.info(
                            "method=getIndexByBeforeDay||template={}||indexName={}||timeIntervalDay={}||msg=index not match",
                            logicTemplate.getName(), indexName, timeIntervalDay);
                    continue;
                }

                LOGGER.info("method=getIndexByBeforeDay||indexName={}||indexTime={}||timeIntervalDay={}", indexName,
                        indexTime, timeIntervalDay);

                finalIndexSet.add(indexName);
            }
            return finalIndexSet;
        } catch (Exception e) {
            LOGGER.warn("method=getIndexByBeforeDay||templateName={}||errMsg={}", physicalWithLogic.getName(),
                    e.getMessage(), e);
        }

        return Sets.newHashSet();
    }

    @Override
    public List<ConsoleTemplatePhyVO> getConsoleTemplatePhyVOS(IndexTemplatePhysicalDTO param, Integer appId) {

        List<ConsoleTemplatePhyVO> consoleTemplatePhyVOS = ConvertUtil.list2List(templatePhyService.getByCondt(param),
            ConsoleTemplatePhyVO.class);

        buildConsoleTemplatePhyVO(consoleTemplatePhyVOS, appId);

        return consoleTemplatePhyVOS;
    }

    @Override
    public List<ConsoleTemplatePhyVO> getConsoleTemplatePhyVOSFromCache(IndexTemplatePhysicalDTO param, Integer appId) {
        if (cacheSwitch.physicalTemplateCacheEnable()) {
            try {
                return consoleTemplatePhyVOSCache.get(appId,
                    () -> getConsoleTemplatePhyVOS(param, appId));
            } catch (ExecutionException e) {
                return getConsoleTemplatePhyVOS(param, appId);
            }
        }

        return getConsoleTemplatePhyVOS(param, appId);
    }

    /**************************************** private method ****************************************************/
    private void initParamWhenAdd(IndexTemplatePhysicalDTO param) {
        IndexTemplateLogic logic = templateLogicService.getLogicTemplateById(param.getLogicId());

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

    private Result checkUpgradeParam(TemplatePhysicalUpgradeDTO param) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("模板升版本信息为空");
        }
        if (AriusObjUtils.isNull(param.getPhysicalId())) {
            return Result.buildParamIllegal("物理模板id为空");
        }
        if (AriusObjUtils.isNull(param.getVersion())) {
            return Result.buildParamIllegal("物理模板版本为空");
        }

        TemplatePhysicalPO oldPO = indexTemplatePhysicalDAO.getById(param.getPhysicalId());
        if (oldPO == null) {
            return Result.buildNotExist("物理模板不存在");
        }
        if (Objects.equals(param.getVersion(), oldPO.getVersion())
                || (param.getVersion() > 0 && param.getVersion() < oldPO.getVersion())) {
            return Result.buildParamIllegal("物理模板版本非法");
        }
        if (param.getRack() != null) {
            if (!esClusterPhyService.isRacksExists(oldPO.getCluster(), param.getRack())) {
                return Result.buildParamIllegal("物理模板rack非法");
            }
        }
        if (param.getShard() != null) {
            if (param.getShard() < 1) {
                return Result.buildParamIllegal("shard个数非法");
            }
        }

        return Result.buildSucc();
    }

    private Result upgradeTemplateWithCheck(TemplatePhysicalUpgradeDTO param, String operator,
                                            int retryCount) throws ESOperateException {
        TemplatePhysicalPO physicalPO = indexTemplatePhysicalDAO.getById(param.getPhysicalId());
        if (templateLabelService.hasDeleteDoc(physicalPO.getLogicId())) {
            return Result.buildParamIllegal("模板有删除操作,禁止升版本");
        }

        IndexTemplateLogic logic = templateLogicService.getLogicTemplateById(physicalPO.getLogicId());

        if (TemplateUtils.isOnly1Index(logic.getExpression())) {
            return Result.buildParamIllegal("不是分区创建的索引，不能升版本");
        }

        LOGGER.info("upgradeTemplate||name={}||rack={}||shard={}||version={}", logic.getName(), param.getRack(),
                param.getShard(), param.getVersion());

        IndexTemplatePhysicalDTO updateParam = new IndexTemplatePhysicalDTO();
        updateParam.setId(physicalPO.getId());
        updateParam.setRack(param.getRack());
        updateParam.setShard(param.getShard());
        updateParam.setVersion(param.getVersion());
        Result editResult = editTemplateWithoutCheck(updateParam, operator, retryCount);

        if (editResult.failed()) {
            return editResult;
        }

        templatePreCreateManager.asyncCreateTodayAndTomorrowIndexByPhysicalId(physicalPO.getId(), 3);

        return Result.buildSucc();
    }

    private Result checkCopyParam(TemplatePhysicalCopyDTO param) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("复制参数为空");
        }
        if (AriusObjUtils.isNull(param.getPhysicalId())) {
            return Result.buildParamIllegal("物理模板id为空");
        }
        if (AriusObjUtils.isNull(param.getCluster())) {
            return Result.buildParamIllegal("目标集群为空");
        }
        if (AriusObjUtils.isNull(param.getShard())) {
            return Result.buildParamIllegal("shard为空");
        }

        TemplatePhysicalPO oldPO = indexTemplatePhysicalDAO.getById(param.getPhysicalId());
        if (oldPO == null) {
            return Result.buildNotExist("物理模板不存在");
        }

        if (!esClusterPhyService.isClusterExists(param.getCluster())) {
            return Result.buildNotExist("目标集群不存在");
        }

        if (oldPO.getCluster().equals(param.getCluster())) {
            return Result.buildParamIllegal("目标集群不能与源集群相同");
        }

        if (StringUtils.isNotEmpty(param.getRack())
                && !esClusterPhyService.isRacksExists(param.getCluster(), param.getRack())) {
            return Result.buildNotExist("rack不存在");
        }

        if (param.getShard() < 1) {
            return Result.buildParamIllegal("shard非法");
        }

        return Result.buildSucc();
    }

    private boolean needOperateAhead(IndexTemplatePhyWithLogic physicalWithLogic) {
        Set<String> clusterSet = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
                "delete.expire.index.ahead.clusters", "", ",");
        return clusterSet.contains(physicalWithLogic.getCluster());
    }

    private Result checkMetaInner(IndexTemplatePhy templatePhysical,
                                  Map<Integer, IndexTemplateLogic> logicId2IndexTemplateLogicMap,
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

    private int checkIndexCreateAndExpire(IndexTemplatePhy templatePhysical,
                                          Map<Integer, IndexTemplateLogic> logicId2IndexTemplateLogicMap) {
        int result = INDEX_OP_OK;
        if (templatePhysical.getCreateTime().before(AriusDateUtils.getZeroDate())) {
            Set<String> indices = Sets.newHashSet( templatePhyService.getMatchNoVersionIndexNames(templatePhysical.getId()));

            IndexTemplateLogic templateLogic = logicId2IndexTemplateLogicMap.get(templatePhysical.getLogicId());
            String tomorrowIndexName = IndexNameFactory.getNoVersion(templateLogic.getExpression(),
                    templateLogic.getDateFormat(), 1);
            String expireIndexName = IndexNameFactory.getNoVersion(templateLogic.getExpression(),
                    templateLogic.getDateFormat(), -1 * templateLogic.getExpireTime());

            if (!indices.contains(tomorrowIndexName)) {
                LOGGER.warn("method=checkIndexCreateAndExpire||cluster={}||template={}||msg=TOMORROW_INDEX_NOT_CREATE",
                        templatePhysical.getCluster(), templatePhysical.getName());
                result = result + TOMORROW_INDEX_NOT_CREATE;
            }

            if (TemplateUtils.isSaveByDay(templateLogic.getDateFormat()) && indices.contains(expireIndexName)) {
                LOGGER.warn("method=checkIndexCreateAndExpire||cluster={}||template={}||msg=EXPIRE_INDEX_NOT_DELETE",
                        templatePhysical.getCluster(), templatePhysical.getName());
                result = result + EXPIRE_INDEX_NOT_DELETE;
            }
        }
        return result;
    }

    private IndexTemplateLogicWithPhyTemplates buildIndexTemplateLogicWithPhysicalForNew(IndexTemplatePhysicalDTO param) {
        IndexTemplateLogicWithPhyTemplates logicWithPhysical = templateLogicService
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
        if (shardNum != null && shardNum > 0) {
            return true;
        }
        return false;
    }

    private void buildConsoleTemplatePhyVO(List<ConsoleTemplatePhyVO> params, Integer currentAppId) {
        
        Map<Integer, String> appId2AppNameMap = Maps.newHashMap();

        for (ConsoleTemplatePhyVO consoleTemplatePhyVO : params) {

            IndexTemplateLogic logicTemplate = templateLogicService.getLogicTemplateById(consoleTemplatePhyVO.getLogicId());
            if (AriusObjUtils.isNull(logicTemplate)) {
                LOGGER.error(
                        "class=TemplatePhyServiceImpl||method=buildConsoleTemplatePhyVO||errMsg=IndexTemplateLogic is empty||logicId={}",
                        consoleTemplatePhyVO.getLogicId());
                continue;
            }

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
                continue;
            }
            if (currentAppId.equals(appIdFromLogicTemplate)) {
                consoleTemplatePhyVO.setAuthType(AppTemplateAuthEnum.OWN.getCode());
            } else {
                AppTemplateAuthEnum authEnum = appLogicTemplateAuthService.getAuthEnumByAppIdAndLogicId(currentAppId,
                        appIdFromLogicTemplate);
                consoleTemplatePhyVO.setAuthType(authEnum.getCode());
            }

        }
    }
}
