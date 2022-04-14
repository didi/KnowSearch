package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
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
    private RegionRackService regionRackService;

    @MockBean
    private RoleClusterHostService roleClusterHostService;
    
    @Autowired
    private ClusterLogicNodeService clusterLogicNodeService;
    
    @Test
    public void getLogicClusterNodesTest() {
        ClusterLogicRackInfo clusterLogicRackInfo = new ClusterLogicRackInfo();
        Mockito.when(regionRackService.listLogicClusterRacks(Mockito.anyLong())).thenReturn(Arrays.asList(
				clusterLogicRackInfo));
        RoleClusterHost roleClusterHost = new RoleClusterHost();
        String clusterName = "wpk";
        roleClusterHost.setCluster(clusterName);
        Mockito.when(roleClusterHostService.queryNodeByCondt(Mockito.any())).thenReturn(Arrays.asList(
                roleClusterHost));
        Long clusterId = 123l;
        Assertions.assertTrue(clusterLogicNodeService
                .getLogicClusterNodes(clusterId)
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getCluster().equals(clusterName)));
    }

    @Test
    public void getLogicClusterNodesIncludeNonDataNodesTest() {
        ClusterLogicRackInfo clusterLogicRackInfo = new ClusterLogicRackInfo();
        Mockito.when(regionRackService.listLogicClusterRacks(Mockito.anyLong())).thenReturn(Arrays.asList(
				clusterLogicRackInfo));
        RoleClusterHost roleClusterHost = new RoleClusterHost();
        String clusterName = "wpk";
        roleClusterHost.setCluster(clusterName);
        Mockito.when(roleClusterHostService.queryNodeByCondt(Mockito.any())).thenReturn(Arrays.asList(
                roleClusterHost));
        Long clusterId = 123l;
        Assertions.assertTrue(clusterLogicNodeService
                .getLogicClusterNodesIncludeNonDataNodes(clusterId)
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getCluster().equals(clusterName)));
    }
}
