package com.didichuxing.datachannel.arius.admin.v3.op.cluster.phy;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.didichuxing.datachannel.arius.admin.base.BasePhyClusterRegionInfoTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.phy.ESPhyClusterRegionControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.LogicClusterInfoSource;

/**
 * @author cjm
 * 因为继承了 BaseClusterManager，那么测试用例执行前，就会创建物理集群（不创建逻辑集群）
 */
public class ESPhyPhyClusterRegionTest extends BasePhyClusterRegionInfoTest {

    /**
     * 测试添加 region
     */
    @Test
    public void createRegionTest() {
        Assertions.assertNotNull(phyClusterRegionInfo.getRegionId());
    }

    /**
     * 测试获取物理集群region列表接口
     */
    @Test
    public void listPhyClusterRegionsTest() throws IOException {
        Result<List<ClusterRegionVO>> result = ESPhyClusterRegionControllerMethod.listPhyClusterRegions(phyClusterInfo.getPhyClusterName(), ClusterResourceTypeEnum.PRIVATE.getCode());
        Assertions.assertTrue(result.success());
    }

    /**
     * 测试获取region下的节点列表
     */
    @Test
    public void getRegionNodesTest() throws IOException {
        Result<List<ESClusterRoleHostVO>> result = ESPhyClusterRegionControllerMethod.getRegionNodes(phyClusterRegionInfo.getRegionId());
        Assertions.assertTrue(result.success());
        if(result.success()) {
            Set<String> rack = result.getData().stream().map(ESClusterRoleHostVO::getRack).collect(Collectors.toSet());
            Assertions.assertTrue(rack.contains("*"));
        }
    }

    /**
     * 测试获取Region物理模板列表接口
     */
    @Test
    public void getRegionPhysicalTemplatesTest() throws IOException {
        Result<List<IndexTemplatePhysicalVO>> result =
                ESPhyClusterRegionControllerMethod.getRegionPhysicalTemplates(phyClusterRegionInfo.getRegionId());
        Assertions.assertTrue(result.success());
    }
}
