package com.didichuxing.datachannel.arius.admin.source;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanRegionDTO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.phy.ESPhyClusterRegionControllerMethod;
import lombok.Data;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

/**
 * @author cjm
 */
public class PhyClusterRegionInfoSource {

    @Data
    public static class PhyClusterRegionInfo {
        private Long regionId;
    }

    /**
     * 添加 region
     */
    public static PhyClusterRegionInfo createRegion(String phyClusterName) throws IOException {
        PhyClusterRegionInfo phyClusterRegionInfo = new PhyClusterRegionInfo();
        CapacityPlanRegionDTO capacityPlanRegionDTO = new CapacityPlanRegionDTO();
        capacityPlanRegionDTO.setClusterName(phyClusterName);
        capacityPlanRegionDTO.setRacks("*");
        capacityPlanRegionDTO.setLogicClusterId(0L);
        Result<Long> result = ESPhyClusterRegionControllerMethod.createRegion(capacityPlanRegionDTO);
        Assertions.assertTrue(result.success());
        phyClusterRegionInfo.regionId = result.getData();
        return phyClusterRegionInfo;
    }

    public static void deleteRegion(Long regionId) throws IOException {
        ESPhyClusterRegionControllerMethod.removeRegion(regionId);
    }
}
