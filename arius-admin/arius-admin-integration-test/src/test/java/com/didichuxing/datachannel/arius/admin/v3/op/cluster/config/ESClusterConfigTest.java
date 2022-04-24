package com.didichuxing.datachannel.arius.admin.v3.op.cluster.config;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm.ESConfigVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.config.ESClusterConfigControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.PhyClusterInfoSource;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
        phyClusterInfo = PhyClusterInfoSource.phyClusterJoin();

    }

    /**
     * 在当前类中的所有测试方法之后执行
     * 本类所有方法测试完毕后，删除物理集群
     */
    @AfterAll
    public static void afterCompletion() throws IOException {
        PhyClusterInfoSource.phyClusterRemove(phyClusterInfo.getPhyClusterName(), phyClusterInfo.getPhyClusterId());
    }

    @Test
    public void gainEsClusterConfigsTest() throws IOException {
        Result<List<ESConfigVO>> result = ESClusterConfigControllerMethod.gainEsClusterConfigs(phyClusterInfo.getPhyClusterId());
        Assert.assertTrue(result.success());
    }

    @Test
    public void gainEsClusterConfigTest() throws IOException {
        Result<List<ESConfigVO>> clusterResults = ESClusterConfigControllerMethod.gainEsClusterConfigs(phyClusterInfo.getPhyClusterId());
        Assert.assertTrue(clusterResults.success());
        for (ESConfigVO configFromCluster : clusterResults.getData()) {
            Result<ESConfigVO> configFromId = ESClusterConfigControllerMethod.gainEsClusterConfig(configFromCluster.getId());
            System.out.println(configFromCluster.getId());
            Assert.assertTrue(configFromId.success());
            Assert.assertEquals(configFromCluster, configFromId.getData());
        }
    }

    @Test
    public void gainEsClusterRolesTest() throws IOException {
        Result<Set<String>> result = ESClusterConfigControllerMethod.gainEsClusterRoles(phyClusterInfo.getPhyClusterId());
        Assert.assertTrue(result.success());
    }

    @Test
    public void gainEsClusterTemplateConfigTest() throws IOException {
        Result<ESConfigVO> result = ESClusterConfigControllerMethod.gainEsClusterTemplateConfig("elasticsearch.yml");
        Assert.assertTrue(result.success());
        Assert.assertNotNull(result.getData());
    }

    @Test
    public void editEsClusterConfigDescTest() throws IOException {
        Long configId = 719L;
        String newDesc = "this is a integrate desc test";
        Result<ESConfigVO> oldConfigResult = ESClusterConfigControllerMethod.gainEsClusterConfig(configId);
        Assert.assertTrue(oldConfigResult.success());

        ESConfigDTO esConfigDTO = new ESConfigDTO();
        esConfigDTO.setId(configId);
        esConfigDTO.setDesc(newDesc);

        Result<Void> editResult = ESClusterConfigControllerMethod.editEsClusterConfigDesc(esConfigDTO);
        Assert.assertTrue(editResult.success());

        Result<ESConfigVO> newConfigResult = ESClusterConfigControllerMethod.gainEsClusterConfig(configId);
        Assert.assertTrue(newConfigResult.success());
        Assert.assertEquals(newDesc, newConfigResult.getData().getDesc());
    }
}
