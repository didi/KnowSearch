package com.didichuxing.datachannel.arius.admin.biz.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.didichuxing.datachannel.arius.admin.biz.gateway.impl.GatewayManagerImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplateLogicAliasesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway.GatewayClusterNode;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.GatewayESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicAliasService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslStatisticsService;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class GatewayManagerTest {
    @Autowired
    private GatewayManager gatewayManager;

    // @Test
    public void directSqlSearchTest() {
        String sql = "SELECT * FROM arius.dsl.template LIMIT 10";
        Assertions.assertEquals("", gatewayManager.directSqlSearch(sql, null, 1));
    }

    @Mock
    private ESUserService                   mockEsUserService;
    @Mock
    private ProjectService                  mockProjectService;
    @Mock
    private ProjectLogicTemplateAuthService mockProjectLogicTemplateAuthService;
    @Mock
    private IndexTemplateService            mockIndexTemplateService;
    @Mock
    private IndexTemplatePhyService         mockIndexTemplatePhyService;
    @Mock
    private TemplateLogicAliasManager       mockTemplateLogicAliasManager;
    @Mock
    private GatewayService                  mockGatewayService;
    @Mock
    private AriusConfigInfoService          mockAriusConfigInfoService;
    @Mock
    private DslStatisticsService mockDslStatisticsService;
    @Mock
    private TemplateLogicAliasService       mockTemplateLogicAliasService;
    @Mock
    private ProjectConfigService            mockProjectConfigService;
    protected HttpHeaders                   headers;

    @InjectMocks
    private GatewayManagerImpl              gatewayManagerImplUnderTest;
    
    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
        headers.add(HttpRequestUtil.USER, "admin");
        initMocks(this);
    }
    
    @Test
    void testHeartbeat1() {
        // Setup
        final GatewayHeartbeat heartbeat = new GatewayHeartbeat("clusterName", "hostName", 0);
        when(mockGatewayService.heartbeat(new GatewayHeartbeat("clusterName", "hostName", 0))).thenReturn(
                Result.buildFail(null));
        
        // Run the test
        final Result<Void> result = gatewayManagerImplUnderTest.heartbeat(heartbeat);
        
        // Verify the results
    }
    
    @Test
    void testHeartbeat2() {
        // Setup
        final Result<Integer> expectedResult = Result.buildSucc(0);
        when(mockGatewayService.aliveCount("clusterName", 0L)).thenReturn(Result.buildSucc(0));
        
        // Run the test
        final Result<Integer> result = gatewayManagerImplUnderTest.heartbeat("clusterName");
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetGatewayAliveNode() {
        // Setup
        final Result<List<GatewayClusterNodeVO>> expectedResult = Result.buildFail(Arrays.asList(
                new GatewayClusterNodeVO(0, "clusterName", "hostName", 0,
                        new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())));
        
        // Configure GatewayService.getAliveNode(...).
        final List<GatewayClusterNode> gatewayClusterNodes = Arrays.asList(
                new GatewayClusterNode(0, "clusterName", "hostName", 0,
                        new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()));
        when(mockGatewayService.getAliveNode("clusterName", 0L)).thenReturn(gatewayClusterNodes);
        
        // Run the test
        final Result<List<GatewayClusterNodeVO>> result = gatewayManagerImplUnderTest.getGatewayAliveNode(
                "clusterName");
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetGatewayAliveNodeNames() {
        // Setup
        final Result<List<String>> expectedResult = Result.buildSucc(Arrays.asList("value"));
        
        // Configure GatewayService.getAliveNode(...).
        final List<GatewayClusterNode> gatewayClusterNodes = Arrays.asList(
                new GatewayClusterNode(0, "clusterName", "hostName", 0,
                        new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()));
        when(mockGatewayService.getAliveNode("clusterName", 0L)).thenReturn(gatewayClusterNodes);
        
        // Run the test
        final Result<List<String>> result = gatewayManagerImplUnderTest.getGatewayAliveNodeNames("clusterName");
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    
    @Test
    void testListProject() {
        final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader("X-ARIUS-GATEWAY-TICKET","xTc59aY72");
        // Setup
        final HttpServletRequest request = mockHttpServletRequest;
        final Result<List<GatewayESUserVO>> expectedResult = Result.buildSucc(Arrays.asList(
                new GatewayESUserVO()));
        
        // Configure ProjectService.getProjectBriefList(...).
        final ProjectBriefVO projectBriefVO = new ProjectBriefVO();
        projectBriefVO.setId(1);
        projectBriefVO.setProjectCode("projectCode");
        projectBriefVO.setProjectName("projectName");
        final List<ProjectBriefVO> projectBriefVOS = Arrays.asList(projectBriefVO);
        when(mockProjectService.getProjectBriefList()).thenReturn(projectBriefVOS);
        
        // Configure ESUserService.listESUsers(...).
        final List<ESUser> esUsers = Arrays.asList(
                new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible", "memo", 0, 0,
                        "cluster", 0, "dataCenter", 1, false, "ip", "indexExp"));
        when(mockEsUserService.listESUsers(Arrays.asList(1))).thenReturn(esUsers);
        
        when(mockProjectConfigService.projectId2ProjectConfigMap()).thenReturn(new HashMap<>());
        when(mockProjectLogicTemplateAuthService.getAllProjectTemplateAuths()).thenReturn(new HashMap<>());
        when(mockAriusConfigInfoService.stringSetting("arius.common.group", "app.default.read.auth.indices",
                "")).thenReturn("result");
        when(mockIndexTemplateService.getAllLogicTemplatesMap()).thenReturn(new HashMap<>());
        when(mockTemplateLogicAliasService.listAliasMapWithCache()).thenReturn(new HashMap<>());
        
        // Run the test
        final Result<List<GatewayESUserVO>> result = gatewayManagerImplUnderTest.listESUserByProject();
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetTemplateMap() {
        // Setup
        final Result<Map<String, GatewayTemplatePhysicalVO>> expectedResult = Result.buildFail(new HashMap<>());
        
        // Configure IndexTemplatePhyService.getNormalTemplateByCluster(...).
        final List<IndexTemplatePhy> indexTemplatePhies = Arrays.asList(
                new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config", 0));
        when(mockIndexTemplatePhyService.getNormalTemplateByCluster("cluster")).thenReturn(indexTemplatePhies);
        
        when(mockIndexTemplateService.getAllLogicTemplatesMap()).thenReturn(new HashMap<>());
        
        // Configure TemplateLogicAliasesManager.listAlias(...).
        final List<IndexTemplateAlias> indexTemplateAliases = Arrays.asList(new IndexTemplateAlias(0, 0, "name"));
        when(mockTemplateLogicAliasManager.listAlias()).thenReturn(indexTemplateAliases);
        
        // Run the test
        final Result<Map<String, GatewayTemplatePhysicalVO>> result = gatewayManagerImplUnderTest.getTemplateMap(
                "cluster");
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testListDeployInfo() {
        // Setup
        final Result<Map<String, GatewayTemplateDeployInfoVO>> expectedResult = Result.buildSucc(new HashMap<>());
        
        // Configure IndexTemplateService.getTemplateWithPhysicalByDataCenter(...).
        final List<IndexTemplateWithPhyTemplates> indexTemplateWithPhyTemplates = Arrays.asList(
                new IndexTemplateWithPhyTemplates(Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0))));
        when(mockIndexTemplateService.listTemplateWithPhysicalByDataCenter("dataCenter")).thenReturn(
                indexTemplateWithPhyTemplates);
        
        // Configure TemplateLogicAliasesManager.listAlias(...).
        final List<IndexTemplateAlias> indexTemplateAliases = Arrays.asList(new IndexTemplateAlias(0, 0, "name"));
        when(mockTemplateLogicAliasManager.listAlias(Arrays.asList(new IndexTemplateWithPhyTemplates(Arrays.asList(
                new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                        0)))))).thenReturn(indexTemplateAliases);
        
        
        // Run the test
        final Result<Map<String, GatewayTemplateDeployInfoVO>> result = gatewayManagerImplUnderTest.listDeployInfo(
                "dataCenter");
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testScrollSearchDslTemplate() {
        // Setup
        final ScrollDslTemplateRequest request = new ScrollDslTemplateRequest();
        request.setScrollSize(0L);
        request.setDslTemplateVersion("dslTemplateVersion");
        request.setLastModifyTime(0L);
        request.setScrollId("scrollId");
        
        final Result<ScrollDslTemplateResponse> expectedResult = Result.buildSucc(new ScrollDslTemplateResponse(
                Arrays.asList(
                        new DslTemplatePO("ariusCreateTime", "ariusModifyTime", 0.0, "requestType", "searchType", 0L,
                                0.0, 0.0, 0.0, 0.0, 0.0, "logTime", "indiceSample", "dslTemplate", 0L, "dslType",
                                "indices", "dslTemplateMd5", 0.0, 0.0, 0L, 0, "dsl", 0.0, "flinkTime", 0.0, false,
                                false, false, "checkMode", 0L, "version", "dslTag")), "scrollId"));
        
        // Configure DslStatisService.scrollSearchDslTemplate(...).
        final Result<ScrollDslTemplateResponse> scrollDslTemplateResponseResult = Result.buildSucc(
                new ScrollDslTemplateResponse(Arrays.asList(
                        new DslTemplatePO("ariusCreateTime", "ariusModifyTime", 0.0, "requestType", "searchType", 0L,
                                0.0, 0.0, 0.0, 0.0, 0.0, "logTime", "indiceSample", "dslTemplate", 0L, "dslType",
                                "indices", "dslTemplateMd5", 0.0, 0.0, 0L, 0, "dsl", 0.0, "flinkTime", 0.0, false,
                                false, false, "checkMode", 0L, "version", "dslTag")), "scrollId"));
        when(mockDslStatisticsService.scrollSearchDslTemplate(new ScrollDslTemplateRequest())).thenReturn(
                scrollDslTemplateResponseResult);
        
        // Run the test
        final Result<ScrollDslTemplateResponse> result = gatewayManagerImplUnderTest.scrollSearchDslTemplate(request);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testAddAlias() {
        // Setup
        final IndexTemplateAliasDTO indexTemplateAliasDTO = new IndexTemplateAliasDTO();
        indexTemplateAliasDTO.setLogicId(0);
        indexTemplateAliasDTO.setName("name");
        
        final Result<Boolean> expectedResult = Result.buildSucc();
        when(mockTemplateLogicAliasService.addAlias(new IndexTemplateAliasDTO())).thenReturn(Result.buildSucc());
        
        // Run the test
        final Result<Boolean> result = gatewayManagerImplUnderTest.addAlias(indexTemplateAliasDTO);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testDelAlias() {
        // Setup
        final IndexTemplateAliasDTO indexTemplateAliasDTO = new IndexTemplateAliasDTO();
        indexTemplateAliasDTO.setLogicId(0);
        indexTemplateAliasDTO.setName("name");
        
        final Result<Boolean> expectedResult = Result.buildSucc();
        when(mockTemplateLogicAliasService.delAlias(new IndexTemplateAliasDTO())).thenReturn(Result.buildSucc());
        
        // Run the test
        final Result<Boolean> result = gatewayManagerImplUnderTest.delAlias(indexTemplateAliasDTO);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testSqlExplain() {
        // Setup
        final Result<String> expectedResult = Result.buildFail("value");
        when(mockEsUserService.checkDefaultESUserByProject(0)).thenReturn(false);
        
        // Configure ESUserService.getDefaultESUserByProject(...).
        final ESUser esUser = new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible",
                "memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp");
        when(mockEsUserService.getDefaultESUserByProject(0)).thenReturn(esUser);
        
        when(mockGatewayService.sqlOperate("sql", "phyClusterName",
                new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible", "memo", 0, 0,
                        "cluster", 0, "dataCenter", 0, false, "ip", "indexExp"), "SQL_EXPLAIN")).thenReturn(
                Result.buildFail("value"));
        
        // Run the test
        final Result<String> result = gatewayManagerImplUnderTest.sqlExplain("sql", 0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testDirectSqlSearch() {
        // Setup
        final Result<String> expectedResult = Result.buildFail("value");
        when(mockEsUserService.checkDefaultESUserByProject(0)).thenReturn(false);
        
        // Configure ESUserService.getDefaultESUserByProject(...).
        final ESUser esUser = new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible",
                "memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp");
        when(mockEsUserService.getDefaultESUserByProject(0)).thenReturn(esUser);
        
        when(mockGatewayService.sqlOperate("sql", "phyClusterName",
                new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible", "memo", 0, 0,
                        "cluster", 0, "dataCenter", 0, false, "ip", "indexExp"), "SQL_SEARCH")).thenReturn(
                Result.buildFail("value"));
        
        // Run the test
        final Result<String> result = gatewayManagerImplUnderTest.directSqlSearch("sql", "phyClusterName", 0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
}