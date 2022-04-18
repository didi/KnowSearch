package com.didichuxing.datachannel.arius.admin.v3.op.cluster.config;

import com.didichuxing.datachannel.arius.admin.base.BasePhyClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsTypeEnum;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.config.ESClusterDynamicConfigControllerMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author cjm
 */
public class ESClusterDynamicConfigTest extends BasePhyClusterInfoTest {

    @Test
    public void getPhyClusterDynamicConfigsTest() throws IOException {
        Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> result =
                ESClusterDynamicConfigControllerMethod.getPhyClusterDynamicConfigs(phyClusterInfo.getPhyClusterName());
        Assertions.assertTrue(result.success());
    }

    @Test
    public void updatePhyClusterDynamicConfigTest() throws IOException {
        ClusterSettingDTO dto = new ClusterSettingDTO();
        dto.setClusterName(phyClusterInfo.getPhyClusterName());
        dto.setKey(ClusterDynamicConfigsEnum.DISCOVERY_ZEN_NO_MASTER_BLOCK.getName());
        dto.setValue("write");
        Result<Boolean> result = ESClusterDynamicConfigControllerMethod.updatePhyClusterDynamicConfig(dto);
        Assertions.assertTrue(result.success());
        dto.setValue("all");
        Result<Boolean> result2 = ESClusterDynamicConfigControllerMethod.updatePhyClusterDynamicConfig(dto);
        Assertions.assertTrue(result2.success());

        Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> result3 =
                ESClusterDynamicConfigControllerMethod.getPhyClusterDynamicConfigs(phyClusterInfo.getPhyClusterName());
        String value = (String) result3.getData()
                .get(ClusterDynamicConfigsTypeEnum.ZEN)
                .get(ClusterDynamicConfigsEnum.DISCOVERY_ZEN_NO_MASTER_BLOCK.getName());
        Assertions.assertEquals(dto.getValue(), value);
    }

    @Test
    public void getRoutingAllocationAwarenessAttributesTest() throws IOException {
        Result<Set<String>> result = ESClusterDynamicConfigControllerMethod.getRoutingAllocationAwarenessAttributes(phyClusterInfo.getPhyClusterName());
        Assertions.assertTrue(result.success());
    }
}
