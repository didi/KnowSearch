package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexShardInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class ESIndexCatServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private ESIndexCatService esIndexCatService;

    @Test
    void syncGetCatIndexInfo() {
        String clusterName = "cjm_6.6.2_test";
        List<String> clusterNameList = new ArrayList<>();
        clusterNameList.add(clusterName);
        String indexName = "keep_test_1";
        String health = null;
        Tuple<Long, List<IndexCatCell>> longListTuple = esIndexCatService.syncGetCatIndexInfo(clusterNameList, indexName, health, 0L, 10L, null, null);
        Assertions.assertNotNull(longListTuple);
        // 不存在的索引
        longListTuple = esIndexCatService.syncGetCatIndexInfo(clusterNameList, "testtest", health, 0L, 10L, null, null);
        Assertions.assertNull(longListTuple);
        // 不存在的集群
        clusterNameList.add("testtest");
        longListTuple = esIndexCatService.syncGetCatIndexInfo(clusterNameList, indexName, health, 0L, 10L, null, null);
        Assertions.assertNotNull(longListTuple);
    }

    @Test
    void syncUpdateCatIndexDeleteFlag() {
        String clusterName = "cjm_6.6.2_test";
        List<String> indexNameList = new ArrayList<>();
        indexNameList.add("keep_test_1");
        int num = esIndexCatService.syncUpdateCatIndexDeleteFlag(clusterName, indexNameList, 1);
        Assertions.assertEquals(1, num);
        // 不存在的集群
        num = esIndexCatService.syncUpdateCatIndexDeleteFlag("testtest", indexNameList, 1);
        Assertions.assertEquals(0, num);
        // 包含不存在的索引
        indexNameList.add("testtest");
        num = esIndexCatService.syncUpdateCatIndexDeleteFlag(clusterName, indexNameList, 1);
        Assertions.assertEquals(1, num);
    }

    @Test
    void syncGetIndexShardInfo() {
        String clusterName = "cjm_6.6.2_test";
        String indexName = "keep_test_1";
        List<IndexShardInfo> indexShardInfos = esIndexCatService.syncGetIndexShardInfo(clusterName, indexName);
        Assertions.assertFalse(indexShardInfos.isEmpty());
        // 不存在的集群
        indexShardInfos  = esIndexCatService.syncGetIndexShardInfo("testtest", indexName);
        Assertions.assertTrue(indexShardInfos.isEmpty());
        // 不存在的索引
        indexShardInfos = esIndexCatService.syncGetIndexShardInfo(clusterName, "testets");
        Assertions.assertTrue(indexShardInfos.isEmpty());
    }
}
