package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexResponse;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterNodeDAO;
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

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsConstant.ONE_BILLION;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class ESClusterNodeServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESClusterNodeService esClusterNodeService;

    @MockBean
    private ESClusterNodeDAO esClusterNodeDAO;

    @Test
    public void syncGetPendingTaskTest() {
        DirectResponse directResponse = new DirectResponse();
        directResponse.setRestStatus(RestStatus.OK);
        directResponse.setResponseContent("{\"tasks\": [{\"time_in_queue\": 10},{\"time_in_queue\": 11},{\"time_in_queue\": 12}]}");
        Mockito.when(esClusterNodeDAO.getDirectResponse(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(directResponse);
        Assertions.assertFalse(esClusterNodeService.syncGetPendingTask(CustomDataSource.PHY_CLUSTER_NAME).isEmpty());
    }

    @Test
    public void syncGetBigIndicesTest() {
        DirectResponse directResponse = new DirectResponse();
        List<IndexResponse> list = new ArrayList<>();
        IndexResponse indexResponse = new IndexResponse();
        indexResponse.setDc(ONE_BILLION + 1000);
        list.add(indexResponse);
        directResponse.setRestStatus(RestStatus.OK);
        directResponse.setResponseContent(JSON.toJSONString(list));
        Mockito.when(esClusterNodeDAO.getDirectResponse(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(directResponse);
        Assertions.assertEquals(list.size(), esClusterNodeService.syncGetBigIndices(CustomDataSource.PHY_CLUSTER_NAME).size());
    }

    @Test
    public void syncGetIndicesCountTest() {
        int cnt = 1;
        Mockito.when(esClusterNodeDAO.getIndicesCount(Mockito.any(), Mockito.any())).thenReturn(cnt);
        Assertions.assertEquals(cnt, esClusterNodeService.syncGetIndicesCount(CustomDataSource.PHY_CLUSTER_NAME, "test"));

    }
}