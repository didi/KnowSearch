package com.didichuxing.datachannel.arius.admin.source;

import java.io.IOException;

import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.phy.ESPhyClusterRegionControllerMethod;

import lombok.Data;

/**
 * @author cjm
 */
public class PhyClusterRegionInfoSource {

    @Data
    public static class PhyClusterRegionInfo {
        private Long regionId;
    }

    public static void deleteRegion(Long regionId) throws IOException {
        ESPhyClusterRegionControllerMethod.removeRegion(regionId);
    }
}
