package com.didi.arius.gateway.core;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.flowcontrol.FlowController;
import com.didi.arius.gateway.common.metadata.FlowThreshold;
import com.didi.arius.gateway.core.service.RateLimitService;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class RateLimitServiceTests {


    @Autowired
    private RateLimitService rateLimitService;

    @Before
    public void setUp() {
    }

    @Test
    public void testAddByteIn() {
        rateLimitService.addByteIn(50L);
    }

    @Test
    public void testRemoveByteIn() {
        rateLimitService.removeByteIn(50L);
    }

    @Test
    public void testIsTrafficDataOverflow() {
        final boolean all = rateLimitService.isTrafficDataOverflow(5, "all");
        System.out.println(all);
    }

    @Test
    public void testAddUp() {
        rateLimitService.addUp(5, "all", 50, 45);
    }

    @Test
    public void testResetAppAreaFlow() {
        FlowThreshold flowThreshold = new FlowThreshold();
        flowThreshold.setOpsUpper(1000);
        flowThreshold.setOpsLower(100);
        rateLimitService.resetAppAreaFlow(5, flowThreshold);
    }

    @Test
    public void testGetFlowControllerMap() {
        final Map<Integer, FlowController> flowControllerMap = rateLimitService.getFlowControllerMap();
        System.out.println(JSON.toJSONString(flowControllerMap));
    }


}
