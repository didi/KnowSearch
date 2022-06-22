package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexShardInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexCatCellPO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index.IndexCatESDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class ESIndexCatServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESIndexCatService esIndexCatService;

    @MockBean
    private IndexCatESDAO indexCatESDAO;

    @Test
    public void syncGetCatIndexInfo() {
    }

    @Test
    public void syncUpdateCatIndexDeleteFlag() {
        Assertions.assertEquals(0, esIndexCatService.syncUpdateCatIndexDeleteFlag(CustomDataSource.PHY_CLUSTER_NAME, new ArrayList<>(), 1));
        Mockito.when(indexCatESDAO.batchUpdateCatIndexDeleteFlag(Mockito.any(), Mockito.any(), Mockito.eq(1))).thenReturn(true);
        List<String> indexNameList = new ArrayList<>();
        indexNameList.add("test");
        Assertions.assertEquals(indexNameList.size(), esIndexCatService.syncUpdateCatIndexDeleteFlag(CustomDataSource.PHY_CLUSTER_NAME, indexNameList, 1));
    }

    @Test
    public void syncGetIndexShardInfo() {
        DirectResponse directResponse = new DirectResponse();
        IndexShardInfo indexShardInfo = new IndexShardInfo();
        indexShardInfo.setIndex("test");
        directResponse.setRestStatus(RestStatus.OK);
        List<IndexShardInfo> list = new ArrayList<>();
        list.add(indexShardInfo);
        directResponse.setResponseContent(JSON.toJSONString(list));
        Mockito.when(indexCatESDAO.getDirectResponse(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(directResponse);
        Assertions.assertFalse(esIndexCatService.syncGetIndexShardInfo(CustomDataSource.PHY_CLUSTER_NAME, "test").isEmpty());
    }
}
