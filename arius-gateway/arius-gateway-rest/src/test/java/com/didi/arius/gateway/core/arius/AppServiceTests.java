package com.didi.arius.gateway.core.arius;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.service.arius.AppService;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author lyn
 * @date 2022/6/8
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class AppServiceTests {
    private static String INDEX_NAME = "cn_record.arius.template.value_2021-05";

    @Autowired
    private AppService appService;

    @Before
    public void setUp() {
    }

    @Test
    public void testGetAppDetail() throws InterruptedException {
        final AppDetail appDetail = appService.getAppDetail(5);
        System.out.println(JSON.toJSONString(appDetail));
        assertEquals(true, appDetail != null);
    }

    @Test
    public void testGetAppDetails() throws InterruptedException {
        final Map<Integer, AppDetail> appDetails = appService.getAppDetails();
        System.out.println(JSON.toJSONString(appDetails));
        assertEquals(true, appDetails != null);
    }

    @Test
    public void testGetAppDetailFromIp() throws InterruptedException {
        final AppDetail appDetailFromIp = appService.getAppDetailFromIp("127.0.0.1");
        System.out.println(JSON.toJSONString(appDetailFromIp));
    }

    @Test
    public void testResetAppInfo() {
        appService.resetAppInfo();
    }

    @Test
    public void testCheckToken() {
        final QueryContext queryContext = new QueryContext();
        queryContext.setRemoteAddr("127.0.0.1");
        appService.checkToken(queryContext);
    }

    @Test
    public void testCheckWriteIndices() {
        final QueryContext queryContext = new QueryContext();
        queryContext.setRemoteAddr("127.0.0.1");
        AppDetail appDetail = new AppDetail();
        appDetail.setWindexExp(Lists.newArrayList("*"));
        queryContext.setAppDetail(appDetail);
        appService.checkWriteIndices(queryContext, Lists.newArrayList(INDEX_NAME));
    }

    @Test
    public void testCheckIndices() {
        final QueryContext queryContext = new QueryContext();
        queryContext.setRemoteAddr("127.0.0.1");
        AppDetail appDetail = new AppDetail();
        appDetail.setIndexExp(Lists.newArrayList("*"));
        queryContext.setAppDetail(appDetail);
        appService.checkIndices(queryContext, Lists.newArrayList(INDEX_NAME));
    }
}
