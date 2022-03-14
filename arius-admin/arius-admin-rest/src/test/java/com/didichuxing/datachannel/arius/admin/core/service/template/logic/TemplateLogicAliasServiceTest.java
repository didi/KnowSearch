package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateAliasDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class TemplateLogicAliasServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private TemplateLogicAliasService templateLogicAliasService;

    @MockBean
    private IndexTemplateAliasDAO indexTemplateAliasDAO;

    @Test
    void getAliasesByIdTest() {
        // 存在别名的logicId
        Mockito.when(indexTemplateAliasDAO.listByTemplateId(1)).thenReturn(CustomDataSource.getTemplateAliasPOList());
        List<String> result = templateLogicAliasService.getAliasesById(1);
        Assertions.assertEquals(result.size(), CustomDataSource.SIZE);
        // 不存在的logicId
        result = templateLogicAliasService.getAliasesById(-1);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getAliasesByIdFromCacheTest() {
        // 存在的logicId
        Mockito.when(indexTemplateAliasDAO.listByTemplateId(1)).thenReturn(CustomDataSource.getTemplateAliasPOList());
        List<String> result = templateLogicAliasService.getAliasesByIdFromCache(1);
        Assertions.assertEquals(result.size(), CustomDataSource.SIZE);
        // 不存在的logicId
        result = templateLogicAliasService.getAliasesByIdFromCache(-1);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void listAliasTest() {
        Mockito.when(indexTemplateAliasDAO.listAll()).thenReturn(CustomDataSource.getTemplateAliasPOList());
        List<IndexTemplateAlias> indexTemplateAliases = templateLogicAliasService.listAlias();
        Assertions.assertEquals(indexTemplateAliases.size(), CustomDataSource.SIZE);
    }

    @Test
    void addAliasTest() {
        Mockito.when(indexTemplateAliasDAO.insert(Mockito.any())).thenReturn(1);
        // 空的
        IndexTemplateAliasDTO dto = CustomDataSource.indexTemplateAliasDTOFactory();
        // 别名为空
        dto.setName(null);
        Result<Boolean> result = templateLogicAliasService.addAlias(dto);
        Assertions.assertFalse(result.failed());
        // 别名长度非法
        dto.setName("testtesttesttesttesttesttesttesttesttesttesttest");
        result = templateLogicAliasService.addAlias(dto);
        Assertions.assertFalse(result.failed());
        // 别名包含特殊字母
        dto.setName("<>}{}>");
        result = templateLogicAliasService.addAlias(dto);
        Assertions.assertFalse(result.failed());
        // 正常的
        dto.setName("test");
        result = templateLogicAliasService.addAlias(dto);
        Assertions.assertTrue(result.success());
    }

    @Test
    void delAlias() {
        Mockito.when(indexTemplateAliasDAO.delete(Mockito.anyInt(), Mockito.anyString())).thenReturn(1);
        IndexTemplateAliasDTO dto = CustomDataSource.indexTemplateAliasDTOFactory();
        Result<Boolean> ret = templateLogicAliasService.delAlias(dto);
        Assertions.assertTrue(ret.getData());
    }

    @Test
    void listAliasMap() {
        Mockito.when(indexTemplateAliasDAO.listAll()).thenReturn(CustomDataSource.getTemplateAliasPOList());
        Map<Integer, List<String>> map = templateLogicAliasService.listAliasMap();
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    void listAliasMapWithCache() {
        Mockito.when(indexTemplateAliasDAO.listAll()).thenReturn(CustomDataSource.getTemplateAliasPOList());
        Map<Integer, List<String>> map = templateLogicAliasService.listAliasMapWithCache();
        Assertions.assertFalse(map.isEmpty());
    }
}
