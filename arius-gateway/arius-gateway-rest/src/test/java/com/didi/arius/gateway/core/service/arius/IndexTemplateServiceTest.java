package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.common.enums.TemplateBlockTypeEnum;
import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.common.metadata.TemplateInfo;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didi.arius.gateway.common.enums.TemplateBlockTypeEnum.READ_BLOCK_TYPE;
import static com.didi.arius.gateway.common.enums.TemplateBlockTypeEnum.WRITE_WRITE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
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
    @Mock
    private AppService appService;
    @InjectMocks
    private IndexTemplateServiceImpl indexTemplateService;

    private Map<String, String>         indexToAlias     = new HashMap<>();
    private Map<String, Map<String, TemplateInfo>> templateAliasesMap 	 = new HashMap<>();
    private Map<String, Map<String, TemplateInfo>> templateExpressionMap = new HashMap<>();

    @Before
    public void setUp() {
        initMocks(this);
        indexTemplateService.init();
        when(esClusterService.listESCluster()).thenReturn(CustomDataSource.listESClusterFactory());
        when(ariusAdminRemoteService.listDeployInfo()).thenReturn(CustomDataSource.indexTemplateListResponseFactory());
        when(esRestClientService.getESClusterMap()).thenReturn(CustomDataSource.listESClusterFactory());
        when(ariusAdminRemoteService.getTemplateInfoMap(anyString())).thenReturn(CustomDataSource.templateInfoListResponseFactory());
        when(ariusAdminRemoteService.addAdminTemplateAlias(any())).thenReturn(CustomDataSource.tempaletAliasResponseFactory());
        when(ariusAdminRemoteService.delAdminTemplateAlias(any())).thenReturn(CustomDataSource.tempaletAliasResponseFactory());
        ReflectionTestUtils.setField(indexTemplateService,"indexTemplateMap",CustomDataSource.indexTemplateMapFactory());
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
        indexTemplateService.resetIndexTemplateInfo();
        indexTemplateService.getIndexTemplateByTire(CustomDataSource.INDEX_NAME);
    }

    @Test
    public void testGetIndexTemplateMap() {
        indexTemplateService.getIndexTemplateMap();
    }

    @Test
    public void testResetIndexTemplateInfo() {
        indexTemplateService.resetIndexTemplateInfo();
        indexTemplateService.resetIndexTemplateInfo();
    }

    @Test
    public void testGetIndexAlias() {
        indexTemplateService.getIndexAlias(CustomDataSource.INDEX_NAME);
    }

    @Test
    public void testCheckIndex() {
        boolean b = indexTemplateService.checkIndex(CustomDataSource.INDEX_NAME+"_2021-05", Lists.newArrayList(CustomDataSource.INDEX_NAME+"*"));
        assertEquals(true, b);
        b = indexTemplateService.checkIndex(".",Lists.newArrayList(CustomDataSource.INDEX_NAME+"asdf*"));
        assertEquals(true,b);
        boolean b1 = indexTemplateService.checkIndex(CustomDataSource.INDEX_NAME+"_2021-05", Lists.newArrayList(CustomDataSource.INDEX_NAME+"asdf*"));
        assertEquals(false, b1);
    }

    @Test
    public void testGetIndexVersion() {
        indexTemplateService.getIndexVersion(null,CustomDataSource.CLUSTER_NAME);
        indexTemplateService.getIndexVersion(CustomDataSource.INDEX_NAME,null);
        indexTemplateService.getIndexVersion(CustomDataSource.INDEX_NAME, CustomDataSource.CLUSTER_NAME);
        Map<String, TemplateInfo> templateInfoMap = new HashMap<>();
        templateInfoMap.put(CustomDataSource.INDEX_NAME,CustomDataSource.templateInfoFactory());
        templateExpressionMap.put(CustomDataSource.CLUSTER_NAME,templateInfoMap);
        ReflectionTestUtils.setField(indexTemplateService,"templateExpressionMap",templateExpressionMap);
        indexTemplateService.getIndexVersion(CustomDataSource.INDEX_NAME, CustomDataSource.CLUSTER_NAME);
        indexTemplateService.getIndexVersion(CustomDataSource.INDEX_NAME2+"*",CustomDataSource.CLUSTER_NAME);
        templateAliasesMap.put(CustomDataSource.CLUSTER_NAME,templateInfoMap);
        ReflectionTestUtils.setField(indexTemplateService,"templateAliasesMap",templateAliasesMap);
        indexTemplateService.getIndexVersion(CustomDataSource.INDEX_NAME, CustomDataSource.CLUSTER_NAME);
    }

    @Test
    public void testGetIndexVersion2() {
        Map<String, TemplateInfo> templateInfoMap = new HashMap<>();
        templateInfoMap.put(CustomDataSource.INDEX_NAME,CustomDataSource.templateInfoFactory());
        templateAliasesMap.put(CustomDataSource.INDEX_NAME,templateInfoMap);
        templateExpressionMap.put(CustomDataSource.INDEX_NAME,templateInfoMap);
        ReflectionTestUtils.setField(indexTemplateService,"templateExpressionMap",templateExpressionMap);
        indexTemplateService.getIndexVersion(CustomDataSource.INDEX_NAME, CustomDataSource.CLUSTER_NAME);
        ReflectionTestUtils.setField(indexTemplateService,"templateAliasesMap",templateAliasesMap);
        indexTemplateService.getIndexVersion(CustomDataSource.INDEX_NAME, CustomDataSource.CLUSTER_NAME);
    }

    @Test
    public void testGetIndexVersion3() {
        Map<String, TemplateInfo> templateInfoMap = new HashMap<>();
        templateInfoMap.put(CustomDataSource.INDEX_NAME+"*",CustomDataSource.templateInfoFactory());
        templateExpressionMap.put(CustomDataSource.INDEX_NAME,templateInfoMap);
        ReflectionTestUtils.setField(indexTemplateService,"templateExpressionMap",templateExpressionMap);
        indexTemplateService.getIndexVersion(CustomDataSource.INDEX_NAME, CustomDataSource.CLUSTER_NAME);
    }

    @Test
    public void testGetTemplateByIndexTire() {
        List<String> indices = new ArrayList<>();
        indices.add(CustomDataSource.INDEX_NAME);
        indices.add(CustomDataSource.INDEX_NAME2);
        indices.add(CustomDataSource.INDEX_NAME3);
        indexTemplateService.resetIndexTemplateInfo();
        indexToAlias.put(CustomDataSource.INDEX_NAME3,CustomDataSource.INDEX_NAME);
        ReflectionTestUtils.setField(indexTemplateService,"indexToAlias",indexToAlias);
        indexTemplateService.getTemplateByIndexTire(indices,CustomDataSource.queryContextFactory());
        indices.clear();
        indices.add(CustomDataSource.INDEX_NAME3);
        indexTemplateService.getTemplateByIndexTire(indices,CustomDataSource.queryContextFactory());
    }

    @Test
    public void testAddTemplateAlias() {
        when(appService.getAppDetail(anyInt())).thenReturn(null);
        indexTemplateService.addTemplateAlias(CustomDataSource.appid,1,CustomDataSource.INDEX_NAME,CustomDataSource.INDEX_NAME2);
    }

    @Test
    public void testAddTemplateAlias2() {
        when(appService.getAppDetail(anyInt())).thenReturn(CustomDataSource.appDetailFactory() );
        indexTemplateService.addTemplateAlias(CustomDataSource.appid,1,CustomDataSource.INDEX_NAME,CustomDataSource.INDEX_NAME2);
        indexTemplateService.resetIndexTemplateInfo();
        indexTemplateService.addTemplateAlias(CustomDataSource.appid,1,CustomDataSource.INDEX_NAME,CustomDataSource.INDEX_NAME2);
        indexTemplateService.addTemplateAlias(CustomDataSource.appid,1,CustomDataSource.INDEX_NAME3,CustomDataSource.INDEX_NAME2);
    }

    @Test
    public void testDelTemplateAlias() {
        when(appService.getAppDetail(anyInt())).thenReturn(null);
        indexTemplateService.delTemplateAlias(CustomDataSource.appid,1,CustomDataSource.INDEX_NAME,CustomDataSource.INDEX_NAME2);
    }

    @Test
    public void testDelTemplateAlias2() {
        when(appService.getAppDetail(anyInt())).thenReturn(CustomDataSource.appDetailFactory() );
        indexTemplateService.resetIndexTemplateInfo();
        indexTemplateService.addTemplateAlias(CustomDataSource.appid,1,CustomDataSource.INDEX_NAME,CustomDataSource.INDEX_NAME2);
        indexTemplateService.delTemplateAlias(CustomDataSource.appid,1,CustomDataSource.INDEX_NAME,CustomDataSource.INDEX_NAME2);
        indexTemplateService.delTemplateAlias(CustomDataSource.appid,1,CustomDataSource.INDEX_NAME3,CustomDataSource.INDEX_NAME2);
    }

    @Test
    public void testCheckTemplateExist() {
        List<String> indices = new ArrayList<>();
        indices.add(CustomDataSource.INDEX_NAME);
        indices.add(CustomDataSource.INDEX_NAME3);
        indices.add(".index");
        when(appService.getAppDetail(anyInt())).thenReturn(CustomDataSource.appDetailFactory() );
        indexTemplateService.resetIndexTemplateInfo();
        indexToAlias.put(CustomDataSource.INDEX_NAME3,CustomDataSource.INDEX_NAME);
        ReflectionTestUtils.setField(indexTemplateService,"indexToAlias",indexToAlias);
        indexTemplateService.checkTemplateExist(indices);
    }

    @Test
    public void testCheckTemplateBlock() {
        List<String> indices = new ArrayList<>();
        indices.add(CustomDataSource.INDEX_NAME);
        indices.add(CustomDataSource.INDEX_NAME3);
        indices.add(".index");
        TemplateBlockTypeEnum templateBlockTypeEnum = READ_BLOCK_TYPE;
        indexTemplateService.resetIndexTemplateInfo();
        indexToAlias.put(CustomDataSource.INDEX_NAME3,CustomDataSource.INDEX_NAME);
        ReflectionTestUtils.setField(indexTemplateService,"indexToAlias",indexToAlias);
        indexTemplateService.checkTemplateBlock(indices,CustomDataSource.appDetailFactory(),templateBlockTypeEnum);
        templateBlockTypeEnum = WRITE_WRITE_TYPE;
        indexTemplateService.checkTemplateBlock(indices,CustomDataSource.appDetailFactory(),templateBlockTypeEnum);
    }
}
