package com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.logic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author cjm
 */
public class ESLogicClusterRegionControllerMethod {

    public static final String LOGIC_CLUSTER_REGION = V3_OP + "/logic/cluster/region";

    public static Result<List<ClusterRegionVO>> listLogicClusterRegions(Long logicClusterId) throws IOException {
        String path = String.format("%s/list", LOGIC_CLUSTER_REGION);
        Map<String, Object> params = new HashMap<>();
        params.put("logicClusterId", logicClusterId);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<List<ClusterRegionVO>>>(){});
    }

    public static Result<Void> cancelBindingLogicClusterRegion(Long regionId, Long logicClusterId) throws IOException {
        String path = String.format("%s/delete", LOGIC_CLUSTER_REGION);
        Map<String, Object> params = new HashMap<>();
        params.put("regionId", regionId);
        params.put("logicClusterId", logicClusterId);
        return JSON.parseObject(AriusClient.delete(path, params, null), new TypeReference<Result<Void>>(){});
    }

    public static Result<Void> bindingLogicClusterRegion(ESLogicClusterWithRegionDTO dto) throws IOException {
        String path = LOGIC_CLUSTER_REGION;
        return JSON.parseObject(AriusClient.post(path, dto), new TypeReference<Result<Void>>(){});
    }
}
