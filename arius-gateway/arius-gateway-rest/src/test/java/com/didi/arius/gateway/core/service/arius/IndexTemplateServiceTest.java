package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.core.service.arius.impl.IndexTemplateServiceImpl;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.IndexTemplateListResponse;
import com.didi.arius.gateway.util.CustomDataSource;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author wuxuan
 * @Date 2022/6/14
 */
public class IndexTemplateServiceTest {

    @Mock
    private ESClusterService esClusterService;
    @Mock
    private AriusAdminRemoteService ariusAdminRemoteService;
    @Mock
    private ESRestClientService esRestClientService;
    @Mock
    private ThreadPool threadPool;
    @InjectMocks
    private IndexTemplateServiceImpl indexTemplateService;

    @Before
    public void setUp() {
        initMocks(this);
        indexTemplateService.init();
        Map<String, ESCluster> listESCluster = new HashMap<>();
        listESCluster.put("key",CustomDataSource.esClusterFactory());
        when(esClusterService.listESCluster()).thenReturn(listESCluster);
        IndexTemplateListResponse indexTemplateListResponse = CustomDataSource.indexTemplateListResponseFactory();
        when(ariusAdminRemoteService.listDeployInfo()).thenReturn(indexTemplateListResponse);
        when(esRestClientService.getESClusterMap()).thenReturn(listESCluster);
        when(ariusAdminRemoteService.getTemplateInfoMap(anyString())).thenReturn(CustomDataSource.templateInfoListResponseFactory());

    }

    @Test
    public void testGetTemplateExpressionMap() {
        indexTemplateService.getTemplateExpressionMap();
    }

    @Test
    public void testGetTemplateAliasMap() {
        indexTemplateService.getTemplateAliasMap();
    }

    @Test
    public void testGetIndexTemplate() {
        indexTemplateService.getIndexTemplate(CustomDataSource.INDEX_NAME);
    }

    @Test
    public void testGetIndexTemplateByTire() {
        indexTemplateService.getIndexTemplateByTire(CustomDataSource.INDEX_NAME);
    }

    @Test
    public void testGetIndexTemplateMap() {
        indexTemplateService.getIndexTemplateMap();
    }

    @Test
    public void testResetIndexTemplateInfo() {
        indexTemplateService.resetIndexTemplateInfo();
    }

    @Test
    public void testGetIndexAlias() {
        indexTemplateService.getIndexAlias(CustomDataSource.INDEX_NAME);
    }

    @Test
    public void testCheckIndex() {
        final boolean b = indexTemplateService.checkIndex(CustomDataSource.INDEX_NAME+"_2021-05", Lists.newArrayList(CustomDataSource.INDEX_NAME+"*"));
        assertEquals(true, b);
        final boolean b1 = indexTemplateService.checkIndex(CustomDataSource.INDEX_NAME+"_2021-05", Lists.newArrayList(CustomDataSource.INDEX_NAME+"asdf*"));
        assertEquals(false, b1);
    }

    @Test
    public void testGetIndexVersion() {
        indexTemplateService.getIndexVersion(null,CustomDataSource.CLUSTER_NAME);
        indexTemplateService.getIndexVersion(CustomDataSource.INDEX_NAME,null);
        indexTemplateService.getIndexVersion(CustomDataSource.INDEX_NAME, CustomDataSource.CLUSTER_NAME);
    }

}
