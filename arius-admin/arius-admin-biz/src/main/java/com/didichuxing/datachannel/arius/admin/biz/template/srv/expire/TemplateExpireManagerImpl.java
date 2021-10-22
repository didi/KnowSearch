package com.didichuxing.datachannel.arius.admin.biz.template.srv.expire;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplatePhysicalPO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhysicalDAO;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.PLATFORM_DELETED_TEMPLATE_EXPIRED_TIME;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.PLATFORM_EXPIRE_TIME_MIN;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_DEL_EXPIRE;

/**
 * 索引过期服务实现
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateExpireManagerImpl extends BaseTemplateSrv implements TemplateExpireManager {

    @Autowired
    private ESIndexService           esIndexService;

    @Autowired
    private IndexTemplatePhysicalDAO indexTemplatePhysicalDAO;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_DEL_EXPIRE;
    }

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

        Set<String> shouldDels = getExpireIndex(physicalId);

        LOGGER
            .error("class=TemplatePhyServiceImpl||method=deleteExpireIndices||physicalId={}||logicId={}||status={}"
                   + "||name={}||shouldDeleteIndices={}",
                physicalId, templatePhysical.getLogicId(), templatePhysical.getStatus(), templatePhysical.getName(),
                JSON.toJSONString(shouldDels));

        if (CollectionUtils.isEmpty(shouldDels)) {
            if (templatePhysical.getStatus().equals(TemplatePhysicalStatusEnum.INDEX_DELETING.getCode())) {
                finishDeleteIndex(physicalId);
            }
            return true;
        }

        return shouldDels.size() == esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(), shouldDels,
            retryCount);
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
        if (!isTemplateSrvOpen(physical.getCluster())) {
            return false;
        }

        List<TemplatePhysicalPO> physicalPOs = indexTemplatePhysicalDAO
                .getByClusterAndStatus(physical.getCluster(), TemplatePhysicalStatusEnum.NORMAL.getCode());

        physicalPOs.addAll(indexTemplatePhysicalDAO.getByClusterAndStatus(physical.getCluster(),
                TemplatePhysicalStatusEnum.INDEX_DELETING.getCode()));

        // 0：noConflict
        // 1：hasConflict
        // 2：reCreate
        int deleteCode = 0;
        for (TemplatePhysicalPO po : physicalPOs) {
            if (po.getId().equals(physical.getId())) {
                continue;
            }

            if (physical.getExpression().equals(po.getExpression())) {
                deleteCode = 2;
                break;
            }

            String expressionPre = physical.getExpression().replace("*", "");
            if (po.getExpression().startsWith(expressionPre)) {
                LOGGER.info("processLogicDeleted hasConflict||deletedTemplate={}||conflictTemplate={}",
                    physical.getName(), po.getName());
                deleteCode = 1;
                break;
            }
        }

        if (deleteCode == 1) {
            LOGGER.warn("processLogicDeleted has conflict||deletedTemplate={}||expression={}", physical.getName(),
                physical.getExpression());
            return false;
        }

        boolean succ = true;
        if (deleteCode == 0) {
            LOGGER.info("processLogicDeleted no conflict and not reCreate||deletedTemplate={}||expression={}",
                physical.getName(), physical.getExpression());
            succ = esIndexService.syncDeleteIndexByExpression(physical.getCluster(), physical.getExpression(),
                retryCount);
        }

        if (succ) {
            //修改模板的状态为已删除
            finishDeleteIndex(physical.getId());
        }

        return succ;
    }

    @Override
    public boolean deleteExpireIndex(String cluster) {
        if (!isTemplateSrvOpen(cluster)) {
            return false;
        }

        int retryCount = 5;

        return deleteNormalTemplateExpireIndex(cluster, retryCount)
               && deleteDeletingTemplateExpireIndex(cluster, retryCount);
    }

    /**
     * 获取模板过期的索引
     *
     * @param physicalId 模板物理ID
     * @return set集合
     */
    @Override
    public Set<String> getExpireIndex(Long physicalId) {

        IndexTemplatePhyWithLogic templatePhysicalWithLogic = getNormalAndDeletingTemplateWithLogicById(physicalId);
        if (templatePhysicalWithLogic == null) {
            return Sets.newHashSet();
        }

        IndexTemplateLogic logicTemplate = templatePhysicalWithLogic.getLogicTemplate();

        int expireTime = logicTemplate.getExpireTime();

        if (expireTime > 0 && expireTime < PLATFORM_EXPIRE_TIME_MIN) {
            expireTime = PLATFORM_EXPIRE_TIME_MIN;
            LOGGER.warn("getExpireIndexByTemplate expire time illegal||template={}", logicTemplate.getName());
        }

        if (expireTime <= 0) {
            if (!TemplatePhysicalStatusEnum.NORMAL
                .equals(TemplatePhysicalStatusEnum.valueOf(templatePhysicalWithLogic.getStatus()))) {
                // 对于已经删除的物理索引模板，我们需要重新设置其过期时间
                expireTime = PLATFORM_DELETED_TEMPLATE_EXPIRED_TIME;

                LOGGER.info("method=getExpireIndex||msg=reset template expire time||template={}",
                    logicTemplate.getName());
            } else {
                LOGGER.info("getExpireIndexByTemplate no expire||template={}", logicTemplate.getName());
                return Sets.newHashSet();
            }

        }

        return templatePhyManager.getIndexByBeforeDay(templatePhysicalWithLogic, expireTime);
    }

    /**************************************** private method ****************************************************/

    private boolean deleteNormalTemplateExpireIndex(String cluster, int retryCount) {
        List<IndexTemplatePhy> templatePhysicals = templatePhyService.getTemplateByClusterAndStatus(cluster,
            TemplatePhysicalStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=deleteNormalTemplateExpireIndex||cluster={}||msg=cluster no template",
                cluster);
            return true;
        }

        Set<String> shouldDels = Sets.newHashSet();
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                shouldDels.addAll(getExpireIndex(templatePhysical.getId()));
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

        return esIndexService.syncBatchDeleteIndices(cluster, shouldDels, retryCount) == shouldDels.size();
    }

    private boolean deleteDeletingTemplateExpireIndex(String cluster, int retryCount) {
        List<IndexTemplatePhy> templatePhysicals = templatePhyService.getTemplateByClusterAndStatus(cluster,
            TemplatePhysicalStatusEnum.INDEX_DELETING.getCode());
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=deleteDeletingTemplateExpireIndex||cluster={}||msg=cluster no deleting template",
                cluster);
            return true;
        }

        boolean succ = true;
        for (IndexTemplatePhy physical : templatePhysicals) {
            try {
                IndexTemplateLogic templateLogic = templateLogicService
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
        return succ;
    }

    private IndexTemplatePhyWithLogic getNormalAndDeletingTemplateWithLogicById(Long physicalId) {
        TemplatePhysicalPO physicalPO = indexTemplatePhysicalDAO.getNormalAndDeletingById(physicalId);
        return templatePhyService.buildIndexTemplatePhysicalWithLogic(physicalPO);
    }

    /**
     * 模板数据删除完成
     *
     * @param physicalId 物理模板id
     * @return true、false
     */
    private boolean finishDeleteIndex(Long physicalId) {
        return 1 == indexTemplatePhysicalDAO.updateStatus(physicalId, TemplatePhysicalStatusEnum.DELETED.getCode());
    }
}
