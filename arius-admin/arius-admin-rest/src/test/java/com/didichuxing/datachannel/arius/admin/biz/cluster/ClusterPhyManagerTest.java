package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ClusterPhyManagerTest extends AriusAdminApplicationTest {
    private  final  static  int APP_ID = 1;
    @Autowired
    private ClusterPhyManager clusterPhyManager;

    @Autowired
    private ClusterPhyService clusterPhyService;

    // @Test
    public void getClusterPhyRegionInfosTest() {
        String clusterName = "dc-test";
        ClusterPhy clusterphy = clusterPhyService.getClusterByName(clusterName);
        Result<List<ESRoleClusterHostVO>> clusterPhyRegionInfos = clusterPhyManager.getClusterPhyRegionInfos(clusterphy.getId());
        Assertions.assertTrue(clusterPhyRegionInfos.success());
        List<ESRoleClusterHostVO> esRoleClusterHostVOs = clusterPhyRegionInfos.getData();
        List<String> masterRegion = Lists.newArrayList();
        List<String> clientRegion = Lists.newArrayList();
        List<String> dataRegion = Lists.newArrayList();
        esRoleClusterHostVOs.forEach(esRoleClusterHostVO -> {
            if(esRoleClusterHostVO.getRole().equals(ESClusterNodeRoleEnum.DATA_NODE.getCode())) {
                dataRegion.add(esRoleClusterHostVO.getClusterLogicNames());
            }
            if(esRoleClusterHostVO.getRole().equals(ESClusterNodeRoleEnum.MASTER_NODE.getCode())) {
                masterRegion.add(esRoleClusterHostVO.getClusterLogicNames());
            }
            if(esRoleClusterHostVO.getRole().equals(ESClusterNodeRoleEnum.CLIENT_NODE.getCode())) {
                clientRegion.add(esRoleClusterHostVO.getClusterLogicNames());
            }

        });
        Assertions.assertTrue(masterRegion.isEmpty() || null == masterRegion.get(0));
        Assertions.assertTrue(clientRegion.isEmpty());
        Assertions.assertFalse(dataRegion.isEmpty());
        Assertions.assertEquals(dataRegion.get(0), clusterName);
    }

    @Test
     public void getTemplateSameVersionClusterNamesByTemplateIdTest() {
         Result<List<String>> rest = clusterPhyManager.getTemplateSameVersionClusterNamesByTemplateId(APP_ID, 37529);
         Assertions.assertTrue(rest.success());
         Assertions.assertTrue(null != rest.getData() && !rest.getData().isEmpty());
     }
}
