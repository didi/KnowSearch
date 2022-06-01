package com.didichuxing.datachannel.arius.admin.core.service.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Mockito.when;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ESUserDAO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.elasticsearch.common.recycler.Recycler.V;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional(timeout = 1000)
@Rollback
class ESUserServiceTest extends AriusAdminApplicationTest {
    
    @Mock
    private ESUserDAO mockEsUserDAO;
    
    @Autowired
    private ESUserService esUserService;
    
    @Test
    void testRegisterESUser_testGetEsUserById() {
        // Setup
        final ESUserDTO esUserDTO = new ESUserDTO(0, 0, "verifyCode", "admin", "memo", 0, 0, "cluster", 0, "code", 1);
        
        // Run the test
        final Tuple<Result<Integer>, ESUserPO> result = esUserService.registerESUser(esUserDTO, "operator");
        
        // Verify the results
        Assertions.assertNotNull(result.getV1().getData());
        final ESUser esUser = esUserService.getEsUserById(result.getV1().getData());
        
        Assertions.assertNotNull(esUser);
        
    }
    
    @Test
    void testListESUsers() {
        final ESUserPO esUserPO = new ESUserPO();
        esUserPO.setIsRoot(0);
        esUserPO.setSearchType(AppSearchTypeEnum.TEMPLATE.getCode());
        esUserPO.setVerifyCode(RandomStringUtils.randomAlphabetic(7));
        esUserPO.setMemo("test项目默认的es user");
        esUserPO.setProjectId(1);
        final List<ESUserPO> esUserPOS = Collections.singletonList(esUserPO);
        
        // Configure ESUserDAO.listByProjectIds(...).
        
        when(mockEsUserDAO.listByProjectIds(Collections.singletonList(0))).thenReturn(esUserPOS);
        // Setup
        final List<ESUser> expectedResult = Collections.singletonList(ConvertUtil.obj2Obj(esUserPO, ESUser.class));
        // Run the test
        final List<ESUser> result = esUserService.listESUsers(Collections.singletonList(1));
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testListESUsers_ESUserDAOReturnsNoItems() {
        // Setup
        when(mockEsUserDAO.listByProjectIds(Collections.singletonList(0))).thenReturn(Collections.emptyList());
        
        // Run the test
        final List<ESUser> result = esUserService.listESUsers(Arrays.asList(0));
        
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
    
    @Test
    void testEditUser() {
        // Setup
        final ESUserDTO esUserDTO = new ESUserDTO(0, 0, "verifyCode", "responsible", "memo", 0, 0, "cluster", 0, "code",
                0);
        when(mockEsUserDAO.update(
                new ESUserPO(0, 0, "verifyCode", "memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp",
                        "responsible"))).thenReturn(0);
        
        // Run the test
        final Tuple<Result<Void>, ESUserPO> result = esUserService.editUser(esUserDTO);
        
        // Verify the results
    }
    
    @Test
    void testDeleteESUserById() {
         Mockito.when(esUserService.deleteESUserById(Mockito.anyInt()).getV1()).thenReturn(Result.buildSucc());
    }
    
    @Test
    void testDeleteByESUsers() {
        final Result<Void> objectResult = Result .<Void >buildSucc();
        final Result<Void> v1 = esUserService.deleteByESUsers(Mockito.anyInt()).getV1();
        Assertions.assertEquals(v1,objectResult);
    }
    
    
    @Test
    void testCountByProjectId() {
        // Setup
        when(mockEsUserDAO.countByProjectId(0)).thenReturn(0);
        
        // Run the test
        final int result = esUserService.countByProjectId(0);
        
        // Verify the results
        assertThat(result).isEqualTo(0);
    }
    
    @Test
    void testGetEsUserById() {
        // Setup
        final ESUser expectedResult = new ESUser(0, "name", 0, "verifyCode", "departmentId", "department",
                "responsible", "memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp");
        
        // Configure ESUserDAO.getByESUser(...).
        final ESUserPO esUserPO = new ESUserPO(0, 0, "verifyCode", "memo", 0, 0, "cluster", 0, "dataCenter", 0, false,
                "ip", "indexExp", "responsible");
        when(mockEsUserDAO.getByESUser(0)).thenReturn(esUserPO);
        
        // Run the test
        final ESUser result = esUserService.getEsUserById(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testVerifyAppCode() {
        // Setup
        // Configure ESUserDAO.getByESUser(...).
        final ESUserPO esUserPO = new ESUserPO(0, 0, "verifyCode", "memo", 0, 0, "cluster", 0, "dataCenter", 0, false,
                "ip", "indexExp", "responsible");
        when(mockEsUserDAO.getByESUser(0)).thenReturn(esUserPO);
        
        // Run the test
        final Result<Void> result = esUserService.verifyAppCode(0, "verifyCode");
        
        // Verify the results
    }
    
    @Test
    void testVerifyAppCode_ESUserDAOReturnsNull() {
        // Setup
        when(mockEsUserDAO.getByESUser(0)).thenReturn(null);
        
        // Run the test
        final Result<Void> result = esUserService.verifyAppCode(0, "verifyCode");
        
        // Verify the results
    }
    
    @Test
    void testValidateESUser() {
        // Setup
        final ESUserDTO appDTO = new ESUserDTO(0, 0, "verifyCode", "responsible", "memo", 0, 0, "cluster", 0, "code",
                0);
        
        // Configure ESUserDAO.getByESUser(...).
        final ESUserPO esUserPO = new ESUserPO(0, 0, "verifyCode", "memo", 0, 0, "cluster", 0, "dataCenter", 0, false,
                "ip", "indexExp", "responsible");
        when(mockEsUserDAO.getByESUser(0)).thenReturn(esUserPO);
        
        // Run the test
        final Result<Void> result = esUserService.validateESUser(appDTO, OperationEnum.ADD);
        
        // Verify the results
    }
    
    @Test
    void testGetProjectWithoutCodeApps() {
        // Setup
        final List<ESUser> expectedResult = Arrays.asList(
                new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible", "memo", 0, 0,
                        "cluster", 0, "dataCenter", 0, false, "ip", "indexExp"));
        
        // Configure ESUserDAO.listByProjectIds(...).
        final List<ESUserPO> esUserPOS = Arrays.asList(
                new ESUserPO(0, 0, "verifyCode", "memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp",
                        "responsible"));
        when(mockEsUserDAO.listByProjectIds(Arrays.asList(0))).thenReturn(esUserPOS);
        
        // Run the test
        final List<ESUser> result = esUserService.getProjectWithoutCodeApps(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testGetProjectWithoutCodeApps_ESUserDAOReturnsNoItems() {
        // Setup
        when(mockEsUserDAO.listByProjectIds(Arrays.asList(0))).thenReturn(Collections.emptyList());
        
        // Run the test
        final List<ESUser> result = esUserService.getProjectWithoutCodeApps(0);
        
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
    
    @Test
    void testCheckDefaultESUserByProject() {
        // Setup
        when(mockEsUserDAO.countDefaultESUserByProject(0)).thenReturn(0);
        
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
        when(mockEsUserDAO.getDefaultESUserByProject(0)).thenReturn(esUser);
        
        // Run the test
        final ESUser result = esUserService.getDefaultESUserByProject(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}