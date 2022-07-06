package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.arius.impl.GateWayHeartBeatServiceImpl;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.ActiveCountResponse;
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
public class GateWayHeartBeatServiceTest {

    @Mock
    private QueryConfig queryConfig;
    @Mock
    private ThreadPool threadPool;
    @Mock
    private AriusAdminRemoteService ariusAdminRemoteService;
    @InjectMocks
    private GateWayHeartBeatServiceImpl gateWayHeartBeatService;

    @Before
    public void setUp() {
        initMocks(this);
        gateWayHeartBeatService.init();
    }

    @Test
    public void testResetHeartBeatInfo(){
        ActiveCountResponse response = new ActiveCountResponse();
        response.setData(0);
        when(ariusAdminRemoteService.getAliveCount(QueryConsts.GATEWAY_GROUP)).thenReturn(response);
        gateWayHeartBeatService.resetHeartBeatInfo();
    }

    @Test
    public void testResetHeartBeatInfo2(){
        ActiveCountResponse response = new ActiveCountResponse();
        response.setData(1);
        when(ariusAdminRemoteService.getAliveCount(QueryConsts.GATEWAY_GROUP)).thenReturn(response);
        gateWayHeartBeatService.resetHeartBeatInfo();
    }

    @Test
    public void testResetHeartBeatInfo3(){
        ActiveCountResponse response = new ActiveCountResponse();
        response.setData(2);
        when(ariusAdminRemoteService.getAliveCount(QueryConsts.GATEWAY_GROUP)).thenReturn(response);
        gateWayHeartBeatService.resetHeartBeatInfo();
    }

}
