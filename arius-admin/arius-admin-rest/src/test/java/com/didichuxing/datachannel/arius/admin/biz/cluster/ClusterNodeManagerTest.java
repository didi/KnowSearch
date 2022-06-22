package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostWithRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;

public class ClusterNodeManagerTest extends AriusAdminApplicationTest {
    @Autowired
    private ClusterNodeManager clusterNodeManager;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Test
    public void listDivide2ClusterNodeInfoTest(){
        ClusterPhyDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Result<List<ESClusterRoleHostWithRegionInfoVO>> ret = clusterNodeManager.listDivide2ClusterNodeInfo(esClusterDTO.getId().longValue());
        Assertions.assertNotNull(ret);
        if (ret.success()) { Assertions.assertNotNull(ret.getData());}
        if (ret.failed()) { Assertions.assertNotNull(ret.getMessage());}
    }

    @Test
    public void createNode2RegionTest(){
        ClusterPhyDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        ClusterRegionWithNodeInfoDTO param = new ClusterRegionWithNodeInfoDTO();
        List<Integer> dataNodeList = Lists.newArrayList(1185, 1191, 1197);
        param.setBindingNodeIds(dataNodeList);
        param.setPhyClusterName("logi-elasticsearch-7.6.0");
        param.setName("region-lyn");

        Result<List<Long>> ret = clusterNodeManager.createMultiNode2Region(Lists.newArrayList(param), AriusUser.SYSTEM.getDesc());
        Assertions.assertNotNull(ret);
        if (ret.success()) { Assertions.assertNotNull(ret.getData());}
        if (ret.failed())  { Assertions.assertNotNull(ret.getMessage());}
    }

    @Test
    public void editNode2RegionTest(){
        ClusterPhyDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        ClusterRegionWithNodeInfoDTO param  = new ClusterRegionWithNodeInfoDTO();
        List<Integer> bindingDataNodeList   = Lists.newArrayList(1185, 1191);
        List<Integer> unBindingDataNodeList = Lists.newArrayList(1197);
        param.setId(1993L);
        param.setBindingNodeIds(bindingDataNodeList);
        param.setUnBindingNodeIds(unBindingDataNodeList);
        param.setPhyClusterName("logi-elasticsearch-7.6.0");
        param.setName("region-lyn");

        Result<Boolean> ret = clusterNodeManager.editMultiNode2Region(Lists.newArrayList(param), AriusUser.SYSTEM.getDesc());
        Assertions.assertNotNull(ret);
        if (ret.success()) { Assertions.assertTrue(ret.getData());}
        if (ret.failed())  { Assertions.assertNotNull(ret.getMessage());}
    }

     @Test
    public void getClusterPhyRegionInfosTest() {
        String clusterName = "logi-elasticsearch-7.6.0";
        ClusterPhy clusterphy = clusterPhyService.getClusterByName(clusterName);
        Result<List<ESClusterRoleHostVO>> clusterPhyRegionInfos = clusterNodeManager.listClusterPhyNode(clusterphy.getId());
        Assertions.assertTrue(clusterPhyRegionInfos.success());
        List<ESClusterRoleHostVO> esClusterRoleHostVOS = clusterPhyRegionInfos.getData();
        List<String> masterRegion = org.apache.commons.compress.utils.Lists.newArrayList();
        List<String> clientRegion = org.apache.commons.compress.utils.Lists.newArrayList();
        List<String> dataRegion = org.apache.commons.compress.utils.Lists.newArrayList();
        esClusterRoleHostVOS.forEach(esClusterRoleHostVO -> {
            if(esClusterRoleHostVO.getRole().equals(ESClusterNodeRoleEnum.DATA_NODE.getCode())) {
                dataRegion.add(esClusterRoleHostVO.getCluster());
            }
            if(esClusterRoleHostVO.getRole().equals(ESClusterNodeRoleEnum.MASTER_NODE.getCode())) {
                masterRegion.add(esClusterRoleHostVO.getCluster());
            }
            if(esClusterRoleHostVO.getRole().equals(ESClusterNodeRoleEnum.CLIENT_NODE.getCode())) {
                clientRegion.add(esClusterRoleHostVO.getCluster());
            }

        });
        Assertions.assertFalse(masterRegion.isEmpty());
        Assertions.assertFalse(clientRegion.isEmpty());
        Assertions.assertFalse(dataRegion.isEmpty());
        Assertions.assertEquals(dataRegion.get(0), clusterName);
    }
}
