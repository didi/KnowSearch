package com.didichuxing.datachannel.arius.admin.core.service.gateway;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.PHY_CLUSTER_NAME;
import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.gatewayHeartbeatFactory;

import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayNodeDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class GatewayServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private GatewayService gatewayService;

    @MockBean
    private GatewayClusterDAO gatewayClusterDAO;

    @MockBean
    private GatewayNodeDAO gatewayNodeDAO;

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
        Mockito.when(gatewayClusterDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(gatewayNodeDAO.recordGatewayNode(Mockito.any())).thenReturn(1);
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
        GatewayHeartbeat gatewayHeartbeat = new GatewayHeartbeat();
        Assertions.assertTrue(gatewayService.heartbeat(gatewayHeartbeat).failed());
        gatewayHeartbeat.setClusterName(CustomDataSource.PHY_CLUSTER_NAME);
        Assertions.assertTrue(gatewayService.heartbeat(gatewayHeartbeat).failed());
        gatewayHeartbeat.setHostName("localhost");
        Assertions.assertTrue(gatewayService.heartbeat(gatewayHeartbeat).failed());
        gatewayHeartbeat.setPort(8080);
        Assertions.assertTrue(gatewayService.heartbeat(gatewayHeartbeat).failed());
        gatewayHeartbeat = gatewayHeartbeatFactory();
        Mockito.when(gatewayClusterDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(gatewayNodeDAO.recordGatewayNode(Mockito.any())).thenReturn(1);
        Mockito.when(gatewayNodeDAO.listAliveNodeByClusterNameAndTime(Mockito.any(), Mockito.any()))
                .thenReturn(CustomDataSource.getGatewayNodePOList());
        Assertions.assertFalse(gatewayService.getAliveNode(CustomDataSource.PHY_CLUSTER_NAME, 1000).isEmpty());
    }
}
