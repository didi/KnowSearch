package com.didichuxing.datachannel.arius.admin.biz.metrics;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.biz.metrics.impl.GatewayMetricsManagerImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.GatewayOverviewMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.gateway.GatewayOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.MetricsContentCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.MetricsContentVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.metadata.service.GatewayMetricsService;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
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
class GatewayMetricsManagerTest {

    @Mock
    private GatewayMetricsService     gatewayMetricsService;
    @Mock
    private GatewayManager            gatewayManager;
    @Mock
    private ProjectService            projectService;
    @Mock
    private TemplateLogicManager      templateLogicManager;

    @InjectMocks
    private GatewayMetricsManagerImpl gatewayMetricsManager;

    @Test
    void getGatewayMetricsEnumsTest() {
        // Setup
        final Result<List<String>> expectedResult = Result.buildFail(Arrays.asList("value"));

        // Run the test
        final Result<List<String>> result = gatewayMetricsManager.getGatewayMetricsEnums("group");

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getDslMd5ListTest() {
        // Setup
        final Result<List<String>> expectedResult = Result.buildFail(Arrays.asList("value"));
        when(gatewayMetricsService.getDslMd5List(0L, 0L, 0)).thenReturn(Arrays.asList("value"));

        // Run the test
        final Result<List<String>> result = gatewayMetricsManager.getDslMd5List(0, 0L, 0L);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getDslMd5ListGatewayMetricsServiceReturnsNoItemsTest() {
        // Setup
        when(gatewayMetricsService.getDslMd5List(0L, 0L, 0)).thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<String>> result = gatewayMetricsManager.getDslMd5List(0, 0L, 0L);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getGatewayOverviewMetricsTest() {
        // Setup
        final GatewayOverviewDTO dto = new GatewayOverviewDTO();
        dto.setStartTime(0L);
        dto.setEndTime(0L);
        dto.setMetricsTypes(Arrays.asList("value"));

        final Result<List<GatewayOverviewMetricsVO>> expectedResult = Result.buildFail(
            Arrays.asList(new GatewayOverviewMetricsVO("type", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))));

        // Configure GatewayMetricsService.getOverviewCommonMetrics(...).
        final List<GatewayOverviewMetrics> gatewayOverviewMetrics = Arrays
            .asList(new GatewayOverviewMetrics("type", Arrays.asList(new MetricsContentCell(0.0, 0L))));
        when(gatewayMetricsService.getOverviewCommonMetrics(Arrays.asList("value"), 0L, 0L))
            .thenReturn(gatewayOverviewMetrics);

        // Configure GatewayMetricsService.getOverviewWriteMetrics(...).
        final List<GatewayOverviewMetrics> gatewayOverviewMetrics1 = Arrays
            .asList(new GatewayOverviewMetrics("type", Arrays.asList(new MetricsContentCell(0.0, 0L))));
        when(gatewayMetricsService.getOverviewWriteMetrics(Arrays.asList("value"), 0L, 0L))
            .thenReturn(gatewayOverviewMetrics1);

        // Configure GatewayMetricsService.getOverviewReadCountMetrics(...).
        final GatewayOverviewMetrics gatewayOverviewMetrics2 = new GatewayOverviewMetrics("type",
            Arrays.asList(new MetricsContentCell(0.0, 0L)));
        when(gatewayMetricsService.getOverviewReadCountMetrics(0L, 0L)).thenReturn(gatewayOverviewMetrics2);

        // Configure GatewayMetricsService.getOverviewSearchTypeMetrics(...).
        final GatewayOverviewMetrics gatewayOverviewMetrics3 = new GatewayOverviewMetrics("type",
            Arrays.asList(new MetricsContentCell(0.0, 0L)));
        when(gatewayMetricsService.getOverviewSearchTypeMetrics(0L, 0L)).thenReturn(gatewayOverviewMetrics3);

        // Run the test
        final Result<List<GatewayOverviewMetricsVO>> result = gatewayMetricsManager.getGatewayOverviewMetrics(dto);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayOverviewMetricsGatewayMetricsServiceGetOverviewCommonMetricsReturnsNoItemsTest() {
        // Setup
        final GatewayOverviewDTO dto = new GatewayOverviewDTO();
        dto.setStartTime(0L);
        dto.setEndTime(0L);
        dto.setMetricsTypes(Arrays.asList("value"));

        final Result<List<GatewayOverviewMetricsVO>> expectedResult = Result.buildFail(
            Arrays.asList(new GatewayOverviewMetricsVO("type", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))));
        when(gatewayMetricsService.getOverviewCommonMetrics(Arrays.asList("value"), 0L, 0L))
            .thenReturn(Collections.emptyList());

        // Configure GatewayMetricsService.getOverviewWriteMetrics(...).
        final List<GatewayOverviewMetrics> gatewayOverviewMetrics = Arrays
            .asList(new GatewayOverviewMetrics("type", Arrays.asList(new MetricsContentCell(0.0, 0L))));
        when(gatewayMetricsService.getOverviewWriteMetrics(Arrays.asList("value"), 0L, 0L))
            .thenReturn(gatewayOverviewMetrics);

        // Configure GatewayMetricsService.getOverviewReadCountMetrics(...).
        final GatewayOverviewMetrics gatewayOverviewMetrics1 = new GatewayOverviewMetrics("type",
            Arrays.asList(new MetricsContentCell(0.0, 0L)));
        when(gatewayMetricsService.getOverviewReadCountMetrics(0L, 0L)).thenReturn(gatewayOverviewMetrics1);

        // Configure GatewayMetricsService.getOverviewSearchTypeMetrics(...).
        final GatewayOverviewMetrics gatewayOverviewMetrics2 = new GatewayOverviewMetrics("type",
            Arrays.asList(new MetricsContentCell(0.0, 0L)));
        when(gatewayMetricsService.getOverviewSearchTypeMetrics(0L, 0L)).thenReturn(gatewayOverviewMetrics2);

        // Run the test
        final Result<List<GatewayOverviewMetricsVO>> result = gatewayMetricsManager.getGatewayOverviewMetrics(dto);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayOverviewMetricsGatewayMetricsServiceGetOverviewWriteMetricsReturnsNoItemsTest() {
        // Setup
        final GatewayOverviewDTO dto = new GatewayOverviewDTO();
        dto.setStartTime(0L);
        dto.setEndTime(0L);
        dto.setMetricsTypes(Arrays.asList("value"));

        final Result<List<GatewayOverviewMetricsVO>> expectedResult = Result.buildFail(
            Arrays.asList(new GatewayOverviewMetricsVO("type", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))));

        // Configure GatewayMetricsService.getOverviewCommonMetrics(...).
        final List<GatewayOverviewMetrics> gatewayOverviewMetrics = Arrays
            .asList(new GatewayOverviewMetrics("type", Arrays.asList(new MetricsContentCell(0.0, 0L))));
        when(gatewayMetricsService.getOverviewCommonMetrics(Arrays.asList("value"), 0L, 0L))
            .thenReturn(gatewayOverviewMetrics);

        when(gatewayMetricsService.getOverviewWriteMetrics(Arrays.asList("value"), 0L, 0L))
            .thenReturn(Collections.emptyList());

        // Configure GatewayMetricsService.getOverviewReadCountMetrics(...).
        final GatewayOverviewMetrics gatewayOverviewMetrics1 = new GatewayOverviewMetrics("type",
            Arrays.asList(new MetricsContentCell(0.0, 0L)));
        when(gatewayMetricsService.getOverviewReadCountMetrics(0L, 0L)).thenReturn(gatewayOverviewMetrics1);

        // Configure GatewayMetricsService.getOverviewSearchTypeMetrics(...).
        final GatewayOverviewMetrics gatewayOverviewMetrics2 = new GatewayOverviewMetrics("type",
            Arrays.asList(new MetricsContentCell(0.0, 0L)));
        when(gatewayMetricsService.getOverviewSearchTypeMetrics(0L, 0L)).thenReturn(gatewayOverviewMetrics2);

        // Run the test
        final Result<List<GatewayOverviewMetricsVO>> result = gatewayMetricsManager.getGatewayOverviewMetrics(dto);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayNodeMetricsTest() {
        // Setup
        final GatewayNodeDTO dto = new GatewayNodeDTO("nodeIp", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics3);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics4 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics4);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics5 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics5);

        when(gatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(Result.buildFail(Arrays.asList("value")));

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayNodeMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayNodeMetricsGatewayManagerReturnsNoItemTest() {
        // Setup
        final GatewayNodeDTO dto = new GatewayNodeDTO("nodeIp", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics3);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics4 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics4);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics5 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics5);

        when(gatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(Result.buildSucc());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayNodeMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayNodeMetricsGatewayManagerReturnsNoItemsTest() {
        // Setup
        final GatewayNodeDTO dto = new GatewayNodeDTO("nodeIp", 0);

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics3);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics4 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics4);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics5 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics5);

        when(gatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(Result.buildFail(Collections.emptyList()));

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayNodeMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getGatewayNodeMetricsGatewayManagerReturnsFailureTest() {
        // Setup
        final GatewayNodeDTO dto = new GatewayNodeDTO("nodeIp", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics3);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics4 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics4);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics5 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics5);

        when(gatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(Result.buildFail());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayNodeMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiGatewayNodesMetricsTest() {
        // Setup
        final MultiGatewayNodesDTO dto = new MultiGatewayNodesDTO(Arrays.asList("value"), 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics3);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics4 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics4);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics5 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics5);

        when(gatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(Result.buildFail(Arrays.asList("value")));

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getMultiGatewayNodesMetrics(dto,
            0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiGatewayNodesMetricsGatewayManagerReturnsNoItemTest() {
        // Setup
        final MultiGatewayNodesDTO dto = new MultiGatewayNodesDTO(Arrays.asList("value"), 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics3);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics4 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics4);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics5 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics5);

        when(gatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(Result.buildSucc());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getMultiGatewayNodesMetrics(dto,
            0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiGatewayNodesMetricsGatewayManagerReturnsNoItemsTest() {
        // Setup
        final MultiGatewayNodesDTO dto = new MultiGatewayNodesDTO(Arrays.asList("value"), 0);

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics3);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics4 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics4);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics5 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics5);

        when(gatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(Result.buildFail(Collections.emptyList()));

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getMultiGatewayNodesMetrics(dto,
            0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getMultiGatewayNodesMetricsGatewayManagerReturnsFailureTest() {
        // Setup
        final MultiGatewayNodesDTO dto = new MultiGatewayNodesDTO(Arrays.asList("value"), 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, "nodeIp")).thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getGatewayNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeWriteMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics3);

        // Configure GatewayMetricsService.getGatewayNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics4 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics4);

        // Configure GatewayMetricsService.getGatewayNodeDSLLenMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics5 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getGatewayNodeDSLLenMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics5);

        when(gatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(Result.buildFail());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getMultiGatewayNodesMetrics(dto,
            0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClientNodeMetricsTest() {
        // Setup
        final ClientNodeDTO dto = new ClientNodeDTO("clientNodeIp");
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getClientNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeWriteMetrics(0L, 0L, 0, "nodeIp", "clientNodeIp"))
            .thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getClientNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeMetrics(0L, 0L, 0, "nodeIp", "clientNodeIp"))
            .thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getClientNodeDSLLENMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeDSLLENMetrics(0L, 0L, 0, "nodeIp", "clientNodeIp"))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getClientNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeWriteMetrics(0L, 0L, 0, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics3);

        // Configure GatewayMetricsService.getClientNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics4 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeMetrics(0L, 0L, 0, 0, "nodeIp")).thenReturn(variousLineChartMetrics4);

        // Configure GatewayMetricsService.getClientNodeDSLLENMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics5 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeDSLLENMetrics(0L, 0L, 0, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics5);

        when(gatewayMetricsService.getEsClientNodeIpListByGatewayNode("nodeIp", 0L, 0L, 0))
            .thenReturn(Arrays.asList("value"));

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getClientNodeMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClientNodeMetricsGatewayMetricsServiceGetEsClientNodeIpListByGatewayNodeReturnsNoItemsTest() {
        // Setup
        final ClientNodeDTO dto = new ClientNodeDTO("clientNodeIp");

        // Configure GatewayMetricsService.getClientNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeWriteMetrics(0L, 0L, 0, "nodeIp", "clientNodeIp"))
            .thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getClientNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeMetrics(0L, 0L, 0, "nodeIp", "clientNodeIp"))
            .thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getClientNodeDSLLENMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeDSLLENMetrics(0L, 0L, 0, "nodeIp", "clientNodeIp"))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getClientNodeWriteMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeWriteMetrics(0L, 0L, 0, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics3);

        // Configure GatewayMetricsService.getClientNodeMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics4 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeMetrics(0L, 0L, 0, 0, "nodeIp")).thenReturn(variousLineChartMetrics4);

        // Configure GatewayMetricsService.getClientNodeDSLLENMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics5 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getClientNodeDSLLENMetrics(0L, 0L, 0, 0, "nodeIp"))
            .thenReturn(variousLineChartMetrics5);

        when(gatewayMetricsService.getEsClientNodeIpListByGatewayNode("nodeIp", 0L, 0L, 0))
            .thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getClientNodeMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getGatewayIndexMetricsTest() {
        // Setup
        final GatewayIndexDTO dto = new GatewayIndexDTO("indexName", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getGatewayIndexWriteMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexWriteMetrics(Arrays.asList("value"), 0L, 0L, 0, "indexName"))
            .thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayIndexSearchMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics1 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexSearchMetrics(Arrays.asList("value"), 0L, 0L, 0, "indexName"))
            .thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayIndexWriteMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics2 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexWriteMetrics(Arrays.asList("value"), 0L, 0L, 0, 0))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getGatewayIndexSearchMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics3 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexSearchMetrics(Arrays.asList("value"), 0L, 0L, 0, 0))
            .thenReturn(variousLineChartMetrics3);

        when(templateLogicManager.getTemplateLogicNames(0)).thenReturn(Arrays.asList("value"));

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayIndexMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayIndexMetricsGatewayMetricsServiceGetGatewayIndexWriteMetricsReturnsNoItemsTest() {
        // Setup
        final GatewayIndexDTO dto = new GatewayIndexDTO("indexName", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getGatewayIndexWriteMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexWriteMetrics(Arrays.asList("value"), 0L, 0L, 0, "indexName"))
            .thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayIndexSearchMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics1 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexSearchMetrics(Arrays.asList("value"), 0L, 0L, 0, "indexName"))
            .thenReturn(variousLineChartMetrics1);

        when(gatewayMetricsService.getGatewayIndexWriteMetrics(Arrays.asList("value"), 0L, 0L, 0, 0))
            .thenReturn(Collections.emptyList());

        // Configure GatewayMetricsService.getGatewayIndexSearchMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics2 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexSearchMetrics(Arrays.asList("value"), 0L, 0L, 0, 0))
            .thenReturn(variousLineChartMetrics2);

        when(templateLogicManager.getTemplateLogicNames(0)).thenReturn(Arrays.asList("value"));

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayIndexMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayIndexMetricsGatewayMetricsServiceGetGatewayIndexSearchMetricsReturnsNoItemsTest() {
        // Setup
        final GatewayIndexDTO dto = new GatewayIndexDTO("indexName", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getGatewayIndexWriteMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexWriteMetrics(Arrays.asList("value"), 0L, 0L, 0, "indexName"))
            .thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayIndexSearchMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics1 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexSearchMetrics(Arrays.asList("value"), 0L, 0L, 0, "indexName"))
            .thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayIndexWriteMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics2 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexWriteMetrics(Arrays.asList("value"), 0L, 0L, 0, 0))
            .thenReturn(variousLineChartMetrics2);

        when(gatewayMetricsService.getGatewayIndexSearchMetrics(Arrays.asList("value"), 0L, 0L, 0, 0))
            .thenReturn(Collections.emptyList());
        when(templateLogicManager.getTemplateLogicNames(0)).thenReturn(Arrays.asList("value"));

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayIndexMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayIndexMetricsTemplateLogicManagerReturnsNoItemsTest() {
        // Setup
        final GatewayIndexDTO dto = new GatewayIndexDTO("indexName", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getGatewayIndexWriteMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexWriteMetrics(Arrays.asList("value"), 0L, 0L, 0, "indexName"))
            .thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getGatewayIndexSearchMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics1 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexSearchMetrics(Arrays.asList("value"), 0L, 0L, 0, "indexName"))
            .thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getGatewayIndexWriteMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics2 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexWriteMetrics(Arrays.asList("value"), 0L, 0L, 0, 0))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getGatewayIndexSearchMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics3 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getGatewayIndexSearchMetrics(Arrays.asList("value"), 0L, 0L, 0, 0))
            .thenReturn(variousLineChartMetrics3);

        when(templateLogicManager.getTemplateLogicNames(0)).thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayIndexMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayAppMetricsTest() {
        // Setup
        final GatewayProjectDTO dto = new GatewayProjectDTO("projectId", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getAppCommonMetricsByProjectId(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getAppCommonMetricsByProjectId(0L, 0L, Arrays.asList("value"), "projectId"))
            .thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getAppCountMetricsByProjectId(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getAppCountMetricsByProjectId(0L, 0L, "projectId"))
            .thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getAppCommonMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics2 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getAppCommonMetrics(0L, 0L, Arrays.asList("value"), 0))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getAppCountMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getAppCountMetrics(0L, 0L, 0)).thenReturn(variousLineChartMetrics3);

        // Configure ProjectService.getProjectBriefList(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        final List<ProjectBriefVO> projectBriefVOS = Arrays.asList(projectBriefVO);
        when(projectService.getProjectBriefList()).thenReturn(projectBriefVOS);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayAppMetrics(dto);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayAppMetricsGatewayMetricsServiceGetAppCommonMetricsByProjectIdReturnsNoItemsTest() {
        // Setup
        final GatewayProjectDTO dto = new GatewayProjectDTO("projectId", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));
        when(gatewayMetricsService.getAppCommonMetricsByProjectId(0L, 0L, Arrays.asList("value"), "projectId"))
            .thenReturn(Collections.emptyList());

        // Configure GatewayMetricsService.getAppCountMetricsByProjectId(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getAppCountMetricsByProjectId(0L, 0L, "projectId"))
            .thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getAppCommonMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics1 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getAppCommonMetrics(0L, 0L, Arrays.asList("value"), 0))
            .thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getAppCountMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getAppCountMetrics(0L, 0L, 0)).thenReturn(variousLineChartMetrics2);

        // Configure ProjectService.getProjectBriefList(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        final List<ProjectBriefVO> projectBriefVOS = Arrays.asList(projectBriefVO);
        when(projectService.getProjectBriefList()).thenReturn(projectBriefVOS);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayAppMetrics(dto);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayAppMetricsGatewayMetricsServiceGetAppCommonMetricsReturnsNoItemsTest() {
        // Setup
        final GatewayProjectDTO dto = new GatewayProjectDTO("projectId", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getAppCommonMetricsByProjectId(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getAppCommonMetricsByProjectId(0L, 0L, Arrays.asList("value"), "projectId"))
            .thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getAppCountMetricsByProjectId(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getAppCountMetricsByProjectId(0L, 0L, "projectId"))
            .thenReturn(variousLineChartMetrics1);

        when(gatewayMetricsService.getAppCommonMetrics(0L, 0L, Arrays.asList("value"), 0))
            .thenReturn(Collections.emptyList());

        // Configure GatewayMetricsService.getAppCountMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getAppCountMetrics(0L, 0L, 0)).thenReturn(variousLineChartMetrics2);

        // Configure ProjectService.getProjectBriefList(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        final List<ProjectBriefVO> projectBriefVOS = Arrays.asList(projectBriefVO);
        when(projectService.getProjectBriefList()).thenReturn(projectBriefVOS);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayAppMetrics(dto);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayAppMetricsProjectServiceReturnsNoItemsTest() {
        // Setup
        final GatewayProjectDTO dto = new GatewayProjectDTO("projectId", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getAppCommonMetricsByProjectId(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getAppCommonMetricsByProjectId(0L, 0L, Arrays.asList("value"), "projectId"))
            .thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getAppCountMetricsByProjectId(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getAppCountMetricsByProjectId(0L, 0L, "projectId"))
            .thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getAppCommonMetrics(...).
        final List<VariousLineChartMetrics> variousLineChartMetrics2 = Arrays.asList(new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0))));
        when(gatewayMetricsService.getAppCommonMetrics(0L, 0L, Arrays.asList("value"), 0))
            .thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getAppCountMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getAppCountMetrics(0L, 0L, 0)).thenReturn(variousLineChartMetrics3);

        when(projectService.getProjectBriefList()).thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayAppMetrics(dto);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayDslMetricsTest() {
        // Setup
        final GatewayDslDTO dto = new GatewayDslDTO("dslMd5", 0);
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure GatewayMetricsService.getDslCountMetricsByMd5(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getDslCountMetricsByMd5(0L, 0L, "dslMd5", 0)).thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getDslTotalCostMetricsByMd5(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getDslTotalCostMetricsByMd5(0L, 0L, "dslMd5", 0))
            .thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getDslCountMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getDslCountMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getDslTotalCostMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getDslTotalCostMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics3);

        when(gatewayMetricsService.getDslMd5List(0L, 0L, 0)).thenReturn(Arrays.asList("value"));

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayDslMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getGatewayDslMetricsGatewayMetricsServiceGetDslMd5ListReturnsNoItemsTest() {
        // Setup
        final GatewayDslDTO dto = new GatewayDslDTO("dslMd5", 0);

        // Configure GatewayMetricsService.getDslCountMetricsByMd5(...).
        final VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getDslCountMetricsByMd5(0L, 0L, "dslMd5", 0)).thenReturn(variousLineChartMetrics);

        // Configure GatewayMetricsService.getDslTotalCostMetricsByMd5(...).
        final VariousLineChartMetrics variousLineChartMetrics1 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getDslTotalCostMetricsByMd5(0L, 0L, "dslMd5", 0))
            .thenReturn(variousLineChartMetrics1);

        // Configure GatewayMetricsService.getDslCountMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics2 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getDslCountMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics2);

        // Configure GatewayMetricsService.getDslTotalCostMetrics(...).
        final VariousLineChartMetrics variousLineChartMetrics3 = new VariousLineChartMetrics("type",
            Arrays.asList(new MetricsContent("cluster", "name", Arrays.asList(new MetricsContentCell(0.0, 0L)), 0.0)));
        when(gatewayMetricsService.getDslTotalCostMetrics(0L, 0L, 0, 0)).thenReturn(variousLineChartMetrics3);

        when(gatewayMetricsService.getDslMd5List(0L, 0L, 0)).thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = gatewayMetricsManager.getGatewayDslMetrics(dto, 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getClientNodeIdListTest() {
        // Setup
        final Result<List<String>> expectedResult = Result.buildFail(Arrays.asList("value"));
        when(gatewayMetricsService.getEsClientNodeIpListByGatewayNode("gatewayNode", 0L, 0L, 0))
            .thenReturn(Arrays.asList("value"));

        // Run the test
        final Result<List<String>> result = gatewayMetricsManager.getClientNodeIdList("gatewayNode", 0L, 0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClientNodeIdListGatewayMetricsServiceReturnsNoItemsTest() {
        // Setup
        when(gatewayMetricsService.getEsClientNodeIpListByGatewayNode("gatewayNode", 0L, 0L, 0))
            .thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<String>> result = gatewayMetricsManager.getClientNodeIdList("gatewayNode", 0L, 0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }
}
