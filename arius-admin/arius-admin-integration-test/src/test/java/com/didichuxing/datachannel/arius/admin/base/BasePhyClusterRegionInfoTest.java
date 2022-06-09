package com.didichuxing.datachannel.arius.admin.base;

import com.didichuxing.datachannel.arius.admin.source.PhyClusterRegionInfoSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

/**
 * @author cjm
 */
public class BasePhyClusterRegionInfoTest extends BasePhyClusterInfoTest {

    protected static PhyClusterRegionInfoSource.PhyClusterRegionInfo phyClusterRegionInfo;

    /**
     * 在当前类的所有测试方法之前执行
     * @throws IOException e
     */
    @BeforeAll
    public static void preHandle() throws IOException {
        BasePhyClusterInfoTest.preHandle();
        // create region
        /*phyClusterRegionInfo = PhyClusterRegionInfoSource.createRegion(phyClusterInfo.getPhyClusterName());*/
    }

    /**
     * 在当前类中的所有测试方法之后执行
     * @throws IOException e
     */
    @AfterAll
    public static void afterCompletion() throws IOException {
        // delete region
        PhyClusterRegionInfoSource.deleteRegion(phyClusterRegionInfo.getRegionId());
        BasePhyClusterInfoTest.afterCompletion();
    }
}
