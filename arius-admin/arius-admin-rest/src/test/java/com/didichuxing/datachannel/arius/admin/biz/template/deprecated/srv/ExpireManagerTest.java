package com.didichuxing.datachannel.arius.admin.biz.template.deprecated.srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.expire.ExpireManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ExpireManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private ExpireManager expireManager;

    @Test
    public void deleteExpireIndexTest() {
        Integer logicTemplateId = 37519;
        Result<Void> result = expireManager.deleteExpireIndex(logicTemplateId);
        Assertions.assertTrue(result.success());
    }
}