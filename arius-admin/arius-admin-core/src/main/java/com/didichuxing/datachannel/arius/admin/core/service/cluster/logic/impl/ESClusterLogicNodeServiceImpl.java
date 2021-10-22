package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicNodeService;
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
public class ESClusterLogicNodeServiceImpl implements ESClusterLogicNodeService {

    @Autowired
    private ESRegionRackService      esRegionRackService;

    @Autowired
    private ESRoleClusterHostService esRoleClusterHostService;

    /**
     * 获取逻辑集群对应的节点列表
     * @param clusterId 逻辑集群ID
     * @return
     */
    @Override
    public List<ESRoleClusterHost> getLogicClusterNodes(Long clusterId) {
        List<ESRoleClusterHost> logicClusterNodes = new ArrayList<>();

        List<ESClusterLogicRackInfo> logicClusterRacks = esRegionRackService.listLogicClusterRacks(clusterId);
        for (ESClusterLogicRackInfo clusterRack : logicClusterRacks) {
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
    public List<ESRoleClusterHost> getLogicClusterNodesIncludeNonDataNodes(Long clusterId) {
        List<ESRoleClusterHost> logicClusterNodes = new ArrayList<>();

        List<String> phyClusters = new ArrayList<>();
        List<ESClusterLogicRackInfo> logicClusterRacks = esRegionRackService.listLogicClusterRacks(clusterId);
        for (ESClusterLogicRackInfo clusterRack : logicClusterRacks) {
            logicClusterNodes.addAll(fetchNodesOfClusterRack(clusterRack));

            if (!phyClusters.contains(clusterRack.getPhyClusterName())) {
                phyClusters.add(clusterRack.getPhyClusterName());
            }
        }

        logicClusterNodes.addAll(getAllNonDataNodes(phyClusters));

        return logicClusterNodes;
    }

    /*************************************************private**********************************************************/
    /**
     * 获取所有物理集群非数据节点
     * @param phyClusters 物理集群列表
     * @return
     */
    private List<ESRoleClusterHost> getAllNonDataNodes(List<String> phyClusters) {
        List<ESRoleClusterHost> nonDataNodes = new ArrayList<>();

        for (String phyCluster : phyClusters) {
            nonDataNodes.addAll(fetchNodesOfClusterRack(buildEmptyRack(phyCluster)));
        }

        return nonDataNodes;
    }

    /**
     * 构建空Rack信息
     * @param phyCluster 物理集群
     * @return
     */
    private ESClusterLogicRackInfo buildEmptyRack(String phyCluster) {
        ESClusterLogicRackInfo rackInfo = new ESClusterLogicRackInfo();
        rackInfo.setPhyClusterName(phyCluster);
        rackInfo.setRack("");
        return rackInfo;
    }

    /**
     * 通过物理集群Rack信息获取对应节点信息
     * @param clusterRack 集群Rack信息
     * @return
     */
    private List<ESRoleClusterHost> fetchNodesOfClusterRack(ESClusterLogicRackInfo clusterRack) {
        if (clusterRack != null) {
            ESRoleClusterHostDTO query = new ESRoleClusterHostDTO();
            query.setCluster(clusterRack.getPhyClusterName());
            query.setRack(clusterRack.getRack());

            return esRoleClusterHostService.queryNodeByCondt(query);
        }

        return new ArrayList<>();
    }
}
