package com.didi.arius.gateway.core.arius;

import com.didi.arius.gateway.core.service.arius.DynamicConfigService;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class DynamicConfigServiceTests {

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Before
    public void setUp() {
    }

    @Test
    public void testGetDetailLogFlag(){
        final boolean detailLogFlag = dynamicConfigService.getDetailLogFlag();
        System.out.println(detailLogFlag);
    }

    @Test
    public void testIsWhiteAppid() throws InterruptedException {
        Thread.sleep(1000L);
        final boolean detailLogFlag = dynamicConfigService.isWhiteAppid(100000000);
        System.out.println(detailLogFlag);
    }

    @Test
    public void testResetDynamicConfigInfo(){
        dynamicConfigService.resetDynamicConfigInfo();
    }



}
