package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class TemplateLogicAliasServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private TemplateLogicAliasService templateLogicAliasService;

    @Test
    void getAliasesByIdTest() {
        // 存在别名的logicId
        Integer logicId = 1105;
        List<String> result = templateLogicAliasService.getAliasesById(logicId);
        Assertions.assertFalse(result.isEmpty());
        // 不存在的logicId
        logicId = 99999;
        result = templateLogicAliasService.getAliasesById(logicId);
        Assertions.assertTrue(result.isEmpty());
        // 空的logicId
        logicId = null;
        result = templateLogicAliasService.getAliasesById(logicId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getAliasesByIdFromCacheTest() {
        // 存在的logicId
        Integer logicId = 1105;
        List<String> result = templateLogicAliasService.getAliasesByIdFromCache(logicId);
        Assertions.assertFalse(result.isEmpty());
        // 不存在的logicId
        logicId = 99999;
        result = templateLogicAliasService.getAliasesByIdFromCache(logicId);
        Assertions.assertTrue(result.isEmpty());
        // 空的logicId
        logicId = null;
        result = templateLogicAliasService.getAliasesByIdFromCache(logicId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void listAliasTest() {
        List<IndexTemplateAlias> indexTemplateAliases = templateLogicAliasService.listAlias();
        Assertions.assertFalse(indexTemplateAliases.isEmpty());
    }

    @Test
    void addAliasTest() {
        // 空的
        IndexTemplateAliasDTO dto = null;
        Result<Boolean> ret = templateLogicAliasService.addAlias(dto);
        Assertions.assertFalse(ret.getData());
        // 不存在的logicId
        dto = new IndexTemplateAliasDTO();
        dto.setLogicId(99999);
        dto.setName("testtest");
        ret = templateLogicAliasService.addAlias(dto);
        Assertions.assertFalse(ret.getData());
        // 正常的
        dto.setLogicId(1105);
        dto.setName("testtest");
        ret = templateLogicAliasService.addAlias(dto);
        Assertions.assertTrue(ret.getData());
    }

    @Test
    void delAlias() {
        IndexTemplateAliasDTO dto = null;
        Result<Boolean> ret = templateLogicAliasService.delAlias(dto);
        // 不存在的logicId
        dto = new IndexTemplateAliasDTO();
        dto.setLogicId(99999);
        dto.setName("testtest");
        ret = templateLogicAliasService.delAlias(dto);
        Assertions.assertFalse(ret.getData());
        // 正常的
        dto.setLogicId(1105);
        dto.setName("testtest");
        ret = templateLogicAliasService.delAlias(dto);
        Assertions.assertTrue(ret.getData());
    }

    @Test
    void listAliasMap() {
        Map<Integer, List<String>> map = templateLogicAliasService.listAliasMap();
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    void listAliasMapWithCache() {
        Map<Integer, List<String>> map = templateLogicAliasService.listAliasMapWithCache();
        Assertions.assertFalse(map.isEmpty());
    }
}
