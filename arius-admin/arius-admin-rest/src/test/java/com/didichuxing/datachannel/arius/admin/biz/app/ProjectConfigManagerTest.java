package com.didichuxing.datachannel.arius.admin.biz.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.didichuxing.datachannel.arius.admin.biz.app.impl.ProjectConfigManagerImpl;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectConfigVo;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class ProjectConfigManagerTest {
    
    @Mock
    private ProjectConfigService mockProjectConfigService;
    @Mock
    private OperateRecordService mockOperateRecordService;
    
    @InjectMocks
    private ProjectConfigManagerImpl projectConfigManagerImplUnderTest;
    
    @BeforeEach
    void setUp() {
        initMocks(this);
    }
    
    @Test
    void testGet() {
        // Setup
        final Result<ProjectConfigVo> expectedResult = Result.buildFail(new ProjectConfigVo(0, 0, 0, 0, 0, 0, "memo"));
        
        // Configure ProjectConfigService.getProjectConfig(...).
        final ProjectConfig projectConfig = new ProjectConfig(0, 0, 0, 0, 0, 0, "memo");
        when(mockProjectConfigService.getProjectConfig(0)).thenReturn(projectConfig);
        
        // Run the test
        final Result<ProjectConfigVo> result = projectConfigManagerImplUnderTest.get(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testUpdateProjectConfig() {
        // Setup
        final ProjectConfigDTO configDTO = new ProjectConfigDTO(0, 0, 0, 0, 0, 0, "memo");
        
        // Configure ProjectConfigService.updateOrInitProjectConfig(...).
        final Tuple<Result<Void>, ProjectConfigPO> resultProjectConfigPOTuple = new Tuple<>(
                Result.buildFail(null), new ProjectConfigPO(0, 0, 0, 0, 0, 0, "memo"));
        when(mockProjectConfigService.updateOrInitProjectConfig(new ProjectConfigDTO(0, 0, 0, 0, 0, 0, "memo"),
                "operator")).thenReturn(resultProjectConfigPOTuple);
        
        when(mockOperateRecordService.save(ModuleEnum.TEMPLATE, OperationEnum.ADD, 0, "content",
                "operator")).thenReturn(Result.buildFail(null));
        
        // Run the test
        final Result<Void> result = projectConfigManagerImplUnderTest.updateProjectConfig(configDTO, "operator");
        
        // Verify the results
        verify(mockOperateRecordService).save(ModuleEnum.TEMPLATE, OperationEnum.ADD, 0, "content", "operator");
    }
    
    @Test
    void testUpdateProjectConfig_OperateRecordServiceReturnsFailure() {
        // Setup
        final ProjectConfigDTO configDTO = new ProjectConfigDTO(0, 0, 0, 0, 0, 0, "memo");
        
        // Configure ProjectConfigService.updateOrInitProjectConfig(...).
        final Tuple<Result<Void>, ProjectConfigPO> resultProjectConfigPOTuple = new Tuple<>(
                Result.buildFail(null), new ProjectConfigPO(0, 0, 0, 0, 0, 0, "memo"));
        when(mockProjectConfigService.updateOrInitProjectConfig(new ProjectConfigDTO(0, 0, 0, 0, 0, 0, "memo"),
                "operator")).thenReturn(resultProjectConfigPOTuple);
        
        when(mockOperateRecordService.save(ModuleEnum.TEMPLATE, OperationEnum.ADD, 0, "content",
                "operator")).thenReturn(Result.buildFail());
        
        // Run the test
        final Result<Void> result = projectConfigManagerImplUnderTest.updateProjectConfig(configDTO, "operator");
        
        // Verify the results
        verify(mockOperateRecordService).save(ModuleEnum.TEMPLATE, OperationEnum.ADD, 0, "content", "operator");
    }
    
    @Test
    void testInitProjectConfig() {
        // Setup
        final ProjectConfigDTO configDTO = new ProjectConfigDTO(0, 0, 0, 0, 0, 0, "memo");
        
        // Configure ProjectConfigService.updateOrInitProjectConfig(...).
        final Tuple<Result<Void>, ProjectConfigPO> resultProjectConfigPOTuple = new Tuple<>(
                Result.buildFail(null), new ProjectConfigPO(0, 0, 0, 0, 0, 0, "memo"));
        when(mockProjectConfigService.updateOrInitProjectConfig(new ProjectConfigDTO(0, 0, 0, 0, 0, 0, "memo"),
                "operator")).thenReturn(resultProjectConfigPOTuple);
        
        when(mockOperateRecordService.save(ModuleEnum.TEMPLATE, OperationEnum.ADD, 0, "content",
                "operator")).thenReturn(Result.buildFail(null));
        
        // Run the test
        final Result<Void> result = projectConfigManagerImplUnderTest.initProjectConfig(configDTO, "operator");
        
        // Verify the results
        verify(mockOperateRecordService).save(ModuleEnum.TEMPLATE, OperationEnum.ADD, 0, "content", "operator");
    }
    
    @Test
    void testInitProjectConfig_OperateRecordServiceReturnsFailure() {
        // Setup
        final ProjectConfigDTO configDTO = new ProjectConfigDTO(0, 0, 0, 0, 0, 0, "memo");
        
        // Configure ProjectConfigService.updateOrInitProjectConfig(...).
        final Tuple<Result<Void>, ProjectConfigPO> resultProjectConfigPOTuple = new Tuple<>(
                Result.buildFail(null), new ProjectConfigPO(0, 0, 0, 0, 0, 0, "memo"));
        when(mockProjectConfigService.updateOrInitProjectConfig(new ProjectConfigDTO(0, 0, 0, 0, 0, 0, "memo"),
                "operator")).thenReturn(resultProjectConfigPOTuple);
        
        when(mockOperateRecordService.save(ModuleEnum.TEMPLATE, OperationEnum.ADD, 0, "content",
                "operator")).thenReturn(Result.buildFail());
        
        // Run the test
        final Result<Void> result = projectConfigManagerImplUnderTest.initProjectConfig(configDTO, "operator");
        
        // Verify the results
        verify(mockOperateRecordService).save(ModuleEnum.TEMPLATE, OperationEnum.ADD, 0, "content", "operator");
    }
}