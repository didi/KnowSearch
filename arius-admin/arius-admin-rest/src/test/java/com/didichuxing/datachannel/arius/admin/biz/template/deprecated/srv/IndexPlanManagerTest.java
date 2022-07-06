package com.didichuxing.datachannel.arius.admin.biz.template.deprecated.srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.indexplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class IndexPlanManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private IndexPlanManager indexPlanManager;

    @Test
    public void indexRolloverTest() {
        Integer logicTemplateId = 37519;
        Result<Void> result = indexPlanManager.indexRollover(logicTemplateId);
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result.success());
    }
}