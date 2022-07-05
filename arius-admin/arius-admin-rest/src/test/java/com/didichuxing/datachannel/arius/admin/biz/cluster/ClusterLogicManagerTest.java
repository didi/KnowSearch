package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.biz.cluster.impl.ClusterLogicManagerImpl;
import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.*;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicAggregate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpringTool.class})
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ClusterLogicManagerTest {

    @Mock
    private ESIndexService esIndexService;
    @Mock
    private ClusterPhyService clusterPhyService;
    @Mock
    private ClusterLogicService clusterLogicService;
    @Mock
    private ClusterRegionService clusterRegionService;
    @Mock
    private ClusterRoleHostService clusterRoleHostService;
    @Mock
    private TemplateSrvManager templateSrvManager;
    @Mock
    private IndexTemplateService indexTemplateService;
    @Mock
    private TemplateLogicManager templateLogicManager;
    @Mock
    private IndexTemplatePhyService indexTemplatePhyService;
    @Mock
    private ProjectClusterLogicAuthService projectClusterLogicAuthService;
    @Mock
    private OperateRecordService operateRecordService;
    @Mock
    private ESMachineNormsService esMachineNormsService;
    @Mock
    private ProjectService projectService;
    @Mock
    private ESClusterNodeService eSClusterNodeService;
    @Mock
    private ESGatewayClient esGatewayClient;
    @Mock
    private ClusterRegionManager clusterRegionManager;
    @Mock
    private ClusterContextManager clusterContextManager;
    @Mock
    private ESClusterService esClusterService;
    @Mock
    private HandleFactory handleFactory;
    @Mock
    private IndicesManager indicesManager;

    @InjectMocks
    private ClusterLogicManagerImpl clusterLogicManager;

    @Test
    void buildClusterLogicsTest() {
        // Setup
        final List<ClusterLogic> logicClusters = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.buildClusterLogics(logicClusters);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicsClusterContextManagerReturnsNullTest()
            throws Exception {
        // Setup
        final List<ClusterLogic> logicClusters = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(null);
        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.buildClusterLogics(logicClusters);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicsClusterRegionServiceListPhysicClusterNamesReturnsNoItemsTest()
            throws Exception {
        // Setup
        final List<ClusterLogic> logicClusters = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Collections.emptyList());

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.buildClusterLogics(logicClusters);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicsClusterPhyServiceReturnsNullTest()
            throws Exception {
        // Setup
        final List<ClusterLogic> logicClusters = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(null);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> result = clusterLogicManager.buildClusterLogics(logicClusters);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicsClusterRegionServiceGetRegionByLogicClusterIdReturnsNullTest(){
        // Setup
        final List<ClusterLogic> logicClusters = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.buildClusterLogics(logicClusters);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicsClusterRoleHostServiceReturnsNoItemTest()
            throws Exception {
        // Setup
        final List<ClusterLogic> logicClusters = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(clusterRoleHostService.listByRegionId(0)).thenReturn(Result.buildSucc());
        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> result = clusterLogicManager.buildClusterLogics(logicClusters);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicsClusterRoleHostServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final List<ClusterLogic> logicClusters = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Collections.emptyList());
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.buildClusterLogics(logicClusters);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicsClusterRoleHostServiceReturnsFailureTest()
            throws Exception {
        // Setup
        final List<ClusterLogic> logicClusters = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail();
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.buildClusterLogics(logicClusters);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicsProjectServiceReturnsNullTest()
            throws Exception {
        // Setup
        final List<ClusterLogic> logicClusters = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(null);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.buildClusterLogics(logicClusters);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicTest()
            throws Exception {
        // Setup
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final ClusterLogicVO resultLogic = clusterLogicManager.buildClusterLogic(clusterLogic);

        // Verify the results
        assertThat(resultLogic).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicClusterContextManagerReturnsNullTest()
            throws Exception {
        // Setup
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(null);
        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final ClusterLogicVO resultList = clusterLogicManager.buildClusterLogic(clusterLogic);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicClusterRegionServiceListPhysicClusterNamesReturnsNoItemsTest()
            throws Exception {
        // Setup
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Collections.emptyList());

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final ClusterLogicVO resultList = clusterLogicManager.buildClusterLogic(clusterLogic);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicClusterPhyServiceReturnsNullTest()
            throws Exception {
        // Setup
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(null);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final ClusterLogicVO result = clusterLogicManager.buildClusterLogic(clusterLogic);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicClusterRegionServiceGetRegionByLogicClusterIdReturnsNullTest()
            throws Exception {
        // Setup
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final ClusterLogicVO resultList = clusterLogicManager.buildClusterLogic(clusterLogic);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicClusterRoleHostServiceReturnsNoItemTest()
            throws Exception {
        // Setup
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(clusterRoleHostService.listByRegionId(0)).thenReturn(Result.buildSucc());
        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final ClusterLogicVO result = clusterLogicManager.buildClusterLogic(clusterLogic);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicClusterRoleHostServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Collections.emptyList());
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final ClusterLogicVO resultList = clusterLogicManager.buildClusterLogic(clusterLogic);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicClusterRoleHostServiceReturnsFailureTest()
            throws Exception {
        // Setup
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail();
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final ClusterLogicVO resultLogic = clusterLogicManager.buildClusterLogic(clusterLogic);

        // Verify the results
        assertThat(resultLogic).isEqualTo(expectedResult);
    }

    @Test
    void buildClusterLogicProjectServiceReturnsNullTest()
            throws Exception {
        // Setup
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(null);

        // Run the test
        final ClusterLogicVO resultList = clusterLogicManager.buildClusterLogic(clusterLogic);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void clearIndicesTest()
            throws Exception {
        // Setup
        final ConsoleTemplateClearDTO clearDTO = new ConsoleTemplateClearDTO(0, Arrays.asList("value"), "delQueryDsl");

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

        when(indexTemplatePhyService.getMatchNoVersionIndexNames(0L)).thenReturn(Arrays.asList("value"));
        when(esIndexService.syncDeleteByQuery("cluster", Arrays.asList("value"), "delQueryDsl")).thenReturn(false);
        when(esIndexService.syncBatchDeleteIndices("cluster", Arrays.asList("value"), 0)).thenReturn(0);

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.clearIndices(clearDTO, "operator");

        // Verify the results
        verify(esIndexService).syncBatchDeleteIndices("cluster", Arrays.asList("value"), 0);
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void clearIndicesIndexTemplatePhyServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final ConsoleTemplateClearDTO clearDTO = new ConsoleTemplateClearDTO(0, Arrays.asList("value"), "delQueryDsl");

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

        when(indexTemplatePhyService.getMatchNoVersionIndexNames(0L)).thenReturn(Collections.emptyList());
        when(esIndexService.syncDeleteByQuery("cluster", Arrays.asList("value"), "delQueryDsl")).thenReturn(false);
        when(esIndexService.syncBatchDeleteIndices("cluster", Arrays.asList("value"), 0)).thenReturn(0);

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.clearIndices(clearDTO, "operator");

        // Verify the results
        verify(esIndexService).syncBatchDeleteIndices("cluster", Arrays.asList("value"), 0);
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void clearIndicesESIndexServiceSyncDeleteByQueryThrowsESOperateExceptionTest()
            throws Exception {
        // Setup
        final ConsoleTemplateClearDTO clearDTO = new ConsoleTemplateClearDTO(0, Arrays.asList("value"), "delQueryDsl");

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

        when(indexTemplatePhyService.getMatchNoVersionIndexNames(0L)).thenReturn(Arrays.asList("value"));
        when(esIndexService.syncDeleteByQuery("cluster", Arrays.asList("value"), "delQueryDsl"))
                .thenThrow(ESOperateException.class);

        // Run the test
        assertThatThrownBy(() -> clusterLogicManager.clearIndices(clearDTO, "operator"))
                .isInstanceOf(ESOperateException.class);
    }

    @Test
    void clearIndicesOperateRecordServiceReturnsFailureTest()
            throws Exception {
        // Setup
        final ConsoleTemplateClearDTO clearDTO = new ConsoleTemplateClearDTO(0, Arrays.asList("value"), "delQueryDsl");

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

        when(indexTemplatePhyService.getMatchNoVersionIndexNames(0L)).thenReturn(Arrays.asList("value"));
        when(esIndexService.syncDeleteByQuery("cluster", Arrays.asList("value"), "delQueryDsl")).thenReturn(false);
        when(esIndexService.syncBatchDeleteIndices("cluster", Arrays.asList("value"), 0)).thenReturn(0);

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail());

        // Run the test
        final Result<Void> result = clusterLogicManager.clearIndices(clearDTO, "operator");

        // Verify the results
        verify(esIndexService).syncBatchDeleteIndices("cluster", Arrays.asList("value"), 0);
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void getLogicClusterAssignedPhysicalClustersTest()
            throws Exception {
        // Setup
        final List<ClusterPhy> expectedResult = Arrays.asList(
                new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress", "httpWriteAddress",
                        0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName", "nsTree", 0,
                        "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                        new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0,
                                "plugIds", false, Arrays.asList(
                                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack",
                                        "nodeSet", "machineSpec", 0, "attributes")))), Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType",
                        0, "gatewayUrl"));

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("phyClusterName")).thenReturn(clusterPhy);

        // Run the test
        final List<ClusterPhy> result = clusterLogicManager.getLogicClusterAssignedPhysicalClusters(0L);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getLogicClusterAssignedPhysicalClustersClusterRegionServiceReturnsNullTest()
            throws Exception {
        // Setup
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Run the test
        final List<ClusterPhy> result = clusterLogicManager.getLogicClusterAssignedPhysicalClusters(0L);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void getLogicClustersByProjectIdTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> result = clusterLogicManager.getLogicClustersByProjectId(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getLogicClustersByProjectIdClusterLogicServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(Collections.emptyList());
        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> result = clusterLogicManager.getLogicClustersByProjectId(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getLogicClustersByProjectIdClusterRegionServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Collections.emptyList());

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> result = clusterLogicManager.getLogicClustersByProjectId(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters1Test()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> resultList = clusterLogicManager.getProjectLogicClusters(0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters1ClusterLogicServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(projectService.checkProjectExist(0)).thenReturn(false);
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(Collections.emptyList());

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> resultList = clusterLogicManager.getProjectLogicClusters(0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters1ClusterContextManagerReturnsNullTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(null);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> resultList = clusterLogicManager.getProjectLogicClusters(0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters1ClusterRegionServiceListPhysicClusterNamesReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Collections.emptyList());

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> resultList = clusterLogicManager.getProjectLogicClusters(0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters1ClusterPhyServiceReturnsNullTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(null);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> resultList = clusterLogicManager.getProjectLogicClusters(0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters1ClusterRegionServiceGetRegionByLogicClusterIdReturnsNullTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> resultList = clusterLogicManager.getProjectLogicClusters(0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters1ClusterRoleHostServiceReturnsNoItemTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(clusterRoleHostService.listByRegionId(0)).thenReturn(Result.buildSucc());
        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> result = clusterLogicManager.getProjectLogicClusters(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters1ClusterRoleHostServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Collections.emptyList());
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> resultList = clusterLogicManager.getProjectLogicClusters(0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters1ClusterRoleHostServiceReturnsFailureTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail();
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<List<ClusterLogicVO>> resultList = clusterLogicManager.getProjectLogicClusters(0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters1ProjectServiceGetProjectBriefByProjectIdReturnsNullTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));
        when(projectService.checkProjectExist(0)).thenReturn(false);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(null);

        // Run the test
        final Result<List<ClusterLogicVO>> resultList = clusterLogicManager.getProjectLogicClusters(0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void listProjectClusterLogicIdsAndNamesTest()
            throws Exception {
        // Setup
        final Result<List<Tuple<Long, String>>> expectedResult = Result.buildFail(
                Arrays.asList(new Tuple<>(0L, "cluster")));

        // Configure ClusterLogicService.listAllClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listAllClusterLogics()).thenReturn(clusterLogics);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics1 = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics1);

        // Run the test
        final Result<List<Tuple<Long, String>>> result = clusterLogicManager.listProjectClusterLogicIdsAndNames(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void listProjectClusterLogicIdsAndNamesClusterLogicServiceListAllClusterLogicsReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<List<Tuple<Long, String>>> expectedResult = Result.buildFail(
                Arrays.asList(new Tuple<>(0L, "cluster")));
        when(clusterLogicService.listAllClusterLogics()).thenReturn(Collections.emptyList());

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        // Run the test
        final Result<List<Tuple<Long, String>>> result = clusterLogicManager.listProjectClusterLogicIdsAndNames(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void listProjectClusterLogicIdsAndNamesClusterLogicServiceGetHasAuthClusterLogicsByProjectIdReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<List<Tuple<Long, String>>> expectedResult = Result.buildFail(
                Arrays.asList(new Tuple<>(0L, "cluster")));

        // Configure ClusterLogicService.listAllClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listAllClusterLogics()).thenReturn(clusterLogics);

        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<Tuple<Long, String>>> result = clusterLogicManager.listProjectClusterLogicIdsAndNames(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters2Test()
            throws Exception {
        // Setup
        final Result<ClusterLogicVO> expectedResult = Result.buildFail(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<ClusterLogicVO> resultList = clusterLogicManager.getProjectLogicClusters(0L, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters2ClusterLogicServiceReturnsNullTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicVO> expectedResult = Result.buildFail(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(null);

        // Run the test
        final Result<ClusterLogicVO> result = clusterLogicManager.getProjectLogicClusters(0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters2ClusterContextManagerReturnsNullTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicVO> expectedResult = Result.buildFail(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(null);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<ClusterLogicVO> resultList = clusterLogicManager.getProjectLogicClusters(0L, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters2ClusterRegionServiceListPhysicClusterNamesReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicVO> expectedResult = Result.buildFail(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Collections.emptyList());

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<ClusterLogicVO> resultList = clusterLogicManager.getProjectLogicClusters(0L, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters2ClusterPhyServiceReturnsNullTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicVO> expectedResult = Result.buildFail(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(null);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<ClusterLogicVO> resultList = clusterLogicManager.getProjectLogicClusters(0L, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters2ClusterRegionServiceGetRegionByLogicClusterIdReturnsNullTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicVO> expectedResult = Result.buildFail(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<ClusterLogicVO> resultList = clusterLogicManager.getProjectLogicClusters(0L, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters2ClusterRoleHostServiceReturnsNoItemTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicVO> expectedResult = Result.buildFail(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(clusterRoleHostService.listByRegionId(0)).thenReturn(Result.buildSucc());
        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<ClusterLogicVO> result = clusterLogicManager.getProjectLogicClusters(0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters2ClusterRoleHostServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicVO> expectedResult = Result.buildFail(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Collections.emptyList());
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<ClusterLogicVO> resultList = clusterLogicManager.getProjectLogicClusters(0L, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters2ClusterRoleHostServiceReturnsFailureTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicVO> expectedResult = Result.buildFail(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail();
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final Result<ClusterLogicVO> resultList = clusterLogicManager.getProjectLogicClusters(0L, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusters2ProjectServiceReturnsNullTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicVO> expectedResult = Result.buildFail(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(null);

        // Run the test
        final Result<ClusterLogicVO> resultList = clusterLogicManager.getProjectLogicClusters(0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusterInfoByTypeTest()
            throws Exception {
        // Setup
        final Result<List<ClusterLogicVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        // Run the test
        final Result<List<ClusterLogicVO>> result = clusterLogicManager.getProjectLogicClusterInfoByType(0, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusterInfoByTypeClusterLogicServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<ClusterLogicVO>> result = clusterLogicManager.getProjectLogicClusterInfoByType(0, 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getClusterLogicTemplatesTest()
            throws Exception {
        // Setup
        final HttpServletRequest request = new MockHttpServletRequest();
        final Result<List<ConsoleTemplateVO>> expectedResult = Result.buildFail(
                Arrays.asList(
                        new ConsoleTemplateVO(0, Arrays.asList("value"), 0, false, 0L, "projectName", false, 0L, 0,
                                "cluster")));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure TemplateLogicManager.fetchConsoleTemplate(...).
        final ConsoleTemplateVO vo = new ConsoleTemplateVO(0, Arrays.asList("value"), 0, false, 0L, "projectName",
                false, 0L, 0, "cluster");
        when(templateLogicManager.fetchConsoleTemplate(new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                Arrays.asList(new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false))).thenReturn(vo);

        // Run the test
        final Result<List<ConsoleTemplateVO>> result = clusterLogicManager.getClusterLogicTemplates(request, 0L);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicTemplatesTemplateLogicManagerGetLogicClusterTemplatesAggregateReturnsNoItemsTest()
            throws Exception {
        // Setup
        final HttpServletRequest request = new MockHttpServletRequest();
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(Collections.emptyList());

        // Configure TemplateLogicManager.fetchConsoleTemplate(...).
        final ConsoleTemplateVO vo = new ConsoleTemplateVO(0, Arrays.asList("value"), 0, false, 0L, "projectName",
                false, 0L, 0, "cluster");
        when(templateLogicManager.fetchConsoleTemplate(new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                Arrays.asList(new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false))).thenReturn(vo);

        // Run the test
        final Result<List<ConsoleTemplateVO>> result = clusterLogicManager.getClusterLogicTemplates(request, 0L);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void listMachineSpecTest()
            throws Exception {
        // Setup
        final Result<List<ESClusterNodeSepcVO>> expectedResult = Result.buildFail(
                Arrays.asList(new ESClusterNodeSepcVO(0, "role", "spec", 0, "Create_time")));

        // Configure ESMachineNormsService.listMachineNorms(...).
        final List<ESMachineNormsPO> esMachineNormsPOS = Arrays.asList(new ESMachineNormsPO(0L, "role", "spec", false));
        when(esMachineNormsService.listMachineNorms()).thenReturn(esMachineNormsPOS);

        // Run the test
        final Result<List<ESClusterNodeSepcVO>> result = clusterLogicManager.listMachineSpec();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void listMachineSpecESMachineNormsServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        when(esMachineNormsService.listMachineNorms()).thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<ESClusterNodeSepcVO>> result = clusterLogicManager.listMachineSpec();

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void getClusterLogicsTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.getClusterLogics(param, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicsClusterLogicServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(Collections.emptyList());

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.getClusterLogics(param, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicsClusterContextManagerReturnsNullTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(null);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.getClusterLogics(param, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicsClusterRegionServiceListPhysicClusterNamesReturnsNoItemsTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Collections.emptyList());

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.getClusterLogics(param, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicsClusterPhyServiceReturnsNullTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(null);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.getClusterLogics(param, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicsClusterRegionServiceGetRegionByLogicClusterIdReturnsNullTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.getClusterLogics(param, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicsClusterRoleHostServiceReturnsNoItemTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        when(clusterRoleHostService.listByRegionId(0)).thenReturn(Result.buildSucc());
        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.getClusterLogics(param, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicsClusterRoleHostServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(Collections.emptyList());
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.getClusterLogics(param, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicsClusterRoleHostServiceReturnsFailureTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail();
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.getClusterLogics(param, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicsProjectServiceReturnsNullTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final List<ClusterLogicVO> expectedResult = Arrays.asList(
                new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"), 0,
                        "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0,
                        "desc", Arrays.asList("value"), new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                        "configJson", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                        Arrays.asList(new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                        "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                        "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L, 0L));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> result = Result.buildFail(
                Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")));
        when(clusterRoleHostService.listByRegionId(0)).thenReturn(result);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(null);

        // Run the test
        final List<ClusterLogicVO> resultList = clusterLogicManager.getClusterLogics(param, 0);

        // Verify the results
        assertThat(resultList).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicTest()
            throws Exception {
        // Setup
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("phyClusterName")).thenReturn(clusterPhy);

        // Configure ProjectClusterLogicAuthService.getLogicClusterAuth(...).
        final ProjectClusterLogicAuth projectClusterLogicAuth = new ProjectClusterLogicAuth(0L, 0, 0L, 0,
                "responsible");
        when(projectClusterLogicAuthService.getLogicClusterAuth(0, 0L)).thenReturn(projectClusterLogicAuth);

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Run the test
        final ClusterLogicVO result = clusterLogicManager.getClusterLogic(0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicClusterContextManagerReturnsNullTest()
            throws Exception {
        // Setup
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(null);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("phyClusterName")).thenReturn(clusterPhy);

        // Configure ProjectClusterLogicAuthService.getLogicClusterAuth(...).
        final ProjectClusterLogicAuth projectClusterLogicAuth = new ProjectClusterLogicAuth(0L, 0, 0L, 0,
                "responsible");
        when(projectClusterLogicAuthService.getLogicClusterAuth(0, 0L)).thenReturn(projectClusterLogicAuth);

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Run the test
        final ClusterLogicVO result = clusterLogicManager.getClusterLogic(0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicClusterRegionServiceReturnsNullTest()
            throws Exception {
        // Setup
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);

        // Configure ProjectClusterLogicAuthService.getLogicClusterAuth(...).
        final ProjectClusterLogicAuth projectClusterLogicAuth = new ProjectClusterLogicAuth(0L, 0, 0L, 0,
                "responsible");
        when(projectClusterLogicAuthService.getLogicClusterAuth(0, 0L)).thenReturn(projectClusterLogicAuth);

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Run the test
        final ClusterLogicVO result = clusterLogicManager.getClusterLogic(0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicProjectClusterLogicAuthServiceReturnsNullTest()
            throws Exception {
        // Setup
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("phyClusterName")).thenReturn(clusterPhy);

        when(projectClusterLogicAuthService.getLogicClusterAuth(0, 0L)).thenReturn(null);

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Run the test
        final ClusterLogicVO result = clusterLogicManager.getClusterLogic(0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicProjectServiceReturnsNullTest()
            throws Exception {
        // Setup
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        // Configure ESClusterService.syncGetClusterStats(...).
        final ESClusterStatsResponse esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
                new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(esClusterService.syncGetClusterStats("clusterName")).thenReturn(esClusterStatsResponse);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("phyClusterName")).thenReturn(clusterPhy);

        // Configure ProjectClusterLogicAuthService.getLogicClusterAuth(...).
        final ProjectClusterLogicAuth projectClusterLogicAuth = new ProjectClusterLogicAuth(0L, 0, 0L, 0,
                "responsible");
        when(projectClusterLogicAuthService.getLogicClusterAuth(0, 0L)).thenReturn(projectClusterLogicAuth);

        when(projectService.getProjectBriefByProjectId(0)).thenReturn(null);
        when(esGatewayClient.getGatewayAddress()).thenReturn("gatewayAddress");

        // Run the test
        final ClusterLogicVO result = clusterLogicManager.getClusterLogic(0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void addLogicClusterAndClusterRegionsTest()
            throws Exception {
        // Setup
        final ESLogicClusterWithRegionDTO param = new ESLogicClusterWithRegionDTO(
                Arrays.asList(new ClusterRegionDTO(0L, "name", "logicClusterIds", "phyClusterName", "config")));
        when(clusterRegionManager.batchBindRegionToClusterLogic(new ESLogicClusterWithRegionDTO(
                        Arrays.asList(new ClusterRegionDTO(0L, "name", "logicClusterIds", "phyClusterName", "config"))),
                "operator", false)).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.addLogicClusterAndClusterRegions(param, "operator");

        // Verify the results
    }

    @Test
    void addLogicClusterAndClusterRegionsClusterRegionManagerReturnsNoItemTest()
            throws Exception {
        // Setup
        final ESLogicClusterWithRegionDTO param = new ESLogicClusterWithRegionDTO(
                Arrays.asList(new ClusterRegionDTO(0L, "name", "logicClusterIds", "phyClusterName", "config")));
        when(clusterRegionManager.batchBindRegionToClusterLogic(new ESLogicClusterWithRegionDTO(
                        Arrays.asList(new ClusterRegionDTO(0L, "name", "logicClusterIds", "phyClusterName", "config"))),
                "operator", false)).thenReturn(Result.buildSucc());

        // Run the test
        final Result<Void> result = clusterLogicManager.addLogicClusterAndClusterRegions(param, "operator");

        // Verify the results
        assertThat(result).isEqualTo(Result.buildSucc());
    }

    @Test
    void addLogicClusterAndClusterRegionsClusterRegionManagerReturnsFailureTest()
            throws Exception {
        // Setup
        final ESLogicClusterWithRegionDTO param = new ESLogicClusterWithRegionDTO(
                Arrays.asList(new ClusterRegionDTO(0L, "name", "logicClusterIds", "phyClusterName", "config")));
        when(clusterRegionManager.batchBindRegionToClusterLogic(new ESLogicClusterWithRegionDTO(
                        Arrays.asList(new ClusterRegionDTO(0L, "name", "logicClusterIds", "phyClusterName", "config"))),
                "operator", false)).thenReturn(Result.buildFail());

        // Run the test
        final Result<Void> result = clusterLogicManager.addLogicClusterAndClusterRegions(param, "operator");

        // Verify the results
    }

    @Test
    void addLogicClusterAndClusterRegionsClusterRegionManagerThrowsAdminOperateExceptionTest()
            throws Exception {
        // Setup
        final ESLogicClusterWithRegionDTO param = new ESLogicClusterWithRegionDTO(
                Arrays.asList(new ClusterRegionDTO(0L, "name", "logicClusterIds", "phyClusterName", "config")));
        when(clusterRegionManager.batchBindRegionToClusterLogic(new ESLogicClusterWithRegionDTO(
                        Arrays.asList(new ClusterRegionDTO(0L, "name", "logicClusterIds", "phyClusterName", "config"))),
                "operator", false))
                .thenThrow(AdminOperateException.class);

        // Run the test
        assertThatThrownBy(() -> clusterLogicManager.addLogicClusterAndClusterRegions(param, "operator"))
                .isInstanceOf(AdminOperateException.class);
    }

    @Test
    void getConsoleClusterVOByIdAndProjectIdTest()
            throws Exception {
        // Setup
        final ClusterLogicVO expectedResult = new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo", "libraDepartmentId",
                "libraDepartment", 0, 0.0, 0L, 0, "desc", Arrays.asList("value"),
                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", Arrays.asList(
                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster", "clusterLogicNames", "port", 0, 0,
                                "rack", "machineSpec", "nodeSet", 0, "logicDepart", "attributes", "regionName", 0.0, 0L,
                                0L)))), 0, 0.0, 0L, 0L);

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Run the test
        final ClusterLogicVO result = clusterLogicManager.getConsoleClusterVOByIdAndProjectId(0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void addLogicClusterTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final Result<Long> expectedResult = Result.buildFail(0L);
        when(clusterLogicService.createClusterLogic(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(Result.buildFail(0L));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Long> result = clusterLogicManager.addLogicCluster(param, "operator", 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void addLogicClusterClusterLogicServiceReturnsNoItemTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        when(clusterLogicService.createClusterLogic(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(Result.buildSucc());

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Long> result = clusterLogicManager.addLogicCluster(param, "operator", 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildSucc());
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void addLogicClusterClusterLogicServiceReturnsFailureTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final Result<Long> expectedResult = Result.buildFail(0L);
        when(clusterLogicService.createClusterLogic(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(Result.buildFail());

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Long> result = clusterLogicManager.addLogicCluster(param, "operator", 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void addLogicClusterOperateRecordServiceReturnsFailureTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");
        final Result<Long> expectedResult = Result.buildFail(0L);
        when(clusterLogicService.createClusterLogic(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(Result.buildFail(0L));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail());

        // Run the test
        final Result<Long> result = clusterLogicManager.addLogicCluster(param, "operator", 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void deleteLogicClusterTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildFail(null));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        when(templateLogicManager.delTemplate(0, "operator", 0)).thenReturn(Result.buildFail(null));
        when(indicesManager.deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator"))
                .thenReturn(Result.buildFail(false));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.deleteLogicCluster(0L, "operator", 0);

        // Verify the results
        verify(indicesManager).deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator");
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void deleteLogicClusterClusterLogicServiceDeleteClusterLogicByIdReturnsNoItemTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildSucc());

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        when(templateLogicManager.delTemplate(0, "operator", 0)).thenReturn(Result.buildFail(null));
        when(indicesManager.deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator"))
                .thenReturn(Result.buildFail(false));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.deleteLogicCluster(0L, "operator", 0);

        // Verify the results
        verify(indicesManager).deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator");
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void deleteLogicClusterClusterLogicServiceDeleteClusterLogicByIdReturnsFailureTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildFail());

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        when(templateLogicManager.delTemplate(0, "operator", 0)).thenReturn(Result.buildFail(null));
        when(indicesManager.deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator"))
                .thenReturn(Result.buildFail(false));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.deleteLogicCluster(0L, "operator", 0);

        // Verify the results
        verify(indicesManager).deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator");
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void deleteLogicClusterClusterLogicServiceDeleteClusterLogicByIdThrowsAdminOperateExceptionTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenThrow(AdminOperateException.class);

        // Run the test
        assertThatThrownBy(() -> clusterLogicManager.deleteLogicCluster(0L, "operator", 0))
                .isInstanceOf(AdminOperateException.class);
    }

    @Test
    void deleteLogicClusterTemplateLogicManagerGetLogicClusterTemplatesAggregateReturnsNoItemsTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildFail(null));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(Collections.emptyList());

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        when(templateLogicManager.delTemplate(0, "operator", 0)).thenReturn(Result.buildFail(null));
        when(indicesManager.deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator"))
                .thenReturn(Result.buildFail(false));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.deleteLogicCluster(0L, "operator", 0);

        // Verify the results
        verify(indicesManager).deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator");
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void deleteLogicClusterIndexTemplateServiceReturnsNullTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildFail(null));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(null);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        when(templateLogicManager.delTemplate(0, "operator", 0)).thenReturn(Result.buildFail(null));
        when(indicesManager.deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator"))
                .thenReturn(Result.buildFail(false));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.deleteLogicCluster(0L, "operator", 0);

        // Verify the results
        verify(indicesManager).deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator");
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void deleteLogicClusterESIndexServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildFail(null));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(Collections.emptyList());
        when(templateLogicManager.delTemplate(0, "operator", 0)).thenReturn(Result.buildFail(null));
        when(indicesManager.deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator"))
                .thenReturn(Result.buildFail(false));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.deleteLogicCluster(0L, "operator", 0);

        // Verify the results
        verify(indicesManager).deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator");
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void deleteLogicClusterTemplateLogicManagerDelTemplateReturnsNoItemTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildFail(null));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        when(templateLogicManager.delTemplate(0, "operator", 0)).thenReturn(Result.buildSucc());
        when(indicesManager.deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator"))
                .thenReturn(Result.buildFail(false));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.deleteLogicCluster(0L, "operator", 0);

        // Verify the results
        verify(indicesManager).deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator");
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void deleteLogicClusterTemplateLogicManagerDelTemplateReturnsFailureTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildFail(null));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        when(templateLogicManager.delTemplate(0, "operator", 0)).thenReturn(Result.buildFail());
        when(indicesManager.deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator"))
                .thenReturn(Result.buildFail(false));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.deleteLogicCluster(0L, "operator", 0);

        // Verify the results
        verify(indicesManager).deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator");
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void deleteLogicClusterTemplateLogicManagerDelTemplateThrowsAdminOperateExceptionTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildFail(null));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        when(templateLogicManager.delTemplate(0, "operator", 0)).thenThrow(AdminOperateException.class);

        // Run the test
        assertThatThrownBy(() -> clusterLogicManager.deleteLogicCluster(0L, "operator", 0))
                .isInstanceOf(AdminOperateException.class);
    }

    @Test
    void deleteLogicClusterIndicesManagerReturnsFailureTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildFail(null));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        when(templateLogicManager.delTemplate(0, "operator", 0)).thenReturn(Result.buildFail(null));
        when(indicesManager.deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator"))
                .thenReturn(Result.buildFail());

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.deleteLogicCluster(0L, "operator", 0);

        // Verify the results
        verify(indicesManager).deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator");
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void deleteLogicClusterOperateRecordServiceReturnsFailureTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.deleteClusterLogicById(0L, "operator", 0)).thenReturn(Result.buildFail(null));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        when(templateLogicManager.delTemplate(0, "operator", 0)).thenReturn(Result.buildFail(null));
        when(indicesManager.deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator"))
                .thenReturn(Result.buildFail(false));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail());

        // Run the test
        final Result<Void> result = clusterLogicManager.deleteLogicCluster(0L, "operator", 0);

        // Verify the results
        verify(indicesManager).deleteIndex(
                Arrays.asList(
                        new IndexCatCellDTO("key", "cluster", "health", "status", "index", "pri", "rep", "docsCount",
                                "docsDeleted", "storeSize", "priStoreSize", false, false)), 0, "operator");
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void editLogicClusterTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.editClusterLogic(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"), "operator", 0)).thenReturn(Result.buildFail(null));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.editLogicCluster(param, "operator", 0);

        // Verify the results
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void editLogicClusterClusterLogicServiceEditClusterLogicReturnsNoItemTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.editClusterLogic(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"), "operator", 0)).thenReturn(Result.buildSucc());

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.editLogicCluster(param, "operator", 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildSucc());
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void editLogicClusterClusterLogicServiceEditClusterLogicReturnsFailureTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.editClusterLogic(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"), "operator", 0)).thenReturn(Result.buildFail());

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail(null));

        // Run the test
        final Result<Void> result = clusterLogicManager.editLogicCluster(param, "operator", 0);

        // Verify the results
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void editLogicClusterOperateRecordServiceReturnsFailureTest()
            throws Exception {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible",
                "memo", 0, 0.0, "configJson", 0, "dataNodeSpec");

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        when(clusterLogicService.editClusterLogic(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"), "operator", 0)).thenReturn(Result.buildFail(null));

        // Configure ProjectService.getProjectBriefByProjectId(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(0);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        when(projectService.getProjectBriefByProjectId(0)).thenReturn(projectBriefVO);

        when(operateRecordService.save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"))).thenReturn(Result.buildFail());

        // Run the test
        final Result<Void> result = clusterLogicManager.editLogicCluster(param, "operator", 0);

        // Verify the results
        verify(operateRecordService).save(
                new OperateRecord("projectName", OperateTypeEnum.PHYSICAL_CLUSTER_JOIN, TriggerWayEnum.MANUAL_TRIGGER,
                        "content", "userOperation", "bizId"));
    }

    @Test
    void pageGetClusterLogicsTest()
            throws Exception {
        // Setup
        final ClusterLogicConditionDTO condition = new ClusterLogicConditionDTO(0, "sortTerm", "sortType", false,
                Arrays.asList("value"));
        final PaginationResult<ClusterLogicVO> expectedResult = new PaginationResult<>(
                Arrays.asList(
                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false, Arrays.asList("value"),
                                0, "gatewayAddress", "responsible", "memo", "libraDepartmentId", "libraDepartment", 0,
                                0.0, 0L, 0, "desc", Arrays.asList("value"),
                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L), "configJson",
                                Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")), Arrays.asList(
                                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                        Arrays.asList(new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                "clusterLogicNames", "port", 0, 0, "rack", "machineSpec", "nodeSet", 0,
                                                "logicDepart", "attributes", "regionName", 0.0, 0L, 0L)))), 0, 0.0, 0L,
                                0L)), 0L, 0L, 0L);
        when(handleFactory.getByHandlerNamePer("pageSearchType")).thenReturn(null);

        // Run the test
        final PaginationResult<ClusterLogicVO> result = clusterLogicManager.pageGetClusterLogics(condition, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void pageGetClusterLogicsHandleFactoryThrowsNotFindSubclassExceptionTest()
            throws Exception {
        // Setup
        final ClusterLogicConditionDTO condition = new ClusterLogicConditionDTO(0, "sortTerm", "sortType", false,
                Arrays.asList("value"));
        when(handleFactory.getByHandlerNamePer("pageSearchType")).thenThrow(NotFindSubclassException.class);

        // Run the test
        assertThatThrownBy(() -> clusterLogicManager.pageGetClusterLogics(condition, 0))
                .isInstanceOf(NotFindSubclassException.class);
    }

    @Test
    void updateClusterLogicHealthTest()
            throws Exception {
        // Setup
        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        when(esClusterService.syncGetClusterHealthEnum("clusterName")).thenReturn(ClusterHealthEnum.GREEN);
        when(clusterLogicService.editClusterLogicNotCheck(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"), "desc")).thenReturn(Result.buildFail(null));

        // Run the test
        final boolean result = clusterLogicManager.updateClusterLogicHealth(0L);

        // Verify the results
        assertThat(result).isFalse();
        verify(clusterLogicService).editClusterLogicNotCheck(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"), "desc");
    }

    @Test
    void updateClusterLogicHealthClusterContextManagerReturnsNullTest()
            throws Exception {
        // Setup
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(null);
        when(esClusterService.syncGetClusterHealthEnum("clusterName")).thenReturn(ClusterHealthEnum.GREEN);
        when(clusterLogicService.editClusterLogicNotCheck(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"), "desc")).thenReturn(Result.buildFail(null));

        // Run the test
        final boolean result = clusterLogicManager.updateClusterLogicHealth(0L);

        // Verify the results
        assertThat(result).isFalse();
        verify(clusterLogicService).editClusterLogicNotCheck(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"), "desc");
    }

    @Test
    void updateClusterLogicHealthClusterLogicServiceReturnsFailureTest()
            throws Exception {
        // Setup
        // Configure ClusterContextManager.getClusterLogicContext(...).
        final ClusterLogicContext clusterLogicContext = new ClusterLogicContext(0L, "clusterLogicName", 0, 0, 0,
                Arrays.asList("value"), 0, Arrays.asList("value"), Arrays.asList(0L));
        when(clusterContextManager.getClusterLogicContext(0L)).thenReturn(clusterLogicContext);

        when(esClusterService.syncGetClusterHealthEnum("clusterName")).thenReturn(ClusterHealthEnum.GREEN);
        when(clusterLogicService.editClusterLogicNotCheck(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"), "desc")).thenReturn(Result.buildFail());

        // Run the test
        final boolean result = clusterLogicManager.updateClusterLogicHealth(0L);

        // Verify the results
        assertThat(result).isFalse();
        verify(clusterLogicService).editClusterLogicNotCheck(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"), "desc");
    }

    @Test
    void indexTemplateCountTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicTemplateIndexCountVO> expectedResult = Result.buildFail(
                new ClusterLogicTemplateIndexCountVO(0, 0));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        // Run the test
        final Result<ClusterLogicTemplateIndexCountVO> result = clusterLogicManager.indexTemplateCount(0L, "operator",
                0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void indexTemplateCountTemplateLogicManagerReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicTemplateIndexCountVO> expectedResult = Result.buildFail(
                new ClusterLogicTemplateIndexCountVO(0, 0));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(Collections.emptyList());

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        // Run the test
        final Result<ClusterLogicTemplateIndexCountVO> result = clusterLogicManager.indexTemplateCount(0L, "operator",
                0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void indexTemplateCountIndexTemplateServiceReturnsNullTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicTemplateIndexCountVO> expectedResult = Result.buildFail(
                new ClusterLogicTemplateIndexCountVO(0, 0));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(null);

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
        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(catIndexResultList);

        // Run the test
        final Result<ClusterLogicTemplateIndexCountVO> result = clusterLogicManager.indexTemplateCount(0L, "operator",
                0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void indexTemplateCountESIndexServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<ClusterLogicTemplateIndexCountVO> expectedResult = Result.buildFail(
                new ClusterLogicTemplateIndexCountVO(0, 0));

        // Configure TemplateLogicManager.getLogicClusterTemplatesAggregate(...).
        final List<IndexTemplateLogicAggregate> indexTemplateLogicAggregates = Arrays.asList(
                new IndexTemplateLogicAggregate(new IndexTemplateWithCluster(
                        Arrays.asList(
                                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0))),
                        new ProjectTemplateAuth(0L, 0, 0, 0, "responsible"),
                        new IndexTemplateValue(0, 0, 0L, 0.0, "logicCluster"), false));
        when(templateLogicManager.getLogicClusterTemplatesAggregate(0L, 0)).thenReturn(indexTemplateLogicAggregates);

        // Configure IndexTemplateService.getLogicTemplateWithPhysicalsById(...).
        final IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(0)).thenReturn(indexTemplateWithPhyTemplates);

        when(esIndexService.syncCatIndexByExpression("cluster", "expression")).thenReturn(Collections.emptyList());

        // Run the test
        final Result<ClusterLogicTemplateIndexCountVO> result = clusterLogicManager.indexTemplateCount(0L, "operator",
                0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void estimatedDiskSizeTest()
            throws Exception {
        // Setup
        final Result<Long> expectedResult = Result.buildFail(0L);

        // Configure ClusterLogicService.getClusterLogicById(...).
        final ClusterLogic clusterLogic = new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        when(clusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);

        // Run the test
        final Result<Long> result = clusterLogicManager.estimatedDiskSize(0L, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusterNameByTypeTest()
            throws Exception {
        // Setup
        final Result<List<String>> expectedResult = Result.buildFail(Arrays.asList("value"));

        // Configure ClusterLogicService.listClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(clusterLogics);

        // Run the test
        final Result<List<String>> result = clusterLogicManager.getProjectLogicClusterNameByType(0, 0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getProjectLogicClusterNameByTypeClusterLogicServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        when(clusterLogicService.listClusterLogics(
                new ESLogicClusterDTO(0L, "cluster", 0, 0, "dataCenter", 0, "responsible", "memo", 0, 0.0, "configJson",
                        0, "dataNodeSpec"))).thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<String>> result = clusterLogicManager.getProjectLogicClusterNameByType(0, 0);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void listClusterLogicNameByProjectIdTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.listAllClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listAllClusterLogics()).thenReturn(clusterLogics);

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics1 = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics1);

        // Run the test
        final List<String> result = clusterLogicManager.listClusterLogicNameByProjectId(0);

        // Verify the results
        assertThat(result).isEqualTo(Arrays.asList("value"));
    }

    @Test
    void listClusterLogicNameByProjectIdClusterLogicServiceListAllClusterLogicsReturnsNoItemsTest()
            throws Exception {
        // Setup
        when(clusterLogicService.listAllClusterLogics()).thenReturn(Collections.emptyList());

        // Configure ClusterLogicService.getHasAuthClusterLogicsByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(clusterLogics);

        // Run the test
        final List<String> result = clusterLogicManager.listClusterLogicNameByProjectId(0);

        // Verify the results
        assertThat(result).isEqualTo(Arrays.asList("value"));
    }

    @Test
    void listClusterLogicNameByProjectIdClusterLogicServiceGetHasAuthClusterLogicsByProjectIdReturnsNoItemsTest()
            throws Exception {
        // Setup
        // Configure ClusterLogicService.listAllClusterLogics(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.listAllClusterLogics()).thenReturn(clusterLogics);

        when(clusterLogicService.getHasAuthClusterLogicsByProjectId(0)).thenReturn(Collections.emptyList());

        // Run the test
        final List<String> result = clusterLogicManager.listClusterLogicNameByProjectId(0);

        // Verify the results
        assertThat(result).isEqualTo(Arrays.asList("value"));
    }

    @Test
    void getClusterRelationByProjectIdTest()
            throws Exception {
        // Setup
        final Result<List<Tuple<String, ClusterPhyVO>>> expectedResult = Result.buildFail(
                Arrays.asList(new Tuple<>("cluster",
                        new ClusterPhyVO(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                                "httpWriteAddress", 0, "tags", "dataCenter", 0, "machineSpec", 0, "esVersion",
                                "imageName", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                                Arrays.asList(
                                        new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                                Arrays.asList(
                                                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                                "clusterLogicNames", "port", 0, 0, "rack",
                                                                "machineSpec", "nodeSet", 0, "logicDepart",
                                                                "attributes", "regionName", 0.0, 0L, 0L)))), 0.0, 0L,
                                0L, "password", "idc", 0, "writeAction", 0, 0L, "platformType", 0, "gatewayUrl",
                                Arrays.asList(new Tuple<>(
                                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                                                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo",
                                                "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0, "desc",
                                                Arrays.asList("value"),
                                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                                                "configJson", Arrays.asList(
                                                new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                                                Arrays.asList(
                                                        new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0,
                                                                "dataNodeSpec", Arrays.asList())), 0, 0.0, 0L, 0L),
                                        new ClusterRegionVO(0L, "name", "logicClusterIds", "clusterName",
                                                "config")))))));

        // Configure ClusterPhyService.listAllClusters(...).
        final List<ClusterPhy> clusterPhies = Arrays.asList(
                new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress", "httpWriteAddress",
                        0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName", "nsTree", 0,
                        "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                        new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0,
                                "plugIds", false, Arrays.asList(
                                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack",
                                        "nodeSet", "machineSpec", 0, "attributes")))), Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType",
                        0, "gatewayUrl"));
        when(clusterPhyService.listAllClusters()).thenReturn(clusterPhies);

        // Configure ClusterLogicService.getOwnedClusterLogicListByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getOwnedClusterLogicListByProjectId(0)).thenReturn(clusterLogics);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("phyClusterName")).thenReturn(clusterPhy);

        // Run the test
        final Result<List<Tuple<String, ClusterPhyVO>>> result = clusterLogicManager.getClusterRelationByProjectId(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterRelationByProjectIdClusterPhyServiceListAllClustersReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<List<Tuple<String, ClusterPhyVO>>> expectedResult = Result.buildFail(
                Arrays.asList(new Tuple<>("cluster",
                        new ClusterPhyVO(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                                "httpWriteAddress", 0, "tags", "dataCenter", 0, "machineSpec", 0, "esVersion",
                                "imageName", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                                Arrays.asList(
                                        new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                                Arrays.asList(
                                                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                                "clusterLogicNames", "port", 0, 0, "rack",
                                                                "machineSpec", "nodeSet", 0, "logicDepart",
                                                                "attributes", "regionName", 0.0, 0L, 0L)))), 0.0, 0L,
                                0L, "password", "idc", 0, "writeAction", 0, 0L, "platformType", 0, "gatewayUrl",
                                Arrays.asList(new Tuple<>(
                                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                                                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo",
                                                "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0, "desc",
                                                Arrays.asList("value"),
                                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                                                "configJson", Arrays.asList(
                                                new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                                                Arrays.asList(
                                                        new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0,
                                                                "dataNodeSpec", Arrays.asList())), 0, 0.0, 0L, 0L),
                                        new ClusterRegionVO(0L, "name", "logicClusterIds", "clusterName",
                                                "config")))))));
        when(clusterPhyService.listAllClusters()).thenReturn(Collections.emptyList());

        // Configure ClusterLogicService.getOwnedClusterLogicListByProjectId(...).
        final List<ClusterLogic> clusterLogics = Arrays.asList(
                new ClusterLogic(0L, "cluster", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
                        "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(clusterLogicService.getOwnedClusterLogicListByProjectId(0)).thenReturn(clusterLogics);

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("phyClusterName")).thenReturn(clusterPhy);

        // Run the test
        final Result<List<Tuple<String, ClusterPhyVO>>> result = clusterLogicManager.getClusterRelationByProjectId(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterRelationByProjectIdClusterLogicServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        final Result<List<Tuple<String, ClusterPhyVO>>> expectedResult = Result.buildFail(
                Arrays.asList(new Tuple<>("cluster",
                        new ClusterPhyVO(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                                "httpWriteAddress", 0, "tags", "dataCenter", 0, "machineSpec", 0, "esVersion",
                                "imageName", Arrays.asList(new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                                Arrays.asList(
                                        new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec",
                                                Arrays.asList(
                                                        new ESClusterRoleHostVO(0L, 0L, "hostname", "ip", "cluster",
                                                                "clusterLogicNames", "port", 0, 0, "rack",
                                                                "machineSpec", "nodeSet", 0, "logicDepart",
                                                                "attributes", "regionName", 0.0, 0L, 0L)))), 0.0, 0L,
                                0L, "password", "idc", 0, "writeAction", 0, 0L, "platformType", 0, "gatewayUrl",
                                Arrays.asList(new Tuple<>(
                                        new ClusterLogicVO(0L, "name", "dataCenter", 0, 0, "projectName", false,
                                                Arrays.asList("value"), 0, "gatewayAddress", "responsible", "memo",
                                                "libraDepartmentId", "libraDepartment", 0, 0.0, 0L, 0, "desc",
                                                Arrays.asList("value"),
                                                new ConsoleClusterStatusVO("name", 0L, "desc", 0, 0.0, 0.0, 0L),
                                                "configJson", Arrays.asList(
                                                new ESClusterTemplateSrvVO(0, "serviceName", "esVersion")),
                                                Arrays.asList(
                                                        new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0,
                                                                "dataNodeSpec", Arrays.asList())), 0, 0.0, 0L, 0L),
                                        new ClusterRegionVO(0L, "name", "logicClusterIds", "clusterName",
                                                "config")))))));

        // Configure ClusterPhyService.listAllClusters(...).
        final List<ClusterPhy> clusterPhies = Arrays.asList(
                new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress", "httpWriteAddress",
                        0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName", "nsTree", 0,
                        "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                        new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0,
                                "plugIds", false, Arrays.asList(
                                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack",
                                        "nodeSet", "machineSpec", 0, "attributes")))), Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType",
                        0, "gatewayUrl"));
        when(clusterPhyService.listAllClusters()).thenReturn(clusterPhies);

        when(clusterLogicService.getOwnedClusterLogicListByProjectId(0)).thenReturn(Collections.emptyList());

        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName",
                "config");
        when(clusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);

        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "hostname", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0,
                "gatewayUrl");
        when(clusterPhyService.getClusterByName("phyClusterName")).thenReturn(clusterPhy);

        // Run the test
        final Result<List<Tuple<String, ClusterPhyVO>>> result = clusterLogicManager.getClusterRelationByProjectId(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicPluginsTest()
            throws Exception {
        // Setup
        final Result<List<PluginVO>> expectedResult = Result.buildFail(
                Arrays.asList(new PluginVO(0L, "name", "version", "url", "md5", "desc", "creator", 0, false)));

        // Configure ClusterLogicService.getClusterLogicPlugins(...).
        final List<Plugin> plugins = Arrays.asList(
                new Plugin(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", "fileName", null,
                        0, false));
        when(clusterLogicService.getClusterLogicPlugins(0L)).thenReturn(plugins);

        // Run the test
        final Result<List<PluginVO>> result = clusterLogicManager.getClusterLogicPlugins(0L);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getClusterLogicPluginsClusterLogicServiceReturnsNoItemsTest()
            throws Exception {
        // Setup
        when(clusterLogicService.getClusterLogicPlugins(0L)).thenReturn(Collections.emptyList());

        // Run the test
        final Result<List<PluginVO>> result = clusterLogicManager.getClusterLogicPlugins(0L);

        // Verify the results
        assertThat(result).isEqualTo(Result.buildFail(Collections.emptyList()));
    }

    @Test
    void rowBoundsTest()
            throws Exception {
        // Setup
        final List list = Arrays.asList();

        // Run the test
        final List result = ClusterLogicManagerImpl.rowBounds(0, 0, list);

        // Verify the results
    }
}
