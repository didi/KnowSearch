package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHostInfo;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author wpk
 * @date 2021/07/08
 */
@Transactional
@Rollback
public class ClusterPhyServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ClusterPhyService esClusterPhyService;

    @Autowired
    private ClusterDAO clusterDAO;

    @Autowired
    private ESClusterNodeService esClusterNodeService;

    @MockBean
    private ESClusterService esClusterService;

    @MockBean
    private RegionRackService regionRackService;

    @MockBean
    private ClusterRoleInfoService clusterRoleInfoService;

    @MockBean
    private ClusterRoleHostInfoService clusterRoleHostInfoService;

    @MockBean
    private ESPluginService esPluginService;

    @Test
    public void listClustersTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertTrue(esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).success());
        List<ClusterPhy> esClusterPhies = esClusterPhyService.listClustersByCondt(esClusterDTO);
        Assertions.assertTrue(esClusterPhies.stream()
                .anyMatch(esClusterPhy -> esClusterPhy.getCluster().equals(esClusterDTO.getCluster())));
    }

    @Test
    public void deleteClusterByIdTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertEquals(Result.buildNotExist("集群不存在").getMessage(),
                esClusterPhyService.deleteClusterById(id + 1, CustomDataSource.OPERATOR).getMessage());
        Assertions.assertTrue(esClusterPhyService.deleteClusterById(id, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void createClusterTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertEquals(Result.buildParamIllegal("集群信息为空").getMessage(),
                esClusterPhyService.createCluster(null, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setEsVersion(null);
        Assertions.assertEquals(Result.buildParamIllegal("es版本为空").getMessage(),
                esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setIdc(null);
        Assertions.assertEquals(Result.buildParamIllegal("机房信息为空").getMessage(),
                esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setDataCenter(null);
        Assertions.assertEquals(Result.buildParamIllegal("数据中心为空").getMessage(),
                esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setType(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群类型为空").getMessage(),
                esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setHttpAddress(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群HTTP地址为空").getMessage(),
                esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setCluster(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群名称为空").getMessage(),
                esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterDTO.setEsVersion("test.test.test");
        Assertions.assertEquals(Result.buildParamIllegal("es版本号非法").getMessage(),
                esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setDataCenter("wpkTest");
        Assertions.assertEquals(Result.buildParamIllegal("数据中心非法").getMessage(),
                esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertTrue(esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void editClusterTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertTrue(esClusterPhyService.editCluster(esClusterDTO, CustomDataSource.OPERATOR).success());
        esClusterDTO.setId(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群ID为空").getMessage(),
                esClusterPhyService.editCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setId(id + 1);
        Assertions.assertEquals(Result.buildNotExist("集群不存在").getMessage(),
                esClusterPhyService.editCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
    }

    @Test
    public void getClusterByNameTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertNull(esClusterPhyService.getClusterByName(esClusterDTO.getCluster()));
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        ClusterRoleInfo clusterRoleInfo = new ClusterRoleInfo();
        clusterRoleInfo.setRole("wpk");
        Mockito.when(clusterRoleInfoService.getAllRoleClusterByClusterId(Mockito.any()))
                .thenReturn(Collections.singletonList(clusterRoleInfo));
        ClusterRoleHostInfo clusterRoleHostInfo = new ClusterRoleHostInfo();
        Mockito.when(clusterRoleHostInfoService.getByRoleClusterId(Mockito.anyLong()))
                .thenReturn(Collections.singletonList(clusterRoleHostInfo));
        ClusterPhy clusterPhy = esClusterPhyService.getClusterByName(esClusterDTO.getCluster());
        Assertions.assertTrue(clusterPhy.getClusterRoleInfos().stream()
                .anyMatch(esRoleCluster1 -> esRoleCluster1.getRole().equals(clusterRoleInfo.getRole())));
    }

    @Test
    public void listAllClustersTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertTrue(
                esClusterPhyService.listAllClusters().stream().anyMatch(esClusterPhy -> esClusterPhy.getId().equals(id)));
    }

    @Test
    public void isClusterExistsTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertFalse(esClusterPhyService.isClusterExists(esClusterDTO.getCluster()));
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        Assertions.assertTrue(esClusterPhyService.isClusterExists(esClusterDTO.getCluster()));
    }

    @Test
    public void isRacksExistsTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = new ClusterRoleHostInfo();
        String rack = "ColdTest";
        clusterRoleHostInfo.setRack(rack);
        clusterRoleHostInfo.setRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        Mockito.when(clusterRoleHostInfoService.getNodesByCluster(esClusterDTO.getCluster()))
                .thenReturn(Collections.singletonList(clusterRoleHostInfo));
        Assertions.assertTrue(esClusterPhyService.isRacksExists(esClusterDTO.getCluster(), rack));
        Assertions.assertFalse(esClusterPhyService.isRacksExists(esClusterDTO.getCluster(), "test"));
    }

    @Test
    public void getClusterRacksTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Mockito.when(clusterRoleHostInfoService.getNodesByCluster(Mockito.anyString())).thenReturn(null);
        Assertions.assertTrue(esClusterPhyService.getClusterRacks(esClusterDTO.getCluster()).isEmpty());
        ClusterRoleHostInfo clusterRoleHostInfo = new ClusterRoleHostInfo();
        String rack = "wpk";
        clusterRoleHostInfo.setRack(rack);
        Mockito.when(clusterRoleHostInfoService.getNodesByCluster(esClusterDTO.getCluster()))
                .thenReturn(Collections.singletonList(clusterRoleHostInfo));
        clusterRoleHostInfo.setRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        Assertions.assertTrue(
                esClusterPhyService.getClusterRacks(esClusterDTO.getCluster()).stream().anyMatch(s -> s.equals(rack)));
    }

    @Test
    public void listHotRacksTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = new ClusterRoleHostInfo();
        String rack = "HotTest";
        clusterRoleHostInfo.setRack(rack);
        clusterRoleHostInfo.setRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        Mockito.when(clusterRoleHostInfoService.getNodesByCluster(esClusterDTO.getCluster()))
                .thenReturn(Collections.singletonList(clusterRoleHostInfo));
        Assertions.assertTrue(esClusterPhyService.listHotRacks(esClusterDTO.getCluster()).contains(rack));
    }

    @Test
    public void listColdRacksTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = new ClusterRoleHostInfo();
        String rack = "ColdTest";
        clusterRoleHostInfo.setRack(rack);
        clusterRoleHostInfo.setRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        Mockito.when(clusterRoleHostInfoService.getNodesByCluster(esClusterDTO.getCluster()))
                .thenReturn(Collections.singletonList(clusterRoleHostInfo));
        Assertions.assertTrue(esClusterPhyService.listColdRacks(esClusterDTO.getCluster()).contains(rack));

    }

    @Test
    public void listClusterPluginsTest() {
        PluginDTO pluginDTO = CustomDataSource.esPluginDTOFactory();
        // 为mock的插件对象设置插件的文件名称
        pluginDTO.setFileName("test");
        pluginDTO.setId(1234L);
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        // mock已经安装到集群的插件id列表
        esClusterDTO.setPlugIds(pluginDTO.getId().toString());
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Mockito.when(esPluginService.listClusterAndDefaultESPlugin(id.toString()))
                .thenReturn(null);
        Assertions.assertTrue(esClusterPhyService.listClusterPlugins(esClusterDTO.getCluster()).isEmpty());
        Mockito.when(esPluginService.listClusterAndDefaultESPlugin(id.toString()))
                .thenReturn(Collections.singletonList(ConvertUtil.obj2Obj(pluginDTO, PluginPO.class)));
        Assertions.assertTrue(esClusterPhyService.listClusterPlugins(esClusterDTO.getCluster()).stream()
                .map(Plugin::getId)
                .anyMatch(pId -> pluginDTO.getId().equals(pId)));
    }

    @Test
    public void getClusterByIdTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertNull(esClusterPhyService.getClusterById(id + 1));
        Assertions.assertNotNull(esClusterPhyService.getClusterById(id));
    }

    @Test
    public void getWriteClientCountTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertEquals(0, esClusterPhyService.getWriteClientCount(esClusterDTO.getCluster()));
        String httpWriteAddress = "1.0.0.0,2.0.0.0";
        esClusterDTO.setHttpWriteAddress(httpWriteAddress);
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        int length = httpWriteAddress.split(",").length;
        Assertions.assertEquals(length, esClusterPhyService.getWriteClientCount(esClusterDTO.getCluster()));
    }

    @Test
    public void ensureDcdrRemoteClusterTest() throws ESOperateException {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        ESClusterDTO remoteESClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertFalse(esClusterPhyService.ensureDcdrRemoteCluster(esClusterDTO.getCluster(), null));
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        Mockito.when(esClusterService.hasSettingExist(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Assertions.assertTrue(
                esClusterPhyService.ensureDcdrRemoteCluster(esClusterDTO.getCluster(), remoteESClusterDTO.getCluster()));
        Mockito.when(esClusterService.hasSettingExist(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Mockito.when(esClusterService.syncPutRemoteCluster(Mockito.anyString(), Mockito.anyString(), Mockito.isNull(),
                Mockito.anyInt())).thenReturn(true);
        Assertions.assertFalse(
                esClusterPhyService.ensureDcdrRemoteCluster(esClusterDTO.getCluster(), remoteESClusterDTO.getCluster()));
    }

    @Test
    public void listPhysicClusterRolesTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertTrue(CollectionUtils.isEmpty(esClusterPhyService.listPhysicClusterRoles(id + 1)));
        ClusterRoleInfo clusterRoleInfo = new ClusterRoleInfo();
        clusterRoleInfo.setRole("wpk");
        Mockito.when(clusterRoleInfoService.getAllRoleClusterByClusterId(Mockito.any()))
                .thenReturn(Collections.singletonList(clusterRoleInfo));
        Assertions.assertTrue(esClusterPhyService.listPhysicClusterRoles(id).stream()
                .anyMatch(esRoleCluster1 -> esRoleCluster1.getRole().equals(clusterRoleInfo.getRole())));
    }

    @Test
    public void getRoutingAllocationAwarenessAttributesTest() {
        String cluster = "logi-elasticsearch-7.6.0";
        Set<String> routingAllocationAwarenessAttributes = esClusterPhyService
                .getRoutingAllocationAwarenessAttributes(cluster);
        Assertions.assertTrue(routingAllocationAwarenessAttributes.isEmpty());
    }

    @Test
    public void updatePhyClusterDynamicConfigTest() {
        ClusterSettingDTO clusterSettingDTO = CustomDataSource.clusterSettingDTOFactory();
        //设置对应的配置
        Mockito.when(esClusterService.syncPutPersistentConfig(Mockito.anyString(), Mockito.anyMap())).thenReturn(true);
        Assertions.assertTrue(esClusterPhyService.updatePhyClusterDynamicConfig(clusterSettingDTO).success());
    }

    @Test
    public void pagingGetClusterPhyByConditionTest() {
        //条件查询
        ClusterPhyConditionDTO clusterPhyConditionDTO = clusterPhyConditionDTOFactory();
        //按照集群名称进行查询
        Assertions.assertTrue(esClusterPhyService.pagingGetClusterPhyByCondition(clusterPhyConditionDTO)
                .stream()
                .anyMatch(clusterPhy -> clusterPhy.getCluster().equals(clusterPhyConditionDTO.getCluster())));
    }

    @Test
    public void fuzzyClusterPhyHitByConditionTest() {
        //模糊查询
        ClusterPhyConditionDTO clusterPhyConditionDTO = clusterPhyConditionDTOFactory();
        //按照集群名称进行查询
        Assertions.assertTrue(esClusterPhyService.fuzzyClusterPhyHitByCondition(clusterPhyConditionDTO) >= 1);
    }

    @Test
    public void updatePluginIdsByIdTest() {
        ClusterPO clusterPO = clusterDAO.getByName(CustomDataSource.PHY_CLUSTER_NAME);
        Integer clusterId = clusterPO.getId();
        String pluginIds = null;
        Assertions.assertTrue(esClusterPhyService.updatePluginIdsById(pluginIds, clusterId).success());
        pluginIds = "135";
        Assertions.assertTrue(esClusterPhyService.updatePluginIdsById(pluginIds, clusterId).success());
        pluginIds = "555";
        Assertions.assertTrue(esClusterPhyService.updatePluginIdsById(pluginIds, clusterId).success());
    }

    private ClusterPhyConditionDTO clusterPhyConditionDTOFactory() {
        ClusterPhyConditionDTO clusterPhyConditionDTO = new ClusterPhyConditionDTO();
        clusterPhyConditionDTO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        clusterPhyConditionDTO.setPage(1L);
        clusterPhyConditionDTO.setSize(1L);
        return clusterPhyConditionDTO;
    }
}
