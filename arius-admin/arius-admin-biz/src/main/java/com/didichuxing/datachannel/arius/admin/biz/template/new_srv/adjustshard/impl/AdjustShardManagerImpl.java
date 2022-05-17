package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.adjustshard.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.adjustshard.AdjustShardManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.indexplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author chengxiang
 * @date 2022/5/17
 */
public class AdjustShardManagerImpl extends BaseTemplateSrvImpl implements AdjustShardManager {

    private final Integer RETRY_TIMES = 3;

    @Autowired
    private IndexPlanManager indexPlanManager;

    @Autowired
    private ESTemplateService esTemplateService;

    @Override
    public NewTemplateSrvEnum templateSrv() {
        return NewTemplateSrvEnum.TEMPLATE_ADJUST_SHARD;
    }

    @Override
    public Result<Void> isTemplateSrvAvailable(Integer logicTemplateId) {
        return Result.buildSucc();
    }

    @Override
    public Result<Void> adjustShard(Integer logicTemplateId, Integer shardNum) {
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);
        try {
            IndexTemplatePhyDTO updateParam = new IndexTemplatePhyDTO();
            for (IndexTemplatePhy templatePhy : templatePhyList) {
                if (templatePhy.getShard().equals(shardNum)) {
                    return Result.buildParamIllegal("该模板已经是" + shardNum + "分片");
                }

                updateParam.setId(templatePhy.getId());
                updateParam.setShard(shardNum);
                indexPlanManager.initShardRoutingAndAdjustShard(updateParam );

                Result<Void> updateDBResult = indexTemplatePhyService.update(updateParam);
                if (updateDBResult.failed()) {
                    return updateDBResult;
                }
                esTemplateService.syncUpdateRackAndShard(templatePhy.getCluster(), templatePhy.getName(), templatePhy.getRack(),
                        updateParam.getShard(), updateParam.getShardRouting(), RETRY_TIMES);

                SpringTool.publish(new PhysicalTemplateModifyEvent(this, templatePhy,
                        indexTemplatePhyService.getTemplateById(templatePhy.getId()),
                        indexTemplateService.getLogicTemplateWithPhysicalsById(logicTemplateId)));
            }
        } catch (Exception e) {
            LOGGER.error("adjustShard error", e);
            return Result.buildFail("模板扩缩容失败");
        }
        return Result.buildSucc();
    }

}
