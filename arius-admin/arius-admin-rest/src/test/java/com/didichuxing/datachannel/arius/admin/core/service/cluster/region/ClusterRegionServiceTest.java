package com.didichuxing.datachannel.arius.admin.core.service.cluster.region;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterRackInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterRegionPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.region.ClusterRegionDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Transactional
@Rollback
public class ClusterRegionServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ClusterRegionDAO clusterRegionDAO;

    @MockBean
    private ClusterLogicService clusterLogicService;

    @MockBean
    private ClusterPhyService esClusterPhyService;

    @MockBean
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private ClusterRegionService clusterRegionService;

    private static final String racks = "r1,r2";

    private static final String clusterName = "wpk";

    @BeforeEach
    public void mockRules() {
        Mockito.when(esClusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(new ClusterPhy());
        Mockito.when(esClusterPhyService.getClusterRacks(Mockito.anyString())).thenReturn(RackUtils.racks2Set(racks));
        Mockito.when(indexTemplatePhyService.getTemplateByRegionId(Mockito.anyLong())).thenReturn(Collections.singletonList(new IndexTemplatePhy()));
    }

    @Test
    public void deleteRackByIdTest() {
        Assertions.assertFalse(clusterRegionService.deleteRackById(null));
    }

    @Test
    public void getRegionByIdTest() {
        Assertions.assertNull(clusterRegionService.getRegionById(null));
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(id, clusterRegionService.getRegionById(id).getId());
    }

    @Test
    public void listAllLogicClusterRacksTest() {
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234L;
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
        Assertions.assertTrue(clusterRegionService
                .listAllLogicClusterRacks()
                .stream()
                .anyMatch(esClusterLogicRackInfo -> esClusterLogicRackInfo.getPhyClusterName().equals(clusterName)));
    }

    @Test
    public void listLogicClusterRacksTest1() {
        ESLogicClusterRackInfoDTO esLogicClusterRackInfoDTO = new ESLogicClusterRackInfoDTO();
        Assertions.assertTrue(clusterRegionService.listLogicClusterRacks(esLogicClusterRackInfoDTO).isEmpty());
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234L;
        esLogicClusterRackInfoDTO.setLogicClusterId(logicClusterId);
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
        Assertions.assertTrue(clusterRegionService
                .listLogicClusterRacks(esLogicClusterRackInfoDTO)
                .stream()
                .anyMatch(esClusterLogicRackInfo ->
                        ListUtils.string2LongList(esClusterLogicRackInfo.getLogicClusterIds()).contains(logicClusterId)));
    }

    @Test
    public void listLogicClusterRacksTest2() {
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234L;
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
        Assertions.assertTrue(clusterRegionService
                .listLogicClusterRacks(logicClusterId)
                .stream()
                .anyMatch(esClusterLogicRackInfo -> esClusterLogicRackInfo.getPhyClusterName().equals(clusterName)));
    }

    @Test
    public void listAssignedRacksByClusterNameTest() {
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234l;
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
        Assertions.assertTrue(clusterRegionService
                .listAssignedRacksByClusterName(clusterName)
                .stream()
                .anyMatch(esClusterLogicRackInfo -> esClusterLogicRackInfo.getPhyClusterName().equals(clusterName)));
    }

    @Test
    public void listPhysicClusterNamesTest() {
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234l;
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
        Assertions.assertTrue(clusterRegionService
                .listPhysicClusterNames(logicClusterId)
                .stream()
                .anyMatch(s -> s.equals(clusterName)));
    }

    @Test
    public void listPhysicClusterIdTest() {
        ClusterPhy clusterPhy = CustomDataSource.esClusterPhyFactory();
        Mockito.when(esClusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(
                clusterPhy);
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234l;
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
        Assertions.assertTrue(clusterRegionService
                .listPhysicClusterId(logicClusterId)
                .stream()
                .anyMatch(integer -> integer.equals(clusterPhy.getId())));
    }

    @Test
    public void countRackMatchedRegionTest() {
        Assertions.assertEquals(0, clusterRegionService.countRackMatchedRegion(null, null));
        ClusterPhy clusterPhy = CustomDataSource.esClusterPhyFactory();
        Mockito.when(esClusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(
                clusterPhy);
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234l;
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
        int count = 1;
        Assertions.assertEquals(count, clusterRegionService.countRackMatchedRegion(clusterName, racks));
    }

    @Test
    public void listRegionsByLogicAndPhyClusterTest() {
        Assertions.assertTrue(clusterRegionService.listRegionsByLogicAndPhyCluster(null, null).isEmpty());
        ClusterPhy clusterPhy = CustomDataSource.esClusterPhyFactory();
        Mockito.when(esClusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(
                clusterPhy);
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234l;
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
        Assertions.assertTrue(clusterRegionService
                .listRegionsByLogicAndPhyCluster(logicClusterId, clusterName)
                .stream()
                .anyMatch(clusterRegion -> clusterRegion.getId().equals(id)));
    }

    @Test
    public void createPhyClusterRegionTest() {
        /*Assertions.assertEquals(Result.buildParamIllegal("物理集群名不能为空").getMessage(),
                clusterRegionService.createPhyClusterRegion(null, null, null, null).getMessage());
        Mockito.when(esClusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(null);
        Assertions.assertEquals(Result.buildParamIllegal(String.format("物理集群 %s 不存在", clusterName)).getMessage(),
                clusterRegionService.createPhyClusterRegion(clusterName, null, null, null).getMessage());
        Mockito.when(esClusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(new ClusterPhy());
        Assertions.assertEquals(Result.buildParamIllegal("racks为空").getMessage(),
                clusterRegionService.createPhyClusterRegion(clusterName, null, null, null).getMessage());
        Mockito.when(esClusterPhyService.getClusterRacks(Mockito.anyString())).thenReturn(Sets.newHashSet("r1"));
        Mockito.when(esClusterPhyService.getClusterRacks(Mockito.anyString())).thenReturn(RackUtils.racks2Set(racks));
        Assertions.assertEquals(Result.buildParamIllegal("指定的share非法").getMessage(),
                clusterRegionService.createPhyClusterRegion(clusterName, racks, 123, CustomDataSource.OPERATOR).getMessage());
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(clusterName, clusterRegionDAO.getById(id).getPhyClusterName());*/
    }

    @Test
    public void createAndBindRegionTest() {
        Assertions.assertEquals(Result.buildParamIllegal("物理集群名不能为空").getMessage(),
                clusterRegionService.createAndBindRegion(null, null, null, null, CustomDataSource.OPERATOR).getMessage());
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        Assertions.assertTrue(
                clusterRegionService.createAndBindRegion(clusterName, racks, 123L, null, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void deletePhyClusterRegionTest() {
        Assertions.assertEquals(Result.buildFail("regionId为null").getMessage(),
                clusterRegionService.deletePhyClusterRegion(null, CustomDataSource.OPERATOR).getMessage());
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(Result.buildFail(String.format("region %d 不存在", id + 1)).getMessage(),
                clusterRegionService.deletePhyClusterRegion(id + 1, CustomDataSource.OPERATOR).getMessage());
        Assertions.assertTrue(clusterRegionService.deletePhyClusterRegion(id, CustomDataSource.OPERATOR).success());
        Assertions.assertNull(clusterRegionDAO.getById(id));
        id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234L;
        ClusterRegionDTO clusterRegionDTO = CustomDataSource.clusterRegionDTOFactory();
        clusterRegionDTO.setId(id);
        clusterRegionDTO.setLogicClusterIds(logicClusterId.toString());
        ClusterRegionPO clusterRegionPO = ConvertUtil.obj2Obj(clusterRegionDTO, ClusterRegionPO.class);
        clusterRegionDAO.update(clusterRegionPO);
        Assertions.assertTrue(clusterRegionService.deletePhyClusterRegion(id, CustomDataSource.OPERATOR).failed());
    }

    @Test
    public void deletePhyClusterRegionWithoutCheckTest() {
        Assertions.assertEquals(Result.buildFail("regionId为null").getMessage(),
                clusterRegionService.deletePhyClusterRegionWithoutCheck(null, CustomDataSource.OPERATOR).getMessage());
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(Result.buildFail(String.format("region %d 不存在", id + 1)).getMessage(),
                clusterRegionService.deletePhyClusterRegionWithoutCheck(id + 1, CustomDataSource.OPERATOR).getMessage());
        Assertions.assertTrue(clusterRegionService.deletePhyClusterRegionWithoutCheck(id, CustomDataSource.OPERATOR).success());
        Assertions.assertNull(clusterRegionDAO.getById(id));
    }

    @Test
    public void bindRegionTest() {
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(Result.buildFail(String.format("region %d 不存在", id + 1)).getMessage(),
                clusterRegionService.bindRegion(id + 1, null, null, CustomDataSource.OPERATOR).getMessage());
        ClusterRegionDTO clusterRegionDTO = CustomDataSource.clusterRegionDTOFactory();
        clusterRegionDTO.setId(id);
        Long logicClusterId = 1234L;
        clusterRegionDTO.setLogicClusterIds(logicClusterId.toString());
        ClusterRegionPO clusterRegionPO = ConvertUtil.obj2Obj(clusterRegionDTO, ClusterRegionPO.class);
        clusterRegionDAO.update(clusterRegionPO);
        Assertions.assertTrue(clusterRegionService.bindRegion(id, null, null, CustomDataSource.OPERATOR).failed());
        clusterRegionDTO = CustomDataSource.clusterRegionDTOFactory();
        clusterRegionDTO.setId(id);
        clusterRegionPO = ConvertUtil.obj2Obj(clusterRegionDTO, ClusterRegionPO.class);
        clusterRegionDAO.update(clusterRegionPO);
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(null);
        Assertions.assertEquals(Result.buildFail(String.format("逻辑集群 %S 不存在", logicClusterId)).getMessage(),
                clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR).getMessage());
        ClusterLogic clusterLogic = new ClusterLogic();
        clusterLogic.setType(ClusterResourceTypeEnum.PUBLIC.getCode());
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(clusterLogic);
        Assertions.assertTrue(clusterRegionService.bindRegion(id, logicClusterId, 123, CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void editRegionRacksTest() {
        Assertions.assertEquals(Result.buildFail("未指定regionId").getMessage(),
                clusterRegionService.editRegionRacks(null, null, CustomDataSource.OPERATOR).getMessage());
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(Result.buildFail(String.format("region %d 不存在", id + 1)).getMessage(),
                clusterRegionService.editRegionRacks(id + 1, racks, CustomDataSource.OPERATOR).getMessage());
        Assertions.assertTrue(clusterRegionService.editRegionRacks(id, racks, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void unbindRegionTest() {
        Assertions.assertEquals(Result.buildFail("未指定regionId").getMessage(),
                clusterRegionService.unbindRegion(null, null, CustomDataSource.OPERATOR).getMessage());
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(Result.buildFail(String.format("region %d 不存在", id + 1)).getMessage(),
                clusterRegionService.unbindRegion(id + 1, null, CustomDataSource.OPERATOR).getMessage());
        Assertions.assertEquals(Result.buildFail(String.format("region %d 未被绑定", id)).getMessage(),
                clusterRegionService.unbindRegion(id, null, CustomDataSource.OPERATOR).getMessage());
        ClusterRegionDTO clusterRegionDTO = CustomDataSource.clusterRegionDTOFactory();
        clusterRegionDTO.setId(id);
        Long logicClusterId = 1234L;
        clusterRegionDTO.setLogicClusterIds(logicClusterId.toString());
        ClusterRegionPO clusterRegionPO = ConvertUtil.obj2Obj(clusterRegionDTO, ClusterRegionPO.class);
        clusterRegionDAO.update(clusterRegionPO);
        Assertions.assertEquals(Result.buildFail(String.format("region %d 上已经分配模板", id)).getMessage(),
                clusterRegionService.unbindRegion(id, null, CustomDataSource.OPERATOR).getMessage());
        Mockito.when(indexTemplatePhyService.getTemplateByRegionId(Mockito.anyLong())).thenReturn(null);
        Assertions.assertTrue(clusterRegionService.unbindRegion(id, null, CustomDataSource.OPERATOR).success());
        Assertions.assertEquals(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID,
                clusterRegionDAO.getById(id).getLogicClusterIds());
    }

    @Test
    public void listLogicClusterRegionsTest() {
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234L;
        Assertions.assertTrue(clusterRegionService.listLogicClusterRegions(logicClusterId).isEmpty());
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
        Assertions.assertTrue(clusterRegionService
                .listLogicClusterRegions(logicClusterId)
                .stream()
                .anyMatch(clusterRegion -> clusterRegion.getId().equals(id)));
    }

    @Test
    public void listRegionsByClusterNameTest() {
        Assertions.assertTrue(clusterRegionService.listRegionsByClusterName(clusterName).isEmpty());
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234L;
        Assertions.assertTrue(clusterRegionService.listLogicClusterRegions(logicClusterId).isEmpty());
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
        Assertions.assertTrue(clusterRegionService
                .listRegionsByClusterName(clusterName)
                .stream()
                .anyMatch(clusterRegion -> clusterRegion.getId().equals(id)));
    }

    /**
     * 一个集群上应该是可能有着多个物理集群的region
     * FIXME
     */
    @Test
    public void getLogicClusterIdByPhyClusterIdTest() {
        Integer phyClusterId = 123;
        Assertions.assertNull(clusterRegionService.getLogicClusterIdByPhyClusterId(phyClusterId));
        Mockito.when(esClusterPhyService.getClusterById(Mockito.anyInt())).thenReturn(new ClusterPhy());
        Assertions.assertNull(clusterRegionService.getLogicClusterIdByPhyClusterId(phyClusterId));
        Long id = clusterRegionService.createPhyClusterRegion(clusterName, racks, null, CustomDataSource.OPERATOR).getData();
        Long logicClusterId = 1234L;
        Assertions.assertTrue(clusterRegionService.listLogicClusterRegions(logicClusterId).isEmpty());
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(new ClusterLogic());
        clusterRegionService.bindRegion(id, logicClusterId, null, CustomDataSource.OPERATOR);
    }

}
