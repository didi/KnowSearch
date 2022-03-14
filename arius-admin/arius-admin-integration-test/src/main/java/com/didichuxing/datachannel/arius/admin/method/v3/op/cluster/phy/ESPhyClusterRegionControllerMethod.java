package com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.phy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.PhyClusterRackVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanRegionDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

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

    public static Result<Long> createRegion(CapacityPlanRegionDTO dto) throws IOException {
        String path = String.format("%s/add", PHY_CLUSTER_REGION);
        return JSON.parseObject(AriusClient.post(path, dto), new TypeReference<Result<Long>>(){});
    }

    public static Result<List<PhyClusterRackVO>> listPhyClusterRacks(String cluster) throws IOException {
        String path = String.format("%s/phyClusterRacks", PHY_CLUSTER_REGION);
        Map<String, Object> params = new HashMap<>();
        params.put("cluster", cluster);
        return JSON.parseObject(AriusClient.post(path, params), new TypeReference<Result<List<PhyClusterRackVO>>>(){});
    }

    public static Result<Void> editClusterRegion(CapacityPlanRegionDTO dto) throws IOException {
        String path = String.format("%s/edit", PHY_CLUSTER_REGION);
        return JSON.parseObject(AriusClient.put(path, dto), new TypeReference<Result<Void>>(){});
    }

    public static Result<Void> removeRegion(Long regionId) throws IOException {
        String path = String.format("%s/delete", PHY_CLUSTER_REGION);
        Map<String, Object> params = new HashMap<>();
        params.put("regionId", regionId);
        return JSON.parseObject(AriusClient.delete(path, params, null), new TypeReference<Result<Void>>(){});
    }

    public static Result<List<ESRoleClusterHostVO>> getRegionNodes(Long regionId) throws IOException {
        String path = String.format("%s/%d/nodes", PHY_CLUSTER_REGION, regionId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ESRoleClusterHostVO>>>(){});
    }

    public static Result<List<IndexTemplatePhysicalVO>> getRegionPhysicalTemplates(Long regionId) throws IOException {
        String path = String.format("%s/%d/templates", PHY_CLUSTER_REGION, regionId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<IndexTemplatePhysicalVO>>>(){});
    }

    public static Result<Set<String>> getClusterPhyRacks(String clusterPhyName) throws IOException {
        String path = String.format("%s/%s/rack", PHY_CLUSTER_REGION, clusterPhyName);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<Set<String>>>(){});
    }
}
