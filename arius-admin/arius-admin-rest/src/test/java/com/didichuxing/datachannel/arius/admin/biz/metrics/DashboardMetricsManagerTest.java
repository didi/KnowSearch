package com.didichuxing.datachannel.arius.admin.biz.metrics;

import com.didichuxing.datachannel.arius.admin.biz.metrics.impl.DashboardMetricsManagerImpl;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardListDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list.MetricList;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list.MetricListContent;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.list.MetricListContentVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.list.MetricListVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.MetricsContentCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.MetricsContentVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DashBoardMetricsService;
import com.didiglobal.logi.security.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SpringTool.class })
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class DashboardMetricsManagerTest {

    @Mock
    private ProjectService              projectService;
    @Mock
    private DashBoardMetricsService     dashBoardMetricsService;
    @Mock
    private AriusConfigInfoService      ariusConfigInfoService;

    @InjectMocks
    private DashboardMetricsManagerImpl dashboardMetricsManager;

    @Test
    void getTopClusterMetricsInfoTest() {
        // Setup
        final MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure DashBoardMetricsService.getToNMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0,0L))));
        when(dashBoardMetricsService
            .getToNMetrics(new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0), "oneLevelType"))
                .thenReturn(variousLineChartMetrics);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = dashboardMetricsManager.getTopClusterMetricsInfo(param,
            0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getTopClusterMetricsInfoDashBoardMetricsServiceReturnsNoItemsTest() {
        // Setup
        final MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0);
        when(projectService.checkProjectExist(0)).thenReturn(false);
        when(dashBoardMetricsService
            .getToNMetrics(new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0), "oneLevelType"))
                .thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = dashboardMetricsManager.getTopClusterMetricsInfo(param,
            0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getTopNodeMetricsInfoTest() {
        // Setup
        final MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure DashBoardMetricsService.getToNMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0,0L))));
        when(dashBoardMetricsService
            .getToNMetrics(new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0), "oneLevelType"))
                .thenReturn(variousLineChartMetrics);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = dashboardMetricsManager.getTopNodeMetricsInfo(param, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getTopNodeMetricsInfoDashBoardMetricsServiceReturnsNoItemsTest() {
        // Setup
        final MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0);
        when(projectService.checkProjectExist(0)).thenReturn(false);
        when(dashBoardMetricsService
            .getToNMetrics(new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0), "oneLevelType"))
                .thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = dashboardMetricsManager.getTopNodeMetricsInfo(param, 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getTopTemplateMetricsInfoTest() {
        // Setup
        final MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure DashBoardMetricsService.getToNMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0,0L))));
        when(dashBoardMetricsService
            .getToNMetrics(new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0), "oneLevelType"))
                .thenReturn(variousLineChartMetrics);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = dashboardMetricsManager.getTopTemplateMetricsInfo(param,
            0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getTopTemplateMetricsInfoDashBoardMetricsServiceReturnsNoItemsTest() {
        // Setup
        final MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0);
        when(projectService.checkProjectExist(0)).thenReturn(false);
        when(dashBoardMetricsService
            .getToNMetrics(new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0), "oneLevelType"))
                .thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = dashboardMetricsManager.getTopTemplateMetricsInfo(param,
            0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getTopIndexMetricsInfoTest() {
        // Setup
        final MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure DashBoardMetricsService.getToNMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0,0L))));
        when(dashBoardMetricsService
            .getToNMetrics(new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0), "oneLevelType"))
                .thenReturn(variousLineChartMetrics);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = dashboardMetricsManager.getTopIndexMetricsInfo(param, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getTopIndexMetricsInfoDashBoardMetricsServiceReturnsNoItemsTest() {
        // Setup
        final MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0);
        when(projectService.checkProjectExist(0)).thenReturn(false);
        when(dashBoardMetricsService
            .getToNMetrics(new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0), "oneLevelType"))
                .thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = dashboardMetricsManager.getTopIndexMetricsInfo(param, 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getTopClusterThreadPoolQueueMetricsInfoTest() {
        // Setup
        final MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure DashBoardMetricsService.getToNMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0,0L))));
        when(dashBoardMetricsService
            .getToNMetrics(new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0), "oneLevelType"))
                .thenReturn(variousLineChartMetrics);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = dashboardMetricsManager
            .getTopClusterThreadPoolQueueMetricsInfo(param, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getTopClusterThreadPoolQueueMetricsInfoDashBoardMetricsServiceReturnsNoItemsTest() {
        // Setup
        final MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0);
        when(projectService.checkProjectExist(0)).thenReturn(false);
        when(dashBoardMetricsService
            .getToNMetrics(new MetricsDashboardTopNDTO(0L, 0L, "aggType", Arrays.asList("value"), 0), "oneLevelType"))
                .thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = dashboardMetricsManager
            .getTopClusterThreadPoolQueueMetricsInfo(param, 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getListClusterMetricsInfoTest() {
        // Setup
        final MetricsDashboardListDTO param = new MetricsDashboardListDTO("aggType", false, Arrays.asList("value"));
        final Result<List<MetricListVO>> expectedResult = Result.buildFail(Arrays.asList(
            new MetricListVO(0L, "type", Arrays.asList(new MetricListContentVO("clusterPhyName", "name", 0.0)))));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure DashBoardMetricsService.getListFaultMetrics(...).
        final MetricList metricList = new MetricList(0L, "type",
            Arrays.asList(new MetricListContent("clusterPhyName", "name", 0.0)));
        when(dashBoardMetricsService.getListFaultMetrics("oneLevelType", "metricsType", "aggType", false))
            .thenReturn(metricList);

        // Configure DashBoardMetricsService.getListValueMetrics(...).
        final MetricList metricList1 = new MetricList(0L, "type",
            Arrays.asList(new MetricListContent("clusterPhyName", "name", 0.0)));
        when(dashBoardMetricsService.getListValueMetrics("oneLevelType", "metricsType", "aggType", false))
            .thenReturn(metricList1);

        when(ariusConfigInfoService.stringSetting("dashboard.threshold", "type", "defaultValue")).thenReturn("result");

        // Run the test
        final Result<List<MetricListVO>> result = dashboardMetricsManager.getListClusterMetricsInfo(param, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getListNodeMetricsInfoTest() {
        // Setup
        final MetricsDashboardListDTO param = new MetricsDashboardListDTO("aggType", false, Arrays.asList("value"));
        final Result<List<MetricListVO>> expectedResult = Result.buildFail(Arrays.asList(
            new MetricListVO(0L, "type", Arrays.asList(new MetricListContentVO("clusterPhyName", "name", 0.0)))));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure DashBoardMetricsService.getListFaultMetrics(...).
        final MetricList metricList = new MetricList(0L, "type",
            Arrays.asList(new MetricListContent("clusterPhyName", "name", 0.0)));
        when(dashBoardMetricsService.getListFaultMetrics("oneLevelType", "metricsType", "aggType", false))
            .thenReturn(metricList);

        // Configure DashBoardMetricsService.getListValueMetrics(...).
        final MetricList metricList1 = new MetricList(0L, "type",
            Arrays.asList(new MetricListContent("clusterPhyName", "name", 0.0)));
        when(dashBoardMetricsService.getListValueMetrics("oneLevelType", "metricsType", "aggType", false))
            .thenReturn(metricList1);

        when(ariusConfigInfoService.stringSetting("dashboard.threshold", "type", "defaultValue")).thenReturn("result");

        // Run the test
        final Result<List<MetricListVO>> result = dashboardMetricsManager.getListNodeMetricsInfo(param, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getListTemplateMetricsInfoTest() {
        // Setup
        final MetricsDashboardListDTO param = new MetricsDashboardListDTO("aggType", false, Arrays.asList("value"));
        final Result<List<MetricListVO>> expectedResult = Result.buildFail(Arrays.asList(
            new MetricListVO(0L, "type", Arrays.asList(new MetricListContentVO("clusterPhyName", "name", 0.0)))));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure DashBoardMetricsService.getListFaultMetrics(...).
        final MetricList metricList = new MetricList(0L, "type",
            Arrays.asList(new MetricListContent("clusterPhyName", "name", 0.0)));
        when(dashBoardMetricsService.getListFaultMetrics("oneLevelType", "metricsType", "aggType", false))
            .thenReturn(metricList);

        // Configure DashBoardMetricsService.getListValueMetrics(...).
        final MetricList metricList1 = new MetricList(0L, "type",
            Arrays.asList(new MetricListContent("clusterPhyName", "name", 0.0)));
        when(dashBoardMetricsService.getListValueMetrics("oneLevelType", "metricsType", "aggType", false))
            .thenReturn(metricList1);

        when(ariusConfigInfoService.stringSetting("dashboard.threshold", "type", "defaultValue")).thenReturn("result");

        // Run the test
        final Result<List<MetricListVO>> result = dashboardMetricsManager.getListTemplateMetricsInfo(param, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getListIndexMetricsInfoTest() {
        // Setup
        final MetricsDashboardListDTO param = new MetricsDashboardListDTO("aggType", false, Arrays.asList("value"));
        final Result<List<MetricListVO>> expectedResult = Result.buildFail(Arrays.asList(
            new MetricListVO(0L, "type", Arrays.asList(new MetricListContentVO("clusterPhyName", "name", 0.0)))));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure DashBoardMetricsService.getListFaultMetrics(...).
        final MetricList metricList = new MetricList(0L, "type",
            Arrays.asList(new MetricListContent("clusterPhyName", "name", 0.0)));
        when(dashBoardMetricsService.getListFaultMetrics("oneLevelType", "metricsType", "aggType", false))
            .thenReturn(metricList);

        // Configure DashBoardMetricsService.getListValueMetrics(...).
        final MetricList metricList1 = new MetricList(0L, "type",
            Arrays.asList(new MetricListContent("clusterPhyName", "name", 0.0)));
        when(dashBoardMetricsService.getListValueMetrics("oneLevelType", "metricsType", "aggType", false))
            .thenReturn(metricList1);

        when(ariusConfigInfoService.stringSetting("dashboard.threshold", "type", "defaultValue")).thenReturn("result");

        // Run the test
        final Result<List<MetricListVO>> result = dashboardMetricsManager.getListIndexMetricsInfo(param, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
