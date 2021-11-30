package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.BigIndexMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.BigShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.MovingShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.PendingTask;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class ESClusterNodeServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private ESClusterNodeService esClusterNodeService;

    @Test
    void syncGetNodeFsStatsMap() {
        String clusterName = "cjm_6.6.2_test";
        Map<String, ClusterNodeStats> stringClusterNodeStatsMap = esClusterNodeService.syncGetNodeFsStatsMap(clusterName);
        Assertions.assertFalse(stringClusterNodeStatsMap.isEmpty());
        // 不存在集群
        stringClusterNodeStatsMap = esClusterNodeService.syncGetNodeFsStatsMap("testtest");
        Assertions.assertTrue(stringClusterNodeStatsMap.isEmpty());
    }

    @Test
    void syncGetNodeHosts() {
        String clusterName = "cjm_6.6.2_test";
        List<String> strings = esClusterNodeService.syncGetNodeHosts(clusterName);
        Assertions.assertFalse(strings.isEmpty());
        // 不存在集群
        strings = esClusterNodeService.syncGetNodeHosts("testtest");
        Assertions.assertTrue(strings.isEmpty());
    }

    @Test
    void syncGetNodeInfo() {
        String clusterName = "cjm_6.6.2_test";
        Map<String, ClusterNodeInfo> stringClusterNodeInfoMap = esClusterNodeService.syncGetNodeInfo(clusterName);
        Assertions.assertFalse(stringClusterNodeInfoMap.isEmpty());
        // 不存在集群
        stringClusterNodeInfoMap = esClusterNodeService.syncGetNodeInfo("testtest");
        Assertions.assertTrue(stringClusterNodeInfoMap.isEmpty());
    }

    @Test
    void syncGetNodeNames() {
        String clusterName = "cjm_6.6.2_test";
        List<String> strings = esClusterNodeService.syncGetNodeNames(clusterName);
        Assertions.assertFalse(strings.isEmpty());
        // 不存在集群
        strings = esClusterNodeService.syncGetNodeNames("testtest");
        Assertions.assertTrue(strings.isEmpty());
    }

    @Test
    void syncGetPendingTask() {
        String clusterName = "cjm_6.6.2_test";
        List<PendingTask> pendingTasks = esClusterNodeService.syncGetPendingTask(clusterName);
        Assertions.assertFalse(pendingTasks.isEmpty());
        // 不存在集群
        pendingTasks = esClusterNodeService.syncGetPendingTask("testtest");
        Assertions.assertTrue(pendingTasks.isEmpty());
    }

    @Test
    void syncGetMovingShards() {
        String clusterName = "cjm_6.6.2_test";
        List<MovingShardMetrics> movingShardMetrics = esClusterNodeService.syncGetMovingShards(clusterName);
        Assertions.assertFalse(movingShardMetrics.isEmpty());
        // 不存在集群
        movingShardMetrics = esClusterNodeService.syncGetMovingShards("testtest");
        Assertions.assertTrue(movingShardMetrics.isEmpty());
    }

    @Test
    void syncGetBigShards() {
        String clusterName = "cjm_6.6.2_test";
        List<BigShardMetrics> bigShardMetrics = esClusterNodeService.syncGetBigShards(clusterName);
        Assertions.assertFalse(bigShardMetrics.isEmpty());
        // 不存在集群
        bigShardMetrics = esClusterNodeService.syncGetBigShards("testtest");
        Assertions.assertTrue(bigShardMetrics.isEmpty());
    }

    @Test
    void syncGetBigIndices() {
        // 先把 ONE_BILLION 改为 1
        String clusterName = "cjm_6.6.2_test";
        List<BigIndexMetrics> bigIndexMetrics = esClusterNodeService.syncGetBigIndices(clusterName);
        Assertions.assertFalse(bigIndexMetrics.isEmpty());
        // 不存在集群
        bigIndexMetrics = esClusterNodeService.syncGetBigIndices("testtest");
        Assertions.assertTrue(bigIndexMetrics.isEmpty());
    }

    @Test
    void syncGetIndicesCount() {
        String clusterName = "cjm_6.6.2_test";
        int num = esClusterNodeService.syncGetIndicesCount(clusterName, "172.23.164.4");
        Assertions.assertTrue(num > 0);
        // 无效的节点host
        num = esClusterNodeService.syncGetIndicesCount(clusterName, "172.23.164.88");
        Assertions.assertEquals(0, num);
        // 不存在集群
        num = esClusterNodeService.syncGetIndicesCount("testtest", "172.23.164.4");
        Assertions.assertEquals(0, num);

    }
}
