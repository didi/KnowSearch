package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.upgrade.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.upgrade.UpgradeManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.BaseTemplateSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chengxiang
 * @date 2022/5/16
 */
@Service
public class UpgradeManagerImpl extends BaseTemplateSrvImpl implements UpgradeManager {

    private final Integer RETRY_TIMES = 3;
    private final String OPERATOR = "admin";

    @Autowired
    private PreCreateManager preCreateManager;

    @Override
    public NewTemplateSrvEnum templateSrv() {
        return NewTemplateSrvEnum.TEMPLATE_UPGRADE;
    }

    @Override
    public Result<Void> isTemplateSrvAvailable(Integer logicTemplateId) {
        IndexTemplate template = indexTemplateService.getLogicTemplateById(logicTemplateId);
        if (TemplateUtils.isOnly1Index(template.getExpression())) {
            return Result.buildParamIllegal("不是分区创建的索引，不能升级版本");
        }

        return Result.buildSucc();
    }

    @Override
    protected Result<Void> openSrvImpl(List<Integer> templateIdList, BaseTemplateSrvOpenDTO openParam) {
        for (Integer templateId : templateIdList) {
            Result<Void> upgradeResult = upgradeTemplate(templateId);
            if (upgradeResult.failed()) {
                return upgradeResult;
            }
        }
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> closeSrvImpl(List<Integer> templateIdList) {
        return Result.buildSucc();
    }

    @Override
    public Result<Void> upgradeTemplate(Integer logicTemplateId) {
        Result<Void> srvAvailableResult = isTemplateSrvAvailable(logicTemplateId);
        if (srvAvailableResult.failed()) {
            return srvAvailableResult;
        }

        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);
        try {
            IndexTemplatePhyDTO updateParam = new IndexTemplatePhyDTO();
            for (IndexTemplatePhy templatePhy : templatePhyList) {
                updateParam.setId(templatePhy.getId());
                updateParam.setRack(templatePhy.getRack());
                updateParam.setShard(updateParam.getShard());
                updateParam.setVersion(templatePhy.getVersion() + 1);

                Result<Void> editResult = templatePhyManager.editTemplateWithoutCheck(updateParam, OPERATOR, RETRY_TIMES);
                if (editResult.failed()) {
                    return editResult;
                }

                preCreateManager.asyncCreateTodayAndTomorrowIndexByPhysicalId(templatePhy.getId());
            }
        } catch (Exception e) {
            LOGGER.error("upgrade template error", e);
        }

        return Result.buildSucc();
    }

}
