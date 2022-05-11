package com.didichuxing.datachannel.arius.admin.core.service.template.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePhyPO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhyDAO;
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
public class IndexTemplatePhyServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private IndexTemplatePhyService service;

    @Autowired
    private IndexTemplatePhyDAO indexTemplatePhyDAO;

    @Autowired
    private IndexTemplateDAO indexTemplateDAO;

    @Autowired
    private IndexTemplateDAO logicDAO;

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
        IndexTemplatePhy indexTemplatePhy = new IndexTemplatePhy();
        Assertions.assertTrue(service.updateTemplateName(indexTemplatePhy, operator).failed());
        indexTemplatePhy.setId(phyTemplateId);
        Assertions.assertTrue(service.updateTemplateName(indexTemplatePhy, operator).failed());
        indexTemplatePhy.setName(updateNewName);
        Assertions.assertTrue(service.updateTemplateName(indexTemplatePhy, operator).failed());

        IndexTemplatePhyPO indexTemplatePhyPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhyPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhyPO.setId(null);
        indexTemplatePhyDAO.insert(indexTemplatePhyPO);
        indexTemplatePhy = ConvertUtil.obj2Obj(indexTemplatePhyPO, IndexTemplatePhy.class);
        indexTemplatePhy.setName("newName");
        indexTemplatePhy.setId(indexTemplatePhyPO.getId());
        indexTemplatePhy.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        Mockito.when(esTemplateService.syncUpdateName(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Assertions.assertTrue(service.updateTemplateName(indexTemplatePhy, operator).success());
    }

    @Test
    public void updateTemplateExpressionTest() throws ESOperateException {
        IndexTemplatePhyPO indexTemplatePhyPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhyPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhyPO.setId(null);
        indexTemplatePhyDAO.insert(indexTemplatePhyPO);
        IndexTemplatePhy indexTemplatePhy = new IndexTemplatePhy();
        String newExpression = "wpk-tes*";
        indexTemplatePhy.setId(indexTemplatePhyPO.getId());
        indexTemplatePhy.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhy.setExpression(newExpression);
        Assertions.assertTrue(service.updateTemplateExpression(indexTemplatePhy, newExpression, operator).success());
    }

    @Test
    public void updateTemplateRoleTest() throws ESOperateException {
        IndexTemplatePhyPO indexTemplatePhyPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhyPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhyDAO.insert(indexTemplatePhyPO);
        IndexTemplatePhy indexTemplatePhy = new IndexTemplatePhy();
        Integer role = 1;
        indexTemplatePhy.setId(indexTemplatePhyPO.getId());
        indexTemplatePhy.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhy.setRole(role);
        Assertions.assertTrue(service.updateTemplateRole(indexTemplatePhy, TemplateDeployRoleEnum.valueOf(role), operator).success());
    }

    @Test
    public void updateTemplateShardNumTest() throws ESOperateException {
        // 插入一个 template
        IndexTemplatePhyPO indexTemplatePhyPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhyPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhyDAO.insert(indexTemplatePhyPO);
        IndexTemplatePhy indexTemplatePhy = new IndexTemplatePhy();
        Integer newShardNumber = 2;
        indexTemplatePhy.setId(indexTemplatePhyPO.getId());
        indexTemplatePhy.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhy.setShard(newShardNumber);
        Assertions.assertTrue(service.updateTemplateShardNum(indexTemplatePhy, newShardNumber, operator).success());
    }

    @Test
    public void deleteDirtyByClusterAndNameTest() {
        // 插入一个 template
        IndexTemplatePhyPO indexTemplatePhyPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhyPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhyDAO.insert(indexTemplatePhyPO);
        indexTemplatePhyDAO.updateStatus(indexTemplatePhyPO.getId(), -1);
        service.deleteDirtyByClusterAndName(CustomDataSource.PHY_CLUSTER_NAME, indexTemplatePhyPO.getName());
        IndexTemplatePhy templateByClusterAndName =
                service.getTemplateByClusterAndName(CustomDataSource.PHY_CLUSTER_NAME, indexTemplatePhyPO.getName());
        Assertions.assertNull(templateByClusterAndName);
    }

    @Test
    public void getValidTemplatesByLogicIdTest() {
        IndexTemplatePO indexTemplatePO = CustomDataSource.templateLogicSource();
        indexTemplateDAO.insert(indexTemplatePO);
        IndexTemplatePhyPO indexTemplatePhyPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhyPO.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhyPO.setLogicId(indexTemplatePO.getId());
        indexTemplatePhyDAO.insert(indexTemplatePhyPO);
        List<IndexTemplatePhy> ret = service.getValidTemplatesByLogicId(indexTemplatePO.getId());
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
        IndexTemplatePO indexTemplatePO = CustomDataSource.templateLogicSource();
        indexTemplateDAO.insert(indexTemplatePO);


        IndexTemplatePhyPO indexTemplatePhyPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhyPO.setLogicId(indexTemplatePO.getId());
        indexTemplatePhyDAO.insert(indexTemplatePhyPO);

        List<Integer> logicIds = new ArrayList<>();
        logicIds.add(indexTemplatePO.getId());
        List<IndexTemplatePhy> ret = service.getTemplateByLogicIds(logicIds);
        Assertions.assertFalse(ret.isEmpty());
        logicIds.add(-1);
        ret = service.getTemplateByLogicIds(logicIds);
        Assertions.assertEquals(1, ret.size());
    }

    @Test
    public void getTest() {
        List<IndexTemplatePhyPO> list = batchInsert();
        IndexTemplatePhyPO po = list.get(0);
        Long id = po.getId();
        IndexTemplatePhyDTO dto = new IndexTemplatePhyDTO();
        dto.setId(id);
        List<IndexTemplatePhy> result = service.getByCondt(dto);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dto.getId(), result.get(0).getId());
        IndexTemplatePhyDTO dto1 = new IndexTemplatePhyDTO();
        dto1.setName("test" + 2);
        List<IndexTemplatePhy> result1 = service.getByCondt(dto1);
        Assertions.assertEquals(1, result1.size());
        Assertions.assertEquals(dto1.getName(), result1.get(0).getName());
        List<IndexTemplatePhy> result2 = service.listTemplate();
        List<Long> ids = result2.stream().map(IndexTemplatePhy::getId).collect(Collectors.toList());
        for (IndexTemplatePhyPO indexTemplatePhyPO : list) {
            Assertions.assertTrue(ids.contains(indexTemplatePhyPO.getId()));
        }
    }

    @Test
    public void getByLogicId() {
        List<IndexTemplatePhyPO> list = batchInsert();
        int logicId = 1;
        List<IndexTemplatePhy> list1 = service.getTemplateByLogicId(logicId);
        Assertions.assertEquals(1, list1.size());
        Assertions.assertEquals(logicId, list1.get(0).getLogicId());
    }

    @Test
    public void getByLogicId0() {
        List<IndexTemplatePhyPO> list = batchInsert0();
        int logicId = 1;
        List<IndexTemplatePhy> list1 = service.getTemplateByLogicId(logicId);
        Assertions.assertEquals(size, list1.size());
        Assertions.assertTrue(list1.stream().allMatch(p -> p.getLogicId() == 1));
    }

    @Test
    public void getByIdTest() {
        List<IndexTemplatePhyPO> list = batchInsert();
        IndexTemplatePhyPO po = list.get(0);
        Long id = po.getId();
        IndexTemplatePhyDTO dto = new IndexTemplatePhyDTO();
        dto.setId(id);
        List<IndexTemplatePhy> result = service.getByCondt(dto);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dto.getId(), result.get(0).getId());
    }

    @Test
    public void buildIndexTemplatePhysicalWithLogicTest() {
        Assertions.assertNull(service.buildIndexTemplatePhysicalWithLogic(null));
        IndexTemplatePhyPO po = CustomDataSource.templatePhysicalSource();
        po.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        indexTemplatePhyDAO.insert(po);
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        po.setLogicId(0);
        IndexTemplatePhyWithLogic empty = service.buildIndexTemplatePhysicalWithLogic(po);
        Assertions.assertNull(empty.getLogicTemplate());
        logicDAO.insert(logicPO);
        po.setLogicId(logicPO.getId());
        indexTemplatePhyDAO.update(po);
        IndexTemplatePhyWithLogic template = service.buildIndexTemplatePhysicalWithLogic(po);
        Assertions.assertEquals(logicPO.getId(), template.getLogicTemplate().getId());
    }

    @Test
    public void getWithLogicByIdTest() {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        IndexTemplatePhyPO po = CustomDataSource.templatePhysicalSource();
        po.setCluster(CustomDataSource.PHY_CLUSTER_NAME);
        po.setLogicId(logicPO.getId());
        indexTemplatePhyDAO.insert(po);
        Long id = po.getId();
        IndexTemplatePhyWithLogic template = service.getTemplateWithLogicById(id);
        Assertions.assertEquals(logicPO.getId(), template.getLogicTemplate().getId());
    }

    @Test
    public void getWithLogicByIdsTest() {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhyPO> templates = batchInsert1(logicId);
        List<Long> ids = templates.stream().map(IndexTemplatePhyPO::getId).collect(Collectors.toList());
        List<IndexTemplatePhyWithLogic> templatePhyWithLogics = service.getTemplateWithLogicByIds(ids);
        Assertions.assertEquals(size, templatePhyWithLogics.size());
        for (IndexTemplatePhyWithLogic templatePhyWithLogic : templatePhyWithLogics) {
            Assertions.assertEquals(logicId, templatePhyWithLogic.getLogicTemplate().getId());
        }
        List<IndexTemplatePhyWithLogic> allTemplateWithLogic = service.listTemplateWithLogic();
        List<Long> idList = allTemplateWithLogic.stream().map(IndexTemplatePhyWithLogic::getId).collect(Collectors.toList());
        Assertions.assertTrue(idList.containsAll(ids));
    }

    @Test
    public void getWithLogicByNameTest() {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        String name = "test3";
        List<IndexTemplatePhyPO> templates = batchInsert1(logicId);
        List<IndexTemplatePhyWithLogic> templatePhyWithLogics = service.getTemplateWithLogicByName(name);
        Assertions.assertEquals(1, templatePhyWithLogics.size());
        Assertions.assertEquals(name, templatePhyWithLogics.get(0).getName());
        Assertions.assertEquals(logicId, templatePhyWithLogics.get(0).getLogicTemplate().getId());
    }


    @Test
    public void deleteTest() throws ESOperateException {
        IndexTemplatePhyPO po = CustomDataSource.templatePhysicalSource();
        indexTemplatePhyDAO.insert(po);
        Long id = po.getId();
        Result result = service.delTemplate(id + 1, operator);
        Assertions.assertFalse(result.success());
        Result result1 = service.delTemplate(id, operator);
        Assertions.assertTrue(result1.success());
        IndexTemplatePhy after = service.getTemplateById(id);
        Assertions.assertNull(after);
    }

    @Test
    public void deleteByLogicTest() throws ESOperateException {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhyPO> list = batchInsert1(logicId);
        Result result = service.delTemplateByLogicId(logicId, operator);
        Assertions.assertTrue(result.success());
        List<IndexTemplatePhy> after = service.getTemplateByLogicId(logicId);
        Assertions.assertTrue(after.isEmpty());
    }

    @Test
    public void deleteByLogicTest0() throws ESOperateException {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        IndexTemplatePhyPO indexTemplatePhyPO = CustomDataSource.templatePhysicalSource();
        indexTemplatePhyPO.setLogicId(logicPO.getId());
        indexTemplatePhyDAO.insert(indexTemplatePhyPO);
        Integer logicId = logicPO.getId();
        Result result = service.delTemplateByLogicId(logicId, operator);
        Assertions.assertTrue(result.success());
        IndexTemplatePhy template = service.getTemplateById(indexTemplatePhyPO.getId());
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
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhyPO> list = batchInsertWithExpression(logicId, expression);
        IndexTemplateDTO param = new IndexTemplateDTO();
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
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhyPO> list = batchInsertWithExpression(logicId, expression);
        IndexTemplateDTO param = new IndexTemplateDTO();
        String expression1 = "test2";
        param.setExpression(expression1);
        param.setId(logicId);
        int shard = 5;
        param.setShardNum(shard);
        Result result = service.editTemplateFromLogic(param, operator);
        Assertions.assertTrue(result.success());
        List<IndexTemplatePhy> templateList = service.getTemplateByLogicId(logicId);
        for (IndexTemplatePhy indexTemplatePhy : templateList) {
            Assertions.assertEquals(expression1, indexTemplatePhy.getExpression());
            Assertions.assertEquals(shard, indexTemplatePhy.getShard());
        }
    }

    /**
     * 修改参数为null、待修改集合为空的情况
     *
     * @throws ESOperateException
     */
    @Test
    public void updateByLogicTest0() throws ESOperateException {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhyPO> list = batchInsert();
        Result result = service.editTemplateFromLogic(null, operator);
        Assertions.assertFalse(result.success());
        IndexTemplateDTO dto = new IndexTemplateDTO();
        dto.setId(-1);
        Result result1 = service.editTemplateFromLogic(dto, operator);
        Assertions.assertTrue(result1.success());
    }

    @Test
    public void getByClusterAndNameTest() {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhyPO> list = batchInsert1(logicId);
        String clusterName = list.get(0).getCluster();
        String name = "test1";
        IndexTemplatePhy templatePhy = service.getTemplateByClusterAndName(clusterName, name);
        Assertions.assertNotNull(templatePhy);
        Assertions.assertEquals(clusterName, templatePhy.getCluster());
        Assertions.assertEquals(name, templatePhy.getName());
    }

    @Test
    public void getWithLogicByClusterAndNameTest() {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhyPO> list = batchInsert1(logicId);
        String clusterName = list.get(0).getCluster();
        String name = "test1";
        IndexTemplatePhyWithLogic templatePhy = service.getTemplateWithLogicByClusterAndName(clusterName, name);
        Assertions.assertNotNull(templatePhy);
        Assertions.assertEquals(clusterName, templatePhy.getCluster());
        Assertions.assertEquals(name, templatePhy.getName());
        Assertions.assertEquals(logicId, templatePhy.getLogicTemplate().getId());
    }

    @Test
    public void getByClusterAndStatusTest() {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<IndexTemplatePhyPO> list = batchInsert1(logicId);
        String clusterName = list.get(0).getCluster();
        int status = 1;
        List<IndexTemplatePhy> templates = service.getTemplateByClusterAndStatus(clusterName, status);
        for (IndexTemplatePhy template : templates) {
            Assertions.assertNotNull(template);
            Assertions.assertEquals(clusterName, template.getCluster());
            Assertions.assertEquals(status, template.getStatus());
        }
        List<IndexTemplatePhy> templates1 = service.getNormalTemplateByCluster(clusterName);
        for (IndexTemplatePhy template : templates1) {
            Assertions.assertNotNull(template);
            Assertions.assertEquals(clusterName, template.getCluster());
            Assertions.assertEquals(status, template.getStatus());
        }
    }

    @Test
    public void getByClusterAndRackTest() {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        String cluster = "c1";
        String rack = "r1";
        int status = 1;
        List<IndexTemplatePhyPO> list = batchInsertWithClusterAndRack(logicId, cluster, rack, status);
        List<IndexTemplatePhyPO> deleted = batchInsertWithClusterAndRack(logicId, cluster, rack, -2);
        List<IndexTemplatePhy> templates = service.getNormalTemplateByClusterAndRack(cluster, Arrays.asList(rack));
        for (IndexTemplatePhy template : templates) {
            Assertions.assertEquals(status, template.getStatus());
            Assertions.assertEquals(cluster, template.getCluster());
            Assertions.assertEquals(rack, template.getRack());
        }
        List<IndexTemplatePhy> empty = service.getNormalTemplateByClusterAndRack(cluster, null);
        Assertions.assertTrue(empty.isEmpty());
        String emptyCluster = "c1000";
        List<IndexTemplatePhy> empty1 = service.getNormalTemplateByClusterAndRack(cluster, Arrays.asList(emptyCluster));
        Assertions.assertTrue(empty1.isEmpty());
        String emptyRack = "r1000";
        List<IndexTemplatePhy> empty2 = service.getNormalTemplateByClusterAndRack(cluster, Arrays.asList(emptyRack));
        Assertions.assertTrue(empty2.isEmpty());
    }

    @Test
    public void getByRegionIdTest() {
        IndexTemplatePO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        String cluster = "c1";
        String rack = "r1";
        int status = 1;
        Long regionId = 1L;
        ClusterRegion region = new ClusterRegion();
        region.setPhyClusterName(cluster);
        region.setRacks(rack);
        List<IndexTemplatePhyPO> list = batchInsertWithClusterAndRack(logicId, cluster, rack, status);
        Mockito.when(regionRackService.getRegionById(Mockito.anyLong())).thenReturn(null);
        List<IndexTemplatePhy> invalid = service.getTemplateByRegionId(1000L);
        Assertions.assertTrue(invalid.isEmpty());
        Mockito.when(regionRackService.getRegionById(Mockito.eq(regionId))).thenReturn(region);
        List<IndexTemplatePhy> templates = service.getTemplateByRegionId(regionId);
        for (IndexTemplatePhy template : templates) {
            Assertions.assertEquals(cluster, template.getCluster());
            Assertions.assertEquals(rack, template.getRack());
        }
    }

    @Test
    public void getMatchNoVersionIndexNamesTest() {
        IndexTemplatePhyPO indexTemplatePhyPO = CustomDataSource.templatePhysicalSource();
        IndexTemplatePO indexTemplatePO = CustomDataSource.templateLogicSource();
        String cluster = "c1";
        String expression = "i1";
        String index = "i1";
        indexTemplatePhyPO.setCluster(cluster);
        indexTemplatePhyPO.setExpression(expression);
        IndexTemplatePhyPO indexTemplatePhyPO1 = CustomDataSource.templatePhysicalSource();

        indexTemplatePO.setId(null);
        indexTemplateDAO.insert(indexTemplatePO);
        indexTemplatePhyPO.setLogicId(indexTemplatePO.getId());
        indexTemplatePhyPO1.setLogicId(indexTemplatePO.getId());
        indexTemplatePhyDAO.insert(indexTemplatePhyPO);
        indexTemplatePhyDAO.insert(indexTemplatePhyPO1);

        Long id = indexTemplatePhyPO.getId();
        Long id1 = indexTemplatePhyPO1.getId();
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
        IndexTemplatePhyPO indexTemplatePhyPO = CustomDataSource.templatePhysicalSource();
        IndexTemplatePO indexTemplatePO = CustomDataSource.templateLogicSource();
        indexTemplateDAO.insert(indexTemplatePO);

        String cluster = CustomDataSource.PHY_CLUSTER_NAME;
        String expression = "e1";
        indexTemplatePhyPO.setCluster(cluster);
        indexTemplatePhyPO.setExpression(expression);
        indexTemplatePhyPO.setLogicId(indexTemplatePO.getId());
        indexTemplatePhyDAO.insert(indexTemplatePhyPO);

        IndexTemplatePhyPO indexTemplatePhyPO1 = CustomDataSource.templatePhysicalSource();
        indexTemplatePhyPO1.setLogicId(indexTemplatePO.getId());
        indexTemplatePhyDAO.insert(indexTemplatePhyPO1);

        Long id = indexTemplatePhyPO.getId();
        Long id1 = indexTemplatePhyPO1.getId();
        Mockito.when(esIndexService.syncCatIndexByExpression(cluster, expression)).thenReturn(indexMatchResult());

        List<String> matchIndexNames = service.getMatchIndexNames(id);
        Assertions.assertEquals(size, matchIndexNames.size());
        List<String> invalid = service.getMatchIndexNames(id1);
        Assertions.assertTrue(invalid.isEmpty());

    }

    @Test
    public void getByLogicIds() {
        List<IndexTemplatePhyPO> list = batchInsert();
        List<Integer> logicIds = Arrays.asList(1, 2, 3, 4, 5);
        List<IndexTemplatePhy> templates = service.getTemplateByLogicIds(logicIds);
        List<Integer> queriedLogicIds = templates.stream().map(IndexTemplatePhy::getLogicId).collect(Collectors.toList());
        Assertions.assertTrue(queriedLogicIds.containsAll(logicIds));
    }

    private List<IndexTemplatePhyPO> batchInsert() {
        List<IndexTemplatePhyPO> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            IndexTemplatePhyPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(i + 1);
            indexTemplatePhyDAO.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<IndexTemplatePhyPO> batchInsert0() {
        List<IndexTemplatePhyPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IndexTemplatePhyPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(1);
            indexTemplatePhyDAO.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<IndexTemplatePhyPO> batchInsert1(Integer logicId) {
        List<IndexTemplatePhyPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IndexTemplatePhyPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(logicId);
            indexTemplatePhyDAO.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<IndexTemplatePhyPO> batchInsertWithClusterAndRack(Integer logicId, String cluster, String rack, int status) {
        List<IndexTemplatePhyPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IndexTemplatePhyPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(logicId);
            po.setCluster(cluster);
            po.setRack(rack);
            indexTemplatePhyDAO.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<IndexTemplatePhyPO> batchInsertWithExpression(Integer logicId, String expression) {
        List<IndexTemplatePhyPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IndexTemplatePhyPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(logicId);
            po.setExpression(expression);
            indexTemplatePhyDAO.insert(po);
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
