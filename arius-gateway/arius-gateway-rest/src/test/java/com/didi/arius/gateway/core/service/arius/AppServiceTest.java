package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.RateLimitService;
import com.didi.arius.gateway.core.service.arius.impl.AppServiceImpl;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.AppListResponse;
import com.didi.arius.gateway.util.CustomDataSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author wuxuan
 * @Date 2022/6/23
 */
public class AppServiceTest {
    @Mock
    private AriusAdminRemoteService ariusAdminRemoteService;

    @Mock
    private IndexTemplateService indexTemplateService;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private ThreadPool threadPool;

    @InjectMocks
    private AppServiceImpl appService;

    @Before
    public void setUp() {
        initMocks(this);
        appService.init();
        AppListResponse appListResponse = CustomDataSource.appListResponseFactory();
        when(ariusAdminRemoteService.listApp()).thenReturn(appListResponse);
        when(indexTemplateService.checkIndex(anyString(),anyList())).thenReturn(true);
    }

    @Test
    public void testGetAppDetail() {
        appService.getAppDetail(CustomDataSource.appid);
    }

    @Test
    public void testGetAppDetails() {
        appService.getAppDetails();
    }

    @Test
    public void testGetAppDetailFromIp() {
        appService.resetAppInfo();
        appService.getAppDetailFromIp(CustomDataSource.ip);
    }

    @Test
    public void testResetAppInfo() {
        appService.resetAppInfo();
    }

    @Test
    public void testCheckToken() {
        appService.resetAppInfo();
        appService.checkToken(CustomDataSource.baseContextFactory());
    }

    @Test
    public void testCheckWriteIndices() {
        List<String> indices = new ArrayList<>();
        indices.add(CustomDataSource.INDEX_NAME);
        indices.add(CustomDataSource.INDEX_NAME2);
        appService.checkWriteIndices(CustomDataSource.baseContextFactory(),indices);
    }

    @Test
    public void testCheckIndices() {
        List<String> indices = new ArrayList<>();
        indices.add(CustomDataSource.INDEX_NAME);
        indices.add(CustomDataSource.INDEX_NAME2);
        appService.checkIndices(CustomDataSource.baseContextFactory(),indices);
    }

}
