package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.cold.ColdManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ColdManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private ColdManager coldManager;

    @Test
    public void move2ColdNodeTest() {
        Integer logicTemplateId = 37519;
        Result<Void> result = coldManager.move2ColdNode(logicTemplateId);
        Assertions.assertTrue(result.success());
    }
}