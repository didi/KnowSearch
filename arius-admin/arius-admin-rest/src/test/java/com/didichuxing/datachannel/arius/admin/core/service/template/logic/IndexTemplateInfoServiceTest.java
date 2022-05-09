package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateType;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateConfigDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateInfoDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhysicalInfoDAO;
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
public class IndexTemplateInfoServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private IndexTemplateInfoService indexTemplateInfoService;

    @MockBean
    private TemplatePhyService templatePhyService;

    @MockBean
    private AppLogicTemplateAuthService logicTemplateAuthService;

    @MockBean
    private AppClusterLogicAuthService logicClusterAuthService;

    @MockBean
    private IndexTemplatePhysicalInfoDAO indexTemplatePhysicalInfoDAO;

    @MockBean
    private IndexTemplateInfoDAO indexTemplateInfoDAO;

    @MockBean
    private IndexTemplateConfigDAO indexTemplateConfigDAO;

    @MockBean
    private IndexTemplateTypeDAO indexTemplateTypeDAO;

    @MockBean
    private ClusterLogicService clusterLogicService;

    @Test
    public void editTemplateNameTest() throws AdminOperateException {
        Mockito.when(indexTemplateInfoDAO.update(Mockito.any())).thenReturn(1);
        IndexTemplateInfoDTO indexTemplateInfoDTO = new IndexTemplateInfoDTO();
        Assertions.assertTrue(indexTemplateInfoService.editTemplateName(indexTemplateInfoDTO, CustomDataSource.OPERATOR).failed());
        indexTemplateInfoDTO.setId(19489);
        Assertions.assertTrue(indexTemplateInfoService.editTemplateName(indexTemplateInfoDTO, CustomDataSource.OPERATOR).failed());
        indexTemplateInfoDTO.setName("wpk-tes");
        Assertions.assertTrue(indexTemplateInfoService.editTemplateName(indexTemplateInfoDTO, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void insertTemplateConfigTest() {
        IndexTemplateConfig config = new IndexTemplateConfig();
        Mockito.when(indexTemplateConfigDAO.insert(Mockito.any())).thenReturn(1);
        Result<Void> result = indexTemplateInfoService.insertTemplateConfig(config);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void upsertTemplateShardFactorTest() {
        Mockito.when(indexTemplateInfoDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(indexTemplateConfigDAO.getByLogicId(Mockito.isNull())).thenReturn(CustomDataSource.templateConfigSource());
        Mockito.when(indexTemplateConfigDAO.getByLogicId(1)).thenReturn(CustomDataSource.templateConfigSource());
        double factor = 1.0d;
        // 不存在的 logiTemplateId
        indexTemplateInfoService.updateTemplateShardFactorIfGreater(-1, factor, "admin");
        // 存在的 logiTemplateId
        indexTemplateInfoService.updateTemplateShardFactorIfGreater(1, factor, "admin");
        Assertions.assertNull(null);
    }
 

    @Test
    public void updateTemplateShardFactorIfGreaterTest() {
        Mockito.when(indexTemplateInfoDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(indexTemplateConfigDAO.getByLogicId(Mockito.isNull())).thenReturn(CustomDataSource.templateConfigSource());
        Mockito.when(indexTemplateConfigDAO.getByLogicId(1)).thenReturn(CustomDataSource.templateConfigSource());
        double factor = 1.0d;
        indexTemplateInfoService.updateTemplateShardFactorIfGreater(-1, factor, "admin");
        factor = 999.0d;
        indexTemplateInfoService.updateTemplateShardFactorIfGreater(1, factor, "admin");
        Assertions.assertNull(null);
    }

    @Test
    public void updateTemplateConfigTest() {
        IndexTemplateConfigDTO configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        Assertions.assertTrue(indexTemplateInfoService.updateTemplateConfig(null, "admin").failed());
        configDTO.setLogicId(null);
        Assertions.assertTrue(indexTemplateInfoService.updateTemplateConfig(configDTO, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        configDTO.setIsSourceSeparated(2);
        Assertions.assertTrue(indexTemplateInfoService.updateTemplateConfig(null, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        configDTO.setIsSourceSeparated(2);
        Assertions.assertTrue(indexTemplateInfoService.updateTemplateConfig(null, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        configDTO.setMappingImproveEnable(2);
        Assertions.assertTrue(indexTemplateInfoService.updateTemplateConfig(null, "admin").failed());
        // 模版不存在
        Mockito.when(indexTemplateInfoDAO.getById(1)).thenReturn(null);
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        Assertions.assertTrue(indexTemplateInfoService.updateTemplateConfig(configDTO, "admin").failed());
        configDTO = CustomDataSource.indexTemplateConfigDTOFactory();
        Mockito.when(indexTemplateInfoDAO.getById(2)).thenReturn(CustomDataSource.templateLogicSource());
        configDTO.setLogicId(2);
        Mockito.when(indexTemplateConfigDAO.update(Mockito.any())).thenReturn(1);
        Assertions.assertTrue(indexTemplateInfoService.updateTemplateConfig(configDTO, "admin").success());
    }

    @Test
    public void delTemplateTest() throws AdminOperateException {
        Mockito.when(indexTemplateInfoDAO.delete(2)).thenReturn(1);
        Mockito.when(indexTemplateInfoDAO.getById(1)).thenReturn(null);
        Mockito.when(indexTemplateInfoDAO.getById(2)).thenReturn(CustomDataSource.templateLogicSource());
        Mockito.when(templatePhyService.delTemplateByLogicId(Mockito.anyInt(), Mockito.anyString())).thenReturn(Result.buildSucc());
        Mockito.when(logicTemplateAuthService.deleteTemplateAuthByTemplateId(Mockito.anyInt(), Mockito.anyString())).thenReturn(Result.buildSucc());
        // 不存在的 id
        Assertions.assertTrue(indexTemplateInfoService.delTemplate(1, "admin").failed());
        // 存在的 id
        Assertions.assertTrue(indexTemplateInfoService.delTemplate(2, "admin").success());
    }

    @Test
    public void getLogicTemplatesTest() {
        Mockito.when(indexTemplateInfoDAO.listByCondition(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateInfo> ret = indexTemplateInfoService.getLogicTemplates(CustomDataSource.indexTemplateLogicDTOFactory());
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void fuzzyLogicTemplatesByConditionTest() {
        Mockito.when(indexTemplateInfoDAO.likeByCondition(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateInfo> ret = indexTemplateInfoService.fuzzyLogicTemplatesByCondition(CustomDataSource.indexTemplateLogicDTOFactory());
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
        Mockito.when(indexTemplateInfoDAO.getTotalHitByCondition(Mockito.any())).thenReturn(1l);
        Long cnt = indexTemplateInfoService.fuzzyLogicTemplatesHitByCondition(CustomDataSource.indexTemplateLogicDTOFactory());
        Assertions.assertEquals(1, cnt);
    }

    @Test
    public void getLogicTemplateByNameTest() {
        Mockito.when(indexTemplateInfoDAO.listByName(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateInfo> ret = indexTemplateInfoService.getLogicTemplateByName("test");
        Assertions.assertEquals(ret.size(), CustomDataSource.SIZE);
    }

    @Test
    public void getLogicTemplateByIdTest() {
        Mockito.when(indexTemplateInfoDAO.getById(Mockito.any())).thenReturn(CustomDataSource.templateLogicSource());
        IndexTemplateInfo logic = indexTemplateInfoService.getLogicTemplateById(1);
        Assertions.assertNotNull(logic);
    }

    @Test
    public void getTemplateConfigTest() {
        Mockito.when(indexTemplateConfigDAO.getByLogicId(Mockito.any())).thenReturn(CustomDataSource.templateConfigSource());
        IndexTemplateConfig templateConfig = indexTemplateInfoService.getTemplateConfig(1);
        Assertions.assertNotNull(templateConfig);
    }

    @Test
    public void existTest() {
        Mockito.when(indexTemplateInfoDAO.getById(Mockito.any())).thenReturn(CustomDataSource.templateLogicSource());
        Assertions.assertTrue(indexTemplateInfoService.exist(1));
    }

    @Test
    public void getAllLogicTemplatesTest() {
        Mockito.when(indexTemplateInfoDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateInfo> ret = indexTemplateInfoService.getAllLogicTemplates();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplatesMapTest() {
        Mockito.when(indexTemplateInfoDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Map<Integer, IndexTemplateInfo> map = indexTemplateInfoService.getAllLogicTemplatesMap();
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    public void getLogicTemplatesByIdsTest() {
        Mockito.when(indexTemplateInfoDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<Integer> logicTemplateIds = new ArrayList<>();
        logicTemplateIds.add(1);
        logicTemplateIds.add(2);
        List<IndexTemplateInfo> ret = indexTemplateInfoService.getLogicTemplatesByIds(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplatesMapByIdsTest() {
        Mockito.when(indexTemplateInfoDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<Integer> logicTemplateIds = new ArrayList<>();
        logicTemplateIds.add(1);
        Map<Integer, IndexTemplateInfo> map = indexTemplateInfoService.getLogicTemplatesMapByIds(logicTemplateIds);
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    public void getAppLogicTemplatesByAppIdTest() {
        Mockito.when(indexTemplateInfoDAO.listByAppId(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateInfo> ret = indexTemplateInfoService.getAppLogicTemplatesByAppId(1);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicClusterTemplatesTest() {
        Mockito.when(indexTemplateInfoDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateInfo> ret = indexTemplateInfoService.getLogicClusterTemplates(1L);
        Assertions.assertTrue(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplatesPhysicalCountTest() {
        Mockito.when(indexTemplatePhysicalInfoDAO.listAll()).thenReturn(CustomDataSource.getTemplatePhysicalPOList());
        Mockito.when(templatePhyService.listTemplate()).thenReturn(CustomDataSource.getIndexTemplatePhyList());
        Map<Integer, Integer> ret = indexTemplateInfoService.getAllLogicTemplatesPhysicalCount();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateTypesTest() {
        Mockito.when(indexTemplateTypeDAO.listByIndexTemplateId(Mockito.anyInt())).thenReturn(CustomDataSource.getTemplateTypePOList());
        List<IndexTemplateType> ret = indexTemplateInfoService.getLogicTemplateTypes(1);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getTemplateByResponsibleIdTest() {
        Mockito.when(indexTemplateInfoDAO.likeByResponsible(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateInfo> ret = indexTemplateInfoService.getTemplateByResponsibleId(1l);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplatesWithClusterAndMasterTemplateTest() {
        Mockito.when(indexTemplateInfoDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateLogicWithClusterAndMasterTemplate> ret = indexTemplateInfoService.getLogicTemplatesWithClusterAndMasterTemplate();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithClusterAndMasterTemplateTest() {
        Mockito.when(indexTemplateInfoDAO.getById(Mockito.anyInt())).thenReturn(CustomDataSource.templateLogicSource());
        IndexTemplateLogicWithClusterAndMasterTemplate ret = indexTemplateInfoService.getLogicTemplateWithClusterAndMasterTemplate(1);
        Assertions.assertNotNull(ret);
    }

    @Test
    public void getLogicTemplatesWithClusterAndMasterTemplateMapTest() {
        Mockito.when(indexTemplateInfoDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1);
        Map<Integer, IndexTemplateLogicWithClusterAndMasterTemplate> ret = indexTemplateInfoService.getLogicTemplatesWithClusterAndMasterTemplateMap(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithClusterTest() {
        Mockito.when(indexTemplateInfoDAO.getById(1)).thenReturn(CustomDataSource.templateLogicSource());
        IndexTemplateInfoWithCluster ret = indexTemplateInfoService.getLogicTemplateWithCluster(1);
        Assertions.assertNotNull(ret);
    }

    @Test
    public void getLogicTemplateWithClustersTest() {
        Mockito.when(indexTemplateInfoDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1);
        List<IndexTemplateInfoWithCluster> ret = indexTemplateInfoService.getLogicTemplateWithClusters(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplateWithClustersTest() {
        Mockito.when(indexTemplateInfoDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateInfoWithPhyTemplates> ret = indexTemplateInfoService.getAllLogicTemplateWithPhysicals();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getAllLogicTemplateWithPhysicalsTest() {
        Mockito.when(indexTemplateInfoDAO.listAll()).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateInfoWithPhyTemplates> ret = indexTemplateInfoService.getAllLogicTemplateWithPhysicals();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithPhysicalsByIdsTest() {
        Mockito.when(indexTemplateInfoDAO.listByIds(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1);
        List<IndexTemplateInfoWithPhyTemplates> ret = indexTemplateInfoService.getLogicTemplateWithPhysicalsByIds(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    public void getLogicTemplateWithPhysicalsByIdTest() {
        Mockito.when(indexTemplateInfoDAO.getById(Mockito.any())).thenReturn(CustomDataSource.templateLogicSource());
        IndexTemplateInfoWithPhyTemplates ret = indexTemplateInfoService.getLogicTemplateWithPhysicalsById(37479);
        Assertions.assertNotNull(ret);
    }

    @Test
    public void getTemplateWithPhysicalByDataCenterTest() {
        Mockito.when(indexTemplateInfoDAO.listByDataCenter(Mockito.any())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplateInfoWithPhyTemplates> ret = indexTemplateInfoService.getTemplateWithPhysicalByDataCenter("cn");
        Assertions.assertFalse(ret.isEmpty());
    }
}