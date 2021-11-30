package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.Assert;
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
public class ClusterPhyServiceTest extends AriusAdminApplicationTests {

    private static final String    OPERATOR = "wpk";

    @Autowired
    private ClusterPhyService      esClusterPhyService;

    @Autowired
    private ClusterDAO             clusterDAO;

    @Autowired
    private ESClusterNodeService   esClusterNodeService;

    @MockBean
    private ESClusterService       esClusterService;

    @MockBean
    private RegionRackService      regionRackService;

    @MockBean
    private RoleClusterService     roleClusterService;

    @MockBean
    private RoleClusterHostService roleClusterHostService;

    @MockBean
    private ESPluginService esPluginService;

    @Test
    public void listClustersTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertTrue(esClusterPhyService.createCluster(esClusterDTO, OPERATOR).success());
        List<ClusterPhy> esClusterPhies = esClusterPhyService.listClustersByCondt(esClusterDTO);
        Assertions.assertTrue(esClusterPhies.stream()
            .anyMatch(esClusterPhy -> esClusterPhy.getCluster().equals(esClusterDTO.getCluster())));
    }

    @Test
    public void deleteClusterByIdTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertEquals(Result.buildNotExist("集群不存在").getMessage(),
            esClusterPhyService.deleteClusterById(id + 1, OPERATOR).getMessage());
        Mockito.when(regionRackService.listAssignedRacksByClusterName(Mockito.any()))
            .thenReturn(new ArrayList<>(Collections.singletonList(new ClusterLogicRackInfo())));
        Assertions.assertEquals(Result.buildParamIllegal("集群region已配置给逻辑集群，无法删除").getMessage(),
            esClusterPhyService.deleteClusterById(id, OPERATOR).getMessage());
        Mockito.when(regionRackService.listAssignedRacksByClusterName(Mockito.any())).thenReturn(new ArrayList<>());
        Assertions.assertTrue(esClusterPhyService.deleteClusterById(id, OPERATOR).success());
    }

    @Test
    public void createClusterTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertEquals(Result.buildParamIllegal("集群信息为空").getMessage(),
            esClusterPhyService.createCluster(null, OPERATOR).getMessage());
        esClusterDTO.setTemplateSrvs(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群的索引服务id列表为空").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO.setEsVersion(null);
        Assertions.assertEquals(Result.buildParamIllegal("es版本为空").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO.setIdc(null);
        Assertions.assertEquals(Result.buildParamIllegal("机房信息为空").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO.setDataCenter(null);
        Assertions.assertEquals(Result.buildParamIllegal("数据中心为空").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO.setType(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群类型为空").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO.setHttpAddress(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群HTTP地址为空").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO.setDesc(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群描述为空").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO.setCluster(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群名称为空").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterDTO.setEsVersion("test.test.test");
        Assertions.assertEquals(Result.buildParamIllegal("es版本号非法").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO.setDataCenter("wpkTest");
        Assertions.assertEquals(Result.buildParamIllegal("数据中心非法").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO.setType(123);
        Assertions.assertEquals(Result.buildParamIllegal("集群类型非法").getMessage(),
            esClusterPhyService.createCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertTrue(esClusterPhyService.createCluster(esClusterDTO, OPERATOR).success());
    }

    @Test
    public void editClusterTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertTrue(esClusterPhyService.editCluster(esClusterDTO, OPERATOR).success());
        esClusterDTO.setId(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群ID为空").getMessage(),
            esClusterPhyService.editCluster(esClusterDTO, OPERATOR).getMessage());
        esClusterDTO.setId(id + 1);
        Assertions.assertEquals(Result.buildNotExist("集群不存在").getMessage(),
            esClusterPhyService.editCluster(esClusterDTO, OPERATOR).getMessage());
    }

    @Test
    public void getClusterByNameTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertNull(esClusterPhyService.getClusterByName(esClusterDTO.getCluster()));
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
        RoleCluster roleCluster = new RoleCluster();
        roleCluster.setRole("wpk");
        Mockito.when(roleClusterService.getAllRoleClusterByClusterId(Mockito.any()))
            .thenReturn(Collections.singletonList(roleCluster));
        RoleClusterHost roleClusterHost = new RoleClusterHost();
        Mockito.when(roleClusterHostService.getByRoleClusterId(Mockito.anyLong()))
            .thenReturn(Collections.singletonList(roleClusterHost));
        ClusterPhy clusterPhy = esClusterPhyService.getClusterByName(esClusterDTO.getCluster());
        Assertions.assertTrue(clusterPhy.getRoleClusters().stream()
            .anyMatch(esRoleCluster1 -> esRoleCluster1.getRole().equals(roleCluster.getRole())));
    }

    @Test
    public void listAllClustersTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertTrue(
            esClusterPhyService.listAllClusters().stream().anyMatch(esClusterPhy -> esClusterPhy.getId().equals(id)));
    }

    @Test
    public void isClusterExistsTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertFalse(esClusterPhyService.isClusterExists(esClusterDTO.getCluster()));
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
        Assertions.assertTrue(esClusterPhyService.isClusterExists(esClusterDTO.getCluster()));
    }

    @Test
    public void isRacksExistsTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        RoleClusterHost roleClusterHost = new RoleClusterHost();
        String rack = "ColdTest";
        roleClusterHost.setRack(rack);
        roleClusterHost.setRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        Mockito.when(roleClusterHostService.getNodesByCluster(esClusterDTO.getCluster()))
            .thenReturn(Collections.singletonList(roleClusterHost));
        Assertions.assertTrue(esClusterPhyService.isRacksExists(esClusterDTO.getCluster(), rack));
        Assertions.assertFalse(esClusterPhyService.isRacksExists(esClusterDTO.getCluster(), null));
    }

    @Test
    public void getClusterRacksTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Mockito.when(roleClusterHostService.getNodesByCluster(Mockito.anyString())).thenReturn(null);
        Assertions.assertTrue(esClusterPhyService.getClusterRacks(esClusterDTO.getCluster()).isEmpty());
        RoleClusterHost roleClusterHost = new RoleClusterHost();
        String rack = "wpk";
        roleClusterHost.setRack(rack);
        Mockito.when(roleClusterHostService.getNodesByCluster(esClusterDTO.getCluster()))
            .thenReturn(Collections.singletonList(roleClusterHost));
        roleClusterHost.setRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        Assertions.assertTrue(
            esClusterPhyService.getClusterRacks(esClusterDTO.getCluster()).stream().anyMatch(s -> s.equals(rack)));
    }

    @Test
    public void listHotRacksTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        RoleClusterHost roleClusterHost = new RoleClusterHost();
        String rack = "HotTest";
        roleClusterHost.setRack(rack);
        roleClusterHost.setRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        Mockito.when(roleClusterHostService.getNodesByCluster(esClusterDTO.getCluster()))
            .thenReturn(Collections.singletonList(roleClusterHost));
        Assertions.assertTrue(esClusterPhyService.listHotRacks(esClusterDTO.getCluster()).contains(rack));
    }

    @Test
    public void listColdRacksTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        RoleClusterHost roleClusterHost = new RoleClusterHost();
        String rack = "ColdTest";
        roleClusterHost.setRack(rack);
        roleClusterHost.setRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        Mockito.when(roleClusterHostService.getNodesByCluster(esClusterDTO.getCluster()))
            .thenReturn(Collections.singletonList(roleClusterHost));
        Assertions.assertTrue(esClusterPhyService.listColdRacks(esClusterDTO.getCluster()).contains(rack));

    }

    @Test
    public void listClusterPluginsTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        ESPluginDTO esPluginDTO = CustomDataSource.esPluginDTOFactory();
        Mockito.when(esPluginService.listClusterAndDefaultESPlugin(id.toString()))
                        .thenReturn(null);
        Assertions.assertTrue(esClusterPhyService.listClusterPlugins(esClusterDTO.getCluster()).isEmpty());
        Mockito.when(esPluginService.listClusterAndDefaultESPlugin(id.toString()))
                        .thenReturn(Collections.singletonList(ConvertUtil.obj2Obj(esPluginDTO, ESPluginPO.class)));
        Assertions.assertEquals(esPluginDTO.getFileName(),esClusterPhyService.listClusterPlugins(esClusterDTO.getCluster()));
    }

    @Test
    public void getClusterByIdTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertNull(esClusterPhyService.getClusterById(id + 1));
        Assertions.assertNotNull(esClusterPhyService.getClusterById(id));
    }

    @Test
    public void getWriteClientCountTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertEquals(1, esClusterPhyService.getWriteClientCount(esClusterDTO.getCluster()));
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
        String httpWriteAddress = "1.0.0.0,2.0.0.0";
        int length = httpWriteAddress.split(",").length;
        Assertions.assertEquals(length, esClusterPhyService.getWriteClientCount(esClusterDTO.getCluster()));
    }

    @Test
    public void ensureDcdrRemoteClusterTest() throws ESOperateException {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        ESClusterDTO remoteESClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertFalse(esClusterPhyService.ensureDcdrRemoteCluster(esClusterDTO.getCluster(), null));
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
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
        esClusterPhyService.createCluster(esClusterDTO, OPERATOR);
        Integer id = clusterDAO.getByName(esClusterDTO.getCluster()).getId();
        Assertions.assertNull(esClusterPhyService.listPhysicClusterRoles(id + 1));
        RoleCluster roleCluster = new RoleCluster();
        roleCluster.setRole("wpk");
        Mockito.when(roleClusterService.getAllRoleClusterByClusterId(Mockito.any()))
            .thenReturn(Collections.singletonList(roleCluster));
        Assertions.assertTrue(esClusterPhyService.listPhysicClusterRoles(id).stream()
            .anyMatch(esRoleCluster1 -> esRoleCluster1.getRole().equals(roleCluster.getRole())));
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
        Assertions.assertTrue(esClusterPhyService.fuzzyClusterPhyHitByCondition(clusterPhyConditionDTO) > 1);
    }

    @Test
    public void updatePluginIdsByIdTest() {
        Integer clusterId = 473;
        String pluginIds = null;
        Assertions.assertTrue(esClusterPhyService.updatePluginIdsById(pluginIds, clusterId).success());
        pluginIds = "135";
        Assertions.assertTrue(esClusterPhyService.updatePluginIdsById(pluginIds, clusterId).success());
        pluginIds = "555";
        Assertions.assertTrue(esClusterPhyService.updatePluginIdsById(pluginIds, clusterId).success());
    }

    private ClusterPhyConditionDTO clusterPhyConditionDTOFactory() {
        ClusterPhyConditionDTO clusterPhyConditionDTO = new ClusterPhyConditionDTO();
        clusterPhyConditionDTO.setCluster("logi-elasticsearch-7.6.0");
        clusterPhyConditionDTO.setFrom(0L);
        clusterPhyConditionDTO.setSize(1L);
        return clusterPhyConditionDTO;
    }
}
