package com.didichuxing.datachannel.arius.admin.biz.indices;

import com.didichuxing.datachannel.arius.admin.rest.AriusAdminApplication;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Authoer: zyl
 * @Date: 2022/07/11
 * @Version: 1.0
 */

@Transactional
@Rollback
public class IndicesManagerTest extends AriusAdminApplication {

    @Autowired
    private IndicesManager indicesManager;

    @Test
    public void getTemplateIndicesDiskSumTest(){
        Long templateIndicesDiskSum = indicesManager.getTemplateIndicesDiskSum(24053);
        Assertions.assertEquals(templateIndicesDiskSum, 522);
    }
}
