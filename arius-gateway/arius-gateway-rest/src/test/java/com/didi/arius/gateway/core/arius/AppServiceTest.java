package com.didi.arius.gateway.core.arius;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.service.arius.AppService;
import com.didi.arius.gateway.core.service.arius.impl.AppServiceImpl;
import com.didi.arius.gateway.util.CustomDataSource;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class AppServiceTest {

    private static String INDEX_NAME = "cn_record.arius.template.value_2021-05";

    AppService appService = new AppServiceImpl();
    Map<Integer, AppDetail> appDetails = Mockito.mock(Map.class);
    Map<String, AppDetail> ipToAppMap = Mockito.mock(Map.class);

    @Before
    public void setUp() {
        ipToAppMap.put(CustomDataSource.ip,CustomDataSource.appDetailFactory());
        ReflectionTestUtils.setField(appService, "appDetails", appDetails);
        ReflectionTestUtils.setField(appService,"ipToAppMap",ipToAppMap);
    }

    @Test
    public void testGetAppDetail() {
        Mockito.when(appDetails.get(Mockito.anyInt())).thenReturn(CustomDataSource.appDetailFactory());
        AppDetail appDetail = appService.getAppDetail(CustomDataSource.appid);
        assertEquals(true, appDetail != null);
    }

    @Test
    public void testGetAppDetails() {
        Map<Integer, AppDetail> appDetails = appService.getAppDetails();
        assertEquals(true, appDetails != null);
    }

    @Test
    public void testGetAppDetailFromIp() {
        AppDetail appDetailFromIp = appService.getAppDetailFromIp(null);
        assertEquals(true,appDetailFromIp == null);
        appDetailFromIp = appService.getAppDetailFromIp(CustomDataSource.ip);
        assertEquals(true,appDetailFromIp != null);
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
