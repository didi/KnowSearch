package com.didichuxing.datachannel.arius.admin.biz.template.deprecated.srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PreCreateManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private PreCreateManager preCreateManager;

    @Test
    public void preCreateIndexTest() {
        Integer logicTemplateId = 37519;
        Result<Void> result = preCreateManager.preCreateIndex(logicTemplateId);
        Assertions.assertTrue(result.success());
    }
}