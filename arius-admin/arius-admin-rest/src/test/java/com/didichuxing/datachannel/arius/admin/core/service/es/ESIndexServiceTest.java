package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESIndexDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.AliasIndexNode;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.elasticsearch.client.response.model.indices.CommonStat;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.TypeConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
@Rollback
public class ESIndexServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESIndexService esIndexService;

    @MockBean
    private ESIndexDAO esIndexDAO;

    @Test
    public void syncCreateIndexTest() throws ESOperateException {
        Mockito.when(esIndexDAO.createIndex(CustomDataSource.PHY_CLUSTER_NAME, "test")).thenReturn(true);
        Assertions.assertTrue(esIndexService.syncCreateIndex(CustomDataSource.PHY_CLUSTER_NAME, "test", 1));
    }

    @Test
    public void syncDelIndexTest() throws ESOperateException {
        Mockito.when(esIndexDAO.deleteIndex(CustomDataSource.PHY_CLUSTER_NAME, "test")).thenReturn(true);
        Assertions.assertTrue(esIndexService.syncDelIndex(CustomDataSource.PHY_CLUSTER_NAME, "test", 1));
    }

    @Test
    public void syncDeleteIndexByExpressionTest() throws ESOperateException {
        Mockito.when(esIndexDAO.deleteIndex(CustomDataSource.PHY_CLUSTER_NAME, "test")).thenReturn(true);
        Assertions.assertTrue(esIndexService.syncDeleteIndexByExpression(CustomDataSource.PHY_CLUSTER_NAME, "test", 1));
    }

    @Test
    public void syncGetIndexMappingTest() {
        Mockito.when(esIndexDAO.getIndexMapping(CustomDataSource.PHY_CLUSTER_NAME, "test1")).thenReturn(null);
        Assertions.assertTrue(esIndexService.syncGetIndexMapping(CustomDataSource.PHY_CLUSTER_NAME, "test1").isEmpty());

        MappingConfig mappingConfig = new MappingConfig();
        mappingConfig.getMapping().put("test", new TypeConfig());
        Mockito.when(esIndexDAO.getIndexMapping(CustomDataSource.PHY_CLUSTER_NAME, "test2")).thenReturn(mappingConfig);
        Assertions.assertFalse(esIndexService.syncGetIndexMapping(CustomDataSource.PHY_CLUSTER_NAME, "test2").isEmpty());
    }

    @Test
    public void syncGetIndexNameByExpressionTest() {
        Mockito.when(esIndexDAO.getIndexByExpression(CustomDataSource.PHY_CLUSTER_NAME, "test1")).thenReturn(null);
        Assertions.assertTrue(esIndexService.syncGetIndexNameByExpression(CustomDataSource.PHY_CLUSTER_NAME, "test1").isEmpty());
        Map<String, IndexNodes> map = new HashMap<>();
        map.put("node1", new IndexNodes());
        map.put("node2", new IndexNodes());
        Mockito.when(esIndexDAO.getIndexByExpression(CustomDataSource.PHY_CLUSTER_NAME, "test2")).thenReturn(map);
        Assertions.assertFalse(esIndexService.syncGetIndexNameByExpression(CustomDataSource.PHY_CLUSTER_NAME, "test2").isEmpty());
    }

    @Test
    public void syncPutIndexSettingTest() throws ESOperateException {
        Mockito.when(esIndexDAO.putIndexSetting(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        List<String> indices = new ArrayList<>();
        indices.add("test");
        Assertions.assertTrue(esIndexService.syncPutIndexSetting(CustomDataSource.PHY_CLUSTER_NAME, indices, "", "1", "1", 1));
    }

    @Test
    public void syncGetIndexByExpressionTest() {
        Map<String, IndexNodes> map = new HashMap<>();
        map.put("node1", new IndexNodes());
        map.put("node2", new IndexNodes());
        Mockito.when(esIndexDAO.getIndexByExpression(Mockito.any(), Mockito.any())).thenReturn(map);
        // 不存在的expression
        Assertions.assertFalse(esIndexService.syncGetIndexByExpression(CustomDataSource.PHY_CLUSTER_NAME, "test").isEmpty());
    }

    @Test
    public void syncBatchGetIndicesTest() {
        Map<String, IndexNodes> map = new HashMap<>();
        map.put("node1", new IndexNodes());
        map.put("node2", new IndexNodes());
        List<String> indices = new ArrayList<>();
        indices.add("test1");
        indices.add("test2");
        Mockito.when(esIndexDAO.getIndexStatsWithShards(Mockito.any(), Mockito.any())).thenReturn(map);
        Assertions.assertFalse(esIndexService.syncBatchGetIndices(CustomDataSource.PHY_CLUSTER_NAME, indices).isEmpty());
    }

    @Test
    public void syncGetIndexAliasesByExpressionTest() {
        Map<String, JSONObject> aliases = new HashMap<>();
        aliases.put("test1", new JSONObject());
        aliases.put("test2", new JSONObject());
        Map<String, AliasIndexNode> map = new HashMap<>();
        AliasIndexNode aliasIndexNode1 = new AliasIndexNode();
        aliasIndexNode1.setAliases(aliases);
        AliasIndexNode aliasIndexNode2 = new AliasIndexNode();
        aliasIndexNode2.setAliases(aliases);
        map.put("node1", aliasIndexNode1);
        map.put("node2", aliasIndexNode2);
        Mockito.when(esIndexDAO.getAliasesByExpression(Mockito.any(), Mockito.eq("test"))).thenReturn(new HashMap<>());
        Assertions.assertTrue(esIndexService.syncGetIndexAliasesByExpression(CustomDataSource.PHY_CLUSTER_NAME, "test").isEmpty());
        Mockito.when(esIndexDAO.getAliasesByExpression(Mockito.any(), Mockito.eq("test1"))).thenReturn(map);
        Assertions.assertFalse(esIndexService.syncGetIndexAliasesByExpression(CustomDataSource.PHY_CLUSTER_NAME, "test1").isEmpty());
    }

    @Test
    public void syncBatchDeleteIndicesTest() {
        List<String> indices = new ArrayList<>();
        Assertions.assertEquals(0, esIndexService.syncBatchDeleteIndices(CustomDataSource.PHY_CLUSTER_NAME, indices, 1));
        indices.add("test1");
        indices.add("test2");
        Mockito.when(esIndexDAO.deleteIndex(Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertEquals(indices.size(), esIndexService.syncBatchDeleteIndices(CustomDataSource.PHY_CLUSTER_NAME, indices, 1));
    }

    @Test
    public void syncDeleteByQueryTest() throws ESOperateException {
        Mockito.when(esIndexDAO.deleteByQuery(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        List<String> indices = new ArrayList<>();
        indices.add("test1");
        indices.add("test2");
        Assertions.assertTrue(esIndexService.syncDeleteByQuery(CustomDataSource.PHY_CLUSTER_NAME, indices, ""));
    }

    @Test
    public void syncBatchBlockIndexWriteTest() throws ESOperateException {
        Mockito.when(esIndexDAO.blockIndexWrite(Mockito.any(), Mockito.any(), Mockito.eq(true))).thenReturn(true);
        List<String> indices = new ArrayList<>();
        indices.add("test1");
        indices.add("test2");
        Assertions.assertTrue(esIndexService.syncBatchBlockIndexWrite(CustomDataSource.PHY_CLUSTER_NAME, indices, true, 1));
    }

    @Test
    public void reOpenIndexTest() throws ESOperateException {
        List<String> indices = new ArrayList<>();
        indices.add("test");
        Mockito.when(esIndexDAO.closeIndex(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(esIndexDAO.openIndex(Mockito.any(), Mockito.any())).thenReturn(true);
        Assertions.assertTrue(esIndexService.reOpenIndex(CustomDataSource.PHY_CLUSTER_NAME, indices, 1));
    }

    @Test
    public void syncCatIndexByExpressionTest() {
        // esIndexDAO.catIndexByExpression
        List<CatIndexResult> list = new ArrayList<>();
        list.add(new CatIndexResult());
        Mockito.when(esIndexDAO.catIndexByExpression(Mockito.any(), Mockito.any())).thenReturn(list);
        Assertions.assertFalse(esIndexService.syncCatIndexByExpression(CustomDataSource.PHY_CLUSTER_NAME, "test").isEmpty());
    }

    @Test
    public void syncGetIndexConfigsTest() throws Exception {
        Mockito.when(esIndexDAO.getIndexConfigs(Mockito.any(), Mockito.any())).thenReturn(new MultiIndexsConfig(new JSONObject()));
        Assertions.assertNotNull(esIndexService.syncGetIndexConfigs(CustomDataSource.PHY_CLUSTER_NAME, "test"));
    }

    @Test
    public void syncGetIndexPrimaryShardNumberTest() throws Exception {
        Map<String, String> settings = new HashMap<>();
        settings.put("index.number_of_shards", "2");
        IndexConfig indexConfig = new IndexConfig();
        indexConfig.setSettings(settings);
        MultiIndexsConfig multiIndexsConfig = new MultiIndexsConfig(new JSONObject());
        multiIndexsConfig.getIndexConfigMap().put("test1", indexConfig);
        Mockito.when(esIndexDAO.getIndexConfigs(Mockito.any(), Mockito.any())).thenReturn(multiIndexsConfig);
        Assertions.assertNotNull(esIndexService.syncGetIndexPrimaryShardNumber(CustomDataSource.PHY_CLUSTER_NAME, "test1"));
    }

    @Test
    public void  syncGetIndexNodesTest() {
        Map<String, IndexNodes> map = new HashMap<>();
        map.put("test1", new IndexNodes());
        map.put("test2", new IndexNodes());
        Mockito.when(esIndexDAO.getIndexNodes(Mockito.any(), Mockito.any())).thenReturn(map);
        Assertions.assertFalse(esIndexService.syncGetIndexNodes(CustomDataSource.PHY_CLUSTER_NAME, "test").isEmpty());
    }
}
