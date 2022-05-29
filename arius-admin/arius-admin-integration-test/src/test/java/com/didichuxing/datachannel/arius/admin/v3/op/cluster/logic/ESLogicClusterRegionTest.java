package com.didichuxing.datachannel.arius.admin.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.base.BaseLogicClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.logic.ESLogicClusterRegionControllerMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cjm
 */
public class ESLogicClusterRegionTest extends BaseLogicClusterInfoTest {

    @Test
    public void listLogicClusterRegionsTest() throws IOException {
        Result<List<ClusterRegionVO>> result = ESLogicClusterRegionControllerMethod.listLogicClusterRegions(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result.success());
    }

    @Test
    public void cancelBindingLogicClusterRegionTest() throws IOException {
        Result<List<ClusterRegionVO>> result = ESLogicClusterRegionControllerMethod.listLogicClusterRegions(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result.success());
        List<Long> regionIdList = new ArrayList<>();
        for(ClusterRegionVO clusterRegionVO : result.getData()) {
            regionIdList.add(clusterRegionVO.getId());
            ESLogicClusterRegionControllerMethod.cancelBindingLogicClusterRegion(clusterRegionVO.getId(), logicClusterInfo.getLogicClusterId());
        }
        Result<List<ClusterRegionVO>> result2 = ESLogicClusterRegionControllerMethod.listLogicClusterRegions(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result2.success());
        Assertions.assertTrue(result2.getData().isEmpty());
        // 重新绑定回来
        bindRegion(regionIdList);
    }

    @Test
    public void bindingLogicClusterRegionTest() throws IOException {
        // 先解绑
        Result<List<ClusterRegionVO>> result = ESLogicClusterRegionControllerMethod.listLogicClusterRegions(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result.success());
        Long regionId = null;
        if(!result.getData().isEmpty()) {
            regionId = result.getData().get(0).getId();
            ESLogicClusterRegionControllerMethod.cancelBindingLogicClusterRegion(regionId, logicClusterInfo.getLogicClusterId());
        }
        // 再绑定
        List<Long> regionIdList = new ArrayList<>();
        regionIdList.add(regionId);
        bindRegion(regionIdList);

        Result<List<ClusterRegionVO>> result2 = ESLogicClusterRegionControllerMethod.listLogicClusterRegions(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result2.success());
        Set<Long> regionIdSet = result2.getData().stream().map(ClusterRegionVO::getId).collect(Collectors.toSet());
        Assertions.assertTrue(regionIdSet.contains(regionId));
    }

    private void bindRegion(List<Long> regionIdList) throws IOException {
        ESLogicClusterWithRegionDTO dto = new ESLogicClusterWithRegionDTO();
        dto.setResponsible("admin");
        dto.setDataCenter("cn");
        dto.setDataNodeNu(0);
        dto.setConfigJson("");
        dto.setId(logicClusterInfo.getLogicClusterId());
        dto.setQuota(0D);
        dto.setType(ClusterResourceTypeEnum.PRIVATE.getCode());

        List<ClusterRegionDTO> list = new ArrayList<>();
        for (Long regionId : regionIdList) {
            ClusterRegionDTO clusterRegionDTO = new ClusterRegionDTO();
            clusterRegionDTO.setPhyClusterName(phyClusterInfo.getPhyClusterName());
            clusterRegionDTO.setId(regionId);
            list.add(clusterRegionDTO);
        }
        dto.setClusterRegionDTOS(list);
        Result<Void> result2 = ESLogicClusterRegionControllerMethod.bindingLogicClusterRegion(dto);
        Assertions.assertTrue(result2.success());
    }
}