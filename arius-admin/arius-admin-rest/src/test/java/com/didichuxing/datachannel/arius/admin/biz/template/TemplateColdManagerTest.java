package com.didichuxing.datachannel.arius.admin.biz.template;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.TemplateColdManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class TemplateColdManagerTest extends AriusAdminApplicationTest {
    @Autowired
    private TemplateColdManager templateColdManager;

    // @Test
    public void coldDataMove() {
        String cluster = "cold_hot_test";
        Result<Boolean> booleanResult = templateColdManager.move2ColdNode(cluster);
        System.out.println(booleanResult);
    }
}
