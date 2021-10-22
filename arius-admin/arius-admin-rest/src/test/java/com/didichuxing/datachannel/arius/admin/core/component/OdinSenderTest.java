package com.didichuxing.datachannel.arius.admin.core.component;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.HOST_NAME;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.client.bean.common.OdinData;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminOdinTemplateMetricEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.google.common.collect.Lists;

/**
 * @author d06679
 * @date 2019-08-08
 */
public class OdinSenderTest extends AriusAdminApplicationTests {

    @Autowired
    private OdinSender odinSender;

    @Test
    public void send() {
    }

    @Test
    public void batchSend() {
        OdinData base = new OdinData();
        base.setTimestamp(System.currentTimeMillis() / 1000);
        base.setStep(15 * 60);
        base.putTag("host", HOST_NAME);
        base.putTag("logicId", "1");
        base.putTag(AdminOdinTemplateMetricEnum.metricTemplte(), "template");

        OdinData diskMsg = ConvertUtil.obj2Obj(base, OdinData.class);
        base.setName(AdminOdinTemplateMetricEnum.TEMPLATE_QUOTA_DISK_USAGE.getMetric());
        diskMsg.putTag("quota", "disk");
        diskMsg.setValue("50");

        OdinData cpuMsg = ConvertUtil.obj2Obj(base, OdinData.class);
        base.setName(AdminOdinTemplateMetricEnum.TEMPLATE_QUOTA_CPU_USAGE.getMetric());
        cpuMsg.putTag("quota", "cpu");
        cpuMsg.setValue("40");

        odinSender.batchSend(Lists.newArrayList(diskMsg, cpuMsg));

    }
}