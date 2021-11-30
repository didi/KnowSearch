package com.didi.arius.gateway.remote;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.core.service.arius.*;
import com.didi.arius.gateway.remote.response.*;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import org.elasticsearch.gateway.GatewayService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class ScheduleServiceTests {

    @Autowired
    private AppService appService;

    @Autowired
    private DslTemplateService dslTemplateService;

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private GateWayHeartBeatService gateWayHeartBeatService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Before
    public void setUp() {
    }

    @Test
    public void testResetAppInfo() {
        appService.resetAppInfo();
    }

    @Test
    public void testResetDslInfo() {
        dslTemplateService.resetDslInfo();
    }

    @Test
    public void testResetDynamicConfigInfo() {
        dynamicConfigService.resetDynamicConfigInfo();
    }

    @Test
    public void testResetESClusaterInfo() {
        esClusterService.resetESClusaterInfo();
    }

    @Test
    public void testResetHeartBeatInfo() {
        gateWayHeartBeatService.resetHeartBeatInfo();
    }

    @Test
    public void testResetIndexTemplateInfo() {
        indexTemplateService.resetIndexTemplateInfo();
    }


}
