package com.didichuxing.datachannel.arius.admin.biz.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicTypeEnum.PUBLIC;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

public class ClusterContextManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private ClusterContextManager clusterContextManager;

    // @Test
    public void getCanBeAssociatedClustersPhysTest() {

        Result<List<String>> canBeAssociatedClustersPhysWithouClusterLogicId = clusterContextManager.getCanBeAssociatedClustersPhys(PUBLIC.getCode(), null);

        Assertions.assertNotNull(canBeAssociatedClustersPhysWithouClusterLogicId.getData());

        Result<List<String>> canBeAssociatedClustersPhysWithClusterLogicId = clusterContextManager.getCanBeAssociatedClustersPhys(PUBLIC.getCode(), 429L);

        Assertions.assertNotNull(canBeAssociatedClustersPhysWithClusterLogicId.getData());

    }
}
