package com.didi.arius.gateway.core;

import com.didi.arius.gateway.core.service.MetricsService;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class MetricsServiceTests {

    private static String INDEX_NAME = "cn_record.arius.template.value_2021-05";

    @Autowired
    private MetricsService metricsService;

    @Before
    public void setUp() {
    }

    @Test
    public void testAddQueryCost() {
        metricsService.addQueryCost(5, 1);
    }

    @Test
    public void testAddSlowlogCost() {
        metricsService.addSlowlogCost(5, 1);
    }

    @Test
    public void testAddSearchResponseMetrics() {
        metricsService.addSearchResponseMetrics(5, 10, 2, 2, 1);
    }

    @Test
    public void testAddQueryMetrics() {
        metricsService.addQueryMetrics(5, 10, 20, 20);
    }

    @Test
    public void testAddIndexMetrics() {
        metricsService.addIndexMetrics(INDEX_NAME, "root", 10, 20, 20);
    }

    @Test
    public void testIncrQueryAggs() {
        metricsService.incrQueryAggs(5);
    }




}
