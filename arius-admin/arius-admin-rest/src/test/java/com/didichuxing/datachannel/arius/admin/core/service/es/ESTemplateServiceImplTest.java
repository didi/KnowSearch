package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESTemplateDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.TemplateConfig;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Transactional
@Rollback
public class ESTemplateServiceImplTest extends AriusAdminApplicationTest {

    @Autowired
    private ESTemplateService esTemplateService;

    @MockBean
    private ESTemplateDAO esTemplateDAO;

    @Test
    public void syncCreateTest() throws ESOperateException {
        Mockito.when(esTemplateDAO.create(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);
        Assertions.assertTrue(esTemplateService
                .syncCreate(CustomDataSource.PHY_CLUSTER_NAME, "test", "test", "test", 1, 1, 1));

    }

    @Test
    public void syncDeleteTest() throws ESOperateException {
        Mockito.when(esTemplateDAO.delete(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Assertions.assertTrue(esTemplateService.syncDelete(CustomDataSource.PHY_CLUSTER_NAME, "test", 1));
    }

    @Test
    public void syncUpdateRackAndShardTest() throws ESOperateException {
        Mockito.when(esTemplateDAO.updateRackAndShard(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esTemplateService.syncUpdateRackAndShard(CustomDataSource.PHY_CLUSTER_NAME, "test", "test", 1, 1, 1));
    }

    @Test
    public void syncUpdateExpressionTest() throws ESOperateException {
        Mockito.when(esTemplateDAO.updateExpression(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esTemplateService.syncUpdateExpression(CustomDataSource.PHY_CLUSTER_NAME, "test", "test", 1));
    }

    @Test
    public void syncUpdateShardNumTest() throws ESOperateException {
        Mockito.when(esTemplateDAO.updateShardNum(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esTemplateService.syncUpdateShardNum(CustomDataSource.PHY_CLUSTER_NAME, "test", 1, 1));
    }

    @Test
    public void syncUpsertSettingTest() throws ESOperateException {
        Mockito.when(esTemplateDAO.upsertSetting(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esTemplateService.syncUpsertSetting(CustomDataSource.PHY_CLUSTER_NAME, "test", new HashMap<>(), 1));
    }

    @Test
    public void syncCopyMappingAndAliasTest() throws ESOperateException {
        Mockito.when(esTemplateDAO.copyMappingAndAlias(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esTemplateService.syncCopyMappingAndAlias(CustomDataSource.PHY_CLUSTER_NAME, "test", "test", "test", 1));
    }

    @Test
    public void syncUpdateTemplateConfigTest() throws ESOperateException {
        Mockito.when(esTemplateDAO.copyMappingAndAlias(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esTemplateService.syncCopyMappingAndAlias(CustomDataSource.PHY_CLUSTER_NAME, "test", "test", "test", 1));
    }

    @Test
    public void syncGetTemplateConfigTest() {
        Assertions.assertNull(esTemplateService.syncGetTemplateConfig(CustomDataSource.PHY_CLUSTER_NAME, null));
        Assertions.assertNull(esTemplateService.syncGetTemplateConfig(null, "test"));
        Mockito.when(esTemplateDAO.getTemplate(Mockito.anyString(), Mockito.any())).thenReturn(new TemplateConfig());
        Assertions.assertNotNull(esTemplateService.syncGetTemplateConfig(CustomDataSource.PHY_CLUSTER_NAME, "test"));
    }

    @Test
    public void syncGetMappingsByClusterNameTest() {
        Mockito.when(esTemplateDAO.getTemplate(Mockito.anyString(), Mockito.any())).thenReturn(new TemplateConfig());
        Assertions.assertNotNull(esTemplateService.syncGetTemplateConfig(CustomDataSource.PHY_CLUSTER_NAME, "test"));
    }

    @Test
    public void syncGetTemplatesTest() {
        Mockito.when(esTemplateDAO.getTemplates(Mockito.anyString(), Mockito.any())).thenReturn(null);
        Assertions.assertNull(esTemplateService.syncGetTemplates(CustomDataSource.PHY_CLUSTER_NAME, "test"));
    }

    @Test
    public void syncGetAllTemplatesTest() {
        Mockito.when(esTemplateDAO.getAllTemplate(Mockito.any())).thenReturn(new HashMap<>());
        List<String> list = new ArrayList<>();
        list.add(CustomDataSource.PHY_CLUSTER_NAME);
        Assertions.assertNotNull(esTemplateService.syncGetAllTemplates(list));
    }

    @Test
    public void syncUpdateNameTest() throws ESOperateException {
        Mockito.when(esTemplateDAO.getTemplate(Mockito.anyString(), Mockito.any())).thenReturn(null);
        Assertions.assertFalse(esTemplateService.syncUpdateName(CustomDataSource.PHY_CLUSTER_NAME, "test", "test", 1));
        Mockito.when(esTemplateDAO.getTemplate(CustomDataSource.PHY_CLUSTER_NAME, "test")).thenReturn(new TemplateConfig());
        Mockito.when(esTemplateDAO.create(CustomDataSource.PHY_CLUSTER_NAME, "test", "test", "test", 1, 1)).thenReturn(true);
        Assertions.assertFalse(esTemplateService.syncUpdateName(CustomDataSource.PHY_CLUSTER_NAME, "test", "test", 1));
    }

    @Test
    public void syncCheckTemplateConfigTest() throws ESOperateException {
        Mockito.when(esTemplateDAO.create(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(esTemplateDAO.delete(Mockito.anyString(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esTemplateService.syncCheckTemplateConfig(CustomDataSource.PHY_CLUSTER_NAME, "test", new TemplateConfig(), 1));
    }

    @Test
    public void syncGetTemplateNumTest() {
        Mockito.when(esTemplateDAO.getTemplate(Mockito.anyString(), Mockito.any())).thenReturn(new TemplateConfig());
        DirectResponse directResponse = new DirectResponse();
        directResponse.setRestStatus(RestStatus.OK);
        List<IndexTemplatePO> list = CustomDataSource.getTemplateLogicPOList();
        directResponse.setResponseContent(JSON.toJSONString(list));
        Mockito.when(esTemplateDAO.getDirectResponse(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(directResponse);
        Assertions.assertEquals(esTemplateService.syncGetTemplateNum(CustomDataSource.PHY_CLUSTER_NAME), CustomDataSource.SIZE);
    }

    @Test
    public void synGetTemplateNumForAllVersionTest() {
        Mockito.when(esTemplateDAO.getAllTemplate(Mockito.any())).thenReturn(new HashMap<>());
        Assertions.assertEquals(0, esTemplateService.synGetTemplateNumForAllVersion(CustomDataSource.PHY_CLUSTER_NAME));
    }

}
