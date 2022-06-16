package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import static org.mockito.MockitoAnnotations.initMocks;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPhyPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl.ClusterPhyServiceImpl;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.PhyClusterDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;

/**
 * @author wpk
 * @date 2021/07/08
 */
public class ClusterPhyServiceTest {

    @InjectMocks
    private ClusterPhyServiceImpl ClusterPhyService;

    @Mock
    private PhyClusterDAO clusterDAO;

    @Mock
    private ESClusterService esClusterService;

    @Mock
    private ClusterRoleService clusterRoleService;

    @Mock
    private ClusterRoleHostService clusterRoleHostService;

    @Mock
    private ESPluginService esPluginService;

    private final String           cluster      = "lyn-test-public12-08";
    private final String           existCluster = "hsl-test-exist-Cluster";
    private final Integer          id      = 157;

    @BeforeEach
    public void setUp() {
        initMocks(this);
        ClusterPhyPO po = new ClusterPhyPO();
        po.setCluster(cluster);
        po.setId(id);
        po.setHttpWriteAddress("2.0.0.0");
        po.setPlugIds("1234,4321");
        List<String> strList = new ArrayList<>();
        strList.add(cluster);
        List<ClusterPhyPO> poList = new ArrayList<>();
        poList.add(po);

        Mockito.when(clusterDAO.listAllName()).thenReturn(strList);
        Mockito.when(clusterDAO.listByNames(Mockito.anyList())).thenReturn(poList);
        Mockito.when(clusterDAO.listByCondition(Mockito.any())).thenReturn(poList);
        Mockito.when(clusterDAO.listAll()).thenReturn(poList);
        Mockito.when(clusterDAO.pagingByCondition(Mockito.any())).thenReturn(poList);
        Mockito.when(clusterDAO.getById(id)).thenReturn(po);
        Mockito.when(clusterDAO.getById(id + 1)).thenReturn(null);
        Mockito.when(clusterDAO.delete(id)).thenReturn(1);
        Mockito.when(clusterDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(clusterDAO.update(Mockito.any())).thenReturn(1);
        Mockito.when(clusterDAO.getByName(cluster)).thenReturn(po);
        Mockito.when(clusterDAO.getByName(existCluster)).thenReturn(null);
        Mockito.when(clusterDAO.getTotalHitByCondition(Mockito.any())).thenReturn(10L);
    }

    @Test
    public void listClustersTest() {
        ClusterPhyDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertTrue(ClusterPhyService.listClustersByCondt(esClusterDTO).stream()
            .anyMatch(esClusterPhy -> esClusterPhy.getCluster().equals(esClusterDTO.getCluster())));
        Mockito.when(clusterDAO.listByCondition(Mockito.any())).thenReturn(null);
        Assertions.assertTrue(CollectionUtils.isEmpty(ClusterPhyService.listClustersByCondt(esClusterDTO)));
    }

    @Test
    public void deleteClusterByIdTest() {
        Assertions.assertEquals(Result.buildNotExist("集群不存在").getMessage(),
                ClusterPhyService.deleteClusterById(id + 1, CustomDataSource.OPERATOR).getMessage());
        Assertions.assertTrue(ClusterPhyService.deleteClusterById(id, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void createClusterTest() {
        ClusterPhyDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterDTO.setCluster(existCluster);
        Assertions.assertEquals(Result.buildParamIllegal("集群信息为空").getMessage(),
                ClusterPhyService.createCluster(null, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setEsVersion(null);
        Assertions.assertEquals(Result.buildParamIllegal("es版本为空").getMessage(),
                ClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setIdc(null);
        Assertions.assertEquals(Result.buildParamIllegal("机房信息为空").getMessage(),
                ClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setDataCenter(null);
        Assertions.assertEquals(Result.buildParamIllegal("数据中心为空").getMessage(),
                ClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setType(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群类型为空").getMessage(),
                ClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setHttpAddress(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群HTTP地址为空").getMessage(),
                ClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setCluster(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群名称为空").getMessage(),
                ClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterDTO.setCluster(existCluster);
        esClusterDTO.setEsVersion("test.test.test");
        Assertions.assertEquals(Result.buildParamIllegal("es版本号非法").getMessage(),
                ClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setDataCenter("wpkTest");
        Assertions.assertEquals(Result.buildParamIllegal("数据中心非法").getMessage(),
                ClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterDTO.setCluster(existCluster);
        Assertions.assertTrue(ClusterPhyService.createCluster(esClusterDTO, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void editClusterTest() {
        ClusterPhyDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        Assertions.assertTrue(ClusterPhyService.editCluster(esClusterDTO, CustomDataSource.OPERATOR).success());
        esClusterDTO.setId(null);
        Assertions.assertEquals(Result.buildParamIllegal("集群ID为空").getMessage(),
                ClusterPhyService.editCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
        esClusterDTO.setId(id + 1);
        Assertions.assertEquals(Result.buildNotExist("集群不存在").getMessage(),
                ClusterPhyService.editCluster(esClusterDTO, CustomDataSource.OPERATOR).getMessage());
    }

    @Test
    public void getClusterByNameTest() {
        ClusterRoleInfo clusterRoleInfo = new ClusterRoleInfo();
        clusterRoleInfo.setRole("wpk");
        Mockito.when(clusterRoleService.getAllRoleClusterByClusterId(Mockito.any()))
                .thenReturn(Collections.singletonList(clusterRoleInfo));
        ClusterRoleHost clusterRoleHost = new ClusterRoleHost();
        Mockito.when(clusterRoleHostService.getByRoleClusterId(Mockito.anyLong()))
                .thenReturn(Collections.singletonList(clusterRoleHost));
        ClusterPhy clusterPhy = ClusterPhyService.getClusterByName(cluster);
        Assertions.assertTrue(clusterPhy.getClusterRoleInfos().stream()
                .anyMatch(esRoleCluster1 -> esRoleCluster1.getRole().equals(clusterRoleInfo.getRole())));
    }

    @Test
    public void listAllClustersTest() {
        Assertions.assertTrue(
                ClusterPhyService.listAllClusters().stream().anyMatch(esClusterPhy -> esClusterPhy.getId().equals(id)));
    }

    @Test
    public void listClusterNamesTest() {
        Assertions.assertTrue(ClusterPhyService.listClusterNames().stream()
                .anyMatch(clusterName -> clusterName.equals(cluster)));
        Mockito.when(clusterDAO.listAllName()).thenThrow(new RuntimeException());
        Assertions.assertTrue(CollectionUtils.isEmpty(ClusterPhyService.listClusterNames()));
    }

    @Test
    public void listClustersByNamesTest() {
        Assertions.assertTrue(ClusterPhyService.listClustersByNames(Arrays.asList(cluster,existCluster)).stream()
            .anyMatch(esClusterPhy -> esClusterPhy.getCluster().equals(cluster)));
        Assertions.assertTrue(CollectionUtils.isEmpty(ClusterPhyService.listClustersByNames(Collections.emptyList())));
    }

    @Test
    public void isClusterExistsTest() {
        Assertions.assertFalse(ClusterPhyService.isClusterExists(existCluster));
        Assertions.assertTrue(ClusterPhyService.isClusterExists(cluster));
    }

    @Test
    public void listClusterPluginsTest() {
        PluginDTO pluginDTO = CustomDataSource.esPluginDTOFactory();
        // 为mock的插件对象设置插件的文件名称
        pluginDTO.setFileName("test");
        pluginDTO.setId(1234L);
        Mockito.when(esPluginService.listClusterAndDefaultESPlugin(id.toString()))
                .thenReturn(null);
        Assertions.assertTrue(ClusterPhyService.listClusterPlugins(cluster).isEmpty());
        Assertions.assertTrue(ClusterPhyService.listClusterPlugins(existCluster).isEmpty());
        Mockito.when(esPluginService.listClusterAndDefaultESPlugin(id.toString()))
                .thenReturn(Collections.singletonList(ConvertUtil.obj2Obj(pluginDTO, PluginPO.class)));
        Assertions.assertTrue(ClusterPhyService.listClusterPlugins(cluster).stream()
                .map(Plugin::getId)
                .anyMatch(pId -> pluginDTO.getId().equals(pId)));
    }

    @Test
    public void getClusterByIdTest() {
        Assertions.assertNull(ClusterPhyService.getClusterById(id + 1));
        Assertions.assertNotNull(ClusterPhyService.getClusterById(id));
    }

    @Test
    public void ensureDcdrRemoteClusterTest() throws ESOperateException {
        Assertions.assertFalse(ClusterPhyService.ensureDCDRRemoteCluster(cluster, null));
        Mockito.when(esClusterService.hasSettingExist(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Assertions.assertTrue(
                ClusterPhyService.ensureDCDRRemoteCluster(cluster, cluster));
        Mockito.when(esClusterService.hasSettingExist(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Mockito.when(esClusterService.syncPutRemoteCluster(Mockito.anyString(), Mockito.anyString(), Mockito.isNull(),
                Mockito.anyInt())).thenReturn(true);
        Assertions.assertFalse(
                ClusterPhyService.ensureDCDRRemoteCluster(cluster, existCluster));
        Assertions.assertFalse(
                ClusterPhyService.ensureDCDRRemoteCluster(existCluster, cluster));
        Mockito.when(esClusterService.hasSettingExist(Mockito.any(), Mockito.any())).thenReturn(false);
        Mockito.when(esClusterService.syncPutRemoteCluster(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.anyInt())).thenReturn(true);
        Assertions.assertTrue(
                ClusterPhyService.ensureDCDRRemoteCluster(cluster, cluster));
    }

    @Test
    public void getRoutingAllocationAwarenessAttributesTest() {
        Assertions.assertTrue(ClusterPhyService.getRoutingAllocationAwarenessAttributes(existCluster).isEmpty());
        Mockito.when(esClusterService.syncGetAllNodesAttributes(Mockito.anyString())).thenReturn(new HashSet<String>(){{add("aaa");}});;
        Assertions.assertFalse(ClusterPhyService.getRoutingAllocationAwarenessAttributes(cluster).isEmpty());
    }

    @Test
    public void updatePhyClusterDynamicConfigTest() {
        ClusterSettingDTO clusterSettingDTO = CustomDataSource.clusterSettingDTOFactory();
        clusterSettingDTO.setClusterName(existCluster);
        Assertions.assertEquals(Result.buildNotExist("集群不存在").getMessage(),
            ClusterPhyService.updatePhyClusterDynamicConfig(clusterSettingDTO).getMessage());
        clusterSettingDTO.setClusterName(cluster);
        clusterSettingDTO.setKey("unknown");
        Assertions.assertEquals(Result.buildNotExist("传入的字段类型未知").getMessage(),
            ClusterPhyService.updatePhyClusterDynamicConfig(clusterSettingDTO).getMessage());
        clusterSettingDTO.setKey("cluster.routing.allocation.node_concurrent_recoveries");
        clusterSettingDTO.setValue("=aa");
        Assertions.assertEquals(Result.buildNotExist("传入的字段参数格式有误").getMessage(),
            ClusterPhyService.updatePhyClusterDynamicConfig(clusterSettingDTO).getMessage());
        clusterSettingDTO = CustomDataSource.clusterSettingDTOFactory();
        clusterSettingDTO.setClusterName(cluster);
        //设置对应的配置
        Mockito.when(esClusterService.syncPutPersistentConfig(Mockito.anyString(), Mockito.anyMap())).thenReturn(true);
        Assertions.assertTrue(ClusterPhyService.updatePhyClusterDynamicConfig(clusterSettingDTO).success());
    }

    @Test
    public void pagingGetClusterPhyByConditionTest() {
        //条件查询
        ClusterPhyConditionDTO clusterPhyConditionDTO = clusterPhyConditionDTOFactory();
        //按照集群名称进行查询
        Assertions.assertTrue(ClusterPhyService.pagingGetClusterPhyByCondition(clusterPhyConditionDTO)
                .stream()
                .anyMatch(clusterPhy -> clusterPhy.getCluster().equals(cluster)));
        Mockito.when(clusterDAO.pagingByCondition(Mockito.any())).thenThrow(new RuntimeException());
        Assertions.assertTrue(ClusterPhyService.pagingGetClusterPhyByCondition(clusterPhyConditionDTO).isEmpty());
    }

    @Test
    public void fuzzyClusterPhyHitByConditionTest() {
        //模糊查询
        ClusterPhyConditionDTO clusterPhyConditionDTO = clusterPhyConditionDTOFactory();
        //按照集群名称进行查询
        Assertions.assertTrue(ClusterPhyService.fuzzyClusterPhyHitByCondition(clusterPhyConditionDTO) >= 1);
    }
    @Test
    public void isClusterExistsByPackageIdTest() {
        //模糊查询

        Mockito.when(clusterDAO.getTotalHitByPackageId(Mockito.any())).thenReturn(2L);
        //按照集群名称进行查询
        Assertions.assertTrue(ClusterPhyService.isClusterExistsByPackageId(1234L));
    }

    @Test
    public void updatePluginIdsByIdTest() {
        Mockito.when(clusterDAO.updatePluginIdsById(Mockito.any(), Mockito.anyInt())).thenReturn(1);
        String pluginIds = null;
        Assertions.assertTrue(ClusterPhyService.updatePluginIdsById(pluginIds, id).success());
        pluginIds = "135";
        Assertions.assertTrue(ClusterPhyService.updatePluginIdsById(pluginIds, id).success());
        pluginIds = "555";
        Assertions.assertTrue(ClusterPhyService.updatePluginIdsById(pluginIds, id).success());
    }

    private ClusterPhyConditionDTO clusterPhyConditionDTOFactory() {
        ClusterPhyConditionDTO clusterPhyConditionDTO = new ClusterPhyConditionDTO();
        clusterPhyConditionDTO.setCluster(cluster);
        clusterPhyConditionDTO.setPage(1L);
        clusterPhyConditionDTO.setSize(10L);
        return clusterPhyConditionDTO;
    }
}
