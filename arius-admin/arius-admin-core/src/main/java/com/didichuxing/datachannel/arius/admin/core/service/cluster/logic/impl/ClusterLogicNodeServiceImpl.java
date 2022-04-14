package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 逻辑集群节点服务类
 * @author wangshu
 * @date 2020/09/10
 */
@Service
public class ClusterLogicNodeServiceImpl implements ClusterLogicNodeService {

    @Autowired
    private RegionRackService regionRackService;

    @Autowired
    private RoleClusterHostService roleClusterHostService;

    /**
     * 获取逻辑集群对应的节点列表
     * @param clusterId 逻辑集群ID
     * @return
     */
    @Override
    public List<RoleClusterHost> getLogicClusterNodes(Long clusterId) {
        List<RoleClusterHost> logicClusterNodes = new ArrayList<>();

        List<ClusterLogicRackInfo> logicClusterRacks = regionRackService.listLogicClusterRacks(clusterId);
        for (ClusterLogicRackInfo clusterRack : logicClusterRacks) {
            logicClusterNodes.addAll(fetchNodesOfClusterRack(clusterRack));
        }

        return logicClusterNodes;
    }

    /**
     * 获取逻辑集群所有节点包括对应物理集群非数据节点
     * @param clusterId 逻辑集群ID
     * @return
     */
    @Override
    public List<RoleClusterHost> getLogicClusterNodesIncludeNonDataNodes(Long clusterId) {
        List<RoleClusterHost> logicClusterNodes = new ArrayList<>();

        List<String> phyClusters = new ArrayList<>();
        List<ClusterLogicRackInfo> logicClusterRacks = regionRackService.listLogicClusterRacks(clusterId);
        for (ClusterLogicRackInfo clusterRack : logicClusterRacks) {
            if(null == clusterRack){continue;}

            logicClusterNodes.addAll(fetchNodesOfClusterRack(clusterRack));

            if (!phyClusters.contains(clusterRack.getPhyClusterName())) {
                phyClusters.add(clusterRack.getPhyClusterName());
            }
        }

        return logicClusterNodes;
    }

    /*************************************************private**********************************************************/

    /**
     * 通过物理集群Rack信息获取对应节点信息
     * @param clusterRack 集群Rack信息
     * @return
     */
    private List<RoleClusterHost> fetchNodesOfClusterRack(ClusterLogicRackInfo clusterRack) {
        if (clusterRack != null) {
            ESRoleClusterHostDTO query = new ESRoleClusterHostDTO();
            query.setCluster(clusterRack.getPhyClusterName());
            query.setRack(clusterRack.getRack());

            return roleClusterHostService.queryNodeByCondt(query);
        }

        return new ArrayList<>();
    }
}
