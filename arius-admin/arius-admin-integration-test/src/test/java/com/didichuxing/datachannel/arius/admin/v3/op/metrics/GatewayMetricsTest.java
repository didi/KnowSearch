package com.didichuxing.datachannel.arius.admin.v3.op.metrics;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.gateway.GatewayOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.metrics.GatewayMetricsControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class GatewayMetricsTest extends BaseContextTest {

    @Test
    public void getGatewayMetricsTest() throws IOException {
        Result<List<String>> result = GatewayMetricsControllerMethod.getGatewayMetrics("overview");
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getDslMd5ListTest() throws IOException {
        Long now = System.currentTimeMillis();
        Result<List<String>> result = GatewayMetricsControllerMethod.getDslMd5List(String.valueOf(now - 5 * 60 * 1000), String.valueOf(now));
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getGatewayOverviewMetricsTest() throws IOException {
        GatewayOverviewDTO param = new GatewayOverviewDTO();
        CustomDataSource.setGatewayMetricsDTO(param);
        param.setMetricsTypes(CustomDataSource.getRandomOverviewMetrics());
        Result<List<GatewayOverviewMetricsVO>> result = GatewayMetricsControllerMethod.getGatewayOverviewMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getGatewayNodeMetricsTest() throws IOException {
        GatewayNodeDTO param = new GatewayNodeDTO();
        CustomDataSource.setGatewayMetricsDTO(param);
        param.setTopNu(5);
        param.setMetricsTypes(CustomDataSource.getRandomGatewayNodeMetrics());
        Result<List<VariousLineChartMetricsVO>> result = GatewayMetricsControllerMethod.getGatewayNodeMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getMultiGatewayNodesMetricsTest() throws IOException {
        MultiGatewayNodesDTO param = new MultiGatewayNodesDTO();
        CustomDataSource.setGatewayMetricsDTO(param);
        param.setNodeIps(Lists.newArrayList("40517-master01-0", "40517-master01-2"));
        param.setMetricsTypes(CustomDataSource.getRandomGatewayNodeMetrics());
        Result<List<VariousLineChartMetricsVO>> result = GatewayMetricsControllerMethod.getMultiGatewayNodesMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getClientNodeMetricsTest() throws IOException {
        ClientNodeDTO param = new ClientNodeDTO();
        CustomDataSource.setGatewayMetricsDTO(param);
        param.setTopNu(5);
        param.setMetricsTypes(CustomDataSource.getRandomClientNodeMetrics());
        Result<List<VariousLineChartMetricsVO>> result = GatewayMetricsControllerMethod.getClientNodeMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getClientNodeIpListTest() throws IOException {
        Long now = System.currentTimeMillis();
        Result<List<String>> result = GatewayMetricsControllerMethod.getClientNodeIpList("", now - 5 * 60 * 1000, now);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getGatewayIndexMetricsTest() throws IOException {
        GatewayIndexDTO param = new GatewayIndexDTO();
        CustomDataSource.setGatewayMetricsDTO(param);
        param.setTopNu(5);
        param.setMetricsTypes(CustomDataSource.getRandomGatewayIndexMetrics());
        Result<List<VariousLineChartMetricsVO>> result = GatewayMetricsControllerMethod.getGatewayIndexMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getGatewayAppMetricsTest() throws IOException {
        GatewayAppDTO param = new GatewayAppDTO();
        CustomDataSource.setGatewayMetricsDTO(param);
        param.setTopNu(5);
        param.setMetricsTypes(CustomDataSource.getRandomGatewayAppMetrics());
        Result<List<VariousLineChartMetricsVO>> result = GatewayMetricsControllerMethod.getGatewayAppMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getGatewayDslMetricsTest() throws IOException {
        GatewayDslDTO param = new GatewayDslDTO();
        CustomDataSource.setGatewayMetricsDTO(param);
        param.setTopNu(5);
        param.setMetricsTypes(CustomDataSource.getRandomGatewayDslMetrics());
        Result<List<VariousLineChartMetricsVO>> result = GatewayMetricsControllerMethod.getGatewayDslMetrics(param);
        Assertions.assertTrue(result.success());
    }

}
