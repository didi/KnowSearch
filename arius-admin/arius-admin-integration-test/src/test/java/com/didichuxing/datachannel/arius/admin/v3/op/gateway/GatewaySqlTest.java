package com.didichuxing.datachannel.arius.admin.v3.op.gateway;

import com.didichuxing.datachannel.arius.admin.base.BasePhyClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.method.v3.op.gateway.GatewaySqlControllerMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author cjm
 */
public class GatewaySqlTest extends BasePhyClusterInfoTest {

    @Test
    public void directSqlSearchTest() throws IOException {
        String sql = "SELECT * FROM my_index";
        Result<String> result = GatewaySqlControllerMethod.directSqlSearchTest(phyClusterInfo.getPhyClusterName(), sql);
        Assertions.assertTrue(result.success());
    }

    public void sqlExplainTest() throws IOException {
        String sql = "SELECT * FROM my_index";
        Result<String> result = GatewaySqlControllerMethod.sqlExplainTest(sql);
        Assertions.assertTrue(result.success());
    }
}
