package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.PhyClusterDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;

public class ClusterPhyManagerTest extends AriusAdminApplicationTest {
    private  final  static  int APP_ID = 1;
    @Autowired
    private ClusterPhyManager clusterPhyManager;

    @MockBean
    private ClusterPhyService clusterPhyService;

    @Autowired
    private PhyClusterDAO clusterDAO;

    @MockBean
    private ClusterRoleService clusterRoleService;
    

    // @Test
    public void getClusterPhyRegionInfosTest() {
        String clusterName = "dc-test";
        ClusterPhy clusterphy = clusterPhyService.getClusterByName(clusterName);
        Result<List<ESClusterRoleHostVO>> clusterPhyRegionInfos = clusterPhyManager.getClusterPhyRegionInfos(clusterphy.getId());
        Assertions.assertTrue(clusterPhyRegionInfos.success());
        List<ESClusterRoleHostVO> esClusterRoleHostVOS = clusterPhyRegionInfos.getData();
        List<String> masterRegion = Lists.newArrayList();
        List<String> clientRegion = Lists.newArrayList();
        List<String> dataRegion = Lists.newArrayList();
        esClusterRoleHostVOS.forEach(esClusterRoleHostVO -> {
            if(esClusterRoleHostVO.getRole().equals(ESClusterNodeRoleEnum.DATA_NODE.getCode())) {
                dataRegion.add(esClusterRoleHostVO.getClusterLogicNames());
            }
            if(esClusterRoleHostVO.getRole().equals(ESClusterNodeRoleEnum.MASTER_NODE.getCode())) {
                masterRegion.add(esClusterRoleHostVO.getClusterLogicNames());
            }
            if(esClusterRoleHostVO.getRole().equals(ESClusterNodeRoleEnum.CLIENT_NODE.getCode())) {
                clientRegion.add(esClusterRoleHostVO.getClusterLogicNames());
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

     @Test
     public void pageGetClusterPhysTest() {
         //条件查询
         ClusterPhyConditionDTO clusterPhyConditionDTO = new ClusterPhyConditionDTO();
         clusterPhyConditionDTO.setPage(1L);
         clusterPhyConditionDTO.setSize(10L);

         PaginationResult<ClusterPhyVO> rest = clusterPhyManager.pageGetClusterPhys(clusterPhyConditionDTO, APP_ID);
         Assertions.assertTrue(rest.success());
         Assertions.assertEquals(10, rest.getData().getBizData().size());
         clusterPhyConditionDTO = new ClusterPhyConditionDTO();
         clusterPhyConditionDTO.setPage(1L);
         clusterPhyConditionDTO.setSize(10L);
         clusterPhyConditionDTO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
         rest = clusterPhyManager.pageGetClusterPhys(clusterPhyConditionDTO, APP_ID);
         Assertions.assertTrue(rest.success());
         Assertions.assertTrue(rest.getData().getBizData().stream()
             .anyMatch(clusterPhy -> clusterPhy.getCluster().equals(CustomDataSource.PHY_CLUSTER_NAME)));
     }

    @Test
    public void listPhysicClusterRolesTest() {
        ClusterPhyDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertTrue(CollectionUtils.isEmpty(clusterPhyManager.listClusterRolesByClusterId(id + 1)));
        ClusterRoleInfo clusterRoleInfo = new ClusterRoleInfo();
        clusterRoleInfo.setRole("wpk");
        Mockito.when(clusterRoleService.getAllRoleClusterByClusterId(Mockito.any()))
            .thenReturn(Collections.singletonList(clusterRoleInfo));
        Assertions.assertTrue(clusterPhyManager.listClusterRolesByClusterId(id).stream()
            .anyMatch(esRoleCluster1 -> esRoleCluster1.getRole().equals(clusterRoleInfo.getRole())));
    }
}
