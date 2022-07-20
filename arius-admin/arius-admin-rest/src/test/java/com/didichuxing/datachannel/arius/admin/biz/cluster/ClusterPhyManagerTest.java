
package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.cluster.impl.ClusterPhyManagerImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterConnectionStatus;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@ActiveProfiles("test")
@ExtendWith({ SpringExtension.class })
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SpringTool.class })
@SpringBootTest
class ClusterPhyManagerTest {

    public static final String            CLUSTER           = "clusterPhyName";
    public static final String            TEMPLATE          = "template";
    public static final String            EXPRESSION        = "expression";
    public static final String            DATE_FORMAT       = "yyyy-MM-dd";

    public static final int               LOGIC_TEMPLATE_ID = 0;
    public static final long              PHYSICAL_ID       = 0L;
    @Mock
    private ESTemplateService             mockEsTemplateService;
    @Mock
    private ClusterPhyService             mockClusterPhyService;
    @Mock
    private ClusterLogicService           mockClusterLogicService;
    @Mock
    private ClusterRoleService            mockClusterRoleService;
    @Mock
    private ClusterRoleHostService        mockClusterRoleHostService;
    @Mock
    private IndexTemplatePhyService       mockIndexTemplatePhyService;
    @Mock
    private TemplatePhyMappingManager     mockTemplatePhyMappingManager;
    @Mock
    private PipelineManager               mockTemplatePipelineManager;
    @Mock
    private IndexTemplateService          mockIndexTemplateService;
    @Mock
    private TemplatePhyManager            mockTemplatePhyManager;
    @Mock
    private ClusterRegionService          mockClusterRegionService;
    @Mock
    private ClusterContextManager         mockClusterContextManager;
    @Mock
    private ProjectService                mockAppService;
    @Mock
    private OperateRecordService          mockOperateRecordService;
    @Mock
    private ESClusterNodeService          mockEsClusterNodeService;
    @Mock
    private ESClusterService              mockEsClusterService;
    @Mock
    private ESOpClient                    mockEsOpClient;

    @InjectMocks
    private ClusterPhyManagerImpl         clusterPhyManager;

    private IndexTemplate                 indexTemplate;
    private IndexTemplatePhy              indexTemplatePhy;
    private List<IndexTemplatePhy>        indexTemplatePhyList;
    private IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates;
    private ClusterPhyDTO                 clusterPhyDTO;
    ESConfigDTO                           esConfigDTO;
    PluginDTO                             pluginDTO;
    ESClusterRoleDTO                      esClusterRoleDTO;
    ESClusterRoleHostDTO                  esClusterRoleHostDTO;
    ClusterPhyVO                          clusterPhyVO;
    ClusterPhyVO                          clusterPhyVOWithNotRole;
    ClusterRoleHost                       clusterRoleHost;
    ClusterRoleInfo                       clusterRoleInfo;

    List<ClusterPhy>                      clusterPhyList;
    ESClusterStatsResponse                esClusterStatsResponse;
    List<ClusterRoleInfo>                 clusterRoleInfos;
    ClusterPhy                            clusterPhy;
    ClusterPhy                            privateClusterPhy;
    ClusterPhy                            exclusiveClusterPhy;
    ClusterLogic                          clusterLogic;
    ClusterRegion                         region;
    List<ClusterRoleHost>                 roleHostList;
    List<ClusterRegion>                   regions;

    @BeforeEach
    void setUp() {
        openMocks(this);
        indexTemplate = new IndexTemplate(LOGIC_TEMPLATE_ID, "name", 0, 0, DATE_FORMAT, "dataCenter", 0, 0, 0,
            "libraDepartmentId", "libraDepartment", "responsible", "dateField", "dateFieldFormat", "idField",
            "routingField", EXPRESSION, 0L, "desc", 0.0, 0, "ingestPipeline", false, false, 0, false, 0L, "openSrv", 0,
            0.0);
        indexTemplatePhy = new IndexTemplatePhy(PHYSICAL_ID, 0, TEMPLATE, EXPRESSION, CLUSTER, "rack", 0, 0, 0, 1, 0,
            "config", 0);
        indexTemplatePhyList = Collections.singletonList(indexTemplatePhy);
        indexTemplateWithPhyTemplates = new IndexTemplateWithPhyTemplates(indexTemplatePhyList);
        esConfigDTO = new ESConfigDTO(0L, 0L, "typeName", "enginName", "configData", "desc", "versionTag", 0, 0);
        pluginDTO = new PluginDTO(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", 0,
            "fileName", null);
        esClusterRoleDTO = new ESClusterRoleDTO(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0,
            "plugIds", false);
        esClusterRoleHostDTO = new ESClusterRoleHostDTO(0L, 0L, "hostname", "ip", CLUSTER, "port", false, 0, 0,
            "nodeSet", 0, "attributes", "16c-32g-1t");
        clusterPhyDTO = new ClusterPhyDTO(0, CLUSTER, "desc", "readAddress", "writeAddress", "httpAddress",
            "httpAddress", 0, "tags", "code", "idc", 0, "esVersion", "imageName", "nsTree", "plugIds", 0L, esConfigDTO,
            Collections.singletonList(pluginDTO), Collections.singletonList(esClusterRoleDTO),
            Collections.singletonList(esClusterRoleHostDTO), 0, "machineSpec", "operator", "templateSrvs", "password",
            0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");

        clusterPhyVO = new ClusterPhyVO(0, CLUSTER, "desc", "readAddress", "writeAddress", "httpAddress",
            "httpWriteAddress", 0, "tags", "dataCenter", 0, "machineSpec", 0, "esVersion", "imageName", null,
            Collections.singletonList(
                new ESClusterRoleVO(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", Lists.newArrayList())),
            0.0, 0L, 0L, "password", "idc", 0, "writeAction", 0, 0L, "platformType", 0, "gatewayUrl", null);

        clusterRoleHost = new ClusterRoleHost(0L, 0L, "hostname", "ip", CLUSTER, "port", 0, 0, "rack", "nodeSet",
            "machineSpec", 0, "attributes");
        clusterRoleInfo = new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "machineSpec", "esVersion", 0,
            "plugIds", false, Collections.singletonList(clusterRoleHost));
        clusterPhy = new ClusterPhy(0, CLUSTER, "desc", "readAddress", "writeAddress", "httpAddress",
            "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
            "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator",
            Collections.singletonList(clusterRoleInfo), Collections.singletonList(clusterRoleHost), 0, "writeAction", 0,
            0L, 0L, 0L, 0.0, "platformType", 1, "gatewayUrl");
        privateClusterPhy = new ClusterPhy(0, CLUSTER, "desc", "readAddress", "writeAddress", "httpAddress",
            "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
            "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator",
            Collections.singletonList(clusterRoleInfo), Collections.singletonList(clusterRoleHost), 0, "writeAction", 0,
            0L, 0L, 0L, 0.0, "platformType", 1, "gatewayUrl");
        exclusiveClusterPhy = new ClusterPhy(0, CLUSTER, "desc", "readAddress", "writeAddress", "httpAddress",
            "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
            "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator",
            Collections.singletonList(clusterRoleInfo), Collections.singletonList(clusterRoleHost), 0, "writeAction", 0,
            0L, 0L, 0L, 0.0, "platformType", 1, "gatewayUrl");
        clusterPhyList = Collections.singletonList(clusterPhy);
        esClusterStatsResponse = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
            new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
            new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(0L, ByteSizeUnit.BYTES),
            new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
            new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        clusterRoleInfos = Collections.singletonList(clusterRoleInfo);
        clusterPhyVOWithNotRole = new ClusterPhyVO(0, CLUSTER, "desc", "readAddress", "writeAddress", "httpAddress",
            "httpWriteAddress", 0, "tags", "dataCenter", 0, "machineSpec", 0, "esVersion", "imageName", null,
            Lists.newArrayList(), 0.0, 0L, 0L, "password", "idc", 0, "writeAction", 0, 0L, "platformType", 1,
            "gatewayUrl", null);

        clusterLogic = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible",
            "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0,0D,0L,0L,"",0);
        region = new ClusterRegion(0L, "name", "logicClusterIds", CLUSTER, "config");

        roleHostList = Collections.singletonList(new ClusterRoleHost(0L, 0L, "hostname", "ip", CLUSTER, "port", 0, 0,
            "rack", "nodeSet", "machineSpec", -1, "attributes"));
        regions = Collections.singletonList(region);
    }

    @Test
    void testCopyMapping() throws Exception {
        final Result<MappingConfig> succ = Result.buildSucc(new MappingConfig(new JSONObject(0, false)));
        final Result<MappingConfig> fail = Result.buildFail();
        when(mockIndexTemplatePhyService.getNormalTemplateByCluster(CLUSTER)).thenReturn(indexTemplatePhyList);
        when(mockIndexTemplateService.getLogicTemplateById(0)).thenReturn(indexTemplate);
        when(mockTemplatePhyMappingManager.syncMappingConfig(CLUSTER, TEMPLATE, EXPRESSION, DATE_FORMAT))
            .thenReturn(succ);
        when(mockEsTemplateService.syncUpsertSetting(CLUSTER, TEMPLATE, new HashMap<>(), 0)).thenReturn(false);
        assertTrue(clusterPhyManager.copyMapping(CLUSTER, 0));

        when(mockTemplatePhyMappingManager.syncMappingConfig(CLUSTER, TEMPLATE, EXPRESSION, DATE_FORMAT))
            .thenReturn(fail);
        when(mockEsTemplateService.syncUpsertSetting(CLUSTER, TEMPLATE, new HashMap<>(), 0)).thenReturn(false);
        assertFalse(clusterPhyManager.copyMapping(CLUSTER, 0));

        when(mockIndexTemplatePhyService.getNormalTemplateByCluster(CLUSTER)).thenReturn(Collections.emptyList());
        assertTrue(clusterPhyManager.copyMapping(CLUSTER, 0));

        when(mockTemplatePhyMappingManager.syncMappingConfig(CLUSTER, TEMPLATE, EXPRESSION, DATE_FORMAT))
            .thenThrow(RuntimeException.class);
        assertTrue(clusterPhyManager.copyMapping(CLUSTER, 0));
    }

    @Test
    void testSyncTemplateMetaData() throws Exception {
        when(mockIndexTemplatePhyService.getNormalTemplateByCluster(CLUSTER)).thenReturn(indexTemplatePhyList);
        when(mockIndexTemplateService.getLogicTemplateWithPhysicalsById(LOGIC_TEMPLATE_ID))
            .thenReturn(indexTemplateWithPhyTemplates);

        clusterPhyManager.syncTemplateMetaData(CLUSTER, 0);

        verify(mockTemplatePhyManager).syncMeta(PHYSICAL_ID, 0);
        verify(mockTemplatePipelineManager).syncPipeline(indexTemplatePhy, indexTemplateWithPhyTemplates);
    }

    @Test
    void testSyncTemplateMetaData_IndexTemplatePhyServiceReturnsNoItems() throws ESOperateException {
        when(mockIndexTemplatePhyService.getNormalTemplateByCluster(CLUSTER)).thenReturn(Collections.emptyList());
        when(mockIndexTemplateService.getLogicTemplateWithPhysicalsById(LOGIC_TEMPLATE_ID))
            .thenReturn(indexTemplateWithPhyTemplates);

        clusterPhyManager.syncTemplateMetaData(CLUSTER, 0);

        verify(mockTemplatePhyManager, times(0)).syncMeta(PHYSICAL_ID, 0);
        verify(mockTemplatePipelineManager, times(0)).syncPipeline(indexTemplatePhy, indexTemplateWithPhyTemplates);

    }

    @Test
    void testSyncTemplateMetaData_TemplatePhyManagerThrowsESOperateException() throws Exception {
        when(mockIndexTemplatePhyService.getNormalTemplateByCluster(CLUSTER)).thenReturn(indexTemplatePhyList);

        doThrow(ESOperateException.class).when(mockTemplatePhyManager).syncMeta(PHYSICAL_ID, 0);

        clusterPhyManager.syncTemplateMetaData(CLUSTER, 0);
    }

    @Test
    void testIsClusterExists() {
        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(false);

        final boolean result = clusterPhyManager.isClusterExists(CLUSTER);

        assertFalse(result);
    }

    @Test
    void testGetClusterPhys() throws InvocationTargetException, IllegalAccessException {

        when(mockClusterPhyService.listClustersByCondt(clusterPhyDTO)).thenReturn(clusterPhyList);

        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(false);

        when(mockEsClusterService.syncGetClusterStats(CLUSTER)).thenReturn(esClusterStatsResponse);

        when(mockClusterRoleService.getAllRoleClusterByClusterId(0)).thenReturn(clusterRoleInfos);

        when(mockClusterRoleHostService.getByRoleClusterIds(Collections.singletonList(0L))).thenReturn(new HashMap<>());

        final List<ClusterPhyVO> result = clusterPhyManager.listClusterPhys(clusterPhyDTO);
        ClusterPhyVO vo = new ClusterPhyVO();
        BeanUtils.copyProperties(vo, clusterPhyVO);
        vo.setResourceType(1);
        assertEquals(Collections.singletonList(vo), result);
    }

    @Test
    void testGetClusterPhys_ClusterPhyServiceGetClustersByCondtReturnsNoItems() {

        when(mockClusterPhyService.listClustersByCondt(clusterPhyDTO)).thenReturn(Collections.emptyList());
        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(false);

        when(mockEsClusterService.syncGetClusterStats(CLUSTER)).thenReturn(esClusterStatsResponse);
        when(mockClusterRoleService.getAllRoleClusterByClusterId(0)).thenReturn(clusterRoleInfos);

        when(mockClusterRoleHostService.getByRoleClusterIds(Collections.singletonList(0L))).thenReturn(new HashMap<>());

        final List<ClusterPhyVO> result = clusterPhyManager.listClusterPhys(clusterPhyDTO);

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void testGetClusterPhys_ESClusterServiceReturnsNull() throws InvocationTargetException, IllegalAccessException {
        // Setup
        when(mockClusterPhyService.listClustersByCondt(clusterPhyDTO)).thenReturn(clusterPhyList);

        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(false);
        when(mockEsClusterService.syncGetClusterStats(CLUSTER)).thenReturn(null);

        when(mockClusterRoleService.getAllRoleClusterByClusterId(0)).thenReturn(clusterRoleInfos);

        when(mockClusterRoleHostService.getByRoleClusterIds(Collections.singletonList(0L))).thenReturn(new HashMap<>());

        final List<ClusterPhyVO> result = clusterPhyManager.listClusterPhys(clusterPhyDTO);
        ClusterPhyVO vo = new ClusterPhyVO();
        BeanUtils.copyProperties(vo, clusterPhyVO);
        vo.setResourceType(1);
        assertEquals(Collections.singletonList(vo), result);
    }

    @Test
    void testGetClusterPhys_ClusterRoleServiceReturnsNoItems() {
        when(mockClusterPhyService.listClustersByCondt(clusterPhyDTO)).thenReturn(clusterPhyList);

        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(false);

        when(mockEsClusterService.syncGetClusterStats(CLUSTER)).thenReturn(esClusterStatsResponse);

        when(mockClusterRoleService.getAllRoleClusterByClusterId(0)).thenReturn(Collections.emptyList());
        when(mockClusterRoleHostService.getByRoleClusterIds(Collections.singletonList(0L))).thenReturn(new HashMap<>());

        final List<ClusterPhyVO> result = clusterPhyManager.listClusterPhys(clusterPhyDTO);

        assertEquals(Collections.singletonList(clusterPhyVOWithNotRole), result);
    }

    @Test
    void testBuildClusterInfo() {
        assertEquals(Collections.emptyList(), clusterPhyManager.buildClusterInfo(Collections.emptyList()));

        when(mockClusterRoleService.getAllRoleClusterByClusterIds(Collections.singletonList(0)))
            .thenReturn(new HashMap<>());
        when(mockClusterRoleHostService.getByRoleClusterIds(Collections.singletonList(0L))).thenReturn(new HashMap<>());
        assertEquals(Collections.singletonList(clusterPhyVOWithNotRole),
            clusterPhyManager.buildClusterInfo(clusterPhyList));
    }

    @Test
    void testGetClusterPhyOverview() throws InvocationTargetException, IllegalAccessException {
        when(mockClusterPhyService.getClusterById(0)).thenReturn(clusterPhy);

        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(false);

        when(mockEsClusterService.syncGetClusterStats(CLUSTER)).thenReturn(esClusterStatsResponse);

        when(mockClusterRoleService.getAllRoleClusterByClusterId(0)).thenReturn(clusterRoleInfos);

        when(mockClusterRoleHostService.getByRoleClusterIds(Collections.singletonList(0L))).thenReturn(new HashMap<>());

        final ClusterPhyVO result = clusterPhyManager.getClusterPhyOverview(0, 0);
        ClusterPhyVO vo = new ClusterPhyVO();
        BeanUtils.copyProperties(vo, clusterPhyVO);
        vo.setResourceType(1);
        assertEquals(vo, result);
    }

    @Test
    void testGetClusterPhyOverview_ClusterPhyServiceGetClusterByIdReturnsNull() {
        when(mockClusterPhyService.getClusterById(0)).thenReturn(null);

        final ClusterPhyVO result = clusterPhyManager.getClusterPhyOverview(0, 0);

        assertEquals(new ClusterPhyVO(), result);
    }

    @Test
    void testGetClusterPhyOverview_ESClusterServiceReturnsNull() throws InvocationTargetException,
                                                                 IllegalAccessException {
        when(mockClusterPhyService.getClusterById(0)).thenReturn(clusterPhy);

        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(false);
        when(mockEsClusterService.syncGetClusterStats(CLUSTER)).thenReturn(null);

        when(mockClusterRoleService.getAllRoleClusterByClusterId(0)).thenReturn(clusterRoleInfos);

        when(mockClusterRoleHostService.getByRoleClusterIds(Arrays.asList(0L))).thenReturn(new HashMap<>());

        final ClusterPhyVO result = clusterPhyManager.getClusterPhyOverview(0, 0);
        ClusterPhyVO vo = new ClusterPhyVO();
        BeanUtils.copyProperties(vo, clusterPhyVO);
        vo.setResourceType(1);
        assertEquals(vo, result);
    }

    @Test
    void testGetClusterPhyOverview_ClusterRoleServiceReturnsNoItems() {
        when(mockClusterPhyService.getClusterById(0)).thenReturn(clusterPhy);

        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(false);

        when(mockEsClusterService.syncGetClusterStats(CLUSTER)).thenReturn(esClusterStatsResponse);

        when(mockClusterRoleService.getAllRoleClusterByClusterId(0)).thenReturn(Collections.emptyList());
        when(mockClusterRoleHostService.getByRoleClusterIds(Collections.singletonList(0L))).thenReturn(new HashMap<>());

        final ClusterPhyVO result = clusterPhyManager.getClusterPhyOverview(0, 0);

        assertEquals(clusterPhyVOWithNotRole, result);
    }

    @Test
    void testListCanBeAssociatedRegionOfClustersPhys() {

        Result<List<String>> result = clusterPhyManager.listCanBeAssociatedRegionOfClustersPhys(0, 0L);
        assertEquals(Result.buildParamIllegal("集群资源类型非法").getMessage(), result.getMessage());

        when(mockClusterLogicService.getClusterLogicById(0L)).thenReturn(clusterLogic);
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(region);
        result = clusterPhyManager.listCanBeAssociatedRegionOfClustersPhys(1, 0L);
        assertEquals(Collections.emptyList(), result.getData());

        List<String> clusterNames = Collections.singletonList(CLUSTER);
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);
        ClusterPhyDTO clusterPhyDTO = new ClusterPhyDTO();
        clusterPhyDTO.setResourceType(1);
        when(mockClusterPhyService.listClustersByCondt(clusterPhyDTO)).thenReturn(clusterPhyList);
        result = clusterPhyManager.listCanBeAssociatedRegionOfClustersPhys(1, 0L);
        assertEquals(clusterNames, result.getData());
    }

    @Test
    void testListCanBeAssociatedClustersPhys() {
        Result<List<String>> result = clusterPhyManager.listCanBeAssociatedClustersPhys(0);
        assertEquals(Result.buildParamIllegal("集群资源类型非法").getMessage(), result.getMessage());

        List<String> clusterNames = Collections.singletonList(CLUSTER);
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);
        ClusterPhyDTO clusterPhyDTO = new ClusterPhyDTO();
        clusterPhyDTO.setResourceType(1);
        when(mockClusterPhyService.listClustersByCondt(clusterPhyDTO)).thenReturn(clusterPhyList);
        result = clusterPhyManager.listCanBeAssociatedClustersPhys(1);
        assertEquals(clusterNames, result.getData());

        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);
        clusterPhyDTO = new ClusterPhyDTO();
        clusterPhyDTO.setResourceType(3);
        when(mockClusterPhyService.listClustersByCondt(clusterPhyDTO))
            .thenReturn(Collections.singletonList(privateClusterPhy));
        when(mockClusterRegionService.listPhyClusterRegions(CLUSTER)).thenReturn(regions);
        when(mockClusterRegionService.isRegionBound(region)).thenReturn(false);
        result = clusterPhyManager.listCanBeAssociatedClustersPhys(3);
        assertEquals(clusterNames, result.getData());

        when(mockClusterRegionService.isRegionBound(region)).thenReturn(true);
        when(mockClusterRoleHostService.getByRoleAndClusterId(0L, "datanode")).thenReturn(roleHostList);
        result = clusterPhyManager.listCanBeAssociatedClustersPhys(3);
        assertEquals(clusterNames, result.getData());

        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);
        clusterPhyDTO = new ClusterPhyDTO();
        clusterPhyDTO.setResourceType(2);
        when(mockClusterPhyService.listClustersByCondt(clusterPhyDTO))
            .thenReturn(Collections.singletonList(exclusiveClusterPhy));
        when(mockClusterRegionService.getLogicClusterIdByPhyClusterId(0)).thenReturn(new HashSet<>());
        result = clusterPhyManager.listCanBeAssociatedClustersPhys(2);
        assertEquals(clusterNames, result.getData());
    }

    @Test
    void testJoinCluster() throws InvocationTargetException, IllegalAccessException, AdminTaskException {
        Integer projectId = 1;
        ClusterJoinDTO param = new ClusterJoinDTO(0, 0, "clusterPhyName", "operator", "esVersion", Lists.newArrayList(),
            "desc", "passwd", 4, "{\"createSource\":1}", "cn", "acs", 1);
        ESClusterRoleHostDTO roleHostDTO = new ESClusterRoleHostDTO(0L, 0L, "hostname", "", CLUSTER, "port", false, 0,
            0, "nodeSet", 0, "attributes", "16c-32g-1t");
        assertEquals(Result.buildParamIllegal("参数为空").getMessage(),
            clusterPhyManager.joinCluster(null, "admin", projectId).getMessage());
        assertEquals(Result.buildParamIllegal("操作人不存在").getMessage(),
            clusterPhyManager.joinCluster(param, null, projectId).getMessage());
        assertEquals(Result.buildParamIllegal("非支持的集群类型").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());
        param.setType(4);
        param.setResourceType(0);
        assertEquals(Result.buildParamIllegal("非支持的集群资源类型").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());
        param.setResourceType(1);
        assertEquals(Result.buildParamIllegal("非集群接入来源").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());
        param.setTags("{\"createSource\":0}");
        assertEquals(Result.buildParamIllegal("非支持的接入规则").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());
        param.setImportRule(1);
        assertEquals(Result.buildParamIllegal("集群节点信息为空").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());
        param.setRoleClusterHosts(Collections.singletonList(roleHostDTO));
        assertEquals(Result.buildParamIllegal("接入集群中端口号存在异常[port]").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());
        roleHostDTO.setPort("997");
        param.setRoleClusterHosts(Collections.singletonList(roleHostDTO));
        assertEquals(Result.buildParamIllegal("集群缺少类型为masternode的节点").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());
        roleHostDTO.setRole(3);
        roleHostDTO.setIp("127.0.0.1");
        ESClusterRoleHostDTO clientNode = new ESClusterRoleHostDTO();
        BeanUtils.copyProperties(clientNode, roleHostDTO);
        clientNode.setRole(2);
        ESClusterRoleHostDTO dataNode = new ESClusterRoleHostDTO();
        BeanUtils.copyProperties(dataNode, roleHostDTO);
        dataNode.setRole(1);
        param.setRoleClusterHosts(Arrays.asList(roleHostDTO, roleHostDTO, clientNode, clientNode, dataNode, dataNode));
        assertEquals(Result.buildParamIllegal("集群ip:127.0.0.1重复, 请重新输入").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());
        param.setRoleClusterHosts(Arrays.asList(roleHostDTO, clientNode, clientNode, dataNode, dataNode));
        assertEquals(Result.buildParamIllegal("集群ip:127.0.0.1重复, 请重新输入").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());
        param.setRoleClusterHosts(Arrays.asList(roleHostDTO, clientNode, dataNode, dataNode));
        assertEquals(Result.buildParamIllegal("集群ip:127.0.0.1重复, 请重新输入").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());

        param.setImportRule(0);
        roleHostDTO.setIp("");
        param.setRoleClusterHosts(Arrays.asList(roleHostDTO, roleHostDTO, roleHostDTO));
        assertEquals(Result.buildParamIllegal(String.format("集群%s的节点个数要求大于等于1，且不重复", param.getCluster())).getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());
        roleHostDTO.setIp("127.0.0.1");
        param.setRoleClusterHosts(Arrays.asList(roleHostDTO, clientNode, dataNode));
        assertEquals(Result.buildParamIllegal("集群ip:127.0.0.1重复, 请重新输入").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());

        param.setRoleClusterHosts(Collections.singletonList(roleHostDTO));
        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(true);
        assertEquals(Result.buildParamIllegal(String.format("物理集群名称:%s已存在", param.getCluster())).getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());

        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(false);
        when(mockClusterRoleHostService.buildESClientHttpAddressesStr(Mockito.anyList()))
            .thenReturn("esClientHttpAddressesStr");
        when(mockEsClusterService.checkClusterPassword("esClientHttpAddressesStr", null))
            .thenReturn(ClusterConnectionStatus.DISCONNECTED);
        assertEquals(Result.buildParamIllegal("集群离线未能连通").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());

        when(mockEsClusterService.checkClusterPassword("esClientHttpAddressesStr", null))
            .thenReturn(ClusterConnectionStatus.NORMAL);
        assertEquals(Result.buildParamIllegal("未设置密码的集群，请勿输入账户信息").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());

        when(mockEsClusterService.checkClusterPassword("esClientHttpAddressesStr", null))
            .thenReturn(ClusterConnectionStatus.UNAUTHORIZED);
        when(mockEsClusterService.checkClusterPassword("esClientHttpAddressesStr", "passwd"))
            .thenReturn(ClusterConnectionStatus.UNAUTHORIZED);
        assertEquals(Result.buildParamIllegal("集群的账户信息错误").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());

        param.setPassword(null);
        assertEquals(Result.buildParamIllegal("集群设置有密码，请输入账户信息").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());

        when(mockEsClusterService.checkClusterPassword("esClientHttpAddressesStr", null))
            .thenReturn(ClusterConnectionStatus.NORMAL);
        when(mockClusterRoleHostService.buildESAllRoleHttpAddressesList(Mockito.anyList()))
            .thenReturn(Lists.newArrayList("esClientHttpAddressesStr"));
        when(mockEsClusterService.checkSameCluster(Mockito.any(), Mockito.any())).thenReturn(Result.buildFail());
        assertEquals(Result.buildParamIllegal("禁止同时接入超过两个不同集群节点").getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());

        when(mockEsClusterService.checkSameCluster(Mockito.any(), Mockito.any())).thenReturn(Result.buildSucc());
        when(mockEsClusterService.synGetESVersionByHttpAddress("esClientHttpAddressesStr", "passwd")).thenReturn(null);
        when(mockEsClusterService.synGetESVersionByHttpAddress("esClientHttpAddressesStr", null)).thenReturn(null);
        assertEquals(Result.buildParamIllegal(String.format("%s无法获取es版本", "esClientHttpAddressesStr")).getMessage(),
            clusterPhyManager.joinCluster(param, "admin", projectId).getMessage());

        when(mockEsClusterService.synGetESVersionByHttpAddress("esClientHttpAddressesStr", null))
            .thenReturn("7.6.0.1401");
        param.setDataCenter("");
        when(mockClusterRoleHostService.collectClusterNodeSettings(CLUSTER)).thenReturn(true);
        when(mockClusterPhyService.createCluster(Mockito.any(), Mockito.anyString())).thenReturn(Result.buildSucc());
        assertTrue(clusterPhyManager.joinCluster(param, "admin", projectId).success());
        verify(mockEsOpClient).connect(CLUSTER);
        verify(mockClusterRoleHostService).collectClusterNodeSettings(CLUSTER);

        param.setImportRule(1);
        when(mockClusterPhyService.getClusterByName(CLUSTER)).thenReturn(clusterPhy);
        when(mockEsClusterService.syncGetClusterHealthEnum(CLUSTER)).thenReturn(ClusterHealthEnum.GREEN);

        when(mockClusterPhyService.editCluster(Mockito.any(), Mockito.anyString())).thenReturn(Result.buildFail(null));
        assertTrue(clusterPhyManager.joinCluster(param, "admin", projectId).success());

        verify(mockClusterRoleHostService).saveClusterNodeSettings(Mockito.any());

    }

    @Test
    void testDeleteClusterJoin() {
        final Integer projectId = 1;
        when(mockClusterPhyService.getClusterById(0)).thenReturn(null);
        assertEquals(Result.buildParamIllegal("物理集群不存在").getMessage(),
            clusterPhyManager.deleteClusterJoin(0, "operator", projectId).getMessage());

        when(mockClusterPhyService.getClusterById(0)).thenReturn(clusterPhy);
        when(mockClusterContextManager.getClusterPhyContext("clusterPhyName")).thenReturn(null);
        assertEquals(Result.buildSucc(), clusterPhyManager.deleteClusterJoin(0, "operator", projectId));
    }

    @Test
    void testGetPhyClusterDynamicConfigs() {
        when(mockClusterPhyService.isClusterExists(Mockito.anyString())).thenReturn(false);
        assertEquals(Result.buildFail("集群[" + CLUSTER + "]不存在").getMessage(),
            clusterPhyManager.getPhyClusterDynamicConfigs(CLUSTER).getMessage());

        when(mockClusterPhyService.isClusterExists(Mockito.anyString())).thenReturn(true);
        when(mockEsClusterService.syncGetClusterSetting(CLUSTER)).thenReturn(null);

        assertEquals(Result.buildFail(String.format("获取集群动态配置信息失败, 请确认是否集群[%s]是否正常", CLUSTER)).getMessage(),
            clusterPhyManager.getPhyClusterDynamicConfigs(CLUSTER).getMessage());
        final ESClusterGetSettingsAllResponse esClusterGetSettingsAllResponse = new ESClusterGetSettingsAllResponse(
            new JSONObject(0, false));
        when(mockEsClusterService.syncGetClusterSetting(CLUSTER)).thenReturn(esClusterGetSettingsAllResponse);

        assertTrue(clusterPhyManager.getPhyClusterDynamicConfigs(CLUSTER).success());
    }

    @Test
    void testUpdatePhyClusterDynamicConfig() {
        final ClusterSettingDTO param = new ClusterSettingDTO("clusterName", "key", "value");
        final Result<Boolean> expectedResult = Result.buildFail(false);
        when(mockClusterPhyService.updatePhyClusterDynamicConfig(new ClusterSettingDTO("clusterName", "key", "value")))
            .thenReturn(Result.buildFail(false));
        Integer projectId = 1;
        final Result<Boolean> result = clusterPhyManager.updatePhyClusterDynamicConfig(param, "operator", projectId);

        assertEquals(expectedResult, result);
    }

    @Test
    void testGetAppClusterPhyNames() {
        when(mockClusterPhyService.listAllClusters()).thenReturn(clusterPhyList);
        assertEquals(Collections.singletonList(CLUSTER), clusterPhyManager.listClusterPhyNameByProjectId(0));
    }

    @Test
    void testGetAppClusterPhyNodeNames() {

        when(mockEsClusterNodeService.syncGetNodeNames("clusterPhyName"))
            .thenReturn(Collections.singletonList("value"));
        assertEquals(Collections.singletonList("value"), clusterPhyManager.listClusterPhyNodeName("clusterPhyName"));

        when(mockEsClusterNodeService.syncGetNodeNames("clusterPhyName")).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), clusterPhyManager.listClusterPhyNodeName("clusterPhyName"));

        assertEquals(Collections.emptyList(), clusterPhyManager.listClusterPhyNodeName(null));
    }

    @Test
    void testDeleteCluster() {
        final Integer projectId = 1;
        when(mockClusterPhyService.getClusterById(0)).thenReturn(null);
        assertEquals(Result.buildFail(String.format("物理集群Id[%s]不存在", 0)),
            clusterPhyManager.deleteCluster(0, "operator", projectId));

        when(mockClusterRegionService.getLogicClusterIdByPhyClusterId(0)).thenReturn(Collections.emptySet());
        when(mockIndexTemplatePhyService.getNormalTemplateByCluster(CLUSTER)).thenReturn(Collections.emptyList());
        when(mockClusterPhyService.getClusterById(0)).thenReturn(clusterPhy);
        when(mockClusterRoleHostService.getNodesByCluster(CLUSTER)).thenReturn(Collections.emptyList());
        when(mockClusterRoleService.deleteRoleClusterByClusterId(0, projectId)).thenReturn(Result.buildSucc());
        when(mockClusterRegionService.listPhyClusterRegions(CLUSTER)).thenReturn(Collections.emptyList());
        when(mockClusterPhyService.deleteClusterById(0, projectId)).thenReturn(Result.buildSucc(true));
        assertEquals(Result.buildSucc(true), clusterPhyManager.deleteCluster(0, "operator", projectId));

        when(mockClusterRoleHostService.getNodesByCluster(CLUSTER)).thenReturn(roleHostList);
        when(mockClusterRoleHostService.deleteByCluster(CLUSTER, projectId)).thenReturn(Result.buildSucc());
        when(mockClusterRoleService.deleteRoleClusterByClusterId(0, projectId)).thenReturn(Result.buildSucc());

        when(mockClusterRegionService.listPhyClusterRegions(CLUSTER)).thenReturn(regions);
        when(mockClusterRegionService.deleteByClusterPhy(CLUSTER)).thenReturn(Result.buildSucc());
        when(mockClusterPhyService.deleteClusterById(0, projectId)).thenReturn(Result.buildSucc(true));
        assertEquals(Result.buildSucc(true), clusterPhyManager.deleteCluster(0, "operator", projectId));
    }

    @Test
    void testAddCluster() {
        when(mockClusterPhyService.createCluster(clusterPhyDTO, "operator")).thenReturn(Result.buildBoolen(true));
        assertEquals(Result.buildBoolen(true), clusterPhyManager.addCluster(clusterPhyDTO, "operator", 0));
    }

    @Test
    void testBuildPhyClusterStatics() {
        when(mockClusterPhyService.isClusterExists(CLUSTER)).thenReturn(false);

        when(mockEsClusterService.syncGetClusterStats(CLUSTER)).thenReturn(esClusterStatsResponse);

        clusterPhyManager.buildPhyClusterStatics(clusterPhyVO);

    }

    @Test
    void testBuildClusterRole1() {

        when(mockClusterRoleService.getAllRoleClusterByClusterId(0)).thenReturn(clusterRoleInfos);

        when(mockClusterRoleHostService.getByRoleClusterIds(Collections.singletonList(0L))).thenReturn(new HashMap<>());

        clusterPhyManager.buildClusterRole(clusterPhyVO);

    }

    @Test
    void testBuildClusterRole2() {
        when(mockClusterRoleHostService.getByRoleClusterIds(Collections.singletonList(0L))).thenReturn(new HashMap<>());

        clusterPhyManager.buildClusterRole(clusterPhyVO, clusterRoleInfos);

    }

    @Test
    void testUpdateClusterHealth() {
        when(mockClusterPhyService.getClusterByName(CLUSTER)).thenReturn(null);
        assertFalse(clusterPhyManager.updateClusterHealth(CLUSTER, "operator"));

        when(mockClusterPhyService.getClusterByName(CLUSTER)).thenReturn(clusterPhy);
        when(mockEsClusterService.syncGetClusterHealthEnum(CLUSTER)).thenReturn(ClusterHealthEnum.GREEN);
        when(mockClusterPhyService.editCluster(Mockito.any(), Mockito.anyString())).thenReturn(Result.buildFail(null));
        assertFalse(clusterPhyManager.updateClusterHealth(CLUSTER, "operator"));

        when(mockClusterPhyService.editCluster(Mockito.any(), Mockito.anyString())).thenReturn(Result.buildSucc());
        assertTrue(clusterPhyManager.updateClusterHealth(CLUSTER, "operator"));
    }

    @Test
    void testUpdateClusterInfo() {
        when(mockClusterPhyService.getClusterByName(CLUSTER)).thenReturn(null);
        assertFalse(clusterPhyManager.updateClusterInfo(CLUSTER, "operator"));

        when(mockClusterPhyService.getClusterByName(CLUSTER)).thenReturn(clusterPhy);
        ESClusterStatsResponse response = new ESClusterStatsResponse("status", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
            new ByteSizeValue(0L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
            new ByteSizeValue(0L, ByteSizeUnit.BYTES), 0L, 0L, new ByteSizeValue(100L, ByteSizeUnit.BYTES),
            new ByteSizeValue(40L, ByteSizeUnit.BYTES), new ByteSizeValue(0L, ByteSizeUnit.BYTES),
            new ByteSizeValue(0L, ByteSizeUnit.BYTES));
        when(mockEsClusterService.syncGetClusterStats(CLUSTER)).thenReturn(response);
        when(mockClusterPhyService.editCluster(Mockito.any(), Mockito.anyString())).thenReturn(Result.buildFail(null));
        assertFalse(clusterPhyManager.updateClusterInfo(CLUSTER, "operator"));

        when(mockClusterPhyService.editCluster(Mockito.any(), Mockito.anyString())).thenReturn(Result.buildSucc());
        assertTrue(clusterPhyManager.updateClusterInfo(CLUSTER, "operator"));
    }

    @Test
    void testCheckClusterHealth() {
        when(mockClusterPhyService.getClusterByName(CLUSTER)).thenReturn(null);
        assertFalse(clusterPhyManager.checkClusterHealth(CLUSTER, "operator").success());
        ClusterPhy clusterPhy = new ClusterPhy();
        clusterPhy.setHealth(0);
        when(mockClusterPhyService.getClusterByName(CLUSTER)).thenReturn(clusterPhy);
        assertTrue(clusterPhyManager.checkClusterHealth(CLUSTER, "operator").getData());
        clusterPhy.setHealth(2);
        when(mockEsClusterService.syncGetClusterHealthEnum(CLUSTER)).thenReturn(ClusterHealthEnum.GREEN);
        when(mockClusterPhyService.editCluster(Mockito.any(), Mockito.anyString())).thenReturn(Result.buildSucc());
        assertEquals(Result.buildSucc(), clusterPhyManager.checkClusterHealth(CLUSTER, "operator"));
    }

    @Test
    void testUpdateClusterGateway() {
        when(mockClusterPhyService.getClusterById(0)).thenReturn(clusterPhy);

        when(mockClusterPhyService.editCluster(Mockito.any(), Mockito.anyString())).thenReturn(Result.buildFail(false));
        assertEquals(Result.buildFail("编辑gateway失败！").getMessage(),
            clusterPhyManager.updateClusterGateway(clusterPhyDTO, "operator").getMessage());

        when(mockClusterPhyService.editCluster(Mockito.any(), Mockito.anyString())).thenReturn(Result.buildSucc(true));
        assertTrue(clusterPhyManager.updateClusterGateway(clusterPhyDTO, "operator").success());

    }

    @Test
    void testListClusterRolesByClusterId() {
        // Setup
        final List<ClusterRoleInfo> expectedResult = Arrays.asList(new ClusterRoleInfo(0L, 0L, "roleClusterName",
            "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds", false, Arrays.asList(new ClusterRoleHost(0L, 0L,
                "hostname", "ip", CLUSTER, "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes"))));

        // Configure ClusterRoleService.getAllRoleClusterByClusterId(...).
        final List<ClusterRoleInfo> clusterRoleInfos = Arrays.asList(new ClusterRoleInfo(0L, 0L, "roleClusterName",
            "role", 0, 0, "machineSpec", "esVersion", 0, "plugIds", false, Arrays.asList(new ClusterRoleHost(0L, 0L,
                "hostname", "ip", CLUSTER, "port", 0, 0, "rack", "nodeSet", "machineSpec", 0, "attributes"))));
        when(mockClusterRoleService.getAllRoleClusterByClusterId(0)).thenReturn(clusterRoleInfos);

        // Run the test
        final List<ClusterRoleInfo> result = clusterPhyManager.listClusterRolesByClusterId(0);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    void testListClusterRolesByClusterId_ClusterRoleServiceReturnsNoItems() {
        // Setup
        when(mockClusterRoleService.getAllRoleClusterByClusterId(0)).thenReturn(Collections.emptyList());

        // Run the test
        final List<ClusterRoleInfo> result = clusterPhyManager.listClusterRolesByClusterId(0);

        // Verify the results
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void getTemplateSameVersionClusterNamesByTemplateIdTest() {
        Result<List<String>> rest = clusterPhyManager.getTemplateSameVersionClusterNamesByTemplateId(1, 37529);
        Assertions.assertTrue(rest.success());

        when(mockClusterPhyService.listAllClusters()).thenReturn(clusterPhyList);
        when(mockIndexTemplateService.getLogicTemplateWithPhysicalsById(Mockito.any()))
            .thenReturn(new IndexTemplateWithPhyTemplates(null));
        Assertions.assertEquals(
            Result.buildFail(String.format("the physicals of templateId[%s] is empty", 37529)).getMessage(),
            clusterPhyManager.getTemplateSameVersionClusterNamesByTemplateId(1, 37529).getMessage());

        when(mockIndexTemplateService.getLogicTemplateWithPhysicalsById(Mockito.any()))
            .thenReturn(indexTemplateWithPhyTemplates);
        when(mockClusterPhyService.getClusterByName(CLUSTER)).thenReturn(clusterPhy);
        rest = clusterPhyManager.getTemplateSameVersionClusterNamesByTemplateId(1, 37529);
        Assertions.assertTrue(rest.success());
    }

    @Test
    public void testDeleteClusterExit() {
        int projectId = 1;
        Assertions.assertEquals(Result.buildFail("无权限删除集群").getMessage(),
            clusterPhyManager.deleteClusterExit(CLUSTER, 2, "operator").getMessage());
        when(mockClusterPhyService.getClusterByName(CLUSTER)).thenReturn(clusterPhy);
        when(mockClusterPhyService.getClusterById(0)).thenReturn(clusterPhy);
        when(mockClusterRoleHostService.getNodesByCluster(CLUSTER)).thenReturn(Collections.emptyList());
        when(mockClusterRoleService.deleteRoleClusterByClusterId(0, projectId)).thenReturn(Result.buildSucc());
        when(mockClusterRegionService.listPhyClusterRegions(CLUSTER)).thenReturn(Collections.emptyList());
        when(mockClusterPhyService.deleteClusterById(0, projectId)).thenReturn(Result.buildSucc(true));
        Assertions.assertTrue(clusterPhyManager.deleteClusterExit(CLUSTER, 1, "operator").success());
    }

}