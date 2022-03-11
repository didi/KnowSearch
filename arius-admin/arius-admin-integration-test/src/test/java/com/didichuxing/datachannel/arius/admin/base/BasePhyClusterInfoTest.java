package com.didichuxing.datachannel.arius.admin.base;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.source.PhyClusterInfoSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

/**
 * @author cjm
 */
public class BasePhyClusterInfoTest extends BaseContextTest {

    protected static PhyClusterInfoSource.PhyClusterInfo phyClusterInfo;

    /**
     * 在当前类的所有测试方法之前执行
     */
    @BeforeAll
    public static void preHandle() throws IOException {
        BaseContextTest.init();
        phyClusterInfo = PhyClusterInfoSource.phyClusterJoin();
    }

    /**
     * 在当前类中的所有测试方法之后执行
     */
    @AfterAll
    public static void afterCompletion() throws IOException {
        PhyClusterInfoSource.phyClusterRemove(phyClusterInfo.getPhyClusterName(), phyClusterInfo.getPhyClusterId());
    }
}
