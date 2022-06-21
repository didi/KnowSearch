package com.didichuxing.datachannel.arius.admin.core.service.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuple2;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.impl.ESUserServiceImpl;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ESUserDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional(timeout = 1000)
@Rollback
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class ESUserServiceTest {
    @InjectMocks
    private ESUserServiceImpl    esUserService;
 
    
    @Mock
    private ESUserDAO esUserDAO;
    
    @BeforeEach
    void setUp() {
        initMocks(this);
    }
    
    @Test
    void testListESUsers() {
        
        when(esUserDAO.listByProjectIds(anyList())).thenReturn(Collections.singletonList(CustomDataSource.esUserPO()));
        
        // Run the test
        final List<ESUser> result = esUserService.listESUsers(Collections.singletonList(1));
        
    }
    
    @Test
    void testRegisterESUser() {
        Assertions.assertEquals(Result.buildParamIllegal("应用信息为空").getMessage(),
                esUserService.registerESUser(null, "operator")._1().getMessage());
        ESUserPO esUserPO = CustomDataSource.esUserPO();
        final ESUserDTO esUserDTO = ConvertUtil.obj2Obj(esUserPO, ESUserDTO.class);
    
        esUserDTO.setProjectId(null);
        esUserDTO.setId(1);
        Assertions.assertEquals(Result.buildParamIllegal("项目id为空").getMessage(),
                esUserService.registerESUser(esUserDTO, "operator")._1()
                        .getMessage());
       
        
        when(esUserDAO.getByESUser(anyInt())).thenReturn(esUserPO);
        when(esUserDAO.insert(any())).thenReturn(1);
        esUserDTO.setProjectId(1);
        Assertions.assertTrue(
                esUserService.registerESUser(esUserDTO, "operator")._1()
                        .success());
        
    }
    
    @Test
    void testEditUser() {
        
        when(esUserDAO.update(any())).thenReturn(1);
        ESUserPO esUserPO = CustomDataSource.esUserPO();
        ESUserDTO esUserDTO = ConvertUtil.obj2Obj(esUserPO, ESUserDTO.class);
        esUserDTO.setProjectId(1);
        esUserDTO.setId(1);
        when(esUserDAO.getByESUser(anyInt())).thenReturn(esUserPO);
        // Run the test
        final Tuple2<Result<Void>, ESUserPO> result = esUserService.editUser(esUserDTO);
        Assertions.assertTrue(result._1().success());
        // Verify the results
    }
    
    @Test
    void testDeleteESUserById() {
        
        when(esUserDAO.delete(anyInt())).thenReturn(1);
        
        // Run the test
        final Tuple2<Result<Void>, ESUserPO> result = esUserService.deleteESUserById(1);
        Assertions.assertTrue(result._1().success());
        // Verify the results
    }
    
    @Test
    void testDeleteByESUsers() {
        // Setup
        // Configure ESUserDAO.listByProjectId(...).
        final List<ESUserPO> esUserPOS = Arrays.asList(
                new ESUserPO(0, 0, "verifyCode", "memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp",
                        "responsible"));
        when(esUserDAO.listByProjectId(0)).thenReturn(esUserPOS);
        
        when(esUserDAO.deleteByProjectId(0)).thenReturn(0);
        
        // Run the test
        esUserService.deleteByESUsers(0);
        
        // Verify the results
    }
    
    @Test
    void testCountByProjectId() {
        // Setup
        when(esUserDAO.countByProjectId(0)).thenReturn(0);
        
        // Run the test
        final int result = esUserService.countByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(0);
    }
    
    @Test
    void testGetEsUserById() {
        final ESUserPO esUserPO = CustomDataSource.esUserPO();
        when(esUserDAO.getByESUser(anyInt())).thenReturn(esUserPO);
        
        // Run the test
        final ESUser result = esUserService.getEsUserById(1);
        
        // Verify the results
        Assertions.assertTrue(Objects.nonNull(result));
    }
    
    @Test
    void testVerifyAppCode() {
        final ESUserPO esUserPO = CustomDataSource.esUserPO();
        when(esUserDAO.getByESUser(anyInt())).thenReturn(esUserPO);
        
        assertThat(Result.buildNotExist("es user 不存在").getMessage()).isEqualTo(
                esUserService.verifyAppCode(null, "verifyCode").getMessage());
         assertThat(Result.buildParamIllegal("校验码错误").getMessage()).isEqualTo(
                esUserService.verifyAppCode(1, "verifyCode1").getMessage());
         assertThat(esUserService.verifyAppCode(1, "verifyCode").success()).isTrue();
         
        
    }
    
    @Test
    void testValidateESUser() {
        
        final ESUserPO esUserPO = CustomDataSource.esUserPO();
        final ESUserDTO esUserDTO = ConvertUtil.obj2Obj(esUserPO, ESUserDTO.class);
        Assertions.assertEquals(Result.buildParamIllegal("应用信息为空").getMessage(),
                esUserService.validateESUser(null, null).getMessage());
        esUserDTO.setMemo(null);
        Assertions.assertEquals(Result.buildParamIllegal("备注为空").getMessage(),
                esUserService.validateESUser(esUserDTO, OperationEnum.ADD).getMessage());
        esUserDTO.setMemo("a");
        esUserDTO.setProjectId(null);
        
        Assertions.assertEquals(Result.buildParamIllegal("项目id为空").getMessage(),
                esUserService.validateESUser(esUserDTO, OperationEnum.ADD).getMessage());
        esUserPO.setProjectId(2);
        when(esUserDAO.getByESUser(anyInt())).thenReturn(esUserPO);
        esUserDTO.setProjectId(1);
        esUserDTO.setId(1);
        Assertions.assertEquals(
                Result.buildParamIllegal(String.format("es user [%s] 已存在", esUserDTO.getId())).getMessage(),
                esUserService.validateESUser(esUserDTO, OperationEnum.ADD).getMessage());
        
        esUserDTO.setId(null);
        Assertions.assertEquals(Result.buildNotExist("es user 不存在").getMessage(),
                esUserService.validateESUser(esUserDTO, OperationEnum.EDIT).getMessage());
        when(esUserDAO.getByESUser(anyInt())).thenReturn(null);
        Assertions.assertEquals(Result.buildNotExist("es user 不存在").getMessage(),
                esUserService.validateESUser(esUserDTO, OperationEnum.EDIT).getMessage());
        esUserPO.setProjectId(2);
        esUserDTO.setId(1);
        esUserPO.setId(1);
        when(esUserDAO.getByESUser(anyInt())).thenReturn(esUserPO);
        esUserDTO.setProjectId(1);
        Assertions.assertEquals(Result.buildParamIllegal(
                        String.format("es user 已经存在在项目[%s],不能为项目[%s]创建,请重新提交es user", esUserPO.getProjectId(),
                                esUserDTO.getProjectId())).getMessage(),
                esUserService.validateESUser(esUserDTO, OperationEnum.EDIT).getMessage());
        esUserPO.setProjectId(1);
        when(esUserDAO.getByESUser(anyInt())).thenReturn(esUserPO);
        esUserDTO.setIsRoot(null);
        Assertions.assertEquals(Result.buildParamIllegal("超管标记非法").getMessage(),
                esUserService.validateESUser(esUserDTO, OperationEnum.EDIT).getMessage());
        esUserDTO.setIsRoot(2);
        Assertions.assertEquals(Result.buildParamIllegal("超管标记非法").getMessage(),
                esUserService.validateESUser(esUserDTO, OperationEnum.EDIT).getMessage());
        esUserDTO.setIsRoot(1);
        esUserDTO.setSearchType(3);
        Assertions.assertEquals(Result.buildParamIllegal("查询模式非法").getMessage(),
                
                esUserService.validateESUser(esUserDTO, OperationEnum.EDIT).getMessage());
        esUserDTO.setSearchType(1);
        esUserDTO.setVerifyCode(null);
        Assertions.assertEquals(Result.buildParamIllegal("校验码不能为空").getMessage(),
                esUserService.validateESUser(esUserDTO, OperationEnum.EDIT).getMessage());
        
        esUserDTO.setVerifyCode("aa");
        Assertions.assertTrue(esUserService.validateESUser(esUserDTO, OperationEnum.EDIT).success());
    }
    
    @Test
    void testGetProjectWithoutCodeApps() {
        final ESUserPO esUserPO = CustomDataSource.esUserPO();
        when(esUserDAO.listByProjectIds(anyList())).thenReturn(Lists.newArrayList(esUserPO));
        final List<ESUser> projectWithoutCodeApps = esUserService.getProjectWithoutCodeApps(1);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projectWithoutCodeApps));
    }
    
    @Test
    void testGetProjectWithoutCodeApps_ESUserDAOReturnsNoItems() {
        // Setup
        when(esUserDAO.listByProjectIds(Arrays.asList(0))).thenReturn(Collections.emptyList());
        
        // Run the test
        final List<ESUser> result = esUserService.getProjectWithoutCodeApps(0);
        
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
    
    @Test
    void testCheckDefaultESUserByProject() {
        // Setup
        when(esUserDAO.countDefaultESUserByProject(0)).thenReturn(0);
        
        // Run the test
        final boolean result = esUserService.checkDefaultESUserByProject(0);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testGetDefaultESUserByProject() {
        // Setup
        final ESUser expectedResult = new ESUser(0, "name", 0, "verifyCode", "departmentId", "department",
                "responsible", "memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp");
        
        // Configure ESUserDAO.getDefaultESUserByProject(...).
        final ESUser esUser = new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible",
                "memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp");
        when(esUserDAO.getDefaultESUserByProject(0)).thenReturn(esUser);
        
        // Run the test
        final ESUser result = esUserService.getDefaultESUserByProject(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}