package com.didi.arius.gateway.core.arius;

import com.didi.arius.gateway.core.service.arius.GateWayHeartBeatService;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class GateWayHeartBeatServiceTests {

    @Autowired
    private GateWayHeartBeatService gateWayHeartBeatService;

    @Before
    public void setUp() {
    }

    @Test
    public void testResetHeartBeatInfo(){
        gateWayHeartBeatService.resetHeartBeatInfo();
    }



}
