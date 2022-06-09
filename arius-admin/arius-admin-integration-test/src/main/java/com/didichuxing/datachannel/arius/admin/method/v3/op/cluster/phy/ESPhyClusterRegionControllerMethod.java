package com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.phy;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;

/**
 * @author cjm
 */
public class ESPhyClusterRegionControllerMethod {

    public static final String PHY_CLUSTER_REGION = V3_OP + "/phy/cluster/region";

    public static Result<List<ClusterRegionVO>> listPhyClusterRegions(String cluster, Integer clusterLogicType) throws IOException {
        String path = PHY_CLUSTER_REGION;
        Map<String, Object> params = new HashMap<>();
        params.put("cluster", cluster);
        params.put("clusterLogicType", clusterLogicType);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<List<ClusterRegionVO>>>(){});
    }

    public static Result<List<ClusterRegionVO>> listPhyClusterRegions(String cluster, Integer clusterLogicType, Long clusterLogicId) throws IOException {
        String path = String.format("%s/bind", PHY_CLUSTER_REGION);
        Map<String, Object> params = new HashMap<>();
        params.put("cluster", cluster);
        params.put("clusterLogicType", clusterLogicType);
        params.put("clusterLogicId", clusterLogicId);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<List<ClusterRegionVO>>>(){});
    }

    public static Result<Void> removeRegion(Long regionId) throws IOException {
        String path = String.format("%s/delete", PHY_CLUSTER_REGION);
        Map<String, Object> params = new HashMap<>();
        params.put("regionId", regionId);
        return JSON.parseObject(AriusClient.delete(path, params, null), new TypeReference<Result<Void>>(){});
    }

    public static Result<List<ESClusterRoleHostVO>> getRegionNodes(Long regionId) throws IOException {
        String path = String.format("%s/%d/nodes", PHY_CLUSTER_REGION, regionId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ESClusterRoleHostVO>>>(){});
    }

    public static Result<List<IndexTemplatePhysicalVO>> getRegionPhysicalTemplates(Long regionId) throws IOException {
        String path = String.format("%s/%d/templates", PHY_CLUSTER_REGION, regionId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<IndexTemplatePhysicalVO>>>(){});
    }
}