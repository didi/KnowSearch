package com.didichuxing.datachannel.arius.admin.biz.metrics;

import com.didichuxing.datachannel.arius.admin.biz.metrics.impl.ClusterPhyMetricsManagerImpl;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MultiMetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ESClusterTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.MetricsContentCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.MetricsContentVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.MetricsConfigService;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatisService;
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
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpringTool.class})
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ClusterPhyMetricsManagerTest {

    @Mock
    private AppService appService;
    @Mock
    private MetricsConfigService metricsConfigService;
    @Mock
    private ESIndexService esIndexService;
    @Mock
    private NodeStatisService nodeStatisService;
    @Mock
    private HandleFactory handleFactory;
    @Mock
    private ClusterLogicService clusterLogicService;
    @Mock
    private ClusterRegionService clusterRegionService;

    @InjectMocks
    private ClusterPhyMetricsManagerImpl clusterPhyMetricsManager;

    @Test
    void getMetricsCode2TypeMap() throws Exception {
        assertThat(clusterPhyMetricsManager.getMetricsCode2TypeMap("type")).isEqualTo(Arrays.asList("value"));
        assertThat(clusterPhyMetricsManager.getMetricsCode2TypeMap("type")).isEqualTo(Collections.emptyList());
    }

    @Test
    void getClusterMetricsByMetricsTypeTest() {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
                "aggType", Arrays.asList("value"), 0, 0, "topMethod");

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicByName("clusterLogicName")).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result result = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0, "domainAccount",
                ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getClusterMetricsByMetricsTypeClusterRegionServiceReturnsNullTest() {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
                "aggType", Arrays.asList("value"), 0, 0, "topMethod");

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicByName("clusterLogicName")).thenReturn(clusterLogic);

        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Run the test
        final Result result = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0, "domainAccount",
                ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getMultiClusterMetricsTest() {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result.buildFail(
                Arrays.asList(new VariousLineChartMetricsVO("type", Arrays.asList(
                        new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicByName("clusterLogicName")).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = clusterPhyMetricsManager.getMultiClusterMetrics(param, 0,
                "domainAccount",
                ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiClusterMetricsClusterRegionServiceReturnsNullTest() {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result.buildFail(
                Arrays.asList(new VariousLineChartMetricsVO("type", Arrays.asList(
                        new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicByName("clusterLogicName")).thenReturn(clusterLogic);

        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = clusterPhyMetricsManager.getMultiClusterMetrics(param, 0,
                "domainAccount",
                ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getDomainAccountConfigMetricsTest() {
        // Setup
        final MetricsConfigInfoDTO metricsConfigInfoDTO = new MetricsConfigInfoDTO("domainAccount", "firstMetricsType",
                "secondMetricsType", Arrays.asList("value"));
        when(metricsConfigService.getMetricsByTypeAndDomainAccount(
                new MetricsConfigInfoDTO("domainAccount", "firstMetricsType", "secondMetricsType",
                        Arrays.asList("value")))).thenReturn(Arrays.asList("value"));

        // Run the test
        final List<String> result = clusterPhyMetricsManager.getDomainAccountConfigMetrics(metricsConfigInfoDTO,
                "domainAccount");

        // Verify the results
        assertThat(result).isEqualTo(Arrays.asList("value"));
    }

    @Test
    void getDomainAccountConfigMetricsMetricsConfigServiceReturnsNoItemsTest() {
        // Setup
        final MetricsConfigInfoDTO metricsConfigInfoDTO = new MetricsConfigInfoDTO("domainAccount", "firstMetricsType",
                "secondMetricsType", Arrays.asList("value"));
        when(metricsConfigService.getMetricsByTypeAndDomainAccount(
                new MetricsConfigInfoDTO("domainAccount", "firstMetricsType", "secondMetricsType",
                        Arrays.asList("value")))).thenReturn(Collections.emptyList());

        // Run the test
        final List<String> result = clusterPhyMetricsManager.getDomainAccountConfigMetrics(metricsConfigInfoDTO,
                "domainAccount");

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void updateDomainAccountConfigMetricsTest() {
        // Setup
        final MetricsConfigInfoDTO param = new MetricsConfigInfoDTO("domainAccount", "firstMetricsType",
                "secondMetricsType", Arrays.asList("value"));
        final Result<Integer> expectedResult = Result.buildFail(0);
        when(metricsConfigService.updateByMetricsByTypeAndDomainAccount(
                new MetricsConfigInfoDTO("domainAccount", "firstMetricsType", "secondMetricsType",
                        Arrays.asList("value")))).thenReturn(Result.buildFail(0));

        // Run the test
        final Result<Integer> result = clusterPhyMetricsManager.updateDomainAccountConfigMetrics(param,
                "domainAccount");

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void updateDomainAccountConfigMetricsMetricsConfigServiceReturnsNoItemTest() {
        // Setup
        final MetricsConfigInfoDTO param = new MetricsConfigInfoDTO("domainAccount", "firstMetricsType",
                "secondMetricsType", Arrays.asList("value"));
        when(metricsConfigService.updateByMetricsByTypeAndDomainAccount(
                new MetricsConfigInfoDTO("domainAccount", "firstMetricsType", "secondMetricsType",
                        Arrays.asList("value")))).thenReturn(Result.buildSucc());

        // Run the test
        final Result<Integer> result = clusterPhyMetricsManager.updateDomainAccountConfigMetrics(param,
                "domainAccount");

        // Verify the results
        assertThat(result).isEqualTo(Result.buildSucc());
    }

    @Test
    void updateDomainAccountConfigMetricsMetricsConfigServiceReturnsFailureTest() {
        // Setup
        final MetricsConfigInfoDTO param = new MetricsConfigInfoDTO("domainAccount", "firstMetricsType",
                "secondMetricsType", Arrays.asList("value"));
        final Result<Integer> expectedResult = Result.buildFail(0);
        when(metricsConfigService.updateByMetricsByTypeAndDomainAccount(
                new MetricsConfigInfoDTO("domainAccount", "firstMetricsType", "secondMetricsType",
                        Arrays.asList("value")))).thenReturn(Result.buildFail());

        // Run the test
        final Result<Integer> result = clusterPhyMetricsManager.updateDomainAccountConfigMetrics(param,
                "domainAccount");

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterPhyTaskDetailTest() {
        // Setup
        final Result<List<ESClusterTaskDetailVO>> expectedResult = Result.buildFail(
                Arrays.asList(new ESClusterTaskDetailVO("taskId", "node", "action", 0L, 0L, "runningTimeString",
                        "description")));
        when(appService.isAppExists(0)).thenReturn(false);

        // Configure NodeStatisService.getClusterTaskDetail(...).
        final List<ESClusterTaskDetail> esClusterTaskDetails = Arrays.asList(
                new ESClusterTaskDetail("taskId", "node", "action", 0L, 0L, "runningTimeString", "description"));
        when(nodeStatisService.getClusterTaskDetail("clusterPhyName", "node", 0L, 0L)).thenReturn(esClusterTaskDetails);

        // Run the test
        final Result<List<ESClusterTaskDetailVO>> result = clusterPhyMetricsManager.getClusterPhyTaskDetail(
                "clusterPhyName", "node", "startTime", "endTime", 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterPhyTaskDetailNodeStatisServiceReturnsNoItemsTest() {
        // Setup
        when(appService.isAppExists(0)).thenReturn(false);
        when(nodeStatisService.getClusterTaskDetail("clusterPhyName", "node", 0L, 0L))
                .thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<ESClusterTaskDetailVO>> result = clusterPhyMetricsManager.getClusterPhyTaskDetail(
                "clusterPhyName", "node", "startTime", "endTime", 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }
}
