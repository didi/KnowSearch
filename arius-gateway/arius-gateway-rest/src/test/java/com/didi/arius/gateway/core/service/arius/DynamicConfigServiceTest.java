package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.arius.impl.DynamicConfigServiceImpl;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.DynamicConfigListResponse;
import com.didi.arius.gateway.util.CustomDataSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author wuxuan
 * @Date 2022/6/14
 */
public class DynamicConfigServiceTest {

    @Mock
    private ThreadPool threadPool;
    @Mock
    private AriusAdminRemoteService ariusAdminRemoteService;
    @InjectMocks
    private DynamicConfigServiceImpl dynamicConfigService;

    @Before
    public void setUp() {
        initMocks(this);
        dynamicConfigService.init();
    }

    @Test
    public void testGetDetailLogFlag(){
        dynamicConfigService.getDetailLogFlag();
    }

    @Test
    public void testIsWhiteAppid(){
        dynamicConfigService.isWhiteAppid(100000000);
    }

    @Test
    public void testGetForbiddenSettings(){
        dynamicConfigService.getForbiddenSettings();
    }

    @Test
    public void testResetDynamicConfigInfo(){
        //设置不同参数遍历不同分支。
        dynamicConfigService.resetDynamicConfigInfo();
        DynamicConfigListResponse dynamicConfigListResponse = CustomDataSource.dynamicConfigListResponseFactory();
        when(ariusAdminRemoteService.listQueryConfig()).thenReturn(dynamicConfigListResponse);
        dynamicConfigService.resetDynamicConfigInfo();
        dynamicConfigListResponse.setCode(1);
        dynamicConfigService.resetDynamicConfigInfo();
    }
}
