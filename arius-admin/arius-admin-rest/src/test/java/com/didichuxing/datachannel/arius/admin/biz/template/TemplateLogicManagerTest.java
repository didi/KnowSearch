package com.didichuxing.datachannel.arius.admin.biz.template;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class TemplateLogicManagerTest extends AriusAdminApplicationTest {

    // @Test
    public void checkTemplateHotDaysSettingTest() {
        String logicClusterName = "";
        Integer totalSaveDays = 0;
        Integer hotSaveDays = 0;
    }
}
