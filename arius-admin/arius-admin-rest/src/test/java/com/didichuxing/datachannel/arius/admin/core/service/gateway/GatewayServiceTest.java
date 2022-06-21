package com.didichuxing.datachannel.arius.admin.core.service.gateway;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.gatewayHeartbeatFactory;

import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.impl.GatewayServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterNodeDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author wuxuan
 * @Date 2022/5/31
 */
public class GatewayServiceTest {

    GatewayService gatewayService = new GatewayServiceImpl();
    //通过Mockito.mock()构造对应的mock对象
    GatewayClusterDAO gatewayClusterDAO = Mockito.mock(GatewayClusterDAO.class);
    GatewayClusterNodeDAO gatewayClusterNodeDAO = Mockito.mock(GatewayClusterNodeDAO.class);
    Set<String> clusterNames = new HashSet<>();
    ESGatewayClient esGatewayClient = Mockito.mock(ESGatewayClient.class);
    @BeforeEach
    public void init(){
        ReflectionTestUtils.setField(gatewayService, "gatewayClusterNodeDAO", gatewayClusterNodeDAO);
        ReflectionTestUtils.setField(gatewayService,"gatewayClusterDAO",gatewayClusterDAO);
        clusterNames.add("admin");
        //通过反射机制实现对应的对象中参数的赋值
        ReflectionTestUtils.setField(gatewayService,"clusterNames",clusterNames);
        //ReflectionTestUtils.setField(gatewayService,"appService",appService);
        ReflectionTestUtils.setField(gatewayService,"esGatewayClient",esGatewayClient);
    }

    @Test
    void heartbeatTest() {
        // 生成新的记录
        GatewayHeartbeat gatewayHeartbeat = gatewayHeartbeatFactory();
        // 设置不同的空参数使能够遍历checkResult.failed()所包含的所有分支
        gatewayHeartbeat.setClusterName(null);
        Assertions.assertEquals("参数错误:cluster name is null，请检查后再提交！",gatewayService.heartbeat(gatewayHeartbeat).getMessage());
        gatewayHeartbeat.setClusterName(CustomDataSource.PHY_CLUSTER_NAME);
        gatewayHeartbeat.setHostName(null);
        Assertions.assertEquals("参数错误:host name is null，请检查后再提交！",gatewayService.heartbeat(gatewayHeartbeat).getMessage());
        gatewayHeartbeat.setHostName("admin");
        gatewayHeartbeat.setPort(-1);
        Assertions.assertEquals("参数错误:port illegal，请检查后再提交！", gatewayService.heartbeat(gatewayHeartbeat).getMessage());
        //通过设置recordHeartbeat(heartbeat)返回的值小于0，实现Result.buildFail("save db fail");
        gatewayHeartbeat = gatewayHeartbeatFactory();
        Mockito.when(gatewayClusterNodeDAO.recordGatewayNode(Mockito.any())).thenReturn(-1);
        Assertions.assertEquals("save db fail",gatewayService.heartbeat(gatewayHeartbeat).getMessage());
        // 记录的正确执行操作
        //通过Mockito.when().thenReturn()返回所需要的与数据库关联的对象的返回值
        Mockito.when(gatewayClusterDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(gatewayClusterNodeDAO.recordGatewayNode(Mockito.any())).thenReturn(1);
        Assertions.assertTrue(gatewayService.heartbeat(gatewayHeartbeat).success());
    }

    @Test
    void aliveCountTest() {
        // 遍历到所有的异常处理的分支
        long gapTime = -1;
        Assertions.assertEquals("参数错误:gapTime name illegal，请检查后再提交！",gatewayService.aliveCount(CustomDataSource.PHY_CLUSTER_NAME,gapTime).getMessage());
        gapTime = 10000;
        Assertions.assertEquals("参数错误:cluster name is null，请检查后再提交！",gatewayService.aliveCount(null,gapTime).getMessage());
        // 执行正确的记录的执行
        ReflectionTestUtils.setField(gatewayService, "gatewayClusterNodeDAO", gatewayClusterNodeDAO);
        Mockito.when(gatewayClusterNodeDAO.aliveCountByClusterNameAndTime(Mockito.anyString(),Mockito.any())).thenReturn(1);
        Assertions.assertTrue(gatewayService.aliveCount(CustomDataSource.PHY_CLUSTER_NAME,gapTime).success());
    }

    @Test
    void sqlOperateTest() {
        //设置不同参数遍历preSqlParamCheck的fail分支(String sql, String phyClusterName, Integer projectId, String postFix)
        int projectId = 1;
        String sql = "show databases";
        Assertions.assertEquals("参数错误:查询的sql语句为空，请检查后再提交！",gatewayService.sqlOperate("",
                CustomDataSource.PHY_CLUSTER_NAME,null,"postFix").getMessage());
        Assertions.assertEquals("参数错误:查询gateway的路径后缀为空，请检查后再提交！",gatewayService.sqlOperate(sql,CustomDataSource.PHY_CLUSTER_NAME,null,null).getMessage());
        Assertions.assertEquals("参数错误:对应的projectId字段非法，请检查后再提交！", gatewayService.sqlOperate(sql,
                CustomDataSource.PHY_CLUSTER_NAME,null,"postFix").getMessage());
        //App app = new App();
        //app.setId(projectId);
        //List<App> apps = new ArrayList<>();
        //apps.add(app);
        //Mockito.when(appService.listApps()).thenReturn(apps);
        //Mockito.when(appService.getAppById(Mockito.anyInt())).thenReturn(app);
        Mockito.when(esGatewayClient.getSingleGatewayAddress()).thenReturn("10.190.32.30");
        Assertions.assertFalse(gatewayService.sqlOperate("sql",CustomDataSource.PHY_CLUSTER_NAME,null,"postFix").success());
    }

    @Test
    void getAliveNodeTest() {
        Mockito.when(gatewayClusterDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(gatewayClusterNodeDAO.recordGatewayNode(Mockito.any())).thenReturn(1);
        Mockito.when(gatewayClusterNodeDAO.listAliveNodeByClusterNameAndTime(Mockito.any(), Mockito.any())).thenReturn(CustomDataSource.getGatewayNodePOList());
        Assertions.assertFalse(gatewayService.getAliveNode(CustomDataSource.PHY_CLUSTER_NAME, 1000).isEmpty());
    }
}