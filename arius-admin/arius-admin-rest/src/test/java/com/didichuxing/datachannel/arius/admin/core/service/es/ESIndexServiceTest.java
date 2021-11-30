package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESIndexDAO;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
@Rollback
public class ESIndexServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private ESIndexDAO esIndexDAO;

    @Autowired
    private ESIndexService esIndexService;

    @Test
    public void syncCreateIndexTest() throws ESOperateException {
        // 创建重复的
        boolean keep_test = esIndexService.syncCreateIndex("test_es_version_7.6.2_2", "keep_test", 1);
        boolean keep_test2 = esIndexService.syncCreateIndex("test_es_version_7.6.2_2", "keep_test_1", 1);
        boolean keep_test3 = esIndexService.syncCreateIndex("test_es_version_7.6.2_2", "keep_test_2", 1);
        Assertions.assertTrue(keep_test);
        Assertions.assertTrue(keep_test2);
        Assertions.assertTrue(keep_test3);
    }

    @Test
    public void syncDelIndexTest() throws ESOperateException {
        boolean keep_test = esIndexService.syncDelIndex("test_es_version_6.6.2_2", "keep_test", 1);
        Assertions.assertTrue(keep_test);
    }

    @Test
    public void syncDeleteIndexByExpressionTest() throws ESOperateException {
        boolean ret = esIndexService.syncDeleteIndexByExpression("test_es_version_6.6.2_2", "keep_test*", 1);
        Assertions.assertTrue(ret);
    }

    @Test
    public void syncGetIndexMappingTest() {
        // 获取存在的索引
        String keep_test = esIndexService.syncGetIndexMapping("test_es_version_6.6.2_2", "keep_test");
        Assertions.assertNotNull(keep_test);
        // 获取不存在的索引
        String keep_test2 = esIndexService.syncGetIndexMapping("test_es_version_6.6.2_2", "keep_test_99");
        Assertions.assertEquals(keep_test2, "");
    }

    @Test
    public void syncGetIndexNameByExpressionTest() {
        // 存在的表达式
        Set<String> strings = esIndexService.syncGetIndexNameByExpression("test_es_version_6.6.2_2", "keep_test*");
        Assertions.assertTrue(!strings.isEmpty());
        // 不存在的表达式
        Set<String> strings2 = esIndexService.syncGetIndexNameByExpression("test_es_version_6.6.2_2", "keep_kkk_test*");
        Assertions.assertTrue(strings2.isEmpty());
    }

    @Test
    public void syncPutIndexSettingTest() throws ESOperateException {
        List<String> indices = new ArrayList<>();
        indices.add("keep_test");
        // 存在的配置
        boolean ret = esIndexService.syncPutIndexSetting("test_es_version_6.6.2_2", indices, "index.number_of_replicas", "2", "1", 1);
        Assertions.assertTrue(ret);
        // 不存在的配置，会抛出异常
        try {
            esIndexService.syncPutIndexSetting("test_es_version_6.6.2_2", indices, "index.ggg.number_of_replicas", "3", "4", 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void syncGetIndexByExpressionTest() {
        // 存在的expression
        Map<String, IndexNodes> map = esIndexService.syncGetIndexByExpression("test_es_version_6.6.2_2", "keep_test*");
        Assertions.assertTrue(!map.isEmpty());
        // 不存在的expression
        Map<String, IndexNodes> map2 = esIndexService.syncGetIndexByExpression("test_es_version_6.6.2_2", "keep_kkk_test*");
        Assertions.assertTrue(map2.isEmpty());
    }

    @Test
    public void syncBatchGetIndicesTest() {
        // 存在的索引
        List<String> indices = new ArrayList<>();
        indices.add("keep_test");
        indices.add("keep_test_2");
        Map<String, IndexNodes> map = esIndexService.syncBatchGetIndices("test_es_version_6.6.2_2", indices);
        Assertions.assertFalse(map.isEmpty());
        // 不存在的索引
        List<String> indices2 = new ArrayList<>();
        indices2.add("keep_test_99");
        Map<String, IndexNodes> map2 = esIndexService.syncBatchGetIndices("test_es_version_6.6.2_2", indices2);
        Assertions.assertTrue(map2.isEmpty());
    }

    @Test
    public void syncGetIndexAliasesByExpressionTest() {
        List<Tuple<String, String>> tuples = esIndexService.syncGetIndexAliasesByExpression("test_es_version_6.6.2_2", "keep_test*");
        Assertions.assertFalse(tuples.isEmpty());
    }

    @Test
    public void syncBatchDeleteIndicesTest() {
        List<String> indices = new ArrayList<>();
        // 存在的
        indices.add("keep_test_1");
        // 不存在的
        indices.add("keep_test_9");
        int ret = esIndexService.syncBatchDeleteIndices("test_es_version_6.6.2_2", indices, 1);
        Assertions.assertTrue(ret > 0);
    }

    @Test
    public void syncDeleteByQueryTest() throws ESOperateException {
        List<String> indices = new ArrayList<>();
        // 存在的
        indices.add("keep_test_1");
        // 不存在的
        indices.add("keep_test_9");
        boolean ret = esIndexService.syncDeleteByQuery("test_es_version_6.6.2_2", indices, "{}");
        Assertions.assertTrue(ret);
    }

    @Test
    public void syncBatchUpdateRackTest() throws ESOperateException {
        List<String> indices = new ArrayList<>();
        indices.add("keep_test_1");
        indices.add("keep_test_9");
        try {
            boolean ret = esIndexService.syncBatchUpdateRack("test_es_version_6.6.2_2", indices, "rack2", 1);
            Assertions.assertTrue(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void syncBatchBlockIndexWriteTest() {
        List<String> indices = new ArrayList<>();
        indices.add("keep_test_1");
        indices.add("keep_test_9");
        try {
            boolean ret = esIndexService.syncBatchBlockIndexWrite("test_es_version_6.6.2_2", indices, false, 1);
            Assertions.assertTrue(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ensureDateSameTest() {
        List<String> indices = new ArrayList<>();
        indices.add("keep_test_1");
        // indices.add("keep_test_2");
        boolean ret = esIndexService.ensureDateSame("test_es_version_7.6.2_2", "test_es_version_6.6.2_2", indices);
        Assertions.assertTrue(ret);

    }

    @Test
    public void reOpenIndexTest() {
        List<String> indices = new ArrayList<>();
        indices.add("keep_test_1");
        try {
            boolean ret = esIndexService.reOpenIndex("test_es_version_6.6.2_2", indices, 1);
            Assertions.assertTrue(ret);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void syncCatIndexByExpressionTest() {
        List<CatIndexResult> catIndexResults = esIndexService.syncCatIndexByExpression("test_es_version_6.6.2_2", "keep_test*");
        Assertions.assertTrue(!catIndexResults.isEmpty());
        List<CatIndexResult> catIndexResults2 = esIndexService.syncCatIndexByExpression("test_es_version_6.6.2_2", "keep_test_kkk*");
        Assertions.assertTrue(catIndexResults2.isEmpty());
    }

    @Test
    public void syncGetIndexConfigsTest() {
        MultiIndexsConfig config = esIndexService.syncGetIndexConfigs("test_es_version_6.6.2_2", "keep_test_1");
        Assertions.assertTrue(config != null);
        MultiIndexsConfig config2 = esIndexService.syncGetIndexConfigs("test_es_version_6.6.2_2", "keep_test_99");
        Assertions.assertTrue(config2 == null);
    }

    @Test
    public void getIndexPrimaryShardNumberTest() {
        Integer num = esIndexService.syncGetIndexPrimaryShardNumber("test_es_version_6.6.2_2", "keep_test_1");
        Assertions.assertTrue(num != null);
        Integer num2 = esIndexService.syncGetIndexPrimaryShardNumber("test_es_version_6.6.2_2", "keep_test_99");
        Assertions.assertTrue(num2 == null);
    }

    @Test
    public void  getIndexNodesTest() {
        Map<String, IndexNodes> stringIndexNodesMap = esIndexService.syncGetIndexNodes("test_es_version_6.6.2_2", "keep_test*");
        Assertions.assertTrue(!stringIndexNodesMap.isEmpty());
        Map<String, IndexNodes> stringIndexNodesMap2 = esIndexService.syncGetIndexNodes("test_es_version_6.6.2_2", "keep_test99*");
        Assertions.assertTrue(stringIndexNodesMap2.isEmpty());
    }
}
