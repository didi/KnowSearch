package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.common.metadata.DSLTemplate;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.arius.impl.DslTemplateServiceImpl;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.util.CustomDataSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author wuxuan
 * @Date 2022/6/14
 */
public class DslTemplateServiceTest {

    @Mock
    private ThreadPool threadPool;
    @Mock
    private AriusAdminRemoteService ariusAdminRemoteService;
    @Mock
    private QueryConfig queryConfig;

    @InjectMocks
    private DslTemplateServiceImpl dslTemplateService;

    private  DSLTemplate dslTemplate = new DSLTemplate(1, 1, true);
    @Before
    public void setUp() {
        initMocks(this);
        dslTemplateService.init();
    }



    @Test
    public void testPutDSLTemplate(){
        dslTemplateService.putDSLTemplate("key", dslTemplate);
    }

    @Test
    public void testRemoveDSLTemplate() {
        dslTemplateService.putDSLTemplate("key", dslTemplate);
        DSLTemplate dslTemplate = dslTemplateService.getDSLTemplate("key");
        assertEquals(true, dslTemplate != null);
        dslTemplateService.removeDSLTemplate("key");
        assertEquals(true, dslTemplateService.getDSLTemplate("key1") == null);
    }

    @Test
    public void testGetDSLTemplate() {
        dslTemplateService.putDSLTemplate("key", dslTemplate);
        DSLTemplate dslTemplate = dslTemplateService.getDSLTemplate("key");
        assertEquals(true, dslTemplate != null);
        dslTemplate = dslTemplateService.getDSLTemplate(null);
        assertEquals(true, dslTemplate == null);
    }

    @Test
    public void testGetDslTemplateKeys() {
        dslTemplateService.putDSLTemplate("key", dslTemplate);
        List<String> dslTemplateKeys = dslTemplateService.getDslTemplateKeys();
        assertEquals(true, dslTemplateKeys.size() >= 0);
    }

    @Test
    public void testPutNewDSLTemplate(){
        dslTemplateService.putNewDSLTemplate("key", dslTemplate);
    }

    @Test
    public void testGetNewDSLTemplate(){
        dslTemplateService.putNewDSLTemplate("key", dslTemplate);
        DSLTemplate dslTemplate = dslTemplateService.getNewDSLTemplate("key");
        assertEquals(true, dslTemplate != null);
        dslTemplate = dslTemplateService.getNewDSLTemplate(null);
        assertEquals(true,dslTemplate==null);
    }

    @Test
    public void testGetNewDslTemplateKeys(){
        dslTemplateService.putNewDSLTemplate("key1",dslTemplate);
        List<String> newDslTemplateKeys = dslTemplateService.getNewDslTemplateKeys();
        assertEquals(true, newDslTemplateKeys.size() >= 0);
    }

    @Test
    public void testResetDslInfo(){
        dslTemplateService.resetDslInfo();
        dslTemplateService.putDSLTemplate("key", dslTemplate);
        when(ariusAdminRemoteService.listDslTemplates(0,null)).thenReturn(CustomDataSource.dslTemplateListResponseFactory());
        dslTemplateService.resetDslInfo();
    }
}
