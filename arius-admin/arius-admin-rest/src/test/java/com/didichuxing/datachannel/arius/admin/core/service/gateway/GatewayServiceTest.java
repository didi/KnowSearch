package com.didichuxing.datachannel.arius.admin.core.service.gateway;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway.GatewayNode;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayNodeDAO;
import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.gatewayHeartbeatFactory;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Rollback
public class GatewayServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private GatewayService gatewayService;

    @Test
    void heartbeatTest() {
        // 生成新的记录
        GatewayHeartbeat gatewayHeartbeat = gatewayHeartbeatFactory();

        // 设置不同的属性使能够遍历私有方法checkHeartbeat的所有的分支
        gatewayHeartbeat.setClusterName(null);
        Assertions.assertEquals(ResultType.ILLEGAL_PARAMS.getCode(), gatewayService.heartbeat(gatewayHeartbeat).getCode().intValue());
        gatewayHeartbeat.setHostName(null);
        Assertions.assertEquals(ResultType.ILLEGAL_PARAMS.getCode(), gatewayService.heartbeat(gatewayHeartbeat).getCode().intValue());
        gatewayHeartbeat.setHostName(null);
        Assertions.assertEquals(ResultType.ILLEGAL_PARAMS.getCode(), gatewayService.heartbeat(gatewayHeartbeat).getCode().intValue());
        gatewayHeartbeat.setPort(-1);
        Assertions.assertEquals(ResultType.ILLEGAL_PARAMS.getCode(), gatewayService.heartbeat(gatewayHeartbeat).getCode().intValue());

        // 记录的正确执行操作
        gatewayHeartbeat = gatewayHeartbeatFactory();
        Assertions.assertTrue(gatewayService.heartbeat(gatewayHeartbeat).success());

    }

    @Test
    void aliveCountTest() {
        // 生成新的记录
        GatewayHeartbeat gatewayHeartbeat = gatewayHeartbeatFactory();
        gatewayService.heartbeat(gatewayHeartbeat);

        // 遍历到所有的异常处理的分支
        long gapTime = -1;
        Assertions.assertEquals(ResultType.ILLEGAL_PARAMS.getCode(), gatewayService.aliveCount(null,gapTime).getCode().intValue());
        Assertions.assertEquals(ResultType.ILLEGAL_PARAMS.getCode(), gatewayService.aliveCount(gatewayHeartbeat.getClusterName(),gapTime).getCode().intValue());

        // 执行正确的记录的执行
        gapTime = 10000;
        Assertions.assertTrue(gatewayService.aliveCount(gatewayHeartbeat.getClusterName(),gapTime).success());
    }

    @Test
    void getAliveNodeTest() {
        // 生成新的记录
        GatewayHeartbeat gatewayHeartbeat = gatewayHeartbeatFactory();
        Assertions.assertTrue(gatewayService.heartbeat(gatewayHeartbeat).success());
    }
}
