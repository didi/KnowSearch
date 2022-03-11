package com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsTypeEnum;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author cjm
 */
public class ESClusterDynamicConfigControllerMethod {

    public static final String CLUSTER_DYNAMIC_CONFIG = V3_OP + "/cluster/dynamicConfig";

    public static Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> getPhyClusterDynamicConfigs(String cluster) throws IOException {
        String path = CLUSTER_DYNAMIC_CONFIG + "/getAll/" + cluster;
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>>>(){});
    }

    public static Result<Boolean> updatePhyClusterDynamicConfig(ClusterSettingDTO dto) throws IOException {
        String path = CLUSTER_DYNAMIC_CONFIG + "/update";
        return JSON.parseObject(AriusClient.post(path, dto), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Set<String>> getRoutingAllocationAwarenessAttributes(String cluster) throws IOException {
        String path = CLUSTER_DYNAMIC_CONFIG + "/getClusterAttributes/" + cluster;
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<Set<String>>>(){});
    }
}
