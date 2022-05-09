package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.listener;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplateInfoPO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateInfoDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateEvent;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author d06679
 * @date 2019-08-04
 */
@Component
public class PhysicalTemplateNewOrDelEventListener implements ApplicationListener<PhysicalTemplateEvent> {

    private static final ILog         LOGGER = LogFactory.getLog(PhysicalTemplateNewOrDelEventListener.class);

    @Autowired
    private CapacityPlanRegionService capacityPlanRegionService;

    @Autowired
    private IndexTemplateInfoDAO indexTemplateInfoDAO;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(PhysicalTemplateEvent event) {
        Double deltaQuota = getDeltaQuota(event);
        IndexTemplatePhy templatePhysical = getOpTemplate(event);
        if (deltaQuota != 0.0 && templatePhysical != null) {
            LOGGER.info("class=PhysicalTemplateNewOrDelEventListener||method=onApplicationEvent||logicId={}||physicalCluster={}||templateName={}||deltaQuota={}",
                    templatePhysical.getLogicId(), templatePhysical.getCluster(), templatePhysical.getName(), deltaQuota);
            updateRegionQuota(templatePhysical, deltaQuota);
            updateLogicTemplateQuota(templatePhysical.getLogicId(), deltaQuota);
        }

    }

    /***************************************** private method ****************************************************/
    /**
     * 更新逻辑模板Quota
     * @param logicId 逻辑模板ID
     * @param deltaQuota Quota增量
     */
    private void updateLogicTemplateQuota(Integer logicId, Double deltaQuota) {
        IndexTemplateInfoPO logicPO = indexTemplateInfoDAO.getById(logicId);
        if (logicPO != null) {
            logicPO.setQuota(logicPO.getQuota() + deltaQuota);
            if (indexTemplateInfoDAO.update(logicPO) == 0) {
                LOGGER.error("class=PhysicalTemplateNewOrDelEventListener||method=updateLogicTemplateQuota||errMsg=updateTemplateQuotaFail||logicId={}||targetQuota={}||deltaQuota={}",
                        logicPO.getId(), logicPO.getQuota(), deltaQuota);
            }
        }
    }

    /**
     * 更新Region  Quota信息
     * @param templatePhysical 物理模板
     * @param deltaQuota 增量Quota
     */
    private void updateRegionQuota(IndexTemplatePhy templatePhysical, Double deltaQuota) {
        CapacityPlanRegion region = capacityPlanRegionService.getRegionOfPhyTemplate(templatePhysical);
        if (region != null) {
            LOGGER.info("class=PhysicalTemplateNewOrDelEventListener||method=onApplicationEvent||region={}||deltaQuota={}||msg=PhysicalTemplateAddEvent", region,
                    deltaQuota);
            capacityPlanRegionService.editRegionFreeQuota(region.getRegionId(), region.getFreeQuota() - deltaQuota);
        }
    }

    private IndexTemplatePhy getOpTemplate(PhysicalTemplateEvent event) {
        if (event instanceof PhysicalTemplateAddEvent) {
            return ((PhysicalTemplateAddEvent) event).getNewTemplate();
        } else if (event instanceof PhysicalTemplateDeleteEvent) {
            return ((PhysicalTemplateDeleteEvent) event).getDelTemplate();
        }

        return null;
    }

    /**
     * 由于目前的物理模板更新策略为更更新记录，再更新Quota
     * 所以使得创建物理模板的时候更新Quota之前对应的物理模板已经创建好了；
     * 删除物理模板的时候，对应的物理模板已经删除了。
     * 所以，再具体更新Quota前，如果是新增物理模板，需要先把对应的物理模板数量减一；
     * 删除逻辑模板的时候，需要把对应的物理模板数量加一，这样算出来的Quota值才是合理值。
     * @param event 更新事件
     * @return
     */
    private Double getDeltaQuota(PhysicalTemplateEvent event) {
        IndexTemplateInfoWithPhyTemplates logicWithPhysical = event.getLogicWithPhysical();
        if (logicWithPhysical.hasPhysicals()) {
            long currentPhysicalSize = logicWithPhysical.getPhysicals().size();

            if (event instanceof PhysicalTemplateAddEvent) {
                if (currentPhysicalSize > 1) {
                    currentPhysicalSize = currentPhysicalSize - 1;
                    return logicWithPhysical.getQuota() / currentPhysicalSize;
                }
            } else if (event instanceof PhysicalTemplateDeleteEvent) {
                return -1 * logicWithPhysical.getQuota() / ( currentPhysicalSize + 1);
            }
        }

        return 0.0;
    }
}
