package com.didichuxing.datachannel.arius.admin.core.service.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ESUserDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional(timeout = 1000)
@Rollback
class ESUserServiceTest extends AriusAdminApplicationTest {
	@Autowired
	private ESUserService esUserService;
	
	@MockBean
	private ESUserDAO esUserDAO;
	


	
	@Test
	void testListESUsers() {
	
		when(esUserDAO.listByProjectIds(anyList())).thenReturn(Collections.singletonList(CustomDataSource.esUserPO()));
		
		// Run the test
		final List<ESUser> result = esUserService.listESUsers(Collections.singletonList(1));
		
	}
	
	@Test
	void testListESUsers_ESUserDAOReturnsNoItems() {
		// Setup
		when(esUserDAO.listByProjectIds(anyList())).thenReturn(Collections.singletonList(CustomDataSource.esUserPO()));
		
		// Run the test
		final List<ESUser> result = esUserService.listESUsers(Collections.singletonList(1));
		
	
	}
	
	@Test
	void testRegisterESUser() {
		
		Assertions.assertEquals(Result.buildParamIllegal("应用信息为空").getMessage(), esUserService.registerESUser(null,
				"operator").getV1().getMessage());
		ESUserPO esUserPO = CustomDataSource.esUserPO();
		esUserPO.setProjectId(null);
		esUserPO.setId(1);
		Assertions.assertEquals(Result.buildParamIllegal("项目id为空").getMessage(),
				esUserService.registerESUser(
				ConvertUtil.obj2Obj(esUserPO,ESUserDTO.class),
				"operator").getV1().getMessage());
		esUserPO.setProjectId(1);
		esUserPO.setId(1);
		when(esUserDAO.getByESUser(anyInt())).thenReturn(esUserPO);
		Assertions.assertEquals(Result.buildParamIllegal("es user [1] 已存在").getMessage(),
				esUserService.registerESUser(ConvertUtil.obj2Obj(esUserPO, ESUserDTO.class), "operator").getV1()
						.getMessage());
		esUserPO.setId(null);
		when(esUserDAO.getByESUser(anyInt())).thenReturn(esUserPO);
		when(esUserDAO.insert(any())).thenReturn(1);
		Assertions.assertTrue(
				esUserService.registerESUser(ConvertUtil.obj2Obj(esUserPO, ESUserDTO.class), "operator").getV1().success());

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
		final Tuple<Result<Void>, ESUserPO> result = esUserService.editUser(esUserDTO);
		Assertions.assertTrue(result.getV1().success());
		// Verify the results
	}
	
	@Test
	void testDeleteESUserById() {
		
		when(esUserDAO.delete(anyInt())).thenReturn(1);
		
		// Run the test
		final Tuple<Result<Void>, ESUserPO> result = esUserService.deleteESUserById(1);
		Assertions.assertTrue(result.getV1().success());
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
		final Tuple<Result<Void>, List<ESUserPO>> result = esUserService.deleteByESUsers(0);
		
		// Verify the results
	}
	
	@Test
	void testDeleteByESUsers_ESUserDAOListByProjectIdReturnsNoItems() {
		// Setup
		when(esUserDAO.listByProjectId(0)).thenReturn(Collections.emptyList());
		when(esUserDAO.deleteByProjectId(0)).thenReturn(0);
		
		// Run the test
		final Tuple<Result<Void>, List<ESUserPO>> result = esUserService.deleteByESUsers(0);
		
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
		// Setup
		final ESUser expectedResult = new ESUser(0, "name", 0, "verifyCode", "departmentId", "department",
				"responsible", "memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp");
		
		// Configure ESUserDAO.getByESUser(...).
		final ESUserPO esUserPO = new ESUserPO(0, 0, "verifyCode", "memo", 0, 0, "cluster", 0, "dataCenter", 0, false,
				"ip", "indexExp", "responsible");
		when(esUserDAO.getByESUser(0)).thenReturn(esUserPO);
		
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
		when(esUserDAO.getByESUser(0)).thenReturn(esUserPO);
		
		// Run the test
		final Result<Void> result = esUserService.verifyAppCode(0, "verifyCode");
		
		// Verify the results
	}
	
	@Test
	void testVerifyAppCode_ESUserDAOReturnsNull() {
		// Setup
		when(esUserDAO.getByESUser(0)).thenReturn(null);
		
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
		when(esUserDAO.getByESUser(0)).thenReturn(esUserPO);
		
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
		when(esUserDAO.listByProjectIds(Arrays.asList(0))).thenReturn(esUserPOS);
		
		// Run the test
		final List<ESUser> result = esUserService.getProjectWithoutCodeApps(0);
		
		// Verify the results
		assertThat(result).isEqualTo(expectedResult);
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