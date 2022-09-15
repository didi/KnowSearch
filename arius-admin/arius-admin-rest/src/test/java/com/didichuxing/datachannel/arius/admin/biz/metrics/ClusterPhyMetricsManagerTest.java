package com.didichuxing.datachannel.arius.admin.biz.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.didichuxing.datachannel.arius.admin.biz.metrics.impl.ClusterPhyMetricsManagerImpl;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.UserConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MultiMetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ESClusterTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.MetricsContentCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.MetricsContentVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.UserConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatsService;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.didiglobal.logi.security.util.HttpRequestUtil;
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

@ActiveProfiles("test")
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SpringTool.class })
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ClusterPhyMetricsManagerTest {

    @Mock
    private ProjectService               projectService;
    @Mock
    private UserConfigService     userConfigService;
    @Mock
    private NodeStatsService             nodeStatsService;
    @Mock
    private HandleFactory                handleFactory;
    @Mock
    private ClusterLogicService          clusterLogicService;
    @Mock
    private ClusterRegionService         clusterRegionService;
    @Mock
    private ClusterRoleHostService       clusterRoleHostService;
    @Mock
    private IndexTemplateService         indexTemplateService;
    @Mock
    private ESIndexService               esIndexService;

    @InjectMocks
    private ClusterPhyMetricsManagerImpl clusterPhyMetricsManager;

    @Test
    void getMetricsCode2TypeMap() throws Exception {
        assertThat(clusterPhyMetricsManager.getMetricsCode2TypeMap("type")).isEqualTo(Arrays.asList("value"));
        assertThat(clusterPhyMetricsManager.getMetricsCode2TypeMap("type")).isEqualTo(Collections.emptyList());
    }

    @Test
    void getClusterMetricsByMetricsTypeTest() throws NotFindSubclassException {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
            "aggType", Arrays.asList("value"), 0, 0, "topMethod", Arrays.asList("value"),null);

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic  = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);

        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Arrays.asList(new ClusterRoleHost(0L, 0L,
            "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(
            Arrays.asList(new IndexTemplate(0, "name", 0, 0, "dateFormat", "dataCenter", 0, 0, 0, "dateField", "dateFieldFormat", "idField", "routingField",
                "expression", 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0, 0.0,1)));
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result resultClusterMetrics = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0,
            "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getClusterMetricsByMetricsTypeClusterRegionServiceReturnsNullTest() {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
            "aggType", Arrays.asList("value"), 0, 0, "topMethod", Arrays.asList("value"),null);

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,  "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Run the test
        final Result result = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0, "userName",
            ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getClusterMetricsByMetricsTypeClusterRoleHostServiceReturnsNoItemTest() throws NotFindSubclassException {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
            "aggType", Arrays.asList("value"), 0, 0, "topMethod", Arrays.asList("value"),null);

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
            "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(clusterRoleHostService.listByRegionId(0)).thenReturn(Result.buildSucc());

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(
            Arrays.asList(new IndexTemplate(0, "name", 0, 0, "dateFormat", "dataCenter", 0, 0, 0,  "dateField", "dateFieldFormat", "idField", "routingField",
                "expression", 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0, 0.0,1)));
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result result = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0, "userName",
            ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getClusterMetricsByMetricsTypeClusterRoleHostServiceReturnsNoItemsTest() throws NotFindSubclassException {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
            "aggType", Arrays.asList("value"), 0, 0, "topMethod", Arrays.asList("value"),null);

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Collections.emptyList());
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(
            Arrays.asList(new IndexTemplate(0, "name", 0, 0, "dateFormat", "dataCenter", 0, 0, 0, "dateField", "dateFieldFormat", "idField", "routingField",
                "expression", 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0, 0.0,1)));
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result resultClusterMetrics = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0,
            "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getClusterMetricsByMetricsTypeClusterRoleHostServiceReturnsFailureTest() throws NotFindSubclassException {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
            "aggType", Arrays.asList("value"), 0, 0, "topMethod", Arrays.asList("value"),null);

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail();
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(
            Arrays.asList(new IndexTemplate(0, "name", 0, 0, "dateFormat", "dataCenter", 0, 0, 0,  "dateField", "dateFieldFormat", "idField", "routingField",
                "expression", 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0, 0.0,1)));
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result resultClusterMetrics = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0,
            "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getClusterMetricsByMetricsTypeIndexTemplateServiceReturnsNoItemTest() throws NotFindSubclassException {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
            "aggType", Arrays.asList("value"), 0, 0, "topMethod", Arrays.asList("value"),null);

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,  "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Arrays.asList(new ClusterRoleHost(0L, 0L,
            "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(indexTemplateService.listByRegionId(0)).thenReturn(Result.buildSucc());

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result resultClusterMetrics = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0,
            "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getClusterMetricsByMetricsTypeIndexTemplateServiceReturnsNoItemsTest() throws NotFindSubclassException {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
            "aggType", Arrays.asList("value"), 0, 0, "topMethod", Arrays.asList("value"),null);

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,  "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Arrays.asList(new ClusterRoleHost(0L, 0L,
            "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(Collections.emptyList());
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result resultClusterMetrics = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0,
            "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getClusterMetricsByMetricsTypeIndexTemplateServiceReturnsFailureTest() throws NotFindSubclassException {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
            "aggType", Arrays.asList("value"), 0, 0, "topMethod", Arrays.asList("value"),null);

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,  "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Arrays.asList(new ClusterRoleHost(0L, 0L,
            "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail();
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result resultClusterMetrics = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0,
            "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getClusterMetricsByMetricsTypeESIndexServiceReturnsNoItemsTest() throws NotFindSubclassException {
        // Setup
        final MetricsClusterPhyDTO param = new MetricsClusterPhyDTO("clusterPhyName", "clusterLogicName", 0L, 0L,
            "aggType", Arrays.asList("value"), 0, 0, "topMethod", Arrays.asList("value"),null);

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Arrays.asList(new ClusterRoleHost(0L, 0L,
            "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(
            Arrays.asList(new IndexTemplate(0, "name", 0, 0, "dateFormat", "dataCenter", 0, 0, 0,  "dateField", "dateFieldFormat", "idField", "routingField",
                "expression", 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0, 0.0,1)));
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression"))
            .thenReturn(Collections.emptyList());
        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result resultClusterMetrics = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 0,
            "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
    }

    @Test
    void getMultiClusterMetricsTest() throws NotFindSubclassException {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Arrays.asList(new ClusterRoleHost(0L, 0L,
            "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(
            Arrays.asList(new IndexTemplate(0, "name", 0, 0, "dateFormat", "dataCenter", 0, 0, 0,  "dateField", "dateFieldFormat", "idField", "routingField",
                "expression", 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0, 0.0,1)));
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> resultClusterMetrics = clusterPhyMetricsManager
            .getMultiClusterMetrics(param, 0, "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiClusterMetricsClusterRegionServiceReturnsNullTest() {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = clusterPhyMetricsManager.getMultiClusterMetrics(param, 0,
            "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiClusterMetricsClusterRoleHostServiceReturnsNoItemTest() throws NotFindSubclassException {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,  "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(clusterRoleHostService.listByRegionId(0)).thenReturn(Result.buildSucc());

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(
            Arrays.asList(new IndexTemplate(0, "name", 0, 0, "dateFormat", "dataCenter", 0, 0, 0, "dateField", "dateFieldFormat", "idField", "routingField",
                "expression", 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0, 0.0,1)));
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> result = clusterPhyMetricsManager.getMultiClusterMetrics(param, 0,
            "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiClusterMetricsClusterRoleHostServiceReturnsNoItemsTest() throws NotFindSubclassException {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,"memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Collections.emptyList());
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(
            Arrays.asList(new IndexTemplate(0, "name", 0, 0, "dateFormat", "dataCenter", 0, 0, 0, "dateField", "dateFieldFormat", "idField", "routingField",
                "expression", 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0, 0.0,1)));
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> resultClusterMetrics = clusterPhyMetricsManager
            .getMultiClusterMetrics(param, 0, "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiClusterMetricsClusterRoleHostServiceReturnsFailureTest() throws NotFindSubclassException {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,  "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail();
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(
            Arrays.asList(new IndexTemplate(0, "name", 0, 0, "dateFormat", "dataCenter", 0, 0, 0, "dateField", "dateFieldFormat", "idField", "routingField",
                "expression", 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0, 0.0,1)));
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> resultClusterMetrics = clusterPhyMetricsManager
            .getMultiClusterMetrics(param, 0, "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiClusterMetricsIndexTemplateServiceReturnsNoItemTest() throws NotFindSubclassException {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Arrays.asList(new ClusterRoleHost(0L, 0L,
            "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(indexTemplateService.listByRegionId(0)).thenReturn(Result.buildSucc());

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> resultClusterMetrics = clusterPhyMetricsManager
            .getMultiClusterMetrics(param, 0, "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiClusterMetricsIndexTemplateServiceReturnsNoItemsTest() throws NotFindSubclassException {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,  "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Arrays.asList(new ClusterRoleHost(0L, 0L,
            "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(Collections.emptyList());
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> resultClusterMetrics = clusterPhyMetricsManager
            .getMultiClusterMetrics(param, 0, "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiClusterMetricsIndexTemplateServiceReturnsFailureTest() throws NotFindSubclassException {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,  "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Arrays.asList(new ClusterRoleHost(0L, 0L,
            "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail();
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        // Configure ESIndexService.syncCatIndexByExpression(...).
        final CatIndexResult catIndexResult = new CatIndexResult();
        catIndexResult.setHealth("health");
        catIndexResult.setStatus("status");
        catIndexResult.setIndex("index");
        catIndexResult.setPri("pri");
        catIndexResult.setRep("rep");
        catIndexResult.setDocsCount("docsCount");
        catIndexResult.setDocsDeleted("docsDeleted");
        catIndexResult.setStoreSize("storeSize");
        catIndexResult.setPriStoreSize("priStoreSize");
        final List<CatIndexResult> catIndexResultList = Arrays.asList(catIndexResult);
        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression")).thenReturn(catIndexResultList);

        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> resultClusterMetrics = clusterPhyMetricsManager
            .getMultiClusterMetrics(param, 0, "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getMultiClusterMetricsESIndexServiceReturnsNoItemsTest() throws NotFindSubclassException {
        // Setup
        final MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO(Arrays.asList("value"));
        final Result<List<VariousLineChartMetricsVO>> expectedResult = Result
            .buildFail(Arrays.asList(new VariousLineChartMetricsVO("type", Arrays
                .asList(new MetricsContentVO("cluster", "name", Arrays.asList(new MetricsContentCellVO(0.0, 0L)))))));

        // Configure ClusterLogicService.getClusterLogicByName(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        when(clusterLogicService.getClusterLogicByNameAndProjectId("clusterLogicName", null)).thenReturn(clusterLogic);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "clusterPhyName",
            "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Arrays.asList(new ClusterRoleHost(0L, 0L,
            "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        // Configure IndexTemplateService.listByRegionId(...).
        final Result<List<IndexTemplate>> listResult = Result.buildFail(
            Arrays.asList(new IndexTemplate(0, "name", 0, 0, "dateFormat", "dataCenter", 0, 0, 0, "dateField", "dateFieldFormat", "idField", "routingField",
                "expression", 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0, 0.0,1)));
        when(indexTemplateService.listByRegionId(0)).thenReturn(listResult);

        when(esIndexService.syncCatIndexByExpression("clusterPhyName", "expression"))
            .thenReturn(Collections.emptyList());
        when(handleFactory.getByHandlerNamePer("type")).thenReturn(null);

        // Run the test
        final Result<List<VariousLineChartMetricsVO>> resultClusterMetrics = clusterPhyMetricsManager
            .getMultiClusterMetrics(param, 0, "userName", ClusterPhyTypeMetricsEnum.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getUserNameConfigMetricsTest() {
        // Setup
        final UserConfigInfoDTO userConfigInfoDTO = new UserConfigInfoDTO("userName", "firstUserConfigType",
            "secondUserConfigType", Arrays.asList("value"),1,1);
        when(userConfigService.getMetricsByTypeAndUserName(
            new UserConfigInfoDTO("userName", "firstUserConfigType", "secondUserConfigType", Arrays.asList("value"),1,1)))
                .thenReturn(Arrays.asList("value"));

        // Run the test
        final List<String> result = clusterPhyMetricsManager.getUserNameConfigMetrics(userConfigInfoDTO, "userName", 1);

        // Verify the results
        assertThat(result).isEqualTo(Arrays.asList("value"));
    }

    @Test
    void getUserNameConfigMetricsUserConfigServiceReturnsNoItemsTest() {
        // Setup
        final UserConfigInfoDTO userConfigInfoDTO = new UserConfigInfoDTO("userName", "firstUserConfigType",
            "secondUserConfigType", Arrays.asList("value"),1,1);
        when(userConfigService.getMetricsByTypeAndUserName(
            new UserConfigInfoDTO("userName", "firstUserConfigType", "secondUserConfigType", Arrays.asList("value"),1,1)))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<String> result = clusterPhyMetricsManager.getUserNameConfigMetrics(userConfigInfoDTO, "userName", 1);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void updateUserNameConfigMetricsTest() {
        // Setup
        final UserConfigInfoDTO param = new UserConfigInfoDTO("userName", "firstUserConfigType", "secondUserConfigType",
            Arrays.asList("value"),1,1);
        final Result<Integer> expectedResult = Result.buildFail(0);
        when(userConfigService.updateByMetricsByTypeAndUserName(
            new UserConfigInfoDTO("userName", "firstUserConfigType", "secondUserConfigType", Arrays.asList("value"),1,1)))
                .thenReturn(Result.buildFail(0));

        // Run the test
        final Result<Integer> result = clusterPhyMetricsManager.updateUserNameConfigMetrics(param, "userName",1);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void updateUserNameConfigMetricsUserConfigServiceReturnsNoItemTest() {
        // Setup
        final UserConfigInfoDTO param = new UserConfigInfoDTO("userName", "firstUserConfigType", "secondUserConfigType",
            Arrays.asList("value"),1,1);
        when(userConfigService.updateByMetricsByTypeAndUserName(
            new UserConfigInfoDTO("userName", "firstUserConfigType", "secondUserConfigType", Arrays.asList("value"),1,1)))
                .thenReturn(Result.buildSucc());

        // Run the test
        final Result<Integer> result = clusterPhyMetricsManager.updateUserNameConfigMetrics(param, "userName",1);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildSucc());
    }

    @Test
    void updateUserNameConfigMetricsUserConfigServiceReturnsFailureTest() {
        // Setup
        final UserConfigInfoDTO param = new UserConfigInfoDTO("userName", "firstUserConfigType", "secondUserConfigType",
            Arrays.asList("value"),1,1);
        final Result<Integer> expectedResult = Result.buildFail(0);
        when(userConfigService.updateByMetricsByTypeAndUserName(
            new UserConfigInfoDTO("userName", "firstUserConfigType", "secondUserConfigType", Arrays.asList("value"),1,1)))
                .thenReturn(Result.buildFail());

        // Run the test
        final Result<Integer> result = clusterPhyMetricsManager.updateUserNameConfigMetrics(param, "userName",1);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterPhyTaskDetailTest() {
        // Setup
        final Result<List<ESClusterTaskDetailVO>> expectedResult = Result.buildFail(Arrays
            .asList(new ESClusterTaskDetailVO("taskId", "node", "action", 0L, 0L, "runningTimeString", "description")));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure NodeStatisService.getClusterTaskDetail(...).
        final List<ESClusterTaskDetail> esClusterTaskDetails = Arrays
            .asList(new ESClusterTaskDetail("taskId", "node", "action", 0L, 0L, "runningTimeString", "description"));
        when(nodeStatsService.getClusterTaskDetail("clusterPhyName", "node", 0L, 0L)).thenReturn(esClusterTaskDetails);

        // Run the test
        final Result<List<ESClusterTaskDetailVO>> result = clusterPhyMetricsManager
            .getClusterPhyTaskDetail("clusterPhyName", "node", "startTime", "endTime", 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterPhyTaskDetailNodeStatisServiceReturnsNoItemsTest() {
        // Setup
        when(projectService.checkProjectExist(0)).thenReturn(false);
        when(nodeStatsService.getClusterTaskDetail("clusterPhyName", "node", 0L, 0L))
            .thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<ESClusterTaskDetailVO>> result = clusterPhyMetricsManager
            .getClusterPhyTaskDetail("clusterPhyName", "node", "startTime", "endTime", 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }
}