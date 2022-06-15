package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateType;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateConfigDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhyDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateTypeDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class IndexTemplateServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private IndexTemplateService indexTemplateService;

    @MockBean
    private IndexTemplatePhyService indexTemplatePhyService;

    @MockBean
    private AppLogicTemplateAuthService logicTemplateAuthService;

    @MockBean
    private AppClusterLogicAuthService logicClusterAuthService;

    @MockBean
    private IndexTemplatePhyDAO indexTemplatePhyDAO;

    @MockBean
    private IndexTemplateDAO indexTemplateDAO;

    @MockBean
    private IndexTemplateConfigDAO indexTemplateConfigDAO;

    @MockBean
    private IndexTemplateTypeDAO indexTemplateTypeDAO;

    @MockBean
    private ClusterLogicService clusterLogicService;

    @Test
    public void editTemplateNameTest() throws AdminOperateException {
        Mockito.when(indexTemplateDAO.update(Mockito.any())).thenReturn(1);
        IndexTemplateDTO indexTemplateDTO = new IndexTemplateDTO();
        Assertions.assertTrue(indexTemplateService.editTemplateName(indexTemplateDTO, CustomDataSource.OPERATOR).failed());
        indexTemplateDTO.setId(19489);
        Assertions.assertTrue(indexTemplateService.editTemplateName(indexTemplateDTO, CustomDataSource.OPERATOR).failed());
        indexTemplateDTO.setName("wpk-tes");
        Assertions.assertTrue(indexTemplateService.editTemplateName(indexTemplateDTO, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void insertTemplateConfigTest() {
        IndexTemplateConfig config = new IndexTemplateConfig();
        Mockito.when(indexTemplateConfigDAO.insert(Mockito.any())).thenReturn(1);
        Result<Void> result = indexTemplateService.insertTemplateConfig(config);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void upsertTemplateShardFactorTest() {
        Mockito.when(indexTemplateDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(indexTemplateConfigDAO.getByLogicId(Mockito.isNull())).thenReturn(CustomDataSource.templateConfigSource());
        Mockito.when(indexTemplateConfigDAO.getByLogicId(1)).thenReturn(CustomDataSource.templateConfigSource());
        double factor = 1.0d;
        // 不存在的 logiTemplateId
        indexTemplateService.updateTemplateShardFactorIfGreater(-1, factor, "admin");
        // 存在的 logiTemplateId
        indexTemplateService.updateTemplateShardFactorIfGreater(1, factor, "admin");
        Assertions.assertNull(null);
    }
 

    @Test
    public void updateTemplateShardFactorIfGreaterTest() {
        Mockito.when(indexTemplateDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(indexTemplateConfigDAO.getByLogicId(Mockito.isNull())).thenReturn(CustomDataSource.templateConfigSource());
        Mockito.when(indexTemplateConfigDAO.getByLogicId(1)).thenReturn(CustomDataSource.templateConfigSource());
        double factor = 1.0d;
        indexTemplateService.updateTemplateShardFactorIfGreater(-1, factor, "admin");
        factor = 999.0d;
        indexTemplateService.updateTemplateShardFactorIfGreater(1, factor, "admin");
        Assertions.assertNull(null);
    }

    @Test
    public void updateTemplateConfigTest() {
        IndexTemplateConfigDTO configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        Assertions.assertTrue(indexTemplateService.updateTemplateConfig(null, "admin").failed());
        configDTO.setLogicId(null);
        Assertions.assertTrue(indexTemplateService.updateTemplateConfig(configDTO, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        configDTO.setIsSourceSeparated(2);
        Assertions.assertTrue(indexTemplateService.updateTemplateConfig(null, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        configDTO.setIsSourceSeparated(2);
        Assertions.assertTrue(indexTemplateService.updateTemplateConfig(null, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        configDTO.setMappingImproveEnable(2);
        Assertions.assertTrue(indexTemplateService.updateTemplateConfig(null, "admin").failed());
        // 模版不存在
        Mockito.when(indexTemplateDAO.getById(1)).thenReturn(null);
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        Assertions.assertTrue(indexTemplateService.updateTemplateConfig(configDTO, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        Mockito.when(indexTemplateDAO.getById(2)).thenReturn(CustomDataSource.templateLogicSource());
        configDTO.setLogicId(2);
        Mockito.when(indexTemplateConfigDAO.update(Mockito.any())).thenReturn(1);
        Assertions.assertTrue(indexTemplateService.updateTemplateConfig(configDTO, "admin").success());
    }

    @Test
    public void delTemplateTest() throws AdminOperateException {
        Mockito.when(indexTemplateDAO.delete(2)).thenReturn(1);
        Mockito.when(indexTemplateDAO.getById(1)).thenReturn(null);
        Mockito.when(indexTemplateDAO.getById(2)).thenReturn(CustomDataSource.templateLogicSource());
        Mockito.when(indexTemplatePhyService.delTemplateByLogicId(Mockito.anyInt(), Mockito.anyString())).thenReturn(Result.buildSucc());
        Mockito.when(logicTemplateAuthService.deleteTemplateAuthByTemplateId(Mockito.anyInt(), Mockito.anyString())).thenReturn(Result.buildSucc());
        // 不存在的 id
        Assertions.assertTrue(indexTemplateService.delTemplate(1, "admin").failed());
        // 存在的 id
        Assertions.assertTrue(indexTemplateService.delTemplate(2, "admin").success());
    }

    @Test
    public void getLogicTemplatesTest() {
        Mockito.when(indexTemplateDAO.listByCondition(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplate> ret = indexTemplateService.getLogicTemplates(CustomDataSource.indexTemplateLogicDTOFactory());
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void fuzzyLogicTemplatesByConditionTest() {
        Mockito.when(indexTemplateDAO.likeByCondition(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplate> ret = indexTemplateService.fuzzyLogicTemplatesByCondition(CustomDataSource.indexTemplateLogicDTOFactory());
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void pagingGetLogicTemplatesByConditionTest() {
       // Mockito.when(indexTemplateLogicDAO.pagingByCondition(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
       //         .thenReturn(CustomDataSource.getTemplateLogicPOList());
        //List<IndexTemplateLogic> ret = templateLogicService.pagingGetLogicTemplatesByCondition(CustomDataSource.indexTemplateLogicDTOFactory());
        //Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void fuzzyLogicTemplatesHitByConditionTest() {
        Mockito.when(indexTemplateDAO.getTotalHitByCondition(Mockito.any())).thenReturn(1l);
        Long cnt = indexTemplateService.fuzzyLogicTemplatesHitByCondition(CustomDataSource.indexTemplateLogicDTOFactory());
        Assertions.assertEquals(1, cnt);
    }

    @Test
    public void getLogicTemplateByNameTest() {
        Mockito.when(indexTemplateDAO.listByName(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplate> ret = indexTemplateService.getLogicTemplateByName("test");
        Assertions.assertEquals(ret.size(), CustomDataSource.SIZE);
    }

    @Test
    public void getLogicTemplateByIdTest() {
        Mockito.when(indexTemplateDAO.getById(Mockito.any())).thenReturn(CustomDataSource.templateLogicSource());
        IndexTemplate logic = indexTemplateService.getLogicTemplateById(1);
        Assertions.assertNotNull(logic);
    }

    @Test
    public void getTemplateConfigTest() {
        Mockito.when(indexTemplateConfigDAO.getByLogicId(Mockito.any())).thenReturn(CustomDataSource.templateConfigSource());
        IndexTemplateConfig templateConfig = indexTemplateService.getTemplateConfig(1);
        Assertions.assertNotNull(templateConfig);
    }

    @Test
    public void existTest() {
        Mockito.when(indexTemplateDAO.getById(Mockito.any())).thenReturn(CustomDataSource.templateLogicSource());
        Assertions.assertTrue(indexTemplateService.exist(1));
    }

    @Test
    public void getAllLogicTemplatesTest() {
        Mockito.when(indexTemplateDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplate> ret = indexTemplateService.getAllLogicTemplates();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplatesMapTest() {
        Mockito.when(indexTemplateDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Map<Integer, IndexTemplate> map = indexTemplateService.getAllLogicTemplatesMap();
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    public void getLogicTemplatesByIdsTest() {
        Mockito.when(indexTemplateDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<Integer> logicTemplateIds = new ArrayList<>();
        logicTemplateIds.add(1);
        logicTemplateIds.add(2);
        List<IndexTemplate> ret = indexTemplateService.getLogicTemplatesByIds(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplatesMapByIdsTest() {
        Mockito.when(indexTemplateDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<Integer> logicTemplateIds = new ArrayList<>();
        logicTemplateIds.add(1);
        Map<Integer, IndexTemplate> map = indexTemplateService.getLogicTemplatesMapByIds(logicTemplateIds);
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    public void getAppLogicTemplatesByAppIdTest() {
        Mockito.when(indexTemplateDAO.listByAppId(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplate> ret = indexTemplateService.getAppLogicTemplatesByAppId(1);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicClusterTemplatesTest() {
        Mockito.when(indexTemplateDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplate> ret = indexTemplateService.getLogicClusterTemplates(1L);
        Assertions.assertTrue(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplatesPhysicalCountTest() {
        Mockito.when(indexTemplatePhyDAO.listAll()).thenReturn(CustomDataSource.getTemplatePhysicalPOList());
        Mockito.when(indexTemplatePhyService.listTemplate()).thenReturn(CustomDataSource.getIndexTemplatePhyList());
        Map<Integer, Integer> ret = indexTemplateService.getAllLogicTemplatesPhysicalCount();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateTypesTest() {
        Mockito.when(indexTemplateTypeDAO.listByIndexTemplateId(Mockito.anyInt())).thenReturn(CustomDataSource.getTemplateTypePOList());
        List<IndexTemplateType> ret = indexTemplateService.getLogicTemplateTypes(1);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getTemplateByResponsibleIdTest() {
        Mockito.when(indexTemplateDAO.likeByResponsible(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplate> ret = indexTemplateService.getTemplateByResponsibleId(1l);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplatesWithClusterAndMasterTemplateTest() {
        Mockito.when(indexTemplateDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogicWithClusterAndMasterTemplate> ret = indexTemplateService.getLogicTemplatesWithClusterAndMasterTemplate();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplatesWithClusterAndMasterTemplateMapTest() {
        Mockito.when(indexTemplateDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1);
        Map<Integer, IndexTemplateLogicWithClusterAndMasterTemplate> ret = indexTemplateService.getLogicTemplatesWithClusterAndMasterTemplateMap(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithClustersTest() {
        Mockito.when(indexTemplateDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1);
        List<IndexTemplateWithCluster> ret = indexTemplateService.getLogicTemplateWithClusters(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplateWithClustersTest() {
        Mockito.when(indexTemplateDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateWithPhyTemplates> ret = indexTemplateService.getAllLogicTemplateWithPhysicals();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplateWithPhysicalsTest() {
        Mockito.when(indexTemplateDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateWithPhyTemplates> ret = indexTemplateService.getAllLogicTemplateWithPhysicals();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithPhysicalsByIdsTest() {
        Mockito.when(indexTemplateDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1);
        List<IndexTemplateWithPhyTemplates> ret = indexTemplateService.getLogicTemplateWithPhysicalsByIds(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithPhysicalsByIdTest() {
        Mockito.when(indexTemplateDAO.getById(Mockito.any())).thenReturn(CustomDataSource.templateLogicSource());
        IndexTemplateWithPhyTemplates ret = indexTemplateService.getLogicTemplateWithPhysicalsById(37479);
        Assertions.assertNotNull(ret);
    }

    @Test
    public void getTemplateWithPhysicalByDataCenterTest() {
        Mockito.when(indexTemplateDAO.listByDataCenter(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateWithPhyTemplates> ret = indexTemplateService.getTemplateWithPhysicalByDataCenter("cn");
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void listByRegionIdTest() {
        Mockito.when(indexTemplatePhyService.listByRegionId(Mockito.anyInt())).thenReturn(Result.buildSucc(CustomDataSource.getIndexTemplatePhyList()));
        Result<List<IndexTemplate>> indexTemplateResult = indexTemplateService.listByRegionId(127);
        Assertions.assertTrue(indexTemplateResult.success());
        Assertions.assertTrue(null != indexTemplateResult.getData());
    }

    @Test
    public void convert2WithClusterTest() {
        List<IndexTemplateWithPhyTemplates> physicalTemplates = indexTemplateService.getLogicTemplateWithPhysicalsByIds(
                Sets.newHashSet(1111, 1109, 1107));
        List<IndexTemplateWithCluster> indexTemplateWithClusters = indexTemplateService.convert2WithCluster(physicalTemplates);
    }

    @Test
    public void listByResourceIdsTest() {
        Mockito.when(indexTemplateDAO.listByResourceIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplate> indexTemplateList = indexTemplateService.listByResourceIds(Lists.newArrayList(1125L, 1129L, 1141L));
        Assertions.assertTrue(CollectionUtils.isNotEmpty(indexTemplateList));
    }

}