package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHostInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.employee.EmployeeService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Transactional
@Rollback
public class ClusterLogicServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private LogicClusterDAO logicClusterDAO;

    @SpyBean
    private RegionRackService rackService;

    @MockBean
    private AppService appService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private TemplatePhyService templatePhyService;

    @MockBean
    private ClusterDAO clusterDAO;

    @MockBean
    private ESPluginService esPluginService;

    @MockBean
    private ClusterPhyService esClusterPhyService;

    @MockBean
    private ESMachineNormsService esMachineNormsService;

    @MockBean
    private RegionRackService regionRackService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    private static String OPERATOR = "wpk";

    @BeforeEach
    public void mockRules() {
        Mockito.when(appService.isAppExists(Mockito.anyInt())).thenReturn(true);
        Mockito.when(employeeService.checkUsers(Mockito.anyString())).thenReturn(Result.buildSucc());
    }

    @Test
    public void listLogicClustersTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Assertions.assertTrue(clusterLogicService.listClusterLogics(esLogicClusterDTO).isEmpty());
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        Assertions.assertTrue(clusterLogicService
                .listClusterLogics(esLogicClusterDTO)
                .stream()
                .anyMatch(esClusterLogic -> esClusterLogic.getId().equals(id)));
    }

    @Test
    public void listAllLogicClustersTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        Assertions.assertTrue(clusterLogicService
                .listAllClusterLogics()
                .stream()
                .anyMatch(esClusterLogic -> esClusterLogic.getId().equals(id)));
    }

    @Test
    public void listAllLogicClustersWithRackInfoTest() {
        ClusterLogicRackInfo clusterLogicRackInfo = new ClusterLogicRackInfo();
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        clusterLogicRackInfo.setLogicClusterIds(id.toString());
        Mockito.when(regionRackService.listAllLogicClusterRacks()).thenReturn(Arrays.asList(
				clusterLogicRackInfo));
        Assertions.assertTrue(clusterLogicService
                .listAllClusterLogicsWithRackInfo()
                .stream()
                .anyMatch(esClusterLogicWithRack -> esClusterLogicWithRack.getItems().stream().anyMatch(esClusterLogicRackInfo1 -> ListUtils.string2LongList(esClusterLogicRackInfo1.getLogicClusterIds()).contains(id))));
    }

    @Test
    public void getLogicClusterWithRackInfoByIdTest() {
        ClusterLogicRackInfo clusterLogicRackInfo = new ClusterLogicRackInfo();
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        clusterLogicRackInfo.setLogicClusterIds(id.toString());
        Mockito.when(regionRackService.listAllLogicClusterRacks()).thenReturn(Collections.singletonList(
                clusterLogicRackInfo));
        Assertions.assertTrue(clusterLogicService.getClusterLogicWithRackInfoById(id).getItems().stream().anyMatch(esClusterLogicRackInfo1 -> ListUtils.string2LongList(esClusterLogicRackInfo1.getLogicClusterIds()).contains(id)));
    }

    @SneakyThrows
    @Test
    public void deleteLogicClusterByIdTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        Assertions.assertEquals(Result.buildNotExist("逻辑集群不存在").getMessage(),
                clusterLogicService.deleteClusterLogicById(id + 1, OPERATOR).getMessage());
        ClusterLogicRackInfo clusterLogicRackInfo = new ClusterLogicRackInfo();
        String clusterName = "wpk";
        String rack = "wpk";
        clusterLogicRackInfo.setPhyClusterName(clusterName);
        clusterLogicRackInfo.setRack(rack);
        Mockito.when(regionRackService.listLogicClusterRacks(Mockito.anyLong())).thenReturn(Arrays.asList(
				clusterLogicRackInfo));
        IndexTemplatePhy indexTemplatePhy = new IndexTemplatePhy();
        indexTemplatePhy.setRack(rack);
        Mockito.when(templatePhyService.getNormalTemplateByCluster(Mockito.anyString())).thenReturn(Arrays.asList(indexTemplatePhy));
        Assertions.assertEquals(Result.build(ResultType.IN_USE_ERROR.getCode(), "逻辑集群使用中").getMessage(),
                clusterLogicService.deleteClusterLogicById(id, OPERATOR).getMessage());
        Mockito.when(regionRackService.listLogicClusterRacks(Mockito.anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(rackService.listLogicClusterRacks(Mockito.anyLong())).thenReturn(new ArrayList<>());
        Assertions.assertTrue(clusterLogicService.deleteClusterLogicById(id, OPERATOR).success());
    }

    @Test
    public void createLogicClusterTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Mockito.when(appService.isAppExists(Mockito.anyInt())).thenReturn(false);
        Assertions.assertEquals(Result.buildParamIllegal("应用ID非法").getMessage(),
                clusterLogicService.createClusterLogic(esLogicClusterDTO).getMessage());
        Mockito.when(appService.isAppExists(Mockito.anyInt())).thenReturn(true);
        Mockito.when(employeeService.checkUsers(Mockito.anyString())).thenReturn(Result.buildFail());
        Assertions.assertEquals(Result.buildParamIllegal("责任人非法").getMessage(),
                clusterLogicService.createClusterLogic(esLogicClusterDTO).getMessage());
        Mockito.when(employeeService.checkUsers(Mockito.anyString())).thenReturn(Result.buildSucc());
        Assertions.assertEquals(Result.buildParamIllegal("逻辑集群信息为空").getMessage(),
                clusterLogicService.createClusterLogic(null).getMessage());
        esLogicClusterDTO.setResponsible("");
        Assertions.assertEquals(Result.buildParamIllegal("责任人为空").getMessage(),
                clusterLogicService.createClusterLogic(esLogicClusterDTO).getMessage());
        esLogicClusterDTO.setAppId(null);
        Assertions.assertEquals(Result.buildParamIllegal("应用ID为空").getMessage(),
                clusterLogicService.createClusterLogic(esLogicClusterDTO).getMessage());
        esLogicClusterDTO.setName("");
        Assertions.assertEquals(Result.buildParamIllegal("集群名字为空").getMessage(),
                clusterLogicService.createClusterLogic(esLogicClusterDTO).getMessage());
        esLogicClusterDTO.setType(ResourceLogicTypeEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("新建逻辑集群提交内容中集群类型非法").getMessage(),
                clusterLogicService.createClusterLogic(esLogicClusterDTO).getMessage());
        esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Assertions.assertTrue(clusterLogicService.createClusterLogic(esLogicClusterDTO).success());
        Assertions.assertEquals(Result.buildDuplicate("逻辑集群重复").getMessage(),
                clusterLogicService.createClusterLogic(esLogicClusterDTO).getMessage());
    }

    @Test
    public void editLogicClusterTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Assertions.assertEquals(Result.buildParamIllegal("逻辑集群ID为空").getMessage(),
                clusterLogicService.editClusterLogic(esLogicClusterDTO, OPERATOR).getMessage());
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        esLogicClusterDTO.setId(id + 1);
        Assertions.assertEquals(Result.buildNotExist("逻辑集群不存在").getMessage(),
                clusterLogicService.editClusterLogic(esLogicClusterDTO, OPERATOR).getMessage());
        esLogicClusterDTO.setId(id);
        String name = "test";
        esLogicClusterDTO.setName(name);
        Assertions.assertTrue(clusterLogicService.editClusterLogic(esLogicClusterDTO, OPERATOR).success());
        Assertions.assertEquals(name, logicClusterDAO.getById(id).getName());
    }

    @Test
    public void editClusterLogicNotCheckTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        esLogicClusterDTO.setId(id);
        String name = "test";
        esLogicClusterDTO.setName(name);
        Assertions.assertTrue(
                clusterLogicService.editClusterLogicNotCheck(esLogicClusterDTO, OPERATOR).success());
        Assertions.assertEquals(name, logicClusterDAO.getById(id).getName());
    }

    @Test
    public void getLogicClusterConfigByIdTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        Assertions.assertNull(clusterLogicService.getClusterLogicConfigById(id + 1));
        Assertions.assertEquals(LogicResourceConfig.QUOTA_CTL_NONE, clusterLogicService.getClusterLogicConfigById(id).getQuotaCtl());
        Integer templateValueBase = 2;
        LogicResourceConfig logicResourceConfig = new LogicResourceConfig();
        logicResourceConfig.setTemplateValueBase(templateValueBase);
        String config = JSON.toJSONString(logicResourceConfig);
        esLogicClusterDTO.setConfigJson(config);
        esLogicClusterDTO.setId(id);
        clusterLogicService.editClusterLogicNotCheck(esLogicClusterDTO, OPERATOR);
        Assertions.assertEquals(templateValueBase, clusterLogicService.getClusterLogicConfigById(id).getTemplateValueBase());
    }

    @Test
    public void getOwnedLogicClustersByAppIdTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        Assertions.assertTrue(
                clusterLogicService.getOwnedClusterLogicListByAppId(esLogicClusterDTO.getAppId()).stream().anyMatch(esClusterLogic -> esClusterLogic.getId().equals(id)));
    }

    @Test
    public void getLogicDataNodeSepcTest() {
        ESMachineNormsPO esMachineNormsPO = new ESMachineNormsPO();
        esMachineNormsPO.setSpec("wpk");
        Mockito.when(esMachineNormsService.listMachineNorms()).thenReturn(Arrays.asList(esMachineNormsPO));
        Long clusterId = 123l;
        Assertions.assertTrue(clusterLogicService
                .getLogicDataNodeSepc(clusterId)
                .stream()
                .anyMatch(esRoleClusterNodeSepc -> esRoleClusterNodeSepc.getSpec().equals(esMachineNormsPO.getSpec())));
    }

    @Test
    public void getLogicClusterRoleTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        Assertions.assertTrue(clusterLogicService.getClusterLogicRole(id).isEmpty());
        String clusterName = "wpk";
        Mockito.when(rackService.listPhysicClusterNames(Mockito.anyLong())).thenReturn(Arrays.asList(clusterName));
        Assertions.assertTrue(clusterLogicService.getClusterLogicRole(id).isEmpty());
        Mockito.when(esClusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(null);
        Assertions.assertTrue(clusterLogicService.getClusterLogicRole(id).isEmpty());
        ClusterPhy clusterPhy = CustomDataSource.esClusterPhyFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(CustomDataSource.esRoleClusterHostDTOFactory(), ClusterRoleHostInfo.class);
        ClusterRoleInfo clusterRoleInfo = ConvertUtil.obj2Obj(CustomDataSource.esRoleClusterDTOFactory(), ClusterRoleInfo.class);
        clusterRoleHostInfo.setId(id);
        clusterPhy.setClusterRoleHostInfos(Arrays.asList(clusterRoleHostInfo));
        clusterRoleInfo.setId(id);
        clusterPhy.setClusterRoleInfos(Arrays.asList(clusterRoleInfo));
        Mockito.when(esClusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(
                clusterPhy);
        Assertions.assertTrue(clusterLogicService
                .getClusterLogicRole(id)
                .stream()
                .anyMatch(esRoleCluster1 -> esRoleCluster1.getRoleClusterName().equals(clusterName)));
    }

    /**
     * 这里显示成未安装的状态是因为插件版本的问题还没有解决？
     */
    @Test
    public void getLogicClusterPluginsTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        Assertions.assertTrue(clusterLogicService.getClusterLogicPlugins(id).isEmpty());
        String clusterPhy = "wpk";
        Mockito.when(rackService.listPhysicClusterNames(Mockito.anyLong())).thenReturn(Collections.singletonList(clusterPhy));
        ClusterPhy esClusterPhy = new ClusterPhy();
        esClusterPhy.setId(123);
        Mockito.when(esClusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(esClusterPhy);
        Assertions.assertTrue(clusterLogicService.getClusterLogicPlugins(id).isEmpty());
        ClusterPO clusterPO = new ClusterPO();
        String clusterName = "wpk";
        clusterPO.setCluster(clusterName);
        PluginDTO pluginDTO = CustomDataSource.esPluginDTOFactory();
        PluginPO pluginPO = ConvertUtil.obj2Obj(pluginDTO, PluginPO.class);
        Mockito.when(esPluginService.listClusterAndDefaultESPlugin(Mockito.anyString())).thenReturn(Collections.singletonList(pluginPO));
        Mockito.when(clusterDAO.listAll()).thenReturn(Collections.singletonList(clusterPO));
        Assertions.assertTrue(clusterLogicService
                .getClusterLogicPlugins(id)
                .stream()
                .anyMatch(esPlugin -> esPlugin.getCreator().equals(pluginDTO.getCreator())));
    }

    @Test
    public void addPluginTest() {
        Integer clusterId = new Integer(5);
        List<Integer> clusterIdList = Arrays.asList(clusterId);
        Mockito.when(rackService.listPhysicClusterId(Mockito.anyLong())).thenReturn(new ArrayList<>());
        Assertions.assertEquals(Result.buildFail("对应物理集群不存在").getMessage(),
                clusterLogicService.addPlugin(5l, new PluginDTO(), OPERATOR).getMessage());
        Mockito.when(rackService.listPhysicClusterId(Mockito.anyLong())).thenReturn(clusterIdList);
        Mockito.when(esPluginService.addESPlugin(Mockito.any())).thenReturn(Result.buildSucc());
        Assertions.assertTrue(clusterLogicService.addPlugin(5l, new PluginDTO(), OPERATOR).success());
    }

    @Test
    public void transferClusterLogicTest() {
        ESLogicClusterDTO esLogicClusterDTO = CustomDataSource.esLogicClusterDTOFactory();
        Long id = clusterLogicService.createClusterLogic(esLogicClusterDTO).getData();
        esLogicClusterDTO.setId(id);
        Integer targetAppId = 1234;
        String targetResponsible = "test";
        Assertions.assertTrue(clusterLogicService
                .transferClusterLogic(id, targetAppId, targetResponsible, OPERATOR).success());
        Assertions.assertEquals(targetAppId, logicClusterDAO.getById(id).getAppId());
    }
}
