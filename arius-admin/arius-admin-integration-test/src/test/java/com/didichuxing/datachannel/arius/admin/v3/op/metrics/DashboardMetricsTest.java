package com.didichuxing.datachannel.arius.admin.v3.op.metrics;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsDashboardListDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.list.MetricListVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.dashboard.ClusterPhyHealthMetricsVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.metrics.DashboardMetricsControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class DashboardMetricsTest extends BaseContextTest {

    @Test
    public void getClusterHealthInfoTest() throws IOException {
        Result<ClusterPhyHealthMetricsVO> result = DashboardMetricsControllerMethod.getClusterHealthInfo();
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getTopClusterMetricsInfoTest() throws IOException {
        MetricsDashboardTopNDTO param = CustomDataSource.getMetricsDashboardTopNDTO();
        param.setMetricsTypes(CustomDataSource.getRandomTopClusterMetrics());
        Result<List<VariousLineChartMetricsVO>> result = DashboardMetricsControllerMethod.getTopClusterMetricsInfo(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getTopNodeMetricsInfoTest() throws IOException {
        MetricsDashboardTopNDTO param = CustomDataSource.getMetricsDashboardTopNDTO();
        param.setMetricsTypes(CustomDataSource.getRandomTopNodeMetrics());
        Result<List<VariousLineChartMetricsVO>> result = DashboardMetricsControllerMethod.getTopNodeMetricsInfo(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getTopTemplateMetricsInfoTest() throws IOException {
    }

    @Test
    public void getTopIndexMetricsInfoTest() throws IOException {
        MetricsDashboardTopNDTO param = CustomDataSource.getMetricsDashboardTopNDTO();
        param.setMetricsTypes(CustomDataSource.getRandomTopIndexMetrics());
        Result<List<VariousLineChartMetricsVO>> result = DashboardMetricsControllerMethod.getTopIndexMetricsInfo(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getTopClusterThreadPoolQueueMetricsInfoTest() throws IOException {
        MetricsDashboardTopNDTO param = CustomDataSource.getMetricsDashboardTopNDTO();
        param.setMetricsTypes(CustomDataSource.getRandomTopClusterThreadPoolQueueMetrics());
        Result<List<VariousLineChartMetricsVO>> result = DashboardMetricsControllerMethod.getTopClusterThreadPoolQueueMetricsInfo(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getListClusterMetricsInfoTest() throws IOException {
    }

    @Test
    public void getListNodeMetricsInfoTest() throws IOException {
        MetricsDashboardListDTO param = CustomDataSource.getMetricsDashboardListDTO();
        param.setMetricsTypes(CustomDataSource.getRandomListNodeMetrics());
        Result<List<MetricListVO>> result = DashboardMetricsControllerMethod.getListNodeMetricsInfo(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getListTemplateMetricsInfoTest() throws IOException {
        MetricsDashboardListDTO param = CustomDataSource.getMetricsDashboardListDTO();
        param.setMetricsTypes(CustomDataSource.getRandomListTemplateMetrics());
        Result<List<MetricListVO>> result = DashboardMetricsControllerMethod.getListTemplateMetricsInfo(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getListIndexMetricsInfoTest() throws IOException {
        MetricsDashboardListDTO param = CustomDataSource.getMetricsDashboardListDTO();
        param.setMetricsTypes(CustomDataSource.getRandomListIndexMetrics());
        Result<List<MetricListVO>> result = DashboardMetricsControllerMethod.getListIndexMetricsInfo(param);
        Assertions.assertTrue(result.success());
    }

}
