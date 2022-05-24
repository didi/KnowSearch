package com.didichuxing.datachannel.arius.admin.biz.template.new_srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.indexplan.IndexPlanManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class IndexPlanManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private IndexPlanManager indexPlanManager;

    @Test
    public void indexRolloverTest() {
        Integer logicTemplateId = 37519;
        indexPlanManager.indexRollover(logicTemplateId);
    }
}
