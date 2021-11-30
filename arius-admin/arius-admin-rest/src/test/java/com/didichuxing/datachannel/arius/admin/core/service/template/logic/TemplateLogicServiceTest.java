package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.*;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class TemplateLogicServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private TemplateLogicService templateLogicService;

    private static final String OPERATOR = "wpk";

    @Test
    void editTemplateNameTest() throws AdminOperateException {
        IndexTemplateLogicDTO indexTemplateLogicDTO = new IndexTemplateLogicDTO();
        Assertions.assertEquals("参数错误:索引ID必填，请检查后再提交！",
                templateLogicService.editTemplateName(indexTemplateLogicDTO, OPERATOR).getMessage());
        indexTemplateLogicDTO.setId(19489);
        Assertions.assertEquals("参数错误:索引名称必填，请检查后再提交！",
                templateLogicService.editTemplateName(indexTemplateLogicDTO, OPERATOR).getMessage());
        indexTemplateLogicDTO.setName("wpk-tes");
        Assertions.assertTrue(templateLogicService.editTemplateName(indexTemplateLogicDTO, OPERATOR).success());
    }

    @Test
    void insertTemplateConfigTest() {
        IndexTemplateConfig config = new IndexTemplateConfig();
        Result<Void> result = templateLogicService.insertTemplateConfig(config);
        Assertions.assertEquals(0, result.getCode());
    }

    @Test
    void upsertTemplateShardFactorTest() {
        // 存在的id
        int logicTemplateId = 1105;
        double factor = 1.0d;
        templateLogicService.updateTemplateShardFactorIfGreater(logicTemplateId, factor, "admin");
        // 不存在的id
        logicTemplateId = 9999999;
        templateLogicService.updateTemplateShardFactorIfGreater(logicTemplateId, factor, "admin");
        Assertions.assertNull(null);
    }

    @Test
    void updateTemplateShardFactorIfGreaterTest() {
        // 存在的id
        int logicTemplateId = 1105;
        double factor = 1.0d;
        templateLogicService.updateTemplateShardFactorIfGreater(logicTemplateId, factor, "admin");
        // 不存在的id
        logicTemplateId = 9999999;
        templateLogicService.updateTemplateShardFactorIfGreater(logicTemplateId, factor, "admin");
        // 更大的factor
        logicTemplateId = 1105;
        factor = 999.0d;
        templateLogicService.updateTemplateShardFactorIfGreater(logicTemplateId, factor, "admin");
        Assertions.assertNull(null);
    }

    @Test
    void updateTemplateConfigTest() {
        IndexTemplateConfigDTO configDTO = null;
        Result<Void> ret = templateLogicService.updateTemplateConfig(configDTO, "admin");
        Assertions.assertNotEquals(0, ret.getCode());
        configDTO = new IndexTemplateConfigDTO();
        // 不存在id
        configDTO.setLogicId(9999999);
        ret = templateLogicService.updateTemplateConfig(configDTO, "admin");
        Assertions.assertNotEquals(0, ret.getCode());
        // 存在id
        configDTO.setLogicId(1105);
        configDTO.setMappingImproveEnable(1);
        ret = templateLogicService.updateTemplateConfig(configDTO, "admin");
        Assertions.assertNotEquals(0, ret.getCode());
    }

    @Test
    void delTemplateTest() {
        // 不存的id
        int logicTemplateId = 9999999;
        Result<Void> ret = null;
        try {
            ret = templateLogicService.delTemplate(logicTemplateId, "admin");
            Assertions.assertNotEquals(0, ret.getCode());
        } catch (AdminOperateException e) {
            e.printStackTrace();
        }
        logicTemplateId = 1105;
        try {
            ret = templateLogicService.delTemplate(logicTemplateId, "admin");
            Assertions.assertEquals(0, ret.getCode());
        } catch (AdminOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getLogicTemplatesTest() {
        IndexTemplateLogicDTO dto = new IndexTemplateLogicDTO();
        dto.setName("arius");
        List<IndexTemplateLogic> ret = templateLogicService.getLogicTemplates(dto);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void fuzzyLogicTemplatesByConditionTest() {
        IndexTemplateLogicDTO dto = new IndexTemplateLogicDTO();
        dto.setName("arius");
        List<IndexTemplateLogic> ret = templateLogicService.fuzzyLogicTemplatesByCondition(dto);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void pagingGetLogicTemplatesByConditionTest() {
        IndexTemplateLogicDTO dto = new IndexTemplateLogicDTO();
        dto.setName("arius");
        List<IndexTemplateLogic> ret = templateLogicService.pagingGetLogicTemplatesByCondition(dto);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void fuzzyLogicTemplatesHitByConditionTest() {
        IndexTemplateLogicDTO dto = new IndexTemplateLogicDTO();
        dto.setName("arius");
        List<IndexTemplateLogic> ret = templateLogicService.fuzzyLogicTemplatesByCondition(dto);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getLogicTemplateByNameTest() {
        String templateName = "arius.dsl.template";
        List<IndexTemplateLogic> logic = templateLogicService.getLogicTemplateByName(templateName);
        Assertions.assertFalse(logic.isEmpty());
    }

    @Test
    void getLogicTemplateByIdTest() {
        Integer logicId = 1105;
        IndexTemplateLogic logic = templateLogicService.getLogicTemplateById(logicId);
        Assertions.assertNotNull(logic);
    }

    @Test
    void getTemplateConfigTest() {
        Integer logicTemplateId = null;
        IndexTemplateConfig templateConfig = templateLogicService.getTemplateConfig(logicTemplateId);
        Assertions.assertNull(templateConfig);
        logicTemplateId = 1105;
        templateConfig = templateLogicService.getTemplateConfig(logicTemplateId);
        Assertions.assertNotNull(templateConfig);
    }

    @Test
    void existTest() {
        int logicTemplateId = 9999999;
        boolean exist = templateLogicService.exist(logicTemplateId);
        Assertions.assertFalse(exist);
        logicTemplateId = 1105;
        exist = templateLogicService.exist(logicTemplateId);
        Assertions.assertTrue(exist);
    }

    @Test
    void getAllLogicTemplatesTest() {
        List<IndexTemplateLogic> ret = templateLogicService.getAllLogicTemplates();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getAllLogicTemplatesMapTest() {
        Map<Integer, IndexTemplateLogic> map = templateLogicService.getAllLogicTemplatesMap();
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    void getLogicTemplatesByIdsTest() {
        List<Integer> logicTemplateIds = new ArrayList<>();
        List<IndexTemplateLogic> ret = templateLogicService.getLogicTemplatesByIds(logicTemplateIds);
        Assertions.assertTrue(ret.isEmpty());
        // 包含不存在的模版id
        logicTemplateIds.add(1105);
        logicTemplateIds.add(9999999);
        ret = templateLogicService.getLogicTemplatesByIds(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getLogicTemplatesMapByIdsTest() {
        List<Integer> logicTemplateIds = new ArrayList<>();
        Map<Integer, IndexTemplateLogic> map = templateLogicService.getLogicTemplatesMapByIds(logicTemplateIds);
        Assertions.assertTrue(map.isEmpty());
        // 包含不存在的模版id
        logicTemplateIds.add(1105);
        logicTemplateIds.add(9999999);
        map = templateLogicService.getLogicTemplatesMapByIds(logicTemplateIds);
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    void getAppLogicTemplatesByAppIdTest() {
        // 存在的appid
        int appid = 1;
        List<IndexTemplateLogic> ret = templateLogicService.getAppLogicTemplatesByAppId(appid);
        Assertions.assertFalse(ret.isEmpty());
        // 不存在的appid
        appid = 999;
        ret = templateLogicService.getAppLogicTemplatesByAppId(appid);
        Assertions.assertTrue(ret.isEmpty());
    }

    @Test
    void getLogicClusterTemplatesTest() {
        // 存在的逻辑集群
        long logicClusterId = 63L;
        List<IndexTemplateLogic> ret = templateLogicService.getLogicClusterTemplates(logicClusterId);
        Assertions.assertFalse(ret.isEmpty());
        // 不存在的逻辑集群
        logicClusterId = 9999999L;
        ret = templateLogicService.getLogicClusterTemplates(logicClusterId);
        Assertions.assertTrue(ret.isEmpty());
    }

    @Test
    void getLogicTemplatesByAppIdTest() {
        // 存在的appid
        int appid = 1;
        Result<List<Tuple<String, String>>> ret = templateLogicService.getLogicTemplatesByAppId(appid);
        Assertions.assertFalse(ret.getData().isEmpty());
        // 不存在的appid
        appid = 999;
        ret = templateLogicService.getLogicTemplatesByAppId(appid);
        Assertions.assertTrue(ret.getData().isEmpty());
    }

    @Test
    void getAllLogicTemplatesPhysicalCountTest() {
        Map<Integer, Integer> ret = templateLogicService.getAllLogicTemplatesPhysicalCount();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getLogicTemplateTypesTest() {
        int logicId = 1105;
        List<IndexTemplateType> ret = templateLogicService.getLogicTemplateTypes(logicId);
        Assertions.assertFalse(ret.isEmpty());
        logicId = 9999999;
        ret = templateLogicService.getLogicTemplateTypes(logicId);
        Assertions.assertTrue(ret.isEmpty());
    }

    @Test
    void getTemplateByResponsibleIdTest() {
        Long responsibleId = 1L;
        List<IndexTemplateLogic> ret = templateLogicService.getTemplateByResponsibleId(responsibleId);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getTemplatesByHasAuthClusterTest() {
        Integer appid = 1;
        List<IndexTemplateLogic> ret = templateLogicService.getTemplatesByHasAuthCluster(appid);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getHasAuthTemplatesInLogicClusterTest() {
        Integer appid = 1;
        Long logicClusterId = 1105L;
        List<IndexTemplateLogic> ret = templateLogicService.getHasAuthTemplatesInLogicCluster(appid, logicClusterId);
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getLogicTemplatesWithClusterAndMasterTemplateTest() {
        List<IndexTemplateLogicWithClusterAndMasterTemplate> ret = templateLogicService.getLogicTemplatesWithClusterAndMasterTemplate();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getLogicTemplateWithClusterAndMasterTemplateTest() {
        // 存在逻辑id
        Integer logicClusterId = 1105;
        IndexTemplateLogicWithClusterAndMasterTemplate ret = templateLogicService.getLogicTemplateWithClusterAndMasterTemplate(logicClusterId);
        Assertions.assertNotNull(ret);
        logicClusterId = 9999999;
        ret = templateLogicService.getLogicTemplateWithClusterAndMasterTemplate(logicClusterId);
        Assertions.assertNull(ret);
    }

    @Test
    void getLogicTemplatesWithClusterAndMasterTemplateMapTest() {
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1105);
        Map<Integer, IndexTemplateLogicWithClusterAndMasterTemplate> ret = templateLogicService.getLogicTemplatesWithClusterAndMasterTemplateMap(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
        // 包含不存在的id
        logicTemplateIds.add(9999999);
        ret = templateLogicService.getLogicTemplatesWithClusterAndMasterTemplateMap(logicTemplateIds);
        Assertions.assertEquals(1, ret.size());
    }

    @Test
    void getLogicTemplateWithClusterAndMasterTemplateByClustersTest() {
        Set<Long> logicClusterIds = new HashSet<>();
        logicClusterIds.add(63L);
        List<IndexTemplateLogicWithClusterAndMasterTemplate> ret = templateLogicService.getLogicTemplateWithClusterAndMasterTemplateByClusters(logicClusterIds);
        Assertions.assertFalse(ret.isEmpty());
        // 包含不存在的id
        logicClusterIds.add(9999999L);
        ret = templateLogicService.getLogicTemplateWithClusterAndMasterTemplateByClusters(logicClusterIds);
        Assertions.assertEquals(1, ret.size());
    }

    @Test
    void getLogicTemplateWithClusterAndMasterTemplateByClusterTest() {
        long logicClusterId = 63L;
        List<IndexTemplateLogicWithClusterAndMasterTemplate> ret = templateLogicService.getLogicTemplateWithClusterAndMasterTemplateByCluster(logicClusterId);
        Assertions.assertNotNull(ret);
        logicClusterId = 9999999L;
        ret = templateLogicService.getLogicTemplateWithClusterAndMasterTemplateByCluster(logicClusterId);
        Assertions.assertNull(ret);
    }

    @Test
    void getLogicTemplateWithClusterTest() {
        int logicTemplateId = 1105;
        IndexTemplateLogicWithCluster ret = templateLogicService.getLogicTemplateWithCluster(logicTemplateId);
        Assertions.assertNotNull(ret);
        logicTemplateId = 999;
        ret = templateLogicService.getLogicTemplateWithCluster(logicTemplateId);
        Assertions.assertNull(ret);
    }

    @Test
    void getLogicTemplateWithClustersTest() {
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1105);
        List<IndexTemplateLogicWithCluster> ret = templateLogicService.getLogicTemplateWithClusters(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
        logicTemplateIds.add(9999999);
        ret = templateLogicService.getLogicTemplateWithClusters(logicTemplateIds);
        Assertions.assertEquals(1, ret.size());
    }

    @Test
    void getAllLogicTemplateWithClustersTest() {
        List<IndexTemplateLogicWithPhyTemplates> ret = templateLogicService.getAllLogicTemplateWithPhysicals();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getLogicTemplateWithClustersByClusterIdTest() {
        long logicClusterId = 63L;
        List<IndexTemplateLogicWithCluster> ret = templateLogicService.getLogicTemplateWithClustersByClusterId(logicClusterId);
        Assertions.assertFalse(ret.isEmpty());
        logicClusterId = 999L;
        ret = templateLogicService.getLogicTemplateWithClustersByClusterId(logicClusterId);
        Assertions.assertTrue(ret.isEmpty());
    }

    @Test
    void getAllLogicTemplateWithPhysicalsTest() {
        List<IndexTemplateLogicWithPhyTemplates> ret = templateLogicService.getAllLogicTemplateWithPhysicals();
        Assertions.assertFalse(ret.isEmpty());
    }

    @Test
    void getLogicTemplateWithPhysicalsByIdsTest() {
        Set<Integer> logicTemplateIds = new HashSet<>();
        logicTemplateIds.add(1105);
        List<IndexTemplateLogicWithPhyTemplates> ret = templateLogicService.getLogicTemplateWithPhysicalsByIds(logicTemplateIds);
        Assertions.assertFalse(ret.isEmpty());
        // 包含不存在的id
        logicTemplateIds.add(9999999);
        ret = templateLogicService.getLogicTemplateWithPhysicalsByIds(logicTemplateIds);
        Assertions.assertEquals(1, ret.size());
    }

    @Test
    void getLogicTemplateWithPhysicalsByIdTest() {
        int logicTemplateId = 1105;
        IndexTemplateLogicWithPhyTemplates ret = templateLogicService.getLogicTemplateWithPhysicalsById(logicTemplateId);
        Assertions.assertNotNull(ret);
        logicTemplateId = 9999999;
        ret = templateLogicService.getLogicTemplateWithPhysicalsById(logicTemplateId);
        Assertions.assertNull(ret);
    }

    @Test
    void getTemplateWithPhysicalByDataCenterTest() {
        String dc = "cn";
        List<IndexTemplateLogicWithPhyTemplates> ret = templateLogicService.getTemplateWithPhysicalByDataCenter(dc);
        Assertions.assertFalse(ret.isEmpty());
        dc = "kk";
        ret = templateLogicService.getTemplateWithPhysicalByDataCenter(dc);
        Assertions.assertTrue(ret.isEmpty());
    }
}
