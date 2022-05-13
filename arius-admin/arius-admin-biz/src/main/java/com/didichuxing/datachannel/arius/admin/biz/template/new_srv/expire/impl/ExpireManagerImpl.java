package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.expire.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.expire.ExpireManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePhyPO;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhyDAO;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.PLATFORM_DELETED_TEMPLATE_EXPIRED_TIME;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.PLATFORM_EXPIRE_TIME_MIN;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.SCHEDULE;

/**
 * @author chengxiang, zqr
 * @date 2022/5/12
 */
@Service
public class ExpireManagerImpl extends BaseTemplateSrvImpl implements ExpireManager {

    private final Integer RETRY_TIMES = 3;

    @Autowired
    private ESIndexService esIndexService;

    @Autowired
    private IndicesManager indicesManager;

    @Autowired
    private IndexTemplatePhyDAO indexTemplatePhyDAO;

    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_DEL_EXPIRE;
    }

    @Override
    public Result<Void> deleteExpireIndex(Integer logicTemplateId) {
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail("模板未开启过期删除服务");
        }

        Boolean succ = deleteNormalTemplateExpireIndex(logicTemplateId, RETRY_TIMES) && deleteDeletingTemplateExpireIndex(logicTemplateId);
        return succ ? Result.buildSucc() : Result.buildFail("删除过期索引失败");
    }

    /////////////////////////////private method/////////////////////////////////////////////

    private Boolean deleteNormalTemplateExpireIndex(Integer logicTemplateId, int retryCount) {
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicIdAndStatus(logicTemplateId, TemplatePhysicalStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isEmpty(templatePhyList)) {
            LOGGER.info("class=ExpireManagerImpl||method=deleteNormalTemplateExpireIndex||logicTemplateId={}||msg=no physical template", logicTemplateId);
            return Boolean.TRUE;
        }

        Multimap<String, String> cluster2ShouldDeleteIndex = ArrayListMultimap.create();
        for (IndexTemplatePhy templatePhy: templatePhyList) {
            try {
                Long templatePhyId = templatePhy.getId();
                Tuple<String, Set<String>> expireIndexTuple = getExpireIndex(templatePhyId);
                if (null == expireIndexTuple) {
                    LOGGER.info("class=ExpireManagerImpl||method=deleteNormalTemplateExpireIndex||templatePhyId={}||msg=no expire index", templatePhyId);
                    continue;
                }

                cluster2ShouldDeleteIndex.putAll(expireIndexTuple.getV1(), expireIndexTuple.getV2());
            } catch (Exception e) {
                LOGGER.warn("class=ExpireManagerImpl||method=deleteNormalTemplateExpireIndex||templatePhyId={}||errMsg={}", templatePhy.getId(), e.getMessage());
            }
        }

        if (cluster2ShouldDeleteIndex.isEmpty()) {
            LOGGER.info("class=ExpireManagerImpl||method=deleteNormalTemplateExpireIndex||logicTemplateId={}||msg=no expire index", logicTemplateId);
            return Boolean.TRUE;
        }

        Boolean succ = Boolean.TRUE;
        for (String cluster: cluster2ShouldDeleteIndex.keySet()) {
            List<String> shouldDeleteIndexList = new ArrayList<>(cluster2ShouldDeleteIndex.get(cluster));
            if (CollectionUtils.isEmpty(shouldDeleteIndexList)) {
                LOGGER.info("class=ExpireManagerImpl||method=deleteNormalTemplateExpireIndex||cluster={}||msg=no expire index", cluster);
                continue;
            }

            succ = succ && esIndexService.syncBatchDeleteIndices(cluster, shouldDeleteIndexList, retryCount) == shouldDeleteIndexList.size();
            succ = succ && indicesManager.batchSetIndexFlagInvalid(cluster, shouldDeleteIndexList).success();
        }
        return succ;
    }

    private boolean deleteDeletingTemplateExpireIndex(Integer logicTemplateId) {
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicIdAndStatus(logicTemplateId, TemplatePhysicalStatusEnum.INDEX_DELETING.getCode());
        if (CollectionUtils.isEmpty(templatePhyList)) {
            LOGGER.info("class=ExpireManagerImpl||method=deleteDeletingTemplateExpireIndex||logicTemplateId={}||msg=no deleting template", logicTemplateId);
            return true;
        }

        Boolean succ = Boolean.TRUE;
        IndexTemplate template = indexTemplateService.getLogicTemplateById(logicTemplateId);
        for (IndexTemplatePhy templatePhy: templatePhyList) {
            try {
                if (null != template) {
                    succ = succ && deleteTemplatePhyExpireIndex(templatePhy.getId());
                } else {
                    succ = succ && deleteTemplatePhyDeletedIndex(templatePhy);
                }
            } catch (Exception e) {
                succ = Boolean.FALSE;
                LOGGER.warn("class=ExpireManagerImpl||method=deleteDeletingTemplateExpireIndex||cluster={}||msg={}", templatePhy.getCluster(), e.getMessage(), e);
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
            LOGGER.warn("class=ExpireManagerImpl||method=getExpireIndex||msg=getExpireIndexByTemplate expire time illegal||template={}", logicTemplate.getName());
        }

        if (expireTime <= 0) {
            if (!TemplatePhysicalStatusEnum.NORMAL.equals(TemplatePhysicalStatusEnum.valueOf(templatePhysicalWithLogic.getStatus()))) {
                // 对于已经删除的物理索引模板，我们需要重新设置其过期时间
                expireTime = PLATFORM_DELETED_TEMPLATE_EXPIRED_TIME;
                LOGGER.info("class=ExpireManagerImpl||method=getExpireIndex||msg=method=getExpireIndex||msg=reset template expire time||template={}", logicTemplate.getName());
            } else {
                LOGGER.info("class=ExpireManagerImpl||method=getExpireIndex||msg=getExpireIndexByTemplate no expire||template={}", logicTemplate.getName());
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

        Set<String> expireIndex = getExpireIndex(physicalId).getV2();
        if (CollectionUtils.isEmpty(expireIndex)) {
            finishDeleteIndex(physicalId);
            return Boolean.TRUE;
        }

        Boolean succ = expireIndex.size() == esIndexService.syncBatchDeleteIndices(templatePhy.getCluster(), expireIndex, RETRY_TIMES);
        if (succ) {
            indicesManager.batchSetIndexFlagInvalid(templatePhy.getCluster(), Lists.newArrayList(expireIndex));
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

        List<IndexTemplatePhyPO> physicalPOs = indexTemplatePhyDAO
                .getByClusterAndNameAndStatus(physical.getCluster(), physical.getName(), TemplatePhysicalStatusEnum.NORMAL.getCode());

        //如果还有正常状态的物理模板存在，那就把非正常状态的物理模板给清理掉
        if(CollectionUtils.isNotEmpty(physicalPOs)){
            deleteTemplateNuNormalStatusFromDB(physical);
            return Boolean.TRUE;
        }

        physicalPOs.addAll(indexTemplatePhyDAO.getByClusterAndStatus(physical.getCluster(),
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
                LOGGER.info("class=ExpireManagerImpl||method=deleteTemplateDeletedIndices||msg=processLogicDeleted hasConflict||deletedTemplate={}||conflictTemplate={}", physical.getName(), po.getName());
                deleteCode = 1;
                break;
            }
        }

        if (deleteCode == 1) {
            LOGGER.warn("class=ExpireManagerImpl||method=deleteTemplateDeletedIndices||msg=processLogicDeleted has conflict||deletedTemplate={}||expression={}", physical.getName(), physical.getExpression());
            return false;
        }

        Boolean succ = Boolean.TRUE;
        if (deleteCode == 0) {
            LOGGER.info("class=ExpireManagerImpl||method=deleteTemplateDeletedIndices||msg=processLogicDeleted no conflict and not reCreate||deletedTemplate={}||expression={}", physical.getName(), physical.getExpression());
            Set<String> shouldDelSet = esIndexService.syncGetIndexNameByExpression(physical.getCluster(), physical.getExpression());
            if (CollectionUtils.isNotEmpty(shouldDelSet)) {
                try {
                    succ = esIndexService.syncDeleteIndexByExpression(physical.getCluster(), physical.getExpression(), RETRY_TIMES);
                } catch (Exception e) {
                    LOGGER.error("class=ExpireManagerImpl||method=deleteTemplateDeletedIndices||msg=processLogicDeleted delete index error||deletedTemplate={}||expression={}", physical.getName(), physical.getExpression(), e);
                    succ = Boolean.FALSE;
                }
                if (succ) {
                    //批量设置存储索引cat/index信息的元数据索引中的文档标志位（deleteFlag）为true
                    indicesManager.batchSetIndexFlagInvalid(physical.getCluster(), Lists.newArrayList(shouldDelSet));
                }
            }
        }

        if (succ) {
            //修改模板的状态为已删除
            finishDeleteIndex(physical.getId());
        }
        return succ;
    }


    private boolean deleteTemplateNuNormalStatusFromDB(IndexTemplatePhy physical){
        return indexTemplatePhyDAO.deleteDirtyByClusterAndName(physical.getCluster(), physical.getName()) > 0;
    }

    /**
     * 模板数据删除完成
     * @param physicalId 物理模板id
     * @return true、false
     */
    private boolean finishDeleteIndex(Long physicalId) {
        return 1 == indexTemplatePhyDAO.updateStatus(physicalId, TemplatePhysicalStatusEnum.DELETED.getCode());
    }

    //todo: 这个好恶心，去除掉，优化代码架构
    private IndexTemplatePhyWithLogic getNormalAndDeletingTemplateWithLogicById(Long physicalId) {
        IndexTemplatePhyPO physicalPO = indexTemplatePhyDAO.getNormalAndDeletingById(physicalId);
        return indexTemplatePhyService.buildIndexTemplatePhysicalWithLogic(physicalPO);
    }

}
