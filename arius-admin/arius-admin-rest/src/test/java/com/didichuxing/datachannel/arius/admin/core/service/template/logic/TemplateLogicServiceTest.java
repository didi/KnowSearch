package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateType;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateConfigDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateLogicDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhysicalDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateTypeDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class TemplateLogicServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private TemplateLogicService templateLogicService;

    @MockBean
    private TemplatePhyService templatePhyService;

    @MockBean
    private AppLogicTemplateAuthService logicTemplateAuthService;

    @MockBean
    private AppClusterLogicAuthService logicClusterAuthService;

    @MockBean
    private IndexTemplatePhysicalDAO indexTemplatePhysicalDAO;

    @MockBean
    private IndexTemplateLogicDAO indexTemplateLogicDAO;

    @MockBean
    private IndexTemplateConfigDAO indexTemplateConfigDAO;

    @MockBean
    private IndexTemplateTypeDAO indexTemplateTypeDAO;

    @MockBean
    private ClusterLogicService clusterLogicService;

    @Test
    public void editTemplateNameTest() throws AdminOperateException {
        Mockito.when(indexTemplateLogicDAO.update(Mockito.any())).thenReturn(1);
        IndexTemplateLogicDTO indexTemplateLogicDTO = new IndexTemplateLogicDTO();
        Assertions.assertTrue(templateLogicService.editTemplateName(indexTemplateLogicDTO, CustomDataSource.OPERATOR).failed());
        indexTemplateLogicDTO.setId(19489);
        Assertions.assertTrue(templateLogicService.editTemplateName(indexTemplateLogicDTO, CustomDataSource.OPERATOR).failed());
        indexTemplateLogicDTO.setName("wpk-tes");
        Assertions.assertTrue(templateLogicService.editTemplateName(indexTemplateLogicDTO, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void insertTemplateConfigTest() {
        IndexTemplateConfig config = new IndexTemplateConfig();
        Mockito.when(indexTemplateConfigDAO.insert(Mockito.any())).thenReturn(1);
        Result<Void> result = templateLogicService.insertTemplateConfig(config);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void upsertTemplateShardFactorTest() {
        Mockito.when(indexTemplateLogicDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(indexTemplateConfigDAO.getByLogicId(Mockito.isNull())).thenReturn(CustomDataSource.templateConfigSource());
        Mockito.when(indexTemplateConfigDAO.getByLogicId(1)).thenReturn(CustomDataSource.templateConfigSource());
        double factor = 1.0d;
        // 不存在的 logiTemplateId
        templateLogicService.updateTemplateShardFactorIfGreater(-1, factor, "admin");
        // 存在的 logiTemplateId
        templateLogicService.updateTemplateShardFactorIfGreater(1, factor, "admin");
        Assertions.assertNull(null);
    }
 

    @Test
    public void updateTemplateShardFactorIfGreaterTest() {
        Mockito.when(indexTemplateLogicDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(indexTemplateConfigDAO.getByLogicId(Mockito.isNull())).thenReturn(CustomDataSource.templateConfigSource());
        Mockito.when(indexTemplateConfigDAO.getByLogicId(1)).thenReturn(CustomDataSource.templateConfigSource());
        double factor = 1.0d;
        templateLogicService.updateTemplateShardFactorIfGreater(-1, factor, "admin");
        factor = 999.0d;
        templateLogicService.updateTemplateShardFactorIfGreater(1, factor, "admin");
        Assertions.assertNull(null);
    }

    @Test
    public void updateTemplateConfigTest() {
        IndexTemplateConfigDTO configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        Assertions.assertTrue(templateLogicService.updateTemplateConfig(null, "admin").failed());
        configDTO.setLogicId(null);
        Assertions.assertTrue(templateLogicService.updateTemplateConfig(configDTO, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        configDTO.setIsSourceSeparated(2);
        Assertions.assertTrue(templateLogicService.updateTemplateConfig(null, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        configDTO.setIsSourceSeparated(2);
        Assertions.assertTrue(templateLogicService.updateTemplateConfig(null, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        configDTO.setMappingImproveEnable(2);
        Assertions.assertTrue(templateLogicService.updateTemplateConfig(null, "admin").failed());
        // 模版不存在
        Mockito.when(indexTemplateLogicDAO.getById(1)).thenReturn(null);
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        Assertions.assertTrue(templateLogicService.updateTemplateConfig(configDTO, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        Mockito.when(indexTemplateLogicDAO.getById(2)).thenReturn(CustomDataSource.templateLogicSource());
        configDTO.setLogicId(2);
        Mockito.when(indexTemplateConfigDAO.update(Mockito.any())).thenReturn(1);
        Assertions.assertTrue(templateLogicService.updateTemplateConfig(configDTO, "admin").success());
    }

    @Test
    public void delTemplateTest() throws AdminOperateException {
        Mockito.when(indexTemplateLogicDAO.delete(2)).thenReturn(1);
        Mockito.when(indexTemplateLogicDAO.getById(1)).thenReturn(null);
        Mockito.when(indexTemplateLogicDAO.getById(2)).thenReturn(CustomDataSource.templateLogicSource());
        Mockito.when(templatePhyService.delTemplateByLogicId(Mockito.anyInt(), Mockito.anyString())).thenReturn(Result.buildSucc());
        Mockito.when(logicTemplateAuthService.deleteTemplateAuthByTemplateId(Mockito.anyInt(), Mockito.anyString())).thenReturn(Result.buildSucc());
        // 不存在的 id
        Assertions.assertTrue(templateLogicService.delTemplate(1, "admin").failed());
        // 存在的 id
        Assertions.assertTrue(templateLogicService.delTemplate(2, "admin").success());
    }

    @Test
    public void getLogicTemplatesTest() {
        Mockito.when(indexTemplateLogicDAO.listByCondition(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogic> ret = templateLogicService.getLogicTemplates(CustomDataSource.indexTemplateLogicDTOFactory());
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void fuzzyLogicTemplatesByConditionTest() {
        Mockito.when(indexTemplateLogicDAO.likeByCondition(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogic> ret = templateLogicService.fuzzyLogicTemplatesByCondition(CustomDataSource.indexTemplateLogicDTOFactory());
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
        Mockito.when(indexTemplateLogicDAO.getTotalHitByCondition(Mockito.any())).thenReturn(1l);
        Long cnt = templateLogicService.fuzzyLogicTemplatesHitByCondition(CustomDataSource.indexTemplateLogicDTOFactory());
        Assertions.assertEquals(1, cnt);
    }

    @Test
    public void getLogicTemplateByNameTest() {
        Mockito.when(indexTemplateLogicDAO.listByName(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogic> ret = templateLogicService.getLogicTemplateByName("test");
        Assertions.assertEquals(ret.size(), CustomDataSource.SIZE);
    }

    @Test
    public void getLogicTemplateByIdTest() {
        Mockito.when(indexTemplateLogicDAO.getById(Mockito.any())).thenReturn(CustomDataSource.templateLogicSource());
        IndexTemplateLogic logic = templateLogicService.getLogicTemplateById(1);
        Assertions.assertNotNull(logic);
    }

    @Test
    public void getTemplateConfigTest() {
        Mockito.when(indexTemplateConfigDAO.getByLogicId(Mockito.any())).thenReturn(CustomDataSource.templateConfigSource());
        IndexTemplateConfig templateConfig = templateLogicService.getTemplateConfig(1);
        Assertions.assertNotNull(templateConfig);
    }

    @Test
    public void existTest() {
        Mockito.when(indexTemplateLogicDAO.getById(Mockito.any())).thenReturn(CustomDataSource.templateLogicSource());
        Assertions.assertTrue(templateLogicService.exist(1));
    }

    @Test
    public void getAllLogicTemplatesTest() {
        Mockito.when(indexTemplateLogicDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogic> ret = templateLogicService.getAllLogicTemplates();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplatesMapTest() {
        Mockito.when(indexTemplateLogicDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Map<Integer, IndexTemplateLogic> map = templateLogicService.getAllLogicTemplatesMap();
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    public void getLogicTemplatesByIdsTest() {
        Mockito.when(indexTemplateLogicDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<Integer> logicTemplateIds = new ArrayList<>();
        logicTemplateIds.add(1);
        logicTemplateIds.add(2);
        List<IndexTemplateLogic> ret = templateLogicService.getLogicTemplatesByIds(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplatesMapByIdsTest() {
        Mockito.when(indexTemplateLogicDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<Integer> logicTemplateIds = new ArrayList<>();
        logicTemplateIds.add(1);
        Map<Integer, IndexTemplateLogic> map = templateLogicService.getLogicTemplatesMapByIds(logicTemplateIds);
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    public void getAppLogicTemplatesByAppIdTest() {
        Mockito.when(indexTemplateLogicDAO.listByAppId(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogic> ret = templateLogicService.getAppLogicTemplatesByAppId(1);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicClusterTemplatesTest() {
        Mockito.when(indexTemplateLogicDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogic> ret = templateLogicService.getLogicClusterTemplates(1L);
        Assertions.assertTrue(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplatesPhysicalCountTest() {
        Mockito.when(indexTemplatePhysicalDAO.listAll()).thenReturn(CustomDataSource.getTemplatePhysicalPOList());
        Mockito.when(templatePhyService.listTemplate()).thenReturn(CustomDataSource.getIndexTemplatePhyList());
        Map<Integer, Integer> ret = templateLogicService.getAllLogicTemplatesPhysicalCount();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateTypesTest() {
        Mockito.when(indexTemplateTypeDAO.listByIndexTemplateId(Mockito.anyInt())).thenReturn(CustomDataSource.getTemplateTypePOList());
        List<IndexTemplateType> ret = templateLogicService.getLogicTemplateTypes(1);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getTemplateByResponsibleIdTest() {
        Mockito.when(indexTemplateLogicDAO.likeByResponsible(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogic> ret = templateLogicService.getTemplateByResponsibleId(1l);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplatesWithClusterAndMasterTemplateTest() {
        Mockito.when(indexTemplateLogicDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogicWithClusterAndMasterTemplate> ret = templateLogicService.getLogicTemplatesWithClusterAndMasterTemplate();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithClusterAndMasterTemplateTest() {
        Mockito.when(indexTemplateLogicDAO.getById(Mockito.anyInt())).thenReturn(CustomDataSource.templateLogicSource());
        IndexTemplateLogicWithClusterAndMasterTemplate ret = templateLogicService.getLogicTemplateWithClusterAndMasterTemplate(1);
        Assertions.assertNotNull(ret);
    }

    @Test
    public void getLogicTemplatesWithClusterAndMasterTemplateMapTest() {
        Mockito.when(indexTemplateLogicDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1);
        Map<Integer, IndexTemplateLogicWithClusterAndMasterTemplate> ret = templateLogicService.getLogicTemplatesWithClusterAndMasterTemplateMap(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithClusterTest() {
        Mockito.when(indexTemplateLogicDAO.getById(1)).thenReturn(CustomDataSource.templateLogicSource());
        IndexTemplateLogicWithCluster ret = templateLogicService.getLogicTemplateWithCluster(1);
        Assertions.assertNotNull(ret);
    }

    @Test
    public void getLogicTemplateWithClustersTest() {
        Mockito.when(indexTemplateLogicDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1);
        List<IndexTemplateLogicWithCluster> ret = templateLogicService.getLogicTemplateWithClusters(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplateWithClustersTest() {
        Mockito.when(indexTemplateLogicDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogicWithPhyTemplates> ret = templateLogicService.getAllLogicTemplateWithPhysicals();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplateWithPhysicalsTest() {
        Mockito.when(indexTemplateLogicDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogicWithPhyTemplates> ret = templateLogicService.getAllLogicTemplateWithPhysicals();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithPhysicalsByIdsTest() {
        Mockito.when(indexTemplateLogicDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1);
        List<IndexTemplateLogicWithPhyTemplates> ret = templateLogicService.getLogicTemplateWithPhysicalsByIds(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithPhysicalsByIdTest() {
        Mockito.when(indexTemplateLogicDAO.getById(Mockito.any())).thenReturn(CustomDataSource.templateLogicSource());
        IndexTemplateLogicWithPhyTemplates ret = templateLogicService.getLogicTemplateWithPhysicalsById(37479);
        Assertions.assertNotNull(ret);
    }

    @Test
    public void getTemplateWithPhysicalByDataCenterTest() {
        Mockito.when(indexTemplateLogicDAO.listByDataCenter(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogicWithPhyTemplates> ret = templateLogicService.getTemplateWithPhysicalByDataCenter("cn");
        Assertions.assertFalse(ret.isEmpty());
    }
}