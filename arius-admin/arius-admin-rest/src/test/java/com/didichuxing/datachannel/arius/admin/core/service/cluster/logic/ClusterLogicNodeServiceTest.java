package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Transactional
@Rollback
public class ClusterLogicNodeServiceTest extends AriusAdminApplicationTest {

    @MockBean
    private ClusterRegionService clusterRegionService;

    @MockBean
    private ClusterRoleHostService clusterRoleHostService;
    
    @Autowired
    private ClusterLogicNodeService clusterLogicNodeService;
    
    @Test
    public void getLogicClusterNodesTest() {
        ClusterLogicRackInfo clusterLogicRackInfo = new ClusterLogicRackInfo();
        Mockito.when(clusterRegionService.listLogicClusterRacks(Mockito.anyLong())).thenReturn(Arrays.asList(
				clusterLogicRackInfo));
        ClusterRoleHost clusterRoleHost = new ClusterRoleHost();
        String clusterName = "wpk";
        clusterRoleHost.setCluster(clusterName);
        Mockito.when(clusterRoleHostService.queryNodeByCondt(Mockito.any())).thenReturn(Arrays.asList(
                clusterRoleHost));
        Long clusterId = 123l;
        Assertions.assertTrue(clusterLogicNodeService
                .getLogicClusterNodes(clusterId)
                .stream()
                .anyMatch(esClusterRoleHost -> esClusterRoleHost.getCluster().equals(clusterName)));
    }

    @Test
    public void getLogicClusterNodesIncludeNonDataNodesTest() {
        ClusterLogicRackInfo clusterLogicRackInfo = new ClusterLogicRackInfo();
        Mockito.when(clusterRegionService.listLogicClusterRacks(Mockito.anyLong())).thenReturn(Arrays.asList(
				clusterLogicRackInfo));
        ClusterRoleHost clusterRoleHost = new ClusterRoleHost();
        String clusterName = "wpk";
        clusterRoleHost.setCluster(clusterName);
        Mockito.when(clusterRoleHostService.queryNodeByCondt(Mockito.any())).thenReturn(Arrays.asList(
                clusterRoleHost));
        Long clusterId = 123l;
        Assertions.assertTrue(clusterLogicNodeService
                .getLogicClusterNodesIncludeNonDataNodes(clusterId)
                .stream()
                .anyMatch(esClusterRoleHost -> esClusterRoleHost.getCluster().equals(clusterName)));
    }
}
