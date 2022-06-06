package com.didichuxing.datachannel.arius.admin.biz.template.new_srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UpgradeManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private TemplateLogicManager templateLogicManager;

    @Test
    public void upgradeTemplateTest() {
        Integer templateId = 37519;
        Result<Void> result = templateLogicManager.upgrade(templateId, "admin");
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result.success());
    }
}
