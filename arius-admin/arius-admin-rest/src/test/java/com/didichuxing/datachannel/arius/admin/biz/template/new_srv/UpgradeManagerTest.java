package com.didichuxing.datachannel.arius.admin.biz.template.new_srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.upgrade.UpgradeManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UpgradeManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private UpgradeManager upgradeManager;

    @Test
    public void upgradeTemplateTest() {
        Integer templateId = 37519;
        Result<Void> result = upgradeManager.upgradeTemplate(templateId);
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result.success());
    }
}
