package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.expire.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.expire.ExpireManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.SCHEDULE;

/**
 * @author chengxiang
 * @date 2022/5/12
 */
public class ExpireManagerImpl extends BaseTemplateSrvImpl implements ExpireManager {

    private final Integer RETRY_TIMES = 3;

    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_DEL_EXPIRE;
    }

    @Override
    public Result<Void> deleteExpireIndex(Integer logicTemplateId) {
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail("模板未开启过期删除服务");
        }

        return deleteNormalTemplateExpireIndex(logicTemplateId, RETRY_TIMES)
                && deleteDeletingTemplateExpireIndex(logicTemplateId, RETRY_TIMES);
    }

    @Override
    public Set<String> getExpireIndex(Long physicalId) {
        return new HashSet<>();
    }

    @Override
    public Result<Void> deleteTemplatePhyExpireIndex(Long physicalId) {
        return Result.buildSucc();
    }

    @Override
    public Result<Void> deleteTemplatePhyDeletedIndex(IndexTemplatePhy physical) {
        return Result.buildSucc();
    }

    /////////////////////////////private method/////////////////////////////////////////////
    private boolean deleteNormalTemplateExpireIndex(String cluster, int retryCount) {
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

        boolean succ = esIndexService.syncBatchDeleteIndices(cluster, shouldDels, retryCount) == shouldDels.size();
        if (succ) {
            List<String> shouldDelList = Lists.newArrayList(shouldDels);
            Result<Boolean> batchSetIndexFlagInvalidResult = indicesManager.batchSetIndexFlagInvalid(cluster, shouldDelList);
            if (batchSetIndexFlagInvalidResult.success()){
                operateRecordService.save(SCHEDULE, OperationEnum.DELETE, null,
                        String.format("根据模板过期时间删除过期索引：集群%s;索引:%s", cluster, ListUtils.strList2String(shouldDelList)),
                        AriusUser.SYSTEM.getDesc());
            }
        }

        return succ;
    }

    private boolean deleteDeletingTemplateExpireIndex(String cluster, int retryCount) {
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
            try {
                IndexTemplate templateLogic = indexTemplateService.getLogicTemplateWithPhysicalsById(physical.getLogicId());
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
            operateRecordService.save(SCHEDULE, OperationEnum.DELETE, null,
                    String.format("删除已删除模板关联的索引：集群%s; 模板%s", cluster, ListUtils.strList2String(indexTemplatePhyNameList)),
                    AriusUser.SYSTEM.getDesc());
        }

        return succ;
    }


}
