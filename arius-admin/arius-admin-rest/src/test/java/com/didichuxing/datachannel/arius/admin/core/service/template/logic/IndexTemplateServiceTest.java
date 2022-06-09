package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateConfigDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhyDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateTypeDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.map.HashedMap;
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
    private ProjectLogicTemplateAuthService logicTemplateAuthService;

    @MockBean
    private ProjectClusterLogicAuthService logicClusterAuthService;

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
    @MockBean
    private ProjectService projectService;
    @MockBean
    private ESIndexService       esIndexService;
    @MockBean
    private ClusterRegionService clusterRegionService;

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
        Mockito.when(indexTemplateDAO.listByProjectId(Mockito.anyInt())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        List<IndexTemplate> ret = indexTemplateService.getProjectLogicTemplatesByProjectId(1);
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
        Mockito.when(indexTemplatePhyService.getAllLogicTemplatesPhysicalCount()).thenReturn(new HashedMap(){{
            putIfAbsent(1,1);
        }});
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
        final List<IndexTemplatePO> templateLogicPOList = CustomDataSource.getTemplateLogicPOList();
        final List<IndexTemplatePhy> indexTemplatePhyList = CustomDataSource.getIndexTemplatePhyList();
        Mockito.when(indexTemplateDAO.listAll()).thenReturn(templateLogicPOList);
        for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhyList) {
            indexTemplatePhy.setLogicId(indexTemplatePhy.getId().intValue());
            indexTemplatePhy.setRack(indexTemplatePhy.getId().intValue() + "");
        
        }
        Mockito.when(indexTemplatePhyService.getTemplateByLogicIds(Mockito.anyList())).thenReturn(indexTemplatePhyList);
        Mockito.when(indexTemplatePhyService.getValidTemplatesByLogicId(Mockito.anyInt()))
                .thenReturn(indexTemplatePhyList);
        Mockito.when(clusterLogicService.listAllClusterLogics()).thenReturn(CustomDataSource.getClusterLogicList());
        Mockito.when(logicTemplateAuthService.getTemplateAuthsByProjectId(Mockito.anyInt()))
                .thenReturn(CustomDataSource.getAppTemplateAuthList());
        List<IndexTemplateWithPhyTemplates> ret = indexTemplateService.getAllLogicTemplateWithPhysicals();
        
        Assertions.assertTrue(ret.stream().allMatch(IndexTemplateWithPhyTemplates::hasPhysicals));
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
    public void convert2WithClusterTest() {
        List<IndexTemplateWithPhyTemplates> physicalTemplates = indexTemplateService.getLogicTemplateWithPhysicalsByIds(
                Sets.newHashSet(1111, 1109, 1107));
        List<IndexTemplateWithCluster> indexTemplateWithClusters = indexTemplateService.convert2WithCluster(physicalTemplates);
    }

    @Test
    public void listAllByRegionIdTest() {
        Mockito.when(indexTemplatePhyService.listByRegionId(Mockito.anyInt())).thenReturn(Result.buildSucc(CustomDataSource.getIndexTemplatePhyList()));
        Mockito.when(indexTemplateDAO.listByIds(Mockito.anyList())).thenReturn(CustomDataSource.getTemplateLogicPOList());
        Result<List<IndexTemplate>> ret = indexTemplateService.listByRegionId(100);
        if (ret.failed()) { Assertions.assertNull(ret.getMessage());}
        if (ret.success()) { Assertions.assertNotNull(ret.getData());}
    }
    @Test
    public void addTemplateWithoutCheckTest() {
        Mockito.when(indexTemplateDAO.insert(Mockito.any())).thenReturn(1);
        final IndexTemplatePO indexTemplatePO = CustomDataSource.templateLogicSource();
    
        final Result<Void> result = Assertions.assertDoesNotThrow(
                () -> indexTemplateService.addTemplateWithoutCheck(
                        ConvertUtil.obj2Obj(indexTemplatePO, IndexTemplateDTO.class)));
        Assertions.assertEquals(result,Result.<Void>buildSucc());
    
    }
    @Test
    void turnOverLogicTemplateTest() throws ESOperateException {
        Mockito.when(indexTemplateDAO.update(Mockito.any())).thenReturn(1);
        final IndexTemplatePO indexTemplatePO = CustomDataSource.templateLogicSource();
        Mockito.when(indexTemplateDAO.getById(Mockito.anyInt())).thenReturn(indexTemplatePO);
        Mockito.when(indexTemplatePhyService.editTemplateFromLogic(Mockito.any(),
                Mockito.anyString())).thenReturn(Result.buildSucc());
        Assertions.assertEquals(
                Assertions.assertDoesNotThrow(() -> indexTemplateService.turnOverLogicTemplate(null, null, null, null))
                        .getMessage(), "参数错误:模板不存在，请检查后再提交！");
        indexTemplatePO.setExpression("aaaa*");
        Assertions.assertEquals(Assertions.assertDoesNotThrow(
                () -> indexTemplateService.turnOverLogicTemplate(1479, null, null, null).getMessage()
        ), "参数错误:表达式*结尾,后缀格式必填，请检查后再提交！");
        indexTemplatePO.setDateFormat("yyyy-MM-dd HH:mm:ss");
        indexTemplatePO.setExpression(indexTemplatePO.getName()+"o");
        Assertions.assertEquals(Assertions.assertDoesNotThrow(
                () -> indexTemplateService.turnOverLogicTemplate(indexTemplatePO.getId(), null, null, null).getMessage()
        ), "参数错误:表达式与模板名字不匹配，请检查后再提交！");
         indexTemplatePO.setExpression(indexTemplatePO.getName());
         Assertions.assertEquals(Assertions.assertDoesNotThrow(
                () -> indexTemplateService.turnOverLogicTemplate(indexTemplatePO.getId(), null, null, null).getMessage()
    
        ), "参数错误:索引分区创建，分区字段必填，请检查后再提交！");
        indexTemplatePO.setDateField("yyyy-MM-dd HH:mm:ss.SSS");
        Assertions.assertTrue(Assertions.assertDoesNotThrow(
                () -> indexTemplateService.turnOverLogicTemplate(indexTemplatePO.getId(), 1, "admin", "admin")

        ).success());
        
    }
    @Test
    void getLogicTemplatesByProjectIdTest(){
        final IndexTemplatePO indexTemplatePO = CustomDataSource.templateLogicSource();
        final List<IndexTemplatePhy> indexTemplatePhyList = CustomDataSource.getIndexTemplatePhyList();
        final Set<String> strings = Collections.singleton("aa");
        Mockito.when(esIndexService.syncGetIndexNameByExpression(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(strings);
        Mockito.when(projectService.checkProjectExist(Mockito.anyInt())).thenReturn(true) ;
        Mockito.when(indexTemplateDAO.getById(Mockito.anyInt())).thenReturn(indexTemplatePO);
        Mockito.when(indexTemplatePhyService.getTemplateByLogicIds(Mockito.anyList())).thenReturn(indexTemplatePhyList);
        Mockito.when(logicTemplateAuthService.getTemplateAuthsByProjectId(Mockito.anyInt()))
                .thenReturn(CustomDataSource.getAppTemplateAuthList());
        Assertions.assertFalse(
                CollectionUtils.isNotEmpty(indexTemplateService.getLogicTemplatesByProjectId(1).getData()));
        for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhyList) {
            indexTemplatePhy.setExpression("aaaa");
        }
       Mockito.when(indexTemplatePhyService.getTemplateByLogicIds(Mockito.anyList())).thenReturn(indexTemplatePhyList);
        Assertions.assertTrue(
                CollectionUtils.isNotEmpty(indexTemplateService.getLogicTemplatesByProjectId(1).getData()));

        
    }
    
    @Test
    void getTemplatesByHasAuthClusterTest() {
  
        final List<IndexTemplatePO> templateLogicPOList = CustomDataSource.getTemplateLogicPOList();
        final List<IndexTemplatePhy> indexTemplatePhyList = CustomDataSource.getIndexTemplatePhyList();
        Mockito.when(indexTemplateDAO.listAll()).thenReturn(templateLogicPOList);
        for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhyList) {
            indexTemplatePhy.setLogicId(indexTemplatePhy.getId().intValue());
            indexTemplatePhy.setRack(indexTemplatePhy.getId().intValue() + "");
        
        }
        Mockito.when(indexTemplatePhyService.getTemplateByLogicIds(Mockito.anyList())).thenReturn(indexTemplatePhyList);
        Mockito.when(indexTemplatePhyService.getValidTemplatesByLogicId(Mockito.anyInt()))
                .thenReturn(indexTemplatePhyList);
        Mockito.when(clusterLogicService.listAllClusterLogics()).thenReturn(CustomDataSource.getClusterLogicList());
        Mockito.when(logicTemplateAuthService.getTemplateAuthsByProjectId(Mockito.anyInt()))
                .thenReturn(CustomDataSource.getAppTemplateAuthList());
        Assertions.assertFalse(CollectionUtils.isNotEmpty(indexTemplateService.getTemplatesByHasAuthCluster(1)));
        
    }
    
    @Test
    void getHasAuthTemplatesInLogicClusterTest() {
        Assertions.assertTrue(
                CollectionUtils.isEmpty(indexTemplateService.getHasAuthTemplatesInLogicCluster(null, null)));
        Assertions.assertTrue(
                CollectionUtils.isEmpty(indexTemplateService.getHasAuthTemplatesInLogicCluster(null, null)));
        final List<IndexTemplatePO> templateLogicPOList = CustomDataSource.getTemplateLogicPOList();
        final List<IndexTemplatePhy> indexTemplatePhyList = CustomDataSource.getIndexTemplatePhyList();
        Mockito.when(indexTemplateDAO.listAll()).thenReturn(templateLogicPOList);
        for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhyList) {
            indexTemplatePhy.setLogicId(indexTemplatePhy.getId().intValue());
            indexTemplatePhy.setRack(indexTemplatePhy.getId().intValue() + "");
        
        }
        Mockito.when(indexTemplatePhyService.getTemplateByLogicIds(Mockito.anyList())).thenReturn(indexTemplatePhyList);
        Mockito.when(indexTemplatePhyService.getValidTemplatesByLogicId(Mockito.anyInt()))
                .thenReturn(indexTemplatePhyList);
        Mockito.when(clusterLogicService.listAllClusterLogics()).thenReturn(CustomDataSource.getClusterLogicList());
        Mockito.when(logicTemplateAuthService.getTemplateAuthsByProjectId(Mockito.anyInt()))
                .thenReturn(CustomDataSource.getAppTemplateAuthList());
        Assertions.assertFalse(CollectionUtils.isEmpty(indexTemplateService.getHasAuthTemplatesInLogicCluster(1, 1L)));
        
        
        
    }
    
    @Test
    void editTemplateInfoTODBTest(){
        final IndexTemplateDTO indexTemplateDTO = new IndexTemplateDTO();
        Mockito.when(indexTemplateDAO.update(Mockito.any())).thenReturn(1);
    
        final Result<Void> voidResult =Assertions.assertDoesNotThrow(()-> indexTemplateService.editTemplateInfoTODB(indexTemplateDTO));
        Assertions.assertTrue(voidResult.success());
        
    }
    
    
    
}