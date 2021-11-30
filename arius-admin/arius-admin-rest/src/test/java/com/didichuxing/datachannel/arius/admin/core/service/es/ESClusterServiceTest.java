package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterDAO;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Transactional
@Rollback
public class ESClusterServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private ESClusterDAO esClusterDAO;

    @Autowired
    private ESClusterService esClusterService;

    @Test
    void testSyncCloseReBalance() throws ESOperateException {
        String clusterName = "logi-elasticsearch-7.6.0";
        Integer retryCount = 1;
        Assertions.assertTrue(esClusterService.syncCloseReBalance(clusterName, retryCount));

    }

    @Test
    void testSyncOpenReBalance() throws ESOperateException {
        String clusterName = "logi-elasticsearch-7.6.0";
        Assertions.assertTrue(esClusterService.syncOpenReBalance(clusterName, null));
    }

    @Test
    void testSyncPutRemoteCluster() {

    }

    @Test
    void testHasSettingExist() {
        // 存在的配置
        String clusterName = "logi-elasticsearch-7.6.0";
        String settingFlatName = "cluster.routing.rebalance.enable";
        Assertions.assertTrue(esClusterService.hasSettingExist(clusterName, settingFlatName));
        // 不存在的配置
        settingFlatName = "xxxxx";
        Assertions.assertFalse(esClusterService.hasSettingExist(clusterName, settingFlatName));
    }

    @Test
    void testSyncConfigColdDateMove() {

    }

    @Test
    void testSyncGetClusterStatus() {
        /*
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Assertions.assertNotEquals(esClusterService.syncGetClusterStatus(clusterName), ClusterHealthEnum.UNKNOWN);
        // 不存在的集群
        clusterName = "ggglogi-elasticsearch-7.6.0";
        Assertions.assertEquals(esClusterService.syncGetClusterStatus(clusterName), ClusterHealthEnum.UNKNOWN);
         */
    }

    @Test
    void testSyncGetNode2PluginsMap() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        esClusterService.syncGetNode2PluginsMap(clusterName);
    }

    @Test
    void testSyncGetAliasMap() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Map<String, Set<String>> stringSetMap = esClusterService.syncGetAliasMap(clusterName);
        Assertions.assertFalse(stringSetMap.isEmpty());
        // 不存在的集群
        stringSetMap = esClusterService.syncGetAliasMap("testest");
        Assertions.assertTrue(stringSetMap.isEmpty());
    }

    @Test
    void testSyncGetClientAlivePercent() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        // 都正常的client
        String clientAddresses = "10.96.64.13:8061,10.96.64.15:8061,10.96.65.62:8061";
        Assertions.assertEquals(esClusterService.syncGetClientAlivePercent(clusterName, clientAddresses), new Integer(100));

        // 存在的集群
        clusterName = "logi-elasticsearch-7.6.0";
        // 带有1个不正常的client
        clientAddresses = "10.96.64.13:8061,10.96.64.15:8061,10.96.65.69:8061";
        Assertions.assertNotEquals(esClusterService.syncGetClientAlivePercent(clusterName, clientAddresses), new Integer(100));

    }

    @Test
    void testJudgeClientAlive() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        // 都正常的client
        String clientAddresses = "10.96.64.13:8061";
        Assertions.assertTrue(esClusterService.judgeClientAlive(clusterName, clientAddresses));

        // 存在的集群
        clusterName = "logi-elasticsearch-7.6.0";
        // 不正常常的client
        clientAddresses = "10.96.64.19:8061";
        Assertions.assertFalse(esClusterService.judgeClientAlive(clusterName, clientAddresses));
    }

    @Test
    void testSyncGetClusterHealth() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Assertions.assertNotNull(esClusterService.syncGetClusterHealth(clusterName));
        // 不存在的集群
        clusterName = "test-logi-elasticsearch-7.6.0";
        Assertions.assertNull(esClusterService.syncGetClusterHealth(clusterName));
    }

    @Test
    void testSyncGetClusterStats() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Assertions.assertNotNull(esClusterService.syncGetClusterStats(clusterName));
        // 不存在的集群
        clusterName = "ggg-test-logi-elasticsearch-7.6.0";
        Assertions.assertNull(esClusterService.syncGetClusterStats(clusterName));
    }

    @Test
    void testSyncGetClusterSetting() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Assertions.assertNotNull(esClusterService.syncGetClusterStats(clusterName));
        // 不存在的集群
        clusterName = "test-logi-elasticsearch-7.6.0";
        Assertions.assertNull(esClusterService.syncGetClusterStats(clusterName));
    }

    @Test
    void testSynGetSegmentsOfIpByCluster() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Assertions.assertFalse(esClusterService.synGetSegmentsOfIpByCluster(clusterName).isEmpty());
        // 不存在的集群
        clusterName = "ggg-test-logi-elasticsearch-7.6.0";
        Assertions.assertTrue(esClusterService.synGetSegmentsOfIpByCluster(clusterName).isEmpty());
    }

    @Test
    void testSyncPutPersistentConfig() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Map<String, Object> persistentConfig = Maps.newHashMap();
        persistentConfig.put("cluster.routing.rebalance.enable", "all");
        Assertions.assertTrue(esClusterService.syncPutPersistentConfig(clusterName, persistentConfig));
    }

    @Test
    void testSyncGetAllNodesAttributes() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Assertions.assertFalse(esClusterService.syncGetAllNodesAttributes(clusterName).isEmpty());
        // 不存在的集群
        clusterName = "ggg-test-logi-elasticsearch-7.6.0";
        Assertions.assertFalse(esClusterService.syncGetAllNodesAttributes(clusterName).isEmpty());
    }

    @Test
    void testSyncGetAllSettingsByCluster() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Assertions.assertFalse(esClusterService.syncGetAllSettingsByCluster(clusterName).isEmpty());
        // 不存在的集群
        clusterName = "ggg-test-logi-elasticsearch-7.6.0";
        Assertions.assertFalse(esClusterService.syncGetAllSettingsByCluster(clusterName).isEmpty());
    }

    @Test
    void testSyncGetPartOfSettingsByCluster() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Assertions.assertFalse(esClusterService.syncGetPartOfSettingsByCluster(clusterName).isEmpty());
        // 不存在的集群
        clusterName = "ggg-test-logi-elasticsearch-7.6.0";
        Assertions.assertFalse(esClusterService.syncGetPartOfSettingsByCluster(clusterName).isEmpty());
    }

    @Test
    void testHasESClientHttpAddressActive() {

        // 已经接入平台的地址
        /*
        String clientAddresses = "10.96.64.13:8061";
        Result<Void> result = esClusterService.hasESClientHttpAddressActive(clientAddresses);
        Assertions.assertEquals((int) result.getCode(), ResultType.ILLEGAL_PARAMS.getCode());

        // 不存在的地址
        clientAddresses = "10.96.64.43:8061";
        result = esClusterService.hasESClientHttpAddressActive(clientAddresses);
        Assertions.assertEquals((int) result.getCode(), ResultType.ILLEGAL_PARAMS.getCode());
        */

    }

    @Test
    void testSynGetESVersionByCluster() {
        // 存在的集群
        String clusterName = "logi-elasticsearch-7.6.0";
        Assertions.assertFalse(esClusterService.synGetESVersionByCluster(clusterName).isEmpty());
        // 不存在的集群
        clusterName = "ggg-test-logi-elasticsearch-7.6.0";
        Assertions.assertNull(esClusterService.synGetESVersionByCluster(clusterName));
    }

    @Test
    void testGetClusterRackByHttpAddress() {
        // 存在的地址
        String clientAddresses = "10.96.64.13:8061";
        Result<Set<String>> result = esClusterService.getClusterRackByHttpAddress(clientAddresses);
        Assertions.assertFalse(result.getData().isEmpty());

        // 不存在的地址
        clientAddresses = "10.96.64.43:8061";
        result = esClusterService.getClusterRackByHttpAddress(clientAddresses);
        Assertions.assertEquals((int) result.getCode(), ResultType.ILLEGAL_PARAMS.getCode());
    }
}

