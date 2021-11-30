package com.didichuxing.datachannel.arius.admin.biz.gateway;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class GatewayManagerTest extends AriusAdminApplicationTests {
    @Autowired
    private GatewayManager gatewayManager;

    @Test
    public void directSqlSearchTest() {
        String sql = "SELECT * FROM arius.dsl.template LIMIT 10";
        Assertions.assertEquals("", gatewayManager.directSqlSearch(sql, 1));
    }
}
