package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.NodeAttrInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ECSegmentOnIp;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.metadata.service.DashBoardMetricsService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterPhyStatsService;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.AliasIndexNode;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class ESClusterServiceTest extends AriusAdminApplicationTest {

    @MockBean
    private ESClusterDAO esClusterDAO;

    @Autowired
    private ESClusterService esClusterService;
    @Autowired
    private ESClusterPhyStatsService esClusterPhyStatsService;
    @Autowired
    private DashBoardMetricsService dashBoardMetricsService;
    
    
    @Test
    public void getToNMetricsTest(){
        MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO();
        //gatewaySucPer
        param.setMetricsTypes(Arrays.asList( "gatewayFailedPer","gatewaySucPer"));
        param.setStartTime(1649214919299L);
        param.setEndTime(1649236519299L);
        param.setTopNu(5);
        param.setAggType("avg");
        Assertions.assertNotNull(dashBoardMetricsService.getToNMetrics(param,
            "cluster"));
    }
    
    
    @Test
    public void   getClustersShardTotalTest(){
        Mockito.when(esClusterDAO.configReBalanceOperate(Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertEquals(6020L, esClusterPhyStatsService.getClustersShardTotal(
            CustomDataSource.PHY_CLUSTER_NAME_LOGI));
    
    }
    @Test
    public void getWriteRequestTotalTest(){
        Mockito.when(esClusterDAO.configReBalanceOperate(Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertEquals(115018L, esClusterPhyStatsService.getCurrentIndexTotal(
            CustomDataSource.PHY_CLUSTER_NAME_LOGI));
    }
    @Test
    public void getHttpConnectionTotalTest(){
        Mockito.when(esClusterDAO.configReBalanceOperate(Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertEquals(954L, esClusterPhyStatsService.getHttpConnectionTotal(
            CustomDataSource.PHY_CLUSTER_NAME_LOGI));
    }
    @Test
    public void getGatewaySuccessRateAndFailureRateTest(){
        Mockito.when(esClusterDAO.configReBalanceOperate(Mockito.any(), Mockito.any())).thenReturn(true);
        final Tuple<Double, Double> rateAndFailureRate = esClusterPhyStatsService.getGatewaySuccessRateAndFailureRate(
            CustomDataSource.PHY_CLUSTER_NAME_LOGI);
        Assertions.assertEquals(0.99,rateAndFailureRate.getV1());
        Assertions.assertEquals(0L,rateAndFailureRate.getV2());
    }
    @Test
    public void getPendingTaskTotalTest(){
        Mockito.when(esClusterDAO.configReBalanceOperate(Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertEquals(0L, esClusterPhyStatsService.getPendingTaskTotal(CustomDataSource.PHY_CLUSTER_NAME_LOGI));
    
    }
    @Test
    public void getQueryRequestsIncrementTest(){
        Mockito.when(esClusterDAO.configReBalanceOperate(Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertEquals(0L,
            esClusterPhyStatsService.getCurrentQueryTotal(CustomDataSource.PHY_CLUSTER_NAME_LOGI));
    }

    
    @Test
    public void syncCloseReBalanceTest() throws ESOperateException {
        Mockito.when(esClusterDAO.configReBalanceOperate(Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esClusterService.syncCloseReBalance(CustomDataSource.PHY_CLUSTER_NAME, 1));
    }
   

    @Test
    public void syncOpenReBalanceTest() throws ESOperateException {
        Mockito.when(esClusterDAO.configReBalanceOperate(Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esClusterService.syncOpenReBalance(CustomDataSource.PHY_CLUSTER_NAME, "v"));
    }

    @Test
    public void syncPutRemoteClusterTest() throws ESOperateException {
        Mockito.when(esClusterDAO.putPersistentRemoteClusters(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esClusterService.syncPutRemoteCluster(CustomDataSource.PHY_CLUSTER_NAME, "test", new ArrayList<>(), 1));
    }

    @Test
    public void hasSettingExistTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("test", "test");
        Mockito.when(esClusterDAO.getPersistentClusterSettings(Mockito.any())).thenReturn(map);
        Assertions.assertTrue(esClusterService.hasSettingExist(CustomDataSource.PHY_CLUSTER_NAME, "test"));
    }

    @Test
    public void syncGetClusterStatusTest() {
        Mockito.when(esClusterDAO.getClusterStats(Mockito.any())).thenReturn(new ESClusterStatsResponse());
        Assertions.assertNotNull(esClusterService.syncGetClusterStats(CustomDataSource.PHY_CLUSTER_NAME));
    }

    @Test
    public void syncGetNode2PluginsMapTest() {
        Mockito.when(esClusterDAO.getNode2PluginsMap(Mockito.any())).thenReturn(new HashMap<>());
        Assertions.assertNotNull(esClusterService.syncGetNode2PluginsMap(CustomDataSource.PHY_CLUSTER_NAME));
    }

    @Test
    public void syncGetAliasMapTest() {
        Mockito.when(esClusterDAO.getClusterAlias(Mockito.any())).thenReturn(null);
        Assertions.assertTrue(esClusterService.syncGetAliasMap(CustomDataSource.PHY_CLUSTER_NAME).isEmpty());
        ESIndicesGetAliasResponse response = new ESIndicesGetAliasResponse();
        Map<String, AliasIndexNode> m = new HashMap<>();
        AliasIndexNode aliasIndexNode = new AliasIndexNode();
        Map<String, JSONObject> aliases = new HashMap<>();
        aliases.put("test1", new JSONObject());
        aliasIndexNode.setAliases(aliases);
        m.put("test1", aliasIndexNode);
        response.setM(m);
        Mockito.when(esClusterDAO.getClusterAlias(Mockito.any())).thenReturn(response);
        Assertions.assertFalse(esClusterService.syncGetAliasMap(CustomDataSource.PHY_CLUSTER_NAME).isEmpty());
    }

    @Test
    public void syncGetClusterHealthTest() {
        Mockito.when(esClusterDAO.getClusterHealth(Mockito.any())).thenReturn(new ESClusterHealthResponse());
        Assertions.assertNotNull(esClusterService.syncGetClusterHealth(CustomDataSource.PHY_CLUSTER_NAME));
    }

    @Test
    public void syncGetClusterStatsTest() {
        Mockito.when(esClusterDAO.getClusterStats(Mockito.any())).thenReturn(new ESClusterStatsResponse());
        Assertions.assertNotNull(esClusterService.syncGetClusterStats(CustomDataSource.PHY_CLUSTER_NAME));
    }

    @Test
    public void syncGetClusterSettingTest() {
        Mockito.when(esClusterDAO.getClusterStats(Mockito.any())).thenReturn(new ESClusterStatsResponse());
        Assertions.assertNotNull(esClusterService.syncGetClusterStats(CustomDataSource.PHY_CLUSTER_NAME));
    }

    @Test
    public void synGetSegmentsOfIpByClusterTest() {
        List<ECSegmentOnIp> list = new ArrayList<>();
        ECSegmentOnIp ecSegmentOnIp = new ECSegmentOnIp();
        ecSegmentOnIp.setSegment("1");
        ecSegmentOnIp.setIp("127.0.0.1");
        ECSegmentOnIp ecSegmentOnIp2 = new ECSegmentOnIp();
        ecSegmentOnIp2.setSegment("2");
        ecSegmentOnIp2.setIp("127.0.0.1");
        list.add(ecSegmentOnIp);
        list.add(ecSegmentOnIp2);
        Mockito.when(esClusterDAO.getSegmentsOfIpByCluster(Mockito.any())).thenReturn(list);
        Assertions.assertEquals(1, esClusterService.synGetSegmentsOfIpByCluster(CustomDataSource.PHY_CLUSTER_NAME).size());
    }

    @Test
    public void syncPutPersistentConfigTest() {
        Mockito.when(esClusterDAO.putPersistentConfig(Mockito.any(), Mockito.anyMap())).thenReturn(true);
        Assertions.assertTrue(esClusterService.syncPutPersistentConfig(CustomDataSource.PHY_CLUSTER_NAME, new HashMap<>()));
    }

    @Test
    public void syncGetAllNodesAttributesTest() {
        Mockito.when(esClusterDAO.syncGetAllNodesAttributes("test")).thenReturn(new ArrayList<>());
        Assertions.assertTrue(esClusterService.syncGetAllNodesAttributes(CustomDataSource.PHY_CLUSTER_NAME).isEmpty());
        List<NodeAttrInfo> list = new ArrayList<>();
        NodeAttrInfo nodeAttrInfo = new NodeAttrInfo();
        nodeAttrInfo.setNode("test");
        nodeAttrInfo.setValue("testValue");
        nodeAttrInfo.setAttribute("testAttribute");
        list.add(nodeAttrInfo);
        Mockito.when(esClusterDAO.syncGetAllNodesAttributes(Mockito.any())).thenReturn(list);
        Assertions.assertFalse(esClusterService.syncGetAllNodesAttributes(CustomDataSource.PHY_CLUSTER_NAME).isEmpty());
    }

    @Test
    public void syncGetAllSettingsByClusterTest() {
        Map<String, ClusterNodeInfo> map = new HashMap<>();
        map.put("test", new ClusterNodeInfo());
        Mockito.when(esClusterDAO.getAllSettingsByCluster(Mockito.any())).thenReturn(map);
        Assertions.assertFalse(esClusterService.syncGetAllSettingsByCluster(CustomDataSource.PHY_CLUSTER_NAME).isEmpty());
    }

    @Test
    public void syncGetPartOfSettingsByClusterTest() {
        Map<String, ClusterNodeSettings> map = new HashMap<>();
        map.put("test", new ClusterNodeSettings());
        Mockito.when(esClusterDAO.getPartOfSettingsByCluster(Mockito.any())).thenReturn(map);
        Assertions.assertFalse(esClusterService.syncGetPartOfSettingsByCluster(CustomDataSource.PHY_CLUSTER_NAME).isEmpty());
    }

    @Test
    public void synGetESVersionByClusterTest() {
        Mockito.when(esClusterDAO.getESVersionByCluster(Mockito.any())).thenReturn("v");
        Assertions.assertNotNull(esClusterService.synGetESVersionByCluster(CustomDataSource.PHY_CLUSTER_NAME));
    }
}