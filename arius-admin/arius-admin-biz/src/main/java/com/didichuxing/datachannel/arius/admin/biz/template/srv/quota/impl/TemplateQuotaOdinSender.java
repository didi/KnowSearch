package com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.HOST_NAME;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.OdinData;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.LogicTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminOdinTemplateMetricEnum;
import com.didichuxing.datachannel.arius.admin.common.event.quota.TemplateQuotaEvent;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.OdinSender;

@Component
public class TemplateQuotaOdinSender implements ApplicationListener<TemplateQuotaEvent> {
    @Autowired
    private OdinSender       odinSender;

    private static final int ODIN_STEP = 15 * 60;

    @Override
    public void onApplicationEvent(TemplateQuotaEvent event) {
        if (null == event) {
            return;
        }

        LogicTemplateQuotaUsage usage = event.getTemplateQuotaUsage();

        OdinData base = new OdinData();
        base.setTimestamp(System.currentTimeMillis() / 1000);
        base.setStep(ODIN_STEP);
        base.putTag("host", HOST_NAME);
        base.putTag("logicId", String.valueOf(usage.getLogicId()));
        base.putTag(AdminOdinTemplateMetricEnum.metricTemplte(), usage.getTemplate());

        List<OdinData> odinDataList = new ArrayList<>();
        if ((usage.getQuotaDiskG() == 0 ? 0 : usage.getActualDiskG() / usage.getQuotaDiskG()) > 0.8) {
            OdinData diskMsg = ConvertUtil.obj2Obj(base, OdinData.class);
            diskMsg.setName(AdminOdinTemplateMetricEnum.TEMPLATE_QUOTA_DISK_USAGE_80.getMetric());
            diskMsg.setValue("1");
            odinDataList.add(diskMsg);
        }

        if ((usage.getQuotaCpuCount() == 0 ? 0 : usage.getActualCpuCount() / usage.getQuotaCpuCount()) > 0.8) {
            OdinData cpuData = ConvertUtil.obj2Obj(base, OdinData.class);
            cpuData.setName(AdminOdinTemplateMetricEnum.TEMPLATE_QUOTA_CPU_USAGE_80.getMetric());
            cpuData.setValue("1");
            odinDataList.add(cpuData);
        }

        odinSender.batchSend(odinDataList);
    }
}
