package com.didichuxing.datachannel.arius.admin.core.service.es;

import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.NodeAttrInfo;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ECSegmentsOnIps;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.AliasIndexNode;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.google.common.collect.Maps;
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
    public void syncConfigColdDateMoveTest() throws ESOperateException {
        Mockito.when(esClusterDAO.putPersistentConfig(Mockito.any(), Mockito.anyMap())).thenReturn(true);
        Assertions.assertTrue(esClusterService.syncConfigColdDateMove(CustomDataSource.PHY_CLUSTER_NAME, 1, 1, "1", 1));
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
        List<ECSegmentsOnIps> list = new ArrayList<>();
        ECSegmentsOnIps ecSegmentsOnIps = new ECSegmentsOnIps();
        ecSegmentsOnIps.setSegment("1");
        ecSegmentsOnIps.setIp("127.0.0.1");
        ECSegmentsOnIps ecSegmentsOnIps2 = new ECSegmentsOnIps();
        ecSegmentsOnIps2.setSegment("2");
        ecSegmentsOnIps2.setIp("127.0.0.1");
        list.add(ecSegmentsOnIps);
        list.add(ecSegmentsOnIps2);
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

