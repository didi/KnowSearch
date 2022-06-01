package com.didichuxing.datachannel.arius.admin.core.service.app;

import static org.mockito.Mockito.when;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ProjectConfigDAO;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
class ProjectConfigServiceTest extends AriusAdminApplicationTest {
    
    @Mock
    private ProjectConfigDAO mockProjectConfigDAO;
    
    @MockBean
    private ProjectConfigService projectConfigService;
    public static final int projectId=1;
    
    
   
    
  
    
 
    
    @Test
    void testUpdateOrInitProjectConfig() {
        // Setup
        final ProjectConfigDTO configDTO = new ProjectConfigDTO(projectId, 0, 0, 0, 0, 0, "memo");
   
        
        // Run the test
        final Tuple<Result<Void>, ProjectConfigPO> result = projectConfigService.updateOrInitProjectConfig(configDTO,
                "operator");
        Assertions.assertEquals(result.getV1(),Result.buildSucc());
        
        // Verify the results
    }
     @Test
    void testGetProjectConfig() {
        
        // Run the test
        final ProjectConfig result = projectConfigService.getProjectConfig(projectId);
        
        // Verify the results
        Assertions.assertNotNull(result);
    }
      @Test
    void testProjectId2ProjectConfigMap() {
        
        // Run the test
        final Map<Integer, ProjectConfig> result = projectConfigService.projectId2ProjectConfigMap();
        
        // Verify the results
        Assertions.assertTrue(MapUtils.isNotEmpty(result));
    }
    
    @Test
    void testDeleteByProjectId() {
        // Setup
        when(mockProjectConfigDAO.checkProjectConfigByProjectId(Mockito.anyInt())).thenReturn(false);
        when(mockProjectConfigDAO.deleteByProjectId(Mockito.anyInt())).thenReturn(0);
        
        // Run the test
        projectConfigService.deleteByProjectId(Mockito.anyInt());
        
        // Verify the results
        when(mockProjectConfigDAO.deleteByProjectId(Mockito.anyInt())).thenReturn(0);
    }
}