package com.didichuxing.datachannel.arius.admin.biz.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.didichuxing.datachannel.arius.admin.biz.app.impl.ProjectConfigManagerImpl;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectConfigVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes ={ SpringTool.class })
@SpringBootTest
class ProjectConfigManagerTest {
    
    @Mock
    private ProjectConfigService projectConfigService;
    @Mock
    private OperateRecordService operateRecordService;
    
    @InjectMocks
    private ProjectConfigManagerImpl projectConfigManager;
    
    @BeforeEach
    void setUp() {
        initMocks(this);
         when(operateRecordService.save(anyInt(), anyInt(), anyString(), anyString(), anyString())).thenReturn(
                Result.buildSucc());
    }
    
    @Test
    void testGet() {
        // Setup
        final Result<ProjectConfigVO> expectedResult = Result.buildSucc(new ProjectConfigVO(0, 0, 0, 0, 0, 0, "memo"));
        
        // Configure ProjectConfigService.getProjectConfig(...).
        final ProjectConfig projectConfig = new ProjectConfig(0, 0, 0, 0, 0, 0, "memo");
        when(projectConfigService.getProjectConfig(0)).thenReturn(projectConfig);
        
        // Run the test
        final Result<ProjectConfigVO> result = projectConfigManager.get(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
   
    
    

    
}