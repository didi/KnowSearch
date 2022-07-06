package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.core.service.ESTcpClientService;
import com.didi.arius.gateway.core.service.arius.impl.ESClusterServiceImpl;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.DataCenterListResponse;
import com.didi.arius.gateway.util.CustomDataSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author wuxuan
 * @Date 2022/6/14
 */
public class ESClusterServiceTest {

    @Mock
    private AriusAdminRemoteService ariusAdminRemoteService;
    @Mock
    private ThreadPool threadPool;
    @Mock
    private ESTcpClientService esTcpClientService;
    @Mock
    private ESRestClientService esRestClientService;
    @Mock
    private QueryConfig queryConfig;

    @InjectMocks
    private ESClusterServiceImpl esClusterService;

    private QueryContext queryContext = new QueryContext();
    private IndexTemplate indexTemplate = new IndexTemplate();
    private ESClient client = new ESClient();
    private ESClient esClient = new ESClient();

    @Before
    public void setUp() {
        initMocks(this);
        esClusterService.init();
        client = new ESClient("clusterName", "version");
        queryContext = CustomDataSource.queryContextFactory();
        indexTemplate = CustomDataSource.indexTemplateFactory();
        DataCenterListResponse dataCenterListResponse = CustomDataSource.dataCenterListResponseFactory();
        when(ariusAdminRemoteService.listCluster()).thenReturn(dataCenterListResponse);
        when(esRestClientService.getClient(anyString(),anyString())).thenReturn(client);
        when(esRestClientService.getAdminClient(anyString())).thenReturn(client);
        when(esRestClientService.getClientStrict(anyString(),anyString())).thenReturn(client);
    }

    @Test
    public void testGetMetaVersionByCluster(){
        esClusterService.getMetaVersionByCluster(CustomDataSource.CLUSTER_NAME);
    }
    @Test
    public void testGetDetailLogFlag(){
        Map<String, ESCluster> stringESClusterMap = esClusterService.listESCluster();
        assertEquals(true, stringESClusterMap != null);
    }

    @Test
    public void testResetESClusaterInfo(){
        esClusterService.resetESClusaterInfo();
    }

    @Test
    public void testGetClient(){
        esClient = esClusterService.getClient(queryContext, "action");
        assertEquals(true, esClient != null);
        queryContext.setFromKibana(true);
        esClient = esClusterService.getClient(queryContext, "action");
        assertEquals(true, esClient != null);
    }

    @Test
    public void testGetClient2(){
        esClient = esClusterService.getClient(queryContext, indexTemplate, "action");
        assertEquals(true, esClient != null);
        queryContext.setFromKibana(true);
        esClient = esClusterService.getClient(queryContext, indexTemplate, "action");
        assertEquals(true, esClient != null);
    }

    @Test
    public void testGetClient3(){
        queryContext.setClusterId(null);
        esClient = esClusterService.getClient(queryContext, indexTemplate, "action");
        assertEquals(true, esClient != null);
    }

    @Test
    public void testGetClient4(){
        queryContext.setClusterId(null);
        indexTemplate.setSlaveInfos(null);
        esClient = esClusterService.getClient(queryContext, indexTemplate, "action");
        assertEquals(true, esClient != null);
    }

    @Test
    public void testGetClientFromCluster(){
        esClient = esClusterService.getClientFromCluster(queryContext, CustomDataSource.CLUSTER_NAME, "action");
        assertEquals(true, esClient != null);
    }

    @Test
    public void testGetWriteClient(){
        esClient = esClusterService.getWriteClient(indexTemplate, "action");
        assertEquals(true, esClient != null);
    }

}
