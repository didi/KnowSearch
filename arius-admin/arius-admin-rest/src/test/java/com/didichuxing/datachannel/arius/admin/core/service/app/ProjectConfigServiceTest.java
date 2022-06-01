package com.didichuxing.datachannel.arius.admin.core.service.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ProjectConfigDAO;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

class ProjectConfigServiceTest extends AriusAdminApplicationTest {
    
    @Mock
    private ProjectConfigDAO mockProjectConfigDAO;
    
    @Autowired
    private ProjectConfigService projectConfigService;
   
    
    @Test
    void testGetProjectConfig() {
        // Setup
        final ProjectConfig expectedResult = new ProjectConfig(0, 0, 0, 0, 0, 0, "memo");
        
        // Configure ProjectConfigDAO.getByProjectId(...).
        final ProjectConfigPO projectConfigPO = new ProjectConfigPO(0, 0, 0, 0, 0, 0, "memo");
        when(mockProjectConfigDAO.getByProjectId(0)).thenReturn(projectConfigPO);
        
        // Run the test
        final ProjectConfig result = projectConfigService.getProjectConfig(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
   
    
    @Test
    void testProjectId2ProjectConfigMap() {
        // Setup
        final Map<Integer, ProjectConfig> expectedResult = new HashMap<>();
        
        // Configure ProjectConfigDAO.listAll(...).
        final List<ProjectConfigPO> projectConfigPOS = Arrays.asList(new ProjectConfigPO(0, 0, 0, 0, 0, 0, "memo"));
        when(mockProjectConfigDAO.listAll()).thenReturn(projectConfigPOS);
        
        // Run the test
        final Map<Integer, ProjectConfig> result = projectConfigService.projectId2ProjectConfigMap();
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testProjectId2ProjectConfigMap_ProjectConfigDAOReturnsNoItems() {
        // Setup
        final Map<Integer, ProjectConfig> expectedResult = new HashMap<>();
        when(mockProjectConfigDAO.listAll()).thenReturn(Collections.emptyList());
        
        // Run the test
        final Map<Integer, ProjectConfig> result = projectConfigService.projectId2ProjectConfigMap();
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testUpdateOrInitProjectConfig() {
        // Setup
        final ProjectConfigDTO configDTO = new ProjectConfigDTO(0, 0, 0, 0, 0, 0, "memo");
        when(mockProjectConfigDAO.checkProjectConfigByProjectId(0)).thenReturn(false);
        
        // Configure ProjectConfigDAO.getByProjectId(...).
        final ProjectConfigPO projectConfigPO = new ProjectConfigPO(0, 0, 0, 0, 0, 0, "memo");
        when(mockProjectConfigDAO.getByProjectId(0)).thenReturn(projectConfigPO);
        
        when(mockProjectConfigDAO.update(new ProjectConfigPO(0, 0, 0, 0, 0, 0, "memo"))).thenReturn(0);
        when(mockProjectConfigDAO.insert(new ProjectConfigPO(0, 0, 0, 0, 0, 0, "memo"))).thenReturn(0);
        
        // Run the test
        final Tuple<Result<Void>, ProjectConfigPO> result = projectConfigService.updateOrInitProjectConfig(
                configDTO, "operator");
        
        // Verify the results
    }
    
    @Test
    void testDeleteByProjectId() {
        // Setup
        when(mockProjectConfigDAO.checkProjectConfigByProjectId(0)).thenReturn(false);
        when(mockProjectConfigDAO.deleteByProjectId(0)).thenReturn(0);
        
        // Run the test
        projectConfigService.deleteByProjectId(0);
        
        // Verify the results
        verify(mockProjectConfigDAO).deleteByProjectId(0);
    }
}