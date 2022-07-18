package com.didichuxing.datachannel.arius.admin.biz.template.srv.expire.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.PLATFORM_DELETED_TEMPLATE_EXPIRED_TIME;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.PLATFORM_EXPIRE_TIME_MIN;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.expire.ExpireManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePhyPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.IndexCatInfoCollector;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author chengxiang, zqr
 * @date 2022/5/12
 */
@Service
public class ExpireManagerImpl extends BaseTemplateSrvImpl implements ExpireManager {

    private final Integer         RETRY_TIMES = 3;

    @Autowired
    private ESIndexService        esIndexService;

    @Autowired
    private IndexCatInfoCollector indexCatInfoCollector;
    @Autowired
    private ESIndexCatService     esIndexCatService;

    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_DEL_EXPIRE;
    }

    @Override
    public Result<Void> deleteExpireIndex(Integer logicTemplateId) {
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail("模板未开启过期删除服务");
        }

        Boolean succ = deleteNormalTemplateExpireIndex(logicTemplateId, RETRY_TIMES)
                       && deleteDeletingTemplateExpireIndex(logicTemplateId);
        return succ ? Result.buildSucc() : Result.buildFail("删除过期索引失败");
    }

    /////////////////////////////private method/////////////////////////////////////////////

    private Boolean deleteNormalTemplateExpireIndex(Integer logicTemplateId, int retryCount) {
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicIdAndStatus(logicTemplateId,
            TemplatePhysicalStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isEmpty(templatePhyList)) {
            LOGGER.info(
                "class=ExpireManagerImpl||method=deleteNormalTemplateExpireIndex||logicTemplateId={}||msg=no physical template",
                logicTemplateId);
            return Boolean.TRUE;
        }

        Multimap<String, String> cluster2ShouldDeleteIndex = ArrayListMultimap.create();
        for (IndexTemplatePhy templatePhy : templatePhyList) {
            try {
                Long templatePhyId = templatePhy.getId();
                Tuple<String, Set<String>> expireIndexTuple = getExpireIndex(templatePhyId);
                if (null == expireIndexTuple) {
                    LOGGER.info(
                        "class=ExpireManagerImpl||method=deleteNormalTemplateExpireIndex||templatePhyId={}||msg=no expire index",
                        templatePhyId);
                    continue;
                }

                cluster2ShouldDeleteIndex.putAll(expireIndexTuple.getV1(), expireIndexTuple.getV2());
            } catch (Exception e) {
                LOGGER.warn(
                    "class=ExpireManagerImpl||method=deleteNormalTemplateExpireIndex||templatePhyId={}||errMsg={}",
                    templatePhy.getId(), e.getMessage());
            }
        }

        if (cluster2ShouldDeleteIndex.isEmpty()) {
            LOGGER.info(
                "class=ExpireManagerImpl||method=deleteNormalTemplateExpireIndex||logicTemplateId={}||msg=no expire index",
                logicTemplateId);
            return Boolean.TRUE;
        }

        Boolean succ = Boolean.TRUE;
        for (String cluster : cluster2ShouldDeleteIndex.keySet()) {
            List<String> shouldDeleteIndexList = new ArrayList<>(cluster2ShouldDeleteIndex.get(cluster));
            if (CollectionUtils.isEmpty(shouldDeleteIndexList)) {
                LOGGER.info(
                    "class=ExpireManagerImpl||method=deleteNormalTemplateExpireIndex||cluster={}||msg=no expire index",
                    cluster);
                continue;
            }

            succ = succ && esIndexService.syncBatchDeleteIndices(cluster, shouldDeleteIndexList,
                retryCount) == shouldDeleteIndexList.size();
            succ = succ && updateIndexFlagInvalid(cluster, shouldDeleteIndexList).success();
        }
        return succ;
    }

    private Boolean deleteDeletingTemplateExpireIndex(Integer logicTemplateId) {
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicIdAndStatus(logicTemplateId,
            TemplatePhysicalStatusEnum.INDEX_DELETING.getCode());
        if (CollectionUtils.isEmpty(templatePhyList)) {
            LOGGER.info(
                "class=ExpireManagerImpl||method=deleteDeletingTemplateExpireIndex||logicTemplateId={}||msg=no deleting template",
                logicTemplateId);
            return true;
        }

        Boolean succ = Boolean.TRUE;
        IndexTemplate template = indexTemplateService.getLogicTemplateById(logicTemplateId);
        for (IndexTemplatePhy templatePhy : templatePhyList) {
            try {
                if (null != template) {
                    succ = succ && deleteTemplatePhyExpireIndex(templatePhy.getId());
                } else {
                    succ = succ && deleteTemplatePhyDeletedIndex(templatePhy);
                }
            } catch (Exception e) {
                succ = Boolean.FALSE;
                LOGGER.warn("class=ExpireManagerImpl||method=deleteDeletingTemplateExpireIndex||cluster={}||msg={}",
                    templatePhy.getCluster(), e.getMessage(), e);
            }
        }
        return succ;
    }

    /**
     * 获取模板过期的索引
     * @param physicalId 模板物理ID
     * @return <cluster, expireIndex>
     */
    private Tuple<String, Set<String>> getExpireIndex(Long physicalId) {
        IndexTemplatePhyWithLogic templatePhysicalWithLogic = getNormalAndDeletingTemplateWithLogicById(physicalId);
        if (templatePhysicalWithLogic == null) {
            return null;
        }

        IndexTemplate logicTemplate = templatePhysicalWithLogic.getLogicTemplate();
        int expireTime = logicTemplate.getExpireTime();
        if (expireTime > 0 && expireTime < PLATFORM_EXPIRE_TIME_MIN) {
            expireTime = PLATFORM_EXPIRE_TIME_MIN;
            LOGGER.warn(
                "class=ExpireManagerImpl||method=getExpireIndex||msg=getExpireIndexByTemplate expire time illegal||template={}",
                logicTemplate.getName());
        }

        if (expireTime <= 0) {
            if (!TemplatePhysicalStatusEnum.NORMAL
                .equals(TemplatePhysicalStatusEnum.valueOf(templatePhysicalWithLogic.getStatus()))) {
                // 对于已经删除的物理索引模板，我们需要重新设置其过期时间
                expireTime = PLATFORM_DELETED_TEMPLATE_EXPIRED_TIME;
                LOGGER.info(
                    "class=ExpireManagerImpl||method=getExpireIndex||msg=method=getExpireIndex||msg=reset template expire time||template={}",
                    logicTemplate.getName());
            } else {
                LOGGER.info(
                    "class=ExpireManagerImpl||method=getExpireIndex||msg=getExpireIndexByTemplate no expire||template={}",
                    logicTemplate.getName());
                return null;
            }
        }

        Set<String> expireIndex = templatePhyManager.getIndexByBeforeDay(templatePhysicalWithLogic, expireTime);
        return new Tuple<>(templatePhysicalWithLogic.getCluster(), expireIndex);
    }

    /**
     * 删除模板过期索引
     *  1、可以是当前集群存在的物理模板
     *  2、可以是已经从当前集群迁移走的模板,但是还有数据在当前集群
     * @param physicalId 物理模板id
     * @return true/false
     */
    private Boolean deleteTemplatePhyExpireIndex(Long physicalId) {
        IndexTemplatePhy templatePhy = getNormalAndDeletingTemplateWithLogicById(physicalId);
        if (templatePhy == null) {
            return Boolean.TRUE;
        }

        Set<String> expireIndex = Optional.ofNullable(getExpireIndex(physicalId)).map(Tuple::getV2).orElse(null);
        if (CollectionUtils.isEmpty(expireIndex)) {
            finishDeleteIndex(physicalId);
            return Boolean.TRUE;
        }

        Boolean succ = expireIndex.size() == esIndexService.syncBatchDeleteIndices(templatePhy.getCluster(),
            expireIndex, RETRY_TIMES);
        if (succ) {
            updateIndexFlagInvalid(templatePhy.getCluster(), Lists.newArrayList(expireIndex));
        }
        return succ;
    }

    /**
     * 删除已经被删除的模板的索引
     * @param physical 物理模板信息
     * @return true/false
     */
    private Boolean deleteTemplatePhyDeletedIndex(IndexTemplatePhy physical) {
        if (!isTemplateSrvOpen(physical.getLogicId())) {
            return Boolean.FALSE;
        }

        List<IndexTemplatePhyPO> physicalPOs = indexTemplatePhyService.getByClusterAndNameAndStatus(
            physical.getCluster(), physical.getName(), TemplatePhysicalStatusEnum.NORMAL.getCode());

        //如果还有正常状态的物理模板存在，那就把非正常状态的物理模板给清理掉
        if (CollectionUtils.isNotEmpty(physicalPOs)) {
            deleteTemplateNuNormalStatusFromDB(physical);
            return Boolean.TRUE;
        }
        physicalPOs.addAll(indexTemplatePhyService.getByClusterAndStatus(physical.getCluster(),
            TemplatePhysicalStatusEnum.INDEX_DELETING.getCode()));

        // 0：noConflict
        // 1：hasConflict
        // 2：reCreate
        int deleteCode = 0;
        for (IndexTemplatePhyPO po : physicalPOs) {
            if (po.getId().equals(physical.getId())) {
                continue;
            }

            if (physical.getExpression().equals(po.getExpression())) {
                deleteCode = 2;
                break;
            }

            String expressionPre = physical.getExpression().replace("*", "");
            if (po.getExpression().startsWith(expressionPre)) {
                LOGGER.info(
                    "class=ExpireManagerImpl||method=deleteTemplateDeletedIndices||msg=processLogicDeleted hasConflict||deletedTemplate={}||conflictTemplate={}",
                    physical.getName(), po.getName());
                deleteCode = 1;
                break;
            }
        }

        if (deleteCode == 1) {
            LOGGER.warn(
                "class=ExpireManagerImpl||method=deleteTemplateDeletedIndices||msg=processLogicDeleted has conflict||deletedTemplate={}||expression={}",
                physical.getName(), physical.getExpression());
            return false;
        }

        Boolean succ = Boolean.TRUE;
        if (deleteCode == 0) {
            LOGGER.info(
                "class=ExpireManagerImpl||method=deleteTemplateDeletedIndices||msg=processLogicDeleted no conflict and not reCreate||deletedTemplate={}||expression={}",
                physical.getName(), physical.getExpression());
            Set<String> shouldDelSet = esIndexService.syncGetIndexNameByExpression(physical.getCluster(),
                physical.getExpression());
            if (CollectionUtils.isNotEmpty(shouldDelSet)) {
                try {
                    succ = esIndexService.syncDeleteIndexByExpression(physical.getCluster(), physical.getExpression(),
                        RETRY_TIMES);
                } catch (Exception e) {
                    LOGGER.error(
                        "class=ExpireManagerImpl||method=deleteTemplateDeletedIndices||msg=processLogicDeleted delete index error||deletedTemplate={}||expression={}",
                        physical.getName(), physical.getExpression(), e);
                    succ = Boolean.FALSE;
                }
                if (succ) {
                    //批量设置存储索引cat/index信息的元数据索引中的文档标志位（deleteFlag）为true
                    updateIndexFlagInvalid(physical.getCluster(), Lists.newArrayList(shouldDelSet));
                }
            }
        }

        if (succ) {
            //修改模板的状态为已删除
            finishDeleteIndex(physical.getId());
        }
        return succ;
    }

    private Result<Boolean> updateIndexFlagInvalid(String cluster, List<String> indexNameList) {
        //不采集已删除索引
        indexCatInfoCollector.updateNotCollectorIndexNames(cluster, indexNameList);
        //更新存储cat/index信息的元信息索引中对应文档删除标识位为true
        boolean succ = indexNameList.size() == esIndexCatService.syncUpdateCatIndexDeleteFlag(cluster, indexNameList,
            3);
        if (!succ) {
            LOGGER.error(
                "class=ExpireManagerImpl||method=batchSetIndexFlagInvalid||cluster={}||indexNameList={}||errMsg=failed to batchSetIndexFlagInvalid",
                cluster, ListUtils.strList2String(indexNameList));
        }
        return Result.build(succ);
    }

    private boolean deleteTemplateNuNormalStatusFromDB(IndexTemplatePhy physical) {
        return indexTemplatePhyService.deleteDirtyByClusterAndName(physical.getCluster(), physical.getName());
    }

    /**
     * 模板数据删除完成
     * @param physicalId 物理模板id
     * @return true、false
     */
    private boolean finishDeleteIndex(Long physicalId) {
        return indexTemplatePhyService.updateStatus(physicalId, TemplatePhysicalStatusEnum.DELETED.getCode());
    }

    private IndexTemplatePhyWithLogic getNormalAndDeletingTemplateWithLogicById(Long physicalId) {
        return indexTemplatePhyService.buildIndexTemplatePhysicalWithLogicByPhysicalId(physicalId);
    }
    ///////////////////////////////////////////srv

    /**
     * 删除模板过期索引
     * 1、可以是当前集群存在的物理模板
     * 2、可以是已经从当前集群迁移走的模板,但是还有数据在当前集群
     *
     * @param physicalId 物理模板id
     * @param retryCount 重试次数
     * @return true/false
     */
    @Override
    public boolean deleteExpireIndices(Long physicalId, int retryCount) {
        IndexTemplatePhy templatePhysical = getNormalAndDeletingTemplateWithLogicById(physicalId);
        if (templatePhysical == null) {
            return true;
        }

        Set<String> shouldDels = Optional.ofNullable(getExpireIndex(physicalId)).map(Tuple::getV2)
            .orElse(Sets.newHashSet());

        LOGGER.error("class=ExpireManagerImpl||method=deleteExpireIndices||physicalId={}||logicId={}||status={}"
                     + "||name={}||shouldDeleteIndices={}",
            physicalId, templatePhysical.getLogicId(), templatePhysical.getStatus(), templatePhysical.getName(),
            JSON.toJSONString(shouldDels));

        if (CollectionUtils.isEmpty(shouldDels)) {
            if (templatePhysical.getStatus().equals(TemplatePhysicalStatusEnum.INDEX_DELETING.getCode())) {
                finishDeleteIndex(physicalId);
            }
            return true;
        }

        boolean succ = shouldDels.size() == esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(),
            shouldDels, retryCount);

        if (succ) {
            updateIndexFlagInvalid(templatePhysical.getCluster(), Lists.newArrayList(shouldDels));
        }

        return succ;
    }

    /**
     * 删除已经被删除的模板的索引
     *
     * @param physical   物理模板信息
     * @param retryCount 重试次数
     * @return true/false
     * @throws ESOperateException
     */
    @Override
    public boolean deleteTemplateDeletedIndices(IndexTemplatePhy physical, int retryCount) throws ESOperateException {
        if (!isTemplateSrvOpen(physical.getLogicId())) {
            return false;
        }

        List<IndexTemplatePhyPO> physicalPOs =

                indexTemplatePhyService.getByClusterAndNameAndStatus(physical.getCluster(), physical.getName(),
                    TemplatePhysicalStatusEnum.NORMAL.getCode());

        //如果还有正常状态的物理模板存在，那就把非正常状态的物理模板给清理掉
        if (CollectionUtils.isNotEmpty(physicalPOs)) {
            deleteTemplateNuNormalStatusFromDB(physical);
            return true;
        }

        physicalPOs.addAll(indexTemplatePhyService.getByClusterAndStatus(physical.getCluster(),
            TemplatePhysicalStatusEnum.INDEX_DELETING.getCode()));

        // 0：noConflict
        // 1：hasConflict
        // 2：reCreate
        int deleteCode = 0;
        for (IndexTemplatePhyPO po : physicalPOs) {
            if (po.getId().equals(physical.getId())) {
                continue;
            }

            if (physical.getExpression().equals(po.getExpression())) {
                deleteCode = 2;
                break;
            }

            String expressionPre = physical.getExpression().replace("*", "");
            if (po.getExpression().startsWith(expressionPre)) {
                LOGGER.info(
                    "class=BaseTemplateSrv||method=deleteTemplateDeletedIndices||msg=processLogicDeleted hasConflict||deletedTemplate={}||conflictTemplate={}",
                    physical.getName(), po.getName());
                deleteCode = 1;
                break;
            }
        }

        if (deleteCode == 1) {
            LOGGER.warn(
                "class=BaseTemplateSrv||method=deleteTemplateDeletedIndices||msg=processLogicDeleted has conflict||deletedTemplate={}||expression={}",
                physical.getName(), physical.getExpression());
            return false;
        }

        boolean succ = true;
        if (deleteCode == 0) {
            LOGGER.info(
                "class=BaseTemplateSrv||method=deleteTemplateDeletedIndices||msg=processLogicDeleted no conflict and not reCreate||deletedTemplate={}||expression={}",
                physical.getName(), physical.getExpression());
            Set<String> shouldDelSet = esIndexService.syncGetIndexNameByExpression(physical.getCluster(),
                physical.getExpression());
            if (CollectionUtils.isNotEmpty(shouldDelSet)) {
                succ = esIndexService.syncDeleteIndexByExpression(physical.getCluster(), physical.getExpression(),
                    retryCount);
                if (succ) {
                    //批量设置存储索引cat/index信息的元数据索引中的文档标志位（deleteFlag）为true
                    updateIndexFlagInvalid(physical.getCluster(), Lists.newArrayList(shouldDelSet));
                }
            }
        }

        if (succ) {
            //修改模板的状态为已删除
            finishDeleteIndex(physical.getId());
        }

        return succ;
    }

    /**
     * @param cluster 集群
     * @return
     */
    @Override
    public boolean deleteExpireIndex(String cluster) {
        //集群侧不存在判断
        //if (!isTemplateSrvOpen(cluster)) {
        //    return false;
        //}

        int retryCount = 5;
        return deleteNormalTemplateExpireIndexByCluster(cluster, retryCount)
               && deleteDeletingTemplateExpireIndexByCluster(cluster, retryCount);
    }

    /**************************************** private method ****************************************************/

    private boolean deleteNormalTemplateExpireIndexByCluster(String cluster, int retryCount) {
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getTemplateByClusterAndStatus(cluster,
            TemplatePhysicalStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=deleteNormalTemplateExpireIndex||cluster={}||msg=cluster no template",
                cluster);
            return true;
        }

        Set<String> shouldDels = Sets.newHashSet();
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            //集群移动到模版侧
            if (Boolean.FALSE.equals(isTemplateSrvOpen(templatePhysical.getLogicId()))){
                continue;
            }
            try {
                shouldDels.addAll(getExpireIndexByPhysicalId(templatePhysical.getId()));
            } catch (Exception e) {
                LOGGER.warn(
                    "class=ESClusterPhyServiceImpl||method=deleteNormalTemplateExpireIndex||cluster={}||errMsg={}",
                    cluster, e.getMessage());
            }
        }

        if (CollectionUtils.isEmpty(shouldDels)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=deleteNormalTemplateExpireIndex||cluster={}||msg=no expire index",
                cluster);
            return true;
        }

        boolean succ = esIndexService.syncBatchDeleteIndices(cluster, shouldDels, retryCount) == shouldDels.size();
        if (succ) {
            List<String> shouldDelList = Lists.newArrayList(shouldDels);
            Result<Boolean> batchSetIndexFlagInvalidResult = updateIndexFlagInvalid(cluster, shouldDelList);
            if (batchSetIndexFlagInvalidResult.success()) {
                operateRecordService.save(new OperateRecord.Builder()

                    .content(
                        String.format("根据模板过期时间删除过期索引：集群%s;索引:%s", cluster, ListUtils.strList2String(shouldDelList)))
                    .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                    .operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_DELETE)
                    .triggerWayEnum(TriggerWayEnum.SCHEDULING_TASKS).userOperation(AriusUser.SYSTEM.getDesc()).build());

            }
        }

        return succ;
    }

    public Set<String> getExpireIndexByPhysicalId(Long physicalId) {

        IndexTemplatePhyWithLogic templatePhysicalWithLogic = getNormalAndDeletingTemplateWithLogicById(physicalId);
        if (templatePhysicalWithLogic == null) {
            return Sets.newHashSet();
        }

        IndexTemplate logicTemplate = templatePhysicalWithLogic.getLogicTemplate();

        int expireTime = logicTemplate.getExpireTime();

        if (expireTime > 0 && expireTime < PLATFORM_EXPIRE_TIME_MIN) {
            expireTime = PLATFORM_EXPIRE_TIME_MIN;
            LOGGER.warn(
                "class=BaseTemplateSrv||method= getExpireIndex||msg=getExpireIndexByTemplate expire time illegal||template={}",
                logicTemplate.getName());
        }

        if (expireTime <= 0) {
            if (!TemplatePhysicalStatusEnum.NORMAL
                .equals(TemplatePhysicalStatusEnum.valueOf(templatePhysicalWithLogic.getStatus()))) {
                // 对于已经删除的物理索引模板，我们需要重新设置其过期时间
                expireTime = PLATFORM_DELETED_TEMPLATE_EXPIRED_TIME;

                LOGGER.info(
                    "class=BaseTemplateSrv||method= getExpireIndex||msg=method=getExpireIndex||msg=reset template expire time||template={}",
                    logicTemplate.getName());
            } else {
                LOGGER.info(
                    "class=BaseTemplateSrv||method= getExpireIndex||msg=getExpireIndexByTemplate no expire||template={}",
                    logicTemplate.getName());
                return Sets.newHashSet();
            }

        }

        return templatePhyManager.getIndexByBeforeDay(templatePhysicalWithLogic, expireTime);
    }

    private boolean deleteDeletingTemplateExpireIndexByCluster(String cluster, int retryCount) {
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getTemplateByClusterAndStatus(cluster,
            TemplatePhysicalStatusEnum.INDEX_DELETING.getCode());
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=deleteDeletingTemplateExpireIndex||cluster={}||msg=cluster no deleting template",
                cluster);
            return true;
        }

        boolean succ = true;
        for (IndexTemplatePhy physical : templatePhysicals) {
            //没有开启就跳过
            if (Boolean.FALSE.equals(isTemplateSrvOpen(physical.getLogicId()))){
                continue;
            }
            try {
                IndexTemplate templateLogic = indexTemplateService
                    .getLogicTemplateWithPhysicalsById(physical.getLogicId());
                
                if (templateLogic != null) {
                    succ = deleteExpireIndices(physical.getId(), retryCount) && succ;
                } else {
                    succ = deleteTemplateDeletedIndices(physical, retryCount) && succ;
                }
            } catch (Exception e) {
                succ = false;
                LOGGER.warn(
                    "class=ESClusterPhyServiceImpl||method=deleteDeletingTemplateExpireIndex||template={}||msg={}",
                    physical.getName(), e.getMessage(), e);
            }

        }

        if (succ) {
            List<String> indexTemplatePhyNameList = templatePhysicals.stream().map(IndexTemplatePhy::getName)
                .collect(Collectors.toList());
            operateRecordService.save(new OperateRecord.Builder()

                .content(String.format("删除已删除模板关联的索引：集群%s; 模板%s", cluster,
                    ListUtils.strList2String(indexTemplatePhyNameList)))
                .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                .operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_DELETE).triggerWayEnum(TriggerWayEnum.SCHEDULING_TASKS)
                .userOperation(AriusUser.SYSTEM.getDesc()).build());

        }

        return succ;
    }

}