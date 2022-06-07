package com.didichuxing.datachannel.arius.admin.core.service.gateway;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.gatewayHeartbeatFactory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.impl.GatewayServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterNodeDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * @author wuxuan
 * @Date 2022/5/31
 */
public class GatewayServiceTest {

    //通过@Mock构造对应的mock对象
    @Mock
    private GatewayClusterDAO gatewayClusterDAO ;
    @Mock
    private GatewayClusterNodeDAO gatewayClusterNodeDAO;
    @Mock
    private Set<String> clusterNames;
    @Mock
    private AppService appService ;
    @Mock
    private ESGatewayClient esGatewayClient ;
    @InjectMocks
    private GatewayServiceImpl gatewayService;

    @BeforeEach
    void setUp() {
        initMocks(this);
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
        when(gatewayClusterNodeDAO.recordGatewayNode(Mockito.any())).thenReturn(-1);
        Assertions.assertEquals("save db fail",gatewayService.heartbeat(gatewayHeartbeat).getMessage());
        // 记录的正确执行操作
        //通过Mockito.when().thenReturn()返回所需要的与数据库关联的对象的返回值
        when(gatewayClusterDAO.insert(Mockito.any())).thenReturn(1);
        when(gatewayClusterNodeDAO.recordGatewayNode(Mockito.any())).thenReturn(1);
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
        when(gatewayClusterNodeDAO.aliveCountByClusterNameAndTime(Mockito.anyString(),Mockito.any())).thenReturn(1);
        Assertions.assertTrue(gatewayService.aliveCount(CustomDataSource.PHY_CLUSTER_NAME,gapTime).success());
    }

    @Test
    void sqlOperateTest() {
        int appid = 1;
        String sql = "show databases";
        String postFix = "postFix" ;
        //设置不同参数遍历preSqlParamCheck的fail分支(String sql, String phyClusterName, Integer appId, String postFix)
        Assertions.assertEquals("参数错误:查询的sql语句为空，请检查后再提交！",gatewayService.sqlOperate("",CustomDataSource.PHY_CLUSTER_NAME,appid,postFix).getMessage());
        Assertions.assertEquals("参数错误:查询gateway的路径后缀为空，请检查后再提交！",gatewayService.sqlOperate(sql,CustomDataSource.PHY_CLUSTER_NAME,appid,null).getMessage());
        Assertions.assertEquals("参数错误:对应的appId字段非法，请检查后再提交！", gatewayService.sqlOperate(sql,CustomDataSource.PHY_CLUSTER_NAME,null,postFix).getMessage());
        App app = new App();
        app.setId(appid);
        List<App> apps = new ArrayList<>();
        apps.add(app);
        when(appService.listApps()).thenReturn(apps);
        when(appService.getAppById(Mockito.anyInt())).thenReturn(app);
        when(esGatewayClient.getSingleGatewayAddress()).thenReturn("10.190.32.30");
        //设置BaseHttpUtil静态方法遍历
        Mockito.mockStatic(BaseHttpUtil.class);
        when(BaseHttpUtil.postForString(anyString(), anyString(), any())).thenReturn("");
        Assertions.assertFalse(gatewayService.sqlOperate("sql",CustomDataSource.PHY_CLUSTER_NAME,appid,postFix).success());
        when(BaseHttpUtil.postForString(anyString(), anyString(), any())).thenReturn("sqlresponse");
        Header header = new BasicHeader("Authorization", "Basic ");
        when(BaseHttpUtil.buildHttpHeader(any(),any())).thenReturn(header);
        Assertions.assertTrue(gatewayService.sqlOperate("sql",CustomDataSource.PHY_CLUSTER_NAME,appid,postFix).success());
    }

    @Test
    void getAliveNodeTest() {
        when(gatewayClusterNodeDAO.listAliveNodeByClusterNameAndTime(any(), any())).thenReturn(CustomDataSource.getGatewayNodePOList());
        Assertions.assertFalse(gatewayService.getAliveNode(CustomDataSource.PHY_CLUSTER_NAME, 1000).isEmpty());
    }
}