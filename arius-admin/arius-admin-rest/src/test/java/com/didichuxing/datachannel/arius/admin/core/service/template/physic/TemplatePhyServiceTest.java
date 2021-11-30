package com.didichuxing.datachannel.arius.admin.core.service.template.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLogicPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplatePhysicalPO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateLogicDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhysicalDAO;
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
public class TemplatePhyServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private TemplatePhyService service;

    @Autowired
    private IndexTemplatePhysicalDAO dao;

    @Autowired
    private IndexTemplateLogicDAO logicDAO;

    @MockBean
    private ESTemplateService esTemplateService;

    @MockBean
    private ESIndexService esIndexService;

    @MockBean
    private RegionRackService regionRackService;

    private static final String operator = "System";
    private static int size = 10;

    @Test
    void updateTemplateNameTest() throws ESOperateException {
        Assertions.assertEquals("参数错误:输入的物理模板为空，请检查后再提交！",
                service.updateTemplateName(null, operator).getMessage());
        Long phyTemplateId = 0L;
        String updateNewName = "wpk-tes";
        IndexTemplatePhy indexTemplatePhy = new IndexTemplatePhy();
        Assertions.assertEquals("参数错误:需要修改的物理模板的Id为空，请检查后再提交！",
                service.updateTemplateName(indexTemplatePhy, operator).getMessage());
        indexTemplatePhy.setId(phyTemplateId);
        Assertions.assertEquals("参数错误:需要修改的物理模板的名称为空，请检查后再提交！",
                service.updateTemplateName(indexTemplatePhy, operator).getMessage());
        indexTemplatePhy.setName(updateNewName);
        Assertions.assertEquals("参数错误:需要修改的物理模板的id对应的原数据不存在，请检查后再提交！",
                service.updateTemplateName(indexTemplatePhy, operator).getMessage());
        //设置一个存在的物理模板id
        phyTemplateId = 21075L;
        String phyCluster = "yyfwltestxj-1125-01";
        indexTemplatePhy.setId(phyTemplateId);
        indexTemplatePhy.setCluster(phyCluster);
        Assertions.assertTrue(service.updateTemplateName(indexTemplatePhy, operator).success());
    }

    @Test
    void updateTemplateExpressionTest() throws ESOperateException {
        IndexTemplatePhy indexTemplatePhy = new IndexTemplatePhy();
        Long phyTemplateId = 21075L;
        String phyCluster = "yyfwltestxj-1125-01";
        String newExpression = "wpk-tes*";
        indexTemplatePhy.setId(phyTemplateId);
        indexTemplatePhy.setCluster(phyCluster);
        indexTemplatePhy.setExpression(newExpression);
        Assertions.assertTrue(service.updateTemplateExpression(indexTemplatePhy, newExpression, operator).success());
    }

    @Test
    void updateTemplateRoleTest() throws ESOperateException {
        IndexTemplatePhy indexTemplatePhy = new IndexTemplatePhy();
        Long phyTemplateId = 21075L;
        String phyCluster = "yyfwltestxj-1125-01";
        Integer role = 1;
        indexTemplatePhy.setId(phyTemplateId);
        indexTemplatePhy.setCluster(phyCluster);
        indexTemplatePhy.setRole(role);
        Assertions.assertTrue(service.updateTemplateRole(indexTemplatePhy, TemplateDeployRoleEnum.valueOf(role), operator).success());
    }

    @Test
    void updateTemplateShardNumTest() throws ESOperateException {
        IndexTemplatePhy indexTemplatePhy = new IndexTemplatePhy();
        Long phyTemplateId = 21075L;
        String phyCluster = "yyfwltestxj-1125-01";
        Integer newShardNumber = 2;
        indexTemplatePhy.setId(phyTemplateId);
        indexTemplatePhy.setCluster(phyCluster);
        indexTemplatePhy.setShard(newShardNumber);
        Assertions.assertTrue(service.updateTemplateShardNum(indexTemplatePhy, newShardNumber, operator).success());
    }

    @Test
    void deleteDirtyByClusterAndNameTest() {
        String cluster = "logi-elasticsearch-7.6.0";
        String name = "arius.template.hit";
        service.deleteDirtyByClusterAndName(cluster, name);
        Assertions.assertTrue(true);
    }

    @Test
    void getValidTemplatesByLogicIdTest() {
        int logicId = 1105;
        List<IndexTemplatePhy> ret = service.getValidTemplatesByLogicId(logicId);
        Assertions.assertFalse(ret.isEmpty());
        logicId = 999999;
        ret = service.getValidTemplatesByLogicId(logicId);
        Assertions.assertTrue(ret.isEmpty());
    }

    @Test
    void getClusterTemplateCountMapTest() {
        Map<String, Integer> ret = service.getClusterTemplateCountMap();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getTemplateByLogicIdsTest() {
        List<Integer> logicIds = new ArrayList<>();
        logicIds.add(1105);
        List<IndexTemplatePhy> ret = service.getTemplateByLogicIds(logicIds);
        Assertions.assertFalse(ret.isEmpty());
        logicIds.add(999999999);
        ret = service.getTemplateByLogicIds(logicIds);
        Assertions.assertEquals(1, ret.size());
    }

    @Test
    void getTest() {
        List<TemplatePhysicalPO> list = batchInsert();
        TemplatePhysicalPO po = list.get(0);
        Long id = po.getId();
        IndexTemplatePhysicalDTO dto = new IndexTemplatePhysicalDTO();
        dto.setId(id);
        List<IndexTemplatePhy> result = service.getByCondt(dto);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dto.getId(), result.get(0).getId());
        IndexTemplatePhysicalDTO dto1 = new IndexTemplatePhysicalDTO();
        dto1.setName("test" + 2);
        List<IndexTemplatePhy> result1 = service.getByCondt(dto1);
        Assertions.assertEquals(1, result1.size());
        Assertions.assertEquals(dto.getName(), result1.get(0).getName());
        List<IndexTemplatePhy> result2 = service.listTemplate();
        List<Long> ids = result2.stream().map(IndexTemplatePhy::getId).collect(Collectors.toList());
        for (TemplatePhysicalPO templatePhysicalPO : list) {
            Assertions.assertTrue(ids.contains(templatePhysicalPO.getId()));
        }
    }

    @Test
    void getByLogicId() {
        List<TemplatePhysicalPO> list = batchInsert();
        int logicId = 1;
        List<IndexTemplatePhy> list1 = service.getTemplateByLogicId(logicId);
        Assertions.assertEquals(1, list1.size());
        Assertions.assertEquals(logicId, list1.get(0).getLogicId());
    }

    @Test
    void getByLogicId0() {
        List<TemplatePhysicalPO> list = batchInsert0();
        int logicId = 1;
        List<IndexTemplatePhy> list1 = service.getTemplateByLogicId(logicId);
        Assertions.assertEquals(size, list1.size());
        Assertions.assertTrue(list1.stream().allMatch(p -> p.getLogicId() == 1));
    }

    @Test
    void getByIdTest() {
        List<TemplatePhysicalPO> list = batchInsert();
        TemplatePhysicalPO po = list.get(0);
        Long id = po.getId();
        IndexTemplatePhysicalDTO dto = new IndexTemplatePhysicalDTO();
        dto.setId(id);
        List<IndexTemplatePhy> result = service.getByCondt(dto);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dto.getId(), result.get(0).getId());
    }

    @Test
    void buildIndexTemplatePhysicalWithLogicTest() {
        Assertions.assertNull(service.buildIndexTemplatePhysicalWithLogic(null));
        TemplatePhysicalPO po = CustomDataSource.templatePhysicalSource();
        dao.insert(po);
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        po.setLogicId(0);
        IndexTemplatePhyWithLogic empty = service.buildIndexTemplatePhysicalWithLogic(po);
        Assertions.assertNull(empty.getLogicTemplate());
        logicDAO.insert(logicPO);
        po.setLogicId(logicPO.getId());
        dao.update(po);
        IndexTemplatePhyWithLogic template = service.buildIndexTemplatePhysicalWithLogic(po);
        Assertions.assertEquals(logicPO.getId(), template.getLogicTemplate().getId());
    }

    @Test
    void getWithLogicByIdTest() {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        TemplatePhysicalPO po = CustomDataSource.templatePhysicalSource();
        po.setLogicId(logicPO.getId());
        dao.insert(po);
        Long id = po.getId();
        IndexTemplatePhyWithLogic template = service.getTemplateWithLogicById(id);
        Assertions.assertEquals(logicPO.getId(), template.getLogicTemplate().getId());
    }

    @Test
    void getWithLogicByIdsTest() {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<TemplatePhysicalPO> templates = batchInsert1(logicId);
        List<Long> ids = templates.stream().map(TemplatePhysicalPO::getId).collect(Collectors.toList());
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
    void getWithLogicByNameTest() {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        String name = "test3";
        List<TemplatePhysicalPO> templates = batchInsert1(logicId);
        List<IndexTemplatePhyWithLogic> templatePhyWithLogics = service.getTemplateWithLogicByName(name);
        Assertions.assertEquals(1, templatePhyWithLogics.size());
        Assertions.assertEquals(name, templatePhyWithLogics.get(0).getName());
        Assertions.assertEquals(logicId, templatePhyWithLogics.get(0).getLogicTemplate().getId());
    }


    @Test
    void deleteTest() throws ESOperateException {
        TemplatePhysicalPO po = CustomDataSource.templatePhysicalSource();
        dao.insert(po);
        Long id = po.getId();
        Result result = service.delTemplate(id + 1, operator);
        Assertions.assertFalse(result.success());
        Result result1 = service.delTemplate(id, operator);
        Assertions.assertTrue(result1.success());
        IndexTemplatePhy after = service.getTemplateById(id);
        Assertions.assertNull(after);
    }

    @Test
    void deleteByLogicTest() throws ESOperateException {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<TemplatePhysicalPO> list = batchInsert1(logicId);
        Result result = service.delTemplateByLogicId(logicId, operator);
        Assertions.assertTrue(result.success());
        List<IndexTemplatePhy> after = service.getTemplateByLogicId(logicId);
        Assertions.assertTrue(after.isEmpty());
    }

    @Test
    void deleteByLogicTest0() throws ESOperateException {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<TemplatePhysicalPO> list = batchInsert();
        Result result = service.delTemplateByLogicId(logicId + 1, operator);
        Assertions.assertFalse(result.success());
        Result result1 = service.delTemplateByLogicId(logicId, operator);
        Assertions.assertFalse(result1.success());
    }

    /**
     * 不修改expression，且shard非法
     *
     * @throws ESOperateException
     */
    @Test
    void updateByLogicWithoutExpressionTest() throws ESOperateException {
        String expression = "test1";
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<TemplatePhysicalPO> list = batchInsertWithExpression(logicId, expression);
        IndexTemplateLogicDTO param = new IndexTemplateLogicDTO();
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
    void updateByLogicTest() throws ESOperateException {
        Mockito.when(esTemplateService.syncUpdateExpression(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(esTemplateService.syncUpdateRackAndShard(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);
        String expression = "test1";
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<TemplatePhysicalPO> list = batchInsertWithExpression(logicId, expression);
        IndexTemplateLogicDTO param = new IndexTemplateLogicDTO();
        String expression1 = "test2";
        param.setExpression(expression1);
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
    void updateByLogicTest0() throws ESOperateException {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<TemplatePhysicalPO> list = batchInsert();
        Result result = service.editTemplateFromLogic(null, operator);
        Assertions.assertFalse(result.success());
        IndexTemplateLogicDTO dto = new IndexTemplateLogicDTO();
        dto.setId(-1);
        Result result1 = service.editTemplateFromLogic(dto, operator);
        Assertions.assertTrue(result1.success());
    }

    @Test
    void getByClusterAndNameTest() {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<TemplatePhysicalPO> list = batchInsert1(logicId);
        String clusterName = list.get(0).getCluster();
        String name = "test1";
        IndexTemplatePhy templatePhy = service.getTemplateByClusterAndName(clusterName, name);
        Assertions.assertNotNull(templatePhy);
        Assertions.assertEquals(clusterName, templatePhy.getCluster());
        Assertions.assertEquals(name, templatePhy.getName());
    }

    @Test
    void getWithLogicByClusterAndNameTest() {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<TemplatePhysicalPO> list = batchInsert1(logicId);
        String clusterName = list.get(0).getCluster();
        String name = "test1";
        IndexTemplatePhyWithLogic templatePhy = service.getTemplateWithLogicByClusterAndName(clusterName, name);
        Assertions.assertNotNull(templatePhy);
        Assertions.assertEquals(clusterName, templatePhy.getCluster());
        Assertions.assertEquals(name, templatePhy.getName());
        Assertions.assertEquals(logicId, templatePhy.getLogicTemplate().getId());
    }

    @Test
    void getByClusterAndStatusTest() {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        List<TemplatePhysicalPO> list = batchInsert1(logicId);
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
    void getByClusterAndRackTest() {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        String cluster = "c1";
        String rack = "r1";
        int status = 1;
        List<TemplatePhysicalPO> list = batchInsertWithClusterAndRack(logicId, cluster, rack, status);
        List<TemplatePhysicalPO> deleted = batchInsertWithClusterAndRack(logicId, cluster, rack, -2);
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
    void getByRegionIdTest() {
        TemplateLogicPO logicPO = CustomDataSource.templateLogicSource();
        logicDAO.insert(logicPO);
        Integer logicId = logicPO.getId();
        String cluster = "c1";
        String rack = "r1";
        int status = 1;
        Long regionId = 1L;
        ClusterRegion region = new ClusterRegion();
        region.setPhyClusterName(cluster);
        region.setRacks(rack);
        List<TemplatePhysicalPO> list = batchInsertWithClusterAndRack(logicId, cluster, rack, status);
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
    void getMatchNoVersionIndexNamesTest() {
        TemplatePhysicalPO templatePhysicalPO = CustomDataSource.templatePhysicalSource();
        String cluster = "c1";
        String expression = "e1";
        String index = "i1";
        templatePhysicalPO.setCluster(cluster);
        templatePhysicalPO.setExpression(expression);
        TemplatePhysicalPO templatePhysicalPO1 = CustomDataSource.templatePhysicalSource();
        dao.insert(templatePhysicalPO);
        dao.insert(templatePhysicalPO1);

        Long id = templatePhysicalPO.getId();
        Long id1 = templatePhysicalPO1.getId();
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
    void getMatchIndexNamesTest() {
        TemplatePhysicalPO templatePhysicalPO = CustomDataSource.templatePhysicalSource();
        String cluster = "c1";
        String expression = "e1";
        String index = "index";
        templatePhysicalPO.setCluster(cluster);
        templatePhysicalPO.setExpression(expression);
        TemplatePhysicalPO templatePhysicalPO1 = CustomDataSource.templatePhysicalSource();
        dao.insert(templatePhysicalPO);
        dao.insert(templatePhysicalPO1);

        Long id = templatePhysicalPO.getId();
        Long id1 = templatePhysicalPO1.getId();
        Mockito.when(esIndexService.syncCatIndexByExpression(Mockito.eq(cluster), Mockito.eq(expression))).thenReturn(indexMatchResult());
        Mockito.when(esIndexService.syncCatIndexByExpression(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
        List<String> invalid = service.getMatchIndexNames(id1);
        Assertions.assertTrue(invalid.isEmpty());
        List<String> matchIndexNames = service.getMatchIndexNames(id);
        Assertions.assertEquals(size, matchIndexNames.size());
        for (String matchIndexName : matchIndexNames) {
            Assertions.assertTrue(matchIndexName.startsWith(index));
        }
    }

    @Test
    void getByLogicIds() {
        List<TemplatePhysicalPO> list = batchInsert();
        List<Integer> logicIds = Arrays.asList(1, 2, 3, 4, 5);
        List<IndexTemplatePhy> templates = service.getTemplateByLogicIds(logicIds);
        List<Integer> queriedLogicIds = templates.stream().map(IndexTemplatePhy::getLogicId).collect(Collectors.toList());
        Assertions.assertTrue(queriedLogicIds.containsAll(logicIds));
    }

    private List<TemplatePhysicalPO> batchInsert() {
        List<TemplatePhysicalPO> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TemplatePhysicalPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(i + 1);
            dao.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<TemplatePhysicalPO> batchInsert0() {
        List<TemplatePhysicalPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TemplatePhysicalPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(1);
            dao.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<TemplatePhysicalPO> batchInsert1(Integer logicId) {
        List<TemplatePhysicalPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TemplatePhysicalPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(logicId);
            dao.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<TemplatePhysicalPO> batchInsertWithClusterAndRack(Integer logicId, String cluster, String rack, int status) {
        List<TemplatePhysicalPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TemplatePhysicalPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(logicId);
            po.setCluster(cluster);
            po.setRack(rack);
            dao.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<TemplatePhysicalPO> batchInsertWithExpression(Integer logicId, String expression) {
        List<TemplatePhysicalPO> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TemplatePhysicalPO po = CustomDataSource.templatePhysicalSource();
            po.setName("test" + i);
            po.setLogicId(logicId);
            po.setExpression(expression);
            dao.insert(po);
            list.add(po);
        }
        return list;
    }

    private List<CatIndexResult> indexMatchResult() {
        List<CatIndexResult> resultList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            CatIndexResult catIndexResult = new CatIndexResult();
            catIndexResult.setIndex(String.format("index_v%02d", i));
            resultList.add(catIndexResult);
        }
        return resultList;
    }
}
