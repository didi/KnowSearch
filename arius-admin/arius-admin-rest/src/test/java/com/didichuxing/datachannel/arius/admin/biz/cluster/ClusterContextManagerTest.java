package com.didichuxing.datachannel.arius.admin.biz.cluster;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.cluster.impl.ClusterContextManagerImpl;

public class ClusterContextManagerTest extends AriusAdminApplicationTest {
    @Autowired
    private ClusterContextManagerImpl clusterContextManagerImpl;

    @Test
    public void flushClusterPhyContextsTest() {
        clusterContextManagerImpl.flushClusterPhyContexts();
        clusterContextManagerImpl.flushClusterLogicContexts();
    }
}
