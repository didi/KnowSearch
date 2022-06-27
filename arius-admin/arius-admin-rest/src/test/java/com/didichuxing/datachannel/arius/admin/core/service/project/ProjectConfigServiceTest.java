package com.didichuxing.datachannel.arius.admin.core.service.project;

import static com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil.obj2Obj;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.project.impl.ProjectConfigServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.project.ProjectConfigDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional(timeout = 1000)
@Rollback
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class ProjectConfigServiceTest {
    
    @Mock
    private ProjectConfigDAO projectConfigDAO;
    
    @InjectMocks
    private ProjectConfigServiceImpl projectConfigService;
    
    @BeforeEach
    void setUp() {
        initMocks(this);
    }
    
    @Test
    void testGetProjectConfig() {
        // Setup
        final ProjectConfig expectedResult = new ProjectConfig(0, 0, 0, 0, 0, 0, "memo");
        
        // Configure ProjectConfigDAO.getByProjectId(...).
        final ProjectConfigPO projectConfigPO = new ProjectConfigPO(0, 0, 0, 0, 0, 0, "memo");
        when(projectConfigDAO.getByProjectId(0)).thenReturn(projectConfigPO);
        
        // Run the test
        final ProjectConfig result = projectConfigService.getProjectConfig(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testProjectId2ProjectConfigMap() {
        // Setup
        
        // Configure ProjectConfigDAO.listAll(...).
        final List<ProjectConfigPO> projectConfigPOS = Arrays.asList(new ProjectConfigPO(1, 0, 0, 0, 0, 0, "memo"));
        when(projectConfigDAO.listAll()).thenReturn(projectConfigPOS);
        final Map<Integer, ProjectConfig> expectedResult = Maps.newHashMap();
        for (Entry<Integer, ProjectConfigPO> configPOEntry : ConvertUtil.list2Map(projectConfigPOS,
                ProjectConfigPO::getProjectId).entrySet()) {
            expectedResult.put(configPOEntry.getKey(),
                    ConvertUtil.obj2Obj(configPOEntry.getValue(), ProjectConfig.class));
        }
        
        // Run the test
        final Map<Integer, ProjectConfig> result = projectConfigService.projectId2ProjectConfigMap();
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testUpdateOrInitProjectConfig() {
        // Setup
        final ProjectConfigPO projectConfigPO = CustomDataSource.projectConfigPO();
        final ProjectConfigDTO projectConfigDTO = ConvertUtil.obj2Obj(projectConfigPO, ProjectConfigDTO.class);
        
        assertThat(projectConfigService.updateOrInitProjectConfig(null, null)._1().getMessage()).isEqualTo(
                Result.buildParamIllegal("配置信息为空").getMessage());
        projectConfigDTO.setProjectId(null);
        assertThat(
                projectConfigService.updateOrInitProjectConfig(projectConfigDTO, null)._1().getMessage()).isEqualTo(
                Result.buildParamIllegal("应用ID为空").getMessage());
        projectConfigDTO.setProjectId(1);
        projectConfigDTO.setAnalyzeResponseEnable(23);
        assertThat(
                projectConfigService.updateOrInitProjectConfig(projectConfigDTO, null)._1().getMessage()).isEqualTo(
                Result.buildParamIllegal("解析响应结果开关非法").getMessage());
        projectConfigDTO.setAnalyzeResponseEnable(1);
        projectConfigDTO.setDslAnalyzeEnable(23);
        assertThat(
                projectConfigService.updateOrInitProjectConfig(projectConfigDTO, null)._1().getMessage()).isEqualTo(
                Result.buildParamIllegal("DSL分析开关非法").getMessage());
        projectConfigDTO.setDslAnalyzeEnable(1);
        projectConfigDTO.setAggrAnalyzeEnable(23);
        assertThat(
                projectConfigService.updateOrInitProjectConfig(projectConfigDTO, null)._1().getMessage()).isEqualTo(
                Result.buildParamIllegal("聚合分析开关非法").getMessage());
        projectConfigDTO.setAggrAnalyzeEnable(1);
        projectConfigDTO.setIsSourceSeparated(23);
        assertThat(
                projectConfigService.updateOrInitProjectConfig(projectConfigDTO, null)._1().getMessage()).isEqualTo(
                Result.buildParamIllegal("索引存储分离开关非法").getMessage());
        projectConfigDTO.setIsSourceSeparated(1);
        when(projectConfigDAO.checkProjectConfigByProjectId(1)).thenReturn(true);
        when(projectConfigDAO.update(any())).thenReturn(1);
        assertThat(projectConfigService.updateOrInitProjectConfig(projectConfigDTO, null)._1().success()).isTrue();
        when(projectConfigDAO.checkProjectConfigByProjectId(1)).thenReturn(false);
        when(projectConfigDAO.insert(any())).thenReturn(1);
        assertThat(projectConfigService.updateOrInitProjectConfig(projectConfigDTO, null)._1().success()).isTrue();
        // Verify the results
    }
    
    @Test
    void testDeleteByProjectId() {
        // Setup
        when(projectConfigDAO.checkProjectConfigByProjectId(anyInt())).thenReturn(true);
        when(projectConfigDAO.deleteByProjectId(anyInt())).thenReturn(1);
        
        // Run the test
        projectConfigService.deleteByProjectId(0);
        
        // Verify the results
        verify(projectConfigDAO).deleteByProjectId(0);
    }
}