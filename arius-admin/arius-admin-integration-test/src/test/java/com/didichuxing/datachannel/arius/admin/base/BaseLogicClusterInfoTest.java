package com.didichuxing.datachannel.arius.admin.base;

import com.didichuxing.datachannel.arius.admin.source.LogicClusterInfoSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

/**
 * @author cjm
 */
public class BaseLogicClusterInfoTest extends BasePhyClusterInfoTest {

    protected static LogicClusterInfoSource.LogicClusterInfo logicClusterInfo;

    /**
     * 在当前类的所有测试方法之前执行
     * 由于本类中的方法都依赖物理集群和逻辑集群，所以需要接入一个物理集群，和创建一个逻辑集群
     */
    @BeforeAll
    public static void preHandle() throws IOException {
        BasePhyClusterInfoTest.preHandle();
        // 创建逻辑集群（关联了前面创建的物理集群的 region）
        logicClusterInfo = LogicClusterInfoSource.applyLogicCluster(phyClusterInfo.getPhyClusterName(), phyClusterInfo.getPhyClusterName());
    }

    /**
     * 在当前类中的所有测试方法之后执行
     * 本类所有方法测试完毕后，删除物理集群，删除逻辑集群
     */
    @AfterAll
    public static void afterCompletion() throws IOException {
        // 删除逻辑集群（会解绑 region）
        LogicClusterInfoSource.removeLogicCluster(logicClusterInfo.getLogicClusterName(), logicClusterInfo.getLogicClusterId());
        BasePhyClusterInfoTest.afterCompletion();
    }
}
