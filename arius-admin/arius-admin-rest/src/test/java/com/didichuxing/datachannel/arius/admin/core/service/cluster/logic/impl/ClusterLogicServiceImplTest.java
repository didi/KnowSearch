package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterNodeSepc;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class ClusterLogicServiceImplTest {
    
    @Mock
    private LogicClusterDAO                mockLogicClusterDAO;
    @Mock
    private ProjectClusterLogicAuthService mockLogicClusterAuthService;
    @Mock
    private ProjectService                 mockProjectService;
    @Mock
    private RoleTool                       mockRoleTool;
    @Mock
    private IndexTemplatePhyService        mockIndexTemplatePhyService;
    @Mock
    private ESPluginService                mockEsPluginService;
    @Mock
    private ClusterPhyService              mockClusterPhyService;
    @Mock
    private ESMachineNormsService          mockEsMachineNormsService;
    @Mock
    private ClusterRegionService           mockClusterRegionService;
    @Mock
    private ClusterRoleHostService         mockClusterRoleHostService;
    
    @InjectMocks
    private ClusterLogicServiceImpl clusterLogicServiceImplUnderTest;
    
    private AutoCloseable mockitoCloseable;
    
    @BeforeEach
    void setUp() {
        mockitoCloseable = openMocks(this);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }
    
    @Test
    void testListClusterLogics() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        final List<ClusterLogic> expectedResult = Arrays.asList(
                new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "libraDepartmentId",
                        "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        
        // Configure LogicClusterDAO.listByCondition(...).
        final List<ClusterLogicPO> clusterLogicPOS = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByCondition(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0))).thenReturn(clusterLogicPOS);
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.listClusterLogics(param);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testListClusterLogics_LogicClusterDAOReturnsNoItems() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        when(mockLogicClusterDAO.listByCondition(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0))).thenReturn(Collections.emptyList());
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.listClusterLogics(param);
        
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
    
    @Test
    void testListAllClusterLogics() {
        // Setup
        final List<ClusterLogic> expectedResult = Arrays.asList(
                new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "libraDepartmentId",
                        "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        
        // Configure LogicClusterDAO.listAll(...).
        final List<ClusterLogicPO> clusterLogicPOS = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listAll()).thenReturn(clusterLogicPOS);
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.listAllClusterLogics();
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testListAllClusterLogics_LogicClusterDAOReturnsNoItems() {
        // Setup
        when(mockLogicClusterDAO.listAll()).thenReturn(Collections.emptyList());
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.listAllClusterLogics();
        
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
    
    @Test
    void testDeleteClusterLogicById() throws Exception {
        // Setup
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure IndexTemplatePhyService.listByRegionId(...).
        final Result<List<IndexTemplatePhy>> listResult = Result.buildFail(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(mockIndexTemplatePhyService.listByRegionId(0)).thenReturn(listResult);
        
        when(mockLogicClusterDAO.delete(0L)).thenReturn(0);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.deleteClusterLogicById(0L, "operator");
        
        // Verify the results
    }
    
    @Test
    void testDeleteClusterLogicById_LogicClusterDAOGetByIdReturnsNull() throws Exception {
        // Setup
        when(mockLogicClusterDAO.getById(0L)).thenReturn(null);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.deleteClusterLogicById(0L, "operator");
        
        // Verify the results
    }
    
    @Test
    void testDeleteClusterLogicById_ClusterRegionServiceReturnsNull() throws Exception {
        // Setup
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);
        when(mockLogicClusterDAO.delete(0L)).thenReturn(0);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.deleteClusterLogicById(0L, "operator");
        
        // Verify the results
    }
    
    @Test
    void testDeleteClusterLogicById_IndexTemplatePhyServiceReturnsNoItem() throws Exception {
        // Setup
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        when(mockIndexTemplatePhyService.listByRegionId(0)).thenReturn(Result.buildSucc());
        when(mockLogicClusterDAO.delete(0L)).thenReturn(0);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.deleteClusterLogicById(0L, "operator");
        
        // Verify the results
    }
    
    @Test
    void testDeleteClusterLogicById_IndexTemplatePhyServiceReturnsNoItems() throws Exception {
        // Setup
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure IndexTemplatePhyService.listByRegionId(...).
        final Result<List<IndexTemplatePhy>> listResult = Result.buildFail(Collections.emptyList());
        when(mockIndexTemplatePhyService.listByRegionId(0)).thenReturn(listResult);
        
        when(mockLogicClusterDAO.delete(0L)).thenReturn(0);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.deleteClusterLogicById(0L, "operator");
        
        // Verify the results
    }
    
    @Test
    void testDeleteClusterLogicById_IndexTemplatePhyServiceReturnsFailure() throws Exception {
        // Setup
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure IndexTemplatePhyService.listByRegionId(...).
        final Result<List<IndexTemplatePhy>> listResult = Result.buildFail();
        when(mockIndexTemplatePhyService.listByRegionId(0)).thenReturn(listResult);
        
        when(mockLogicClusterDAO.delete(0L)).thenReturn(0);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.deleteClusterLogicById(0L, "operator");
        
        // Verify the results
    }
    
    @Test
    void testHasLogicClusterWithTemplates() {
        // Setup
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure IndexTemplatePhyService.listByRegionId(...).
        final Result<List<IndexTemplatePhy>> listResult = Result.buildFail(
                Arrays.asList(
                        new IndexTemplatePhy(0L, 0, "name", "expression", "cluster", "rack", 0, 0, 0, 0, 0, "config",
                                0)));
        when(mockIndexTemplatePhyService.listByRegionId(0)).thenReturn(listResult);
        
        // Run the test
        final Boolean result = clusterLogicServiceImplUnderTest.hasLogicClusterWithTemplates(0L);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testHasLogicClusterWithTemplates_ClusterRegionServiceReturnsNull() {
        // Setup
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);
        
        // Run the test
        final Boolean result = clusterLogicServiceImplUnderTest.hasLogicClusterWithTemplates(0L);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testHasLogicClusterWithTemplates_IndexTemplatePhyServiceReturnsNoItem() {
        // Setup
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        when(mockIndexTemplatePhyService.listByRegionId(0)).thenReturn(Result.buildSucc());
        
        // Run the test
        final Boolean result = clusterLogicServiceImplUnderTest.hasLogicClusterWithTemplates(0L);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testHasLogicClusterWithTemplates_IndexTemplatePhyServiceReturnsNoItems() {
        // Setup
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure IndexTemplatePhyService.listByRegionId(...).
        final Result<List<IndexTemplatePhy>> listResult = Result.buildFail(Collections.emptyList());
        when(mockIndexTemplatePhyService.listByRegionId(0)).thenReturn(listResult);
        
        // Run the test
        final Boolean result = clusterLogicServiceImplUnderTest.hasLogicClusterWithTemplates(0L);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testHasLogicClusterWithTemplates_IndexTemplatePhyServiceReturnsFailure() {
        // Setup
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure IndexTemplatePhyService.listByRegionId(...).
        final Result<List<IndexTemplatePhy>> listResult = Result.buildFail();
        when(mockIndexTemplatePhyService.listByRegionId(0)).thenReturn(listResult);
        
        // Run the test
        final Boolean result = clusterLogicServiceImplUnderTest.hasLogicClusterWithTemplates(0L);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testCreateClusterLogic() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        final Result<Long> expectedResult = Result.buildFail(0L);
        when(mockProjectService.checkProjectExist(0)).thenReturn(false);
        
        // Configure ProjectService.getProjectDetailByProjectId(...).
        final ProjectVO projectVO = new ProjectVO();
        projectVO.setId(0);
        projectVO.setProjectCode("projectCode");
        projectVO.setProjectName("projectName");
        final UserBriefVO userBriefVO = new UserBriefVO();
        userBriefVO.setId(0);
        userBriefVO.setUserName("userName");
        userBriefVO.setRealName("realName");
        userBriefVO.setDeptId(0);
        userBriefVO.setPhone("phone");
        userBriefVO.setEmail("email");
        userBriefVO.setRoleList(Arrays.asList("value"));
        projectVO.setUserList(Arrays.asList(userBriefVO));
        final UserBriefVO userBriefVO1 = new UserBriefVO();
        userBriefVO1.setId(0);
        userBriefVO1.setUserName("userName");
        userBriefVO1.setRealName("realName");
        userBriefVO1.setDeptId(0);
        userBriefVO1.setPhone("phone");
        userBriefVO1.setEmail("email");
        userBriefVO1.setRoleList(Arrays.asList("value"));
        projectVO.setOwnerList(Arrays.asList(userBriefVO1));
        when(mockProjectService.getProjectDetailByProjectId(0)).thenReturn(projectVO);
        
        when(mockRoleTool.isAdmin("targetResponsible")).thenReturn(false);
        
        // Configure LogicClusterDAO.getByName(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getByName("name")).thenReturn(clusterLogicPO);
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO1 = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO1);
        
        when(mockLogicClusterDAO.insert(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0))).thenReturn(0);
        
        // Run the test
        final Result<Long> result = clusterLogicServiceImplUnderTest.createClusterLogic(param);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testCreateClusterLogic_ProjectServiceGetProjectDetailByProjectIdThrowsLogiSecurityException() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        when(mockProjectService.checkProjectExist(0)).thenReturn(false);
        when(mockProjectService.getProjectDetailByProjectId(0)).thenThrow(LogiSecurityException.class);
        
        // Run the test
        assertThatThrownBy(() -> clusterLogicServiceImplUnderTest.createClusterLogic(param))
                .isInstanceOf(LogiSecurityException.class);
    }
    
    @Test
    void testCreateClusterLogic_LogicClusterDAOGetByIdReturnsNull() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        final Result<Long> expectedResult = Result.buildFail(0L);
        when(mockProjectService.checkProjectExist(0)).thenReturn(false);
        
        // Configure ProjectService.getProjectDetailByProjectId(...).
        final ProjectVO projectVO = new ProjectVO();
        projectVO.setId(0);
        projectVO.setProjectCode("projectCode");
        projectVO.setProjectName("projectName");
        final UserBriefVO userBriefVO = new UserBriefVO();
        userBriefVO.setId(0);
        userBriefVO.setUserName("userName");
        userBriefVO.setRealName("realName");
        userBriefVO.setDeptId(0);
        userBriefVO.setPhone("phone");
        userBriefVO.setEmail("email");
        userBriefVO.setRoleList(Arrays.asList("value"));
        projectVO.setUserList(Arrays.asList(userBriefVO));
        final UserBriefVO userBriefVO1 = new UserBriefVO();
        userBriefVO1.setId(0);
        userBriefVO1.setUserName("userName");
        userBriefVO1.setRealName("realName");
        userBriefVO1.setDeptId(0);
        userBriefVO1.setPhone("phone");
        userBriefVO1.setEmail("email");
        userBriefVO1.setRoleList(Arrays.asList("value"));
        projectVO.setOwnerList(Arrays.asList(userBriefVO1));
        when(mockProjectService.getProjectDetailByProjectId(0)).thenReturn(projectVO);
        
        when(mockRoleTool.isAdmin("targetResponsible")).thenReturn(false);
        
        // Configure LogicClusterDAO.getByName(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getByName("name")).thenReturn(clusterLogicPO);
        
        when(mockLogicClusterDAO.getById(0L)).thenReturn(null);
        when(mockLogicClusterDAO.insert(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0))).thenReturn(0);
        
        // Run the test
        final Result<Long> result = clusterLogicServiceImplUnderTest.createClusterLogic(param);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testValidateClusterLogicParams() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        when(mockProjectService.checkProjectExist(0)).thenReturn(false);
        
        // Configure ProjectService.getProjectDetailByProjectId(...).
        final ProjectVO projectVO = new ProjectVO();
        projectVO.setId(0);
        projectVO.setProjectCode("projectCode");
        projectVO.setProjectName("projectName");
        final UserBriefVO userBriefVO = new UserBriefVO();
        userBriefVO.setId(0);
        userBriefVO.setUserName("userName");
        userBriefVO.setRealName("realName");
        userBriefVO.setDeptId(0);
        userBriefVO.setPhone("phone");
        userBriefVO.setEmail("email");
        userBriefVO.setRoleList(Arrays.asList("value"));
        projectVO.setUserList(Arrays.asList(userBriefVO));
        final UserBriefVO userBriefVO1 = new UserBriefVO();
        userBriefVO1.setId(0);
        userBriefVO1.setUserName("userName");
        userBriefVO1.setRealName("realName");
        userBriefVO1.setDeptId(0);
        userBriefVO1.setPhone("phone");
        userBriefVO1.setEmail("email");
        userBriefVO1.setRoleList(Arrays.asList("value"));
        projectVO.setOwnerList(Arrays.asList(userBriefVO1));
        when(mockProjectService.getProjectDetailByProjectId(0)).thenReturn(projectVO);
        
        when(mockRoleTool.isAdmin("targetResponsible")).thenReturn(false);
        
        // Configure LogicClusterDAO.getByName(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getByName("name")).thenReturn(clusterLogicPO);
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO1 = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO1);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.validateClusterLogicParams(param,
                OperationEnum.ADD);
        
        // Verify the results
    }
    
    @Test
    void testValidateClusterLogicParams_ProjectServiceGetProjectDetailByProjectIdThrowsLogiSecurityException() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        when(mockProjectService.checkProjectExist(0)).thenReturn(false);
        when(mockProjectService.getProjectDetailByProjectId(0)).thenThrow(LogiSecurityException.class);
        
        // Run the test
        assertThatThrownBy(() -> clusterLogicServiceImplUnderTest.validateClusterLogicParams(param,
                OperationEnum.ADD))
                .isInstanceOf(LogiSecurityException.class);
    }
    
    @Test
    void testValidateClusterLogicParams_LogicClusterDAOGetByIdReturnsNull() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        when(mockProjectService.checkProjectExist(0)).thenReturn(false);
        
        // Configure ProjectService.getProjectDetailByProjectId(...).
        final ProjectVO projectVO = new ProjectVO();
        projectVO.setId(0);
        projectVO.setProjectCode("projectCode");
        projectVO.setProjectName("projectName");
        final UserBriefVO userBriefVO = new UserBriefVO();
        userBriefVO.setId(0);
        userBriefVO.setUserName("userName");
        userBriefVO.setRealName("realName");
        userBriefVO.setDeptId(0);
        userBriefVO.setPhone("phone");
        userBriefVO.setEmail("email");
        userBriefVO.setRoleList(Arrays.asList("value"));
        projectVO.setUserList(Arrays.asList(userBriefVO));
        final UserBriefVO userBriefVO1 = new UserBriefVO();
        userBriefVO1.setId(0);
        userBriefVO1.setUserName("userName");
        userBriefVO1.setRealName("realName");
        userBriefVO1.setDeptId(0);
        userBriefVO1.setPhone("phone");
        userBriefVO1.setEmail("email");
        userBriefVO1.setRoleList(Arrays.asList("value"));
        projectVO.setOwnerList(Arrays.asList(userBriefVO1));
        when(mockProjectService.getProjectDetailByProjectId(0)).thenReturn(projectVO);
        
        when(mockRoleTool.isAdmin("targetResponsible")).thenReturn(false);
        
        // Configure LogicClusterDAO.getByName(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getByName("name")).thenReturn(clusterLogicPO);
        
        when(mockLogicClusterDAO.getById(0L)).thenReturn(null);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.validateClusterLogicParams(param,
                OperationEnum.ADD);
        
        // Verify the results
    }
    
    @Test
    void testEditClusterLogic() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        when(mockProjectService.checkProjectExist(0)).thenReturn(false);
        
        // Configure ProjectService.getProjectDetailByProjectId(...).
        final ProjectVO projectVO = new ProjectVO();
        projectVO.setId(0);
        projectVO.setProjectCode("projectCode");
        projectVO.setProjectName("projectName");
        final UserBriefVO userBriefVO = new UserBriefVO();
        userBriefVO.setId(0);
        userBriefVO.setUserName("userName");
        userBriefVO.setRealName("realName");
        userBriefVO.setDeptId(0);
        userBriefVO.setPhone("phone");
        userBriefVO.setEmail("email");
        userBriefVO.setRoleList(Arrays.asList("value"));
        projectVO.setUserList(Arrays.asList(userBriefVO));
        final UserBriefVO userBriefVO1 = new UserBriefVO();
        userBriefVO1.setId(0);
        userBriefVO1.setUserName("userName");
        userBriefVO1.setRealName("realName");
        userBriefVO1.setDeptId(0);
        userBriefVO1.setPhone("phone");
        userBriefVO1.setEmail("email");
        userBriefVO1.setRoleList(Arrays.asList("value"));
        projectVO.setOwnerList(Arrays.asList(userBriefVO1));
        when(mockProjectService.getProjectDetailByProjectId(0)).thenReturn(projectVO);
        
        when(mockRoleTool.isAdmin("targetResponsible")).thenReturn(false);
        
        // Configure LogicClusterDAO.getByName(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getByName("name")).thenReturn(clusterLogicPO);
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO1 = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO1);
        
        when(mockLogicClusterDAO.update(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0))).thenReturn(0);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.editClusterLogic(param, "operator");
        
        // Verify the results
    }
    
    @Test
    void testEditClusterLogic_ProjectServiceGetProjectDetailByProjectIdThrowsLogiSecurityException() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        when(mockProjectService.checkProjectExist(0)).thenReturn(false);
        when(mockProjectService.getProjectDetailByProjectId(0)).thenThrow(LogiSecurityException.class);
        
        // Run the test
        assertThatThrownBy(() -> clusterLogicServiceImplUnderTest.editClusterLogic(param, "operator"))
                .isInstanceOf(LogiSecurityException.class);
    }
    
    @Test
    void testEditClusterLogic_LogicClusterDAOGetByIdReturnsNull() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        when(mockProjectService.checkProjectExist(0)).thenReturn(false);
        
        // Configure ProjectService.getProjectDetailByProjectId(...).
        final ProjectVO projectVO = new ProjectVO();
        projectVO.setId(0);
        projectVO.setProjectCode("projectCode");
        projectVO.setProjectName("projectName");
        final UserBriefVO userBriefVO = new UserBriefVO();
        userBriefVO.setId(0);
        userBriefVO.setUserName("userName");
        userBriefVO.setRealName("realName");
        userBriefVO.setDeptId(0);
        userBriefVO.setPhone("phone");
        userBriefVO.setEmail("email");
        userBriefVO.setRoleList(Arrays.asList("value"));
        projectVO.setUserList(Arrays.asList(userBriefVO));
        final UserBriefVO userBriefVO1 = new UserBriefVO();
        userBriefVO1.setId(0);
        userBriefVO1.setUserName("userName");
        userBriefVO1.setRealName("realName");
        userBriefVO1.setDeptId(0);
        userBriefVO1.setPhone("phone");
        userBriefVO1.setEmail("email");
        userBriefVO1.setRoleList(Arrays.asList("value"));
        projectVO.setOwnerList(Arrays.asList(userBriefVO1));
        when(mockProjectService.getProjectDetailByProjectId(0)).thenReturn(projectVO);
        
        when(mockRoleTool.isAdmin("targetResponsible")).thenReturn(false);
        
        // Configure LogicClusterDAO.getByName(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getByName("name")).thenReturn(clusterLogicPO);
        
        when(mockLogicClusterDAO.getById(0L)).thenReturn(null);
        when(mockLogicClusterDAO.update(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0))).thenReturn(0);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.editClusterLogic(param, "operator");
        
        // Verify the results
    }
    
    @Test
    void testEditClusterLogicNotCheck() {
        // Setup
        final ESLogicClusterDTO param = new ESLogicClusterDTO(0L, "name", 0, 0, "code", 0, "targetResponsible", "memo",
                0, 0.0, "configJson", 0);
        when(mockLogicClusterDAO.update(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0))).thenReturn(0);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.editClusterLogicNotCheck(param, "operator");
        
        // Verify the results
    }
    
    @Test
    void testGetClusterLogicById() {
        // Setup
        final ClusterLogic expectedResult = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        // Run the test
        final ClusterLogic result = clusterLogicServiceImplUnderTest.getClusterLogicById(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicByName() {
        // Setup
        final ClusterLogic expectedResult = new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "libraDepartmentId", "libraDepartment", "memo", 0.0, 0, "configJson", 0);
        
        // Configure LogicClusterDAO.getByName(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getByName("logicClusterName")).thenReturn(clusterLogicPO);
        
        // Run the test
        final ClusterLogic result = clusterLogicServiceImplUnderTest.getClusterLogicByName("logicClusterName");
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicConfigById() {
        // Setup
        final LogicResourceConfig expectedResult = new LogicResourceConfig("quotaCtl", 0, false, 0, 0, false);
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        // Run the test
        final LogicResourceConfig result = clusterLogicServiceImplUnderTest.getClusterLogicConfigById(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetOwnedClusterLogicListByProjectId() {
        // Setup
        final List<ClusterLogic> expectedResult = Arrays.asList(
                new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "libraDepartmentId",
                        "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        
        // Configure LogicClusterDAO.listByProjectId(...).
        final List<ClusterLogicPO> clusterLogicPOS = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByProjectId(0)).thenReturn(clusterLogicPOS);
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.getOwnedClusterLogicListByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetOwnedClusterLogicListByProjectId_LogicClusterDAOReturnsNoItems() {
        // Setup
        when(mockLogicClusterDAO.listByProjectId(0)).thenReturn(Collections.emptyList());
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.getOwnedClusterLogicListByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
    
    @Test
    void testGetHasAuthClusterLogicIdsByProjectId() {
        // Setup
        // Configure ProjectClusterLogicAuthService.getAllLogicClusterAuths(...).
        final List<ProjectClusterLogicAuth> projectClusterLogicAuths = Arrays.asList(
                new ProjectClusterLogicAuth(0L, 0, 0L, 0, "responsible"));
        when(mockLogicClusterAuthService.getAllLogicClusterAuths(0)).thenReturn(projectClusterLogicAuths);
        
        // Run the test
        final List<Long> result = clusterLogicServiceImplUnderTest.getHasAuthClusterLogicIdsByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(Arrays.asList(0L));
    }
    
    @Test
    void testGetHasAuthClusterLogicIdsByProjectId_ProjectClusterLogicAuthServiceReturnsNoItems() {
        // Setup
        when(mockLogicClusterAuthService.getAllLogicClusterAuths(0)).thenReturn(Collections.emptyList());
        
        // Run the test
        final List<Long> result = clusterLogicServiceImplUnderTest.getHasAuthClusterLogicIdsByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
    
    @Test
    void testGetHasAuthClusterLogicsByProjectId() {
        // Setup
        final List<ClusterLogic> expectedResult = Arrays.asList(
                new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "libraDepartmentId",
                        "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        
        // Configure ProjectClusterLogicAuthService.getAllLogicClusterAuths(...).
        final List<ProjectClusterLogicAuth> projectClusterLogicAuths = Arrays.asList(
                new ProjectClusterLogicAuth(0L, 0, 0L, 0, "responsible"));
        when(mockLogicClusterAuthService.getAllLogicClusterAuths(0)).thenReturn(projectClusterLogicAuths);
        
        // Configure LogicClusterDAO.listByIds(...).
        final List<ClusterLogicPO> clusterLogicPOS = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByIds(new HashSet<>(Arrays.asList(0L)))).thenReturn(clusterLogicPOS);
        
        // Configure LogicClusterDAO.listAll(...).
        final List<ClusterLogicPO> clusterLogicPOS1 = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listAll()).thenReturn(clusterLogicPOS1);
        
        // Configure LogicClusterDAO.listByProjectId(...).
        final List<ClusterLogicPO> clusterLogicPOS2 = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByProjectId(0)).thenReturn(clusterLogicPOS2);
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.getHasAuthClusterLogicsByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetHasAuthClusterLogicsByProjectId_ProjectClusterLogicAuthServiceReturnsNoItems() {
        // Setup
        final List<ClusterLogic> expectedResult = Arrays.asList(
                new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "libraDepartmentId",
                        "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        when(mockLogicClusterAuthService.getAllLogicClusterAuths(0)).thenReturn(Collections.emptyList());
        
        // Configure LogicClusterDAO.listByIds(...).
        final List<ClusterLogicPO> clusterLogicPOS = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByIds(new HashSet<>(Arrays.asList(0L)))).thenReturn(clusterLogicPOS);
        
        // Configure LogicClusterDAO.listAll(...).
        final List<ClusterLogicPO> clusterLogicPOS1 = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listAll()).thenReturn(clusterLogicPOS1);
        
        // Configure LogicClusterDAO.listByProjectId(...).
        final List<ClusterLogicPO> clusterLogicPOS2 = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByProjectId(0)).thenReturn(clusterLogicPOS2);
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.getHasAuthClusterLogicsByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetHasAuthClusterLogicsByProjectId_LogicClusterDAOListByIdsReturnsNoItems() {
        // Setup
        final List<ClusterLogic> expectedResult = Arrays.asList(
                new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "libraDepartmentId",
                        "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        
        // Configure ProjectClusterLogicAuthService.getAllLogicClusterAuths(...).
        final List<ProjectClusterLogicAuth> projectClusterLogicAuths = Arrays.asList(
                new ProjectClusterLogicAuth(0L, 0, 0L, 0, "responsible"));
        when(mockLogicClusterAuthService.getAllLogicClusterAuths(0)).thenReturn(projectClusterLogicAuths);
        
        when(mockLogicClusterDAO.listByIds(new HashSet<>(Arrays.asList(0L)))).thenReturn(Collections.emptyList());
        
        // Configure LogicClusterDAO.listAll(...).
        final List<ClusterLogicPO> clusterLogicPOS = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listAll()).thenReturn(clusterLogicPOS);
        
        // Configure LogicClusterDAO.listByProjectId(...).
        final List<ClusterLogicPO> clusterLogicPOS1 = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByProjectId(0)).thenReturn(clusterLogicPOS1);
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.getHasAuthClusterLogicsByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetHasAuthClusterLogicsByProjectId_LogicClusterDAOListAllReturnsNoItems() {
        // Setup
        final List<ClusterLogic> expectedResult = Arrays.asList(
                new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "libraDepartmentId",
                        "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        
        // Configure ProjectClusterLogicAuthService.getAllLogicClusterAuths(...).
        final List<ProjectClusterLogicAuth> projectClusterLogicAuths = Arrays.asList(
                new ProjectClusterLogicAuth(0L, 0, 0L, 0, "responsible"));
        when(mockLogicClusterAuthService.getAllLogicClusterAuths(0)).thenReturn(projectClusterLogicAuths);
        
        // Configure LogicClusterDAO.listByIds(...).
        final List<ClusterLogicPO> clusterLogicPOS = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByIds(new HashSet<>(Arrays.asList(0L)))).thenReturn(clusterLogicPOS);
        
        when(mockLogicClusterDAO.listAll()).thenReturn(Collections.emptyList());
        
        // Configure LogicClusterDAO.listByProjectId(...).
        final List<ClusterLogicPO> clusterLogicPOS1 = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByProjectId(0)).thenReturn(clusterLogicPOS1);
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.getHasAuthClusterLogicsByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetHasAuthClusterLogicsByProjectId_LogicClusterDAOListByProjectIdReturnsNoItems() {
        // Setup
        final List<ClusterLogic> expectedResult = Arrays.asList(
                new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "libraDepartmentId",
                        "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        
        // Configure ProjectClusterLogicAuthService.getAllLogicClusterAuths(...).
        final List<ProjectClusterLogicAuth> projectClusterLogicAuths = Arrays.asList(
                new ProjectClusterLogicAuth(0L, 0, 0L, 0, "responsible"));
        when(mockLogicClusterAuthService.getAllLogicClusterAuths(0)).thenReturn(projectClusterLogicAuths);
        
        // Configure LogicClusterDAO.listByIds(...).
        final List<ClusterLogicPO> clusterLogicPOS = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByIds(new HashSet<>(Arrays.asList(0L)))).thenReturn(clusterLogicPOS);
        
        // Configure LogicClusterDAO.listAll(...).
        final List<ClusterLogicPO> clusterLogicPOS1 = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listAll()).thenReturn(clusterLogicPOS1);
        
        when(mockLogicClusterDAO.listByProjectId(0)).thenReturn(Collections.emptyList());
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.getHasAuthClusterLogicsByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testIsClusterLogicExists() {
        // Setup
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        // Run the test
        final Boolean result = clusterLogicServiceImplUnderTest.isClusterLogicExists(0L);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testIsClusterLogicExists_LogicClusterDAOReturnsNull() {
        // Setup
        when(mockLogicClusterDAO.getById(0L)).thenReturn(null);
        
        // Run the test
        final Boolean result = clusterLogicServiceImplUnderTest.isClusterLogicExists(0L);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testGenClusterLogicConfig() {
        // Setup
        final LogicResourceConfig expectedResult = new LogicResourceConfig("quotaCtl", 0, false, 0, 0, false);
        
        // Run the test
        final LogicResourceConfig result = clusterLogicServiceImplUnderTest.genClusterLogicConfig("configJson");
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetLogicDataNodeSepc() {
        // Setup
        final Set<RoleClusterNodeSepc> expectedResult = new HashSet<>(
                Arrays.asList(new RoleClusterNodeSepc("desc", "dataNodeSpec")));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> listResult = Result.buildFail(
                Arrays.asList(new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")));
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(listResult);
        
        // Configure ESMachineNormsService.listMachineNorms(...).
        final List<ESMachineNormsPO> esMachineNormsPOS = Arrays.asList(new ESMachineNormsPO(0L, "role", "spec", false));
        when(mockEsMachineNormsService.listMachineNorms()).thenReturn(esMachineNormsPOS);
        
        // Run the test
        final Set<RoleClusterNodeSepc> result = clusterLogicServiceImplUnderTest.getLogicDataNodeSepc(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetLogicDataNodeSepc_ClusterRegionServiceListPhysicClusterNamesReturnsNoItems() {
        // Setup
        final Set<RoleClusterNodeSepc> expectedResult = new HashSet<>(
                Arrays.asList(new RoleClusterNodeSepc("desc", "dataNodeSpec")));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Collections.emptyList());
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> listResult = Result.buildFail(
                Arrays.asList(new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")));
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(listResult);
        
        // Configure ESMachineNormsService.listMachineNorms(...).
        final List<ESMachineNormsPO> esMachineNormsPOS = Arrays.asList(new ESMachineNormsPO(0L, "role", "spec", false));
        when(mockEsMachineNormsService.listMachineNorms()).thenReturn(esMachineNormsPOS);
        
        // Run the test
        final Set<RoleClusterNodeSepc> result = clusterLogicServiceImplUnderTest.getLogicDataNodeSepc(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetLogicDataNodeSepc_ClusterPhyServiceReturnsNull() {
        // Setup
        final Set<RoleClusterNodeSepc> expectedResult = new HashSet<>(
                Arrays.asList(new RoleClusterNodeSepc("desc", "dataNodeSpec")));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(null);
        
        // Configure ESMachineNormsService.listMachineNorms(...).
        final List<ESMachineNormsPO> esMachineNormsPOS = Arrays.asList(new ESMachineNormsPO(0L, "role", "spec", false));
        when(mockEsMachineNormsService.listMachineNorms()).thenReturn(esMachineNormsPOS);
        
        // Run the test
        final Set<RoleClusterNodeSepc> result = clusterLogicServiceImplUnderTest.getLogicDataNodeSepc(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetLogicDataNodeSepc_ClusterRegionServiceGetRegionByLogicClusterIdReturnsNull() {
        // Setup
        final Set<RoleClusterNodeSepc> expectedResult = new HashSet<>(
                Arrays.asList(new RoleClusterNodeSepc("desc", "dataNodeSpec")));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);
        
        // Configure ESMachineNormsService.listMachineNorms(...).
        final List<ESMachineNormsPO> esMachineNormsPOS = Arrays.asList(new ESMachineNormsPO(0L, "role", "spec", false));
        when(mockEsMachineNormsService.listMachineNorms()).thenReturn(esMachineNormsPOS);
        
        // Run the test
        final Set<RoleClusterNodeSepc> result = clusterLogicServiceImplUnderTest.getLogicDataNodeSepc(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetLogicDataNodeSepc_ClusterRoleHostServiceReturnsNoItem() {
        // Setup
        final Set<RoleClusterNodeSepc> expectedResult = new HashSet<>(
                Arrays.asList(new RoleClusterNodeSepc("desc", "dataNodeSpec")));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(Result.buildSucc());
        
        // Configure ESMachineNormsService.listMachineNorms(...).
        final List<ESMachineNormsPO> esMachineNormsPOS = Arrays.asList(new ESMachineNormsPO(0L, "role", "spec", false));
        when(mockEsMachineNormsService.listMachineNorms()).thenReturn(esMachineNormsPOS);
        
        // Run the test
        final Set<RoleClusterNodeSepc> result = clusterLogicServiceImplUnderTest.getLogicDataNodeSepc(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetLogicDataNodeSepc_ClusterRoleHostServiceReturnsNoItems() {
        // Setup
        final Set<RoleClusterNodeSepc> expectedResult = new HashSet<>(
                Arrays.asList(new RoleClusterNodeSepc("desc", "dataNodeSpec")));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> listResult = Result.buildFail(Collections.emptyList());
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(listResult);
        
        // Configure ESMachineNormsService.listMachineNorms(...).
        final List<ESMachineNormsPO> esMachineNormsPOS = Arrays.asList(new ESMachineNormsPO(0L, "role", "spec", false));
        when(mockEsMachineNormsService.listMachineNorms()).thenReturn(esMachineNormsPOS);
        
        // Run the test
        final Set<RoleClusterNodeSepc> result = clusterLogicServiceImplUnderTest.getLogicDataNodeSepc(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetLogicDataNodeSepc_ClusterRoleHostServiceReturnsFailure() {
        // Setup
        final Set<RoleClusterNodeSepc> expectedResult = new HashSet<>(
                Arrays.asList(new RoleClusterNodeSepc("desc", "dataNodeSpec")));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> listResult = Result.buildFail();
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(listResult);
        
        // Configure ESMachineNormsService.listMachineNorms(...).
        final List<ESMachineNormsPO> esMachineNormsPOS = Arrays.asList(new ESMachineNormsPO(0L, "role", "spec", false));
        when(mockEsMachineNormsService.listMachineNorms()).thenReturn(esMachineNormsPOS);
        
        // Run the test
        final Set<RoleClusterNodeSepc> result = clusterLogicServiceImplUnderTest.getLogicDataNodeSepc(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetLogicDataNodeSepc_ESMachineNormsServiceReturnsNoItems() {
        // Setup
        final Set<RoleClusterNodeSepc> expectedResult = new HashSet<>(
                Arrays.asList(new RoleClusterNodeSepc("desc", "dataNodeSpec")));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> listResult = Result.buildFail(
                Arrays.asList(new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")));
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(listResult);
        
        when(mockEsMachineNormsService.listMachineNorms()).thenReturn(Collections.emptyList());
        
        // Run the test
        final Set<RoleClusterNodeSepc> result = clusterLogicServiceImplUnderTest.getLogicDataNodeSepc(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicRole() {
        // Setup
        final List<ClusterRoleInfo> expectedResult = Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes"))));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> listResult = Result.buildFail(
                Arrays.asList(new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")));
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(listResult);
        
        // Run the test
        final List<ClusterRoleInfo> result = clusterLogicServiceImplUnderTest.getClusterLogicRole(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicRole_ClusterRegionServiceListPhysicClusterNamesReturnsNoItems() {
        // Setup
        final List<ClusterRoleInfo> expectedResult = Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes"))));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Collections.emptyList());
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> listResult = Result.buildFail(
                Arrays.asList(new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                        "machineSpec", 0, "attributes")));
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(listResult);
        
        // Run the test
        final List<ClusterRoleInfo> result = clusterLogicServiceImplUnderTest.getClusterLogicRole(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicRole_ClusterPhyServiceReturnsNull() {
        // Setup
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(null);
        
        // Run the test
        final List<ClusterRoleInfo> result = clusterLogicServiceImplUnderTest.getClusterLogicRole(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
    
    @Test
    void testGetClusterLogicRole_ClusterRegionServiceGetRegionByLogicClusterIdReturnsNull() {
        // Setup
        final List<ClusterRoleInfo> expectedResult = Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes"))));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(null);
        
        // Run the test
        final List<ClusterRoleInfo> result = clusterLogicServiceImplUnderTest.getClusterLogicRole(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicRole_ClusterRoleHostServiceReturnsNoItem() {
        // Setup
        final List<ClusterRoleInfo> expectedResult = Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes"))));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(Result.buildSucc());
        
        // Run the test
        final List<ClusterRoleInfo> result = clusterLogicServiceImplUnderTest.getClusterLogicRole(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicRole_ClusterRoleHostServiceReturnsNoItems() {
        // Setup
        final List<ClusterRoleInfo> expectedResult = Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes"))));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> listResult = Result.buildFail(Collections.emptyList());
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(listResult);
        
        // Run the test
        final List<ClusterRoleInfo> result = clusterLogicServiceImplUnderTest.getClusterLogicRole(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicRole_ClusterRoleHostServiceReturnsFailure() {
        // Setup
        final List<ClusterRoleInfo> expectedResult = Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes"))));
        
        // Configure LogicClusterDAO.getById(...).
        final ClusterLogicPO clusterLogicPO = new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0,
                "responsible", "memo", 0.0, 0, "configJson", 0);
        when(mockLogicClusterDAO.getById(0L)).thenReturn(clusterLogicPO);
        
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ClusterRegionService.getRegionByLogicClusterId(...).
        final ClusterRegion clusterRegion = new ClusterRegion(0L, "name", "logicClusterIds", "phyClusterName");
        when(mockClusterRegionService.getRegionByLogicClusterId(0L)).thenReturn(clusterRegion);
        
        // Configure ClusterRoleHostService.listByRegionId(...).
        final Result<List<ClusterRoleHost>> listResult = Result.buildFail();
        when(mockClusterRoleHostService.listByRegionId(0)).thenReturn(listResult);
        
        // Run the test
        final List<ClusterRoleInfo> result = clusterLogicServiceImplUnderTest.getClusterLogicRole(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicPlugins() {
        // Setup
        final List<Plugin> expectedResult = Arrays.asList(
                new Plugin(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", "fileName", null,
                        0, false));
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ESPluginService.listClusterAndDefaultESPlugin(...).
        final List<PluginPO> pluginPOS = Arrays.asList(
                new PluginPO(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", 0, false));
        when(mockEsPluginService.listClusterAndDefaultESPlugin("id")).thenReturn(pluginPOS);
        
        // Configure ClusterPhyService.listAllClusters(...).
        final List<ClusterPhy> clusterPhies = Arrays.asList(
                new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress", "httpWriteAddress",
                        0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName", "nsTree", 0,
                        "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                        new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0,
                                "plugIds", false, Arrays.asList(
                                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack",
                                        "nodeSet", "machineSpec", 0, "attributes")))), Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType",
                        0, "gatewayUrl"));
        when(mockClusterPhyService.listAllClusters()).thenReturn(clusterPhies);
        
        // Run the test
        final List<Plugin> result = clusterLogicServiceImplUnderTest.getClusterLogicPlugins(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicPlugins_ClusterRegionServiceReturnsNoItems() {
        // Setup
        final List<Plugin> expectedResult = Arrays.asList(
                new Plugin(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", "fileName", null,
                        0, false));
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Collections.emptyList());
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ESPluginService.listClusterAndDefaultESPlugin(...).
        final List<PluginPO> pluginPOS = Arrays.asList(
                new PluginPO(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", 0, false));
        when(mockEsPluginService.listClusterAndDefaultESPlugin("id")).thenReturn(pluginPOS);
        
        // Configure ClusterPhyService.listAllClusters(...).
        final List<ClusterPhy> clusterPhies = Arrays.asList(
                new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress", "httpWriteAddress",
                        0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName", "nsTree", 0,
                        "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                        new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0,
                                "plugIds", false, Arrays.asList(
                                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack",
                                        "nodeSet", "machineSpec", 0, "attributes")))), Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType",
                        0, "gatewayUrl"));
        when(mockClusterPhyService.listAllClusters()).thenReturn(clusterPhies);
        
        // Run the test
        final List<Plugin> result = clusterLogicServiceImplUnderTest.getClusterLogicPlugins(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicPlugins_ESPluginServiceReturnsNoItems() {
        // Setup
        final List<Plugin> expectedResult = Arrays.asList(
                new Plugin(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", "fileName", null,
                        0, false));
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        when(mockEsPluginService.listClusterAndDefaultESPlugin("id")).thenReturn(Collections.emptyList());
        
        // Configure ClusterPhyService.listAllClusters(...).
        final List<ClusterPhy> clusterPhies = Arrays.asList(
                new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress", "httpWriteAddress",
                        0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName", "nsTree", 0,
                        "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                        new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0,
                                "plugIds", false, Arrays.asList(
                                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack",
                                        "nodeSet", "machineSpec", 0, "attributes")))), Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType",
                        0, "gatewayUrl"));
        when(mockClusterPhyService.listAllClusters()).thenReturn(clusterPhies);
        
        // Run the test
        final List<Plugin> result = clusterLogicServiceImplUnderTest.getClusterLogicPlugins(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicPlugins_ClusterPhyServiceListAllClustersReturnsNoItems() {
        // Setup
        final List<Plugin> expectedResult = Arrays.asList(
                new Plugin(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", "fileName", null,
                        0, false));
        when(mockClusterRegionService.listPhysicClusterNames(0L)).thenReturn(Arrays.asList("value"));
        
        // Configure ClusterPhyService.getClusterByName(...).
        final ClusterPhy clusterPhy = new ClusterPhy(0, "cluster", "desc", "readAddress", "writeAddress", "httpAddress",
                "httpWriteAddress", 0, "tags", "dataCenter", "idc", 0, "esVersion", 0L, "plugIds", 0L, "imageName",
                "nsTree", 0, "machineSpec", "templateSrvs", "password", "creator", Arrays.asList(
                new ClusterRoleInfo(0L, 0L, "roleClusterName", "role", 0, 0, "dataNodeSpec", "esVersion", 0, "plugIds",
                        false, Arrays.asList(
                        new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet",
                                "machineSpec", 0, "attributes")))), Arrays.asList(
                new ClusterRoleHost(0L, 0L, "hostname", "ip", "cluster", "port", 0, 0, "rack", "nodeSet", "machineSpec",
                        0, "attributes")), 0, "writeAction", 0, 0L, 0L, 0L, 0.0, "platformType", 0, "gatewayUrl");
        when(mockClusterPhyService.getClusterByName("clusterName")).thenReturn(clusterPhy);
        
        // Configure ESPluginService.listClusterAndDefaultESPlugin(...).
        final List<PluginPO> pluginPOS = Arrays.asList(
                new PluginPO(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", 0, false));
        when(mockEsPluginService.listClusterAndDefaultESPlugin("id")).thenReturn(pluginPOS);
        
        when(mockClusterPhyService.listAllClusters()).thenReturn(Collections.emptyList());
        
        // Run the test
        final List<Plugin> result = clusterLogicServiceImplUnderTest.getClusterLogicPlugins(0L);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testAddPlugin() {
        // Setup
        final PluginDTO pluginDTO = new PluginDTO(0L, "name", "physicClusterId", "version", "url", "md5", "desc",
                "creator", 0, "fileName", null);
        final Result<Long> expectedResult = Result.buildFail(0L);
        when(mockClusterRegionService.listPhysicClusterId(0L)).thenReturn(Arrays.asList(0));
        when(mockEsPluginService.addESPlugin(
                new PluginDTO(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", 0, "fileName",
                        null))).thenReturn(Result.buildFail(0L));
        
        // Run the test
        final Result<Long> result = clusterLogicServiceImplUnderTest.addPlugin(0L, pluginDTO, "operator");
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testAddPlugin_ClusterRegionServiceReturnsNoItems() {
        // Setup
        final PluginDTO pluginDTO = new PluginDTO(0L, "name", "physicClusterId", "version", "url", "md5", "desc",
                "creator", 0, "fileName", null);
        final Result<Long> expectedResult = Result.buildFail(0L);
        when(mockClusterRegionService.listPhysicClusterId(0L)).thenReturn(Collections.emptyList());
        when(mockEsPluginService.addESPlugin(
                new PluginDTO(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", 0, "fileName",
                        null))).thenReturn(Result.buildFail(0L));
        
        // Run the test
        final Result<Long> result = clusterLogicServiceImplUnderTest.addPlugin(0L, pluginDTO, "operator");
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testAddPlugin_ESPluginServiceReturnsNoItem() {
        // Setup
        final PluginDTO pluginDTO = new PluginDTO(0L, "name", "physicClusterId", "version", "url", "md5", "desc",
                "creator", 0, "fileName", null);
        when(mockClusterRegionService.listPhysicClusterId(0L)).thenReturn(Arrays.asList(0));
        when(mockEsPluginService.addESPlugin(
                new PluginDTO(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", 0, "fileName",
                        null))).thenReturn(Result.buildSucc());
        
        // Run the test
        final Result<Long> result = clusterLogicServiceImplUnderTest.addPlugin(0L, pluginDTO, "operator");
        
        // Verify the results
        assertThat(result).isEqualTo(Result.buildSucc());
    }
    
    @Test
    void testAddPlugin_ESPluginServiceReturnsFailure() {
        // Setup
        final PluginDTO pluginDTO = new PluginDTO(0L, "name", "physicClusterId", "version", "url", "md5", "desc",
                "creator", 0, "fileName", null);
        final Result<Long> expectedResult = Result.buildFail(0L);
        when(mockClusterRegionService.listPhysicClusterId(0L)).thenReturn(Arrays.asList(0));
        when(mockEsPluginService.addESPlugin(
                new PluginDTO(0L, "name", "physicClusterId", "version", "url", "md5", "desc", "creator", 0, "fileName",
                        null))).thenReturn(Result.buildFail());
        
        // Run the test
        final Result<Long> result = clusterLogicServiceImplUnderTest.addPlugin(0L, pluginDTO, "operator");
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testTransferClusterLogic() {
        // Setup
        when(mockLogicClusterDAO.update(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0))).thenReturn(0);
        
        // Run the test
        final Result<Void> result = clusterLogicServiceImplUnderTest.transferClusterLogic(0L, 0, "targetResponsible",
                "submitor");
        
        // Verify the results
    }
    
    @Test
    void testPagingGetClusterLogicByCondition() {
        // Setup
        final ClusterLogicConditionDTO param = new ClusterLogicConditionDTO(0, "sortTerm", false);
        final List<ClusterLogic> expectedResult = Arrays.asList(
                new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "libraDepartmentId",
                        "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        
        // Configure LogicClusterDAO.pagingByCondition(...).
        final List<ClusterLogicPO> clusterLogicPOS = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.pagingByCondition("name", 0, 0, 0, 0L, 0L, "sortTerm", "sortType"))
                .thenReturn(clusterLogicPOS);
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.pagingGetClusterLogicByCondition(param);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testPagingGetClusterLogicByCondition_LogicClusterDAOReturnsNoItems() {
        // Setup
        final ClusterLogicConditionDTO param = new ClusterLogicConditionDTO(0, "sortTerm", false);
        when(mockLogicClusterDAO.pagingByCondition("name", 0, 0, 0, 0L, 0L, "sortTerm", "sortType"))
                .thenReturn(Collections.emptyList());
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.pagingGetClusterLogicByCondition(param);
        
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
    
    @Test
    void testFuzzyClusterLogicHitByCondition() {
        // Setup
        final ClusterLogicConditionDTO param = new ClusterLogicConditionDTO(0, "sortTerm", false);
        when(mockLogicClusterDAO.getTotalHitByCondition(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0))).thenReturn(0L);
        
        // Run the test
        final Long result = clusterLogicServiceImplUnderTest.fuzzyClusterLogicHitByCondition(param);
        
        // Verify the results
        assertThat(result).isEqualTo(0L);
    }
    
    @Test
    void testGetClusterLogicListByIds() {
        // Setup
        final List<ClusterLogic> expectedResult = Arrays.asList(
                new ClusterLogic(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "libraDepartmentId",
                        "libraDepartment", "memo", 0.0, 0, "configJson", 0));
        
        // Configure LogicClusterDAO.listByIds(...).
        final List<ClusterLogicPO> clusterLogicPOS = Arrays.asList(
                new ClusterLogicPO(0L, "name", 0, 0, "dataCenter", "dataNodeSpec", 0, "responsible", "memo", 0.0, 0,
                        "configJson", 0));
        when(mockLogicClusterDAO.listByIds(new HashSet<>(Arrays.asList(0L)))).thenReturn(clusterLogicPOS);
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.getClusterLogicListByIds(Arrays.asList(0L));
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetClusterLogicListByIds_LogicClusterDAOReturnsNoItems() {
        // Setup
        when(mockLogicClusterDAO.listByIds(new HashSet<>(Arrays.asList(0L)))).thenReturn(Collections.emptyList());
        
        // Run the test
        final List<ClusterLogic> result = clusterLogicServiceImplUnderTest.getClusterLogicListByIds(Arrays.asList(0L));
        
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
}