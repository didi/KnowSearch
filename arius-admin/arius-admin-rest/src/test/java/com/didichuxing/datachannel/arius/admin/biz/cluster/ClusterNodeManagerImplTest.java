
package com.didichuxing.datachannel.arius.admin.biz.cluster;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import com.didichuxing.datachannel.arius.admin.biz.cluster.impl.ClusterNodeManagerImpl;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;

@ActiveProfiles("test")
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SpringTool.class })
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ClusterNodeManagerImplTest {

    @Mock
    private ClusterRoleHostService clusterRoleHostService;
    @Mock
    private ClusterPhyService      clusterPhyService;
    @Mock
    private ESClusterNodeService   esClusterNodeService;

    @InjectMocks
    private ClusterNodeManagerImpl clusterNodeManager;

    @Test
    void listClusterPhyInstanceTest() {
        when(clusterPhyService.getClusterById(0)).thenReturn(getClusterPhy());

        when(clusterRoleHostService.getNodesByCluster(Mockito.any())).thenReturn(Collections.singletonList(getClusterRoleHost()));

        when(esClusterNodeService.syncGetNodesDiskUsage(PHY_CLUSTER_NAME)).thenReturn(new HashMap<>());

        final Result<List<ESClusterRoleHostVO>> result = clusterNodeManager.listClusterPhyNode(0);

        assertThat(result).isEqualTo(Result
                .buildSucc(Collections.singletonList(getESClusterRoleHostVO())));
    }

    @Test
    void listClusterPhyInstanceClusterRoleHostServiceReturnsNoItemsTest() {
        when(clusterPhyService.getClusterById(0)).thenReturn(getClusterPhy());

        when(clusterRoleHostService.getNodesByCluster(Mockito.any())).thenReturn(Collections.emptyList());
        when(esClusterNodeService.syncGetNodesDiskUsage(PHY_CLUSTER_NAME)).thenReturn(new HashMap<>());

        final Result<List<ESClusterRoleHostVO>> result = clusterNodeManager.listClusterPhyNode(0);

        assertThat(result).isEqualTo(Result.buildSucc(Collections.emptyList()));
    }
}
