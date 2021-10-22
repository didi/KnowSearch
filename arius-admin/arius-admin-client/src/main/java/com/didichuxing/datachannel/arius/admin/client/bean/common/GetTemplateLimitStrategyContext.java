package com.didichuxing.datachannel.arius.admin.client.bean.common;

import java.util.List;

import lombok.Data;

@Data
public class GetTemplateLimitStrategyContext extends GetTemplateQuotaUsageContext {

    private PhysicalTemplateTpsMetric physicalTemplateTpsMetric;

    private List<RackMetaMetric>      rackMetaMetrics;

    public boolean isRateLimited() {

        if (physicalTemplateTpsMetric == null) {
            return false;
        }

        if (physicalTemplateTpsMetric.getCurrentTps() == null
            || physicalTemplateTpsMetric.getCurrentFailCount() == null) {
            return false;
        }

        return physicalTemplateTpsMetric.getCurrentFailCount() / physicalTemplateTpsMetric.getCurrentTps() > 0.01;

    }

}
