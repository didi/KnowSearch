package com.didichuxing.datachannel.arius.admin.v3.op.cluster.config;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.source.PhyClusterInfoSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author cjm
 */
public class ESClusterConfigTest extends BaseContextTest {

    private static PhyClusterInfoSource.PhyClusterInfo phyClusterInfo;

    /**
     * 在当前类的所有测试方法之前执行
     * 由于本类中的方法都依赖物理集群，所以需要接入一个物理集群
     */
    @BeforeAll
    public static void preHandle() throws IOException {
        // 接入物理集群
        // phyClusterInfo = PhyClusterManager.phyClusterJoin();
        // 添加 config

    }

    /**
     * 在当前类中的所有测试方法之后执行
     * 本类所有方法测试完毕后，删除物理集群
     */
    @AfterAll
    public static void afterCompletion() throws IOException {
        // PhyClusterManager.phyClusterRemove(phyClusterInfo.getPhyClusterName(), phyClusterInfo.getPhyClusterId());
    }

    @Test
    public void gainEsClusterConfigsTest() throws IOException {
    }

    @Test
    public void gainEsClusterConfigTest() throws IOException {
    }

    @Test
    public void gainEsClusterRolesTest() throws IOException {

    }

    @Test
    public void gainEsClusterTemplateConfigTest() throws IOException {

    }

    @Test
    public void editEsClusterConfigDescTest() throws IOException {

    }
}
