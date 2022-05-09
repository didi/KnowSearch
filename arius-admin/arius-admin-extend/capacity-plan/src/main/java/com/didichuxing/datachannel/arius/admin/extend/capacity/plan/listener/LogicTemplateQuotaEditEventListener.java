package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.listener;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfo;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author d06679
 * @date 2019-08-04
 */
@Component
public class LogicTemplateQuotaEditEventListener implements ApplicationListener<LogicTemplateEvent> {

    private static final ILog         LOGGER = LogFactory.getLog(LogicTemplateQuotaEditEventListener.class);

    @Autowired
    private CapacityPlanRegionService capacityPlanRegionService;

    @Autowired
    private IndexTemplateInfoService indexTemplateInfoService;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(LogicTemplateEvent event) {
        if (event instanceof LogicTemplateModifyEvent) {
            IndexTemplateInfoWithPhyTemplates newTemplateWithPhysical = indexTemplateInfoService.getLogicTemplateWithPhysicalsById(((LogicTemplateModifyEvent) event).getOldTemplate().getId());

            if (CollectionUtils.isNotEmpty(newTemplateWithPhysical.getPhysicals())) {
                double deltaQuota = (((LogicTemplateModifyEvent) event).getOldTemplate().getQuota()
                                     - newTemplateWithPhysical.getQuota())
                                    / newTemplateWithPhysical.getPhysicals().size();
                for (IndexTemplatePhyInfo physical : newTemplateWithPhysical.getPhysicals()) {
                    CapacityPlanRegion region = capacityPlanRegionService.getRegionOfPhyTemplate(physical);
                    if (region != null) {
                        LOGGER.info("class=LogicTemplateQuotaEditEventListener||method=onApplicationEvent||region={}||deltaQuota={}||msg=LogicTemplateModifyEvent",
                            region, deltaQuota);
                        capacityPlanRegionService.editRegionFreeQuota(region.getRegionId(),
                            region.getFreeQuota() + deltaQuota);
                    }
                }

            }

        }
    }
}
