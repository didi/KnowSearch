package com.didichuxing.datachannel.arius.admin.v3.op.cluster.phy;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.base.BasePhyClusterRegionInfoTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PhyClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanRegionDTO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.phy.ESPhyClusterRegionControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.LogicClusterInfoSource;
import com.didichuxing.datachannel.arius.admin.source.PhyClusterRegionInfoSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
     * 测试删除 region
     */
    @Test
    public void removeRegionTest() throws IOException {
        // 先删除
        Result<Void> result = ESPhyClusterRegionControllerMethod.removeRegion(phyClusterRegionInfo.getRegionId());
        Assertions.assertTrue(result.success());
        // 再创建回来
        PhyClusterRegionInfoSource.createRegion(phyClusterInfo.getPhyClusterName());
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
     * 测试获取物理集群可划分至region的Racks信息
     */
    @Test
    public void listPhyClusterRacksTest() throws IOException {
        Result<List<PhyClusterRackVO>> result = ESPhyClusterRegionControllerMethod.listPhyClusterRacks(phyClusterInfo.getPhyClusterName());
        Assertions.assertTrue(result.success());
    }

    /**
     * 测试修改容量规划region接口
     */
    @Test
    public void editClusterRegionTest() throws IOException {
        CapacityPlanRegionDTO capacityPlanRegionDTO = new CapacityPlanRegionDTO();
        capacityPlanRegionDTO.setRegionId(phyClusterRegionInfo.getRegionId());
        Map<String, Object> params = new HashMap<>();
        params.put("test", "my_test");
        capacityPlanRegionDTO.setConfigJson(JSON.toJSONString(params));
        Result<Void> result = ESPhyClusterRegionControllerMethod.editClusterRegion(capacityPlanRegionDTO);
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

    /**
     * 获取物理集群下的rack列表
     */
    @Test
    public void getClusterPhyRacksTest() throws IOException {
        Result<Set<String>> result = ESPhyClusterRegionControllerMethod.getClusterPhyRacks(phyClusterInfo.getPhyClusterName());
        Assertions.assertTrue(result.success());
        Assertions.assertTrue(result.getData().contains("*"));
    }

    /**
     * 测试获取物理集群region列表接口（带有 clusterLogicId）
     */
    @Test
    public void listPhyClusterRegions2Test() throws IOException {
        // 先创建逻辑集群
        LogicClusterInfoSource.LogicClusterInfo logicClusterInfo =
                LogicClusterInfoSource.applyLogicCluster(phyClusterInfo.getPhyClusterName(), phyClusterInfo.getPhyClusterName());
        Result<List<ClusterRegionVO>> result =
                ESPhyClusterRegionControllerMethod.listPhyClusterRegions(phyClusterInfo.getPhyClusterName(), ClusterResourceTypeEnum.PRIVATE.getCode(), logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result.success());
        // 删除逻辑集群
        LogicClusterInfoSource.removeLogicCluster(logicClusterInfo.getLogicClusterName(), logicClusterInfo.getLogicClusterId());
    }
}
