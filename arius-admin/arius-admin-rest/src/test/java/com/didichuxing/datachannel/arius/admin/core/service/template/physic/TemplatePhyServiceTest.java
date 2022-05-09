package com.didichuxing.datachannel.arius.admin.core.service.template.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhysicalInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfoWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplateInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePhysicalInfoPO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateInfoDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhysicalInfoDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Rollback
public class TemplatePhyServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private TemplatePhyService service;

    @Autowired
    private IndexTemplatePhysicalInfoDAO indexTemplatePhysicalInfoDAO;

    @Autowired
    private IndexTemplateInfoDAO indexTemplateInfoDAO;

    @Autowired
    private IndexTemplateInfoDAO logicDAO;

    @MockBean
    private ESTemplateService esTemplateService;

    @MockBean
    private ESIndexService esIndexService;

    @MockBean
    private RegionRackService regionRackService;

    private static final String operator = "System";
    private static int size = 10;

    @Test
    public void updateTemplateNameTest() throws ESOperateException {
        Assertions.assertTrue(service.updateTemplateName(null, operator).failed());
        Long phyTemplateId = 0L;
        String updateNewName = "wpk-tes";
        IndexTemplatePhyInfo indexTemplatePhyInfo = new IndexTemplatePhyInfo();
        Assertions.assertTrue(service.updateTemplateName(indexTemplatePhyInfo, operator).failed());
        indexTemplatePhyInfo.setId(phyTemplateId);
        Assertions.assertTrue(service.updateTemplateName(indexTemplatePhyInfo, operator).failed());
        indexTemplatePhyInfo.setName(updateNewName);
        Assertions.assertTrue(service.updateTemplateName(indexTemplatePhyInfo, operator).failed());

        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhysicalInfoPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhysicalInfoPO.setId(null);
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO);
        indexTemplatePhyInfo = ConvertUtil.obj2Obj(indexTemplatePhysicalInfoPO, IndexTemplatePhyInfo.class);
        indexTemplatePhyInfo.setName("newName");
        indexTemplatePhyInfo.setId(indexTemplatePhysicalInfoPO.getId());
        indexTemplatePhyInfo.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        Mockito.when(esTemplateService.syncUpdateName(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Assertions.assertTrue(service.updateTemplateName(indexTemplatePhyInfo, operator).success());
    }

    @Test
    public void updateTemplateExpressionTest() throws ESOperateException {
        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhysicalInfoPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhysicalInfoPO.setId(null);
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO);
        IndexTemplatePhyInfo indexTemplatePhyInfo = new IndexTemplatePhyInfo();
        String newExpression = "wpk-tes*";
        indexTemplatePhyInfo.setId(indexTemplatePhysicalInfoPO.getId());
        indexTemplatePhyInfo.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhyInfo.setExpression(newExpression);
        Assertions.assertTrue(service.updateTemplateExpression(indexTemplatePhyInfo, newExpression, operator).success());
    }

    @Test
    public void updateTemplateRoleTest() throws ESOperateException {
        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhysicalInfoPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO);
        IndexTemplatePhyInfo indexTemplatePhyInfo = new IndexTemplatePhyInfo();
        Integer role = 1;
        indexTemplatePhyInfo.setId(indexTemplatePhysicalInfoPO.getId());
        indexTemplatePhyInfo.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhyInfo.setRole(role);
        Assertions.assertTrue(service.updateTemplateRole(indexTemplatePhyInfo, TemplateDeployRoleEnum.valueOf(role), operator).success());
    }

    @Test
    public void updateTemplateShardNumTest() throws ESOperateException {
        // 插入一个 template
        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhysicalInfoPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO);
        IndexTemplatePhyInfo indexTemplatePhyInfo = new IndexTemplatePhyInfo();
        Integer newShardNumber = 2;
        indexTemplatePhyInfo.setId(indexTemplatePhysicalInfoPO.getId());
        indexTemplatePhyInfo.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhyInfo.setShard(newShardNumber);
        Assertions.assertTrue(service.updateTemplateShardNum(indexTemplatePhyInfo, newShardNumber, operator).success());
    }

    @Test
    public void deleteDirtyByClusterAndNameTest() {
        // 插入一个 template
        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhysicalInfoPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO);
        indexTemplatePhysicalInfoDAO.updateStatus(indexTemplatePhysicalInfoPO.getId(), -1);
        service.deleteDirtyByClusterAndName(CustomDataSource.PHY_CLUSTER_NAME, indexTemplatePhysicalInfoPO.getName());
        IndexTemplatePhyInfo templateByClusterAndName =
                service.getTemplateByClusterAndName(CustomDataSource.PHY_CLUSTER_NAME, indexTemplatePhysicalInfoPO.getName());
        Assertions.assertNull(templateByClusterAndName);
    }

    @Test
    public void getValidTemplatesByLogicIdTest() {
        IndexTemplateInfoPO indexTemplateInfoPO = CustomDataSource.templateLogicSource();
        indexTemplateInfoDAO.insert(indexTemplateInfoPO);
        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhysicalInfoPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhysicalInfoPO.setLogicId(indexTemplateInfoPO.getId());
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO);
        List<IndexTemplatePhyInfo> ret = service.getValidTemplatesByLogicId(indexTemplateInfoPO.getId());
        Assertions.assertFalse(ret.isEmpty());
        ret = service.getValidTemplatesByLogicId(-1);
        Assertions.assertTrue(ret.isEmpty());
    }

    @Test
    public void getClusterTemplateCountMapTest() {
        Map<String, Integer> ret = service.getClusterTemplateCountMap();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getTemplateByLogicIdsTest() {
        IndexTemplateInfoPO indexTemplateInfoPO = CustomDataSource.templateLogicSource();
        indexTemplateInfoDAO.insert(indexTemplateInfoPO);


        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhysicalInfoPO.setLogicId(indexTemplateInfoPO.getId());
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO);

        List<Integer> logicIds = new ArrayList<>();
        logicIds.add(indexTemplateInfoPO.getId());
        List<IndexTemplatePhyInfo> ret = service.getTemplateByLogicIds(logicIds);
        Assertions.assertFalse(ret.isEmpty());
        logicIds.add(-1);
        ret = service.getTemplateByLogicIds(logicIds);
        Assertions.assertEquals(1, ret.size());
    }

    @Test
    public void getTest() {
        List<IndexTemplatePhysicalInfoPO> list = batchInsert();
        IndexTemplatePhysicalInfoPO po = list.get(0);
        Long id = po.getId();
        IndexTemplatePhysicalInfoDTO dto = new IndexTemplatePhysicalInfoDTO();
        dto.setId(id);
        List<IndexTemplatePhyInfo> result = service.getByCondt(dto);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dto.getId(), result.get(0).getId());
        IndexTemplatePhysicalInfoDTO dto1 = new IndexTemplatePhysicalInfoDTO();
        dto1.setName("test" + 2);
        List<IndexTemplatePhyInfo> result1 = service.getByCondt(dto1);
        Assertions.assertEquals(1, result1.size());
        Assertions.assertEquals(dto1.getName(), result1.get(0).getName());
        List<IndexTemplatePhyInfo> result2 = service.listTemplate();
        List<Long> ids = result2.stream().map(IndexTemplatePhyInfo::getId).collect(Collectors.toList());
        for (IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO : list) {
            Assertions.assertTrue(ids.contains(indexTemplatePhysicalInfoPO.getId()));
        }
    }

    @Test
    public void getByLogicId() {
        List<IndexTemplatePhysicalInfoPO> list = batchInsert();
        int logicId = 1;
        List<IndexTemplatePhyInfo> list1 = service.getTemplateByLogicId(logicId);
        Assertions.assertEquals(1, list1.size());
        Assertions.assertEquals(logicId, list1.get(0).getLogicId());
    }

    @Test
    public void getByLogicId0() {
        List<IndexTemplatePhysicalInfoPO> list = batchInsert0();
        int logicId = 1;
        List<IndexTemplatePhyInfo> list1 = service.getTemplateByLogicId(logicId);
        Assertions.assertEquals(size, list1.size());
        Assertions.assertTrue(list1.stream().allMatch(p -> p.getLogicId() == 1));
    }

    @Test
    public void getByIdTest() {
        List<IndexTemplatePhysicalInfoPO> list = batchInsert();
        IndexTemplatePhysicalInfoPO po = list.get(0);
        Long id = po.getId();
        IndexTemplatePhysicalInfoDTO dto = new IndexTemplatePhysicalInfoDTO();
        dto.setId(id);
        List<IndexTemplatePhyInfo> result = service.getByCondt(dto);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dto.getId(), result.get(0).getId());
    }

    @Test
    public void buildIndexTemplatePhysicalWithLogicTest() {
        Assertions.assertNull(service.buildIndexTemplatePhysicalWithLogic(null));
        IndexTemplatePhysicalInfoPO po = CustomDataSource.templatePhysicalSource();
        po.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhysicalInfoDAO.insert(po);
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        po.setLogicId(0);
        IndexTemplatePhyInfoWithLogic empty = service.buildIndexTemplatePhysicalWithLogic(po);
        Assertions.assertNull(empty.getLogicTemplate());
        logicDAO.insert(logicPO);
        po.setLogicId(logicPO.getId());
        indexTemplatePhysicalInfoDAO.update(po);
        IndexTemplatePhyInfoWithLogic template = service.buildIndexTemplatePhysicalWithLogic(po);
        Assertions.assertEquals(logicPO.getId(), template.getLogicTemplate().getId());
    }

    @Test
    public void getWithLogicByIdTest() {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        IndexTemplatePhysicalInfoPO po = CustomDataSource.templatePhysicalSource();
        po.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        po.setLogicId(logicPO.getId());
        indexTemplatePhysicalInfoDAO.insert(po);
        Long id = po.getId();
        IndexTemplatePhyInfoWithLogic template = service.getTemplateWithLogicById(id);
        Assertions.assertEquals(logicPO.getId(), template.getLogicTemplate().getId());
    }

    @Test
    public void getWithLogicByIdsTest() {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhysicalInfoPO> templates = batchInsert1(logicId);
        List<Long> ids = templates.stream().map(IndexTemplatePhysicalInfoPO::getId).collect(Collectors.toList());
        List<IndexTemplatePhyInfoWithLogic> templatePhyWithLogics = service.getTemplateWithLogicByIds(ids);
        Assertions.assertEquals(size, templatePhyWithLogics.size());
        for (IndexTemplatePhyInfoWithLogic templatePhyWithLogic : templatePhyWithLogics) {
            Assertions.assertEquals(logicId, templatePhyWithLogic.getLogicTemplate().getId());
        }
        List<IndexTemplatePhyInfoWithLogic> allTemplateWithLogic = service.listTemplateWithLogic();
        List<Long> idList = allTemplateWithLogic.stream().map(IndexTemplatePhyInfoWithLogic::getId).collect(Collectors.toList());
        Assertions.assertTrue(idList.containsAll(ids));
    }

    @Test
    public void getWithLogicByNameTest() {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        String name = "test3";
        List<IndexTemplatePhysicalInfoPO> templates = batchInsert1(logicId);
        List<IndexTemplatePhyInfoWithLogic> templatePhyWithLogics = service.getTemplateWithLogicByName(name);
        Assertions.assertEquals(1, templatePhyWithLogics.size());
        Assertions.assertEquals(name, templatePhyWithLogics.get(0).getName());
        Assertions.assertEquals(logicId, templatePhyWithLogics.get(0).getLogicTemplate().getId());
    }


    @Test
    public void deleteTest() throws ESOperateException {
        IndexTemplatePhysicalInfoPO po = CustomDataSource.templatePhysicalSource();
        indexTemplatePhysicalInfoDAO.insert(po);
        Long id = po.getId();
        Result result = service.delTemplate(id + 1, operator);
        Assertions.assertFalse(result.success());
        Result result1 = service.delTemplate(id, operator);
        Assertions.assertTrue(result1.success());
        IndexTemplatePhyInfo after = service.getTemplateById(id);
        Assertions.assertNull(after);
    }

    @Test
    public void deleteByLogicTest() throws ESOperateException {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhysicalInfoPO> list = batchInsert1(logicId);
        Result result = service.delTemplateByLogicId(logicId, operator);
        Assertions.assertTrue(result.success());
        List<IndexTemplatePhyInfo> after = service.getTemplateByLogicId(logicId);
        Assertions.assertTrue(after.isEmpty());
    }

    @Test
    public void deleteByLogicTest0() throws ESOperateException {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhysicalInfoPO.setLogicId(logicPO.getId());
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO);
        Integer logicId = logicPO.getId();
        Result result = service.delTemplateByLogicId(logicId, operator);
        Assertions.assertTrue(result.success());
        IndexTemplatePhyInfo template = service.getTemplateById(indexTemplatePhysicalInfoPO.getId());
        Assertions.assertNull(template);
    }

    /**
     * 不修改expression，且shard非法
     *
     * @throws ESOperateException
     */
    @Test
    public void updateByLogicWithoutExpressionTest() throws ESOperateException {
        String expression = "test1";
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhysicalInfoPO> list = batchInsertWithExpression(logicId, expression);
        IndexTemplateInfoDTO param = new IndexTemplateInfoDTO();
        param.setExpression(expression);
        param.setShardNum(0);
        Result result = service.editTemplateFromLogic(param, operator);
        Assertions.assertTrue(result.success());
    }

    /**
     * 修改expression和shard
     *
     * @throws ESOperateException
     */
    @Test
    public void updateByLogicTest() throws ESOperateException {
        Mockito.when(esTemplateService.syncUpdateExpression(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(esTemplateService.syncUpdateRackAndShard(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);
        String expression = "test1";
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhysicalInfoPO> list = batchInsertWithExpression(logicId, expression);
        IndexTemplateInfoDTO param = new IndexTemplateInfoDTO();
        String expression1 = "test2";
        param.setExpression(expression1);
        param.setId(logicId);
        int shard = 5;
        param.setShardNum(shard);
        Result result = service.editTemplateFromLogic(param, operator);
        Assertions.assertTrue(result.success());
        List<IndexTemplatePhyInfo> templateList = service.getTemplateByLogicId(logicId);
        for (IndexTemplatePhyInfo indexTemplatePhyInfo : templateList) {
            Assertions.assertEquals(expression1, indexTemplatePhyInfo.getExpression());
            Assertions.assertEquals(shard, indexTemplatePhyInfo.getShard());
        }
    }

    /**
     * 修改参数为null、待修改集合为空的情况
     *
     * @throws ESOperateException
     */
    @Test
    public void updateByLogicTest0() throws ESOperateException {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhysicalInfoPO> list = batchInsert();
        Result result = service.editTemplateFromLogic(null, operator);
        Assertions.assertFalse(result.success());
        IndexTemplateInfoDTO dto = new IndexTemplateInfoDTO();
        dto.setId(-1);
        Result result1 = service.editTemplateFromLogic(dto, operator);
        Assertions.assertTrue(result1.success());
    }

    @Test
    public void getByClusterAndNameTest() {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhysicalInfoPO> list = batchInsert1(logicId);
        String clusterName = list.get(0).getCluster();
        String name = "test1";
        IndexTemplatePhyInfo templatePhy = service.getTemplateByClusterAndName(clusterName, name);
        Assertions.assertNotNull(templatePhy);
        Assertions.assertEquals(clusterName, templatePhy.getCluster());
        Assertions.assertEquals(name, templatePhy.getName());
    }

    @Test
    public void getWithLogicByClusterAndNameTest() {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhysicalInfoPO> list = batchInsert1(logicId);
        String clusterName = list.get(0).getCluster();
        String name = "test1";
        IndexTemplatePhyInfoWithLogic templatePhy = service.getTemplateWithLogicByClusterAndName(clusterName, name);
        Assertions.assertNotNull(templatePhy);
        Assertions.assertEquals(clusterName, templatePhy.getCluster());
        Assertions.assertEquals(name, templatePhy.getName());
        Assertions.assertEquals(logicId, templatePhy.getLogicTemplate().getId());
    }

    @Test
    public void getByClusterAndStatusTest() {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhysicalInfoPO> list = batchInsert1(logicId);
        String clusterName = list.get(0).getCluster();
        int status = 1;
        List<IndexTemplatePhyInfo> templates = service.getTemplateByClusterAndStatus(clusterName, status);
        for (IndexTemplatePhyInfo template : templates) {
            Assertions.assertNotNull(template);
            Assertions.assertEquals(clusterName, template.getCluster());
            Assertions.assertEquals(status, template.getStatus());
        }
        List<IndexTemplatePhyInfo> templates1 = service.getNormalTemplateByCluster(clusterName);
        for (IndexTemplatePhyInfo template : templates1) {
            Assertions.assertNotNull(template);
            Assertions.assertEquals(clusterName, template.getCluster());
            Assertions.assertEquals(status, template.getStatus());
        }
    }

    @Test
    public void getByClusterAndRackTest() {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        String cluster = "c1";
        String rack = "r1";
        int status = 1;
        List<IndexTemplatePhysicalInfoPO> list = batchInsertWithClusterAndRack(logicId, cluster, rack, status);
        List<IndexTemplatePhysicalInfoPO> deleted = batchInsertWithClusterAndRack(logicId, cluster, rack, -2);
        List<IndexTemplatePhyInfo> templates = service.getNormalTemplateByClusterAndRack(cluster, Arrays.asList(rack));
        for (IndexTemplatePhyInfo template : templates) {
            Assertions.assertEquals(status, template.getStatus());
            Assertions.assertEquals(cluster, template.getCluster());
            Assertions.assertEquals(rack, template.getRack());
        }
        List<IndexTemplatePhyInfo> empty = service.getNormalTemplateByClusterAndRack(cluster, null);
        Assertions.assertTrue(empty.isEmpty());
        String emptyCluster = "c1000";
        List<IndexTemplatePhyInfo> empty1 = service.getNormalTemplateByClusterAndRack(cluster, Arrays.asList(emptyCluster));
        Assertions.assertTrue(empty1.isEmpty());
        String emptyRack = "r1000";
        List<IndexTemplatePhyInfo> empty2 = service.getNormalTemplateByClusterAndRack(cluster, Arrays.asList(emptyRack));
        Assertions.assertTrue(empty2.isEmpty());
    }

    @Test
    public void getByRegionIdTest() {
        IndexTemplateInfoPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        String cluster = "c1";
        String rack = "r1";
        int status = 1;
        Long regionId = 1L;
        ClusterRegion region = new ClusterRegion();
        region.setPhyClusterName(cluster);
        region.setRacks(rack);
        List<IndexTemplatePhysicalInfoPO> list = batchInsertWithClusterAndRack(logicId, cluster, rack, status);
        Mockito.when(regionRackService.getRegionById(Mockito.anyLong())).thenReturn(null);
        List<IndexTemplatePhyInfo> invalid = service.getTemplateByRegionId(1000L);
        Assertions.assertTrue(invalid.isEmpty());
        Mockito.when(regionRackService.getRegionById(Mockito.eq(regionId))).thenReturn(region);
        List<IndexTemplatePhyInfo> templates = service.getTemplateByRegionId(regionId);
        for (IndexTemplatePhyInfo template : templates) {
            Assertions.assertEquals(cluster, template.getCluster());
            Assertions.assertEquals(rack, template.getRack());
        }
    }

    @Test
    public void getMatchNoVersionIndexNamesTest() {
        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO = CustomDataSource.templatePhysicalSource();
        IndexTemplateInfoPO indexTemplateInfoPO = CustomDataSource.templateLogicSource();
        String cluster = "c1";
        String expression = "i1";
        String index = "i1";
        indexTemplatePhysicalInfoPO.setCluster(cluster);
        indexTemplatePhysicalInfoPO.setExpression(expression);
        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO1 = CustomDataSource.templatePhysicalSource();

        indexTemplateInfoPO.setId(null);
        indexTemplateInfoDAO.insert(indexTemplateInfoPO);
        indexTemplatePhysicalInfoPO.setLogicId(indexTemplateInfoPO.getId());
        indexTemplatePhysicalInfoPO1.setLogicId(indexTemplateInfoPO.getId());
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO);
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO1);

        Long id = indexTemplatePhysicalInfoPO.getId();
        Long id1 = indexTemplatePhysicalInfoPO1.getId();
        List<String> empty = service.getMatchNoVersionIndexNames(id + 1);
        Assertions.assertTrue(empty.isEmpty());

        Set<String> indices = new HashSet<>(Arrays.asList("i1", "i2", "i3"));
        Mockito.when(esIndexService.syncGetIndexNameByExpression(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptySet());
        List<String> invalid = service.getMatchNoVersionIndexNames(id1);
        Assertions.assertTrue(invalid.isEmpty());
        Mockito.when(esIndexService.syncGetIndexNameByExpression(Mockito.eq(cluster), Mockito.eq(expression))).thenReturn(indices);
        List<String> noMatchIndices = service.getMatchNoVersionIndexNames(id);
        Assertions.assertEquals(1, noMatchIndices.size());
        Assertions.assertEquals(index, noMatchIndices.get(0));
    }

    @Test
    public void getMatchIndexNamesTest() throws ESOperateException {
        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO = CustomDataSource.templatePhysicalSource();
        IndexTemplateInfoPO indexTemplateInfoPO = CustomDataSource.templateLogicSource();
        indexTemplateInfoDAO.insert(indexTemplateInfoPO);

        String cluster = CustomDataSource.PHY_CLUSTER_NAME;
        String expression = "e1";
        indexTemplatePhysicalInfoPO.setCluster(cluster);
        indexTemplatePhysicalInfoPO.setExpression(expression);
        indexTemplatePhysicalInfoPO.setLogicId(indexTemplateInfoPO.getId());
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO);

        IndexTemplatePhysicalInfoPO indexTemplatePhysicalInfoPO1 = CustomDataSource.templatePhysicalSource();
        indexTemplatePhysicalInfoPO1.setLogicId(indexTemplateInfoPO.getId());
        indexTemplatePhysicalInfoDAO.insert(indexTemplatePhysicalInfoPO1);

        Long id = indexTemplatePhysicalInfoPO.getId();
        Long id1 = indexTemplatePhysicalInfoPO1.getId();
        Mockito.when(esIndexService.syncCatIndexByExpression(cluster, expression)).thenReturn(indexMatchResult());

        List<String> matchIndexNames = service.getMatchIndexNames(id);
        Assertions.assertEquals(size, matchIndexNames.size());
        List<String> invalid = service.getMatchIndexNames(id1);
        Assertions.assertTrue(invalid.isEmpty());

    }

    @Test
    public void getByLogicIds() {
        List<IndexTemplatePhysicalInfoPO> list = batchInsert();
        List<Integer> logicIds = Arrays.asList(1, 2, 3, 4, 5);
        List<IndexTemplatePhyInfo> templates = service.getTemplateByLogicIds(logicIds);
        List<Integer> queriedLogicIds = templates.stream().map(IndexTemplatePhyInfo::getLogicId).collect(Collectors.toList());
        Assertions.assertTrue(queriedLogicIds.containsAll(logicIds));
    }

    private List<IndexTemplatePhysicalInfoPO> batchInsert() {
        List<IndexTemplatePhysicalInfoPO> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            IndexTemplatePhysicalInfoPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(i + 1);
            indexTemplatePhysicalInfoDAO.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<IndexTemplatePhysicalInfoPO> batchInsert0() {
        List<IndexTemplatePhysicalInfoPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IndexTemplatePhysicalInfoPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(1);
            indexTemplatePhysicalInfoDAO.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<IndexTemplatePhysicalInfoPO> batchInsert1(Integer logicId) {
        List<IndexTemplatePhysicalInfoPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IndexTemplatePhysicalInfoPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(logicId);
            indexTemplatePhysicalInfoDAO.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<IndexTemplatePhysicalInfoPO> batchInsertWithClusterAndRack(Integer logicId, String cluster, String rack, int status) {
        List<IndexTemplatePhysicalInfoPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IndexTemplatePhysicalInfoPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(logicId);
            po.setCluster(cluster);
            po.setRack(rack);
            indexTemplatePhysicalInfoDAO.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<IndexTemplatePhysicalInfoPO> batchInsertWithExpression(Integer logicId, String expression) {
        List<IndexTemplatePhysicalInfoPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IndexTemplatePhysicalInfoPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(logicId);
            po.setExpression(expression);
            indexTemplatePhysicalInfoDAO.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<CatIndexResult> indexMatchResult() {
        List<CatIndexResult> resultList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            CatIndexResult catIndexResult = new CatIndexResult();
            catIndexResult.setIndex("e1");
            resultList.add(catIndexResult);
        }
        return resultList;
    }
}
