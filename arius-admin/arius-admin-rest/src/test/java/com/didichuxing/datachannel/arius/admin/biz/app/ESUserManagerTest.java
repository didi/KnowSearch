package com.didichuxing.datachannel.arius.admin.biz.app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.didichuxing.datachannel.arius.admin.biz.app.impl.ESUserManagerImpl;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class ESUserManagerTest {
    
    @Mock
    private ProjectService projectService;
    
    @Mock
    private   ESUserService         esUserService;
    @Mock
    private   RoleTool              roleTool;
    @Mock
    private   OperateRecordService  operateRecordService;
    @InjectMocks
    private   ESUserManagerImpl     esUserManager;
    
    @BeforeEach
    void setUp() {
        initMocks(this);
        when(operateRecordService.save(anyInt(), anyInt(), anyString(), anyString(), anyString())).thenReturn(
                Result.buildSucc());
    }
    
    @Test
    void testListESUsers() {
        final ProjectBriefVO projectBriefVO = CustomDataSource.projectBriefVO();
        final ESUserPO esUserPO = CustomDataSource.esUserPO();
        final ESUser esUser = ConvertUtil.obj2Obj(esUserPO, ESUser.class);
        when(projectService.getProjectBriefList()).thenReturn(Lists.newArrayList(projectBriefVO));
        when(esUserService.listESUsers(Lists.newArrayList(projectBriefVO.getId()))).thenReturn(
                Lists.newArrayList(esUser));
        final Result<List<ESUser>> listResult = esUserManager.listESUsers();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(listResult.getData()));
        
    }
    
    @Test
    void testListESUsersByProjectId() {
        final ESUserPO esUserPO = CustomDataSource.esUserPO();
        final ESUser esUser = ConvertUtil.obj2Obj(esUserPO, ESUser.class);
        final ProjectVO projectVO = CustomDataSource.projectVO();
        
        when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(null);

        when(roleTool.isAdmin(anyString())).thenReturn(false);

        Assertions.assertEquals(Result.buildParamIllegal(String.format("项目:[%s]不存在成员:[%s]", 1, "aaaa")).getMessage(),
                esUserManager.listESUsersByProjectId(1, "aaaa").getMessage());
        when(roleTool.isAdmin(anyString())).thenReturn(false);
        when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(CustomDataSource.projectVO());

        Assertions.assertEquals(Result.buildParamIllegal(String.format("项目:[%s]不存在成员:[%s]", 1, "aaaa")).getMessage(),
                esUserManager.listESUsersByProjectId(1, "aaaa").getMessage());
        when(roleTool.isAdmin(anyString())).thenReturn(true);
        when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(null);
        Assertions.assertTrue(esUserManager.listESUsersByProjectId(1, "aaaa").success());
        when(roleTool.isAdmin(anyString())).thenReturn(false);
        when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(projectVO);
        when(esUserService.listESUsers(anyList())).thenReturn(Lists.newArrayList(esUser));
         when(roleTool.isAdmin(anyString())).thenReturn(false);
        Assertions.assertTrue(
                esUserManager.listESUsersByProjectId(1, "admin").success());
        
    }
    
    @Test
    void testRegisterESUser() {
        final ESUserPO esUserPO = CustomDataSource.esUserPO();
        final ESUserDTO esUserDTO = ConvertUtil.obj2Obj(esUserPO, ESUserDTO.class);
        when(roleTool.isAdmin(anyString())).thenReturn(false);
        esUserDTO.setResponsible("aaa");
        Assertions.assertEquals(
                Result.buildParamIllegal(String.format("当前操作[%s] 不能创建es user", esUserDTO.getResponsible()))
                        .getMessage(), esUserManager.registerESUser(esUserDTO, 1, "admin").getMessage());
        when(roleTool.isAdmin(anyString())).thenReturn(true);
        when(esUserService.validateESUser(any(),any())).thenReturn(Result.buildParamIllegal("应用信息为空"));
        when(esUserService.registerESUser(null, null)).thenReturn(new Tuple<>(Result.buildParamIllegal("应用信息为空"),
                esUserPO));
        Assertions.assertEquals(Result.buildParamIllegal("应用信息为空").getMessage(),
                esUserManager.registerESUser(null, null,null).getMessage());
        when(esUserService.registerESUser(any(), anyString())).thenReturn(new Tuple<>(Result.buildSucc(1),
                esUserPO));
         Assertions.assertTrue(
                esUserManager.registerESUser(esUserDTO, 1,"a").success());
    }
    
    @Test
    void testEditESUser() {
        final ESUserPO esUserPO = CustomDataSource.esUserPO();
        final ESUserDTO esUserDTO = ConvertUtil.obj2Obj(esUserPO, ESUserDTO.class);
        when(projectService.checkProjectExist(anyInt())).thenReturn(false);
        when(esUserService.validateESUser(any(), any())).thenReturn(Result.buildFail("应用不存在"));
        Assertions.assertEquals(Result.buildFail("应用不存在").getMessage(),
                esUserManager.editESUser(esUserDTO, "admin").getMessage());
        when(esUserService.validateESUser(any(), any())).thenReturn(Result.buildSucc());
        when(esUserService.editUser(any())).thenReturn(new Tuple<>(Result.buildSucc(), esUserPO));
        Assertions.assertTrue(esUserManager.editESUser(esUserDTO, "admin").success());
        
    }
    
    @Test
    void testDeleteESUserByProject() {
    
    }
    
    @Test
    void testDeleteAllESUserByProject() {
    
    }
    
    @Test
    void testVerifyAppCode() {
        final ESUserPO esUserPO = CustomDataSource.esUserPO();
        final ProjectVO projectVO = CustomDataSource.projectVO();
        final ESUser esUser = ConvertUtil.obj2Obj(esUserPO, ESUser.class);
        when(roleTool.isAdmin(anyString())).thenReturn(false);
        when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(projectVO);
        Assertions.assertEquals(Result.buildFail("权限不足").getMessage(),
                esUserManager.getNoCodeESUser(1, "admin1").getMessage());
        Assertions.assertTrue(esUserManager.getNoCodeESUser(1, "admin").success());
        
    }
    
    @Test
    void testGet() {
        final ESUserPO esUserPO = CustomDataSource.esUserPO();
        final ESUser esUser = ConvertUtil.obj2Obj(esUserPO, ESUser.class);
        when(esUserService.getEsUserById(anyInt())).thenReturn(esUser);
        final Result<ConsoleESUserVO> consoleESUserVOResult = esUserManager.get(1);
        Assertions.assertEquals(consoleESUserVOResult.getData().getId(),esUser.getId());
        
    
    }
    
    @Test
    void testGetNoCodeESUser() {
    
    }
    
}